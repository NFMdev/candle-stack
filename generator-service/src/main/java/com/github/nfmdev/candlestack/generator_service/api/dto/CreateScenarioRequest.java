package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateScenarioRequest(
    @NotBlank String name,
    Long seed,
    @NotEmpty List<@Valid CreateSymbolRequest> symbols,
    @NotNull @Valid CreateDeliveryRequest delivery
) {}
