package com.technova.payment.model;

/**
 * Concrete implementation representing a Credit/Debit Card payment transaction.
 * Extends Payment and implements Refundable.
 */
public class CardPayment extends Payment implements Refundable {

    // -------------------------------------------------------------------------
    // Private Encapsulated Fields
    // -------------------------------------------------------------------------
    private final String cardLabel;
    private double refundedAmount;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Constructs a CardPayment with payment ID, amount, and masked card label.
     *
     * @param paymentId unique payment identifier
     * @param amount    monetary amount
     * @param cardLabel non-sensitive card reference (e.g., "Visa Ending **4321")
     */
    public CardPayment(String paymentId, double amount, String cardLabel) {
        super(paymentId, amount);

        if (cardLabel == null || cardLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("Card label cannot be null or blank.");
        }

        this.cardLabel = cardLabel.trim();
        this.refundedAmount = 0.0;
    }

    // -------------------------------------------------------------------------
    // Encapsulated Getters
    // -------------------------------------------------------------------------
    public String getCardLabel() {
        return cardLabel;
    }

    // -------------------------------------------------------------------------
    // Overridden Payment Processing (Runtime Polymorphism)
    // -------------------------------------------------------------------------
    @Override
    public void processPayment() {
        System.out.printf("Processing card payment of Rs. %.2f [Card: %s, ID: %s]%n",
                getAmount(), cardLabel, getPaymentId());
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
                    "Refund rejected for Card Payment %s: requested Rs. %.2f exceeds remaining refundable balance Rs. %.2f (Original: Rs. %.2f, Already Refunded: Rs. %.2f)",
                    getPaymentId(), amount, remaining, getAmount(), refundedAmount));
        }

        this.refundedAmount += amount;
        System.out.printf("Refund processed for Card Payment %s: Rs. %.2f [Remaining Refundable: Rs. %.2f, Total Refunded: Rs. %.2f / Rs. %.2f]%n",
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
        return String.format("CardPayment[id='%s', amount=Rs. %.2f, cardLabel='%s', refunded=Rs. %.2f]",
                getPaymentId(), getAmount(), cardLabel, refundedAmount);
    }
}
