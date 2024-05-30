package com.imt.dlms.server.exception;

public class LoraConfigurationException extends RuntimeException {

    public LoraConfigurationException(String message) {
        super(message);
    }

    public LoraConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
