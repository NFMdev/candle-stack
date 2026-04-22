package com.github.nfmdev.candlestack.processing_service.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
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
) {}
