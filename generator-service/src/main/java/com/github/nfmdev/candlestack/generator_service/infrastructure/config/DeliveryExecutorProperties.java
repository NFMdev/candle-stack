package com.github.nfmdev.candlestack.generator_service.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "generator.delivery")
public record DeliveryExecutorProperties(
    int lanes,
    int queueCapacityPerLane,
    long maxOutstandingPayloadBytes,
    Duration shutdownTimeout
) {
    public DeliveryExecutorProperties {
        if (lanes <= 0) {
            throw new IllegalArgumentException(
                "lanes must be greater than 0."
            );
        }

        if (queueCapacityPerLane <= 0) {
            throw new IllegalArgumentException(
                "queueCapacityPerLane must be greater than zero"
            );
        }

        if (maxOutstandingPayloadBytes <= 0) {
            throw new IllegalArgumentException(
                "maxOutstandingPayloadBytes must be greater than zero"
            );
        }

        if (shutdownTimeout == null || shutdownTimeout.isNegative()) {
            throw new IllegalArgumentException(
                "shutdownTimeout must be non-negative"
            );
        }
    }
}
