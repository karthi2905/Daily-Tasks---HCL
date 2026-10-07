# Daily Tasks — HCL Training

Repository to track daily learning, assessments, and tasks during the HCL training program.

## Daily Progress Tracker

| Day | Topic / Task | Project / Code | Status |
|:---:|---|---|:---:|
| **Day 01** | Core Java Assessment: TechNova Meeting Room & Resource Booking System | [Day-01/technova-booking](./Day-01/technova-booking) | Completed |
| **Day 02** | Core Java Assessment: Monthly Usage Analyser | [Day-02/monthly-usage-analyser](./Day-02/monthly-usage-analyser) | Completed |

### Day 02 Overview: Monthly Usage Analyser
A pure **Core Java console application** demonstrating array processing, primitive operations, and type safety:
- **1-D & 2-D Arrays**: Single-house 12-month usage and 3-house comparative multi-dimensional array analysis.
- **Constants**: `static final` slab thresholds and billing rate definitions.
- **Arithmetic & Casting**: Integer division truncation vs explicit `(double)` casting for floating-point precision.
- **Manual Min/Max & Aggregations**: Manual iterative loops to identify min/max values and month names without stream shortcuts.
- **Ternary Operator**: Multi-level ternary expression for usage grading (`'A'`, `'B'`, `'C'`, `'D'`).
- **Integer Overflow & Long Promotion**: Demonstration of 32-bit `int` arithmetic wraparound and resolution using 64-bit `long` casting and accumulator.
- **Modular Code**: Small, focused static methods with clean console presentation.

### Day 01 Overview: TechNova Meeting Room & Resource Booking System
A complete **Core Java console application** demonstrating object-oriented design and standard Java practices:
- **OOP Concepts**: Encapsulation, Polymorphism, Abstract classes, Interface (`Bookable`), Inheritance hierarchy.
- **Collections Framework**: `List`, `Set` (duplicate prevention), `Map` (in-memory lookup), `Queue` (FIFO approval queue).
- **Generics**: Generic `Repository<T, ID>` and bounded generics.
- **Exception Handling**: Custom checked (`BookingConflictException`) and unchecked exceptions with proper `try-catch-finally` / `try-with-resources`.
- **Sorting**: `Comparable` and custom `Comparator` implementations.
- **Testing**: 18 comprehensive JUnit 5 unit tests covering business rules and edge cases.
