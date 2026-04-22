package com.github.nfmdev.candlestack.processing_service.domain.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeSide;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

class TradeEventValidation {
    private final TradeEventValidator validator = new TradeEventValidator();

    @Test
    void shouldNotThrowExceptionForValidEvent() {
        TradeEvent event = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "USD-BTC",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );

        assertDoesNotThrow(() -> {
            validator.validate(event);
        });
    }

    @Test
    void shouldThrowExceptionForNullEvent() {
        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            validator.validate(null);
        });

        assertThat(exception.getMessage()).isEqualTo("tradeEvent must not be null");
    }

    @Test
    void shouldThrowExceptionForNullEventId() {
        TradeEvent event = new TradeEvent(
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
            validator.validate(event);
        });

        assertThat(exception.getMessage()).isEqualTo("eventId must not be null");
    }

    @Test
    void shouldThrowExceptionForNonPositivePrice() {
        TradeEvent event = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "USD-BTC",
            Instant.now(),
            1L,
            new BigDecimal("-68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );

        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            validator.validate(event);
        });

        assertThat(exception.getMessage()).isEqualTo("price must be > 0");
    }

    @Test
    void shouldThrowExceptionForNonPositiveQuantity() {
        TradeEvent event = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "USD-BTC",
            Instant.now(),
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("-0.250000"),
            TradeSide.BUY,
            "USD"
        );

        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            validator.validate(event);
        });

        assertThat(exception.getMessage()).isEqualTo("quantity must be > 0");
    }

    @Test
    void shouldThrowExceptionForSequenceLowerThanZero() {
        TradeEvent event = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "USD-BTC",
            Instant.now(),
            -1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );

        Exception exception = assertThrows(InvalidTradeEventException.class, () -> {
            validator.validate(event);
        });

        assertThat(exception.getMessage()).isEqualTo("sequence must be >= 0");
    }
}
