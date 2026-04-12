package com.github.nfmdev.candlestack.generator_service.domain.exception;

public class InvalidScenarioStateException extends RuntimeException {
    public InvalidScenarioStateException(String message) {
        super("Invalid scenario state: " + message);
    }
}
