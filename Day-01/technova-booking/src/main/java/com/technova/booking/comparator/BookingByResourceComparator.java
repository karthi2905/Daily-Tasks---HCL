package com.technova.booking.comparator;

import com.technova.booking.model.Booking;

import java.util.Comparator;

/**
 * Sorts bookings alphabetically by resource name.
 * Demonstrates: Comparator interface, external sorting strategy.
 */
public class BookingByResourceComparator implements Comparator<Booking> {

    @Override
    public int compare(Booking a, Booking b) {
        return a.getResource().getName().compareTo(b.getResource().getName());
    }
}
