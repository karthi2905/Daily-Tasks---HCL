package com.technova.booking.comparator;

import com.technova.booking.model.Booking;

import java.util.Comparator;

/**
 * Sorts bookings by start time only (useful when all bookings are on the same date).
 * Demonstrates: Comparator interface, fine-grained alternate ordering.
 */
public class BookingByStartTimeComparator implements Comparator<Booking> {

    @Override
    public int compare(Booking a, Booking b) {
        return a.getStartTime().compareTo(b.getStartTime());
    }
}
