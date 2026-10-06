package com.technova.booking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a booking in the TechNova system.
 *
 * Demonstrates:
 *  - Encapsulation (all fields private, accessed via getters/setters)
 *  - Constructors and 'this' keyword
 *  - Comparable<Booking> for natural sort order (by date, then startTime)
 *  - LocalDate, LocalTime, LocalDateTime for proper date/time handling
 */
public class Booking implements Comparable<Booking> {

    /** Atomic counter for unique booking ID generation — thread-safe in this demo. */
    private static final AtomicInteger counter = new AtomicInteger(1);

    private final String bookingId;
    private final Employee employee;
    private final Resource resource;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private BookingStatus status;
    private final LocalDateTime createdAt;

    /**
     * Primary constructor.
     * Uses 'this' to assign all fields.
     * Generates a unique booking ID in the format B001, B002, etc.
     */
    public Booking(Employee employee, Resource resource,
                   LocalDate date, LocalTime startTime, LocalTime endTime,
                   BookingStatus status) {
        this.bookingId = String.format("B%03d", counter.getAndIncrement());
        this.employee = employee;
        this.resource = resource;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Comparable: natural sort order = date ascending, then startTime ascending ----

    /**
     * Compares bookings first by date, then by start time.
     * Demonstrates: Comparable interface and natural ordering.
     */
    @Override
    public int compareTo(Booking other) {
        int dateCompare = this.date.compareTo(other.date);
        if (dateCompare != 0) {
            return dateCompare;
        }
        return this.startTime.compareTo(other.startTime);
    }

    // ---- Getters ----

    public String getBookingId() {
        return bookingId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---- Setter: only status is mutable after creation ----

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    // ---- Utility ----

    /**
     * Prints a formatted summary of this booking to the console.
     */
    public void printSummary() {
        System.out.println("  Booking ID : " + bookingId);
        System.out.println("  Employee   : " + employee.getName());
        System.out.println("  Resource   : " + resource.getName());
        System.out.println("  Date       : " + date);
        System.out.println("  Time       : " + startTime + " - " + endTime);
        System.out.println("  Status     : " + status);
        System.out.println("  Created At : " + createdAt);
    }

    @Override
    public String toString() {
        return bookingId + " | " + employee.getName() + " | "
                + resource.getName() + " | " + date
                + " | " + startTime + "-" + endTime
                + " | " + status;
    }
}
