package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ScenarioStatusResponse(
    UUID id,
    // TODO: How to retrieve scenario status
    // String status,
    Instant startedAt,
    long  emittedEvents,
    long deliveredEvents,
    long deliveryFailures,
    Map<String, BigDecimal> currentPrices,
    Map<String, Long> currentSequences
) {
}