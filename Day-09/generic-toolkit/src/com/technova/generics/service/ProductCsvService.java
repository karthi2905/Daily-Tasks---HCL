package com.technova.generics.service;

import com.technova.generics.model.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for parsing and loading Product records from CSV files.
 *
 * Demonstrates:
 * - Deterministic resource management using try-with-resources with {@link BufferedReader}.
 * - Explicit character encoding (UTF-8).
 * - Rigorous line-by-line parsing with informative line-number error reporting.
 * - Guaranteed reader closure even when parsing or file read failures occur.
 */
public class ProductCsvService {

    private static final String EXPECTED_HEADER = "id,name,category,price";

    /**
     * Loads and parses all products from the given CSV file path.
     *
     * @param csvPath path to the products CSV file
     * @return unmodifiable list of successfully parsed products
     * @throws IOException       if a file I/O error occurs (e.g., file not found, unreadable)
     * @throws CsvParseException if the header is invalid or any data row is malformed
     */
    public List<Product> loadProducts(Path csvPath) throws IOException, CsvParseException {
        Objects.requireNonNull(csvPath, "CSV path cannot be null");
        if (!Files.exists(csvPath)) {
            throw new IOException(String.format("CSV file not found at path: %s", csvPath.toAbsolutePath()));
        }

        List<Product> products = new ArrayList<>();

        // Try-with-resources: automatically closes the BufferedReader upon exit
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            int lineNumber = 1;

            // Step 1: Read and validate header row
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new CsvParseException("CSV file is completely empty", lineNumber);
            }

            if (!normalizeHeader(headerLine).equalsIgnoreCase(EXPECTED_HEADER)) {
                throw new CsvParseException(
                        String.format("Invalid CSV header. Expected '%s', but found '%s'", EXPECTED_HEADER, headerLine),
                        lineNumber);
            }

            // Step 2: Parse subsequent data rows
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue; // Skip intentional blank lines safely
                }

                Product product = parseProductLine(trimmed, lineNumber);
                products.add(product);
            }
        }

        return Collections.unmodifiableList(products);
    }

    /**
     * Overloaded convenience method accepting a file path string.
     */
    public List<Product> loadProducts(String filePath) throws IOException, CsvParseException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        return loadProducts(Paths.get(filePath));
    }

    /**
     * Parses a single row into a validated {@link Product}.
     */
    private Product parseProductLine(String line, int lineNumber) throws CsvParseException {
        String[] parts = line.split(",", -1);
        if (parts.length != 4) {
            throw new CsvParseException(
                    String.format("Expected 4 comma-separated fields, but found %d in line '%s'", parts.length, line),
                    lineNumber);
        }

        String rawId = parts[0].trim();
        String name = parts[1].trim();
        String category = parts[2].trim();
        String rawPrice = parts[3].trim();

        if (rawId.isEmpty()) {
            throw new CsvParseException("Product ID cannot be blank", lineNumber);
        }
        if (name.isEmpty()) {
            throw new CsvParseException("Product name cannot be blank", lineNumber);
        }
        if (category.isEmpty()) {
            throw new CsvParseException("Product category cannot be blank", lineNumber);
        }
        if (rawPrice.isEmpty()) {
            throw new CsvParseException("Product price cannot be blank", lineNumber);
        }

        int id;
        try {
            id = Integer.parseInt(rawId);
            if (id <= 0) {
                throw new CsvParseException(String.format("Product ID must be positive, found: %d", id), lineNumber);
            }
        } catch (NumberFormatException nfe) {
            throw new CsvParseException(String.format("Invalid integer for Product ID: '%s'", rawId), lineNumber, nfe);
        }

        BigDecimal price;
        try {
            price = new BigDecimal(rawPrice);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new CsvParseException(String.format("Product price cannot be negative, found: %s", rawPrice), lineNumber);
            }
        } catch (NumberFormatException nfe) {
            throw new CsvParseException(String.format("Invalid decimal format for Product price: '%s'", rawPrice), lineNumber, nfe);
        }

        return new Product(id, name, category, price);
    }

    private String normalizeHeader(String header) {
        // Strip BOM if present and remove extra spaces
        String clean = header.replace("\uFEFF", "").trim();
        return clean.replaceAll("\\s*,\\s*", ",");
    }
}
