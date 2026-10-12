# Day 7 — Library Waitlist: Java Collections & Performance Analysis

Comprehensive assessment documentation for **Day 7** of the HCL Core Java Training program. This project implements a console-based **Library Waitlist Manager** demonstrating core data structures within the Java Collections Framework (`ArrayList`, `LinkedHashSet`, `ArrayDeque`, `PriorityQueue`, and `Iterator.remove()`), along with an empirical performance experiment benchmarking `ArrayList` versus `LinkedList`.

---

## 1. Project Objectives & Overview

1. **Library Catalogue (`ArrayList<Book>`)**:
   - Stores the master catalogue in an indexed, dynamic array.
   - Demonstrates $O(1)$ random indexed access via `get(index)`.
   - Manages loan state transitions (`borrowBook()` / `returnBook()`) and prevents duplicate checkouts.
   - Enforces field validations and defensive indexing bounds.

2. **Duplicate-Free Student Registration (`LinkedHashSet<String>`)**:
   - Ensures student IDs or waitlist registrations are unique.
   - Guarantees $O(1)$ duplicate rejection while strictly preserving arrival/insertion order.
   - Highlights why `LinkedHashSet` is optimal over `ArrayList` ($O(n)$ search) and standard `HashSet` (non-deterministic iteration order).

3. **Standard FIFO Waitlist (`ArrayDeque<WaitlistRequest>`)**:
   - Manages standard book waitlist queues using a circular array-backed deque.
   - Demonstrates true First-In, First-Out (FIFO) processing via `offerLast()` and `pollFirst()`.
   - Prevents duplicate active requests for the same student and book combination.
   - Explains why `ArrayDeque` is superior to `LinkedList` for queue operations (no per-element `Node` heap allocation, superior cache locality).

4. **Faculty-First Priority Processing (`PriorityQueue<WaitlistRequest>`)**:
   - Implements a min-heap prioritizing `FACULTY` over `STUDENT` requests.
   - Preserves deterministic FIFO tie-breaking within identical tiers via auto-incrementing sequence numbers.
   - Highlights that `PriorityQueue` iteration (`for-each`) exposes heap-internal storage, whereas true priority ordering is realized only via repeated `poll()` operations.

5. **Safe Overdue Loan Removal (`Iterator.remove()`)**:
   - Filters and purges overdue, unreturned records from a loan history collection.
   - Demonstrates why directly invoking `collection.remove()` during an enhanced `for` loop triggers `ConcurrentModificationException`.
   - Shows how `Iterator.remove()` safely synchronizes collection structural mutations (`modCount == expectedModCount`).

6. **Empirical Performance Benchmark (`CollectionBenchmark.java`)**:
   - Measures and compares `ArrayList` vs `LinkedList` for indexed access (`get(i)`), head prepends (`add(0, e)`), middle insertions (`add(mid, e)`), and tail appends (`add(e)`).
   - Reports empirical averages and medians across multiple warm-up and timed trials.

---

## 2. Project Structure

```text
Daily-Tasks---HCL/
└── Day-07/
    └── library-waitlist/
        ├── src/
        │   └── com/
        │       └── technova/
        │           └── library/
        │               ├── model/
        │               │   ├── Book.java               # Book entity with availability tracking
        │               │   ├── WaitlistRequest.java    # Request entity with Priority and tie-breakers
        │               │   └── LoanRecord.java         # Loan entity with overdue calculation
        │               ├── service/
        │               │   └── LibraryService.java     # Manages all 5 collections and business rules
        │               └── app/
        │                   └── LibraryApplication.java # Console demonstration runner
        ├── benchmark/
        │   └── CollectionBenchmark.java                # Performance experiment (ArrayList vs LinkedList)
        ├── test/
        │   └── com/
        │       └── technova/
        │           └── library/
        │               └── LibraryServiceTest.java     # 8 JUnit 5 unit tests
        ├── pom.xml                                     # Maven project configuration (Java 17/25)
        ├── run.bat                                     # Windows compilation and execution script
        ├── README.md                                   # Comprehensive Day 7 documentation
        └── .gitignore                                  # Excludes out/, target/, and *.class
```

> **Structure Note**: Follows the established repository naming convention (`Day-07/library-waitlist/` matching `Day-05/payment-hierarchy/` and `Day-06/order-processor/`).

---

## 3. Collections Deep Dive & Design Justifications

| Collection | Role in Library | Underlying Architecture | Time Complexity | Why Chosen over Alternatives |
|---|---|---|---|---|
| **`ArrayList<Book>`** | Master catalogue | Contiguous resizable array (`Object[]`) | `get(i)`: $O(1)$<br>`add(e)`: amortized $O(1)$ | Direct indexed access allows instant lookup by index; compact contiguous memory layout maximizes CPU cache hit ratios. |
| **`LinkedHashSet<String>`** | Registered students | Hash table combined with doubly-linked list | `add()`: $O(1)$<br>`contains()`: $O(1)$ | Guarantees duplicate prevention while preserving original arrival order. Standard `HashSet` loses insertion order; `ArrayList` requires an expensive $O(n)$ scan. |
| **`ArrayDeque<WaitlistRequest>`** | Normal FIFO waitlist | Resizable circular array with head & tail pointers | `offerLast()`: $O(1)$<br>`pollFirst()`: $O(1)$ | Outperforms `LinkedList` for queue operations because it avoids allocating individual `Node` wrapper objects and provides cache-friendly sequential storage. |
| **`PriorityQueue<WaitlistRequest>`** | Faculty-first waitlist | Binary min-heap array | `offer()`: $O(\log n)$<br>`poll()`: $O(\log n)$<br>`peek()`: $O(1)$ | Efficiently bubbles highest priority requests to the root without sorting the entire collection after every insertion. |
| **`Iterator<LoanRecord>`** | Overdue loan pruning | Fail-fast cursor with structural modification tracking | `remove()`: $O(1)$ cursor step | Synchronously updates both `modCount` and `expectedModCount`, avoiding `ConcurrentModificationException`. |

---

## 4. Priority Queue & Tie-Breaking Mechanics

The `PriorityQueue<WaitlistRequest>` orders waitlist requests using an explicit two-tier comparator:

```java
public static final Comparator<WaitlistRequest> PRIORITY_COMPARATOR =
        Comparator.comparing(WaitlistRequest::getPriority)
                  .thenComparingLong(WaitlistRequest::getSequenceNumber);
```

### Ordering Rules
1. **Primary Key — Priority**:
   - `FACULTY` has enum ordinal 0.
   - `STUDENT` has enum ordinal 1.
   - Natural comparison places `FACULTY` ahead of `STUDENT`.
2. **Secondary Key — Sequence Number (Tie-Breaking)**:
   - When two requests belong to the same tier (e.g. two Students or two Faculty members), the tie is broken deterministically by their `sequenceNumber` in ascending order (earlier arrival first).

### Crucial PriorityQueue Insight
A `PriorityQueue` implements a **binary min-heap** stored in an array:
- The element at index 0 (`queue[0]`) is always the minimum (highest priority).
- **Iteration order is NOT sorted**: Traversing a `PriorityQueue` using `for (WaitlistRequest r : queue)` prints elements in internal heap-array level order, not priority order!
- **True priority order requires calling `poll()`**: Calling `poll()` repeatedly extracts the root and sifts down the remaining nodes in $O(\log n)$ time, yielding the true sorted priority sequence.

---

## 5. Safe Removal with `Iterator.remove()`

### The Problem: `ConcurrentModificationException`
When iterating over a collection using an enhanced `for` loop:
```java
// DANGEROUS CODE - THROWS ConcurrentModificationException!
for (LoanRecord loan : loans) {
    if (loan.isOverdue(referenceDate)) {
        loans.remove(loan); // Mutates collection directly!
    }
}
```
**Why this fails**:
1. The enhanced `for` loop creates an internal `Iterator` under the hood.
2. The iterator stores an internal field `expectedModCount = list.modCount`.
3. Calling `loans.remove(loan)` directly mutates the list, incrementing `list.modCount`.
4. On the next call to `iterator.next()`, the iterator checks `if (modCount != expectedModCount)`.
5. Because `modCount` changed without the iterator knowing, it immediately throws `ConcurrentModificationException`.

### The Solution: Explicit `Iterator.remove()`
```java
Iterator<LoanRecord> iterator = loans.iterator();
while (iterator.hasNext()) {
    LoanRecord loan = iterator.next();
    if (loan.isOverdue(referenceDate)) {
        iterator.remove(); // Safely updates both list structure and expectedModCount!
    }
}
```
`Iterator.remove()` structurally removes the last element returned by `next()`, decrements the internal cursor, and updates `expectedModCount = list.modCount`. No exception is thrown, and iteration proceeds safely.

> **Domain Note**: In enterprise production systems, overdue loan records are flagged with an updated status (e.g., `OVERDUE_PENALTY`) rather than deleted, ensuring financial and regulatory audit trails remain intact.

---

## 6. Actual Console Demonstration Output

Executing `java -cp out com.technova.library.app.LibraryApplication`:

```text
================================================================================
     TECHNOVA LIBRARY WAITLIST -- JAVA COLLECTIONS BEHAVIOR DEMO                 
================================================================================

--------------------------------------------------------------------------------
1. CATALOGUE DEMO :: ArrayList<Book>
--------------------------------------------------------------------------------
Catalogue initialized with 5 seeded books:
  [0] [B101] "Clean Code" by Robert C. Martin (AVAILABLE)
  [1] [B102] "Effective Java" by Joshua Bloch (AVAILABLE)
  [2] [B103] "Design Patterns" by Erich Gamma et al. (AVAILABLE)
  [3] [B104] "Java Concurrency in Practice" by Brian Goetz (AVAILABLE)
  [4] [B105] "Introduction to Algorithms" by Thomas H. Cormen (AVAILABLE)

Demonstrating Direct O(1) Indexed Access with get(index):
  service.getBookByIndex(2) -> Design Patterns

Borrowing and State Transition Demonstration:
  Action: Borrowing book 'B101'...
  [PASS] Status updated: [B101] "Clean Code" by Robert C. Martin (CHECKED OUT)
  Action: Attempting to borrow 'B101' again while unavailable...
  [PASS] Correctly intercepted: Book 'Clean Code' (B101) is already unavailable.
  Action: Returning book 'B101' back to catalogue...
  [PASS] Status restored: [B101] "Clean Code" by Robert C. Martin (AVAILABLE)

--------------------------------------------------------------------------------
2. DUPLICATE PREVENTION & INSERTION ORDER :: LinkedHashSet<String>
--------------------------------------------------------------------------------
Registration attempts in order:
  Register 'STU-1001' -> ACCEPTED (New) (Current Set Size: 1)
  Register 'STU-1002' -> ACCEPTED (New) (Current Set Size: 2)
  Register 'STU-1003' -> ACCEPTED (New) (Current Set Size: 3)
  Register 'STU-1001' -> REJECTED (Duplicate) (Current Set Size: 3)
  Register 'STU-1004' -> ACCEPTED (New) (Current Set Size: 4)
  Register 'STU-1002' -> REJECTED (Duplicate) (Current Set Size: 4)

Final Registered Students in LinkedHashSet (strictly preserves insertion order):
  1. STU-1001
  2. STU-1002
  3. STU-1003
  4. STU-1004

Why LinkedHashSet?
  - ArrayList allows duplicates and requires O(n) contains() scan.
  - HashSet prevents duplicates in O(1) time but loses original arrival order.
  - LinkedHashSet maintains doubly-linked buckets: O(1) uniqueness + order retention.

--------------------------------------------------------------------------------
3. NORMAL FIFO WAITLIST :: ArrayDeque<WaitlistRequest>
--------------------------------------------------------------------------------
Enqueueing 3 normal requests into ArrayDeque:
  Enqueued 3 requests. Queue size: 3

Attempting duplicate active request for STU-1001 on B102:
  Enqueue duplicate -> REJECTED [Duplicate active request] (Queue size remains: 3)

Processing requests from front of queue (FIFO order):
  Processing #1: [REQ-101 | Seq: 1] STU-1001 (STUDENT) for Book B102
  Processing #2: [REQ-102 | Seq: 2] STU-1002 (STUDENT) for Book B102
  Processing #3: [REQ-103 | Seq: 3] STU-1003 (STUDENT) for Book B102
  All normal requests fulfilled. Queue size after processing: 0

Why ArrayDeque over LinkedList for Queues?
  - ArrayDeque is backed by a circular array (no node object allocations).
  - Better cache locality, lower memory footprint, and faster head/tail ops.

--------------------------------------------------------------------------------
4. FACULTY-FIRST PRIORITY QUEUE :: PriorityQueue<WaitlistRequest>
--------------------------------------------------------------------------------
Enqueueing interleaved requests into PriorityQueue:
  1. Enqueued: [REQ-201 | Seq: 101] STU-1001 (STUDENT) for Book B104
  2. Enqueued: [REQ-202 | Seq: 102] FAC-5001 (FACULTY) for Book B104
  3. Enqueued: [REQ-203 | Seq: 103] STU-1002 (STUDENT) for Book B104
  4. Enqueued: [REQ-204 | Seq: 104] FAC-5002 (FACULTY) for Book B104

Processing requests using repeated poll() (demonstrates true priority order):
  Turn #1 -> [REQ-202 | Seq: 102] FAC-5001 (FACULTY) for Book B104
  Turn #2 -> [REQ-204 | Seq: 104] FAC-5002 (FACULTY) for Book B104
  Turn #3 -> [REQ-201 | Seq: 101] STU-1001 (STUDENT) for Book B104
  Turn #4 -> [REQ-203 | Seq: 103] STU-1002 (STUDENT) for Book B104

Crucial PriorityQueue Insight:
  - PriorityQueue is a binary min-heap, NOT a sorted list!
  - Iterating over PriorityQueue with an enhanced for-loop does NOT guarantee sorted order.
  - True priority order is only produced by repeatedly invoking poll().

--------------------------------------------------------------------------------
5. SAFE REMOVAL OF OVERDUE ITEMS :: Iterator.remove()
--------------------------------------------------------------------------------
Cutoff Reference Date: 2026-10-15

Initial Loan Records (5 total):
  [LN-101] Student: STU-1001 | Book: B101   | Due: 2026-10-01 | Status: ACTIVE
  [LN-102] Student: STU-1002 | Book: B102   | Due: 2026-10-05 | Status: RETURNED
  [LN-103] Student: STU-1003 | Book: B103   | Due: 2026-10-20 | Status: ACTIVE
  [LN-104] Student: STU-1004 | Book: B104   | Due: 2026-09-25 | Status: ACTIVE
  [LN-105] Student: STU-1005 | Book: B105   | Due: 2026-10-12 | Status: RETURNED

Executing service.removeOverdueLoans(referenceDate) via Iterator.remove()...

[REMOVED] 2 Overdue Unreturned Records:
  - LN-101 (Due 2026-10-01 was before 2026-10-15)
  - LN-104 (Due 2026-09-25 was before 2026-10-15)

[RETAINED] 3 Active/Returned Records Still in Library:
  + [LN-102] Student: STU-1002 | Book: B102   | Due: 2026-10-05 | Status: RETURNED
  + [LN-103] Student: STU-1003 | Book: B103   | Due: 2026-10-20 | Status: ACTIVE
  + [LN-105] Student: STU-1005 | Book: B105   | Due: 2026-10-12 | Status: RETURNED

Why Iterator.remove() is Mandatory:
  - Directly calling collection.remove() inside a 'for (e : list)' loop triggers
    java.util.ConcurrentModificationException because modCount differs from expectedModCount.
  - Iterator.remove() cleanly synchronizes expectedModCount = modCount, preventing crashes.

--------------------------------------------------------------------------------
6. FINAL STATE & COLLECTION SIZE SUMMARY
--------------------------------------------------------------------------------
  1. Books in Catalogue (ArrayList):         5 books
  2. Registered Students (LinkedHashSet):    4 unique IDs
  3. Normal Waitlist (ArrayDeque):           0 requests
  4. Priority Waitlist (PriorityQueue):      0 requests
  5. Active/Historical Loans (ArrayList):    3 records

================================================================================
                    ALL SCENARIOS COMPLETED SUCCESSFULLY                        
================================================================================
```

---

## 7. Performance Benchmark: ArrayList vs. LinkedList

### A. Theoretical Time Complexity Comparison

| Operation | `ArrayList` | `LinkedList` | Hardware / Memory Driver |
|---|---|---|---|
| **Indexed Access (`get(i)`)** | **$O(1)$** | **$O(n)$** | `ArrayList` computes offset via base address + index * 4/8 bytes. `LinkedList` must traverse $N/4$ pointers on average. |
| **Insert at Beginning (`add(0, e)`)** | **$O(n)$** | **$O(1)$** | `ArrayList` shifts all $N$ elements via `System.arraycopy`. `LinkedList` allocates a new `Node` and updates the head reference. |
| **Insert at Middle (`add(mid, e)`)** | **$O(n)$ shift** | **$O(n)$ traverse + $O(1)$ relink** | `ArrayList` locates in $O(1)$ and shifts $N/2$ elements. `LinkedList` must traverse $N/2$ nodes to reach the index. |
| **Append at End (`add(e)`)** | **Amortized $O(1)$** | **$O(1)$** | Both are asymptotically $O(1)$, but `ArrayList` wins in wall-clock time due to cache locality and no `Node` object creation. |
| **Memory per Element** | ~4–8 bytes | ~24–32 bytes | `LinkedList` incurs heavy overhead: object header + `prev` reference + `next` reference + item pointer. |
| **CPU Cache Locality** | **High** | **Low** | `ArrayList` elements reside consecutively in CPU L1/L2 cache lines; `LinkedList` nodes are scattered in the heap. |

### B. Actual Measured Benchmark Results

Environment: **Java 25.0.1 (Oracle Corporation) on Windows 11 64-bit**.  
Methodology: Pre-warming loops executed, deterministic fixed-seed queries, checksum accumulators to prevent dead code elimination, 5 trials reporting average and median.

```text
================================================================================
EXPERIMENT 1: INDEXED ACCESS (get(index))
Dataset Size: 25,000 elements | Query Count: 10,000 random lookups
================================================================================
  ArrayList    | Avg:    0.648 ms | Median:    0.546 ms | (Trials: 1.75, 0.55, 0.55, 0.22, 0.17 ms)
  LinkedList   | Avg:  196.691 ms | Median:  195.069 ms | (Trials: 206.03, 195.07, 195.81, 195.05, 191.49 ms)
Result: ArrayList was ~303.8x FASTER than LinkedList for random indexed access.

================================================================================
EXPERIMENT 2: HEAD INSERTIONS (add(0, element))
Insertions Count: 20,000 prepends at index 0
================================================================================
  ArrayList    | Avg:   41.430 ms | Median:   41.035 ms | (Trials: 42.03, 41.02, 39.60, 41.03, 43.46 ms)
  LinkedList   | Avg:    1.676 ms | Median:    0.679 ms | (Trials: 3.03, 3.47, 0.68, 0.65, 0.55 ms)
Result: LinkedList was ~24.7x FASTER than ArrayList for head insertions.

================================================================================
EXPERIMENT 3: MIDDLE INSERTIONS (add(size / 2, element))
Insertions Count: 10,000 middle insertions
================================================================================
  ArrayList    | Avg:    8.201 ms | Median:    8.331 ms | (Trials: 8.33, 8.47, 8.58, 7.30, 8.32 ms)
  LinkedList   | Avg:  109.263 ms | Median:  107.230 ms | (Trials: 106.73, 108.24, 105.80, 107.23, 118.31 ms)
Result: ArrayList was ~13.3x FASTER than LinkedList for middle insertions.

================================================================================
EXPERIMENT 4: TAIL APPENDS (add(element))
Appends Count: 25,000 elements
================================================================================
  ArrayList    | Avg:    2.196 ms | Median:    1.504 ms | (Trials: 3.91, 2.94, 1.50, 1.30, 1.33 ms)
  LinkedList   | Avg:    1.726 ms | Median:    1.148 ms | (Trials: 2.53, 2.77, 1.15, 1.11, 1.07 ms)
```

### C. Key Takeaway & Interview Insights
1. **The "LinkedList is O(1) for Insertions" Myth**:
   - In textbooks, `LinkedList` insertion is called $O(1)$, but that is **only** true when you already hold a direct reference to the target `Node`!
   - When calling `list.add(index, element)`, `LinkedList` must first traverse $N/2$ nodes from head or tail, making the entire call **$O(n)$**.
   - In practice, modern CPU architectures favor `ArrayList` even for middle insertions because hardware memory prefetchers and vector operations (`System.arraycopy`) shift contiguous memory blocks far faster than traversing pointer chains through fragmented heap memory.
2. **When to Actually Use `LinkedList`**:
   - Almost never in modern Java. If queue or deque operations are needed, `ArrayDeque` is consistently faster and uses less memory.

### D. Limitations of the Timing Experiment
- This benchmark uses `System.nanoTime()` with manual warm-up iterations and is designed for illustrative comparison.
- It is not a formal Java Microbenchmark Harness (JMH) execution. In production benchmarks, JMH should be used to protect against JVM JIT dead-code elimination, compiler hoisting, and garbage collector latency spikes.

---

## 8. Compilation and Execution Commands

Navigate to `Day-07/library-waitlist`:

### Option 1: Direct JDK Toolchain (`javac` and `java`)
```bash
# Compile all source and benchmark classes into out/
javac -d out src/com/technova/library/model/*.java src/com/technova/library/service/*.java src/com/technova/library/app/*.java benchmark/CollectionBenchmark.java

# Run main application demo
java -cp out com.technova.library.app.LibraryApplication

# Run performance benchmark
java -cp out com.technova.library.benchmark.CollectionBenchmark
```

### Option 2: Windows Batch Runner (`run.bat`)
```cmd
# Run main demo
run.bat

# Run benchmark experiment
run.bat --benchmark

# Run both demonstration and benchmark
run.bat --all
```

### Option 3: Apache Maven
```bash
# Run unit test suite
mvn clean test

# Run application via exec-maven-plugin
mvn compile exec:java
```

---

## 9. Unit Test Suite Results

Tests located in `test/com/technova/library/LibraryServiceTest.java`:

| # | Test Method | Scenario Verified | Expected Outcome | Actual Result |
|:---:|---|---|---|:---:|
| 1 | `testCatalogueAddAndGetByIndex` | Seeded catalogue indexing | Size is 5, `get(0)` & `get(2)` return expected books | **PASS** |
| 2 | `testCatalogueBorrowAndReturn` | Borrow & return state transitions | `borrowBook()` flips available to false; duplicate borrow throws `IllegalStateException`; return restores true | **PASS** |
| 3 | `testLinkedHashSetDuplicatePreventionAndOrder` | Uniqueness & insertion order | Duplicate returns `false`, set size does not increase, iteration order matches insertion order | **PASS** |
| 4 | `testNormalWaitlistFifoOrder` | FIFO queuing in `ArrayDeque` | 3 requests processed in exact arrival sequence (R1 $\to$ R2 $\to$ R3) | **PASS** |
| 5 | `testNormalWaitlistRejectsDuplicateActiveRequest` | Duplicate active request prevention | Duplicate active request returns `false`, queue size remains 1 | **PASS** |
| 6 | `testPriorityQueueFacultyFirstWithTieBreaking` | Priority ordering with tie-breaker | Interleaved requests polled in order: Faculty 1 $\to$ Faculty 2 $\to$ Student 1 $\to$ Student 2 | **PASS** |
| 7 | `testOverdueRemovalIteratorSafe` | Safe overdue cleanup via iterator | 2 overdue unreturned records removed; 2 non-overdue/returned records preserved | **PASS** |
| 8 | `testModelValidations` | Constructor validation | Blank strings and invalid due dates throw `IllegalArgumentException` | **PASS** |

**Surefire Test Execution**:
```text
[INFO] Running com.technova.library.LibraryServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.174 s -- in com.technova.library.LibraryServiceTest
[INFO] BUILD SUCCESS
```

---

## 10. Completion Summary

- **Repository Rules**: Preserved all previous daily tasks and `BookMg/`.
- **Collections Behavior**: All 5 collections implemented with verified console scenarios.
- **Empirical Benchmark**: Measured real hardware numbers for ArrayList vs LinkedList across 4 operations.
- **Test Quality**: 8 JUnit 5 tests passing with zero failures.
- **Git Commit**: `day-7: library waitlist and collections benchmark`.
