package com.technova.booking.service;

import com.technova.booking.exception.BookingConflictException;
import com.technova.booking.exception.InvalidBookingException;
import com.technova.booking.exception.ResourceNotFoundException;
import com.technova.booking.model.Bookable;
import com.technova.booking.model.Booking;
import com.technova.booking.model.BookingStatus;
import com.technova.booking.model.Employee;
import com.technova.booking.model.Resource;
import com.technova.booking.repository.InMemoryRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Core booking service.
 *
 * Demonstrates:
 *  - List<Booking> for storing all booking records
 *  - Queue<Booking> (ArrayDeque) as FIFO approval queue for restricted resources
 *  - Checked exception (BookingConflictException) declared with 'throws'
 *  - Unchecked exceptions (ResourceNotFoundException, InvalidBookingException)
 *  - Method overloading: two createBooking variants
 *  - 'throw' keyword
 *  - try-catch-finally
 *  - Service layer pattern
 */
public class BookingService {

    /**
     * List<Booking> — stores all bookings (confirmed, pending, rejected, cancelled).
     * ArrayList chosen for O(1) indexed access and easy iteration.
     * Memory note: in a long-running system, old cancelled bookings should be
     * periodically removed to prevent unbounded heap growth.
     */
    private final List<Booking> allBookings = new ArrayList<>();

    /**
     * Queue<Booking> — FIFO queue for bookings awaiting approval.
     * ArrayDeque is the preferred Queue implementation when no blocking
     * behaviour is required (Deque without synchronisation overhead).
     * Demonstrates FIFO: offer() at tail, poll() from head.
     */
    private final Queue<Booking> approvalQueue = new ArrayDeque<>();

    private final ResourceService resourceService;

    public BookingService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // =========================================================================
    // METHOD OVERLOADING — two ways to create a booking
    // =========================================================================

    /**
     * Overload 1: create a booking using a resolved Resource object.
     * Useful when the caller already has the Resource reference.
     *
     * @throws InvalidBookingException  if the time slot is invalid
     * @throws BookingConflictException if the resource is already booked (checked)
     */
    public Booking createBooking(Employee employee, Resource resource,
                                  LocalDate date, LocalTime startTime, LocalTime endTime)
            throws BookingConflictException {

        validateTimeSlot(startTime, endTime);     // throws InvalidBookingException
        checkConflict(resource, date, startTime, endTime); // throws BookingConflictException

        // Delegate booking creation to the Bookable interface
        Bookable bookable = resource;
        Booking booking = bookable.createBooking(employee, date, startTime, endTime);

        allBookings.add(booking);

        // If restricted, enqueue for approval
        if (resource.isRestricted()) {
            approvalQueue.offer(booking);         // FIFO: add at tail
        }

        return booking;
    }

    /**
     * Overload 2: create a booking using a resourceId String.
     * Resolves the resource first, then delegates to overload 1.
     * Demonstrates method overloading with meaningful purpose.
     *
     * @throws ResourceNotFoundException if the resource ID does not exist
     * @throws InvalidBookingException   if the time slot is invalid
     * @throws BookingConflictException  if the resource is already booked (checked)
     */
    public Booking createBooking(Employee employee, String resourceId,
                                  LocalDate date, LocalTime startTime, LocalTime endTime)
            throws BookingConflictException {

        // Resolve resource — throws ResourceNotFoundException (unchecked) if absent
        Resource resource = resourceService.findResource(resourceId);
        return createBooking(employee, resource, date, startTime, endTime);
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Validates that the time slot is logically valid.
     *
     * @throws InvalidBookingException (unchecked) for invalid slots
     */
    private void validateTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidBookingException("Start time and end time must not be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidBookingException(
                    "Invalid booking time. End time must be after start time. "
                            + "Provided: " + startTime + " - " + endTime);
        }
    }

    /**
     * Checks for overlapping bookings on the resource.
     *
     * @throws BookingConflictException (checked) if an overlap is detected
     */
    private void checkConflict(Resource resource, LocalDate date,
                                 LocalTime startTime, LocalTime endTime)
            throws BookingConflictException {

        System.out.println("  Checking booking conflicts...");

        boolean available = resource.isAvailable(date, startTime, endTime);
        if (!available) {
            throw new BookingConflictException(
                    "Resource '" + resource.getName() + "' is already booked "
                            + "between " + startTime + " and " + endTime + " on " + date + ".");
        }
    }

    // =========================================================================
    // QUERY METHODS
    // =========================================================================

    /**
     * Returns all bookings.
     * Memory note: this list grows with every booking. In a production system
     * an archival/eviction policy would be needed.
     */
    public List<Booking> getAllBookings() {
        return new ArrayList<>(allBookings);    // defensive copy
    }

    /**
     * Returns bookings belonging to a specific employee.
     */
    public List<Booking> getBookingsForEmployee(String employeeId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : allBookings) {
            if (b.getEmployee().getId().equals(employeeId)) {
                result.add(b);
            }
        }
        return result;
    }

    /**
     * Returns the pending approval queue (read-only view).
     */
    public Queue<Booking> getApprovalQueue() {
        return approvalQueue;
    }

    /**
     * Cancels a booking by its ID.
     * Demonstrates: iteration over a List, status mutation.
     *
     * @param bookingId the booking to cancel
     * @return true if found and cancelled
     */
    public boolean cancelBooking(String bookingId) {
        for (Booking b : allBookings) {
            if (b.getBookingId().equals(bookingId)) {
                b.setStatus(BookingStatus.CANCELLED);
                return true;
            }
        }
        return false;
    }
}
