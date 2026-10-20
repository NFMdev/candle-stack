package com.github.nfmdev.candlestack.generator_service.runtime;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.github.nfmdev.candlestack.generator_service.domain.engine.EventFactory;
import com.github.nfmdev.candlestack.generator_service.domain.engine.RateController;
import com.github.nfmdev.candlestack.generator_service.domain.engine.WeightedRandomSymbolSelector;
import com.github.nfmdev.candlestack.generator_service.domain.engine.fault.FaultDecider;
import com.github.nfmdev.candlestack.generator_service.domain.engine.fault.FaultDecision;
import com.github.nfmdev.candlestack.generator_service.domain.engine.inter.PayloadGenerator;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.ScenarioStatus;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.infrastructure.observability.GeneratorMetrics;
import com.github.nfmdev.candlestack.generator_service.runtime.delivery.StripedDeliveryDispatcher;

public final class ScenarioRunner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScenarioRunner.class);
    private static final long TICK_INTERVAL_MS = 10;

    private final SimulationScenario scenario;
    private final SimulationRuntimeState runtimeState;

    private final EventFactory eventFactory;

    private final Random eventRandom;
    private final Random symbolSelectRandom;
    private final Random faultRandom;

    private final RateController rateController;
    private final WeightedRandomSymbolSelector symbolSelector;
    private final PayloadGenerator payloadGenerator;
    private final FaultDecider faultDecisionEngine;

    private final StripedDeliveryDispatcher deliveryDispatcher;

    private final ScheduledExecutorService generationScheduler;
    private final ScheduledExecutorService completionScheduler;
    private ScheduledFuture<?> generationTask;
    private ScheduledFuture<?> completionTask;

    private final AtomicBoolean terminated = new AtomicBoolean(false);

    private final Runnable onCompleted;

    private final GeneratorMetrics metrics;

    public ScenarioRunner(
        SimulationScenario scenario,
        SimulationRuntimeState runtimeState,
        EventFactory eventFactory,
        PayloadGenerator payloadGenerator,
        StripedDeliveryDispatcher deliveryDispatcher,
        Runnable onCompleted,
        GeneratorMetrics metrics
    ) {
        long seed = resolveSeed(scenario);

        this.scenario = scenario;
        this.runtimeState = runtimeState;
        this.eventFactory = eventFactory;
        this.eventRandom = new Random(seed);
        this.symbolSelectRandom = new Random(seed ^ 0x9E3779B97F4A7C15L);
        this.faultRandom = new Random(seed ^ 0xD1B54A32D192ED03L);
        this.generationScheduler = Executors.newSingleThreadScheduledExecutor();
        this.completionScheduler = Executors.newSingleThreadScheduledExecutor();
        this.rateController = new RateController(scenario.getLoadConfig().eventsPerSecond());
        this.symbolSelector = new WeightedRandomSymbolSelector(scenario.getSymbols(), symbolSelectRandom); 
        this.payloadGenerator = payloadGenerator;
        this.deliveryDispatcher = deliveryDispatcher;
        this.faultDecisionEngine = new FaultDecider(faultRandom);
        this.onCompleted = onCompleted;
        this.metrics = metrics;
    }

    public void start() {
        rateController.start();

        generationTask = generationScheduler.scheduleAtFixedRate(
            this::tick,
            0L,
            TICK_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        scheduleCompletion();
    }

    public void stop() {
        terminate(ScenarioStatus.STOPPED);
    }

    public void complete() {
        boolean completed = terminate(ScenarioStatus.COMPLETED);
        if (completed) onCompleted.run();
    }

    private void tick() {
        if (!runtimeState.isRunning()) {
            return;
        }

        var eventsDue = rateController.eventsDue();
        var generated = 0L;

        for (long i = 0; i < eventsDue; i++) {
            if (!runtimeState.isRunning()) {
                break;
            }

            var symbolConfig = symbolSelector.next();
            emit(symbolConfig);
            generated++;
        }

        rateController.recordGenerated(generated);
    }

    private void emit(SymbolConfig symbolConfig) {
        try {
            FaultDecision decision = faultDecisionEngine.decide(scenario.getFaultConfig());
            Instant eventTime = Instant.now().minus(decision.delay());
            
            MarketTradeEvent event = eventFactory.createTrade(scenario, symbolConfig, runtimeState, eventRandom, payloadGenerator, eventTime);
            runtimeState.incrementEmitted();
            metrics.eventEmitted();

            if (decision.duplicate()) {
                runtimeState.incrementDuplicates();
                metrics.duplicateEvent();
            }
            if (decision.delay().isPositive()) {
                runtimeState.incrementDelayedEvents();
                metrics.delayedEvent();
            }

            var accepted = deliveryDispatcher.dispatch(
                scenario,
                event,
                runtimeState,
                decision.deliveryCount(),
                scenario.getLoadConfig().payloadSizeBytes()
            );

            if (!accepted) {
                log.warn(
                    "Event rejected by delivery dispatcher: scenario={} symbol={} event={}",
                    scenario.getId(),
                    event.symbol(),
                    event.eventId()
                );
            }
        } catch (Exception e) {
            runtimeState.incrementDeliveryFailures();
            log.warn("Failed to emit/deliver event for scenario={} and symbol={}", scenario.getId(), symbolConfig.symbol(), e);
        }
    }

    private long resolveSeed(SimulationScenario scenario) {
        return scenario.getSeed() != null ? scenario.getSeed() : System.nanoTime();
    }

    private void scheduleCompletion() {
        Duration duration = scenario.getLoadConfig().duration();

        if (duration.isZero()) {
            return;
        }

        completionTask = completionScheduler.schedule(
            this::complete,
            duration.toNanos(),
            TimeUnit.NANOSECONDS
        );
    }

    private boolean terminate(ScenarioStatus reason) {
        if (!terminated.compareAndSet(false, true)) {
            return false;
        }

        runtimeState.markDraining();

        if (generationTask != null) {
            generationTask.cancel(false);
        }
        if (completionTask != null && reason != ScenarioStatus.COMPLETED) {
            completionTask.cancel(false);
        }

        generationScheduler.shutdown();

        try {
            if (!generationScheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn(
                    "Timed out waiting for generation to stop: scenario={}",
                    scenario.getId()
                );
                runtimeState.markFailed();
                return true;
            }

            deliveryDispatcher.awaitScenarioDrained(scenario.getId());

            if (reason == ScenarioStatus.COMPLETED) {
                runtimeState.markCompleted();
            } else {
                runtimeState.markStopped();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            runtimeState.markFailed();

            log.warn(
                "Interrupted while terminating scenario={}",
                scenario.getId(),
                e
            );
        } finally {
            generationScheduler.shutdownNow();
            completionScheduler.shutdown();
        }

        return true;
    }
}