package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.math.BigDecimal;

public record SymbolConfigResponse(
    String symbol,
    BigDecimal initialPrice,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    BigDecimal minQuantity,
    BigDecimal maxQuantity,
    int ticksPerSecond,
    String currency
) {}
