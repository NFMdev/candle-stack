package com.github.nfmdev.candlestack.generator_service.api.dto;

public record DeliveryConfigResponse(
    String mode,
    String ingestionBaseUrl,
    String endpointPath,
    int connectTimeoutMs,
    int readTimeoutMs
) {}
