package com.github.nfmdev.candlestack.generator_service.runtime.delivery;

import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;

record DeliveryRequest(
    SimulationScenario scenario,
    MarketTradeEvent event,
    SimulationRuntimeState runtimeState,
    int copies,
    long estimatedPayloadBytes
) {
    DeliveryRequest {
        if (scenario == null) {
            throw new IllegalArgumentException(
                "scenario cannot be null"
            );
        }

        if (event == null) {
            throw new IllegalArgumentException(
                "event cannot be null"
            );
        }

        if (runtimeState == null) {
            throw new IllegalArgumentException(
                "runtimeState cannot be null"
            );
        }

        if (copies <= 0) {
            throw new IllegalArgumentException(
                "copies must be greater than zero"
            );
        }

        if (estimatedPayloadBytes < 0) {
            throw new IllegalArgumentException(
                "estimatedPayloadBytes cannot be negative"
            );
        }
    }
}
