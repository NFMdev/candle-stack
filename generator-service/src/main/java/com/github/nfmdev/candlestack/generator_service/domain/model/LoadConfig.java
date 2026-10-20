package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.time.Duration;

public record LoadConfig(
    int eventsPerSecond,
    int payloadSizeBytes,
    Duration duration
) {
    public LoadConfig {
        if (eventsPerSecond <= 0) {
            throw new IllegalArgumentException(
                "eventsPerSecond must be greater than zero"
            );
        }

        if (payloadSizeBytes < 0) {
            throw new IllegalArgumentException(
                "payloadSizeBytes cannot be negative"
            );
        }

        if (duration == null) {
            throw new IllegalArgumentException(
                "duration cannot be null"
            );
        }

        if (duration.isNegative()) {
            throw new IllegalArgumentException(
                "duration cannot be negative"
            );
        }
    }

    public boolean unlimited() {
        return duration.isZero();
    }
}
