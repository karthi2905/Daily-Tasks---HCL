package com.technova.booking.model;

/**
 * Represents a regular employee who can create bookings.
 * Demonstrates: inheritance, super(), method overriding.
 */
public class Employee extends User {

    private String department;

    /**
     * Constructs an Employee.
     * Uses super() to delegate to the parent constructor.
     */
    public Employee(String id, String name, String email, String department) {
        super(id, name, email);
        this.department = department;
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Employee | Department: " + department);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return super.toString() + " | Dept: " + department;
    }
}
