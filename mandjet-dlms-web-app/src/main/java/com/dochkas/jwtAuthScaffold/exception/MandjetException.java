package com.dochkas.jwtAuthScaffold.exception;

public class MandjetException extends RuntimeException {

    public MandjetException(String message) {
        super(message);
    }

    public MandjetException(String message, Throwable cause) {
        super(message, cause);
    }
}
