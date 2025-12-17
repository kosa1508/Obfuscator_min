package com.obfuscator.obfuscator;

public class ObfuscationException extends RuntimeException {

    private final String errorCode;

    public ObfuscationException(String message) {
        super(message);
        this.errorCode = "OBFUSCATION_ERROR";
    }

    public ObfuscationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "OBFUSCATION_ERROR";
    }

    public ObfuscationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("ObfuscationException[code=%s]: %s", errorCode, getMessage());
    }
}