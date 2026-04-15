package com.github.nfmdev.candlestack.processing_service.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeEvent(
    UUID eventId,
    UUID scenarioId,
    String source,
    String instrumentId,
    Instant eventTime,
    long sequence,
    BigDecimal price,
    BigDecimal quantity,
    TradeSide side,
    String currency
) {}