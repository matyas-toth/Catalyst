package com.reigindustries.catalyst.command.factory.parsing;

public class ParameterParseException extends Exception {

    public enum ErrorKind {
        INVALID,
        MISSING_REQUIRED
    }

    private final String expectedType;
    private final int index;
    private final ErrorKind kind;

    public ParameterParseException(String expectedType, int index, ErrorKind kind) {
        super();
        this.expectedType = expectedType;
        this.index = index;
        this.kind = kind;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public int getIndex() {
        return index;
    }

    public ErrorKind getKind() {
        return kind;
    }
}


