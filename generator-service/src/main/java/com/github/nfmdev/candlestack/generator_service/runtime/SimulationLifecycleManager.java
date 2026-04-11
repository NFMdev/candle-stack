package com.github.nfmdev.candlestack.generator_service.runtime;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.engine.EventFactory;
import com.github.nfmdev.candlestack.generator_service.domain.exception.InvalidScenarioStateException;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;

import jakarta.annotation.PreDestroy;

@Component
public class SimulationLifecycleManager {
    private final EventFactory eventFactory;
    private final EventDeliveryPort deliveryPort;

    private final Map<UUID, ScenarioRunner> runners = new ConcurrentHashMap<>();
    private final Map<UUID, SimulationRuntimeState> runtimeStates = new ConcurrentHashMap<>();

    public SimulationLifecycleManager(EventFactory eventFactory, EventDeliveryPort deliveryPort) {
        this.eventFactory = eventFactory;
        this.deliveryPort = deliveryPort;
    }

    public synchronized SimulationRuntimeState start(SimulationScenario scenario) {
        UUID scenarioId = scenario.getId();
        if (runners.containsKey(scenarioId)) {
            throw new InvalidScenarioStateException("Scenario is already running");
        }

        scenario.start();

        SimulationRuntimeState runtimeState = new SimulationRuntimeState(scenarioId, Instant.now());
        scenario.getSymbols().forEach(symbol -> runtimeState.initializeSymbol(symbol.symbol(), symbol.initialPrice()));

        ScenarioRunner runner = new ScenarioRunner(scenario, runtimeState, eventFactory, deliveryPort);
        runtimeStates.put(scenarioId, runtimeState);
        runners.put(scenarioId, runner);

        runner.start();
        return runtimeState;
    }

    public synchronized void stop(SimulationScenario scenario) {
        UUID scenarioId = scenario.getId();
        ScenarioRunner runner = runners.remove(scenarioId);

        if (runner == null) {
            throw new InvalidScenarioStateException("Scenario is not running");
        }

        runner.stop();
        scenario.stop();
    }

    public Optional<SimulationRuntimeState> runtimeState(UUID scenarioId) {
        return Optional.ofNullable(runtimeStates.get(scenarioId));
    }

    @PreDestroy
    public void shutdownAll() {
        runners.values().forEach(ScenarioRunner::stop);
        runners.clear();
    }


}