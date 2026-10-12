package com.technova.library.app;

import com.technova.library.model.Book;
import com.technova.library.model.LoanRecord;
import com.technova.library.model.WaitlistRequest;
import com.technova.library.model.WaitlistRequest.Priority;
import com.technova.library.service.LibraryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main console demonstration application for Day 7:
 * Demonstrates Java Collections Framework best practices, behavioral nuances,
 * and data structure trade-offs.
 */
public class LibraryApplication {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     TECHNOVA LIBRARY WAITLIST -- JAVA COLLECTIONS BEHAVIOR DEMO                 ");
        System.out.println("================================================================================");

        LibraryService service = new LibraryService();

        demonstrateCatalogueArrayList(service);
        demonstrateDuplicatePreventionLinkedHashSet(service);
        demonstrateNormalFifoWaitlistArrayDeque(service);
        demonstrateFacultyFirstPriorityQueue(service);
        demonstrateSafeOverdueRemovalIterator(service);
        demonstrateCollectionSummary(service);

        System.out.println("\n================================================================================");
        System.out.println("                    ALL SCENARIOS COMPLETED SUCCESSFULLY                        ");
        System.out.println("================================================================================");
    }

    /**
     * Scenario 1: ArrayList for Library Catalogue
     */
    public static void demonstrateCatalogueArrayList(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("1. CATALOGUE DEMO :: ArrayList<Book>");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Catalogue initialized with %d seeded books:%n", service.getCatalogueSize());
        for (int i = 0; i < service.getCatalogueSize(); i++) {
            // Demonstrating O(1) indexed access: get(index)
            System.out.printf("  [%d] %s%n", i, service.getBookByIndex(i));
        }

        // Fast random indexed access demo
        System.out.println("\nDemonstrating Direct O(1) Indexed Access with get(index):");
        Book bookAt2 = service.getBookByIndex(2);
        System.out.printf("  service.getBookByIndex(2) -> %s%n", bookAt2.getTitle());

        // Borrowing and state transition demo
        System.out.println("\nBorrowing and State Transition Demonstration:");
        System.out.println("  Action: Borrowing book 'B101'...");
        Book borrowedBook = service.borrowBook("B101");
        System.out.printf("  [PASS] Status updated: %s%n", borrowedBook);

        System.out.println("  Action: Attempting to borrow 'B101' again while unavailable...");
        try {
            service.borrowBook("B101");
            System.err.println("  [FAIL] Expected IllegalStateException was not thrown!");
        } catch (IllegalStateException ex) {
            System.out.printf("  [PASS] Correctly intercepted: %s%n", ex.getMessage());
        }

        System.out.println("  Action: Returning book 'B101' back to catalogue...");
        Book returnedBook = service.returnBook("B101");
        System.out.printf("  [PASS] Status restored: %s%n", returnedBook);
    }

    /**
     * Scenario 2: LinkedHashSet for Duplicate-Free Registration
     */
    public static void demonstrateDuplicatePreventionLinkedHashSet(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("2. DUPLICATE PREVENTION & INSERTION ORDER :: LinkedHashSet<String>");
        System.out.println("--------------------------------------------------------------------------------");

        String[] registrationSequence = {"STU-1001", "STU-1002", "STU-1003", "STU-1001", "STU-1004", "STU-1002"};

        System.out.println("Registration attempts in order:");
        for (String stuId : registrationSequence) {
            boolean added = service.registerStudent(stuId);
            System.out.printf("  Register '%s' -> %s (Current Set Size: %d)%n",
                    stuId, added ? "ACCEPTED (New)" : "REJECTED (Duplicate)", service.getRegisteredStudentCount());
        }

        System.out.println("\nFinal Registered Students in LinkedHashSet (strictly preserves insertion order):");
        int rank = 1;
        for (String id : service.getRegisteredStudents()) {
            System.out.printf("  %d. %s%n", rank++, id);
        }

        System.out.println("\nWhy LinkedHashSet?");
        System.out.println("  - ArrayList allows duplicates and requires O(n) contains() scan.");
        System.out.println("  - HashSet prevents duplicates in O(1) time but loses original arrival order.");
        System.out.println("  - LinkedHashSet maintains doubly-linked buckets: O(1) uniqueness + order retention.");
    }

    /**
     * Scenario 3: ArrayDeque for Normal FIFO Waitlist
     */
    public static void demonstrateNormalFifoWaitlistArrayDeque(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("3. NORMAL FIFO WAITLIST :: ArrayDeque<WaitlistRequest>");
        System.out.println("--------------------------------------------------------------------------------");

        WaitlistRequest req1 = new WaitlistRequest("REQ-101", "STU-1001", "B102", Priority.STUDENT, 1, LocalDateTime.now());
        WaitlistRequest req2 = new WaitlistRequest("REQ-102", "STU-1002", "B102", Priority.STUDENT, 2, LocalDateTime.now());
        WaitlistRequest req3 = new WaitlistRequest("REQ-103", "STU-1003", "B102", Priority.STUDENT, 3, LocalDateTime.now());

        System.out.println("Enqueueing 3 normal requests into ArrayDeque:");
        service.enqueueNormalRequest(req1);
        service.enqueueNormalRequest(req2);
        service.enqueueNormalRequest(req3);
        System.out.printf("  Enqueued 3 requests. Queue size: %d%n", service.getNormalWaitlistSize());

        // Test duplicate prevention for active request
        System.out.println("\nAttempting duplicate active request for STU-1001 on B102:");
        WaitlistRequest duplicateReq = new WaitlistRequest("REQ-999", "STU-1001", "B102", Priority.STUDENT, 4, LocalDateTime.now());
        boolean enqueued = service.enqueueNormalRequest(duplicateReq);
        System.out.printf("  Enqueue duplicate -> %s (Queue size remains: %d)%n",
                enqueued ? "ACCEPTED" : "REJECTED [Duplicate active request]", service.getNormalWaitlistSize());

        System.out.println("\nProcessing requests from front of queue (FIFO order):");
        int step = 1;
        while (service.getNormalWaitlistSize() > 0) {
            Optional<WaitlistRequest> processed = service.processNextNormalRequest();
            if (processed.isPresent()) {
                System.out.printf("  Processing #%d: %s%n", step, processed.get());
            }
            step++;
        }
        System.out.printf("  All normal requests fulfilled. Queue size after processing: %d%n", service.getNormalWaitlistSize());

        System.out.println("\nWhy ArrayDeque over LinkedList for Queues?");
        System.out.println("  - ArrayDeque is backed by a circular array (no node object allocations).");
        System.out.println("  - Better cache locality, lower memory footprint, and faster head/tail ops.");
    }

    /**
     * Scenario 4: PriorityQueue for Faculty-First Requests with Tie-Breaking
     */
    public static void demonstrateFacultyFirstPriorityQueue(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("4. FACULTY-FIRST PRIORITY QUEUE :: PriorityQueue<WaitlistRequest>");
        System.out.println("--------------------------------------------------------------------------------");

        // Interleaved arrival sequence: Student arrives first, then Faculty, then Student, then Faculty
        WaitlistRequest s1 = new WaitlistRequest("REQ-201", "STU-1001", "B104", Priority.STUDENT, 101, LocalDateTime.now());
        WaitlistRequest f1 = new WaitlistRequest("REQ-202", "FAC-5001", "B104", Priority.FACULTY, 102, LocalDateTime.now());
        WaitlistRequest s2 = new WaitlistRequest("REQ-203", "STU-1002", "B104", Priority.STUDENT, 103, LocalDateTime.now());
        WaitlistRequest f2 = new WaitlistRequest("REQ-204", "FAC-5002", "B104", Priority.FACULTY, 104, LocalDateTime.now());

        System.out.println("Enqueueing interleaved requests into PriorityQueue:");
        System.out.printf("  1. Enqueued: %s%n", s1);
        System.out.printf("  2. Enqueued: %s%n", f1);
        System.out.printf("  3. Enqueued: %s%n", s2);
        System.out.printf("  4. Enqueued: %s%n", f2);

        service.enqueuePriorityRequest(s1);
        service.enqueuePriorityRequest(f1);
        service.enqueuePriorityRequest(s2);
        service.enqueuePriorityRequest(f2);

        System.out.println("\nProcessing requests using repeated poll() (demonstrates true priority order):");
        int step = 1;
        while (service.getPriorityWaitlistSize() > 0) {
            Optional<WaitlistRequest> polled = service.processNextPriorityRequest();
            if (polled.isPresent()) {
                System.out.printf("  Turn #%d -> %s%n", step, polled.get());
            }
            step++;
        }

        System.out.println("\nCrucial PriorityQueue Insight:");
        System.out.println("  - PriorityQueue is a binary min-heap, NOT a sorted list!");
        System.out.println("  - Iterating over PriorityQueue with an enhanced for-loop does NOT guarantee sorted order.");
        System.out.println("  - True priority order is only produced by repeatedly invoking poll().");
    }

    /**
     * Scenario 5: Overdue Items & Safe Iterator.remove()
     */
    public static void demonstrateSafeOverdueRemovalIterator(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("5. SAFE REMOVAL OF OVERDUE ITEMS :: Iterator.remove()");
        System.out.println("--------------------------------------------------------------------------------");

        LocalDate referenceDate = LocalDate.of(2026, 10, 15);
        System.out.printf("Cutoff Reference Date: %s%n%n", referenceDate);

        // Populate sample loans
        service.addLoanRecord(new LoanRecord("LN-101", "STU-1001", "B101", LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 1), false)); // OVERDUE
        service.addLoanRecord(new LoanRecord("LN-102", "STU-1002", "B102", LocalDate.of(2026, 9, 20), LocalDate.of(2026, 10, 5), true));  // Returned (Safe)
        service.addLoanRecord(new LoanRecord("LN-103", "STU-1003", "B103", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 20), false)); // Active, not overdue (Safe)
        service.addLoanRecord(new LoanRecord("LN-104", "STU-1004", "B104", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 25), false)); // OVERDUE
        service.addLoanRecord(new LoanRecord("LN-105", "STU-1005", "B105", LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 12), true));  // Returned (Safe)

        System.out.printf("Initial Loan Records (%d total):%n", service.getLoanRecordsCount());
        for (LoanRecord record : service.getLoanRecords()) {
            System.out.printf("  %s%n", record);
        }

        System.out.println("\nExecuting service.removeOverdueLoans(referenceDate) via Iterator.remove()...");
        List<LoanRecord> removedLoans = service.removeOverdueLoans(referenceDate);

        System.out.printf("\n[REMOVED] %d Overdue Unreturned Records:%n", removedLoans.size());
        for (LoanRecord removed : removedLoans) {
            System.out.printf("  - %s (Due %s was before %s)%n", removed.getLoanId(), removed.getDueDate(), referenceDate);
        }

        System.out.printf("\n[RETAINED] %d Active/Returned Records Still in Library:%n", service.getLoanRecordsCount());
        for (LoanRecord remaining : service.getLoanRecords()) {
            System.out.printf("  + %s%n", remaining);
        }

        System.out.println("\nWhy Iterator.remove() is Mandatory:");
        System.out.println("  - Directly calling collection.remove() inside a 'for (e : list)' loop triggers");
        System.out.println("    java.util.ConcurrentModificationException because modCount differs from expectedModCount.");
        System.out.println("  - Iterator.remove() cleanly synchronizes expectedModCount = modCount, preventing crashes.");
    }

    /**
     * Scenario 6: Final Collection Summary
     */
    public static void demonstrateCollectionSummary(LibraryService service) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("6. FINAL STATE & COLLECTION SIZE SUMMARY");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  1. Books in Catalogue (ArrayList):         %d books%n", service.getCatalogueSize());
        System.out.printf("  2. Registered Students (LinkedHashSet):    %d unique IDs%n", service.getRegisteredStudentCount());
        System.out.printf("  3. Normal Waitlist (ArrayDeque):           %d requests%n", service.getNormalWaitlistSize());
        System.out.printf("  4. Priority Waitlist (PriorityQueue):      %d requests%n", service.getPriorityWaitlistSize());
        System.out.printf("  5. Active/Historical Loans (ArrayList):    %d records%n", service.getLoanRecordsCount());
    }
}
