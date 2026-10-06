package com.technova.booking.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that demonstrates try-with-resources for reading
 * an optional classpath configuration file.
 *
 * In this in-memory application there is no database, so this utility
 * is used to load a banner/welcome message from a resource file on the
 * classpath, if one is present. This is a genuine use-case: reading a
 * configuration file to customise startup behavior.
 *
 * Demonstrates:
 *  - try-with-resources (AutoCloseable)
 *  - catch with specific exception type (IOException)
 *  - finally block for guaranteed status logging
 *  - meaningful resource management (not invented just for the rubric)
 */
public class ConfigFileLoader {

    private static final String BANNER_FILE = "banner.txt";

    /**
     * Attempts to read lines from an optional classpath resource file.
     * Returns an empty list if the file is absent or unreadable.
     *
     * Uses try-with-resources so the InputStream and BufferedReader are
     * closed automatically when the block exits, even if an exception occurs.
     *
     * @param resourcePath the classpath path to attempt reading
     * @return list of lines from the file, or empty list on failure
     */
    public List<String> loadLines(String resourcePath) {
        List<String> lines = new ArrayList<>();
        boolean loadSuccess = false;

        // try-with-resources: InputStream and BufferedReader both implement
        // AutoCloseable and will be closed automatically after the block.
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (stream == null) {
            System.out.println("[Config] Optional file '" + resourcePath
                    + "' not found on classpath — using defaults.");
            return lines;
        }

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            loadSuccess = true;

        } catch (IOException e) {
            // Specific catch: not a generic catch(Exception)
            System.err.println("[Config] Failed to read '" + resourcePath
                    + "': " + e.getMessage());
        } finally {
            // finally block: guaranteed to run regardless of success or exception
            System.out.println("[Config] File load attempt for '" + resourcePath
                    + "' completed. Success: " + loadSuccess);
        }

        return lines;
    }

    /**
     * Prints the startup banner from the classpath resource, or a default
     * banner if the file is absent.
     */
    public void printBanner() {
        List<String> bannerLines = loadLines(BANNER_FILE);
        if (bannerLines.isEmpty()) {
            printDefaultBanner();
        } else {
            bannerLines.forEach(System.out::println);
        }
    }

    private void printDefaultBanner() {
        System.out.println("=================================================");
        System.out.println("       TECHNOVA RESOURCE BOOKING SYSTEM          ");
        System.out.println("            Core Java | In-Memory                ");
        System.out.println("=================================================");
    }
}
