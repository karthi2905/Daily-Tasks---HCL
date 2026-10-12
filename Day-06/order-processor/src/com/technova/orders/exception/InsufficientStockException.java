package com.technova.orders.exception;

/**
 * Custom CHECKED exception thrown when an order requests more inventory than currently available.
 *
 * Demonstrates:
 * - Extending {@link java.lang.Exception} to enforce compile-time error handling.
 * - Constructor overloading including message and cause parameters.
 * - Capturing rich operational context (product, requested quantity, and available stock).
 */
public class InsufficientStockException extends Exception {

    private final String product;
    private final int requestedQuantity;
    private final int availableStock;

    /**
     * Standard message constructor.
     *
     * @param message descriptive error message
     */
    public InsufficientStockException(String message) {
        super(message);
        this.product = "UNKNOWN";
        this.requestedQuantity = -1;
        this.availableStock = -1;
    }

    /**
     * Chained exception constructor preserving an underlying cause.
     *
     * @param message descriptive error message
     * @param cause   the root cause exception
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
        this.product = "UNKNOWN";
        this.requestedQuantity = -1;
        this.availableStock = -1;
    }

    /**
     * Detailed domain constructor embedding stock parameters.
     *
     * @param product           name of the item
     * @param requestedQuantity quantity attempted in the order
     * @param availableStock    quantity remaining in inventory
     */
    public InsufficientStockException(String product, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product '%s': requested %d item(s), but only %d in stock.",
                product, requestedQuantity, availableStock));
        this.product = product;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    /**
     * Detailed constructor with both stock context and underlying cause.
     */
    public InsufficientStockException(String product, int requestedQuantity, int availableStock, Throwable cause) {
        super(String.format("Insufficient stock for product '%s': requested %d item(s), but only %d in stock.",
                product, requestedQuantity, availableStock), cause);
        this.product = product;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public String getProduct() {
        return product;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
