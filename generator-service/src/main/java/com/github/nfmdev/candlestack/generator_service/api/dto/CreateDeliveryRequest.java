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
    @Min(100) Integer connectTimeoutMs,
    @Min(100) Integer readTimeoutMs
) {
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 1_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 2_000;

    public CreateDeliveryRequest {
        if (connectTimeoutMs == null) {
            connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
        }
        if (readTimeoutMs == null) {
            readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
        }
    }
}
