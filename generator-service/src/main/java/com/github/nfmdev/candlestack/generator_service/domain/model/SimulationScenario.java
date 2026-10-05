package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SimulationScenario {
    private final UUID id;
    private final String name;
    private final List<SymbolConfig> symbols;
    private final LoadConfig loadConfig;
    private final DeliveryConfig deliveryConfig;
    private final Long seed;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    
    public SimulationScenario(
        UUID id,
        String name,
        List<SymbolConfig> symbols,
        LoadConfig loadProfile,
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
        this.loadConfig = Objects.requireNonNull(loadProfile, "load profile must not be null");
        this.deliveryConfig = Objects.requireNonNull(deliveryConfig, "delivery must not be null");
        this.seed = seed;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<SymbolConfig> getSymbols() {
        return symbols;
    }

    public LoadConfig getLoadConfig() {
        return loadConfig;
    }

    public DeliveryConfig getDeliveryConfig() {
        return deliveryConfig;
    }

    public Long getSeed() {
        return seed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}