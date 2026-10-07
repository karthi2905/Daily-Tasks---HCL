package com.technova.usage;

/**
 * Monthly Usage Analyser
 * Day 2 Core Java Training Assessment
 *
 * Demonstrates:
 * 1. 1-D primitive int[] array for 12 months
 * 2. Static final constants for billing slabs
 * 3. Total usage calculation with loops
 * 4. Average calculation with integer division vs explicit (double) casting
 * 5. Maximum and Minimum usage with corresponding month identification (manual loops)
 * 6. Character grade assignment using nested ternary operators
 * 7. Slab classification using constants
 * 8. Integer overflow demonstration and resolution using long
 * 9. Long accumulator for large array summations
 * 10. 2-D array representing monthly usage across 3 residential houses
 * 11. Nested loop processing for multi-dimensional data
 * 12. Modular method design and clean console reporting
 */
public class MonthlyUsageAnalyser {

    // -------------------------------------------------------------------------
    // Slab Constants
    // -------------------------------------------------------------------------
    private static final int SLAB_1_LIMIT = 100;
    private static final int SLAB_2_LIMIT = 200;

    private static final double SLAB_1_RATE = 2.0;
    private static final double SLAB_2_RATE = 3.5;
    private static final double SLAB_3_RATE = 5.0;

    // -------------------------------------------------------------------------
    // Month Names Reference Array
    // -------------------------------------------------------------------------
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    };

    // Short month abbreviations for compact tabular reporting
    private static final String[] SHORT_MONTHS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // =========================================================================
    // Main Entry Point
    // =========================================================================
    public static void main(String[] args) {
        // 1. Single house monthly usage data (12 months)
        int[] monthlyUsage = {
            120, 150, 180, 220,
            250, 300, 280, 260,
            210, 190, 160, 140
        };

        // 2. Display raw monthly usage
        displayMonthlyUsage(monthlyUsage, MONTH_NAMES);

        // 3. Perform and display monthly metrics analysis
        performMonthlyAnalysis(monthlyUsage, MONTH_NAMES);

        // 4. 2-D array representing 3 houses across 12 months
        int[][] houseUsage = {
            {120, 150, 180, 220, 250, 300, 280, 260, 210, 190, 160, 140},
            {100, 130, 160, 190, 220, 240, 230, 210, 180, 160, 140, 120},
            {200, 220, 240, 260, 280, 300, 320, 310, 290, 270, 250, 230}
        };

        // 5. Display 2-D house usage table
        displayHouseUsage(houseUsage, SHORT_MONTHS);

        // 6. Calculate and display 3-house summary
        performThreeHouseAnalysis(houseUsage);

        // 7. Demonstrate integer overflow and long resolution
        demonstrateOverflow();

        // 8. Completion Banner
        System.out.println("========================================================");
        System.out.println("             ANALYSIS COMPLETE");
        System.out.println("========================================================");
    }

    // =========================================================================
    // 1-D Array Operations: Display & Basic Metrics
    // =========================================================================

    /**
     * Displays raw monthly usage in a structured key-value layout.
     */
    public static void displayMonthlyUsage(int[] usage, String[] months) {
        System.out.println("========================================================");
        System.out.println("             MONTHLY USAGE ANALYSER");
        System.out.println("========================================================");
        System.out.println();
        System.out.println("Monthly Usage");
        System.out.println("--------------------------------------------------------");
        for (int i = 0; i < usage.length; i++) {
            System.out.printf("%-13s: %d%n", months[i], usage[i]);
        }
        System.out.println();
    }

    /**
     * Calculates the sum of all monthly usage values using an enhanced for-loop.
     */
    public static int calculateTotal(int[] usage) {
        int total = 0;
        for (int u : usage) {
            total += u;
        }
        return total;
    }

    /**
     * Calculates the average usage with explicit floating-point casting.
     */
    public static double calculateAverage(int total, int count) {
        return (double) total / count;
    }

    /**
     * Finds the maximum usage value using a standard iterative loop.
     */
    public static int findMaximum(int[] usage) {
        int max = usage[0];
        for (int i = 1; i < usage.length; i++) {
            if (usage[i] > max) {
                max = usage[i];
            }
        }
        return max;
    }

    /**
     * Identifies the 0-based month index of the maximum usage value.
     */
    public static int findMaxMonthIndex(int[] usage) {
        int maxIndex = 0;
        for (int i = 1; i < usage.length; i++) {
            if (usage[i] > usage[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Finds the minimum usage value using a standard iterative loop.
     */
    public static int findMinimum(int[] usage) {
        int min = usage[0];
        for (int i = 1; i < usage.length; i++) {
            if (usage[i] < min) {
                min = usage[i];
            }
        }
        return min;
    }

    /**
     * Identifies the 0-based month index of the minimum usage value.
     */
    public static int findMinMonthIndex(int[] usage) {
        int minIndex = 0;
        for (int i = 1; i < usage.length; i++) {
            if (usage[i] < usage[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    // =========================================================================
    // Classification: Slabs & Grades
    // =========================================================================

    /**
     * Classifies usage into slabs (1, 2, or 3) using predefined static final constants.
     */
    public static int determineSlab(double usage) {
        if (usage <= SLAB_1_LIMIT) {
            return 1;
        } else if (usage <= SLAB_2_LIMIT) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Assigns a character grade ('A', 'B', 'C', 'D') using the nested ternary operator.
     * Demonstrates compact conditional expression syntax.
     */
    public static char calculateGrade(double averageUsage) {
        return averageUsage <= 150 ? 'A' :
               averageUsage <= 250 ? 'B' :
               averageUsage <= 350 ? 'C' :
               'D';
    }

    /**
     * Executes the single-household summary and displays metrics.
     */
    private static void performMonthlyAnalysis(int[] monthlyUsage, String[] months) {
        int totalUsage = calculateTotal(monthlyUsage);

        // Demonstrating integer division vs explicit casting
        int integerAverage = totalUsage / monthlyUsage.length;
        double decimalAverage = calculateAverage(totalUsage, monthlyUsage.length);

        int maxUsage = findMaximum(monthlyUsage);
        int maxMonthIdx = findMaxMonthIndex(monthlyUsage);
        String maxMonth = months[maxMonthIdx];

        int minUsage = findMinimum(monthlyUsage);
        int minMonthIdx = findMinMonthIndex(monthlyUsage);
        String minMonth = months[minMonthIdx];

        int slab = determineSlab(decimalAverage);
        char grade = calculateGrade(decimalAverage);

        System.out.println("========================================================");
        System.out.println("             MONTHLY ANALYSIS");
        System.out.println("========================================================");
        System.out.printf("Total Usage       : %d units%n", totalUsage);
        System.out.printf("Integer Average   : %d units%n", integerAverage);
        System.out.printf("Decimal Average   : %.2f units%n", decimalAverage);
        System.out.printf("Maximum Usage     : %d units%n", maxUsage);
        System.out.printf("Maximum Month     : %s%n", maxMonth);
        System.out.printf("Minimum Usage     : %d units%n", minUsage);
        System.out.printf("Minimum Month     : %s%n", minMonth);
        System.out.printf("Usage Slab        : %d%n", slab);
        System.out.printf("Usage Grade       : %c%n", grade);
        System.out.println();
    }

    // =========================================================================
    // 2-D Array Operations: Multi-House Analysis
    // =========================================================================

    /**
     * Prints the 2-D array representing 3 houses across all 12 months in a tabular format.
     */
    public static void displayHouseUsage(int[][] houseUsage, String[] shortMonths) {
        System.out.println("========================================================");
        System.out.println("                 HOUSE USAGE");
        System.out.println("========================================================");
        System.out.println();

        // Header row with months
        System.out.printf("%-10s", "House");
        for (String month : shortMonths) {
            System.out.printf("%5s", month);
        }
        System.out.println();

        // Data rows for each house
        for (int house = 0; house < houseUsage.length; house++) {
            System.out.printf("%-10s", "House " + (house + 1));
            for (int month = 0; month < houseUsage[house].length; month++) {
                System.out.printf("%5d", houseUsage[house][month]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Calculates the annual total for each house using nested loops.
     */
    public static int[] calculateHouseTotals(int[][] houseUsage) {
        int[] totals = new int[houseUsage.length];
        for (int house = 0; house < houseUsage.length; house++) {
            int sum = 0;
            for (int month = 0; month < houseUsage[house].length; month++) {
                sum += houseUsage[house][month];
            }
            totals[house] = sum;
        }
        return totals;
    }

    /**
     * Identifies the index of the house with the highest annual usage.
     */
    public static int findHighestUsageHouse(int[] houseTotals) {
        int highestIndex = 0;
        for (int i = 1; i < houseTotals.length; i++) {
            if (houseTotals[i] > houseTotals[highestIndex]) {
                highestIndex = i;
            }
        }
        return highestIndex;
    }

    /**
     * Coordinates and displays the 3-house total comparisons.
     */
    private static void performThreeHouseAnalysis(int[][] houseUsage) {
        int[] houseTotals = calculateHouseTotals(houseUsage);
        int highestHouseIdx = findHighestUsageHouse(houseTotals);

        System.out.println("========================================================");
        System.out.println("             THREE HOUSE ANALYSIS");
        System.out.println("========================================================");
        for (int i = 0; i < houseTotals.length; i++) {
            System.out.printf("House %d Total     : %d units%n", (i + 1), houseTotals[i]);
        }
        System.out.println();
        System.out.printf("Highest Usage House: House %d%n", (highestHouseIdx + 1));
        System.out.printf("Annual Usage       : %d units%n", houseTotals[highestHouseIdx]);
        System.out.println();
    }

    // =========================================================================
    // Integer Overflow & Type Promotion Demonstration
    // =========================================================================

    /**
     * Demonstrates integer overflow when numbers exceed 32-bit signed int capacity,
     * and shows how casting to 64-bit long before addition guarantees correct results.
     */
    public static void demonstrateOverflow() {
        System.out.println("========================================================");
        System.out.println("             INTEGER OVERFLOW DEMO");
        System.out.println("========================================================");

        int usage1 = 2_000_000_000;
        int usage2 = 1_500_000_000;

        // Problem: 32-bit signed int overflow (wraps around)
        int incorrectTotal = usage1 + usage2;

        // Solution: Promote operand to 64-bit long prior to addition
        long correctTotal = (long) usage1 + usage2;

        System.out.println();
        System.out.println("Usage 1: " + usage1);
        System.out.println("Usage 2: " + usage2);
        System.out.println();
        System.out.println("Using int:");
        System.out.println("Incorrect Total: " + incorrectTotal);
        System.out.println();
        System.out.println("Using long:");
        System.out.println("Correct Total: " + correctTotal);
        System.out.println();
        System.out.println("Why this happens:");
        System.out.println("  - 32-bit int maximum value = 2,147,483,647 (Integer.MAX_VALUE)");
        System.out.println("  - Mathematical sum         = 3,500,000,000");
        System.out.println("  - Since 3,500,000,000 > 2,147,483,647, the 32-bit register wraps to negative.");
        System.out.println("  - Casting (long) usage1 promotes arithmetic to 64-bit, preserving accuracy.");
        System.out.println();

        // Demonstrating long accumulator for large totals
        int[] largeBatch = { 1_000_000_000, 1_200_000_000, 1_500_000_000 };
        long batchTotal = calculateLargeTotal(largeBatch);
        System.out.println("Demonstrating calculateLargeTotal (long accumulator):");
        System.out.println("Sum of 3 large items (1B + 1.2B + 1.5B) = " + batchTotal);
        System.out.println();
    }

    /**
     * Demonstrates using a long accumulator (long total = 0L;) to safely sum large values
     * that would otherwise overflow an int total variable.
     */
    public static long calculateLargeTotal(int[] largeValues) {
        long total = 0L;
        for (int value : largeValues) {
            total += (long) value;
        }
        return total;
    }
}
