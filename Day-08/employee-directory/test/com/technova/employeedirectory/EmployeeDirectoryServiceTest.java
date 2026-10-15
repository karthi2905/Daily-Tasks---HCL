package com.technova.employeedirectory;

import com.technova.employeedirectory.model.DepartmentSummary;
import com.technova.employeedirectory.model.Employee;
import com.technova.employeedirectory.service.EmployeeDirectoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day 08 :: Employee Directory & Object Contracts Test Suite")
class EmployeeDirectoryServiceTest {

    private EmployeeDirectoryService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeDirectoryService();
    }

    @Test
    @DisplayName("1. Employee ID must be strictly positive (> 0)")
    void testEmployeeIdMustBePositive() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee(0, "John Doe", "Engineering", new BigDecimal("50000")));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(-5, "Jane Doe", "HR", new BigDecimal("60000")));
    }

    @Test
    @DisplayName("2. Blank names, blank departments, and negative salaries must be rejected")
    void testBlankFieldsAndNegativeSalaryRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee(1, "   ", "Engineering", new BigDecimal("50000")));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(2, "Alice", "   ", new BigDecimal("50000")));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(3, "Bob", "Engineering", null));

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(4, "Charlie", "Engineering", new BigDecimal("-100.00")));
    }

    @Test
    @DisplayName("3. Duplicate employee IDs are rejected with an IllegalArgumentException")
    void testDuplicateEmployeeIdRejected() {
        Employee emp1 = new Employee(101, "Alice Smith", "Engineering", new BigDecimal("90000"));
        Employee emp2 = new Employee(101, "Alice Clone", "Engineering", new BigDecimal("95000"));

        service.addEmployee(emp1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.addEmployee(emp2));

        assertTrue(ex.getMessage().contains("already exists"));
        assertEquals(1, service.getEmployeeCount());
    }

    @Test
    @DisplayName("4. Looking up a valid ID returns the correct employee")
    void testLookupValidIdReturnsEmployee() {
        Employee emp = new Employee(205, "Sarah Connor", "Security", new BigDecimal("105000"));
        service.addEmployee(emp);

        Optional<Employee> found = service.findEmployeeById(205);
        assertTrue(found.isPresent());
        assertEquals("Sarah Connor", found.get().getName());
        assertEquals(new BigDecimal("105000"), found.get().getSalary());
    }

    @Test
    @DisplayName("5. Looking up a missing ID returns empty Optional without crashing")
    void testLookupMissingIdReturnsEmptyOptional() {
        Optional<Employee> missing = service.findEmployeeById(9999);
        assertNotNull(missing);
        assertTrue(missing.isEmpty(), "Non-existent employee lookup should return empty Optional");
    }

    @Test
    @DisplayName("6. Department frequency counts via Map.merge() match expected sample counts")
    void testDepartmentFrequencyCounts() {
        service.addEmployee(new Employee(1, "Emp1", "Engineering", new BigDecimal("60000")));
        service.addEmployee(new Employee(2, "Emp2", "engineering", new BigDecimal("70000"))); // Case variation
        service.addEmployee(new Employee(3, "Emp3", "HR", new BigDecimal("65000")));
        service.addEmployee(new Employee(4, "Emp4", "Finance", new BigDecimal("80000")));

        Map<String, Integer> counts = service.getDepartmentFrequencyCounts();

        assertEquals(3, counts.size(), "Should normalize case variations into 3 distinct departments");
        assertEquals(2, counts.get("ENGINEERING"));
        assertEquals(1, counts.get("HR"));
        assertEquals(1, counts.get("FINANCE"));
    }

    @Test
    @DisplayName("7. TreeMap department report keys are sorted alphabetically")
    void testDepartmentReportSortedAlphabetically() {
        service.addEmployee(new Employee(1, "E1", "Marketing", new BigDecimal("60000")));
        service.addEmployee(new Employee(2, "E2", "Accounting", new BigDecimal("75000")));
        service.addEmployee(new Employee(3, "E3", "Engineering", new BigDecimal("90000")));

        Map<String, DepartmentSummary> reports = service.getDepartmentReports();

        List<String> keys = new ArrayList<>(reports.keySet());
        assertEquals(List.of("ACCOUNTING", "ENGINEERING", "MARKETING"), keys,
                "TreeMap keys must appear in alphabetical order");
    }

    @Test
    @DisplayName("8. Natural ordering (Comparable) sorts employees by ascending employee ID")
    void testNaturalOrderingByAscendingId() {
        // Insert in reverse order
        service.addEmployee(new Employee(300, "Zoe", "HR", new BigDecimal("70000")));
        service.addEmployee(new Employee(100, "Adam", "HR", new BigDecimal("80000")));
        service.addEmployee(new Employee(200, "Maya", "HR", new BigDecimal("90000")));

        List<Employee> sorted = service.getEmployeesSortedById();

        assertEquals(100, sorted.get(0).getId());
        assertEquals(200, sorted.get(1).getId());
        assertEquals(300, sorted.get(2).getId());
    }

    @Test
    @DisplayName("9. Top-five salaries sort descending, and equal salaries break ties by ascending ID")
    void testTopFiveSalariesAndTieBreaking() {
        service.addEmployee(new Employee(105, "E5", "Engineering", new BigDecimal("120000"))); // Rank 1 (tied)
        service.addEmployee(new Employee(102, "E2", "Engineering", new BigDecimal("120000"))); // Rank 1 (tied, lower ID)
        service.addEmployee(new Employee(101, "E1", "HR", new BigDecimal("110000")));          // Rank 3
        service.addEmployee(new Employee(104, "E4", "Finance", new BigDecimal("95000")));       // Rank 4
        service.addEmployee(new Employee(107, "E7", "Finance", new BigDecimal("90000")));       // Rank 5
        service.addEmployee(new Employee(109, "E9", "Marketing", new BigDecimal("80000")));     // Rank 6 (excluded)

        List<Employee> topFive = service.getTopPaidEmployees(5);

        assertEquals(5, topFive.size());
        // For $120,000 tie, ID 102 must appear before ID 105
        assertEquals(102, topFive.get(0).getId());
        assertEquals(105, topFive.get(1).getId());
        assertEquals(101, topFive.get(2).getId());
        assertEquals(104, topFive.get(3).getId());
        assertEquals(107, topFive.get(4).getId());
    }

    @Test
    @DisplayName("10. An empty directory produces valid empty reports without throwing exceptions")
    void testEmptyDirectoryHandling() {
        assertEquals(0, service.getEmployeeCount());
        assertTrue(service.getAllEmployees().isEmpty());
        assertTrue(service.getDepartmentFrequencyCounts().isEmpty());
        assertTrue(service.getDepartmentReports().isEmpty());
        assertTrue(service.getEmployeesSortedById().isEmpty());
        assertTrue(service.getTopPaidEmployees(5).isEmpty());
    }

    @Test
    @DisplayName("11. Two equal Employee objects have identical hash codes")
    void testEqualsAndHashCodeAgreement() {
        Employee e1 = new Employee(42, "Arthur Dent", "Galactic", new BigDecimal("42000"));
        Employee e2 = new Employee(42, "Arthur Dent (Duplicate)", "Galactic", new BigDecimal("99000"));

        assertEquals(e1, e2, "Employees with identical IDs must be equal");
        assertEquals(e1.hashCode(), e2.hashCode(), "Equal employees MUST have identical hash codes");
    }

    @Test
    @DisplayName("12. A HashSet does not store duplicate real Employee identities")
    void testHashSetDuplicatePrevention() {
        Employee e1 = new Employee(42, "Arthur Dent", "Galactic", new BigDecimal("42000"));
        Employee e2 = new Employee(42, "Arthur Dent (Duplicate)", "Galactic", new BigDecimal("99000"));

        Set<Employee> set = new HashSet<>();
        assertTrue(set.add(e1));
        assertFalse(set.add(e2), "Duplicate employee ID must be rejected by HashSet");
        assertEquals(1, set.size());
        assertTrue(set.contains(e2), "HashSet must find equivalent employee object via hash + equals");
    }
}
