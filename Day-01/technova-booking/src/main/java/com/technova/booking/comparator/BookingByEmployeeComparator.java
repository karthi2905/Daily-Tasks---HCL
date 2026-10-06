package com.technova.booking.comparator;

import com.technova.booking.model.Booking;

import java.util.Comparator;

/**
 * Sorts bookings alphabetically by employee name.
 * Demonstrates: Comparator interface, alternate sorting strategies.
 */
public class BookingByEmployeeComparator implements Comparator<Booking> {

    @Override
    public int compare(Booking a, Booking b) {
        return a.getEmployee().getName().compareTo(b.getEmployee().getName());
    }
}
