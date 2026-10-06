package com.technova.booking;

import com.technova.booking.exception.BookingConflictException;
import com.technova.booking.exception.InvalidBookingException;
import com.technova.booking.exception.ResourceNotFoundException;
import com.technova.booking.model.Booking;
import com.technova.booking.model.BookingStatus;
import com.technova.booking.model.Employee;
import com.technova.booking.model.MeetingRoom;
import com.technova.booking.model.Resource;
import com.technova.booking.repository.InMemoryRepository;
import com.technova.booking.service.ApprovalService;
import com.technova.booking.service.BookingService;
import com.technova.booking.service.ResourceService;
import com.technova.booking.util.DataInitializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TechNova booking system.
 *
 * Tests cover all the edge cases listed in the assessment:
 *  1.  Valid normal booking
 *  2.  Valid restricted booking (PENDING)
 *  3.  Resource not found
 *  4.  End time before start time
 *  5.  Start time equals end time
 *  6.  Exact same booking (conflict)
 *  7.  Partial overlap
 *  8.  New booking completely contains old booking
 *  9.  Existing booking completely contains new booking
 *  10. Booking before existing (no conflict)
 *  11. Booking after existing (no conflict)
 *  12. Approve pending booking
 *  13. Reject pending booking
 *  14. Duplicate employee ID via Set
 *  15. Sorting works (Comparable + Comparator)
 */
class BookingSystemTest {

    private ResourceService resourceService;
    private BookingService bookingService;
    private ApprovalService approvalService;

    private Employee karthik;
    private Employee arun;

    private static final LocalDate DATE = LocalDate.of(2026, 10, 6);

    @BeforeEach
    void setUp() {
        InMemoryRepository<Resource, String> resourceRepo = new InMemoryRepository<>();
        resourceService = new ResourceService(resourceRepo);
        bookingService = new BookingService(resourceService);
        approvalService = new ApprovalService(bookingService);

        DataInitializer initializer = new DataInitializer();
        initializer.initResources(resourceService);

        karthik = new Employee("E101", "Karthik", "karthik@technova.com", "Engineering");
        arun = new Employee("E102", "Arun", "arun@technova.com", "Product");
    }

    // =========================================================================
    // 1. Valid normal booking
    // =========================================================================

    @Test
    @DisplayName("Valid normal booking creates a CONFIRMED booking")
    void testValidNormalBooking() throws BookingConflictException {
        Booking booking = bookingService.createBooking(
                karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertNotNull(booking);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("Karthik", booking.getEmployee().getName());
        assertEquals("Conference Room A", booking.getResource().getName());
    }

    // =========================================================================
    // 2. Valid restricted booking → PENDING and enqueued
    // =========================================================================

    @Test
    @DisplayName("Restricted resource booking creates PENDING booking and enqueues it")
    void testRestrictedBookingIsPending() throws BookingConflictException {
        Booking booking = bookingService.createBooking(
                karthik, "LAB01", DATE,
                LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(1, approvalService.pendingCount());
    }

    // =========================================================================
    // 3. Resource not found
    // =========================================================================

    @Test
    @DisplayName("Non-existent resource ID throws ResourceNotFoundException")
    void testResourceNotFound() {
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(
                        karthik, "RM999", DATE,
                        LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertTrue(ex.getMessage().contains("RM999"));
    }

    // =========================================================================
    // 4. End time before start time → InvalidBookingException
    // =========================================================================

    @Test
    @DisplayName("End time before start time throws InvalidBookingException")
    void testEndBeforeStart() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(
                        karthik, "RM001", DATE,
                        LocalTime.of(14, 0), LocalTime.of(13, 0)));
    }

    // =========================================================================
    // 5. Start equals end time → InvalidBookingException
    // =========================================================================

    @Test
    @DisplayName("Start time equal to end time throws InvalidBookingException")
    void testStartEqualsEnd() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(
                        karthik, "RM001", DATE,
                        LocalTime.of(10, 0), LocalTime.of(10, 0)));
    }

    // =========================================================================
    // 6. Exact same booking → conflict
    // =========================================================================

    @Test
    @DisplayName("Exact duplicate booking throws BookingConflictException")
    void testExactDuplicateConflict() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(arun, "RM001", DATE,
                        LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    // =========================================================================
    // 7. Partial overlap (right side)
    // =========================================================================

    @Test
    @DisplayName("Partial right-side overlap throws BookingConflictException")
    void testPartialOverlapRight() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(arun, "RM001", DATE,
                        LocalTime.of(10, 30), LocalTime.of(11, 30)));
    }

    // =========================================================================
    // 7b. Partial overlap (left side)
    // =========================================================================

    @Test
    @DisplayName("Partial left-side overlap throws BookingConflictException")
    void testPartialOverlapLeft() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(arun, "RM001", DATE,
                        LocalTime.of(9, 30), LocalTime.of(10, 30)));
    }

    // =========================================================================
    // 8. New booking completely contains existing → conflict
    // =========================================================================

    @Test
    @DisplayName("New booking containing existing booking throws BookingConflictException")
    void testNewContainsExisting() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        // 09:00 - 12:00 completely contains 10:00 - 11:00
        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(arun, "RM001", DATE,
                        LocalTime.of(9, 0), LocalTime.of(12, 0)));
    }

    // =========================================================================
    // 9. Existing booking completely contains new → conflict
    // =========================================================================

    @Test
    @DisplayName("Existing booking containing new request throws BookingConflictException")
    void testExistingContainsNew() throws BookingConflictException {
        // Existing: 09:00 - 12:00
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        // New: 10:00 - 11:00 — inside existing
        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(arun, "RM001", DATE,
                        LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    // =========================================================================
    // 10. Booking before existing → no conflict
    // =========================================================================

    @Test
    @DisplayName("Booking that ends exactly when existing starts has no conflict")
    void testBookingBeforeExisting() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        // Ends at 10:00 exactly — adjacent, not overlapping
        Booking second = bookingService.createBooking(arun, "RM001", DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertNotNull(second);
        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
    }

    // =========================================================================
    // 11. Booking after existing → no conflict
    // =========================================================================

    @Test
    @DisplayName("Booking that starts exactly when existing ends has no conflict")
    void testBookingAfterExisting() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        // Starts at 11:00 exactly — adjacent, not overlapping
        Booking second = bookingService.createBooking(arun, "RM001", DATE,
                LocalTime.of(11, 0), LocalTime.of(12, 0));

        assertNotNull(second);
        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
    }

    // =========================================================================
    // 12. Approve pending booking
    // =========================================================================

    @Test
    @DisplayName("Approving a pending booking changes status to CONFIRMED")
    void testApproveBooking() throws BookingConflictException {
        Booking pending = bookingService.createBooking(
                karthik, "LAB01", DATE,
                LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertEquals(BookingStatus.PENDING, pending.getStatus());

        Booking approved = approvalService.approveNext();
        assertNotNull(approved);
        assertEquals(BookingStatus.CONFIRMED, approved.getStatus());
        assertEquals(0, approvalService.pendingCount());
    }

    // =========================================================================
    // 13. Reject pending booking
    // =========================================================================

    @Test
    @DisplayName("Rejecting a pending booking changes status to REJECTED")
    void testRejectBooking() throws BookingConflictException {
        Booking pending = bookingService.createBooking(
                karthik, "LAB01", DATE,
                LocalTime.of(14, 0), LocalTime.of(16, 0));

        Booking rejected = approvalService.rejectNext();
        assertNotNull(rejected);
        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    // =========================================================================
    // 14. Empty approval queue
    // =========================================================================

    @Test
    @DisplayName("Polling an empty approval queue returns null")
    void testEmptyApprovalQueue() {
        assertNull(approvalService.approveNext());
        assertNull(approvalService.rejectNext());
    }

    // =========================================================================
    // 15. Duplicate employee ID → Set rejects it
    // =========================================================================

    @Test
    @DisplayName("Set<String> rejects duplicate employee IDs")
    void testDuplicateEmployeeId() {
        DataInitializer initializer = new DataInitializer();
        InMemoryRepository<
                com.technova.booking.model.User, String> userRepo = new InMemoryRepository<>();
        initializer.initUsers(userRepo);

        // E101 is already in the Set — adding again must return false
        java.util.Set<String> ids = initializer.getRegisteredEmployeeIds();
        boolean addedAgain = ids.add("E101");
        assertFalse(addedAgain, "Set must reject duplicate employee ID E101");
    }

    // =========================================================================
    // 16. Sorting — Comparable (natural order) and Comparator
    // =========================================================================

    @Test
    @DisplayName("Bookings sort correctly by date and start time (Comparable)")
    void testSortingByDateAndTime() throws BookingConflictException {
        // Create bookings out of order
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        bookingService.createBooking(arun, "RM002", DATE,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        bookingService.createBooking(karthik, "RM003",
                DATE.minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        List<Booking> bookings = bookingService.getAllBookings();
        Collections.sort(bookings); // uses Comparable<Booking>

        // First booking should be the one on DATE-1 at 10:00
        assertEquals(DATE.minusDays(1), bookings.get(0).getDate());
        // Second should be DATE at 09:00
        assertEquals(LocalTime.of(9, 0), bookings.get(1).getStartTime());
        // Third should be DATE at 14:00
        assertEquals(LocalTime.of(14, 0), bookings.get(2).getStartTime());
    }

    // =========================================================================
    // 17. Multiple bookings for the same resource on different dates — no conflict
    // =========================================================================

    @Test
    @DisplayName("Same time slot on different dates does not conflict")
    void testSameSlotsOnDifferentDatesNoConflict() throws BookingConflictException {
        bookingService.createBooking(karthik, "RM001", DATE,
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        Booking second = bookingService.createBooking(arun, "RM001", DATE.plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertNotNull(second);
        assertEquals(BookingStatus.CONFIRMED, second.getStatus());
    }
}
