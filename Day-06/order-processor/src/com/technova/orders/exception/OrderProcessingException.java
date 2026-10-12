package com.technova.orders.exception;

/**
 * Custom higher-level CHECKED exception used to demonstrate Exception Chaining.
 *
 * In enterprise software architecture, wrapping lower-level technical or inventory exceptions
 * (such as {@link InsufficientStockException}) into a domain-level {@link OrderProcessingException}
 * shields calling clients from internal subsystem details while retaining the full causal chain
 * for troubleshooting through {@link Throwable#getCause()}.
 */
public class OrderProcessingException extends Exception {

    private final String orderId;

    /**
     * Standard message constructor.
     */
    public OrderProcessingException(String message) {
        super(message);
        this.orderId = "N/A";
    }

    /**
     * Primary exception-chaining constructor preserving the underlying cause.
     *
     * @param message higher-level operational context
     * @param cause   the root exception that caused this failure
     */
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = "N/A";
    }

    /**
     * Detailed chaining constructor with order identification.
     *
     * @param orderId reference identifier for the order
     * @param message higher-level operational context
     * @param cause   the root exception that caused this failure
     */
    public OrderProcessingException(String orderId, String message, Throwable cause) {
        super(String.format("Order [%s] failed: %s", orderId, message), cause);
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
