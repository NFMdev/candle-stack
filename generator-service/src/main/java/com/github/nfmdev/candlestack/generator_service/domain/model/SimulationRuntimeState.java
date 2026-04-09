package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRuntimeState {
    private UUID scenarioId;
    private boolean running;
    private Instant startedAt;
    private long emittedEvents;
    private long deliveredEvents;
    private long deliveryFailures;
    private Map<String, BigDecimal> currentPrices;
    private Map<String, Long> currentSequences;
}