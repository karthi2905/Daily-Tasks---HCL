package com.technova.generics.demo;

import com.technova.generics.model.Box;
import com.technova.generics.model.Pair;
import com.technova.generics.model.Product;
import com.technova.generics.resource.TrackedResource;
import com.technova.generics.service.CsvParseException;
import com.technova.generics.service.ProductCsvService;
import com.technova.generics.util.GenericUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates educational demonstrations for all Day 9 concepts:
 * 1. Type-safe generic Pair<K,V>
 * 2. Bounded type parameter Box<T extends Number>
 * 3. Generic maximum method <T extends Comparable<T>>
 * 4. Wildcard variance & PECS (sumNumbers, copyNumbers, printItems)
 * 5. Robust CSV processing via try-with-resources
 * 6. AutoCloseable reverse declaration closing order
 * 7. Guaranteed resource cleanup on exception propagation & suppressed exceptions
 */
public class GenericToolkitDemo {

    /**
     * SECTION 1: Generic Pair<K,V> Demonstration
     */
    public static void demonstratePairs() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("1. GENERIC Pair<K, V> DEMONSTRATION (Compile-time Type Safety)");
        System.out.println("--------------------------------------------------------------------------------");

        // Pair 1: String key, Integer value
        Pair<String, Integer> studentScore = Pair.of("Alice Smith", 98);
        System.out.println("  Pair<String, Integer>: " + studentScore);
        System.out.printf("    Extracted key: '%s' (Length: %d)%n", studentScore.getKey(), studentScore.getKey().length());
        System.out.printf("    Extracted value: %d (Primitive int, no casting needed)%n", studentScore.getValue());

        // Pair 2: Integer key, String value
        Pair<Integer, String> errorCode = Pair.of(404, "Not Found");
        System.out.println("  Pair<Integer, String>: " + errorCode);

        // Pair 3: String key, Product domain object value
        Product laptop = new Product(101, "Dell XPS 15", "Electronics", new BigDecimal("1899.99"));
        Pair<String, Product> featuredItem = Pair.of("DEAL_OF_THE_DAY", laptop);
        System.out.println("  Pair<String, Product>: " + featuredItem);
        System.out.printf("    Featured Product Name: '%s' | Price: $%,.2f%n",
                featuredItem.getValue().getName(), featuredItem.getValue().getPrice());

        System.out.println("\nWhy Generics?");
        System.out.println("  - Generics eliminate the need for dangerous runtime casts like '(String) pair.getKey()'.");
        System.out.println("  - Type mismatches are caught at COMPILE TIME rather than throwing ClassCastException at runtime.");
    }

    /**
     * SECTION 2: Numeric Box<T extends Number> Demonstration
     */
    public static void demonstrateBoxes() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. BOUNDED Box<T extends Number> DEMONSTRATION");
        System.out.println("--------------------------------------------------------------------------------");

        Box<Integer> intBox = Box.of(42);
        Box<Double> doubleBox = Box.of(3.14159);
        Box<Long> longBox = Box.of(100_000_000_000L);
        Box<Float> floatBox = Box.of(99.95f);
        Box<Integer> emptyBox = Box.empty();

        System.out.println("  Box<Integer>: " + intBox + " -> doubleValue(): " + intBox.doubleValue());
        System.out.println("  Box<Double>:  " + doubleBox + " -> doubleValue(): " + doubleBox.doubleValue());
        System.out.println("  Box<Long>:    " + longBox + " -> doubleValue(): " + longBox.doubleValue());
        System.out.println("  Box<Float>:   " + floatBox + " -> doubleValue(): " + floatBox.doubleValue());
        System.out.println("  Box<Integer> (Empty): " + emptyBox + " -> isPresent(): " + emptyBox.isPresent());

        // Demonstrating safe null handling
        System.out.printf("  emptyBox.doubleValueOrDefault(0.0) -> %.1f%n", emptyBox.doubleValueOrDefault(0.0));
        try {
            emptyBox.doubleValue();
            System.err.println("  [FAIL] Expected IllegalStateException was not thrown!");
        } catch (IllegalStateException ex) {
            System.out.printf("  [PASS] Calling doubleValue() on empty Box caught expected error: '%s'%n", ex.getMessage());
        }

        System.out.println("\nWhy Upper Bounds (<T extends Number>)?");
        System.out.println("  - Restricts Box to types that inherit from java.lang.Number.");
        System.out.println("  - Guarantees methods like doubleValue(), intValue(), and longValue() exist.");
        System.out.println("  - Attempting to compile 'Box<String> strBox = new Box<>()' produces a COMPILE ERROR:");
        System.out.println("    'Type parameter 'java.lang.String' is not within its bound; should extend 'java.lang.Number''.");
    }

    /**
     * SECTION 3: Generic Maximum Method Demonstration
     */
    public static void demonstrateGenericMax() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. GENERIC max(T, T) WITH <T extends Comparable<T>> DEMONSTRATION");
        System.out.println("--------------------------------------------------------------------------------");

        // Integers
        int maxInt = GenericUtils.max(15, 87);
        System.out.printf("  GenericUtils.max(15, 87)       -> %d%n", maxInt);

        // Strings
        String maxStr = GenericUtils.max("Apple", "Zebra");
        System.out.printf("  GenericUtils.max(\"Apple\", \"Zebra\") -> \"%s\"%n", maxStr);

        // Custom Comparable type: Product (ordered by price)
        Product p1 = new Product(101, "Budget Mouse", "Accessories", new BigDecimal("19.99"));
        Product p2 = new Product(102, "Curved 4K Display", "Electronics", new BigDecimal("899.99"));
        Product maxProduct = GenericUtils.max(p1, p2);
        System.out.printf("  GenericUtils.max(Budget Mouse, 4K Display) -> %s ($%,.2f)%n",
                maxProduct.getName(), maxProduct.getPrice());

        // Null policy validation
        try {
            GenericUtils.max(null, "Test");
            System.err.println("  [FAIL] Expected NullPointerException was not thrown!");
        } catch (NullPointerException npe) {
            System.out.printf("  [PASS] Null input correctly rejected: '%s'%n", npe.getMessage());
        }

        System.out.println("\nWhy <T extends Comparable<T>>?");
        System.out.println("  - It allows one single method implementation to work for any comparable type.");
        System.out.println("  - Guarantees that 'first.compareTo(second)' can be invoked safely at compile time.");
    }

    /**
     * SECTION 4: Wildcards & PECS Principle Demonstration
     */
    public static void demonstrateWildcardsAndPecs() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. WILDCARDS & THE PECS PRINCIPLE (Producer Extends, Consumer Super)");
        System.out.println("--------------------------------------------------------------------------------");

        // 4A: Producer Extends (Reading from List<? extends Number>)
        List<Integer> intList = List.of(10, 20, 30, 40);
        List<Double> doubleList = List.of(1.5, 2.5, 3.5);
        List<Long> longList = List.of(100L, 200L, 300L);

        double intSum = GenericUtils.sumNumbers(intList);
        double doubleSum = GenericUtils.sumNumbers(doubleList);
        double longSum = GenericUtils.sumNumbers(longList);

        System.out.printf("  sumNumbers(List<Integer>) -> %.2f%n", intSum);
        System.out.printf("  sumNumbers(List<Double>)  -> %.2f%n", doubleSum);
        System.out.printf("  sumNumbers(List<Long>)    -> %.2f%n", longSum);

        // 4B: Consumer Super (Writing into List<? super Number>)
        System.out.println("\nDemonstrating copyNumbers(List<? extends Number>, List<? super Number>):");

        List<Number> numberDest = new ArrayList<>();
        GenericUtils.copyNumbers(intList, numberDest);
        System.out.println("  Copied List<Integer> into List<Number>:");
        System.out.println("    Destination content: " + numberDest);

        List<Object> objectDest = new ArrayList<>();
        GenericUtils.copyNumbers(doubleList, objectDest);
        System.out.println("  Copied List<Double> into List<Object>:");
        System.out.println("    Destination content: " + objectDest);

        // 4C: Unbounded Wildcard (List<?>)
        System.out.println("\nDemonstrating printItems(List<?>):");
        GenericUtils.printItems(List.of("Java", "Generics", "PECS", "Wildcards"));
        GenericUtils.printItems(List.of(101, 202, 303));
    }

    /**
     * SECTION 5: Product CSV Parsing with Try-with-Resources
     */
    public static void demonstrateCsvParsing(Path csvPath) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("5. PRODUCT CSV PARSING WITH TRY-WITH-RESOURCES (UTF-8)");
        System.out.println("--------------------------------------------------------------------------------");

        ProductCsvService service = new ProductCsvService();
        System.out.printf("Reading products from: %s%n", csvPath);

        try {
            List<Product> products = service.loadProducts(csvPath);
            System.out.printf("Successfully loaded %d products:%n", products.size());
            for (Product p : products) {
                System.out.println("  " + p);
            }
        } catch (IOException e) {
            System.err.printf("[ERROR] Failed to read CSV file: %s%n", e.getMessage());
        } catch (CsvParseException e) {
            System.err.printf("[ERROR] Failed to parse CSV at line %d: %s%n", e.getLineNumber(), e.getMessage());
        }

        // Deliberate malformed CSV demonstration
        System.out.println("\nTesting error reporting with non-existent CSV path:");
        try {
            service.loadProducts(Paths.get("data", "non_existent_file.csv"));
            System.err.println("  [FAIL] Expected IOException was not thrown!");
        } catch (IOException e) {
            System.out.printf("  [PASS] Intercepted expected file error: %s%n", e.getMessage());
        } catch (CsvParseException e) {
            System.err.println("  [FAIL] Unexpected CsvParseException: " + e.getMessage());
        }
    }

    /**
     * SECTION 6: AutoCloseable Reverse Closing Order Demonstration
     */
    public static void demonstrateReverseCloseOrder() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("6. AutoCloseable REVERSE DECLARATION CLOSING ORDER");
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("Opening three resources in a single try-with-resources statement:");
        try (TrackedResource resA = new TrackedResource("Resource-A (Database)");
             TrackedResource resB = new TrackedResource("Resource-B (AuditStream)");
             TrackedResource resC = new TrackedResource("Resource-C (NetworkSocket)")) {

            System.out.println("  >> Inside try block: executing business operations");
            resA.doWork("Execute query");
            resB.doWork("Write audit record");
            resC.doWork("Send acknowledgement packet");
            System.out.println("  >> Exiting try block (automatic cleanup commences)");
        } catch (Exception ex) {
            System.err.println("  Unexpected exception: " + ex.getMessage());
        }

        System.out.println("\nNotice Closing Order:");
        System.out.println("  1. Resource-C was declared LAST  -> Closed FIRST.");
        System.out.println("  2. Resource-B was declared SECOND -> Closed SECOND.");
        System.out.println("  3. Resource-A was declared FIRST -> Closed LAST.");
        System.out.println("  (LIFO / Stack semantics guarantee dependent outer resources close before foundations)");
    }

    /**
     * SECTION 7: AutoCloseable Exception-Path Cleanup & Suppressed Exceptions
     */
    public static void demonstrateExceptionPathCleanup() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("7. EXCEPTION-PATH CLEANUP & SUPPRESSED EXCEPTIONS DEMONSTRATION");
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("Demonstration 7A: Try block throws an exception. Verifying resources STILL close:");
        try (TrackedResource res1 = new TrackedResource("DatabasePool");
             TrackedResource res2 = new TrackedResource("FileChannel")) {

            System.out.println("  >> Performing operations inside try block...");
            res1.doWork("Acquire lock");
            System.out.println("  >> Deliberately throwing RuntimeException inside try block!");
            throw new RuntimeException("Simulated unexpected business logic failure!");

        } catch (RuntimeException ex) {
            System.out.printf("  [PASS] Caught expected in-flight exception: '%s'%n", ex.getMessage());
            System.out.println("         (Both resources were guaranteed closed before entering catch block!)");
        } catch (Exception ex) {
            System.err.println("  Unexpected exception: " + ex.getMessage());
        }

        System.out.println("\nDemonstration 7B: Suppressed Exceptions (Close failure while exception in-flight):");
        try (TrackedResource failingRes = new TrackedResource("FaultyResource", true)) {

            System.out.println("  >> Performing operations with FaultyResource...");
            System.out.println("  >> Throwing Primary Exception from try block!");
            throw new RuntimeException("PRIMARY BUSINESS EXCEPTION from try block");

        } catch (Exception primaryEx) {
            System.out.printf("  [PRIMARY EXCEPTION CAUGHT]: %s%n", primaryEx.getMessage());

            Throwable[] suppressed = primaryEx.getSuppressed();
            System.out.printf("  [SUPPRESSED EXCEPTIONS COUNT]: %d%n", suppressed.length);
            for (Throwable supp : suppressed) {
                System.out.printf("    - Attached Suppressed Exception: %s%n", supp.getMessage());
            }

            System.out.println("\nWhy Suppressed Exceptions Matter:");
            System.out.println("  - In legacy pre-Java 7 'try-finally', if finally.close() threw an exception,");
            System.out.println("    it completely ATE / WIPED OUT the original primary business exception!");
            System.out.println("  - Try-with-resources preserves the original primary exception and attaches close()");
            System.out.println("    failures as suppressed exceptions via Throwable.addSuppressed().");
        }
    }
}
