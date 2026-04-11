package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ScenarioSummaryResponse(
    UUID id,
    String name,
    String status,
    int symbolCount,
    Instant createdAt,
    Instant updatedAt
) {}
