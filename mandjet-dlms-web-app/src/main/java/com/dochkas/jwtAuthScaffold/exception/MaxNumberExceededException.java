package com.dochkas.jwtAuthScaffold.exception;

public class MaxNumberExceededException extends RuntimeException {

    public MaxNumberExceededException(String message) {
        super(message);
    }

    public MaxNumberExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
