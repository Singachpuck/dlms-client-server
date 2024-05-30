package com.imt.dlms.server.exception;

public class SerialCommunicationException extends RuntimeException {

    public SerialCommunicationException(String message) {
        super(message);
    }

    public SerialCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
