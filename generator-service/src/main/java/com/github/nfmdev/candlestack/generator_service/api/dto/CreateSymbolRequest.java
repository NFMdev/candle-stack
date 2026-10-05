package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSymbolRequest(
    @NotBlank String symbol,
    @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal initialPrice,
    @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal minPrice,
    @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal maxPrice,
    @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal minQuantity,
    @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal maxQuantity,
    @DecimalMin(value="0.1", inclusive=true) long loadWeight,
    @NotBlank String currency
) {}
