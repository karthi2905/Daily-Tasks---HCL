package com.technova.orders.service;

import com.technova.orders.exception.InsufficientStockException;
import com.technova.orders.exception.InvalidQuantityException;
import com.technova.orders.exception.OrderProcessingException;
import com.technova.orders.model.Order;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for inventory management, order validation, and audit recording.
 *
 * Demonstrates:
 * 1. Custom Checked Exception: {@link InsufficientStockException}
 * 2. Custom Unchecked Exception: {@link InvalidQuantityException}
 * 3. Exception Chaining: {@link OrderProcessingException} wrapping low-level causes
 * 4. Multi-catch syntax: Handling multiple disjoint exception types in a single catch block
 * 5. Guaranteed Execution: A {@code finally} block ensuring audit logging for every attempt
 */
public class OrderProcessor {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path auditLogPath;
    private final Map<String, Integer> inventory;
    private boolean storeOpen = true;

    /**
     * Default constructor initializing standard inventory (Keyboard: 5 items)
     * and audit log destination under audit/order-audit.log.
     */
    public OrderProcessor() {
        this(Paths.get("audit", "order-audit.log"));
    }

    /**
     * Parameterized constructor allowing a custom audit log path.
     *
     * @param auditLogPath destination path for audit entries
     */
    public OrderProcessor(Path auditLogPath) {
        this.auditLogPath = auditLogPath;
        this.inventory = new HashMap<>();
        // Seed initial inventory: Product 'Keyboard' with stock of 5
        this.inventory.put("Keyboard", 5);
    }

    /**
     * Primary order processing method enforcing validation, inventory reduction,
     * and guaranteed audit logging via a finally block.
     *
     * @param product  name of the item to purchase
     * @param quantity number of items requested
     * @return the processed {@link Order} record
     * @throws InvalidQuantityException   (unchecked) when quantity <= 0
     * @throws InsufficientStockException (checked) when quantity > available stock
     */
    public Order processOrder(String product, int quantity) throws InsufficientStockException {
        String outcome = "FAILED";
        String auditMessage = "Unknown initial state";
        Order order = new Order(product, quantity);

        try {
            // Pre-condition 1: Check store operational state
            if (!storeOpen) {
                auditMessage = "Store order processing subsystem is currently paused";
                throw new IllegalStateException(auditMessage);
            }

            // Pre-condition 2: Validate product parameter
            if (product == null || product.trim().isEmpty()) {
                auditMessage = "Product name cannot be null or blank";
                throw new IllegalArgumentException(auditMessage);
            }

            // Step 1: Validate Quantity (Throws UNCHECKED InvalidQuantityException if <= 0)
            // Quantity must be validated BEFORE checking or modifying inventory
            if (quantity <= 0) {
                auditMessage = String.format("Rejected invalid quantity: %d (must be > 0)", quantity);
                throw new InvalidQuantityException(quantity);
            }

            // Step 2: Validate Inventory Availability (Throws CHECKED InsufficientStockException if unavailable)
            int currentStock = inventory.getOrDefault(product, 0);
            if (quantity > currentStock) {
                auditMessage = String.format("Shortage: requested %d, available %d", quantity, currentStock);
                throw new InsufficientStockException(product, quantity, currentStock);
            }

            // Step 3: Mutate State — Reduce inventory only after ALL validations pass
            int updatedStock = currentStock - quantity;
            inventory.put(product, updatedStock);

            order.setStatus("CONFIRMED");
            outcome = "SUCCESS";
            auditMessage = String.format("Order placed. Remaining '%s' stock: %d", product, updatedStock);

            System.out.printf("[SUCCESS] %s: Successfully ordered %d unit(s) of '%s'. Remaining stock: %d%n",
                    order.getOrderId(), quantity, product, updatedStock);

            return order;

        } catch (InvalidQuantityException | InsufficientStockException ex) {
            auditMessage = ex.getMessage();
            throw ex; // Re-throw to allow caller handling or stack trace generation
        } finally {
            // STEP 4: GUARANTEED AUDIT LOGGING
            // The finally block executes unconditionally regardless of whether the order
            // succeeded, threw InvalidQuantityException, InsufficientStockException, or any other error.
            writeAuditEntry(product, quantity, outcome, auditMessage);
        }
    }

    /**
     * Demonstrates EXCEPTION CHAINING at a service workflow boundary.
     *
     * Catches the low-level {@link InsufficientStockException} and wraps it inside
     * a higher-level {@link OrderProcessingException}, preserving the original cause.
     *
     * @param product  product name
     * @param quantity requested quantity
     * @return the confirmed order
     * @throws OrderProcessingException higher-level domain exception retaining the underlying cause
     */
    public Order processOrderWithChaining(String product, int quantity) throws OrderProcessingException {
        try {
            return processOrder(product, quantity);
        } catch (InsufficientStockException ex) {
            // Wrap the low-level inventory exception into a higher-level business exception.
            // Notice ex is passed as the cause parameter, preserving the causal chain!
            String msg = String.format("Checkout workflow failed for item '%s' due to warehouse inventory shortage.", product);
            throw new OrderProcessingException(msg, ex);
        }
    }

    /**
     * Demonstrates MULTI-CATCH syntax for catching disjoint unchecked exceptions.
     *
     * In Java 7+, multi-catch allows handling multiple alternative exception types in a single block
     * using the pipe '|' operator, provided the exception types are not in an inheritance hierarchy.
     */
    public void processOrderWithMultiCatch(String product, int quantity) {
        try {
            processOrder(product, quantity);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // Multi-catch handling disjoint standard Java runtime exceptions
            System.err.printf("[MULTI-CATCH] Validation / state error intercepted: %s%n", ex.getMessage());
        } catch (InsufficientStockException ex) {
            System.err.printf("[CHECKED CATCH] Handled checked stock shortage: %s%n", ex.getMessage());
        } catch (InvalidQuantityException ex) {
            System.err.printf("[UNCHECKED CATCH] Handled unchecked invalid quantity: %s%n", ex.getMessage());
        }
    }

    /**
     * Writes an audit entry to the designated log file.
     *
     * Defensive design principles:
     * - Uses try-with-resources to ensure file streams are closed.
     * - Creates parent directory if it does not yet exist.
     * - Never throws an exception from inside finally that could suppress an in-flight business exception.
     */
    private void writeAuditEntry(String product, int quantity, String outcome, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String safeProduct = (product != null && !product.isBlank()) ? product : "N/A";
        String logLine = String.format("[%s] | Product: %-12s | Qty: %3d | Outcome: %-7s | Reason: %s%n",
                timestamp, safeProduct, quantity, outcome, reason);

        try {
            // Ensure parent directory exists
            if (auditLogPath.getParent() != null) {
                Files.createDirectories(auditLogPath.getParent());
            }

            // Append to audit log file
            try (BufferedWriter writer = Files.newBufferedWriter(auditLogPath,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(logLine);
            }
        } catch (IOException ioEx) {
            // Fallback: report audit failure to standard error without swallowing or hiding business exceptions
            System.err.printf("[AUDIT WARNING] Failed to write audit record to '%s': %s%n",
                    auditLogPath, ioEx.getMessage());
        }
    }

    // =========================================================================
    // Inspection and Test Utilities
    // =========================================================================

    public int getStock(String product) {
        return inventory.getOrDefault(product, 0);
    }

    public void setStock(String product, int stock) {
        this.inventory.put(product, stock);
    }

    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    public void setStoreOpen(boolean storeOpen) {
        this.storeOpen = storeOpen;
    }

    public Path getAuditLogPath() {
        return auditLogPath;
    }
}
