package com.technova.booking.model;

/**
 * Represents a system administrator.
 * Demonstrates: inheritance, super(), method overriding.
 */
public class Admin extends User {

    private String adminCode;

    /**
     * Constructs an Admin using super() to call the User constructor.
     */
    public Admin(String id, String name, String email, String adminCode) {
        super(id, name, email);
        this.adminCode = adminCode;
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Admin | Code: " + adminCode);
    }

    public String getAdminCode() {
        return adminCode;
    }

    @Override
    public String toString() {
        return super.toString() + " | Admin";
    }
}
