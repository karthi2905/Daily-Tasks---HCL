package com.technova.employeedirectory.service;

import com.technova.employeedirectory.model.DepartmentSummary;
import com.technova.employeedirectory.model.Employee;

import java.util.*;

/**
 * Service managing employee records in memory.
 *
 * Demonstrates:
 * 1. {@link HashMap} for O(1) average-case key-based employee lookups.
 * 2. {@link Map#merge(Object, Object, java.util.function.BiFunction)} for department frequency counting.
 * 3. {@link TreeMap} for naturally sorted department aggregation and reporting.
 * 4. Natural ordering via {@link Comparable} (ascending Employee ID).
 * 5. Custom multi-level ordering via chained {@link Comparator} (Salary descending, ID ascending).
 */
public class EmployeeDirectoryService {

    // Primary store: O(1) average lookup time by Employee ID
    private final Map<Integer, Employee> employeeMap;

    public EmployeeDirectoryService() {
        this.employeeMap = new HashMap<>();
    }

    // =========================================================================
    // A. HASHMAP CRUD & DIRECTORY OPERATIONS
    // =========================================================================

    /**
     * Adds an employee to the directory.
     * Rejects duplicate IDs with an IllegalArgumentException.
     *
     * @param employee employee to add
     * @throws IllegalArgumentException if an employee with the same ID already exists
     */
    public void addEmployee(Employee employee) {
        Objects.requireNonNull(employee, "Employee cannot be null");
        int id = employee.getId();
        if (employeeMap.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Employee with ID %d already exists in the directory.", id));
        }
        employeeMap.put(id, employee);
    }

    /**
     * Finds an employee by ID using HashMap's O(1) average lookup.
     *
     * @param id employee ID
     * @return Optional containing the employee if found, or empty Optional
     */
    public Optional<Employee> findEmployeeById(int id) {
        return Optional.ofNullable(employeeMap.get(id));
    }

    /**
     * Updates an existing employee in the directory.
     *
     * @param employee updated employee details
     * @throws NoSuchElementException if the employee ID does not exist
     */
    public void updateEmployee(Employee employee) {
        Objects.requireNonNull(employee, "Employee cannot be null");
        int id = employee.getId();
        if (!employeeMap.containsKey(id)) {
            throw new NoSuchElementException(String.format("Cannot update: Employee with ID %d does not exist.", id));
        }
        employeeMap.put(id, employee);
    }

    /**
     * Removes an employee from the directory by ID.
     *
     * @param id employee ID to remove
     * @return Optional containing the removed employee if existed
     */
    public Optional<Employee> removeEmployee(int id) {
        return Optional.ofNullable(employeeMap.remove(id));
    }

    /**
     * Returns an unmodifiable list of all employees in the directory.
     * Prevents external code from modifying internal storage directly.
     */
    public List<Employee> getAllEmployees() {
        return List.copyOf(employeeMap.values());
    }

    public int getEmployeeCount() {
        return employeeMap.size();
    }

    public void clear() {
        employeeMap.clear();
    }

    // =========================================================================
    // B. FREQUENCY COUNTING WITH Map.merge()
    // =========================================================================

    /**
     * Counts how many employees belong to each department using {@link Map#merge}.
     *
     * Department names are normalized (trimmed and converted to UPPERCASE)
     * so that case discrepancies do not fragment counts.
     *
     * How Map.merge(dept, 1, Integer::sum) operates:
     * - If 'dept' is NOT present: sets value to 1 (initialization).
     * - If 'dept' IS present: remapping function (Integer::sum) adds 1 to the existing count.
     *
     * @return map of normalized department names to their employee count
     */
    public Map<String, Integer> getDepartmentFrequencyCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Employee emp : employeeMap.values()) {
            String dept = normalizeDepartment(emp.getDepartment());
            counts.merge(dept, 1, Integer::sum);
        }
        return counts;
    }

    // =========================================================================
    // C. SORTED DEPARTMENT REPORT USING TreeMap
    // =========================================================================

    /**
     * Generates a department-by-department summary report sorted alphabetically
     * by department name using a {@link TreeMap}.
     *
     * Each entry contains:
     * - Department name
     * - Employee count
     * - Roster of member employees
     * - Average department salary calculated with BigDecimal and HALF_UP rounding
     *
     * @return sorted map of department summaries
     */
    public Map<String, DepartmentSummary> getDepartmentReports() {
        // TreeMap ensures alphabetical sorting of department keys: O(log K) per insertion
        Map<String, DepartmentSummary> reportMap = new TreeMap<>();

        for (Employee emp : employeeMap.values()) {
            String dept = normalizeDepartment(emp.getDepartment());
            reportMap.computeIfAbsent(dept, DepartmentSummary::new).addEmployee(emp);
        }

        return Collections.unmodifiableMap(reportMap);
    }

    // =========================================================================
    // D. NATURAL ORDERING (Comparable by Employee ID)
    // =========================================================================

    /**
     * Returns all employees sorted according to their NATURAL ORDERING (ascending Employee ID).
     *
     * Natural ordering is governed directly by {@link Employee#compareTo(Employee)}.
     * This method does not supply an external Comparator.
     */
    public List<Employee> getEmployeesSortedById() {
        List<Employee> list = new ArrayList<>(employeeMap.values());
        Collections.sort(list); // Relies exclusively on Employee.compareTo()
        return Collections.unmodifiableList(list);
    }

    // =========================================================================
    // E. TOP-FIVE SALARY REPORT (Comparator Chaining)
    // =========================================================================

    /**
     * Returns the highest-paid employees up to the specified limit (e.g. 5).
     *
     * Ordering rules:
     * 1. Salary descending (highest salary first).
     * 2. Employee ID ascending (secondary tie-breaker when salaries are equal).
     * 3. Employee Name ascending (tertiary tie-breaker for deterministic output).
     *
     * Chained Comparator Design:
     * Note: We reverse ONLY the salary comparison key using Comparator.reverseOrder(),
     * ensuring the secondary ID comparison remains ascending.
     *
     * @param limit maximum number of top employees to return
     * @return unmodifiable list of top-paid employees
     */
    public List<Employee> getTopPaidEmployees(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        Comparator<Employee> topSalaryComparator = Comparator
                .comparing(Employee::getSalary, Comparator.reverseOrder())
                .thenComparingInt(Employee::getId)
                .thenComparing(Employee::getName);

        List<Employee> sortedList = new ArrayList<>(employeeMap.values());
        sortedList.sort(topSalaryComparator);

        int count = Math.min(limit, sortedList.size());
        return Collections.unmodifiableList(sortedList.subList(0, count));
    }

    // =========================================================================
    // NORMALIZATION UTILITY
    // =========================================================================

    /**
     * Normalizes department names to upper-case trimmed representation to ensure
     * consistent grouping across case variations (e.g., "Engineering" vs "engineering").
     */
    public static String normalizeDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return department.trim().toUpperCase(Locale.ENGLISH);
    }
}
