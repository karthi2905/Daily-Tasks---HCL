package com.technova.booking.model;

/**
 * Represents a piece of equipment (e.g., projector) that can be booked.
 * Demonstrates: inheritance, super(), method overriding, encapsulation.
 */
public class Equipment extends Resource {

    private String equipmentType;

    /**
     * Constructs an Equipment resource.
     * Uses super() to delegate shared fields to the Resource constructor.
     */
    public Equipment(String resourceId, String name, boolean restricted,
                     String equipmentType) {
        super(resourceId, name, restricted);
        this.equipmentType = equipmentType;
    }

    /**
     * Overrides the abstract displayDetails() from Resource.
     */
    @Override
    public void displayDetails() {
        System.out.printf("  %-6s | %-25s | Equipment | Type: %-10s | %s%n",
                getResourceId(), getName(), equipmentType,
                isRestricted() ? "RESTRICTED" : "Available");
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    @Override
    public String toString() {
        return super.toString() + " | Type: " + equipmentType;
    }
}
