package com.github.nfmdev.candlestack.generator_service.domain.exception;

public class ScenarioNotFoundException extends RuntimeException {
    public ScenarioNotFoundException(String message) {
        super("Scenario not found: " + message);
    }
}
