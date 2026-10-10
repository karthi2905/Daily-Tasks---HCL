package com.technova.payment.model;

/**
 * Concrete implementation representing a Unified Payments Interface (UPI) transaction.
 * Extends Payment and implements Refundable.
 */
public class UPIPayment extends Payment implements Refundable {

    // -------------------------------------------------------------------------
    // Private Encapsulated Fields
    // -------------------------------------------------------------------------
    private final String upiId;
    private double refundedAmount;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Constructs a UPIPayment instance with payment ID, amount, and virtual payment address (VPA).
     *
     * @param paymentId unique payment identifier
     * @param amount    monetary amount
     * @param upiId     fictional UPI identifier (e.g., "student@upi")
     */
    public UPIPayment(String paymentId, double amount, String upiId) {
        super(paymentId, amount);

        if (upiId == null || upiId.trim().isEmpty()) {
            throw new IllegalArgumentException("UPI ID cannot be null or blank.");
        }
        if (!upiId.contains("@")) {
            throw new IllegalArgumentException("Invalid UPI ID format. Expected format: user@bank, received: " + upiId);
        }

        this.upiId = upiId.trim();
        this.refundedAmount = 0.0;
    }

    // -------------------------------------------------------------------------
    // Encapsulated Getters
    // -------------------------------------------------------------------------
    public String getUpiId() {
        return upiId;
    }

    // -------------------------------------------------------------------------
    // Overridden Payment Processing (Runtime Polymorphism)
    // -------------------------------------------------------------------------
    @Override
    public void processPayment() {
        System.out.printf("Processing UPI payment of Rs. %.2f [UPI ID: %s, ID: %s]%n",
                getAmount(), upiId, getPaymentId());
    }

    // -------------------------------------------------------------------------
    // Refundable Interface Implementation
    // -------------------------------------------------------------------------
    @Override
    public void refund(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException(String.format(
                    "Refund amount must be greater than zero. Received: Rs. %.2f", amount));
        }

        double remaining = getRemainingRefundableAmount();
        if (amount > remaining) {
            throw new IllegalArgumentException(String.format(
                    "Refund rejected for UPI Payment %s: requested Rs. %.2f exceeds remaining refundable balance Rs. %.2f (Original: Rs. %.2f, Already Refunded: Rs. %.2f)",
                    getPaymentId(), amount, remaining, getAmount(), refundedAmount));
        }

        this.refundedAmount += amount;
        System.out.printf("Refund processed for UPI Payment %s: Rs. %.2f [Remaining Refundable: Rs. %.2f, Total Refunded: Rs. %.2f / Rs. %.2f]%n",
                getPaymentId(), amount, getRemainingRefundableAmount(), refundedAmount, getAmount());
    }

    @Override
    public double getRefundedAmount() {
        return refundedAmount;
    }

    @Override
    public double getRemainingRefundableAmount() {
        return getAmount() - refundedAmount;
    }

    // -------------------------------------------------------------------------
    // String Representation
    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format("UPIPayment[id='%s', amount=Rs. %.2f, upiId='%s', refunded=Rs. %.2f]",
                getPaymentId(), getAmount(), upiId, refundedAmount);
    }
}
