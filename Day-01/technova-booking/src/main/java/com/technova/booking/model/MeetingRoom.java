package com.technova.booking.model;

/**
 * Represents a meeting room at TechNova.
 * Demonstrates: inheritance, super(), method overriding, encapsulation.
 */
public class MeetingRoom extends Resource {

    private int capacity;
    private String location;

    /**
     * Constructs a MeetingRoom.
     * Uses super() to pass common fields to the parent Resource constructor.
     */
    public MeetingRoom(String resourceId, String name, boolean restricted,
                       int capacity, String location) {
        super(resourceId, name, restricted);
        this.capacity = capacity;
        this.location = location;
    }

    /**
     * Overrides the abstract displayDetails() from Resource.
     * Demonstrates polymorphism — calling displayDetails() on a Resource
     * reference pointing to a MeetingRoom dispatches here at runtime.
     */
    @Override
    public void displayDetails() {
        System.out.printf("  %-6s | %-25s | Room      | Cap: %-3d | %-15s | %s%n",
                getResourceId(), getName(), capacity, location,
                isRestricted() ? "RESTRICTED" : "Available");
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return super.toString() + " | Capacity: " + capacity + " | " + location;
    }
}
