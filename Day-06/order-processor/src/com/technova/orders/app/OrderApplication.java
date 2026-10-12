package com.technova.orders.app;

import com.technova.orders.exception.InsufficientStockException;
import com.technova.orders.exception.InvalidQuantityException;
import com.technova.orders.exception.OrderProcessingException;
import com.technova.orders.service.OrderProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main application entry point for the Day 6 Order Processor.
 *
 * Demonstrates:
 * 1. Checked vs. Unchecked Exception Handling
 * 2. Exception Chaining with {@link OrderProcessingException#getCause()}
 * 3. Multi-catch syntax for disjoint exceptions
 * 4. Guaranteed audit logging via {@code finally}
 * 5. Standalone Reproducible Stack Trace scenarios for Scenario 1 and Scenario 2
 */
public class OrderApplication {

    public static void main(String[] args) {
        String mode = (args.length > 0) ? args[0].trim().toLowerCase() : "--demo";

        switch (mode) {
            case "--scenario1" -> runScenario1InvalidQuantity();
            case "--scenario2" -> runScenario2ChainedInsufficientStock();
            case "--demo", "" -> runFullDemonstration();
            default -> {
                System.out.println("Unknown option: " + mode);
                System.out.println("Usage: java com.technova.orders.app.OrderApplication [--demo | --scenario1 | --scenario2]");
            }
        }
    }

    /**
     * Executes the comprehensive Day 6 demonstration suite.
     */
    public static void runFullDemonstration() {
        System.out.println("================================================================================");
        System.out.println("     TECHNOVA ORDER PROCESSOR -- EXCEPTION HANDLING & AUDIT DEMONSTRATION       ");
        System.out.println("================================================================================");

        OrderProcessor processor = new OrderProcessor();
        System.out.printf("Initial Inventory: Product 'Keyboard' has %d unit(s) in stock.%n%n",
                processor.getStock("Keyboard"));

        // ---------------------------------------------------------------------
        // Case 1: Successful Order
        // ---------------------------------------------------------------------
        System.out.println("--- [TEST CASE 1] Normal Flow: Valid Order (2 units) ---");
        try {
            processor.processOrder("Keyboard", 2);
            System.out.println("[PASS] Result: Order completed successfully.");
        } catch (InsufficientStockException ex) {
            System.err.println("[FAIL] Unexpected stock exception: " + ex.getMessage());
        } catch (InvalidQuantityException ex) {
            System.err.println("[FAIL] Unexpected quantity exception: " + ex.getMessage());
        }
        System.out.printf("  Current stock after Case 1: %d unit(s)%n%n", processor.getStock("Keyboard"));

        // ---------------------------------------------------------------------
        // Case 2: Unchecked Exception -- Invalid Quantity (Negative or Zero)
        // ---------------------------------------------------------------------
        System.out.println("--- [TEST CASE 2] Unchecked Exception: Invalid Quantity (0 units) ---");
        try {
            processor.processOrder("Keyboard", 0);
            System.err.println("[FAIL] Expected InvalidQuantityException was not thrown!");
        } catch (InvalidQuantityException ex) {
            System.out.printf("[PASS] CAUGHT UNCHECKED EXCEPTION [%s]:%n  %s%n",
                    ex.getClass().getSimpleName(), ex.getMessage());
            System.out.println("  (Inventory remains untouched. Demonstration continues safely.)");
        } catch (InsufficientStockException ex) {
            System.err.println("[FAIL] Unexpected exception: " + ex.getMessage());
        }
        System.out.printf("  Current stock after Case 2: %d unit(s)%n%n", processor.getStock("Keyboard"));

        // ---------------------------------------------------------------------
        // Case 3: Checked Exception -- Insufficient Stock
        // ---------------------------------------------------------------------
        System.out.println("--- [TEST CASE 3] Checked Exception: Insufficient Stock (10 units requested, 3 available) ---");
        try {
            processor.processOrder("Keyboard", 10);
            System.err.println("[FAIL] Expected InsufficientStockException was not thrown!");
        } catch (InsufficientStockException ex) {
            System.out.printf("[PASS] CAUGHT CHECKED EXCEPTION [%s]:%n  %s%n",
                    ex.getClass().getSimpleName(), ex.getMessage());
            System.out.printf("  Details: Product='%s', Requested=%d, Available=%d%n",
                    ex.getProduct(), ex.getRequestedQuantity(), ex.getAvailableStock());
            System.out.println("  (Inventory remains untouched. Demonstration continues safely.)");
        } catch (InvalidQuantityException ex) {
            System.err.println("[FAIL] Unexpected exception: " + ex.getMessage());
        }
        System.out.printf("  Current stock after Case 3: %d unit(s)%n%n", processor.getStock("Keyboard"));

        // ---------------------------------------------------------------------
        // Case 4: Exception Chaining -- Wrapping Lower-Level Exception
        // ---------------------------------------------------------------------
        System.out.println("--- [TEST CASE 4] Exception Chaining: OrderProcessingException wrapping InsufficientStockException ---");
        try {
            processor.processOrderWithChaining("Keyboard", 7);
            System.err.println("[FAIL] Expected OrderProcessingException was not thrown!");
        } catch (OrderProcessingException ex) {
            System.out.printf("[PASS] CAUGHT WRAPPER EXCEPTION: %s%n", ex.getMessage());
            Throwable cause = ex.getCause();
            if (cause != null) {
                System.out.printf("  Underlying Root Cause [%s]: %s%n",
                        cause.getClass().getName(), cause.getMessage());
            } else {
                System.err.println("[FAIL] Root cause was unexpectedly missing!");
            }
        }
        System.out.printf("  Current stock after Case 4: %d unit(s)%n%n", processor.getStock("Keyboard"));

        // ---------------------------------------------------------------------
        // Case 5: Multi-Catch Syntax
        // ---------------------------------------------------------------------
        System.out.println("--- [TEST CASE 5] Multi-Catch Demonstration (IllegalArgumentException | IllegalStateException) ---");
        System.out.println("Subcase 5A: Blank product name triggering IllegalArgumentException:");
        processor.processOrderWithMultiCatch("   ", 1);

        System.out.println("\nSubcase 5B: Store paused triggering IllegalStateException:");
        processor.setStoreOpen(false);
        processor.processOrderWithMultiCatch("Keyboard", 1);
        processor.setStoreOpen(true); // Re-open store

        // ---------------------------------------------------------------------
        // Case 6: Finally Block & Audit Log Verification
        // ---------------------------------------------------------------------
        System.out.println("\n--- [TEST CASE 6] Guaranteed Audit Log Inspection (audit/order-audit.log) ---");
        printAuditLog(processor.getAuditLogPath());

        System.out.println("\n================================================================================");
        System.out.println("                    DEMONSTRATION COMPLETED SUCCESSFULLY                        ");
        System.out.println("================================================================================");
    }

    /**
     * SCENARIO 1: Generates the genuine stack trace for InvalidQuantityException.
     */
    public static void runScenario1InvalidQuantity() {
        System.out.println("================================================================================");
        System.out.println("                 SCENARIO 1: UNCHECKED InvalidQuantityException                 ");
        System.out.println("================================================================================");
        OrderProcessor processor = new OrderProcessor();
        try {
            // Trigger with non-positive quantity (-3)
            processor.processOrder("Keyboard", -3);
        } catch (InvalidQuantityException ex) {
            System.err.println("[STACK TRACE GENERATED]:");
            ex.printStackTrace(System.err);
        } catch (InsufficientStockException ex) {
            System.err.println("Unexpected checked exception: " + ex.getMessage());
        }
    }

    /**
     * SCENARIO 2: Generates the genuine stack trace for InsufficientStockException
     * wrapped in OrderProcessingException (Exception Chaining).
     */
    public static void runScenario2ChainedInsufficientStock() {
        System.out.println("================================================================================");
        System.out.println("         SCENARIO 2: EXCEPTION CHAINING (OrderProcessingException)             ");
        System.out.println("================================================================================");
        OrderProcessor processor = new OrderProcessor();
        try {
            // Trigger shortage: request 10 units when only 5 exist
            processor.processOrderWithChaining("Keyboard", 10);
        } catch (OrderProcessingException ex) {
            // Re-throw as RuntimeException or print stack trace directly to standard error
            System.err.println("[STACK TRACE GENERATED]:");
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Helper to read and display audit log file contents.
     */
    private static void printAuditLog(Path logPath) {
        if (!Files.exists(logPath)) {
            System.out.println("  (Audit log file does not exist yet)");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(logPath)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
                count++;
            }
            System.out.printf("  Total Audit Records Logged: %d%n", count);
        } catch (IOException e) {
            System.err.println("  Failed to read audit log: " + e.getMessage());
        }
    }
}
