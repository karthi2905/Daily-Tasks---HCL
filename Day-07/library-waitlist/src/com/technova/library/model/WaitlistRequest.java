package com.technova.library.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a library book waitlist request.
 * Supports priority ranking (FACULTY before STUDENT) and deterministic tie-breaking.
 */
public class WaitlistRequest implements Comparable<WaitlistRequest> {

    private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong(1000);

    /**
     * Priority tier for library members.
     * Declared in natural priority order: FACULTY (higher priority), STUDENT (normal priority).
     */
    public enum Priority {
        FACULTY("Faculty (High Priority)"),
        STUDENT("Student (Normal Priority)");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final String requestId;
    private final String studentId;
    private final String bookId;
    private final Priority priority;
    private final long sequenceNumber;
    private final LocalDateTime requestTime;

    /**
     * Primary constructor assigning an auto-incrementing sequence number and current timestamp.
     */
    public WaitlistRequest(String studentId, String bookId, Priority priority) {
        this("REQ-" + SEQUENCE_GENERATOR.incrementAndGet(), studentId, bookId, priority,
                SEQUENCE_GENERATOR.get(), LocalDateTime.now());
    }

    /**
     * Detailed constructor allowing deterministic sequence numbers for testing.
     */
    public WaitlistRequest(String requestId, String studentId, String bookId, Priority priority,
                           long sequenceNumber, LocalDateTime requestTime) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or blank");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or blank");
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        this.requestId = requestId.trim();
        this.studentId = studentId.trim();
        this.bookId = bookId.trim();
        this.priority = priority;
        this.sequenceNumber = sequenceNumber;
        this.requestTime = (requestTime != null) ? requestTime : LocalDateTime.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getBookId() {
        return bookId;
    }

    public Priority getPriority() {
        return priority;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    /**
     * Explicit comparator for priority queues:
     * 1. Priority order: FACULTY before STUDENT (natural enum ordinal comparison).
     * 2. Sequence order: Lower sequenceNumber (earlier arrival) first to break ties consistently.
     */
    public static final Comparator<WaitlistRequest> PRIORITY_COMPARATOR =
            Comparator.comparing(WaitlistRequest::getPriority)
                    .thenComparingLong(WaitlistRequest::getSequenceNumber);

    @Override
    public int compareTo(WaitlistRequest other) {
        return PRIORITY_COMPARATOR.compare(this, other);
    }

    /**
     * Equality is defined by studentId and bookId to prevent duplicate active waitlist
     * entries for the same student and book combination.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitlistRequest that)) return false;
        return Objects.equals(studentId, that.studentId) &&
               Objects.equals(bookId, that.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, bookId);
    }

    @Override
    public String toString() {
        return String.format("[%s | Seq: %d] %s (%s) for Book %s",
                requestId, sequenceNumber, studentId, priority, bookId);
    }
}
