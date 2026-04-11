package com.github.nfmdev.candlestack.generator_service.domain.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.TradeSide;

@Component
public class EventFactory {
    private static final BigDecimal MAX_PRICE_DELTA_PERCENT = new BigDecimal("0.0020");
    private static final String SOURCE = "generator-service";
    private static final String EVENT_TYPE = "TRADE";

    public MarketTradeEvent createTrade(
        SimulationScenario scenario,
        SymbolConfig symbolConfig,
        SimulationRuntimeState runtimeState,
        Random random
    ) {
        BigDecimal currentPrice = runtimeState.currenPrice(symbolConfig.symbol());
        if (currentPrice == null) {
            currentPrice = symbolConfig.initialPrice();
        }

        BigDecimal nextPrice = nextPrice(currentPrice, symbolConfig, random);
        BigDecimal quantity = nextQuantity(symbolConfig, random);
        TradeSide side = random.nextBoolean() ? TradeSide.BUY :  TradeSide.SELL;
        long sequence = runtimeState.nextSequence(symbolConfig.symbol());

        runtimeState.updateCurrentPrice(symbolConfig.symbol(), nextPrice);

        return new MarketTradeEvent(
            UUID.randomUUID(),
            scenario.getId(), 
            SOURCE, 
            EVENT_TYPE, 
            SOURCE,
            Instant.now(),
            sequence,
            nextPrice,
            quantity,
            side,
            symbolConfig.currency()
        );
    }

    private BigDecimal nextPrice(BigDecimal currentPrice, SymbolConfig symbolConfig, Random random) {
        double direction = (random.nextDouble() * 0.2) - 0.1;
        BigDecimal delta = MAX_PRICE_DELTA_PERCENT.multiply(BigDecimal.valueOf(direction));
        BigDecimal factor = BigDecimal.ONE.add(delta);

        BigDecimal candidate = currentPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        if (candidate.compareTo(symbolConfig.minPrice()) < 0) {
            return symbolConfig.minPrice().setScale(2, RoundingMode.HALF_UP);
        } else if (candidate.compareTo(symbolConfig.maxPrice()) > 0) {
            return symbolConfig.maxPrice().setScale(2, RoundingMode.HALF_UP);
        } else {
            return candidate;
        }
    }

    private BigDecimal nextQuantity(SymbolConfig symbolConfig, Random random) {
        BigDecimal range = symbolConfig.maxQuantity().subtract(symbolConfig.minQuantity());
        BigDecimal factor = BigDecimal.valueOf(random.nextDouble());
        return symbolConfig.minQuantity()
                .add(range.multiply(factor))
                .setScale(6, RoundingMode.HALF_UP);
    }
}