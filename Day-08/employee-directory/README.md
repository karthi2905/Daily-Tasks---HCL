# Day 8 — Employee Directory: Java Collections & Object Contracts

Comprehensive assessment documentation for **Day 8** of the HCL Core Java Training program. This project implements a console-based **Employee Directory** demonstrating key aspects of the Java Collections Framework (`HashMap`, `TreeMap`, `Map.merge()`), natural ordering via `Comparable`, multi-level tie-breaking via chained `Comparator`, and an educational demonstration of the `equals()` and `hashCode()` contract bug and its resolution.

---

## 1. Project Objectives & Overview

1. **HashMap Employee Lookup**:
   - Stores employee records keyed by `int id` in a `HashMap<Integer, Employee>`.
   - Provides $O(1)$ average-case lookup, duplicate-ID rejection, and safe missing-ID handling returning `Optional.empty()`.
   - Encapsulates internal collection state by returning unmodifiable collections.

2. **Department Frequency Counting with `Map.merge()`**:
   - Employs `Map.merge(dept, 1, Integer::sum)` to count employees per department.
   - Normalizes department names (trimmed and uppercase) to avoid fragmentation across case variations.
   - Eliminates boilerplate conditional checking (`containsKey` / `get` / `put`).

3. **Sorted Department Report with `TreeMap`**:
   - Aggregates headcount, roster, total payroll, and average salary per department.
   - Uses `TreeMap<String, DepartmentSummary>` to guarantee alphabetical ordering of department keys ($O(\log K)$ operations via Red-Black Tree).
   - Utilizes `BigDecimal` and `RoundingMode.HALF_UP` for exact financial calculations without floating-point inaccuracies.

4. **Natural Ordering via `Comparable<Employee>`**:
   - Implements `Comparable<Employee>` on `Employee` to define intrinsic sorting by ascending employee ID (`Integer.compare(this.id, other.id)`).
   - Demonstrates natural sorting using `Collections.sort(list)` without external comparators.

5. **Top-Five Salaries via Comparator Chaining**:
   - Ranks top earners using a chained comparator:
     ```java
     Comparator.comparing(Employee::getSalary, Comparator.reverseOrder())
               .thenComparingInt(Employee::getId)
               .thenComparing(Employee::getName)
     ```
   - Sorts salary descending while keeping secondary tie-breakers (employee ID and name) ascending.
   - Handles directories with fewer than 5 employees and empty collections safely.

6. **Reproducible Missing-`hashCode()` Bug Demonstration**:
   - **Part A (Broken)**: A nested `BrokenEmployee` class overrides `equals()` without `hashCode()`. Demonstrates that equal objects with different identity hash codes fail `HashSet.contains()` lookups and corrupt set uniqueness by storing duplicate entries.
   - **Part B (Fixed)**: Shows how the production `Employee` class adheres to the `equals()`/`hashCode()` contract, ensuring predictable hashing, correct retrieval, and duplicate prevention.

---

## 2. Project Structure

```text
Daily-Tasks---HCL/
└── Day-08/
    └── employee-directory/
        ├── src/
        │   └── com/
        │       └── technova/
        │           └── employeedirectory/
        │               ├── model/
        │               │   ├── Employee.java               # Validated model with Comparable & equals/hashCode
        │               │   └── DepartmentSummary.java      # Aggregated department metrics (BigDecimal math)
        │               ├── service/
        │               │   └── EmployeeDirectoryService.java # HashMap, TreeMap, merge(), & Comparator logic
        │               ├── demo/
        │               │   └── HashCodeBugDemo.java        # Broken vs Fixed equals/hashCode demonstration
        │               └── app/
        │                   └── EmployeeDirectoryApplication.java # Complete console demonstration suite
        ├── test/
        │   └── com/
        │       └── technova/
        │           └── employeedirectory/
        │               └── EmployeeDirectoryServiceTest.java # 12 JUnit 5 unit tests
        ├── pom.xml                                         # Maven build file (Java 17/25 compatible)
        ├── run.bat                                         # Windows batch runner
        ├── README.md                                       # Comprehensive Day 8 documentation
        └── .gitignore                                      # Excludes out/, target/, and *.class
```

> **Repository Convention**: Follows the established repository naming convention (`Day-08/employee-directory/`, matching `Day-05/payment-hierarchy/`, `Day-06/order-processor/`, and `Day-07/library-waitlist/`).

---

## 3. Core Concepts & Collections Deep Dive

### A. HashMap vs. TreeMap

| Feature | `HashMap<K, V>` | `TreeMap<K, V>` |
|---|---|---|
| **Underlying Data Structure** | Hash table (array of buckets with linked lists / red-black tree bins in Java 8+) | Red-Black Tree (Self-balancing Binary Search Tree) |
| **Ordering** | None (non-deterministic; depends on hash code and table capacity) | Sorted (natural ordering of keys or custom `Comparator`) |
| **Lookup Time Complexity** | **$O(1)$ average**, $O(n)$ worst-case (or $O(\log n)$ with treeified bins) | **$O(\log n)$ guaranteed** for all operations |
| **Null Key Support** | Allows one `null` key | Does **not** allow `null` keys (throws `NullPointerException` during comparison) |
| **Role in Project** | Primary employee storage keyed by Employee ID for fast lookups. | Department summary report sorted alphabetically by department name. |

### B. Idiomatic Frequency Counting with `Map.merge()`
Traditional counting in Java required verbose boilerplate:
```java
// Traditional boilerplate
Integer count = map.get(dept);
if (count == null) {
    map.put(dept, 1);
} else {
    map.put(dept, count + 1);
}
```
Java 8 introduced `Map.merge()`, which simplifies this to a single, atomic operation:
```java
counts.merge(dept, 1, Integer::sum);
```
**Execution Semantics**:
1. If the key `dept` is not in the map (or mapped to `null`): sets the value to `1`.
2. If the key `dept` already exists: passes `(existingValue, 1)` to the remapping function `Integer::sum`, storing the result back in the map.

### C. Natural Ordering (`Comparable`) vs. Custom Ordering (`Comparator`)

| Dimension | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| **Definition** | Implemented directly on the class (`public class Employee implements Comparable<Employee>`). | External strategy class or lambda expression (`Comparator<Employee> comp = ...`). |
| **Role** | Defines the **single, default "natural" ordering** of objects (e.g. Employee ID ascending). | Allows **multiple, arbitrary ordering strategies** (e.g., salary descending, hire date, name). |
| **Method** | `int compareTo(T other)` | `int compare(T o1, T o2)` |
| **Sorting Call** | `Collections.sort(list)` or `list.sort(null)` | `list.sort(comparator)` or `Collections.sort(list, comparator)` |

### D. Comparator Chaining & The Reversal Trap
To sort by salary descending and break ties by employee ID ascending, one must avoid reversing the entire chain:
```java
// TRAP: Reversing the entire chain reverses secondary keys too!
Comparator.comparing(Employee::getSalary)
          .thenComparingInt(Employee::getId)
          .reversed(); // BUG: ID is now sorted DESCENDING instead of ascending!

// CORRECT: Apply reverseOrder() specifically to the salary comparator
Comparator.comparing(Employee::getSalary, Comparator.reverseOrder())
          .thenComparingInt(Employee::getId)
          .thenComparing(Employee::getName);
```

---

## 4. The `equals()` and `hashCode()` Contract & Bug Reproduction

### The Contract Rules (JLS / java.lang.Object)
1. **Consistency**: If `a.equals(b)` is `true`, then `a.hashCode()` **must** be equal to `b.hashCode()`.
2. **Pigeonhole Principle**: If `a.hashCode() == b.hashCode()`, `a.equals(b)` is **not** necessarily true (hash collision).
3. **Immutability of Keys**: Fields involved in computing equality and hash codes must remain immutable while the object is a key in a map or stored in a set.

### What Happens When You Override `equals()` Without `hashCode()`?
In `HashCodeBugDemo.java`, `BrokenEmployee` overrides `equals()` checking `id == other.id`, but inherits `Object.hashCode()`:
1. `b1` and `b2` both have `id = 101`. `b1.equals(b2)` evaluates to `true`.
2. Because `hashCode()` was not overridden, the JVM computes identity hash codes (typically derived from object memory locations):
   - `b1.hashCode() -> 723074861` (bucket index A)
   - `b2.hashCode() -> 895328852` (bucket index B)
3. Adding `b1` puts it into **Bucket A**.
4. Calling `set.contains(b2)` computes `b2.hashCode()`, directs the search to **Bucket B**, and returns `false` without ever calling `equals()`!
5. Calling `set.add(b2)` places `b2` into **Bucket B**. The set now holds two "equal" objects, breaking set uniqueness!

### The Fix
In `Employee.java`, `hashCode()` is overridden to hash the exact same immutable business identity:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee employee)) return false;
    return this.id == employee.id;
}

@Override
public int hashCode() {
    return Integer.hashCode(id);
}
```
Now both objects yield hash code `101`, always target the same bucket, invoke `equals()` when needed, and allow `HashSet` to reject duplicates properly.

---

## 5. Actual Console Output

Executing `java -cp out com.technova.employeedirectory.app.EmployeeDirectoryApplication`:

```text
================================================================================
   TECHNOVA EMPLOYEE DIRECTORY -- COLLECTIONS & OBJECT CONTRACTS (DAY 8)        
================================================================================

--------------------------------------------------------------------------------
1. EMPLOYEE DIRECTORY OVERVIEW (Internal HashMap Storage)
--------------------------------------------------------------------------------
Total employees loaded: 10
  [ID: 0101] Alice Smith        | Dept: Engineering    | Salary: $110,000.00
  [ID: 0102] Bob Vance          | Dept: Engineering    | Salary: $125,000.00
  [ID: 0103] Charlie Brown      | Dept: Engineering    | Salary: $ 95,000.00
  [ID: 0104] Diana Prince       | Dept: HR             | Salary: $ 84,000.00
  [ID: 0105] Edward Cullen      | Dept: Marketing      | Salary: $ 78,000.00
  [ID: 0106] Fiona Gallagher    | Dept: HR             | Salary: $ 84,000.00
  [ID: 0107] George Clark       | Dept: Finance        | Salary: $ 92,000.00
  [ID: 0108] Hannah Abbott      | Dept: Finance        | Salary: $ 92,000.00
  [ID: 0109] Ian Malcolm        | Dept: Marketing      | Salary: $ 88,000.00
  [ID: 0110] Jack Reacher       | Dept: Engineering    | Salary: $125,000.00

--------------------------------------------------------------------------------
2. HASHMAP LOOKUP DEMO (O(1) Average Complexity)
--------------------------------------------------------------------------------
Looking up Employee ID 101...
  [PASS] Found: [ID: 0101] Alice Smith        | Dept: Engineering    | Salary: $110,000.00

Looking up Non-Existent Employee ID 999...
  [PASS] Correctly returned empty Optional (no crash or null pointer exception).

Attempting to add duplicate Employee with existing ID 101...
  [PASS] Intercepted expected validation error: Employee with ID 101 already exists in the directory.

--------------------------------------------------------------------------------
3. DEPARTMENT FREQUENCY COUNTS (Using Map.merge(dept, 1, Integer::sum))
--------------------------------------------------------------------------------
  FINANCE        :  2 employee(s)
  MARKETING      :  2 employee(s)
  HR             :  2 employee(s)
  ENGINEERING    :  4 employee(s)

--------------------------------------------------------------------------------
4. SORTED DEPARTMENT REPORT (Using TreeMap<String, DepartmentSummary>)
--------------------------------------------------------------------------------

=== Department: ENGINEERING ===
  Headcount: 4 | Average Salary: $113,750.00 | Total Payroll: $455,000.00
  Members:
    - [ID: 0101] Alice Smith      ($110,000.00)
    - [ID: 0102] Bob Vance        ($125,000.00)
    - [ID: 0103] Charlie Brown    ($95,000.00)
    - [ID: 0110] Jack Reacher     ($125,000.00)

=== Department: FINANCE ===
  Headcount: 2 | Average Salary: $92,000.00 | Total Payroll: $184,000.00
  Members:
    - [ID: 0107] George Clark     ($92,000.00)
    - [ID: 0108] Hannah Abbott    ($92,000.00)

=== Department: HR ===
  Headcount: 2 | Average Salary: $84,000.00 | Total Payroll: $168,000.00
  Members:
    - [ID: 0104] Diana Prince     ($84,000.00)
    - [ID: 0106] Fiona Gallagher  ($84,000.00)

=== Department: MARKETING ===
  Headcount: 2 | Average Salary: $83,000.00 | Total Payroll: $166,000.00
  Members:
    - [ID: 0105] Edward Cullen    ($78,000.00)
    - [ID: 0109] Ian Malcolm      ($88,000.00)

--------------------------------------------------------------------------------
5. EMPLOYEES SORTED BY NATURAL ORDERING (Comparable: Ascending Employee ID)
--------------------------------------------------------------------------------
  [ID: 0101] Alice Smith        | Dept: Engineering    | Salary: $110,000.00
  [ID: 0102] Bob Vance          | Dept: Engineering    | Salary: $125,000.00
  [ID: 0103] Charlie Brown      | Dept: Engineering    | Salary: $ 95,000.00
  [ID: 0104] Diana Prince       | Dept: HR             | Salary: $ 84,000.00
  [ID: 0105] Edward Cullen      | Dept: Marketing      | Salary: $ 78,000.00
  [ID: 0106] Fiona Gallagher    | Dept: HR             | Salary: $ 84,000.00
  [ID: 0107] George Clark       | Dept: Finance        | Salary: $ 92,000.00
  [ID: 0108] Hannah Abbott      | Dept: Finance        | Salary: $ 92,000.00
  [ID: 0109] Ian Malcolm        | Dept: Marketing      | Salary: $ 88,000.00
  [ID: 0110] Jack Reacher       | Dept: Engineering    | Salary: $125,000.00

--------------------------------------------------------------------------------
6. TOP FIVE SALARIES (Comparator Chaining: Salary Descending, ID Ascending)
--------------------------------------------------------------------------------
Top 5 Compensated Employees (with ID tie-breaker for identical salaries):
  #1. [ID: 0102] Bob Vance          | Dept: Engineering    | Salary: $125,000.00
  #2. [ID: 0110] Jack Reacher       | Dept: Engineering    | Salary: $125,000.00
  #3. [ID: 0101] Alice Smith        | Dept: Engineering    | Salary: $110,000.00
  #4. [ID: 0103] Charlie Brown      | Dept: Engineering    | Salary: $ 95,000.00
  #5. [ID: 0107] George Clark       | Dept: Finance        | Salary: $ 92,000.00

--------------------------------------------------------------------------------
7. MISSING-hashCode() BUG & RESOLUTION
--------------------------------------------------------------------------------
PART A: BROKEN IMPLEMENTATION (equals() without hashCode())
Step 1: Created two separate objects with the same ID (101):
  Object 1: BrokenEmployee[ID=101, name='Alice', identityHash=723074861]
  Object 2: BrokenEmployee[ID=101, name='Alice', identityHash=895328852]

Step 2: Checking logical equality with equals():
  b1.equals(b2) -> true [OBJECTS ARE LOGICALLY EQUAL]

Step 3: Inspecting observed hash codes:
  b1.hashCode() -> 723074861
  b2.hashCode() -> 895328852
  Hash codes match? -> false
  [CONTRACT VIOLATION OBSERVED]: b1.equals(b2) is TRUE, but their hashCodes DIFFER!

Step 4: Adding b1 to a HashSet<BrokenEmployee>:
  brokenSet.add(b1) -> true. Set size: 1

Step 5: Querying HashSet for b2 using brokenSet.contains(b2):
  brokenSet.contains(b2) -> false
  [BUG REPRODUCED]: The HashSet CANNOT find 'b2' because it computed b2's hash code,
                    inspected the wrong hash bucket, and never even called equals()!

Step 6: Attempting to add b2 to the same HashSet:
  brokenSet.add(b2) -> true. Set size now: 2
  [BUG REPRODUCED]: Duplicate 'equal' employees now coexist in a Set!
                    HashSet's contract of uniqueness is completely broken.

PART B: CORRECT IMPLEMENTATION (Employee with consistent equals() & hashCode())
Step 1: Created two separate Employee objects with the same ID (101):
  Employee 1: [ID: 0101] Alice              | Dept: Engineering    | Salary: $ 85,000.00
  Employee 2: [ID: 0101] Alice              | Dept: Engineering    | Salary: $ 85,000.00

Step 2: Checking logical equality with equals():
  e1.equals(e2) -> true [LOGICALLY EQUAL]

Step 3: Inspecting observed hash codes:
  e1.hashCode() -> 101
  e2.hashCode() -> 101
  e1.hashCode() == e2.hashCode() -> true [CONSISTENT CONTRACT]

Step 4: Adding e1 to a HashSet<Employee>:
  fixedSet.add(e1) -> true. Set size: 1

Step 5: Querying HashSet for e2 using fixedSet.contains(e2):
  fixedSet.contains(e2) -> true [FOUND SUCCESSFULLY]

Step 6: Attempting to add e2 to the HashSet:
  fixedSet.add(e2) -> false. Set size remains: 1 [DUPLICATE PREVENTED]

================================================================================
                    ALL DAY 8 DEMONSTRATIONS COMPLETED                          
================================================================================
```

---

## 6. Compilation, Execution & Testing Commands

Navigate to `Day-08/employee-directory`:

### Option 1: Direct JDK Toolchain (`javac` and `java`)
```bash
# Compile all source classes into out/
javac -d out src/com/technova/employeedirectory/model/*.java src/com/technova/employeedirectory/service/*.java src/com/technova/employeedirectory/demo/*.java src/com/technova/employeedirectory/app/*.java

# Run main employee directory application
java -cp out com.technova.employeedirectory.app.EmployeeDirectoryApplication

# Run standalone hashCode contract bug demo
java -cp out com.technova.employeedirectory.demo.HashCodeBugDemo
```

### Option 2: Windows Batch Runner (`run.bat`)
```cmd
# Run main demo
run.bat

# Run hashCode bug demo
run.bat --demo

# Run both suites sequentially
run.bat --all
```

### Option 3: Apache Maven
```bash
# Run 12 JUnit tests
mvn clean test

# Execute main application
mvn compile exec:java
```

---

## 7. Test Suite Verification

Unit tests in `test/com/technova/employeedirectory/EmployeeDirectoryServiceTest.java`:

| # | Test Method | Scenario Verified | Expected Outcome | Actual Result |
|:---:|---|---|---|:---:|
| 1 | `testEmployeeIdMustBePositive` | Rejects ID $\le 0$ | Throws `IllegalArgumentException` | **PASS** |
| 2 | `testBlankFieldsAndNegativeSalaryRejected` | Name/dept blank, salary negative/null | Throws `IllegalArgumentException` | **PASS** |
| 3 | `testDuplicateEmployeeIdRejected` | Attempting to add duplicate ID | Throws `IllegalArgumentException`, size remains 1 | **PASS** |
| 4 | `testLookupValidIdReturnsEmployee` | Lookup existing ID 205 | Returns present Optional with correct employee | **PASS** |
| 5 | `testLookupMissingIdReturnsEmptyOptional` | Lookup nonexistent ID 9999 | Returns `Optional.empty()` without crashing | **PASS** |
| 6 | `testDepartmentFrequencyCounts` | Case-insensitive frequency counts | Normalizes case, produces exact counts | **PASS** |
| 7 | `testDepartmentReportSortedAlphabetically` | `TreeMap` department ordering | Keys sorted in alphabetical order | **PASS** |
| 8 | `testNaturalOrderingByAscendingId` | Natural sorting via `Comparable` | Employees ordered by ascending ID ($100 \to 200 \to 300$) | **PASS** |
| 9 | `testTopFiveSalariesAndTieBreaking` | Salary descending, ID tie-breaker | High salaries first; ID 102 precedes ID 105 on $120k tie | **PASS** |
| 10 | `testEmptyDirectoryHandling` | Reports on empty directory | Produces empty collections without throwing exceptions | **PASS** |
| 11 | `testEqualsAndHashCodeAgreement` | Contract verification | Equal objects have equal hash codes | **PASS** |
| 12 | `testHashSetDuplicatePrevention` | Duplicate prevention in `HashSet` | Rejects duplicate object, maintains size 1 | **PASS** |

**Surefire Execution Result**:
```text
[INFO] Running com.technova.employeedirectory.EmployeeDirectoryServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.196 s -- in com.technova.employeedirectory.EmployeeDirectoryServiceTest
[INFO] BUILD SUCCESS
```

---

## 8. Completion Summary

- **Repository Rules**: Preserved all previous daily tasks (Days 1–7) and `BookMg/`.
- **Collections Utilized**: `HashMap`, `TreeMap`, `Map.merge()`, `Comparable`, and chained `Comparator`.
- **Contract Bug Solved**: Technical explanation and reproducible demonstration of `equals()` and `hashCode()`.
- **Tests**: 12/12 passing JUnit 5 tests.
- **Git Commit**: `day-8: employee directory and hashcode contract`.
