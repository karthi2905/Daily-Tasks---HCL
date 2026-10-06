package com.technova.booking.service;

import com.technova.booking.model.Booking;
import com.technova.booking.model.BookingStatus;

import java.util.Queue;

/**
 * Service responsible for the approval workflow.
 *
 * Demonstrates:
 *  - Queue<Booking> FIFO behaviour via poll() (head retrieval + removal)
 *  - Status mutation via setStatus()
 *  - ApprovalService as a focused single-responsibility class
 */
public class ApprovalService {

    private final BookingService bookingService;

    public ApprovalService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Returns the current pending queue without removing entries.
     */
    public Queue<Booking> getPendingQueue() {
        return bookingService.getApprovalQueue();
    }

    /**
     * Returns the number of pending requests.
     */
    public int pendingCount() {
        return bookingService.getApprovalQueue().size();
    }

    /**
     * Peeks at the next booking without removing it from the queue.
     * Returns null if the queue is empty.
     */
    public Booking peekNext() {
        return bookingService.getApprovalQueue().peek();
    }

    /**
     * Retrieves and removes the next booking from the head of the FIFO queue,
     * then approves it by setting status to CONFIRMED.
     *
     * @return the approved booking, or null if queue was empty
     */
    public Booking approveNext() {
        Booking booking = bookingService.getApprovalQueue().poll(); // FIFO: removes head
        if (booking == null) {
            return null;
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return booking;
    }

    /**
     * Retrieves and removes the next booking from the head of the FIFO queue,
     * then rejects it by setting status to REJECTED.
     *
     * @return the rejected booking, or null if queue was empty
     */
    public Booking rejectNext() {
        Booking booking = bookingService.getApprovalQueue().poll();
        if (booking == null) {
            return null;
        }
        booking.setStatus(BookingStatus.REJECTED);
        return booking;
    }
}
