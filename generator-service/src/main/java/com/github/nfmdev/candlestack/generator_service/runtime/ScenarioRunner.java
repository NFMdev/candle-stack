package com.github.nfmdev.candlestack.generator_service.runtime;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.github.nfmdev.candlestack.generator_service.domain.engine.EventFactory;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;

public final class ScenarioRunner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScenarioRunner.class);
    private static final long TICK_INTERVAL_MS = 10;

    private final SimulationScenario scenario;
    private final SimulationRuntimeState runtimeState;
    private final EventFactory eventFactory;
    private final EventDeliveryPort eventDeliveryPort;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private final RateController rateController;
    private final WeightedRandomSymbolSelector symbolSelector;
    private ScheduledFuture<?> generationTask;

    public ScenarioRunner(
        SimulationScenario scenario,
        SimulationRuntimeState runtimeState,
        EventFactory eventFactory,
        EventDeliveryPort eventDeliveryPort
    ) {
        this.scenario = scenario;
        this.runtimeState = runtimeState;
        this.eventFactory = eventFactory;
        this.eventDeliveryPort = eventDeliveryPort;
        this.random = new Random(resolveSeed(scenario));
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.rateController = new RateController(scenario.getLoadConfig().eventsPerSecond());
        this.symbolSelector = new WeightedRandomSymbolSelector(scenario.getSymbols(), random); 
    }

    public void start() {
        rateController.start();

        generationTask = scheduler.scheduleAtFixedRate(
            this::tick,
            0L,
            TICK_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        runtimeState.markStopped();
        if (generationTask != null) {
            generationTask.cancel(false);
        }
        scheduler.shutdown();
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
            generated ++;
        }

        rateController.recordGenerated(generated);
    }

    private void emit(SymbolConfig symbolConfig) {
        try {
            MarketTradeEvent event = eventFactory.createTrade(scenario, symbolConfig, runtimeState, random);
            runtimeState.incrementEmitted();

            eventDeliveryPort.deliver(scenario, event);
            runtimeState.incrementDelivered();
        } catch (Exception e) {
            runtimeState.incrementFailures();
            log.warn("Failed to emit/deliver event for scenario={} and symbol={}", scenario.getId(), symbolConfig.symbol(), e);
        }
    }

    private long resolveSeed(SimulationScenario scenario) {
        return scenario.getSeed() != null ? scenario.getSeed() : System.nanoTime();
    }
}