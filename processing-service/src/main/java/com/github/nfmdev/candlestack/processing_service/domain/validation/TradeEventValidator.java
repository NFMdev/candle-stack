package com.github.nfmdev.candlestack.processing_service.domain.validation;

import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

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
            throw new InvalidTradeEventException("sequence must be >= 0");
        }
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidTradeEventException(fieldName + " must Non be null");
        }
    }

    private void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidTradeEventException(fieldName + " must Non be null or blank");
        }
    }

    private void requirePositive(java.math.BigDecimal value, String fieldName) {
        if (value == null) {
            throw new InvalidTradeEventException(fieldName + " must Non be null");
        }
        if (value.signum() <= 0) {
            throw new InvalidTradeEventException(fieldName + " must be > 0");
        }
    }
}
