package com.technova.library;

import com.technova.library.model.Book;
import com.technova.library.model.LoanRecord;
import com.technova.library.model.WaitlistRequest;
import com.technova.library.model.WaitlistRequest.Priority;
import com.technova.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day 07 :: Library Collections & Service Test Suite")
class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }

    @Test
    @DisplayName("1. Catalogue (ArrayList): Indexed access and book search")
    void testCatalogueAddAndGetByIndex() {
        assertEquals(5, service.getCatalogueSize(), "Seeded catalogue should have 5 books");

        Book book0 = service.getBookByIndex(0);
        assertEquals("B101", book0.getBookId());
        assertEquals("Clean Code", book0.getTitle());

        Book book2 = service.getBookByIndex(2);
        assertEquals("B103", book2.getBookId());

        Optional<Book> found = service.findBookById("B102");
        assertTrue(found.isPresent());
        assertEquals("Effective Java", found.get().getTitle());

        assertThrows(IndexOutOfBoundsException.class, () -> service.getBookByIndex(99));
    }

    @Test
    @DisplayName("2. Catalogue (ArrayList): Borrow and return state transitions")
    void testCatalogueBorrowAndReturn() {
        Book book = service.borrowBook("B101");
        assertFalse(book.isAvailable(), "Book should be marked unavailable after borrowing");

        // Attempting to borrow unavailable book should throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.borrowBook("B101"));
        assertTrue(ex.getMessage().contains("already unavailable"));

        // Returning restores availability
        Book returned = service.returnBook("B101");
        assertTrue(returned.isAvailable(), "Book should be available after return");
    }

    @Test
    @DisplayName("3. LinkedHashSet: Duplicate prevention and insertion order preservation")
    void testLinkedHashSetDuplicatePreventionAndOrder() {
        assertTrue(service.registerStudent("STU-1001"));
        assertTrue(service.registerStudent("STU-1002"));
        assertTrue(service.registerStudent("STU-1003"));

        // Duplicate registration attempt
        assertFalse(service.registerStudent("STU-1001"), "Duplicate registration must return false");
        assertEquals(3, service.getRegisteredStudentCount(), "Collection size must not increase on duplicate");

        // Verify insertion order is preserved
        List<String> order = List.copyOf(service.getRegisteredStudents());
        assertEquals(List.of("STU-1001", "STU-1002", "STU-1003"), order);
    }

    @Test
    @DisplayName("4. ArrayDeque: Normal waitlist processes in strict FIFO order")
    void testNormalWaitlistFifoOrder() {
        WaitlistRequest r1 = new WaitlistRequest("REQ-1", "STU-1001", "B105", Priority.STUDENT, 1, LocalDateTime.now());
        WaitlistRequest r2 = new WaitlistRequest("REQ-2", "STU-1002", "B105", Priority.STUDENT, 2, LocalDateTime.now());
        WaitlistRequest r3 = new WaitlistRequest("REQ-3", "STU-1003", "B105", Priority.STUDENT, 3, LocalDateTime.now());

        assertTrue(service.enqueueNormalRequest(r1));
        assertTrue(service.enqueueNormalRequest(r2));
        assertTrue(service.enqueueNormalRequest(r3));

        assertEquals(3, service.getNormalWaitlistSize());

        // Process in FIFO order: r1 first, then r2, then r3
        assertEquals(r1, service.processNextNormalRequest().orElseThrow());
        assertEquals(r2, service.processNextNormalRequest().orElseThrow());
        assertEquals(r3, service.processNextNormalRequest().orElseThrow());
        assertTrue(service.processNextNormalRequest().isEmpty());
    }

    @Test
    @DisplayName("5. ArrayDeque: Rejects duplicate active request for same student and book")
    void testNormalWaitlistRejectsDuplicateActiveRequest() {
        WaitlistRequest r1 = new WaitlistRequest("REQ-1", "STU-1001", "B102", Priority.STUDENT, 1, LocalDateTime.now());
        WaitlistRequest r1Duplicate = new WaitlistRequest("REQ-2", "STU-1001", "B102", Priority.STUDENT, 2, LocalDateTime.now());

        assertTrue(service.enqueueNormalRequest(r1));
        assertFalse(service.enqueueNormalRequest(r1Duplicate), "Duplicate active request must be rejected");
        assertEquals(1, service.getNormalWaitlistSize());
    }

    @Test
    @DisplayName("6. PriorityQueue: Faculty-first ordering with sequence tie-breaking")
    void testPriorityQueueFacultyFirstWithTieBreaking() {
        // Interleaved arrival: Student (seq 1), Faculty (seq 2), Student (seq 3), Faculty (seq 4)
        WaitlistRequest s1 = new WaitlistRequest("REQ-1", "STU-1001", "B104", Priority.STUDENT, 1, LocalDateTime.now());
        WaitlistRequest f1 = new WaitlistRequest("REQ-2", "FAC-5001", "B104", Priority.FACULTY, 2, LocalDateTime.now());
        WaitlistRequest s2 = new WaitlistRequest("REQ-3", "STU-1002", "B104", Priority.STUDENT, 3, LocalDateTime.now());
        WaitlistRequest f2 = new WaitlistRequest("REQ-4", "FAC-5002", "B104", Priority.FACULTY, 4, LocalDateTime.now());

        service.enqueuePriorityRequest(s1);
        service.enqueuePriorityRequest(f1);
        service.enqueuePriorityRequest(s2);
        service.enqueuePriorityRequest(f2);

        // Expected polling order:
        // 1. f1 (Faculty, seq 2)
        // 2. f2 (Faculty, seq 4)
        // 3. s1 (Student, seq 1)
        // 4. s2 (Student, seq 3)
        assertEquals(f1, service.processNextPriorityRequest().orElseThrow());
        assertEquals(f2, service.processNextPriorityRequest().orElseThrow());
        assertEquals(s1, service.processNextPriorityRequest().orElseThrow());
        assertEquals(s2, service.processNextPriorityRequest().orElseThrow());
    }

    @Test
    @DisplayName("7. Iterator.remove(): Safely cleans overdue unreturned records")
    void testOverdueRemovalIteratorSafe() {
        LocalDate refDate = LocalDate.of(2026, 10, 15);

        LoanRecord overdue1 = new LoanRecord("L1", "STU-1", "B1", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1), false);
        LoanRecord returnedOverdue = new LoanRecord("L2", "STU-2", "B2", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1), true);
        LoanRecord futureDue = new LoanRecord("L3", "STU-3", "B3", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 20), false);
        LoanRecord overdue2 = new LoanRecord("L4", "STU-4", "B4", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 25), false);

        service.addLoanRecord(overdue1);
        service.addLoanRecord(returnedOverdue);
        service.addLoanRecord(futureDue);
        service.addLoanRecord(overdue2);

        assertEquals(4, service.getLoanRecordsCount());

        List<LoanRecord> removed = service.removeOverdueLoans(refDate);

        assertEquals(2, removed.size(), "Two overdue unreturned records should be removed");
        assertTrue(removed.contains(overdue1));
        assertTrue(removed.contains(overdue2));

        // Preserved records
        assertEquals(2, service.getLoanRecordsCount());
        assertTrue(service.getLoanRecords().contains(returnedOverdue));
        assertTrue(service.getLoanRecords().contains(futureDue));
    }

    @Test
    @DisplayName("8. Model input validation: Blank parameters rejected")
    void testModelValidations() {
        assertThrows(IllegalArgumentException.class, () -> new Book("  ", "Title", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("B1", "  ", "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book("B1", "Title", "  "));

        assertThrows(IllegalArgumentException.class, () ->
                new LoanRecord("L1", "S1", "B1", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 5)));
    }
}
