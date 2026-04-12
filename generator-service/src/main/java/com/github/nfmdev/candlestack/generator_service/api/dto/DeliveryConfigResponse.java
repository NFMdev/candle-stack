package com.github.nfmdev.candlestack.generator_service.api.dto;

public record DeliveryConfigResponse(
    String mode,
    String ingestionBaseUrl,
    String endpointPath,
    // Keep timeouts for future use
    int connectTimeoutMs,
    int readTimeoutMs
) {}
