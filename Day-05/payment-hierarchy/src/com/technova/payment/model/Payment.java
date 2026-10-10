package com.technova.payment.model;

/**
 * Abstract base class representing a generic payment transaction.
 * Demonstrates abstraction and encapsulation in object-oriented design.
 */
public abstract class Payment {

    // -------------------------------------------------------------------------
    // Private Encapsulated Fields
    // -------------------------------------------------------------------------
    private final String paymentId;
    private final double amount;

    // -------------------------------------------------------------------------
    // Constructor with Input Validation
    // -------------------------------------------------------------------------
    /**
     * Constructs a Payment instance with the specified unique ID and monetary amount.
     *
     * @param paymentId unique payment identifier (non-null, non-blank)
     * @param amount    transaction amount (must be strictly positive)
     * @throws IllegalArgumentException if validation constraints fail
     */
    public Payment(String paymentId, double amount) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment ID cannot be null or empty.");
        }
        if (amount <= 0.0) {
            throw new IllegalArgumentException(String.format(
                    "Payment amount must be greater than zero. Received: %.2f", amount));
        }

        this.paymentId = paymentId.trim();
        this.amount = amount;
    }

    // -------------------------------------------------------------------------
    // Encapsulated Getters
    // -------------------------------------------------------------------------
    public String getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    // -------------------------------------------------------------------------
    // Abstract Method for Polymorphic Processing
    // -------------------------------------------------------------------------
    /**
     * Processes the specific payment according to the concrete payment mechanism.
     * Subclasses must provide concrete implementation.
     */
    public abstract void processPayment();

    // -------------------------------------------------------------------------
    // Object Identity & String Representation
    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format("%s[id='%s', amount=Rs. %.2f]",
                getClass().getSimpleName(), paymentId, amount);
    }
}
