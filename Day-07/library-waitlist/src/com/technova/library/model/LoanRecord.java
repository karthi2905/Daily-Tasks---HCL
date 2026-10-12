package com.technova.library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a library book loan record.
 * Demonstrates loan tracking, overdue detection, and status management.
 */
public class LoanRecord {

    private final String loanId;
    private final String studentId;
    private final String bookId;
    private final LocalDate loanDate;
    private final LocalDate dueDate;
    private boolean returned;

    public LoanRecord(String loanId, String studentId, String bookId, LocalDate loanDate, LocalDate dueDate) {
        this(loanId, studentId, bookId, loanDate, dueDate, false);
    }

    public LoanRecord(String loanId, String studentId, String bookId, LocalDate loanDate, LocalDate dueDate, boolean returned) {
        if (loanId == null || loanId.trim().isEmpty()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or blank");
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Book ID cannot be null or blank");
        }
        if (loanDate == null) {
            throw new IllegalArgumentException("Loan date cannot be null");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (dueDate.isBefore(loanDate)) {
            throw new IllegalArgumentException("Due date cannot be earlier than loan date");
        }

        this.loanId = loanId.trim();
        this.studentId = studentId.trim();
        this.bookId = bookId.trim();
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getBookId() {
        return bookId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned() {
        this.returned = true;
    }

    /**
     * Determines whether this loan is unreturned and past its due date.
     *
     * @param referenceDate the date against which overdue status is evaluated
     * @return true if unreturned and due date is strictly before the reference date
     */
    public boolean isOverdue(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date cannot be null");
        }
        return !returned && dueDate.isBefore(referenceDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoanRecord that)) return false;
        return Objects.equals(loanId, that.loanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        return String.format("[%s] Student: %-8s | Book: %-6s | Due: %s | Status: %s",
                loanId, studentId, bookId, dueDate, returned ? "RETURNED" : "ACTIVE");
    }
}
