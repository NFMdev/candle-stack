package com.github.nfmdev.candlestack.generator_service.domain.engine.fault;

import java.time.Duration;
import java.util.Random;

public final class FaultDecider {
    
    private final Random random;

    public FaultDecider(Random random) {
        this.random = random;
    }

    public FaultDecision decide(FaultConfig config) {
        boolean duplicate =
            occurs(config.duplicateProbability());

        Duration lateness =
            occurs(config.outOfOrderProbability())
                ? randomDelay(config.maxDelay())
                : Duration.ZERO;

        return new FaultDecision(
            duplicate,
            lateness
        );
    }

    private boolean occurs(double probability) {
        return random.nextDouble() < probability;
    }

    private Duration randomDelay(Duration max) {
        var maxMs = max.toMillis();

        if (maxMs <= 1L) {
            return max;
        }

        long delayMs = random.nextLong(
            1L,
            maxMs + 1
        );

        return Duration.ofMillis(delayMs);
    }
}
