package com.technova.payment.service;

import com.technova.payment.model.Payment;

/**
 * Service orchestrating payment operations.
 * Demonstrates compile-time polymorphism (method overloading) with multiple pay() methods.
 */
public class PaymentService {

    // =========================================================================
    // Overload 1: Standard Payment
    // =========================================================================
    /**
     * Processes standard payment dispatch.
     *
     * @param payment the payment instance to process (must not be null)
     * @throws IllegalArgumentException if payment reference is null
     */
    public void pay(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment object cannot be null.");
        }

        System.out.println("----------------------------------------------------------------");
        System.out.printf("[PaymentService] Dispatching standard payment transaction: %s%n", payment.getPaymentId());
        payment.processPayment();
    }

    // =========================================================================
    // Overload 2: Payment with Descriptive Context
    // =========================================================================
    /**
     * Processes payment tagged with transaction purpose or memo.
     *
     * @param payment     the payment instance to process (must not be null)
     * @param description contextual memo or transaction description (non-null, non-blank)
     * @throws IllegalArgumentException if payment is null or description is blank
     */
    public void pay(Payment payment, String description) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment object cannot be null.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment description cannot be null or blank.");
        }

        System.out.println("----------------------------------------------------------------");
        System.out.printf("[PaymentService] Dispatching payment with note: \"%s\"%n", description.trim());
        payment.processPayment();
    }

    // =========================================================================
    // Overload 3: Installment / EMI Payment
    // =========================================================================
    /**
     * Processes installment-based payment after validating tenure.
     *
     * @param payment      the payment instance to process (must not be null)
     * @param installments number of monthly installment tenures (must be >= 1)
     * @throws IllegalArgumentException if payment is null or installments < 1
     */
    public void pay(Payment payment, int installments) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment object cannot be null.");
        }
        if (installments < 1) {
            throw new IllegalArgumentException(String.format(
                    "Installment count must be at least 1. Provided: %d", installments));
        }

        double emiAmount = payment.getAmount() / installments;
        System.out.println("----------------------------------------------------------------");
        System.out.printf("[PaymentService] Splitting payment into %d installments @ Rs. %.2f/month%n",
                installments, emiAmount);
        payment.processPayment();
    }
}
