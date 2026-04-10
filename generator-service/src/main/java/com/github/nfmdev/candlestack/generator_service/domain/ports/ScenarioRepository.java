package com.github.nfmdev.candlestack.generator_service.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;

public interface ScenarioRepository {
    SimulationScenario save(SimulationScenario scenario);
    Optional<SimulationScenario> findById(UUID scenarioId);
    List<SimulationScenario> findAll();
    void deleteById(UUID scenarioId);
}