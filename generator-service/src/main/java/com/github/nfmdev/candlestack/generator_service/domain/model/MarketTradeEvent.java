package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record MarketTradeEvent(
     UUID eventId,
     UUID scenarioId,
     String source,
     String eventType,
     String symbol,
     Instant eventTime,
     long sequence,
     BigDecimal price,
     BigDecimal quantity,
     TradeSide side,
     String currency
) {
    public MarketTradeEvent {
        requireNonNull(eventId, "eventId");
        requireNonNull(scenarioId, "scenarioId");
        requireNonBlank(source, "source");
        requireNonBlank(eventType, "eventType");
        requireNonBlank(symbol, "symbol");
        requireNonNull(eventTime, "eventTime");
        requirePositive(sequence, "sequence");
        requirePositive(price, "price");
        requirePositive(quantity, "quantity");
        requireNonNull(side, "side");
        requireNonBlank(currency, "currency");
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value;
    }

    private static long requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
