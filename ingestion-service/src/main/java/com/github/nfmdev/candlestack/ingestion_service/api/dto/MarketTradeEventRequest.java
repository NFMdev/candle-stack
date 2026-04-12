package com.github.nfmdev.candlestack.ingestion_service.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.github.nfmdev.candlestack.ingestion_service.domain.model.TradeSide;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MarketTradeEventRequest(
    @NotNull UUID eventId,
    @NotNull UUID scenarioId,
    @NotBlank String source,
    @NotBlank String eventType,
    @NotBlank String symbol,
    @NotNull Instant eventTime,
    @Positive long sequence,
    @NotNull @Positive BigDecimal price,
    @NotNull @Positive BigDecimal quantity,
    @NotNull TradeSide side,
    @NotBlank String currency
) {}
