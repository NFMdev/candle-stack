package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.github.nfmdev.candlestack.generator_service.domain.exception.InvalidScenarioStateException;

import lombok.Data;

@Data
public final class SimulationScenario {
    private final UUID id;
    private final String name;
    private volatile ScenarioStatus status;
    private final List<SymbolConfig> symbols;
    private final DeliveryConfig deliveryConfig;
    private final Long seed;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    
    public SimulationScenario(
        UUID id,
        String name,
        List<SymbolConfig> symbols,
        DeliveryConfig deliveryConfig,
        Long seed,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("symbols cannot be null or empty");
        }

        this.name = name;
        this.symbols = symbols;
        this.deliveryConfig = Objects.requireNonNull(deliveryConfig, "delivery must not be null");
        this.seed = seed;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.status = ScenarioStatus.STOPPED;
    }
    
    public synchronized void start() {
        if (status == ScenarioStatus.RUNNING) {
            throw new InvalidScenarioStateException("Scenario is already running");
        }
        this.status = ScenarioStatus.RUNNING;
        this.updatedAt = Instant.now();
    }

    public synchronized void stop() {
        if (status == ScenarioStatus.STOPPED) {
            throw new InvalidScenarioStateException("Scenario is already stopped");
        }
        this.status = ScenarioStatus.STOPPED;
        this.updatedAt = Instant.now();
    }
}