package com.github.nfmdev.candlestack.generator_service.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateScenarioRequest(
    @NotBlank String name,
    Long seed,
    @NotEmpty List<CreateSymbolRequest> symbols,
    @Valid CreateDeliveryRequest delivery
) {}