package com.technova.library.service;

import com.technova.library.model.Book;
import com.technova.library.model.LoanRecord;
import com.technova.library.model.WaitlistRequest;

import java.time.LocalDate;
import java.util.*;

/**
 * Core business service managing library collections:
 * 1. {@link ArrayList} for the main book catalogue and indexed access.
 * 2. {@link LinkedHashSet} for duplicate-free student registration preserving insertion order.
 * 3. {@link ArrayDeque} for normal FIFO waitlist queue processing.
 * 4. {@link PriorityQueue} for faculty-first priority request processing with tie-breaking.
 * 5. {@link Iterator#remove()} for safe modification during overdue loan cleanups.
 */
public class LibraryService {

    // 1. ArrayList: Fast indexed access, sequential ordering for library catalogue
    private final List<Book> catalogue;

    // 2. LinkedHashSet: Duplicate-free registration while preserving exact insertion order
    private final Set<String> registeredStudentIds;

    // 3. ArrayDeque: High-performance circular array FIFO queue for standard book requests
    private final Deque<WaitlistRequest> normalWaitlist;

    // 4. PriorityQueue: Min-heap prioritizing faculty over students with sequence tie-breakers
    private final PriorityQueue<WaitlistRequest> priorityWaitlist;

    // 5. Loan records collection: Managed using Iterator.remove() for overdue item cleanup
    private final List<LoanRecord> loanRecords;

    public LibraryService() {
        this.catalogue = new ArrayList<>();
        this.registeredStudentIds = new LinkedHashSet<>();
        this.normalWaitlist = new ArrayDeque<>();
        this.priorityWaitlist = new PriorityQueue<>(WaitlistRequest.PRIORITY_COMPARATOR);
        this.loanRecords = new ArrayList<>();

        seedDefaultCatalogue();
    }

    // =========================================================================
    // 1. CATALOGUE OPERATIONS (ArrayList)
    // =========================================================================

    /**
     * Seeds the catalogue with 5 representative computer science books.
     */
    private void seedDefaultCatalogue() {
        addBook(new Book("B101", "Clean Code", "Robert C. Martin"));
        addBook(new Book("B102", "Effective Java", "Joshua Bloch"));
        addBook(new Book("B103", "Design Patterns", "Erich Gamma et al."));
        addBook(new Book("B104", "Java Concurrency in Practice", "Brian Goetz"));
        addBook(new Book("B105", "Introduction to Algorithms", "Thomas H. Cormen"));
    }

    /**
     * Adds a new book to the catalogue ArrayList.
     */
    public void addBook(Book book) {
        Objects.requireNonNull(book, "Book cannot be null");
        if (findBookById(book.getBookId()).isPresent()) {
            throw new IllegalArgumentException(String.format("Book with ID '%s' already exists in catalogue.", book.getBookId()));
        }
        catalogue.add(book);
    }

    /**
     * Demonstrates O(1) indexed access using ArrayList.get(index).
     */
    public Book getBookByIndex(int index) {
        if (index < 0 || index >= catalogue.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds for catalogue size %d", index, catalogue.size()));
        }
        return catalogue.get(index);
    }

    /**
     * Searches for a book by its unique ID.
     */
    public Optional<Book> findBookById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return Optional.empty();
        }
        return catalogue.stream()
                .filter(b -> b.getBookId().equalsIgnoreCase(bookId.trim()))
                .findFirst();
    }

    /**
     * Borrows a book if it is currently available in the catalogue.
     *
     * @param bookId identifier of the book to borrow
     * @return the borrowed Book
     * @throws NoSuchElementException if book does not exist
     * @throws IllegalStateException  if book is already checked out
     */
    public Book borrowBook(String bookId) {
        Book book = findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found in catalogue: " + bookId));
        book.borrowBook(); // Throws IllegalStateException if already unavailable
        return book;
    }

    /**
     * Returns a borrowed book back to the catalogue.
     */
    public Book returnBook(String bookId) {
        Book book = findBookById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found in catalogue: " + bookId));
        book.returnBook();
        return book;
    }

    public List<Book> getCatalogue() {
        return Collections.unmodifiableList(catalogue);
    }

    public int getCatalogueSize() {
        return catalogue.size();
    }

    // =========================================================================
    // 2. DUPLICATE PREVENTION & ORDER PRESERVATION (LinkedHashSet)
    // =========================================================================

    /**
     * Registers a student ID into the LinkedHashSet.
     * Guarantees:
     * - No duplicates (returns false on second attempt).
     * - Insertion order is strictly preserved.
     *
     * @param studentId unique student identifier (e.g. "STU-2001")
     * @return true if student was registered; false if already present
     */
    public boolean registerStudent(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or blank");
        }
        return registeredStudentIds.add(studentId.trim());
    }

    public Set<String> getRegisteredStudents() {
        return Collections.unmodifiableSet(registeredStudentIds);
    }

    public int getRegisteredStudentCount() {
        return registeredStudentIds.size();
    }

    // =========================================================================
    // 3. FIFO WAITLIST (ArrayDeque)
    // =========================================================================

    /**
     * Enqueues a normal waitlist request at the end of the ArrayDeque.
     * Rejects nulls and duplicate active requests for the same student and book.
     *
     * @param request the waitlist request to enqueue
     * @return true if enqueued; false if duplicate active request exists
     */
    public boolean enqueueNormalRequest(WaitlistRequest request) {
        Objects.requireNonNull(request, "Waitlist request cannot be null");
        if (normalWaitlist.contains(request)) {
            return false; // Prevent duplicate active request for the same student and book
        }
        return normalWaitlist.offerLast(request); // Append to tail in FIFO fashion
    }

    /**
     * Processes and removes the earliest request from the front of the FIFO queue.
     */
    public Optional<WaitlistRequest> processNextNormalRequest() {
        return Optional.ofNullable(normalWaitlist.pollFirst());
    }

    /**
     * Cancels an existing waitlist request.
     */
    public boolean cancelNormalRequest(String studentId, String bookId) {
        return normalWaitlist.removeIf(req ->
                req.getStudentId().equalsIgnoreCase(studentId) && req.getBookId().equalsIgnoreCase(bookId));
    }

    public List<WaitlistRequest> getNormalWaitlistInProcessingOrder() {
        return new ArrayList<>(normalWaitlist);
    }

    public int getNormalWaitlistSize() {
        return normalWaitlist.size();
    }

    // =========================================================================
    // 4. FACULTY-FIRST PRIORITY QUEUE (PriorityQueue)
    // =========================================================================

    /**
     * Enqueues a request into the PriorityQueue.
     * Uses min-heap ordering based on Priority (FACULTY before STUDENT)
     * and sequenceNumber (earliest request first to break ties).
     */
    public boolean enqueuePriorityRequest(WaitlistRequest request) {
        Objects.requireNonNull(request, "Priority request cannot be null");
        if (priorityWaitlist.contains(request)) {
            return false; // Prevent duplicate active request
        }
        return priorityWaitlist.offer(request);
    }

    /**
     * Polls the highest-priority request from the PriorityQueue.
     * Always calls poll() rather than relying on iteration order.
     */
    public Optional<WaitlistRequest> processNextPriorityRequest() {
        return Optional.ofNullable(priorityWaitlist.poll());
    }

    public int getPriorityWaitlistSize() {
        return priorityWaitlist.size();
    }

    // =========================================================================
    // 5. OVERDUE ITEM REMOVAL (Iterator.remove())
    // =========================================================================

    /**
     * Adds a loan record for tracking.
     */
    public void addLoanRecord(LoanRecord record) {
        Objects.requireNonNull(record, "Loan record cannot be null");
        loanRecords.add(record);
    }

    /**
     * Safely identifies and removes overdue, unreturned loan records using Iterator.remove().
     *
     * Preserves:
     * - Records where returned == true.
     * - Records where dueDate >= referenceDate.
     *
     * @param referenceDate the cutoff date used to evaluate overdue status
     * @return list of removed overdue loan records
     */
    public List<LoanRecord> removeOverdueLoans(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "Reference date cannot be null");
        List<LoanRecord> removedLoans = new ArrayList<>();

        Iterator<LoanRecord> iterator = loanRecords.iterator();
        while (iterator.hasNext()) {
            LoanRecord loan = iterator.next();
            if (loan.isOverdue(referenceDate)) {
                removedLoans.add(loan);
                // Safe structural removal: keeps modCount and expectedModCount in sync
                iterator.remove();
            }
        }

        return removedLoans;
    }

    public List<LoanRecord> getLoanRecords() {
        return Collections.unmodifiableList(loanRecords);
    }

    public int getLoanRecordsCount() {
        return loanRecords.size();
    }
}
