package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public final class SimulationRuntimeState {
    private final UUID scenarioId;
    private final Instant startedAt;
    private volatile ScenarioStatus status = ScenarioStatus.RUNNING;

    private final AtomicLong emitted = new AtomicLong();
    private final AtomicLong enqueued = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong queueRejected = new AtomicLong();
    private final AtomicLong byteBudgetRejected = new AtomicLong();
    private final AtomicLong delivered = new AtomicLong();
    private final AtomicLong deliveryFailures = new AtomicLong(0);
    private final AtomicLong duplicates = new AtomicLong(0);
    private final AtomicLong delayedEvents = new AtomicLong(0);

    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> currentSequences = new ConcurrentHashMap<>();

    public SimulationRuntimeState(UUID scenarioId, Instant startedAt) {
        this.scenarioId = scenarioId;
        this.startedAt = startedAt;
    }

    public void initializeSymbol(String symbol, BigDecimal initialPrice) {
        currentPrices.put(symbol, initialPrice);
        currentSequences.put(symbol, new AtomicLong(0));
    }

    public long nextSequence(String symbol) {
        return currentSequences.computeIfAbsent(symbol, ignored -> new AtomicLong(0)).incrementAndGet();
    }

    public BigDecimal currenPrice(String symbol) {
        return currentPrices.get(symbol);
    }

    public void updateCurrentPrice(String symbol, BigDecimal newPrice) {
        currentPrices.put(symbol, newPrice);
    }

    public void incrementEmitted() {
        emitted.incrementAndGet();
    }

    public void incrementEnqueued() {
        enqueued.incrementAndGet();
    }

    public void incrementRejected() {
        rejected.incrementAndGet();
    }

    public void incrementQueueRejected() {
        queueRejected.incrementAndGet();
    }

    public void incrementByteBudgetRejected() {
        byteBudgetRejected.incrementAndGet();
    }

    public void incrementDelivered() {
        delivered.incrementAndGet();
    }

    public void incrementDeliveryFailures() {
        deliveryFailures.incrementAndGet();
    }

    public void incrementDuplicates() {
        duplicates.incrementAndGet();
    }

    public void incrementDelayedEvents() {
        delayedEvents.incrementAndGet();
    }

    public void markCreated() {
        status = ScenarioStatus.CREATED;
    }
    
    public void markRunning() {
        status = ScenarioStatus.RUNNING;
    }

    public void markDraining() {
        status = ScenarioStatus.DRAINING;
    }

    public void markStopped() {
        status = ScenarioStatus.STOPPED;
    }

    public void markCompleted() {
        status = ScenarioStatus.COMPLETED;
    }

    public void markFailed() {
        status = ScenarioStatus.FAILED;
    }

    public boolean isRunning() {
        return status == ScenarioStatus.RUNNING;
    }

    public String getScenarioStatus() {
        return status.name();
    }

    public UUID getScenarioId() {
        return scenarioId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long getEmitted() {
        return emitted.get();
    }

    public long getEnqueued() {
        return enqueued.get();
    }

    public long getRejected() {
        return rejected.get();
    }

    public long getQueueRejected() {
        return queueRejected.get();
    }

    public long getbyteBudgetRejected() {
        return byteBudgetRejected.get();
    }

    public long getDelivered() {
        return delivered.get();
    }

    public long getDeliveryFailures() {
        return deliveryFailures.get();
    }

    public long getDuplicates() {
        return duplicates.get();
    }

    public long getDelayedEvents() {
        return delayedEvents.get();
    }

    public Map<String, BigDecimal> currentPricesSnapshot() {
        return Map.copyOf(new HashMap<>(currentPrices));
    }

    public Map<String, Long> currentSequencesSnapshot() {
        Map<String, Long> snapshot = new HashMap<>();
        currentSequences.forEach((symbol, seq) -> snapshot.put(symbol, seq.get()));
        return Map.copyOf(snapshot);
    }
}