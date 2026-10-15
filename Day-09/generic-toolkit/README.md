# Day 9 — Generic Toolkit: Generics, File I/O & Resource Management

Comprehensive assessment documentation for **Day 9** of the HCL Core Java Training program. This project implements a production-grade **Generic Toolkit** demonstrating compile-time type safety via Java generics, upper-bounded type parameters, the PECS principle with wildcards, robust CSV parsing using try-with-resources, and deterministic resource lifecycle management using `AutoCloseable` with reverse-order closing and suppressed exception handling.

---

## 1. Project Objectives & Learning Outcomes

1. **Compile-Time Type Safety with Generic `Pair<K, V>`**:
   - Implements a generic two-tuple storing key `K` and value `V`.
   - Eliminates dangerous manual casts (e.g. `(String) pair.getKey()`) and replaces runtime `ClassCastException` hazards with compile-time verification.
   - Prohibits raw types in client code.

2. **Bounded Type Parameters with `Box<T extends Number>`**:
   - Restricts generic type parameters using the upper bound `<T extends Number>`.
   - Enables direct access to numeric operations (`doubleValue()`, `intValue()`, `longValue()`) on generic values without casting.
   - Enforces explicit null safety via `isPresent()`, `get()`, and `doubleValueOrDefault()`.
   - Explains why `Box<String>` fails at compile time due to bound violation.

3. **Generic Maximum Method with Bounded Comparable `<T extends Comparable<T>>`**:
   - Provides a universal static algorithm `GenericUtils.max(T first, T second)`.
   - Leverages `first.compareTo(second)` across any comparable type (integers, strings, custom domain entities).
   - Enforces strict null-rejection policy using `Objects.requireNonNull()`.

4. **Wildcard Variance & The PECS Principle**:
   - **Producer Extends (`? extends Number`)**: Implements `sumNumbers(List<? extends Number>)` to safely read numeric values from heterogeneous subtypes (`Integer`, `Double`, `Long`).
   - **Consumer Super (`? super Number`)**: Implements `copyNumbers(List<? extends Number>, List<? super Number>)` to safely copy values into wider consumer collections (`List<Number>`, `List<Object>`).
   - **Unbounded Wildcard (`?`)**: Implements `printItems(List<?>)` for non-invasive, read-only collection inspection.

5. **UTF-8 CSV Processing with Try-with-Resources**:
   - Encapsulates `Product` domain records (`id`, `name`, `category`, `price`).
   - Reads CSV records using `Files.newBufferedReader(path, StandardCharsets.UTF_8)` within try-with-resources.
   - Enforces strict validation: header compliance, 4-column structure, numeric ID parsing, non-blank strings, and non-negative `BigDecimal` pricing.
   - Translates low-level parsing faults into context-rich `CsvParseException` preserving line numbers and root causes.

6. **Deterministic Resource Management with `AutoCloseable`**:
   - Implements `TrackedResource` to log acquisition, operations, and disposal.
   - Demonstrates guaranteed **reverse declaration (LIFO)** closing order across multi-resource try statements.
   - Proves guaranteed cleanup on exception paths.
   - Demonstrates **Suppressed Exceptions**: preserves the primary in-flight exception while attaching secondary `close()` exceptions via `Throwable.addSuppressed()`.

---

## 2. Project Structure

```text
Daily-Tasks---HCL/
└── Day-09/
    └── generic-toolkit/
        ├── src/
        │   └── com/
        │       └── technova/
        │           └── generics/
        │               ├── model/
        │               │   ├── Pair.java                   # Generic immutable tuple (K, V)
        │               │   ├── Box.java                    # Numeric upper-bounded container (T extends Number)
        │               │   └── Product.java                # Validated domain entity with Comparable<Product>
        │               ├── util/
        │               │   └── GenericUtils.java           # max(), sumNumbers(), copyNumbers(), printItems()
        │               ├── service/
        │               │   ├── CsvParseException.java      # Contextual checked exception with line number
        │               │   └── ProductCsvService.java       # UTF-8 CSV parsing with try-with-resources
        │               ├── resource/
        │               │   └── TrackedResource.java         # AutoCloseable lifecycle & suppression tracking
        │               ├── demo/
        │               │   └── GenericToolkitDemo.java      # Modular educational demonstration runners
        │               └── app/
        │                   └── GenericToolkitApplication.java # Complete CLI application entry point
        ├── test/
        │   └── com/
        │       └── technova/
        │           └── generics/
        │               └── GenericToolkitTest.java          # 12 JUnit 5 test cases
        ├── data/
        │   └── products.csv                                # 10 sample products across 4 categories
        ├── pom.xml                                         # Maven build descriptor (Java 17/21/25 compatible)
        ├── run.bat                                         # Windows batch runner for app and tests
        ├── README.md                                       # Comprehensive Day 9 technical manual
        └── .gitignore                                      # Excludes out/, target/, and *.class
```

> **Repository Convention**: Follows the established repository naming convention (`Day-09/generic-toolkit/`, matching `Day-07/library-waitlist` and `Day-08/employee-directory`).

---

## 3. Core Concepts & Technical Deep Dive

### A. Generic Type Parameters & Type Safety (`Pair<K, V>`)

In pre-generics Java (prior to Java 5), data structures stored `Object`, requiring manual downcasting:
```java
// Pre-generics (Dangerous)
Object key = rawPair.getKey();
String name = (String) key; // Throws ClassCastException at runtime if key was an Integer!
```

With `Pair<K, V>`, the compiler enforces type constraints at compile time via **type erasure**:
```java
Pair<String, Integer> student = new Pair<>("Alice Smith", 98);
String name = student.getKey();  // No cast required
int grade = student.getValue();  // Autounboxed safely
```

Benefits:
- Eliminates manual casts across the codebase.
- Guarantees compile-time type checking.
- Detects type incompatibilities before code reaches testing or production environments.

### B. Bounded Type Parameters (`Box<T extends Number>`)

An unbounded type parameter `<T>` allows *any* type (`Object` and its subclasses). When specialized behaviors are needed, an **upper bound** restricts the allowable type arguments:

```java
public final class Box<T extends Number> {
    private final T value;
    
    public double doubleValue() {
        if (value == null) {
            throw new NoSuchElementException("Cannot retrieve doubleValue from an empty Box");
        }
        return value.doubleValue(); // Available because T inherits java.lang.Number!
    }
}
```

#### Why `Box<String>` Fails Compilation
If client code attempts:
```java
Box<String> stringBox = new Box<>("Hello"); // COMPILE ERROR!
```
The Java compiler rejects this statement with:
```text
Type parameter 'java.lang.String' is not within its bound; should extend 'java.lang.Number'
```
Because `String` does not extend `java.lang.Number`, it cannot be supplied as `T`. This prevents calling numeric operations like `doubleValue()` on non-numeric objects.

### C. Generic Maximum Algorithm (`<T extends Comparable<T>>`)

To determine the maximum of two values of unknown type, the type must support comparison. In Java, this is represented by `Comparable<T>`:

```java
public static <T extends Comparable<T>> T max(T first, T second) {
    Objects.requireNonNull(first, "First argument to max() cannot be null");
    Objects.requireNonNull(second, "Second argument to max() cannot be null");
    return first.compareTo(second) >= 0 ? first : second;
}
```

#### Why `<T extends Comparable<T>>` is Required
1. Without the bound, `first` is treated as `Object`, which has no `compareTo()` method.
2. The recursive bound `<T extends Comparable<T>>` ensures that `first` is comparable specifically against objects of its own type `T`, preventing invalid cross-type comparisons (e.g., comparing an `Integer` with a `String`).

### D. Wildcards & The PECS Principle

In Java generics, generic types are **invariant**. Even though `Integer` is a subtype of `Number`, `List<Integer>` is **not** a subtype of `List<Number>`. To design flexible APIs, Java provides wildcard arguments:

| Wildcard Syntax | Terminology | Role | Capability |
|---|---|---|---|
| `List<? extends Number>` | Covariant (Upper Bounded) | **Producer** | Safe for **reading** numbers (`Number n = list.get(i)`). Cannot write into. |
| `List<? super Number>` | Contravariant (Lower Bounded) | **Consumer** | Safe for **writing** numbers (`list.add(42)`). Reading yields `Object`. |
| `List<?>` | Unbounded | Inspector / Read-only | Safe for reading `Object`. Used when type information is irrelevant. |

#### PECS: Producer Extends, Consumer Super
- **Producer Extends**: When your method only **reads** data from a collection, use `? extends T`. The collection *produces* items for consumption:
  ```java
  public static double sumNumbers(List<? extends Number> numbers) {
      double sum = 0.0;
      for (Number n : numbers) { // Reading is safe: every element is at least a Number
          if (n != null) sum += n.doubleValue();
      }
      return sum;
  }
  ```
  *Attempting to add elements (`numbers.add(10)`) will not compile because the actual underlying list might be `List<Double>`.*

- **Consumer Super**: When your method only **writes** data to a collection, use `? super T`. The collection *consumes* items:
  ```java
  public static void copyNumbers(List<? extends Number> source, List<? super Number> destination) {
      for (Number n : source) {
          destination.add(n); // Safe: destination accepts any Number or its supertypes
      }
  }
  ```

---

## 4. File I/O & Resource Management

### A. CSV Format & Parser Specifications

The `ProductCsvService` parses four-column product records:
```text
id,name,category,price
101,Mechanical Keyboard,Accessories,1499.00
102,UltraWide Monitor,Electronics,12999.00
```

- **Scope & Limitations**: The parser is dependency-free (standard JDK `java.nio.file` and `java.io`). It expects standard comma-delimited columns without embedded newline characters or escaped commas.
- **Validation**:
  1. Header must match `id,name,category,price` (case-insensitive).
  2. Each row must contain exactly 4 non-empty tokens.
  3. `id` must parse to a valid positive integer.
  4. `name` and `category` must not be blank.
  5. `price` must parse to a valid non-negative `BigDecimal`.
- **Contextual Exceptions**: Malformed records trigger `CsvParseException` which records the 1-based line number and file path.

### B. Try-with-Resources & Reverse Closing Order (LIFO)

Java 7 introduced try-with-resources for any object implementing `java.lang.AutoCloseable`. The Java Language Specification guarantees that **resources declared in a try-with-resources statement are closed in reverse order of their declaration** (Last-In, First-Out):

```java
try (TrackedResource a = new TrackedResource("Resource-A");
     TrackedResource b = new TrackedResource("Resource-B");
     TrackedResource c = new TrackedResource("Resource-C")) {
    // operations
}
```

Execution trace:
```text
Opening: Resource-A
Opening: Resource-B
Opening: Resource-C
>> Inside try block
Closing: Resource-C   <-- Closed FIRST
Closing: Resource-B   <-- Closed SECOND
Closing: Resource-A   <-- Closed LAST
```

**Architectural Rationale**: Dependent outer resources (e.g., buffered streams, network sockets) are constructed after their foundational inner resources (e.g., file descriptors, database connections). Closing in reverse order ensures outer wrappers flush and cleanly close before their underlying dependencies are torn down.

### C. Exception-Path Cleanup & Suppressed Exceptions

#### The Legacy `finally` Flaw (Pre-Java 7)
In legacy Java, resource cleanup was performed in `finally` blocks:
```java
// Pre-Java 7 anti-pattern
Resource res = null;
try {
    res = openResource();
    throw new BusinessException("Primary failure");
} finally {
    if (res != null) {
        res.close(); // If close() throws, BusinessException is LOST!
    }
}
```
If `res.close()` threw an `IOException`, it **suppressed and wiped out** the original `BusinessException`, masking the true root cause from diagnostic logs.

#### Modern Resolution: Suppressed Exceptions
Under try-with-resources:
1. The exception thrown from the `try` block is treated as the **primary exception**.
2. Any exception thrown during the automatic invocation of `close()` is caught by the runtime and attached to the primary exception via `Throwable.addSuppressed(Throwable exception)`.
3. The catch block receives the primary exception, and diagnostic tools can inspect all secondary failures via `Throwable.getSuppressed()`.

```java
try (TrackedResource res = new TrackedResource("FaultyResource", true)) {
    throw new RuntimeException("Primary business failure");
} catch (Exception ex) {
    System.out.println("Primary: " + ex.getMessage());
    for (Throwable suppressed : ex.getSuppressed()) {
        System.out.println("Suppressed: " + suppressed.getMessage());
    }
}
```

---

## 5. Verification & Testing

### Test Suite (`GenericToolkitTest.java`)
The project includes 12 JUnit 5 unit tests verifying all core requirements:

| Test Method | Description |
|---|---|
| `testPairTypeSafetyAndValues` | Verifies `Pair<K,V>` stores types without casting. |
| `testBoxNumericTypes` | Verifies `Box` works for `Integer`, `Double`, `Long`, and computes `doubleValue()`. |
| `testBoxEmptyAndNullHandling` | Verifies null and empty handling in `Box`. |
| `testGenericMax` | Verifies `max()` for numbers, strings, and custom `Product` entities. |
| `testGenericMaxNullRejection` | Verifies null inputs are rejected with `NullPointerException`. |
| `testSumNumbersWildcard` | Verifies `sumNumbers(List<? extends Number>)` for multiple subtypes. |
| `testCopyNumbersPecs` | Verifies `copyNumbers()` copies to `List<Number>` and `List<Object>`. |
| `testPrintItems` | Verifies unbounded wildcard display for diverse list types. |
| `testValidCsvParsing` | Verifies valid parsing of 10 products from `data/products.csv`. |
| `testCsvMalformedLineRejection` | Verifies `CsvParseException` is thrown with correct line number on malformed lines. |
| `testCsvMissingFileHandling` | Verifies `FileNotFoundException` is caught and reported when file is absent. |
| `testAutoCloseableReverseOrderAndSuppression` | Verifies LIFO closing order and suppressed exception attachment. |

### Test Execution Output (`mvn test`)
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.technova.generics.GenericToolkitTest
  Opening: Res1
  Opening: Res2
  Closing: Res2
  Closing: Res1
  Opening: Faulty
  Closing: Faulty
  Displaying 3 item(s):
    [0] (String) A
    [1] (String) B
    [2] (String) C
  Displaying 3 item(s):
    [0] (Integer) 1
    [1] (Integer) 2
    [2] (Integer) 3
  Displaying 1 item(s):
    [0] (Product) [ID: 001] Item               | Cat: Cat          | Price: $    10.00
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.900 s -- in com.technova.generics.GenericToolkitTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. How to Build & Run

### Environment Requirements
- **JDK**: Java 17 or higher (tested on OpenJDK 25 / Java 17).
- **Build Tool**: Maven 3.8+ (optional; plain `javac` supported).
- **OS**: Windows, Linux, or macOS.

### Option 1: Using Maven (Recommended)
Navigate to `Day-09/generic-toolkit/`:
```bash
# Run test suite
mvn test

# Run application
mvn compile exec:java -Dexec.mainClass="com.technova.generics.app.GenericToolkitApplication"
```

### Option 2: Using the Windows Batch Runner (`run.bat`)
From `Day-09/generic-toolkit/`:
```cmd
# Run tests
run.bat --test

# Run demonstration application
run.bat --app

# Clean build artifacts
run.bat --clean
```

### Option 3: Plain JDK (`javac` / `java`)
#### Windows PowerShell:
```powershell
# From Day-09/generic-toolkit:
New-Item -ItemType Directory -Force -Path out
$sources = Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName
javac -d out $sources
java -cp out com.technova.generics.app.GenericToolkitApplication
```

#### Linux / macOS / Bash:
```bash
# From Day-09/generic-toolkit:
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.technova.generics.app.GenericToolkitApplication
```

---

## 7. Sample Real Console Execution Output

```text
================================================================================
   TECHNOVA GENERIC TOOLKIT: GENERICS, FILE I/O & RESOURCE MANAGEMENT (DAY 9)   
================================================================================

--------------------------------------------------------------------------------
1. GENERIC Pair<K, V> DEMONSTRATION (Compile-time Type Safety)
--------------------------------------------------------------------------------
  Pair<String, Integer>: Pair(Alice Smith -> 98)
    Extracted key: 'Alice Smith' (Length: 11)
    Extracted value: 98 (Primitive int, no casting needed)
  Pair<Integer, String>: Pair(404 -> Not Found)
  Pair<String, Product>: Pair(DEAL_OF_THE_DAY -> [ID: 101] Dell XPS 15        | Cat: Electronics  | Price: $ 1,899.99)
    Featured Product Name: 'Dell XPS 15' | Price: $1,899.99

Why Generics?
  - Generics eliminate the need for dangerous runtime casts like '(String) pair.getKey()'.
  - Type mismatches are caught at COMPILE TIME rather than throwing ClassCastException at runtime.

--------------------------------------------------------------------------------
2. BOUNDED Box<T extends Number> DEMONSTRATION
--------------------------------------------------------------------------------
  Box<Integer>: Box[42 (Integer)] -> doubleValue(): 42.0
  Box<Double>:  Box[3.14159 (Double)] -> doubleValue(): 3.14159
  Box<Long>:    Box[100000000000 (Long)] -> doubleValue(): 1.0E11
  Box<Float>:   Box[99.95 (Float)] -> doubleValue(): 99.94999694824219
  Box<Integer> (Empty): Box[EMPTY] -> isPresent(): false
  emptyBox.doubleValueOrDefault(0.0) -> 0.0
  [PASS] Calling doubleValue() on empty Box caught expected error: 'Cannot retrieve doubleValue from an empty Box'

Why Upper Bounds (<T extends Number>)?
  - Restricts Box to types that inherit from java.lang.Number.
  - Guarantees methods like doubleValue(), intValue(), and longValue() exist.
  - Attempting to compile 'Box<String> strBox = new Box<>()' produces a COMPILE ERROR:
    'Type parameter 'java.lang.String' is not within its bound; should extend 'java.lang.Number''.

--------------------------------------------------------------------------------
3. GENERIC max(T, T) WITH <T extends Comparable<T>> DEMONSTRATION
--------------------------------------------------------------------------------
  GenericUtils.max(15, 87)       -> 87
  GenericUtils.max("Apple", "Zebra") -> "Zebra"
  GenericUtils.max(Budget Mouse, 4K Display) -> Curved 4K Display ($899.99)
  [PASS] Null input correctly rejected: 'First argument to max() cannot be null'

Why <T extends Comparable<T>>?
  - It allows one single method implementation to work for any comparable type.
  - Guarantees that 'first.compareTo(second)' can be invoked safely at compile time.

--------------------------------------------------------------------------------
4. WILDCARDS & THE PECS PRINCIPLE (Producer Extends, Consumer Super)
--------------------------------------------------------------------------------
  sumNumbers(List<Integer>) -> 100.00
  sumNumbers(List<Double>)  -> 7.50
  sumNumbers(List<Long>)    -> 600.00

Demonstrating copyNumbers(List<? extends Number>, List<? super Number>):
  Copied List<Integer> into List<Number>:
    Destination content: [10, 20, 30, 40]
  Copied List<Double> into List<Object>:
    Destination content: [1.5, 2.5, 3.5]

Demonstrating printItems(List<?>):
  Displaying 4 item(s):
    [0] (String) Java
    [1] (String) Generics
    [2] (String) PECS
    [3] (String) Wildcards
  Displaying 3 item(s):
    [0] (Integer) 101
    [1] (Integer) 202
    [2] (Integer) 303

--------------------------------------------------------------------------------
5. PRODUCT CSV PARSING WITH TRY-WITH-RESOURCES (UTF-8)
--------------------------------------------------------------------------------
Reading products from: data\products.csv
Successfully loaded 10 products:
  [ID: 101] Mechanical Keyboard | Cat: Accessories  | Price: $ 1,499.00
  [ID: 102] UltraWide Monitor  | Cat: Electronics  | Price: $12,999.00
  [ID: 103] Ergonomic Mouse    | Cat: Accessories  | Price: $   849.50
  [ID: 104] USB-C Hub Multiport | Cat: Electronics  | Price: $ 2,199.00
  [ID: 105] Desk Lamp LED      | Cat: Office       | Price: $   750.00
  [ID: 106] Noise Cancelling Headset | Cat: Audio        | Price: $ 4,999.00
  [ID: 107] Standing Desk Mat  | Cat: Office       | Price: $ 1,250.00
  [ID: 108] Bluetooth Speaker  | Cat: Audio        | Price: $ 1,899.00
  [ID: 109] Webcam 4K Ultra    | Cat: Electronics  | Price: $ 3,499.00
  [ID: 110] Laptop Cooling Stand | Cat: Accessories  | Price: $   999.00

Testing error reporting with non-existent CSV path:
  [PASS] Intercepted expected file error: CSV file not found at path: D:\projects\Daily-Tasks---HCL\Day-09\generic-toolkit\data\non_existent_file.csv

--------------------------------------------------------------------------------
6. AutoCloseable REVERSE DECLARATION CLOSING ORDER
--------------------------------------------------------------------------------
Opening three resources in a single try-with-resources statement:
  Opening: Resource-A (Database)
  Opening: Resource-B (AuditStream)
  Opening: Resource-C (NetworkSocket)
  >> Inside try block: executing business operations
    [Resource-A (Database)] Performing: Execute query
    [Resource-B (AuditStream)] Performing: Write audit record
    [Resource-C (NetworkSocket)] Performing: Send acknowledgement packet
  >> Exiting try block (automatic cleanup commences)
  Closing: Resource-C (NetworkSocket)
  Closing: Resource-B (AuditStream)
  Closing: Resource-A (Database)

Notice Closing Order:
  1. Resource-C was declared LAST  -> Closed FIRST.
  2. Resource-B was declared SECOND -> Closed SECOND.
  3. Resource-A was declared FIRST -> Closed LAST.
  (LIFO / Stack semantics guarantee dependent outer resources close before foundations)

--------------------------------------------------------------------------------
7. EXCEPTION-PATH CLEANUP & SUPPRESSED EXCEPTIONS DEMONSTRATION
--------------------------------------------------------------------------------
Demonstration 7A: Try block throws an exception. Verifying resources STILL close:
  Opening: DatabasePool
  Opening: FileChannel
  >> Performing operations inside try block...
    [DatabasePool] Performing: Acquire lock
  >> Deliberately throwing RuntimeException inside try block!
  Closing: FileChannel
  Closing: DatabasePool
  [PASS] Caught expected in-flight exception: 'Simulated unexpected business logic failure!'
         (Both resources were guaranteed closed before entering catch block!)

Demonstration 7B: Suppressed Exceptions (Close failure while exception in-flight):
  Opening: FaultyResource
  >> Performing operations with FaultyResource...
  >> Throwing Primary Exception from try block!
  Closing: FaultyResource
  [PRIMARY EXCEPTION CAUGHT]: PRIMARY BUSINESS EXCEPTION from try block
  [SUPPRESSED EXCEPTIONS COUNT]: 1
    - Attached Suppressed Exception: Simulated close failure in resource 'FaultyResource'

Why Suppressed Exceptions Matter:
  - In legacy pre-Java 7 'try-finally', if finally.close() threw an exception,
    it completely ATE / WIPED OUT the original primary business exception!
  - Try-with-resources preserves the original primary exception and attaches close()
    failures as suppressed exceptions via Throwable.addSuppressed().

================================================================================
                    ALL DAY 9 DEMONSTRATIONS COMPLETED                          
================================================================================
```
