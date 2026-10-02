package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ScenarioSummaryResponse(
    UUID id,
    String name,
    // TODO: How to receive scenario status
    // String status,
    int symbolCount,
    Instant createdAt,
    Instant updatedAt
) {}
