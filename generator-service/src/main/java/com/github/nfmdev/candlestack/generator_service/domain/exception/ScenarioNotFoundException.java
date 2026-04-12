package com.github.nfmdev.candlestack.generator_service.domain.exception;

import java.util.UUID;

public class ScenarioNotFoundException extends RuntimeException {
    public ScenarioNotFoundException(UUID id) {
        super("Scenario not found: " + id);
    }
}
