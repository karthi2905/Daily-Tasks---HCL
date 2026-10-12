package com.technova.library.benchmark;

import java.util.*;

/**
 * Performance experiment comparing ArrayList and LinkedList across:
 * 1. Indexed Access: Repeated get(index) operations.
 * 2. Head Insertions: add(0, element).
 * 3. Middle Insertions: add(size / 2, element).
 * 4. Tail Appends: add(element).
 *
 * NOTE: This is a standalone diagnostic timing experiment using System.nanoTime()
 * with pre-warming loops and multiple trials. It illustrates algorithmic time-complexity
 * trade-offs and memory access patterns (contiguous array vs pointer chasing).
 */
public class CollectionBenchmark {

    private static final int DEFAULT_DATASET_SIZE = 25_000;
    private static final int DEFAULT_ACCESS_QUERIES = 10_000;
    private static final int DEFAULT_HEAD_INSERTIONS = 20_000;
    private static final int DEFAULT_MIDDLE_INSERTIONS = 10_000;
    private static final int TRIALS = 5;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     TECHNOVA COLLECTIONS BENCHMARK: ArrayList vs LinkedList                    ");
        System.out.println("================================================================================");
        System.out.printf("Environment: Java %s (%s) on %s%n",
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("os.name"));
        System.out.println("Trials per experiment: " + TRIALS + " (Reporting Average and Median)");
        System.out.println("--------------------------------------------------------------------------------");

        warmUpJvm();

        runIndexedAccessBenchmark(DEFAULT_DATASET_SIZE, DEFAULT_ACCESS_QUERIES);
        runHeadInsertionBenchmark(DEFAULT_HEAD_INSERTIONS);
        runMiddleInsertionBenchmark(DEFAULT_MIDDLE_INSERTIONS);
        runTailAppendBenchmark(DEFAULT_DATASET_SIZE);

        printComplexitySummary();
    }

    /**
     * JVM Warm-Up: Executes miniature benchmark loops to trigger JIT compilation
     * (C1/C2 tiers) and avoid cold-start bias.
     */
    private static void warmUpJvm() {
        System.out.print("[WARM-UP] Warming up JVM JIT compiler...");
        for (int i = 0; i < 3; i++) {
            List<Integer> a = new ArrayList<>();
            List<Integer> l = new LinkedList<>();
            for (int j = 0; j < 5000; j++) {
                a.add(j);
                l.add(j);
            }
            long sum = 0;
            for (int j = 0; j < 2000; j++) {
                sum += a.get(j % 5000) + l.get(j % 5000);
            }
            if (sum == 0) System.out.print(".");
        }
        System.out.println(" Done.\n");
    }

    /**
     * EXPERIMENT 1: Indexed Access (get(index))
     *
     * ArrayList: Backed by contiguous Object[] array. Direct pointer arithmetic: O(1).
     * LinkedList: Doubly linked nodes. Must traverse from head or tail node-by-node: O(n).
     */
    public static void runIndexedAccessBenchmark(int size, int queryCount) {
        System.out.println("================================================================================");
        System.out.printf("EXPERIMENT 1: INDEXED ACCESS (get(index))%n");
        System.out.printf("Dataset Size: %,d elements | Query Count: %,d random lookups%n", size, queryCount);
        System.out.println("================================================================================");

        // Deterministic query index sequence (fixed seed for fairness across trials)
        int[] queryIndexes = new int[queryCount];
        Random rng = new Random(42);
        for (int i = 0; i < queryCount; i++) {
            queryIndexes[i] = rng.nextInt(size);
        }

        long[] arrayListTimes = new long[TRIALS];
        long[] linkedListTimes = new long[TRIALS];

        for (int t = 0; t < TRIALS; t++) {
            List<Integer> arrayList = new ArrayList<>(size);
            List<Integer> linkedList = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                arrayList.add(i);
                linkedList.add(i);
            }

            // Benchmark ArrayList get(index)
            long startAl = System.nanoTime();
            long checksumAl = 0;
            for (int idx : queryIndexes) {
                checksumAl += arrayList.get(idx);
            }
            long timeAl = System.nanoTime() - startAl;
            arrayListTimes[t] = timeAl;

            // Benchmark LinkedList get(index)
            long startLl = System.nanoTime();
            long checksumLl = 0;
            for (int idx : queryIndexes) {
                checksumLl += linkedList.get(idx);
            }
            long timeLl = System.nanoTime() - startLl;
            linkedListTimes[t] = timeLl;

            // Verify checksums match to ensure identical work was performed
            if (checksumAl != checksumLl) {
                throw new IllegalStateException("Checksum mismatch between lists!");
            }
        }

        printTrialResults("ArrayList", arrayListTimes);
        printTrialResults("LinkedList", linkedListTimes);

        double avgAlMs = toMs(average(arrayListTimes));
        double avgLlMs = toMs(average(linkedListTimes));
        double ratio = avgLlMs / Math.max(avgAlMs, 0.0001);
        System.out.printf("Result: ArrayList was ~%.1fx FASTER than LinkedList for random indexed access.%n", ratio);
        System.out.println("Explanation: ArrayList accesses memory via array offset [base + idx * 4/8 bytes].");
        System.out.println("             LinkedList traverses an average of N/4 node references per lookup.\n");
    }

    /**
     * EXPERIMENT 2: Head Insertions (add(0, element))
     *
     * ArrayList: Must shift all existing elements to the right via System.arraycopy: O(n).
     * LinkedList: Simply allocates a new Node and updates head pointer: O(1).
     */
    public static void runHeadInsertionBenchmark(int insertions) {
        System.out.println("================================================================================");
        System.out.printf("EXPERIMENT 2: HEAD INSERTIONS (add(0, element))%n");
        System.out.printf("Insertions Count: %,d prepends at index 0%n", insertions);
        System.out.println("================================================================================");

        long[] arrayListTimes = new long[TRIALS];
        long[] linkedListTimes = new long[TRIALS];

        for (int t = 0; t < TRIALS; t++) {
            // Benchmark ArrayList prepend
            List<Integer> arrayList = new ArrayList<>();
            long startAl = System.nanoTime();
            for (int i = 0; i < insertions; i++) {
                arrayList.add(0, i);
            }
            arrayListTimes[t] = System.nanoTime() - startAl;

            // Benchmark LinkedList prepend
            List<Integer> linkedList = new LinkedList<>();
            long startLl = System.nanoTime();
            for (int i = 0; i < insertions; i++) {
                linkedList.add(0, i);
            }
            linkedListTimes[t] = System.nanoTime() - startLl;
        }

        printTrialResults("ArrayList", arrayListTimes);
        printTrialResults("LinkedList", linkedListTimes);

        double avgAlMs = toMs(average(arrayListTimes));
        double avgLlMs = toMs(average(linkedListTimes));
        double ratio = avgAlMs / Math.max(avgLlMs, 0.0001);
        System.out.printf("Result: LinkedList was ~%.1fx FASTER than ArrayList for head insertions.%n", ratio);
        System.out.println("Explanation: LinkedList.add(0, e) is O(1) pointer adjustment.");
        System.out.println("             ArrayList.add(0, e) is O(n) due to shifting n elements each time.\n");
    }

    /**
     * EXPERIMENT 3: Middle Insertions (add(size / 2, element))
     *
     * ArrayList: O(1) locate, O(n/2) element shift.
     * LinkedList: O(n/2) node pointer traversal, O(1) link insertion.
     */
    public static void runMiddleInsertionBenchmark(int insertions) {
        System.out.println("================================================================================");
        System.out.printf("EXPERIMENT 3: MIDDLE INSERTIONS (add(size / 2, element))%n");
        System.out.printf("Insertions Count: %,d middle insertions%n", insertions);
        System.out.println("================================================================================");

        long[] arrayListTimes = new long[TRIALS];
        long[] linkedListTimes = new long[TRIALS];

        for (int t = 0; t < TRIALS; t++) {
            // Seed initial lists
            List<Integer> arrayList = new ArrayList<>();
            List<Integer> linkedList = new LinkedList<>();
            for (int i = 0; i < 2000; i++) {
                arrayList.add(i);
                linkedList.add(i);
            }

            // Benchmark ArrayList middle insertions
            long startAl = System.nanoTime();
            for (int i = 0; i < insertions; i++) {
                arrayList.add(arrayList.size() / 2, i);
            }
            arrayListTimes[t] = System.nanoTime() - startAl;

            // Benchmark LinkedList middle insertions
            long startLl = System.nanoTime();
            for (int i = 0; i < insertions; i++) {
                linkedList.add(linkedList.size() / 2, i);
            }
            linkedListTimes[t] = System.nanoTime() - startLl;
        }

        printTrialResults("ArrayList", arrayListTimes);
        printTrialResults("LinkedList", linkedListTimes);

        System.out.println("Explanation: For middle insertions, both are O(n) overall.");
        System.out.println("             ArrayList spends time on System.arraycopy memory block movement.");
        System.out.println("             LinkedList spends time traversing pointer chains node-by-node.\n");
    }

    /**
     * EXPERIMENT 4: Tail Appends (add(element))
     *
     * ArrayList: Amortized O(1). Fast contiguous array append with occasional resizing.
     * LinkedList: O(1). Appends to tail node pointer.
     */
    public static void runTailAppendBenchmark(int appends) {
        System.out.println("================================================================================");
        System.out.printf("EXPERIMENT 4: TAIL APPENDS (add(element))%n");
        System.out.printf("Appends Count: %,d elements%n", appends);
        System.out.println("================================================================================");

        long[] arrayListTimes = new long[TRIALS];
        long[] linkedListTimes = new long[TRIALS];

        for (int t = 0; t < TRIALS; t++) {
            List<Integer> arrayList = new ArrayList<>();
            long startAl = System.nanoTime();
            for (int i = 0; i < appends; i++) {
                arrayList.add(i);
            }
            arrayListTimes[t] = System.nanoTime() - startAl;

            List<Integer> linkedList = new LinkedList<>();
            long startLl = System.nanoTime();
            for (int i = 0; i < appends; i++) {
                linkedList.add(i);
            }
            linkedListTimes[t] = System.nanoTime() - startLl;
        }

        printTrialResults("ArrayList", arrayListTimes);
        printTrialResults("LinkedList", linkedListTimes);

        System.out.println("Explanation: Both have O(1) append complexity.");
        System.out.println("             ArrayList usually wins due to contiguous cache locality and");
        System.out.println("             avoiding individual Node object heap allocation overhead.\n");
    }

    // =========================================================================
    // HELPER & STATISTICAL UTILITIES
    // =========================================================================

    private static void printTrialResults(String label, long[] nanoTimes) {
        long[] sorted = nanoTimes.clone();
        Arrays.sort(sorted);
        long median = sorted[sorted.length / 2];
        long avg = average(nanoTimes);

        System.out.printf("  %-12s | Avg: %8.3f ms | Median: %8.3f ms | (Trials: %s ms)%n",
                label, toMs(avg), toMs(median), formatTrialsMs(nanoTimes));
    }

    private static long average(long[] array) {
        long sum = 0;
        for (long v : array) sum += v;
        return sum / array.length;
    }

    private static double toMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static String formatTrialsMs(long[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(String.format("%.2f", toMs(array[i])));
            if (i < array.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private static void printComplexitySummary() {
        System.out.println("================================================================================");
        System.out.println("           THEORETICAL TIME COMPLEXITY SUMMARY & TRADE-OFFS                     ");
        System.out.println("================================================================================");
        System.out.println("| Operation                           | ArrayList       | LinkedList          |");
        System.out.println("|-------------------------------------|-----------------|---------------------|");
        System.out.println("| Indexed get(index)                  | O(1)            | O(n)                |");
        System.out.println("| Insert at beginning: add(0, e)      | O(n)            | O(1)                |");
        System.out.println("| Insert at middle: add(mid, e)       | O(n) shift      | O(n) traverse + O(1)|");
        System.out.println("| Append at end: add(e)               | Amortized O(1)  | O(1)                |");
        System.out.println("| Memory overhead                     | Low (primitive  | High (24 bytes per  |");
        System.out.println("|                                     | array buffer)   | Node wrapper object)|");
        System.out.println("| CPU Cache Locality                  | High (L1/L2 hits| Low (pointer        |");
        System.out.println("|                                     | in cache lines) | chasing in heap)    |");
        System.out.println("================================================================================");
    }
}
