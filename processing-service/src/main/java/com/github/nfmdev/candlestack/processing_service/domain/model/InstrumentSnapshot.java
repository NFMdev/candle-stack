package com.github.nfmdev.candlestack.processing_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record InstrumentSnapshot(
    String instrumentId,
    UUID lastEventId,
    LocalDate tradingDate,
    BigDecimal lastPrice,
    BigDecimal lastQuantity,
    Instant lastEventTime,
    BigDecimal dayVolume,
    BigDecimal dayHigh,
    BigDecimal dayLow,
    long tradeCount,
    Instant updatedAt,
    String currency
) {
    public InstrumentSnapshot {
        requireNonBlank(instrumentId, "instrumentId");
        requireNonNull(lastEventId, "lastEventId");
        requireNonNull(tradingDate, "tradingDate");
        requireNonNull(tradingDate, "tradingDate");
        requirePositive(lastPrice, "lastPrice");
        requirePositive(lastQuantity, "lastQuantity");
        requireNonNull(lastEventTime, "lastEventTime");
        requireNatural(dayVolume, "dayVolume");
        requirePositive(dayHigh, "dayHigh");
        requirePositive(dayLow, "dayLow");
        requireNatural(tradeCount, "tradeCount");
        requireNonNull(updatedAt, "updatedAt");
        requireNonBlank(currency, "currency");

        if (dayHigh.compareTo(dayLow) < 0) {
            throw new IllegalArgumentException("dayHigh must be >= dayLow");
        }
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

    private static long requireNatural(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }

    private static BigDecimal requireNatural(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return value;
    }
}