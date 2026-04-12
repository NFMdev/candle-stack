package com.github.nfmdev.candlestack.generator_service.runtime;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private final SimulationScenario scenario;
    private final SimulationRuntimeState runtimeState;
    private final EventFactory eventFactory;
    private final EventDeliveryPort eventDeliveryPort;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private final List<ScheduledFuture<?>> futures = new CopyOnWriteArrayList<>();

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
        this.scheduler = Executors.newScheduledThreadPool(Math.max(1, scenario.getSymbols().size()));
    }

    public void start() {
        for (SymbolConfig symbolConfig : scenario.getSymbols()) {
            long periodMs = Math.max(1L, 1000L / symbolConfig.ticksPerSecond());

            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> emit(symbolConfig),
                0L,
                periodMs,
                TimeUnit.MILLISECONDS
            );
        }
    }

    public void stop() {
        runtimeState.markStopped();
        for (ScheduledFuture<?> future : futures) {
            future.cancel(false);
        }
        scheduler.shutdown();
    }

    private void emit(SymbolConfig symbolConfig) {
        if (!runtimeState.isRunning()) {
            return;
        }

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