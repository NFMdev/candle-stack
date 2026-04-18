package com.github.nfmdev.candlestack.processing_service.domain.mapper;

import java.util.Locale;
import java.util.Objects;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;

public class TradeEventMapper {
    

    public TradeEvent toCanonical(MarketTradeEvent event) {
        Objects.requireNonNull(event, "event cannot be null");

        if (event.eventType() != null && !event.eventType().isBlank()) {
            String normalizedEventType = event.eventType().trim().toUpperCase(Locale.ROOT);
            if (!"TRADE".equals(normalizedEventType)) {
                throw new IllegalArgumentException("Unsopported eventType for processing-service: " + event.eventType());
            }
        }

        return new TradeEvent(
            event.eventId(),
            event.scenarioId(),
            normalizeText(event.source()),
            normalizeInstrumentId(event.symbol()),
            event.eventTime(),
            event.sequence(),
            event.price(),
            event.quantity(),
            event.side(),
            normalizeCurrency(event.currency())
        );
    }

    private String normalizeInstrumentId(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol cannot be null or blank");
        }

        return symbol.trim().toUpperCase(Locale.ROOT).replace('/', '-').replace(' ', '-');
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency cannot be null or blank");
        }

        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}