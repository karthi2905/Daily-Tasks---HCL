package com.technova.orders;

import com.technova.orders.exception.InsufficientStockException;
import com.technova.orders.exception.InvalidQuantityException;
import com.technova.orders.exception.OrderProcessingException;
import com.technova.orders.model.Order;
import com.technova.orders.service.OrderProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day 06 :: Order Processor & Exception Handling Tests")
class OrderProcessorTest {

    @TempDir
    Path tempDir;

    private Path testAuditLog;
    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        testAuditLog = tempDir.resolve("test-audit.log");
        processor = new OrderProcessor(testAuditLog);
    }

    @Test
    @DisplayName("1. Valid order reduces stock and confirms order")
    void testSuccessfulOrderReducesStock() throws InsufficientStockException {
        assertEquals(5, processor.getStock("Keyboard"));

        Order order = processor.processOrder("Keyboard", 2);

        assertNotNull(order);
        assertEquals("Keyboard", order.getProduct());
        assertEquals(2, order.getQuantity());
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals(3, processor.getStock("Keyboard"));
    }

    @Test
    @DisplayName("2. Zero quantity throws InvalidQuantityException (unchecked) without altering stock")
    void testZeroQuantityThrowsInvalidQuantityException() {
        assertEquals(5, processor.getStock("Keyboard"));

        InvalidQuantityException ex = assertThrows(InvalidQuantityException.class, () ->
                processor.processOrder("Keyboard", 0));

        assertEquals(0, ex.getInvalidQuantity());
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
        assertEquals(5, processor.getStock("Keyboard"), "Stock must remain unchanged on invalid quantity");
    }

    @Test
    @DisplayName("3. Negative quantity throws InvalidQuantityException (unchecked) without altering stock")
    void testNegativeQuantityThrowsInvalidQuantityException() {
        assertEquals(5, processor.getStock("Keyboard"));

        InvalidQuantityException ex = assertThrows(InvalidQuantityException.class, () ->
                processor.processOrder("Keyboard", -4));

        assertEquals(-4, ex.getInvalidQuantity());
        assertEquals(5, processor.getStock("Keyboard"), "Stock must remain unchanged on invalid quantity");
    }

    @Test
    @DisplayName("4. Quantity exceeding stock throws InsufficientStockException (checked) without altering stock")
    void testExcessQuantityThrowsInsufficientStockException() {
        assertEquals(5, processor.getStock("Keyboard"));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () ->
                processor.processOrder("Keyboard", 10));

        assertEquals("Keyboard", ex.getProduct());
        assertEquals(10, ex.getRequestedQuantity());
        assertEquals(5, ex.getAvailableStock());
        assertTrue(ex.getMessage().contains("requested 10 item(s), but only 5 in stock"));
        assertEquals(5, processor.getStock("Keyboard"), "Stock must remain untouched when stock is insufficient");
    }

    @Test
    @DisplayName("5. Exception chaining wraps InsufficientStockException in OrderProcessingException preserving cause")
    void testExceptionChainingPreservesCause() {
        OrderProcessingException ex = assertThrows(OrderProcessingException.class, () ->
                processor.processOrderWithChaining("Keyboard", 8));

        assertTrue(ex.getMessage().contains("Checkout workflow failed"));
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Chained cause must not be null");
        assertInstanceOf(InsufficientStockException.class, cause);
        InsufficientStockException stockEx = (InsufficientStockException) cause;
        assertEquals(8, stockEx.getRequestedQuantity());
        assertEquals(5, stockEx.getAvailableStock());
    }

    @Test
    @DisplayName("6. Finally block guarantees audit log entry on success")
    void testFinallyAuditWrittenOnSuccess() throws IOException, InsufficientStockException {
        processor.processOrder("Keyboard", 3);

        assertTrue(Files.exists(testAuditLog), "Audit log file must be created");
        List<String> lines = Files.readAllLines(testAuditLog);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("SUCCESS"));
        assertTrue(lines.get(0).contains("Keyboard"));
        assertTrue(lines.get(0).contains("Qty:   3"));
    }

    @Test
    @DisplayName("7. Finally block guarantees audit log entry on failure (InvalidQuantityException)")
    void testFinallyAuditWrittenOnInvalidQuantityFailure() throws IOException {
        assertThrows(InvalidQuantityException.class, () ->
                processor.processOrder("Keyboard", -1));

        assertTrue(Files.exists(testAuditLog));
        List<String> lines = Files.readAllLines(testAuditLog);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("FAILED"));
        assertTrue(lines.get(0).contains("Invalid order quantity"));
    }

    @Test
    @DisplayName("8. Finally block guarantees audit log entry on failure (InsufficientStockException)")
    void testFinallyAuditWrittenOnInsufficientStockFailure() throws IOException {
        assertThrows(InsufficientStockException.class, () ->
                processor.processOrder("Keyboard", 9));

        assertTrue(Files.exists(testAuditLog));
        List<String> lines = Files.readAllLines(testAuditLog);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("FAILED"));
        assertTrue(lines.get(0).contains("Insufficient stock"));
    }

    @Test
    @DisplayName("9. Multi-catch handles disjoint IllegalArgumentException and IllegalStateException safely")
    void testMultiCatchSafety() {
        assertDoesNotThrow(() -> processor.processOrderWithMultiCatch("   ", 1));

        processor.setStoreOpen(false);
        assertDoesNotThrow(() -> processor.processOrderWithMultiCatch("Keyboard", 1));
    }
}
