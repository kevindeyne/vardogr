package com.kevindeyne.datascrambler.exceptions;

public class ModelCreationException extends Exception implements ExceptionWrapper {

    private final Exception wrappedException;

    public ModelCreationException(String message, Exception e) {
        super(message);
        this.wrappedException = e;
    }

    public Exception getWrappedException() {
        return wrappedException;
    }

    @Override
    public String getMessage() {
        return wrappedException.getMessage();
    }
}
