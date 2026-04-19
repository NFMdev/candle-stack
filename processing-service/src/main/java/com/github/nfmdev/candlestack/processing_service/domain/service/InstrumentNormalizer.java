package com.github.nfmdev.candlestack.processing_service.domain.service;

import java.util.Locale;

import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;

public class InstrumentNormalizer {

    public String normalizeInstrumentId(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidTradeEventException("symbol must not be null or blank");
        }

        return symbol.trim()
                .toUpperCase(Locale.ROOT)
                .replace('/', '-')
                .replace(' ', '-');
    }

    public String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new InvalidTradeEventException("currency must not be null or blank");
        }

        return currency.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            throw new InvalidTradeEventException("source must not be null or blank");
        }

        return source.trim();
    }

    public String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new InvalidTradeEventException("eventType must not be null or blank");
        }

        return eventType.trim().toUpperCase(Locale.ROOT);
    }
}
