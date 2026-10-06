package com.technova.booking.exception;

/**
 * Unchecked exception thrown for invalid booking input (e.g., end before start,
 * same start and end, blank employee ID).
 *
 * Extends RuntimeException (unchecked) — validation failures are programming
 * or user errors that callers handle at the UI boundary.
 *
 * Demonstrates: additional unchecked exception, custom exception hierarchy.
 */
public class InvalidBookingException extends RuntimeException {

    /**
     * Constructs an InvalidBookingException with the specified message.
     *
     * @param message human-readable description of the invalid state
     */
    public InvalidBookingException(String message) {
        super(message);
    }

    /**
     * Constructs an InvalidBookingException with a message and cause.
     * Method overloading.
     *
     * @param message human-readable description
     * @param cause   the underlying exception
     */
    public InvalidBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
