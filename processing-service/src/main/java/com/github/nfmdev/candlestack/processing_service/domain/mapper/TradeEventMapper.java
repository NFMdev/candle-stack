package com.github.nfmdev.candlestack.processing_service.domain.mapper;

import java.util.Objects;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentNormalizer;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

public class TradeEventMapper {
    private final InstrumentNormalizer instrumentNormalizer;

    public TradeEventMapper(InstrumentNormalizer instrumentNormalizer) {
        this.instrumentNormalizer = Objects.requireNonNull(instrumentNormalizer, "instrumentNormalizer cannot be null");
    }

    public TradeEvent toCanonical(MarketTradeEvent event) {
        requireNonNull(event, "event cannot be null");

        if (event.eventType() != null && !event.eventType().isBlank()) {
            String normalizedEventType = instrumentNormalizer.normalizeEventType(event.eventType());
            if (!"TRADE".equals(normalizedEventType)) {
                throw new InvalidTradeEventException("Unsopported eventType for processing-service: " + event.eventType());
            }
        }

        return new TradeEvent(
            requireNonNull(event.eventId(), "eventId"),
            requireNonNull(event.scenarioId(), "scenarioId"),
            instrumentNormalizer.normalizeSource(event.source()),
            instrumentNormalizer.normalizeInstrumentId(event.symbol()),
            requireNonNull(event.eventTime(), "eventTime"),
            event.sequence(),
            requireNonNull(event.price(), "price"),
            requireNonNull(event.quantity(), "quantity"),
            requireNonNull(event.side(), "side"),
            instrumentNormalizer.normalizeCurrency(event.currency())
        );
    }

    private <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new InvalidTradeEventException(fieldName + " must not be null");
        }
        return value;
    }
}