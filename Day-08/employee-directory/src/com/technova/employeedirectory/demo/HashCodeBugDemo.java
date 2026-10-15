package com.technova.employeedirectory.demo;

import com.technova.employeedirectory.model.Employee;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Educational demonstration illustrating the catastrophic consequences of violating
 * the {@code equals()} and {@code hashCode()} contract in Java hash-based collections.
 *
 * Demonstrates:
 * Part A: Broken implementation (equals overridden without hashCode).
 * Part B: Fixed implementation (consistent equals and hashCode).
 */
public class HashCodeBugDemo {

    /**
     * Intentionally defective class that overrides equals() without overriding hashCode().
     */
    public static class BrokenEmployee {
        private final int id;
        private final String name;

        public BrokenEmployee(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        /**
         * Overrides equals based on ID.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenEmployee that)) return false;
            return this.id == that.id;
        }

        // Intentionally NOT overriding hashCode()!
        // Inherits default java.lang.Object.hashCode() (identity-based address hash).

        @Override
        public String toString() {
            return String.format("BrokenEmployee[ID=%d, name='%s', identityHash=%d]",
                    id, name, System.identityHashCode(this));
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("      JAVA OBJECT CONTRACTS: MISSING-hashCode() BUG & RESOLUTION DEMO           ");
        System.out.println("================================================================================");

        runBrokenDemo();
        runFixedDemo();

        printContractSummary();
    }

    /**
     * PART A: Broken Implementation Demonstration
     */
    public static void runBrokenDemo() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("PART A: BROKEN IMPLEMENTATION (equals() without hashCode())");
        System.out.println("--------------------------------------------------------------------------------");

        BrokenEmployee b1 = new BrokenEmployee(101, "Alice");
        BrokenEmployee b2 = new BrokenEmployee(101, "Alice");

        System.out.println("Step 1: Created two separate objects with the same ID (101):");
        System.out.println("  Object 1: " + b1);
        System.out.println("  Object 2: " + b2);

        System.out.println("\nStep 2: Checking logical equality with equals():");
        boolean areEqual = b1.equals(b2);
        System.out.printf("  b1.equals(b2) -> %b [OBJECTS ARE LOGICALLY EQUAL]%n", areEqual);

        System.out.println("\nStep 3: Inspecting observed hash codes:");
        int hash1 = b1.hashCode();
        int hash2 = b2.hashCode();
        System.out.printf("  b1.hashCode() -> %d%n", hash1);
        System.out.printf("  b2.hashCode() -> %d%n", hash2);
        boolean hashCodesMatch = (hash1 == hash2);
        System.out.printf("  Hash codes match? -> %b%n", hashCodesMatch);

        if (!hashCodesMatch) {
            System.out.println("  [CONTRACT VIOLATION OBSERVED]: b1.equals(b2) is TRUE, but their hashCodes DIFFER!");
        } else {
            System.out.println("  [NOTE]: Random identity hash collision occurred on this JVM run.");
        }

        System.out.println("\nStep 4: Adding b1 to a HashSet<BrokenEmployee>:");
        Set<BrokenEmployee> brokenSet = new HashSet<>();
        brokenSet.add(b1);
        System.out.printf("  brokenSet.add(b1) -> true. Set size: %d%n", brokenSet.size());

        System.out.println("\nStep 5: Querying HashSet for b2 using brokenSet.contains(b2):");
        boolean containsB2 = brokenSet.contains(b2);
        System.out.printf("  brokenSet.contains(b2) -> %b%n", containsB2);
        if (!containsB2) {
            System.out.println("  [BUG REPRODUCED]: The HashSet CANNOT find 'b2' because it computed b2's hash code,");
            System.out.println("                    inspected the wrong hash bucket, and never even called equals()!");
        }

        System.out.println("\nStep 6: Attempting to add b2 to the same HashSet:");
        boolean addedB2 = brokenSet.add(b2);
        System.out.printf("  brokenSet.add(b2) -> %b. Set size now: %d%n", addedB2, brokenSet.size());
        if (addedB2 && brokenSet.size() == 2) {
            System.out.println("  [BUG REPRODUCED]: Duplicate 'equal' employees now coexist in a Set!");
            System.out.println("                    HashSet's contract of uniqueness is completely broken.");
        }
    }

    /**
     * PART B: Correct Implementation Demonstration
     */
    public static void runFixedDemo() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("PART B: CORRECT IMPLEMENTATION (Employee with consistent equals() & hashCode())");
        System.out.println("--------------------------------------------------------------------------------");

        Employee e1 = new Employee(101, "Alice", "Engineering", new BigDecimal("85000.00"));
        Employee e2 = new Employee(101, "Alice", "Engineering", new BigDecimal("85000.00"));

        System.out.println("Step 1: Created two separate Employee objects with the same ID (101):");
        System.out.println("  Employee 1: " + e1);
        System.out.println("  Employee 2: " + e2);

        System.out.println("\nStep 2: Checking logical equality with equals():");
        System.out.printf("  e1.equals(e2) -> %b [LOGICALLY EQUAL]%n", e1.equals(e2));

        System.out.println("\nStep 3: Inspecting observed hash codes:");
        System.out.printf("  e1.hashCode() -> %d%n", e1.hashCode());
        System.out.printf("  e2.hashCode() -> %d%n", e2.hashCode());
        System.out.printf("  e1.hashCode() == e2.hashCode() -> %b [CONSISTENT CONTRACT]%n",
                e1.hashCode() == e2.hashCode());

        System.out.println("\nStep 4: Adding e1 to a HashSet<Employee>:");
        Set<Employee> fixedSet = new HashSet<>();
        fixedSet.add(e1);
        System.out.printf("  fixedSet.add(e1) -> true. Set size: %d%n", fixedSet.size());

        System.out.println("\nStep 5: Querying HashSet for e2 using fixedSet.contains(e2):");
        boolean containsE2 = fixedSet.contains(e2);
        System.out.printf("  fixedSet.contains(e2) -> %b [FOUND SUCCESSFULLY]%n", containsE2);

        System.out.println("\nStep 6: Attempting to add e2 to the HashSet:");
        boolean addedE2 = fixedSet.add(e2);
        System.out.printf("  fixedSet.add(e2) -> %b. Set size remains: %d [DUPLICATE PREVENTED]%n",
                addedE2, fixedSet.size());
    }

    /**
     * Summary of Java Object Contracts
     */
    public static void printContractSummary() {
        System.out.println("\n================================================================================");
        System.out.println("           THE FUNDAMENTAL JAVA EQUALS & HASHCODE CONTRACT                      ");
        System.out.println("================================================================================");
        System.out.println("1. If a.equals(b) is TRUE, then a.hashCode() MUST EQUAL b.hashCode().");
        System.out.println("   - Reason: Hash-based collections (HashMap, HashSet, Hashtable) calculate the");
        System.out.println("     hash code first to locate the memory bucket. If equal objects have different");
        System.out.println("     hash codes, they will land in different buckets and never be compared.");
        System.out.println();
        System.out.println("2. If a.hashCode() == b.hashCode(), a.equals(b) IS NOT NECESSARILY TRUE.");
        System.out.println("   - Reason: Hash collisions are natural and unavoidable due to the Pigeonhole Principle.");
        System.out.println("     Collections resolve collisions by chaining elements in the same bucket and");
        System.out.println("     using equals() to locate the precise match.");
        System.out.println();
        System.out.println("3. Immutability Requirement:");
        System.out.println("   - The fields used to calculate equals() and hashCode() MUST remain stable/immutable");
        System.out.println("     while an object resides inside a hash-based collection. If an ID changes,");
        System.out.println("     the object remains trapped in its original bucket and becomes unretrievable.");
        System.out.println("================================================================================");
    }
}
