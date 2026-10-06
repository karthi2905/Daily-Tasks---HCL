package com.technova.booking.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all bookable resources in TechNova.
 * Demonstrates: abstract class, encapsulation, access modifiers,
 * this keyword, protected access for subclass use.
 *
 * Implements Bookable so any Resource subclass can participate in booking.
 */
public abstract class Resource implements Bookable {

    private final String resourceId;
    private String name;
    private final boolean restricted;

    /**
     * In-memory list of bookings held by this resource instance.
     * Protected so subclasses can read it without breaking encapsulation externally.
     * Using ArrayList (List) as the concrete type here.
     */
    protected final List<Booking> bookings = new ArrayList<>();

    /**
     * Constructs a Resource.
     * Uses 'this' to assign fields.
     */
    public Resource(String resourceId, String name, boolean restricted) {
        this.resourceId = resourceId;
        this.name = name;
        this.restricted = restricted;
    }

    /**
     * Abstract method: each subclass must describe its specific details.
     * Demonstrates polymorphism — calling displayDetails() on a Resource
     * reference dispatches to the overridden method in the subclass at runtime.
     */
    public abstract void displayDetails();

    // ---- Bookable interface implementation ----

    /**
     * Checks whether this resource has any booking that overlaps with
     * the requested time slot on the given date.
     *
     * Overlap logic (correct interval arithmetic):
     *   newStart < existingEnd AND newEnd > existingStart
     *
     * This correctly handles:
     *   - Partial overlap from the left
     *   - Partial overlap from the right
     *   - New booking completely containing an existing booking
     *   - Existing booking completely containing the new booking
     *
     * (AI Review: original Copilot-generated version only checked
     *  whether newStart fell inside an existing slot, missing the
     *  containment case. See docs/AI_REVIEW.md for details.)
     */
    @Override
    public boolean isAvailable(LocalDate date, LocalTime startTime, LocalTime endTime) {
        for (Booking existing : bookings) {
            if (existing.getDate().equals(date)
                    && existing.getStatus() != BookingStatus.CANCELLED
                    && existing.getStatus() != BookingStatus.REJECTED) {
                boolean overlaps = startTime.isBefore(existing.getEndTime())
                        && endTime.isAfter(existing.getStartTime());
                if (overlaps) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates a Booking for this resource.
     * Status is CONFIRMED for normal resources, PENDING for restricted ones.
     */
    @Override
    public Booking createBooking(Employee employee, LocalDate date,
                                 LocalTime startTime, LocalTime endTime) {
        BookingStatus status = restricted ? BookingStatus.PENDING : BookingStatus.CONFIRMED;
        Booking booking = new Booking(employee, this, date, startTime, endTime, status);
        bookings.add(booking);
        return booking;
    }

    /**
     * Adds an existing Booking object directly to this resource's list.
     * Used internally when a booking is created via the service layer.
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    // ---- Getters ----

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[" + resourceId + "] " + name + (restricted ? " (RESTRICTED)" : "");
    }
}
