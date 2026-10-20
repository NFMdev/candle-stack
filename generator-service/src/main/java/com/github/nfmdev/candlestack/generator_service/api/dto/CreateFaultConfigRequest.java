package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateFaultConfigRequest(
    @Min(0) double duplicateProbability,
    @Min(0) double outOfOrderProbability,
    @NotNull Duration maxDelay
) {
    
}
