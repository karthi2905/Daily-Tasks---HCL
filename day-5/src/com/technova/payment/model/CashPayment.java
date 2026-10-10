package com.technova.payment.model;

/**
 * Concrete implementation representing a physical Cash payment transaction.
 * Extends Payment. Note: Cash payments are non-refundable through this system
 * and intentionally do not implement the Refundable interface.
 */
public class CashPayment extends Payment {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Constructs a CashPayment instance with payment ID and amount.
     *
     * @param paymentId unique payment identifier
     * @param amount    monetary amount
     */
    public CashPayment(String paymentId, double amount) {
        super(paymentId, amount);
    }

    // -------------------------------------------------------------------------
    // Overridden Payment Processing (Runtime Polymorphism)
    // -------------------------------------------------------------------------
    @Override
    public void processPayment() {
        System.out.printf("Processing cash payment of Rs. %.2f [ID: %s]%n",
                getAmount(), getPaymentId());
    }

    // -------------------------------------------------------------------------
    // String Representation
    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format("CashPayment[id='%s', amount=Rs. %.2f]",
                getPaymentId(), getAmount());
    }
}
