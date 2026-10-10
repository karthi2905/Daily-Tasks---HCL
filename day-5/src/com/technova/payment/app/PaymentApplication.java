package com.technova.payment.app;

import com.technova.payment.model.CardPayment;
import com.technova.payment.model.CashPayment;
import com.technova.payment.model.Payment;
import com.technova.payment.model.Refundable;
import com.technova.payment.model.UPIPayment;
import com.technova.payment.service.PaymentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point application demonstrating:
 * 1. Abstraction & Inheritance (Payment hierarchy)
 * 2. Runtime Polymorphism (dynamic method dispatch via parent references)
 * 3. Compile-time Polymorphism (PaymentService method overloading)
 * 4. Interface-based capability handling (Refundable)
 * 5. Defensive validation and exception handling
 */
public class PaymentApplication {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("               TECHNOVA PAYMENT PROCESSING SYSTEM -- DAY 5                      ");
        System.out.println("      Demonstrating Abstraction, Inheritance, Polymorphism & Interfaces        ");
        System.out.println("================================================================================");

        PaymentService paymentService = new PaymentService();

        // =====================================================================
        // SECTION 1: Polymorphic Parent-Type Reference Initialization
        // =====================================================================
        System.out.println("\n>>> [1] CREATING INSTANCES USING BASE CLASS REFERENCES (UPCASTING)");

        Payment cardPayment = new CardPayment("PAY-CRD-1001", 1500.00, "Visa Platinum **4321");
        Payment upiPayment = new UPIPayment("PAY-UPI-2002", 500.00, "student@upi");
        Payment cashPayment = new CashPayment("PAY-CSH-3003", 200.00);

        List<Payment> paymentList = new ArrayList<>();
        paymentList.add(cardPayment);
        paymentList.add(upiPayment);
        paymentList.add(cashPayment);

        System.out.println("Payment instances registered:");
        for (Payment p : paymentList) {
            System.out.println("  * " + p);
        }

        // =====================================================================
        // SECTION 2: Runtime Polymorphism (Overriding & Dynamic Method Dispatch)
        // =====================================================================
        System.out.println("\n>>> [2] DEMONSTRATING RUNTIME POLYMORPHISM (ITERATING Payment[] / List<Payment>)");
        System.out.println("Calling payment.processPayment() dynamically dispatches to subclass implementations:");

        int index = 1;
        for (Payment payment : paymentList) {
            System.out.printf("[%d] Target class: %-12s | ID: %-12s -> ",
                    index++, payment.getClass().getSimpleName(), payment.getPaymentId());
            payment.processPayment();
        }

        // =====================================================================
        // SECTION 3: Compile-Time Polymorphism (Method Overloading in PaymentService)
        // =====================================================================
        System.out.println("\n>>> [3] DEMONSTRATING COMPILE-TIME POLYMORPHISM (OVERLOADED pay() METHODS)");

        System.out.println("* Invocation 1: pay(Payment)");
        paymentService.pay(cardPayment);

        System.out.println("\n* Invocation 2: pay(Payment, String)");
        paymentService.pay(upiPayment, "Quarterly Cloud Training Subscription - TechNova");

        System.out.println("\n* Invocation 3: pay(Payment, int)");
        Payment enterpriseCard = new CardPayment("PAY-CRD-1004", 12000.00, "Corporate Amex **9009");
        paymentService.pay(enterpriseCard, 6);

        // =====================================================================
        // SECTION 4: Interface-Based Capability & Refund Processing
        // =====================================================================
        System.out.println("\n>>> [4] DEMONSTRATING REFUND PROCESSING VIA Refundable INTERFACE");
        System.out.println("Inspecting payments for Refundable capability via type pattern checking:");

        for (Payment payment : paymentList) {
            System.out.println("----------------------------------------------------------------");
            System.out.printf("Checking payment %s (%s, Amount: Rs. %.2f):%n",
                    payment.getPaymentId(), payment.getClass().getSimpleName(), payment.getAmount());

            if (payment instanceof Refundable refundable) {
                System.out.println("  [VALID] This payment implements Refundable interface.");
                // Demonstrate valid partial refund
                double partialRefund = payment.getAmount() * 0.40;
                System.out.printf("  Executing partial refund of 40%% (Rs. %.2f)...%n  -> ", partialRefund);
                refundable.refund(partialRefund);

                // Demonstrate another valid partial refund
                double secondRefund = payment.getAmount() * 0.30;
                System.out.printf("  Executing second refund of 30%% (Rs. %.2f)...%n  -> ", secondRefund);
                refundable.refund(secondRefund);
            } else {
                System.out.println("  [INFO] CashPayment does NOT implement Refundable. Cash transactions cannot be refunded through digital gateways.");
            }
        }

        // =====================================================================
        // SECTION 5: Defensive Validation & Exception Handling
        // =====================================================================
        System.out.println("\n>>> [5] DEMONSTRATING VALIDATION & DEFENSIVE EXCEPTION HANDLING");

        // Scenario A: Negative payment amount
        System.out.println("\n--- Scenario A: Creating Payment with Negative Amount ---");
        try {
            System.out.println("Attempting: new CardPayment(\"PAY-ERR-01\", -500.00, \"Visa **1111\")");
            Payment invalid = new CardPayment("PAY-ERR-01", -500.00, "Visa **1111");
            System.out.println("Unexpected success: " + invalid);
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        // Scenario B: Blank payment ID
        System.out.println("\n--- Scenario B: Creating Payment with Blank Payment ID ---");
        try {
            System.out.println("Attempting: new CashPayment(\"   \", 350.00)");
            Payment invalid = new CashPayment("   ", 350.00);
            System.out.println("Unexpected success: " + invalid);
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        // Scenario C: Invalid UPI ID format
        System.out.println("\n--- Scenario C: Creating UPI Payment with Invalid Format ---");
        try {
            System.out.println("Attempting: new UPIPayment(\"PAY-ERR-03\", 250.00, \"invalid-upi-handle\")");
            Payment invalid = new UPIPayment("PAY-ERR-03", 250.00, "invalid-upi-handle");
            System.out.println("Unexpected success: " + invalid);
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        // Scenario D: Negative refund amount
        System.out.println("\n--- Scenario D: Refunding Negative Amount ---");
        try {
            System.out.println("Attempting: upiPayment refund of -50.00");
            if (upiPayment instanceof Refundable ref) {
                ref.refund(-50.00);
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        // Scenario E: Refund exceeding original payment limit
        System.out.println("\n--- Scenario E: Excessive Refund Exceeding Remaining Balance ---");
        try {
            // UPI was Rs. 500.00, already refunded Rs. 200.00 + Rs. 150.00 = Rs. 350.00. Remaining: Rs. 150.00.
            System.out.println("Attempting: upiPayment refund of Rs. 300.00 (Remaining is only Rs. 150.00)");
            if (upiPayment instanceof Refundable ref) {
                ref.refund(300.00);
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        // Scenario F: Invalid installment count in PaymentService
        System.out.println("\n--- Scenario F: Service Overload with Zero Installments ---");
        try {
            System.out.println("Attempting: paymentService.pay(cardPayment, 0)");
            paymentService.pay(cardPayment, 0);
        } catch (IllegalArgumentException ex) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + ex.getMessage());
        }

        System.out.println("\n================================================================================");
        System.out.println("      TECHNOVA PAYMENT PROCESSING APPLICATION COMPLETED SUCCESSFULLY           ");
        System.out.println("================================================================================");
    }
}
