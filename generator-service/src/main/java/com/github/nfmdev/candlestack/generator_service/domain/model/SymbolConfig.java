package com.github.nfmdev.candlestack.generator_service.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SymbolConfig (
    String symbol,
    BigDecimal initialPrice,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    BigDecimal minQuantity,
    BigDecimal maxQuantity,
    int ticksPerSecond,
    String currency
) {
    public SymbolConfig {
        requireNonBlank(symbol, "symbol");
        requireNonBlank(currency, "currency");
        requirePositive(initialPrice, "initialPrice");
        requirePositive(minPrice, "minPrice");
        requirePositive(maxPrice, "maxPrice");
        requirePositive(minQuantity, "minQuantity");
        requirePositive(maxQuantity, "maxQuantity");
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
        if (minQuantity.compareTo(maxQuantity) > 0) {
            throw new IllegalArgumentException("minQuantity cannot be greater than maxQuantity");
        }
        if (initialPrice.compareTo(minPrice) < 0 || initialPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("initialPrice must be between minPrice and maxPrice");
        }
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("ticksPerSecond must be greater than 0");
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

    private static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}