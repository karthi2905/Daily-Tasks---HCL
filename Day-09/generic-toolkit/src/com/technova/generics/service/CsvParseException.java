package com.technova.generics.service;

/**
 * Custom checked exception thrown when a CSV parsing or validation error occurs.
 * Encapsulates the exact line number and problematic content to assist troubleshooting.
 */
public class CsvParseException extends Exception {

    private final int lineNumber;

    public CsvParseException(String message, int lineNumber) {
        super(String.format("CSV Parse Error at line %d: %s", lineNumber, message));
        this.lineNumber = lineNumber;
    }

    public CsvParseException(String message, int lineNumber, Throwable cause) {
        super(String.format("CSV Parse Error at line %d: %s", lineNumber, message), cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
