package com.github.nfmdev.candlestack.generator_service.domain.ports;

import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;

public interface EventDeliveryPort {
    void deliver(SimulationScenario scenario, MarketTradeEvent event);
}