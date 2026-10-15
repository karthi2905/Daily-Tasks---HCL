package com.technova.employeedirectory.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an employee in the directory.
 *
 * Implements {@link Comparable} for natural ordering by employee ID (ascending).
 * Overrides {@link #equals(Object)} and {@link #hashCode()} consistently using
 * the employee ID as the sole unique business identity.
 */
public class Employee implements Comparable<Employee> {

    private final int id;
    private final String name;
    private final String department;
    private final BigDecimal salary;

    /**
     * Constructs a validated Employee instance.
     *
     * @param id         unique employee identifier (must be positive > 0)
     * @param name       full name of the employee (non-null, non-blank)
     * @param department department name (non-null, non-blank)
     * @param salary     monthly/annual salary (non-null, non-negative >= 0)
     * @throws IllegalArgumentException if any validation rule is violated
     */
    public Employee(int id, String name, String department, BigDecimal salary) {
        if (id <= 0) {
            throw new IllegalArgumentException(String.format("Employee ID must be strictly positive (> 0), got: %d", id));
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or blank");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee department cannot be null or blank");
        }
        if (salary == null) {
            throw new IllegalArgumentException("Employee salary cannot be null");
        }
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("Employee salary cannot be negative, got: %s", salary));
        }

        this.id = id;
        this.name = name.trim();
        this.department = department.trim();
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    /**
     * Natural ordering for Employee: Ascending order by Employee ID.
     * Required by {@link Comparable}.
     */
    @Override
    public int compareTo(Employee other) {
        Objects.requireNonNull(other, "Cannot compare to null Employee");
        return Integer.compare(this.id, other.id);
    }

    /**
     * Equality is defined strictly by the unique Employee ID.
     * Note: Mutable attributes (such as department or salary) are intentionally
     * excluded to keep the identity stable across hash-based collections.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        return this.id == employee.id;
    }

    /**
     * Hash code is computed strictly from the unique Employee ID,
     * maintaining the contract that equal objects MUST produce identical hash codes.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("[ID: %04d] %-18s | Dept: %-14s | Salary: $%,10.2f",
                id, name, department, salary);
    }
}
