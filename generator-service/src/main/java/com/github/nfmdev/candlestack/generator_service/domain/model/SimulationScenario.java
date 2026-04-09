package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationScenario {
    private UUID scenarioId;
    private String name;
    private ScenarioStatus status;
    private List<SymbolConfig> symbols;
    private DeliveryConfig deliveryConfig;
    private Long seed;
    private Instant createdAt;
    private Instant updatedAt;
}