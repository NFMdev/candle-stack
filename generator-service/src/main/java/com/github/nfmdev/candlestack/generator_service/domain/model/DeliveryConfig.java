package com.github.nfmdev.candlestack.generator_service.domain.model;

public record DeliveryConfig (
     DeliveryMode mode,
     String ingestionBaseUrl,
     String endpointPath,
     int connectTimeoutMs,
     int readTimeoutMs
) {
    public DeliveryConfig {
        requireNonBlank(ingestionBaseUrl, "ingestionBaseUrl");
        requireNonBlank(endpointPath, "endpointPath");
        if (mode == null) {
            throw new IllegalArgumentException("Delivery mode cannot be null");
        }
        if (!endpointPath.startsWith("/")) {
            throw new IllegalArgumentException("Endpoint path must start with '/'");
        }
        if (connectTimeoutMs < 100) {
            throw new IllegalArgumentException("Connect timeout must be at least 100 ms");
        }
        if (readTimeoutMs < 100) {
            throw new IllegalArgumentException("Read timeout must be at least 100 ms");
        }
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value;
    }
}