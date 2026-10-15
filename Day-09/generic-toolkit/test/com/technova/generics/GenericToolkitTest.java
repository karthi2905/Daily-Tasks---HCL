package com.technova.generics;

import com.technova.generics.model.Box;
import com.technova.generics.model.Pair;
import com.technova.generics.model.Product;
import com.technova.generics.resource.TrackedResource;
import com.technova.generics.service.CsvParseException;
import com.technova.generics.service.ProductCsvService;
import com.technova.generics.util.GenericUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day 09 :: Generic Toolkit, File I/O & Resource Management Test Suite")
class GenericToolkitTest {

    @TempDir
    Path tempDir;

    // =========================================================================
    // 1. GENERIC PAIR TESTS
    // =========================================================================

    @Test
    @DisplayName("1. Pair preserves typed key and value without casting")
    void testPairTypes() {
        Pair<String, Integer> stringIntPair = Pair.of("Score", 100);
        assertEquals("Score", stringIntPair.getKey());
        assertEquals(100, stringIntPair.getValue());

        Product product = new Product(1, "Pen", "Stationery", new BigDecimal("1.50"));
        Pair<Integer, Product> intProductPair = Pair.of(1, product);
        assertEquals(1, intProductPair.getKey());
        assertEquals(product, intProductPair.getValue());

        Pair<String, Integer> identical = Pair.of("Score", 100);
        assertEquals(stringIntPair, identical);
        assertEquals(stringIntPair.hashCode(), identical.hashCode());
    }

    // =========================================================================
    // 2. BOUNDED BOX TESTS
    // =========================================================================

    @Test
    @DisplayName("2. Box accepts Integer, Double, and Long, and accurately computes doubleValue()")
    void testBoxWithNumericSubtypes() {
        Box<Integer> intBox = Box.of(50);
        assertEquals(50, intBox.getValue());
        assertEquals(50.0, intBox.doubleValue(), 0.0001);
        assertTrue(intBox.isPresent());

        Box<Double> dblBox = Box.of(2.718);
        assertEquals(2.718, dblBox.getValue());
        assertEquals(2.718, dblBox.doubleValue(), 0.0001);

        Box<Long> longBox = Box.of(9999999999L);
        assertEquals(9999999999L, longBox.getValue());
        assertEquals(9999999999.0, longBox.doubleValue(), 0.0001);

        Box<Integer> emptyBox = Box.empty();
        assertFalse(emptyBox.isPresent());
        assertEquals(0.0, emptyBox.doubleValueOrDefault(0.0));
        assertThrows(IllegalStateException.class, emptyBox::doubleValue);
    }

    // =========================================================================
    // 3. GENERIC MAXIMUM METHOD TESTS
    // =========================================================================

    @Test
    @DisplayName("3. max() correctly determines maximum for numbers, strings, and custom products")
    void testGenericMax() {
        assertEquals(80, GenericUtils.max(80, 20));
        assertEquals("Orange", GenericUtils.max("Apple", "Orange"));

        Product cheaper = new Product(101, "Mouse", "Accessories", new BigDecimal("25.00"));
        Product pricier = new Product(102, "Monitor", "Electronics", new BigDecimal("350.00"));
        assertEquals(pricier, GenericUtils.max(cheaper, pricier));

        assertThrows(NullPointerException.class, () -> GenericUtils.max(null, "Test"));
        assertThrows(NullPointerException.class, () -> GenericUtils.max("Test", null));
    }

    // =========================================================================
    // 4. WILDCARDS & PECS TESTS
    // =========================================================================

    @Test
    @DisplayName("4. sumNumbers() accurately sums various Number subtype lists (Producer Extends)")
    void testSumNumbersWildcard() {
        List<Integer> ints = List.of(1, 2, 3, 4);
        List<Double> doubles = List.of(1.5, 2.5, 3.5);
        List<Long> longs = List.of(10L, 20L, 30L);

        assertEquals(10.0, GenericUtils.sumNumbers(ints), 0.0001);
        assertEquals(7.5, GenericUtils.sumNumbers(doubles), 0.0001);
        assertEquals(60.0, GenericUtils.sumNumbers(longs), 0.0001);

        assertThrows(NullPointerException.class, () -> GenericUtils.sumNumbers(null));
    }

    @Test
    @DisplayName("5. copyNumbers() copies from Producer to Consumer (List<Number> and List<Object>)")
    void testCopyNumbersWildcard() {
        List<Integer> source = List.of(10, 20, 30);

        List<Number> numDest = new ArrayList<>();
        GenericUtils.copyNumbers(source, numDest);
        assertEquals(List.of(10, 20, 30), numDest);

        List<Object> objDest = new ArrayList<>();
        GenericUtils.copyNumbers(source, objDest);
        assertEquals(List.of(10, 20, 30), objDest);

        assertThrows(NullPointerException.class, () -> GenericUtils.copyNumbers(null, numDest));
        assertThrows(NullPointerException.class, () -> GenericUtils.copyNumbers(source, null));
    }

    @Test
    @DisplayName("6. printItems() accepts lists of any type without throwing exceptions")
    void testPrintItemsUnboundedWildcard() {
        assertDoesNotThrow(() -> GenericUtils.printItems(List.of("A", "B", "C")));
        assertDoesNotThrow(() -> GenericUtils.printItems(List.of(1, 2, 3)));
        assertDoesNotThrow(() -> GenericUtils.printItems(List.of(new Product(1, "Item", "Cat", BigDecimal.TEN))));
        assertThrows(NullPointerException.class, () -> GenericUtils.printItems(null));
    }

    // =========================================================================
    // 5. CSV PROCESSING TESTS
    // =========================================================================

    @Test
    @DisplayName("7. ProductCsvService correctly parses valid CSV files")
    void testValidCsvParsing() throws IOException, CsvParseException {
        Path validCsv = tempDir.resolve("valid_products.csv");
        String content = """
                id,name,category,price
                101,Keyboard,Accessories,150.00
                102,Mouse,Accessories,50.00
                """;
        Files.writeString(validCsv, content);

        ProductCsvService service = new ProductCsvService();
        List<Product> products = service.loadProducts(validCsv);

        assertEquals(2, products.size());
        assertEquals("Keyboard", products.get(0).getName());
        assertEquals(new BigDecimal("150.00"), products.get(0).getPrice());
        assertEquals("Mouse", products.get(1).getName());
    }

    @Test
    @DisplayName("8. ProductCsvService rejects malformed rows and reports exact line numbers")
    void testMalformedCsvReportsLineNumber() throws IOException {
        Path malformedCsv = tempDir.resolve("bad_products.csv");
        String content = """
                id,name,category,price
                101,Valid Product,Accessories,150.00
                102,Missing Column,Accessories
                103,Another Product,Office,75.00
                """;
        Files.writeString(malformedCsv, content);

        ProductCsvService service = new ProductCsvService();
        CsvParseException ex = assertThrows(CsvParseException.class, () -> service.loadProducts(malformedCsv));

        assertEquals(3, ex.getLineNumber(), "Error should identify line 3 as the malformed line");
        assertTrue(ex.getMessage().contains("line 3"));
    }

    @Test
    @DisplayName("9. ProductCsvService throws IOException when CSV file is missing")
    void testMissingCsvFile() {
        ProductCsvService service = new ProductCsvService();
        Path nonExistent = tempDir.resolve("ghost.csv");

        IOException ex = assertThrows(IOException.class, () -> service.loadProducts(nonExistent));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // =========================================================================
    // 6. AUTOCLOSEABLE & RESOURCE MANAGEMENT TESTS
    // =========================================================================

    @Test
    @DisplayName("10. TrackedResource guarantees closing in reverse declaration order (LIFO)")
    void testReverseClosingOrder() {
        List<String> closeOrder = new ArrayList<>();

        class OrderTrackingResource implements AutoCloseable {
            private final String name;

            OrderTrackingResource(String name) {
                this.name = name;
            }

            @Override
            public void close() {
                closeOrder.add(name);
            }
        }

        try (OrderTrackingResource r1 = new OrderTrackingResource("First");
             OrderTrackingResource r2 = new OrderTrackingResource("Second");
             OrderTrackingResource r3 = new OrderTrackingResource("Third")) {
            // Nothing to do inside try
        }

        assertEquals(List.of("Third", "Second", "First"), closeOrder,
                "Resources must close in exact reverse declaration order");
    }

    @Test
    @DisplayName("11. TrackedResource closes automatically even when try block throws an exception")
    void testResourceClosesOnException() {
        TrackedResource res1 = null;
        TrackedResource res2 = null;

        try (TrackedResource r1 = new TrackedResource("Res1");
             TrackedResource r2 = new TrackedResource("Res2")) {
            res1 = r1;
            res2 = r2;
            throw new RuntimeException("Simulated failure inside try");
        } catch (Exception ex) {
            // Expected simulation
        }

        assertNotNull(res1);
        assertNotNull(res2);
        assertTrue(res1.isClosed(), "Resource 1 must be closed even after exception");
        assertTrue(res2.isClosed(), "Resource 2 must be closed even after exception");
    }

    @Test
    @DisplayName("12. Try-with-resources preserves primary exception and attaches close failures to getSuppressed()")
    void testSuppressedExceptionsPreserved() {
        Exception caughtException = null;

        try (TrackedResource faulty = new TrackedResource("Faulty", true)) {
            throw new RuntimeException("Primary error");
        } catch (Exception ex) {
            caughtException = ex;
        }

        assertNotNull(caughtException);
        assertEquals("Primary error", caughtException.getMessage(), "Primary exception must be preserved");
        assertEquals(1, caughtException.getSuppressed().length, "Close exception must be attached as suppressed");
        assertTrue(caughtException.getSuppressed()[0].getMessage().contains("Simulated close failure"));
    }
}
