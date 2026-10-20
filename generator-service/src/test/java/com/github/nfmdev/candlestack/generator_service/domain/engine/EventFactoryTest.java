package com.github.nfmdev.candlestack.generator_service.domain.engine;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.generator_service.domain.engine.fault.FaultConfig;
import com.github.nfmdev.candlestack.generator_service.domain.engine.payload.ReusablePayloadGenerator;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;
import com.github.nfmdev.candlestack.generator_service.domain.model.LoadConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;

class EventFactoryTest {;
    private final EventFactory eventFactory = new EventFactory();

    @Test
    void createTradeShouldGenerateValidEvent() {
        SymbolConfig symbol = symbol();
        SimulationScenario scenario = scenario(symbol);
        SimulationRuntimeState runtimeState = new SimulationRuntimeState(scenario.getId(), Instant.now());
        runtimeState.initializeSymbol(symbol.symbol(), symbol.initialPrice());
        ReusablePayloadGenerator payloadGenerator = ReusablePayloadGenerator.getInstance();

        MarketTradeEvent event = eventFactory.createTrade(scenario, symbol, runtimeState, new Random(42), payloadGenerator, Instant.now());

        assertThat(event.eventId()).isNotNull();
        assertThat(event.scenarioId()).isEqualTo(scenario.getId());
        assertThat(event.eventType()).isEqualTo("TRADE");
        assertThat(event.symbol()).isEqualTo("BTC-USD");
        assertThat(event.sequence()).isEqualTo(1L);
        assertThat(event.price()).isBetween(symbol.minPrice(), symbol.maxPrice());
        assertThat(event.quantity()).isBetween(symbol.minQuantity(), symbol.maxQuantity());
        assertThat(runtimeState.currenPrice("BTC-USD")).isEqualTo(event.price());
    }

    @Test
    void createTradeShouldIncrementSequencePerSymbol() {
        SymbolConfig symbol = symbol();
        SimulationScenario scenario = scenario(symbol);
        SimulationRuntimeState runtimeState = new SimulationRuntimeState(scenario.getId(), Instant.now());
        runtimeState.initializeSymbol(symbol.symbol(), symbol.initialPrice());
        ReusablePayloadGenerator payloadGenerator = ReusablePayloadGenerator.getInstance();

        MarketTradeEvent first = eventFactory.createTrade(scenario, symbol, runtimeState, new Random(7), payloadGenerator, Instant.now());
        MarketTradeEvent second = eventFactory.createTrade(scenario, symbol, runtimeState, new Random(8), payloadGenerator, Instant.now());

        assertThat(first.sequence()).isEqualTo(1L);
        assertThat(second.sequence()).isEqualTo(2L);
    }

    private SymbolConfig symbol() {
        return new SymbolConfig(
            "BTC-USD",
            new BigDecimal("68000.00"),
            new BigDecimal("50000.00"),
            new BigDecimal("90000.00"),
            new BigDecimal("0.010000"),
            new BigDecimal("1.500000"),
            5,
            "USD"
        );
    }

    private SimulationScenario scenario(SymbolConfig symbol) {
        Instant now = Instant.now();
        return new SimulationScenario(
            UUID.randomUUID(),
            "trade-dev",
            List.of(symbol),
            new LoadConfig(1000, 5000, Duration.ZERO),
            new DeliveryConfig(
                DeliveryMode.HTTP,
                "http://localhost:8081",
            "/api/v1/market-events",
                1000,
                2000
            ),
            new FaultConfig(0, 0, Duration.ZERO),
            42L,
            now,
            now
        );
    }
}
