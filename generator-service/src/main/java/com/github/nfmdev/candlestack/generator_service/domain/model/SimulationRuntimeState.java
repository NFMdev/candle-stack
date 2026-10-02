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
    private final AtomicLong emittedEvents = new AtomicLong(0);
    private final AtomicLong deliveredEvents = new AtomicLong(0);
    private final AtomicLong deliveryFailures = new AtomicLong(0);
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
        emittedEvents.incrementAndGet();
    }

    public void incrementDelivered() {
        deliveredEvents.incrementAndGet();
    }

    public void incrementFailures() {
        deliveryFailures.incrementAndGet();
    }

    public void markStopped() {
        status = ScenarioStatus.STOPPED;
    }

    public boolean isRunning() {
        return status == ScenarioStatus.RUNNING;
    }

    public UUID getScenarioId() {
        return scenarioId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long getEmittedEvents() {
        return emittedEvents.get();
    }

    public long getDeliveredEvents() {
        return deliveredEvents.get();
    }

    public long getDeliveryFailures() {
        return deliveryFailures.get();
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