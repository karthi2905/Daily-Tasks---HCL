package com.technova.employeedirectory.app;

import com.technova.employeedirectory.demo.HashCodeBugDemo;
import com.technova.employeedirectory.model.DepartmentSummary;
import com.technova.employeedirectory.model.Employee;
import com.technova.employeedirectory.service.EmployeeDirectoryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main console application demonstrating all required Day 8 scenarios:
 * 1. Initial Directory Seed & Display
 * 2. HashMap Fast Lookup (O(1)) & Validation Handling
 * 3. Department Frequency Counting using Map.merge()
 * 4. Alphabetically Sorted Department Report using TreeMap
 * 5. Natural Ordering by Employee ID (Comparable)
 * 6. Top Five Salaries with Chained Comparator (Salary desc, ID asc)
 * 7. Missing-hashCode() Bug & Resolution
 */
public class EmployeeDirectoryApplication {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   TECHNOVA EMPLOYEE DIRECTORY -- COLLECTIONS & OBJECT CONTRACTS (DAY 8)        ");
        System.out.println("================================================================================");

        EmployeeDirectoryService service = new EmployeeDirectoryService();

        seedSampleData(service);

        demonstrateDirectoryOverview(service);
        demonstrateHashMapLookup(service);
        demonstrateFrequencyCountsWithMerge(service);
        demonstrateSortedDepartmentReportTreeMap(service);
        demonstrateNaturalOrderingComparable(service);
        demonstrateTopFiveSalariesComparatorChaining(service);
        demonstrateHashCodeBugAndFix();

        System.out.println("\n================================================================================");
        System.out.println("                    ALL DAY 8 DEMONSTRATIONS COMPLETED                          ");
        System.out.println("================================================================================");
    }

    /**
     * Seeds 10 sample employees across 4 departments with deliberately unsorted IDs
     * and equal salaries to test tie-breaking.
     */
    private static void seedSampleData(EmployeeDirectoryService service) {
        // Insertion in non-ascending order of IDs
        service.addEmployee(new Employee(108, "Hannah Abbott", "Finance", new BigDecimal("92000.00")));
        service.addEmployee(new Employee(102, "Bob Vance", "Engineering", new BigDecimal("125000.00")));
        service.addEmployee(new Employee(105, "Edward Cullen", "Marketing", new BigDecimal("78000.00")));
        service.addEmployee(new Employee(101, "Alice Smith", "Engineering", new BigDecimal("110000.00")));
        service.addEmployee(new Employee(110, "Jack Reacher", "Engineering", new BigDecimal("125000.00")));
        service.addEmployee(new Employee(104, "Diana Prince", "HR", new BigDecimal("84000.00")));
        service.addEmployee(new Employee(107, "George Clark", "Finance", new BigDecimal("92000.00")));
        service.addEmployee(new Employee(103, "Charlie Brown", "Engineering", new BigDecimal("95000.00")));
        service.addEmployee(new Employee(109, "Ian Malcolm", "Marketing", new BigDecimal("88000.00")));
        service.addEmployee(new Employee(106, "Fiona Gallagher", "HR", new BigDecimal("84000.00")));
    }

    /**
     * 1. Overview of initial unsorted directory
     */
    private static void demonstrateDirectoryOverview(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("1. EMPLOYEE DIRECTORY OVERVIEW (Internal HashMap Storage)");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Total employees loaded: %d%n", service.getEmployeeCount());
        for (Employee emp : service.getAllEmployees()) {
            System.out.println("  " + emp);
        }
    }

    /**
     * 2. HashMap lookup demonstration (O(1) average lookup, duplicate rejection, missing ID)
     */
    private static void demonstrateHashMapLookup(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. HASHMAP LOOKUP DEMO (O(1) Average Complexity)");
        System.out.println("--------------------------------------------------------------------------------");

        // Valid ID lookup
        int targetId = 101;
        System.out.printf("Looking up Employee ID %d...%n", targetId);
        Optional<Employee> found = service.findEmployeeById(targetId);
        found.ifPresentOrElse(
                emp -> System.out.printf("  [PASS] Found: %s%n", emp),
                () -> System.err.printf("  [FAIL] Employee ID %d was not found!%n", targetId)
        );

        // Missing ID lookup
        int missingId = 999;
        System.out.printf("\nLooking up Non-Existent Employee ID %d...%n", missingId);
        Optional<Employee> notFound = service.findEmployeeById(missingId);
        if (notFound.isEmpty()) {
            System.out.println("  [PASS] Correctly returned empty Optional (no crash or null pointer exception).");
        }

        // Duplicate ID insertion rejection
        System.out.println("\nAttempting to add duplicate Employee with existing ID 101...");
        try {
            service.addEmployee(new Employee(101, "Imposter Alice", "Engineering", new BigDecimal("99999.00")));
            System.err.println("  [FAIL] Duplicate ID 101 was unexpectedly allowed!");
        } catch (IllegalArgumentException ex) {
            System.out.printf("  [PASS] Intercepted expected validation error: %s%n", ex.getMessage());
        }
    }

    /**
     * 3. Department frequency counts using Map.merge()
     */
    private static void demonstrateFrequencyCountsWithMerge(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. DEPARTMENT FREQUENCY COUNTS (Using Map.merge(dept, 1, Integer::sum))");
        System.out.println("--------------------------------------------------------------------------------");

        Map<String, Integer> counts = service.getDepartmentFrequencyCounts();
        counts.forEach((dept, count) ->
                System.out.printf("  %-14s : %2d employee(s)%n", dept, count)
        );

        System.out.println("\nHow Map.merge() Works:");
        System.out.println("  1. If department is not yet in the map, it sets count = 1.");
        System.out.println("  2. If department already exists, it calls Integer::sum(existingCount, 1).");
        System.out.println("  3. Completely avoids boilerplate 'if (!map.containsKey()) ... else ...'.");
    }

    /**
     * 4. Sorted department report using TreeMap
     */
    private static void demonstrateSortedDepartmentReportTreeMap(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. SORTED DEPARTMENT REPORT (Using TreeMap<String, DepartmentSummary>)");
        System.out.println("--------------------------------------------------------------------------------");

        Map<String, DepartmentSummary> report = service.getDepartmentReports();

        for (DepartmentSummary summary : report.values()) {
            System.out.printf("%n=== Department: %s ===%n", summary.getDepartmentName());
            System.out.printf("  Headcount: %d | Average Salary: $%,.2f | Total Payroll: $%,.2f%n",
                    summary.getEmployeeCount(), summary.getAverageSalary(), summary.getTotalSalary());
            System.out.println("  Members:");
            for (Employee emp : summary.getEmployees()) {
                System.out.printf("    - [ID: %04d] %-16s ($%,.2f)%n", emp.getId(), emp.getName(), emp.getSalary());
            }
        }

        System.out.println("\nWhy TreeMap?");
        System.out.println("  - Guarantees natural alphabetical ordering of department keys (ENGINEERING -> FINANCE -> HR -> MARKETING).");
        System.out.println("  - Self-balancing Red-Black binary search tree: O(log K) insertion & retrieval.");
    }

    /**
     * 5. Natural ordering by Employee ID (Comparable)
     */
    private static void demonstrateNaturalOrderingComparable(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("5. EMPLOYEES SORTED BY NATURAL ORDERING (Comparable: Ascending Employee ID)");
        System.out.println("--------------------------------------------------------------------------------");

        List<Employee> naturalSorted = service.getEmployeesSortedById();
        for (Employee emp : naturalSorted) {
            System.out.println("  " + emp);
        }

        System.out.println("\nKey Takeaway: Natural ordering is defined within the Employee class itself");
        System.out.println("              via 'implements Comparable<Employee>' and compareTo().");
    }

    /**
     * 6. Top Five Salaries with Comparator Chaining
     */
    private static void demonstrateTopFiveSalariesComparatorChaining(EmployeeDirectoryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("6. TOP FIVE SALARIES (Comparator Chaining: Salary Descending, ID Ascending)");
        System.out.println("--------------------------------------------------------------------------------");

        List<Employee> topFive = service.getTopPaidEmployees(5);

        System.out.println("Top 5 Compensated Employees (with ID tie-breaker for identical salaries):");
        int rank = 1;
        for (Employee emp : topFive) {
            System.out.printf("  #%d. %s%n", rank++, emp);
        }

        System.out.println("\nTie-Breaker Verification:");
        System.out.println("  - Notice Bob Vance (ID: 102) and Jack Reacher (ID: 110) both earn $125,000.00.");
        System.out.println("  - Because the secondary comparator is ascending ID, ID 102 correctly precedes ID 110.");
    }

    /**
     * 7. Missing-hashCode bug demonstration
     */
    private static void demonstrateHashCodeBugAndFix() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("7. MISSING-hashCode() BUG & RESOLUTION");
        System.out.println("--------------------------------------------------------------------------------");
        HashCodeBugDemo.runBrokenDemo();
        HashCodeBugDemo.runFixedDemo();
        HashCodeBugDemo.printContractSummary();
    }
}
