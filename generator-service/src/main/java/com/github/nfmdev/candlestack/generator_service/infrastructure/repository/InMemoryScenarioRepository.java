package com.github.nfmdev.candlestack.generator_service.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.ports.ScenarioRepository;

@Repository
public class InMemoryScenarioRepository implements ScenarioRepository {
    private final ConcurrentMap<UUID, SimulationScenario> storage = new ConcurrentHashMap<>();
    
    @Override
    public SimulationScenario save(SimulationScenario scenario) {
        storage.put(scenario.getId(), scenario);
        return scenario;
    }

    @Override
    public Optional<SimulationScenario> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<SimulationScenario> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }
}