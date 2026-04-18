package com.github.nfmdev.candlestack.processing_service.domain.validation;

import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;

public class TradeEventValidator {
    public void validate(TradeEvent tradeEvent) {
        requireNonNull(tradeEvent, "tradeEvent");

        requireNonNull(tradeEvent.eventId(), "eventId");
        requireNonNull(tradeEvent.scenarioId(), "scenarioId");
        requireNonBlank(tradeEvent.source(), "source");
        requireNonBlank(tradeEvent.instrumentId(), "instrumentId");
        requireNonNull(tradeEvent.eventTime(), "eventTime");
        requirePositive(tradeEvent.price(), "price");
        requirePositive(tradeEvent.quantity(), "quantity");
        requireNonNull(tradeEvent.side(), "side");
        requireNonBlank(tradeEvent.currency(), "currency");

        if (tradeEvent.sequence() < 0) {
            throw new IllegalArgumentException("sequence must be >= 0");
        }
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must Non be null");
        }
    }

    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must Non be null or blank");
        }
    }

    private void requirePositive(java.math.BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must Non be null");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }
}
