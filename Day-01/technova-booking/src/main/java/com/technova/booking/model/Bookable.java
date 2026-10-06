package com.technova.booking.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface representing any bookable resource in the TechNova system.
 * Demonstrates: interfaces, polymorphism, dependency inversion.
 *
 * The service layer depends on this interface rather than concrete types,
 * enabling any resource (MeetingRoom, Equipment) to be booked uniformly.
 */
public interface Bookable {

    /**
     * Checks whether this resource is available for the given time slot.
     *
     * @param date      the date of the desired booking
     * @param startTime start of the desired time slot
     * @param endTime   end of the desired time slot
     * @return true if no conflict exists; false otherwise
     */
    boolean isAvailable(LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Creates and returns a booking for this resource.
     *
     * @param employee  the employee requesting the booking
     * @param date      the booking date
     * @param startTime booking start time
     * @param endTime   booking end time
     * @return a new Booking object
     */
    Booking createBooking(Employee employee, LocalDate date,
                          LocalTime startTime, LocalTime endTime);
}
