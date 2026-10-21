package com.github.nfmdev.candlestack.generator_service.runtime.delivery;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;
import com.github.nfmdev.candlestack.generator_service.infrastructure.config.DeliveryExecutorProperties;
import com.github.nfmdev.candlestack.generator_service.infrastructure.observability.GeneratorMetrics;

import jakarta.annotation.PreDestroy;

@Component
public final class StripedDeliveryDispatcher {
    private static final Logger log = LoggerFactory.getLogger(
                    StripedDeliveryDispatcher.class
            );

    private final EventDeliveryPort eventDeliveryPort;
    private final DeliveryExecutorProperties properties;

    private final ThreadPoolExecutor[] lanes;

    private final AtomicLong outstandingPayloadBytes = new AtomicLong();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final ConcurrentHashMap<UUID, ScenarioDeliveryTracker> scenarioTrackers = new ConcurrentHashMap<>();

    private final GeneratorMetrics metrics;

    public StripedDeliveryDispatcher(
            EventDeliveryPort eventDeliveryPort,
            DeliveryExecutorProperties properties,
            GeneratorMetrics metrics
    ) {
        this.eventDeliveryPort = eventDeliveryPort;
        this.properties = properties;
        this.metrics = metrics;

        this.lanes = new ThreadPoolExecutor[properties.lanes()];

        for (int i = 0; i < lanes.length; i++) {
            lanes[i] = createLane(i);
        }
    }

    public boolean dispatch(
            SimulationScenario scenario,
            MarketTradeEvent event,
            SimulationRuntimeState runtimeState,
            int copies,
            long estimatedPayloadBytes
    ) {
        if (shuttingDown.get()) {
            runtimeState.incrementRejected();
            metrics.deliveryRejected();

            log.warn(
                    "Delivery rejected because dispatcher is shutting down: scenario={} event={}",
                    scenario.getId(),
                    event.eventId()
            );

            return false;
        }

        if (!reserveBytes(estimatedPayloadBytes)) {
            runtimeState.incrementRejected();
            runtimeState.incrementByteBudgetRejected();
            metrics.deliveryRejected();
            metrics.byteBudgetRejected();

            log.warn(
                    "Delivery rejected due to outstanding payload byte limit: scenario={} event={} outstandingBytes={} requestedBytes={}",
                    scenario.getId(),
                    event.eventId(),
                    outstandingPayloadBytes.get(),
                    estimatedPayloadBytes
            );

            return false;
        }

        UUID scenarioId = scenario.getId();

        ScenarioDeliveryTracker tracker = scenarioTrackers.computeIfAbsent(
                scenarioId,
                ignored -> new ScenarioDeliveryTracker()
        );

        tracker.increment();

        DeliveryRequest request = new DeliveryRequest(
                scenario,
                event,
                runtimeState,
                copies,
                estimatedPayloadBytes
        );

        int laneIndex = laneFor(
                scenarioId,
                event.symbol()
        );

        try {
            long enqueuedAtNanos = System.nanoTime();
            lanes[laneIndex].execute(
                    () -> executeDelivery(
                            request,
                            tracker,
                            enqueuedAtNanos
                    )
            );

            runtimeState.incrementEnqueued();
            metrics.eventEnqueued();

            return true;

        } catch (RejectedExecutionException e) {
            tracker.decrement();

            releaseBytes(
                    estimatedPayloadBytes
            );

            runtimeState.incrementRejected();
            runtimeState.incrementQueueRejected();
            metrics.deliveryRejected();
            metrics.queueRejected();

            log.warn(
                    "Delivery queue full: scenario={} symbol={} event={} lane={} queueDepth={}",
                    scenarioId,
                    event.symbol(),
                    event.eventId(),
                    laneIndex,
                    lanes[laneIndex]
                            .getQueue()
                            .size()
            );

            return false;
        }
    }

    public void awaitScenarioDrained(UUID scenarioId) throws InterruptedException {
        ScenarioDeliveryTracker tracker = scenarioTrackers.get(scenarioId);
        if (tracker == null) return;

        tracker.awaitEmpty();

        scenarioTrackers.remove(
                scenarioId,
                tracker
        );
    }

    public long pendingForScenario(UUID scenarioId) {
        ScenarioDeliveryTracker tracker = scenarioTrackers.get(scenarioId);
        return tracker == null ? 0L : tracker.pending();
    }

    public long totalQueueDepth() {
        long total = 0L;
        for (ThreadPoolExecutor lane : lanes) {
            total += lane.getQueue().size();
        }

        return total;
    }

    public int activeWorkers() {
        int total = 0;
        for (ThreadPoolExecutor lane : lanes) {
            total += lane.getActiveCount();
        }

        return total;
    }

    public long outstandingPayloadBytes() {
        return outstandingPayloadBytes.get();
    }

    public int queueDepth(
            int lane
    ) {
        return lanes[lane]
                .getQueue()
                .size();
    }

    public int lanes() {
        return lanes.length;
    }

    public int workerCapacity() {
        return lanes.length;
    }

    public long totalQueueCapacity() {
        return (long) lanes.length * properties.queueCapacityPerLane();
    }

    public long maxOutstandingPayloadBytes() {
        return properties.maxOutstandingPayloadBytes();
    }

    private void executeDelivery(
        DeliveryRequest request,
        ScenarioDeliveryTracker tracker,
        long enqueuedAtNanos
    ) {
        var queueWaitNanos = System.nanoTime() - enqueuedAtNanos;
        metrics.recordQueueWaitDuration(queueWaitNanos);

        try {
            for (int i = 0; i < request.copies(); i++) {
                deliverOnce(request);
            }

        } finally {
            releaseBytes(request.estimatedPayloadBytes());

            tracker.decrement();
        }
    }

    private void deliverOnce(DeliveryRequest request) {
        long startedAtNanos = System.nanoTime();

        try {
            eventDeliveryPort.deliver(
                    request.scenario(),
                    request.event()
            );

            request.runtimeState().incrementDelivered();
            metrics.deliverySucceeded();

        } catch (Exception e) {
            request.runtimeState().incrementDeliveryFailures();
            metrics.deliveryFailed();

            log.warn(
                    "HTTP event delivery failed: scenario={} symbol={} event={}",
                    request.scenario().getId(),
                    request.event().symbol(),
                    request.event().eventId(),
                    e
            );
        } finally {
            long durationNanos = System.nanoTime() - startedAtNanos;
            metrics.recordDeliveryDuration(durationNanos);
        }
    }

    private int laneFor(UUID scenarioId, String symbol) {
        int hash = 31 * scenarioId.hashCode() + symbol.hashCode();

        return Math.floorMod(
                hash,
                lanes.length
        );
    }

    private ThreadPoolExecutor createLane(int laneIndex) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                        1,
                        1,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(
                                properties.queueCapacityPerLane()
                        ),
                        threadFactory(laneIndex),
                        new ThreadPoolExecutor.AbortPolicy()
                );
        executor.prestartCoreThread();

        return executor;
    }

    private ThreadFactory threadFactory(int laneIndex) {
        return runnable -> {
            Thread thread = new Thread(
                            runnable,
                            "candle-delivery-lane-"
                            + laneIndex
                    );

            thread.setDaemon(false);

            return thread;
        };
    }

    private boolean reserveBytes(long bytes) {
        while (true) {
            long current = outstandingPayloadBytes.get();

            long next = current + bytes;

            if (next < current) {
                // overflow protection
                return false;
            }

            if (next > properties.maxOutstandingPayloadBytes()) {
                return false;
            }

            if (outstandingPayloadBytes.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    private void releaseBytes(long bytes) {
        long remaining = outstandingPayloadBytes.addAndGet(-bytes);
        if (remaining < 0) {
            throw new IllegalStateException(
                    "Outstanding payload bytes became negative"
            );
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }

        for (ThreadPoolExecutor lane : lanes) {
            lane.shutdown();
        }

        Duration timeout = properties.shutdownTimeout();

        long deadline = System.nanoTime() + timeout.toNanos();

        for (ThreadPoolExecutor lane : lanes) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0L) {
                break;
            }

            try {
                lane.awaitTermination(
                        remainingNanos,
                        TimeUnit.NANOSECONDS
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        for (ThreadPoolExecutor lane : lanes) {
            if (!lane.isTerminated()) {
                lane.shutdownNow();
            }
        }
    }

    // METRICS
    public int activeWorkers(int lane) {
        return lanes[lane].getActiveCount();
    }

    public int queueCapacity(int lane) {
        return properties.queueCapacityPerLane();
    }
}
