package com.technova.orders.exception;

/**
 * Custom UNCHECKED exception thrown when an order quantity is zero or negative.
 *
 * Demonstrates:
 * - Extending {@link java.lang.RuntimeException} to model programming/precondition violations.
 * - Constructor overloading including message and cause parameters.
 * - Early input boundary validation before inspecting or mutating state.
 */
public class InvalidQuantityException extends RuntimeException {

    private final int invalidQuantity;

    /**
     * Standard message constructor.
     *
     * @param message descriptive error message
     */
    public InvalidQuantityException(String message) {
        super(message);
        this.invalidQuantity = Integer.MIN_VALUE;
    }

    /**
     * Chained exception constructor preserving an underlying cause.
     *
     * @param message descriptive error message
     * @param cause   the root cause exception
     */
    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
        this.invalidQuantity = Integer.MIN_VALUE;
    }

    /**
     * Detailed domain constructor embedding the offending quantity.
     *
     * @param invalidQuantity non-positive quantity submitted
     */
    public InvalidQuantityException(int invalidQuantity) {
        super(String.format("Invalid order quantity: %d. Requested quantity must be strictly greater than zero (> 0).",
                invalidQuantity));
        this.invalidQuantity = invalidQuantity;
    }

    /**
     * Detailed constructor with both quantity and underlying cause.
     */
    public InvalidQuantityException(int invalidQuantity, Throwable cause) {
        super(String.format("Invalid order quantity: %d. Requested quantity must be strictly greater than zero (> 0).",
                invalidQuantity), cause);
        this.invalidQuantity = invalidQuantity;
    }

    public int getInvalidQuantity() {
        return invalidQuantity;
    }
}
