# TechNova Meeting Room & Resource Booking System

A complete **Core Java in-memory console application** demonstrating all essential Java concepts for a professional assessment.

---

## Project Overview

TechNova is a technology company that needs an internal system for employees to reserve meeting rooms and equipment.  
The system manages six resources (three meeting rooms, one restricted lab, two projectors) and implements two complete business flows — **resource booking** and **approval processing** — entirely in memory using Core Java and Maven.

---

## Features

### Flow 1 — Resource Booking
- Employee selects a resource by ID
- Enters date and time slot
- System validates input and checks for conflicts
- Normal resources → booking confirmed immediately
- Restricted resources → booking queued for approval (PENDING)

### Flow 2 — Approval Processing
- Approver views the FIFO approval queue
- Processes the next pending request
- Approves → status becomes CONFIRMED
- Rejects → status becomes REJECTED

---

## Core Java Concepts Demonstrated

| Concept | Where Used |
|---|---|
| Encapsulation | All domain model fields are `private` with getters/setters |
| Inheritance | `Employee`, `Approver`, `Admin` extend `User`; `MeetingRoom`, `Equipment` extend `Resource` |
| Polymorphism | `displayDetails()` dispatches to `MeetingRoom` or `Equipment` at runtime |
| Abstract class | `User` and `Resource` are abstract |
| Interface | `Bookable` — implemented by `Resource`; used by `BookingService` |
| Method overriding | `displayRole()` in `Employee`, `Approver`, `Admin`; `displayDetails()` in `MeetingRoom`, `Equipment` |
| Method overloading | Two `createBooking(...)` variants in `BookingService`; constructors in exceptions |
| `this` keyword | All constructors use `this.field = param` |
| `super` keyword | All subclass constructors call `super(id, name, email)` |
| Access modifiers | `private` fields, `public` methods, `protected` bookings list in `Resource` |
| Checked exception | `BookingConflictException extends Exception` — declared with `throws` |
| Unchecked exception | `ResourceNotFoundException`, `InvalidBookingException` extend `RuntimeException` |
| `throw` | Used in service and resource layers to raise exceptions |
| `throws` | Declared on `BookingService.createBooking` |
| `try-catch` | `ConsoleMenu` uses specific catch blocks for each exception type |
| `finally` | `ConfigFileLoader.loadLines()` logs completion status in `finally` |
| Try-with-resources | `ConfigFileLoader` uses `try (BufferedReader ...)` for classpath file reading |
| `List<Booking>` | `BookingService.allBookings` — stores all booking records |
| `Set<String>` | `DataInitializer.registeredEmployeeIds` — prevents duplicate employee IDs |
| `Map<String, Resource>` | `InMemoryRepository` backed by `HashMap` — O(1) resource lookup |
| `Queue<Booking>` | `BookingService.approvalQueue` — FIFO queue via `ArrayDeque` |
| Generics | `Repository<T, ID>` and `InMemoryRepository<T, ID>` |
| Bounded generics | `<E extends Resource> void printResources(List<E> resources)` |
| `Comparable` | `Booking implements Comparable<Booking>` — sorts by date then start time |
| `Comparator` | `BookingByResourceComparator`, `BookingByEmployeeComparator`, `BookingByStartTimeComparator` |
| Collections iteration | `for-each` over `List`, `Queue`, `Set` throughout the codebase |
| Input validation | `ConsoleMenu` validates all user input before delegating to services |
| Maven | Standard Maven project structure with JUnit 5 dependency |

---

## Architecture

```
com.technova.booking/
│
├── Main.java                    ← Entry point only; bootstraps and delegates
│
├── model/                       ← Domain model (no business logic)
│   ├── User.java                ← Abstract base class
│   ├── Employee.java            ← Concrete user subclass
│   ├── Approver.java            ← Concrete user subclass
│   ├── Admin.java               ← Concrete user subclass
│   ├── Resource.java            ← Abstract resource; implements Bookable
│   ├── MeetingRoom.java         ← Concrete resource subclass
│   ├── Equipment.java           ← Concrete resource subclass
│   ├── Bookable.java            ← Interface
│   ├── Booking.java             ← Booking entity; implements Comparable
│   └── BookingStatus.java       ← Enum
│
├── exception/                   ← Custom exception hierarchy
│   ├── BookingConflictException.java   ← Checked
│   ├── ResourceNotFoundException.java  ← Unchecked
│   └── InvalidBookingException.java    ← Unchecked
│
├── repository/                  ← Generic in-memory storage
│   ├── Repository.java          ← Generic interface
│   └── InMemoryRepository.java  ← HashMap-backed implementation
│
├── service/                     ← Business logic
│   ├── ResourceService.java     ← Resource CRUD + bounded generic display
│   ├── BookingService.java      ← Booking creation, conflict checking, queue
│   └── ApprovalService.java     ← Approval workflow
│
├── comparator/                  ← Comparator implementations
│   ├── BookingByResourceComparator.java
│   ├── BookingByEmployeeComparator.java
│   └── BookingByStartTimeComparator.java
│
├── util/                        ← Utilities (no business logic)
│   ├── ConfigFileLoader.java    ← try-with-resources demonstration
│   └── DataInitializer.java     ← Sample data seeding
│
└── ui/
    └── ConsoleMenu.java         ← All user interaction; specific exception handling
```

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Clone and run

```bash
git clone <repo-url>
cd technova-booking

# Compile
mvn clean compile

# Run application
mvn exec:java

# Run tests
mvn test
```

---

## Sample Console Output

```
=================================================
       TECHNOVA RESOURCE BOOKING SYSTEM
=================================================

[Init] Attempted duplicate registration of E101 — correctly rejected by Set
[Config] Optional file 'banner.txt' not found on classpath — using defaults.
[Config] File load attempt for 'banner.txt' completed. Success: false

  System initialised. 6 resources loaded.


=================================================
       TECHNOVA RESOURCE BOOKING SYSTEM
=================================================
  1. View Resources
  2. Create Booking
  3. View All Bookings
  4. View Approval Queue
  5. Process Approval
  6. Sort Bookings
  7. View Employees
  8. Exit
-------------------------------------------------
Enter your choice: 2

=================================================
           CREATE BOOKING
=================================================
Enter Employee ID: E101
Enter Resource ID: RM001
Enter Date (YYYY-MM-DD): 2026-10-06
Enter Start Time (HH:MM): 10:00
Enter End Time (HH:MM): 11:00

  Checking resource...
  Checking booking conflicts...

  Booking created successfully.

  Booking ID : B001
  Employee   : Karthik
  Resource   : Conference Room A
  Date       : 2026-10-06
  Time       : 10:00 - 11:00
  Status     : CONFIRMED
  Created At : 2026-10-06T...
```

---

## Memory Awareness

### Stack
The JVM stack stores:
- Local variables and method parameters for each active method call
- Primitive values (e.g., `int choice`, `boolean overlaps`)
- References to objects (the reference itself is on the stack; the object is on the heap)

Each method call creates a new **stack frame**. Frames are automatically popped when the method returns.

### Heap
All objects created with `new` live on the heap:

```java
new Booking(...)         // every booking record
new Employee(...)        // user objects
new MeetingRoom(...)     // resource objects
new ArrayList<>()        // allBookings list
new HashMap<>()          // inside InMemoryRepository
new ArrayDeque<>()       // approval queue
```

The garbage collector reclaims heap objects when no references point to them.

### Memory Pitfall — Unbounded List Growth

```java
private final List<Booking> allBookings = new ArrayList<>();
```

In this demo, every booking is kept forever (confirmed, rejected, cancelled).  
In a long-running production system this list would grow without bound, increasing heap usage continuously.

**Mitigation strategies:**
- Archive old bookings to external storage and remove them from the in-memory list.
- Implement a retention policy (e.g., keep only last 90 days).
- Use weak references or an LRU cache for historical data.

---

## AI Review

See [`docs/AI_REVIEW.md`](docs/AI_REVIEW.md) for:
- The GitHub Copilot-generated booking conflict method
- The containment-case bug that was identified
- The corrected interval-overlap implementation
- Test results verifying the fix

---

## Git Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Production-ready, stable code |
| `feature/core-java-booking` | Active development branch |

---

## Author

Karthik — TechNova Core Java Assessment, October 2026
