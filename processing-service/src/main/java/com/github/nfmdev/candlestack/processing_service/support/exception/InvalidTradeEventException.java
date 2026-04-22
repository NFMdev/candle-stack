package com.github.nfmdev.candlestack.processing_service.support.exception;

public class InvalidTradeEventException extends RuntimeException {
    public InvalidTradeEventException(String mesesage) {
        super(mesesage);
    }

    public InvalidTradeEventException(String message, Throwable cause) {
        super(message, cause);
    }
}