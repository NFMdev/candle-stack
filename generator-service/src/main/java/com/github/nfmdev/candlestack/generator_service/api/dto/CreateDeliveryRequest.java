package com.github.nfmdev.candlestack.generator_service.api.dto;

import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeliveryRequest(
    @NotNull DeliveryMode mode,
    @NotBlank String ingestionBaseUrl,
    @NotBlank String endpointPath,
    // Keep timeouts for future use
    @Min(100) int connectTimeoutMs,
    @Min(100) int readTimeoutMs
) {}
