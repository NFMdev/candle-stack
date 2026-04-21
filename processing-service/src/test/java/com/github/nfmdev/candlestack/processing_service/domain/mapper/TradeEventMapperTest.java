package com.github.nfmdev.candlestack.processing_service.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeSide;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentNormalizer;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

class TradeEventMapperTest {
    private TradeEventMapper tradeEventMapper;

    @BeforeEach
    void setUp() {
        tradeEventMapper = new TradeEventMapper(new InstrumentNormalizer());
    }

    @Test
    void testToCanonical() {
        MarketTradeEvent event = new MarketTradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "TRADE",
            "USD-BTC",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );

        TradeEvent result = tradeEventMapper.toCanonical(event);

        assertThat(result.eventId()).isEqualTo(event.eventId());
        assertThat(result.scenarioId()).isEqualTo(event.scenarioId());
        assertThat(result.source()).isEqualTo("generator-service");
        assertThat(result.instrumentId()).isEqualTo("USD-BTC");
        assertThat(result.eventTime()).isEqualTo(event.eventTime());
        assertThat(result.sequence()).isEqualTo(event.sequence());
        assertThat(result.price()).isEqualTo(event.price());
        assertThat(result.quantity()).isEqualTo(event.quantity());
        assertThat(result.side()).isEqualTo(TradeSide.BUY);
        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void shouldReturnNormalizedValues() {
        MarketTradeEvent event = new MarketTradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            " generator-service ",
            "TRADE",
            "USD BTC",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            " usd "
        );

        TradeEvent result = tradeEventMapper.toCanonical(event);

        assertThat(result.source()).isEqualTo("generator-service");
        assertThat(result.instrumentId()).isEqualTo("USD-BTC");
        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void shouldThrowExceptionForUnsupportedEventType() {
        MarketTradeEvent event = new MarketTradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "TEST",
            "USD-BTC",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );

        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            tradeEventMapper.toCanonical(event);
        });

        assertThat(exception.getMessage()).isEqualTo("Unsopported eventType for processing-service: TEST");
    }

    @Test
    void shouldThrowExceptionForNullEventId() {
        MarketTradeEvent event = new MarketTradeEvent(
            null,
            null,
            null,
            null,
            null,
            null,
            1L,
            null,
            null,
            null,
            null
        );

        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            tradeEventMapper.toCanonical(event);
        });

        assertThat(exception.getMessage()).isEqualTo("eventId must not be null");
    }
}
