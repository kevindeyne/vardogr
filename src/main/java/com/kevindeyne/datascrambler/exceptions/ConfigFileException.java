package com.kevindeyne.datascrambler.exceptions;

public class ConfigFileException extends Exception {

    private final Exception wrappedException;

    public ConfigFileException(String message, Exception e) {
        super(message);
        this.wrappedException = e;
    }

    public Exception getWrappedException() {
        return wrappedException;
    }
}
