package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLoadConfigRequest(
    @Positive int eventsPerSecond,
    @Min(0) int payloadSizeBytes,
    @NotNull Duration duration
) {
}
