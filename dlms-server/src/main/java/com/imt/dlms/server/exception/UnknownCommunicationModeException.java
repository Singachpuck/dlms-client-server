package com.imt.dlms.server.exception;

public class UnknownCommunicationModeException extends RuntimeException {

    public UnknownCommunicationModeException(String message) {
        super(message);
    }

    public UnknownCommunicationModeException(String message, Throwable cause) {
        super(message, cause);
    }
}
