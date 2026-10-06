package com.technova.booking.model;

/**
 * Represents an approver who can approve or reject restricted resource bookings.
 * Demonstrates: inheritance, super(), method overriding.
 */
public class Approver extends User {

    private String approverLevel;

    /**
     * Constructs an Approver using super() to call the User constructor.
     */
    public Approver(String id, String name, String email, String approverLevel) {
        super(id, name, email);
        this.approverLevel = approverLevel;
    }

    @Override
    public void displayRole() {
        System.out.println("Role: Approver | Level: " + approverLevel);
    }

    public String getApproverLevel() {
        return approverLevel;
    }

    public void setApproverLevel(String approverLevel) {
        this.approverLevel = approverLevel;
    }

    @Override
    public String toString() {
        return super.toString() + " | Level: " + approverLevel;
    }
}
