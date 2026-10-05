package com.github.nfmdev.candlestack.generator_service.domain.model;

public record LoadConfig(
    int eventsPerSecond,
    int payloadSizeBytes,
    double failurePercentage
) {
    public LoadConfig {
        if (eventsPerSecond <= 0) {
            throw new IllegalArgumentException(
                "eventsPerSecond must be greater than 0."
            );
        }
    }
}
