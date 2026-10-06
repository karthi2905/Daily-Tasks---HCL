package com.technova.booking.exception;

/**
 * Checked exception thrown when a new booking conflicts with an existing one.
 *
 * Extends Exception (checked) so the compiler forces callers to either
 * handle it with try-catch or declare it with 'throws'.
 *
 * Demonstrates: checked exceptions, custom exception hierarchy.
 */
public class BookingConflictException extends Exception {

    /**
     * Constructs a BookingConflictException with the specified message.
     *
     * @param message description of the conflict
     */
    public BookingConflictException(String message) {
        super(message);
    }

    /**
     * Constructs a BookingConflictException with a message and a cause.
     * Method overloading: same name, different signature.
     *
     * @param message description of the conflict
     * @param cause   the original exception that caused this
     */
    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
