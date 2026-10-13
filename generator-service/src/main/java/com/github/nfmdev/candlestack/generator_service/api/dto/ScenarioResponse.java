package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScenarioResponse(
    UUID id,
    String name,
    String status,
    Long seed,
    List<SymbolConfigResponse> symbols,
    DeliveryConfigResponse delivery,
    Instant createdAt,
    Instant updatedAt
) {}