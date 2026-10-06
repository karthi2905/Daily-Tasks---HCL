package com.technova.booking.model;

/**
 * Abstract base class for all users in the TechNova system.
 * Demonstrates: abstract classes, encapsulation, access modifiers,
 * constructors, this keyword, private fields.
 */
public abstract class User {

    private final String id;
    private String name;
    private String email;

    /**
     * Constructs a User with the given id, name, and email.
     * Uses 'this' to disambiguate fields from parameters.
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * Abstract method — every concrete user type must describe its role.
     * Demonstrates: abstract methods and polymorphism.
     */
    public abstract void displayRole();

    // ---- Getters ----

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // ---- Setters (controlled mutation) ----

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getName() + " <" + getEmail() + ">";
    }
}
