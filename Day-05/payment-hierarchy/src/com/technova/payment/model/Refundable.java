package com.technova.payment.model;

/**
 * Interface representing payment methods that support partial or full refund processing.
 * Demonstrates contract abstraction and polymorphic capability checking.
 */
public interface Refundable {

    /**
     * Issues a refund for the specified amount against this payment.
     *
     * @param amount the refund amount to issue (must be strictly positive and <= remaining refundable balance)
     * @throws IllegalArgumentException if the amount is non-positive or exceeds refundable limits
     */
    void refund(double amount);

    /**
     * Returns the cumulative amount that has already been refunded.
     *
     * @return refunded amount
     */
    double getRefundedAmount();

    /**
     * Returns the remaining balance eligible for refund.
     *
     * @return remaining refundable amount
     */
    double getRemainingRefundableAmount();
}
