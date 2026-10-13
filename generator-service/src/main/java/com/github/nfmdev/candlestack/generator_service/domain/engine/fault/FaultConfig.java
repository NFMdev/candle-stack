package com.github.nfmdev.candlestack.generator_service.domain.engine.fault;

import java.time.Duration;

public record FaultConfig(
        double duplicateProbability,
        double outOfOrderProbability,
        Duration maxDelay
        ) {

    public FaultConfig {
        validateProbability(
            "duplicateProbability",
            duplicateProbability
        );

        validateProbability(
            "outOfOrderProbability",
            outOfOrderProbability
        );

        if (maxDelay == null) {
            throw new IllegalArgumentException(
                "maxLateness cannot be null"
            );
        }

        if (maxDelay.isNegative()) {
            throw new IllegalArgumentException(
                "maxLateness cannot be negative"
            );
        }

        if (outOfOrderProbability > 0.0
                && maxDelay.isZero()) {
            throw new IllegalArgumentException(
                "maxLateness must be greater than zero " +
                "when outOfOrderProbability > 0"
            );
        }
    }

    public static FaultConfig none() {
        return new FaultConfig(
            0.0,
            0.0,
            Duration.ZERO
        );
    }

    private static void validateProbability(
        String name,
        double probability
    ) {
        if (!Double.isFinite(probability)) {
            throw new IllegalArgumentException(
                name + " must be finite"
            );
        }

        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                name + " must be between 0.0 and 1.0"
            );
        }
    }
}
