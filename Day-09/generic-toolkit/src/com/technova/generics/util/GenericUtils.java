package com.technova.generics.util;

import java.util.List;
import java.util.Objects;

/**
 * Utility class demonstrating generic algorithms, bounded type parameters,
 * and wildcard variance following the PECS (Producer Extends, Consumer Super) principle.
 */
public final class GenericUtils {

    private GenericUtils() {
        // Prevent instantiation of utility class
    }

    // =========================================================================
    // 1. GENERIC MAXIMUM METHOD WITH BOUNDED TYPE PARAMETER
    // =========================================================================

    /**
     * Determines the maximum of two comparable elements.
     *
     * The bound {@code <T extends Comparable<T>>} ensures at compile time that
     * type T implements {@link Comparable} with respect to itself, allowing
     * invocation of {@link Comparable#compareTo(Object)} safely without casting.
     *
     * @param first  first comparable element (non-null)
     * @param second second comparable element (non-null)
     * @param <T>    the type of elements being compared
     * @return the larger element, or the first element if both compare equally
     * @throws NullPointerException if either argument is null
     */
    public static <T extends Comparable<T>> T max(T first, T second) {
        Objects.requireNonNull(first, "First argument to max() cannot be null");
        Objects.requireNonNull(second, "Second argument to max() cannot be null");
        return (first.compareTo(second) >= 0) ? first : second;
    }

    // =========================================================================
    // 2. WILDCARD METHOD: READ FROM UNKNOWN SUBTYPE (Producer Extends)
    // =========================================================================

    /**
     * Calculates the sum of all elements in a list of numeric values.
     *
     * Uses an UPPER BOUNDED WILDCARD {@code List<? extends Number>} because the list
     * acts strictly as a PRODUCER of numbers (we only read from it).
     *
     * According to PECS (Producer Extends, Consumer Super):
     * If you only read items of type T from a collection, declare it with {@code ? extends T}.
     * This enables the method to accept {@code List<Integer>}, {@code List<Double>},
     * {@code List<Long>}, etc.
     *
     * @param numbers list producing numeric elements
     * @return total sum as a double (0.0 if list is empty)
     * @throws NullPointerException if numbers list is null
     */
    public static double sumNumbers(List<? extends Number> numbers) {
        Objects.requireNonNull(numbers, "Numbers list cannot be null");
        double sum = 0.0;
        for (Number num : numbers) {
            if (num != null) {
                sum += num.doubleValue();
            }
        }
        return sum;
    }

    // =========================================================================
    // 3. WILDCARD METHOD: WRITE TO DESTINATION (Consumer Super)
    // =========================================================================

    /**
     * Copies all elements from a source numeric list into a destination list.
     *
     * Demonstrates both sides of PECS simultaneously:
     * - {@code source} is a PRODUCER: {@code List<? extends Number>} (we read Number from it).
     * - {@code destination} is a CONSUMER: {@code List<? super Number>} (we put Number into it).
     *
     * This allows copying from a specific subtype list (e.g. {@code List<Integer>}) into
     * any collection capable of holding general numbers (e.g. {@code List<Number>} or {@code List<Object>}).
     *
     * @param source      producer list containing numbers to copy (non-null)
     * @param destination consumer list receiving the numbers (non-null)
     * @throws NullPointerException if source or destination is null
     */
    public static void copyNumbers(List<? extends Number> source, List<? super Number> destination) {
        Objects.requireNonNull(source, "Source list cannot be null");
        Objects.requireNonNull(destination, "Destination list cannot be null");
        for (Number num : source) {
            destination.add(num);
        }
    }

    // =========================================================================
    // 4. UNBOUNDED WILDCARD: GENERIC DISPLAY
    // =========================================================================

    /**
     * Displays all items in any list regardless of element type.
     *
     * Uses an UNBOUNDED WILDCARD {@code List<?>} because the method does not rely
     * on any specific type features beyond those provided by {@link Object} (such as {@link Object#toString()}).
     *
     * @param items list of elements of unknown type
     * @throws NullPointerException if items list is null
     */
    public static void printItems(List<?> items) {
        Objects.requireNonNull(items, "Items list cannot be null");
        System.out.printf("  Displaying %d item(s):%n", items.size());
        int index = 0;
        for (Object item : items) {
            System.out.printf("    [%d] (%s) %s%n",
                    index++,
                    (item != null ? item.getClass().getSimpleName() : "null"),
                    item);
        }
    }
}
