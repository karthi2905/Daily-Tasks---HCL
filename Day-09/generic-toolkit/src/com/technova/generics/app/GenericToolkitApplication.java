package com.technova.generics.app;

import com.technova.generics.demo.GenericToolkitDemo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application runner for Day 9 — Generic Toolkit: Generics, File I/O & Resource Management.
 *
 * Coordinates execution of:
 * 1. Generic Pair<K,V>
 * 2. Bounded Box<T extends Number>
 * 3. Generic max() with Comparable
 * 4. Wildcard Variance & PECS
 * 5. UTF-8 CSV Parsing with try-with-resources
 * 6. AutoCloseable Reverse LIFO Closing Order
 * 7. Exception Cleanup & Suppressed Exceptions
 */
public class GenericToolkitApplication {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   TECHNOVA GENERIC TOOLKIT: GENERICS, FILE I/O & RESOURCE MANAGEMENT (DAY 9)   ");
        System.out.println("================================================================================");

        Path csvPath = resolveCsvPath(args);

        GenericToolkitDemo.demonstratePairs();
        GenericToolkitDemo.demonstrateBoxes();
        GenericToolkitDemo.demonstrateGenericMax();
        GenericToolkitDemo.demonstrateWildcardsAndPecs();
        GenericToolkitDemo.demonstrateCsvParsing(csvPath);
        GenericToolkitDemo.demonstrateReverseCloseOrder();
        GenericToolkitDemo.demonstrateExceptionPathCleanup();

        System.out.println("\n================================================================================");
        System.out.println("                    ALL DAY 9 DEMONSTRATIONS COMPLETED                          ");
        System.out.println("================================================================================");
    }

    /**
     * Resolves the CSV path from command-line arguments or falls back to standard defaults.
     */
    private static Path resolveCsvPath(String[] args) {
        if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            return Paths.get(args[0].trim());
        }

        // Try local relative directory
        Path localDefault = Paths.get("data", "products.csv");
        if (Files.exists(localDefault)) {
            return localDefault;
        }

        // Try Day-09 relative directory
        Path day9Default = Paths.get("Day-09", "generic-toolkit", "data", "products.csv");
        if (Files.exists(day9Default)) {
            return day9Default;
        }

        // Return local default path
        return localDefault;
    }
}
