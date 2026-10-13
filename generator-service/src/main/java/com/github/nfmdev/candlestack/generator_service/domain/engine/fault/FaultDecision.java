package com.github.nfmdev.candlestack.generator_service.domain.engine.fault;

import java.time.Duration;

public record FaultDecision(
        boolean duplicate,
        Duration delay
        ) {

    public FaultDecision {
        if (delay == null) {
            throw new IllegalArgumentException(
                    "delay cannot be null"
            );
        }

        if (delay.isNegative()) {
            throw new IllegalArgumentException(
                    "delay cannot be negative"
            );
        }
    }

    public static FaultDecision none() {
        return new FaultDecision(
                false,
                Duration.ZERO
        );
    }

    public boolean late() {
        return !delay.isZero();
    }

    public int deliveryCount() {
        return duplicate ? 2 : 1;
    }
}
