package com.technova.employeedirectory.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Summary metrics for a specific department.
 * Used by TreeMap reporting to present aggregate employee counts,
 * member rosters, and average salaries with accurate BigDecimal arithmetic.
 */
public class DepartmentSummary {

    private final String departmentName;
    private final List<Employee> employees;
    private BigDecimal totalSalary;

    public DepartmentSummary(String departmentName) {
        this.departmentName = departmentName;
        this.employees = new ArrayList<>();
        this.totalSalary = BigDecimal.ZERO;
    }

    /**
     * Adds an employee to this department summary and accumulates their salary.
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        totalSalary = totalSalary.add(employee.getSalary());
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getEmployeeCount() {
        return employees.size();
    }

    public List<Employee> getEmployees() {
        return Collections.unmodifiableList(employees);
    }

    public BigDecimal getTotalSalary() {
        return totalSalary;
    }

    /**
     * Calculates average salary with 2 decimal places and HALF_UP rounding.
     * Returns BigDecimal.ZERO if the department contains no employees.
     */
    public BigDecimal getAverageSalary() {
        if (employees.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return totalSalary.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%-14s | Count: %2d | Avg Salary: $%,10.2f | Total: $%,11.2f",
                departmentName, getEmployeeCount(), getAverageSalary(), totalSalary);
    }
}
