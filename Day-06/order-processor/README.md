# Day 6 — Order Processor: Exception Handling & Copilot Stack Trace Review

Comprehensive assessment documentation for **Day 6** of the HCL Core Java Training program. This project implements a robust, console-based **Order Processor** adhering strictly to enterprise Java exception handling best practices, exception chaining, multi-catch alternatives, guaranteed audit logging via `finally`, and IDE diagnostic workflows with GitHub Copilot Chat.

---

## 1. Project Objectives

1. **Custom Checked Exception (`InsufficientStockException`)**:
   - Extends `java.lang.Exception`.
   - Represents a recoverable business condition where requested items exceed warehouse stock.
   - Enforces explicit compiler handling (`try-catch` or `throws`).
   - Carries structured diagnostics: requested quantity, available stock, and product name.

2. **Custom Unchecked Exception (`InvalidQuantityException`)**:
   - Extends `java.lang.RuntimeException`.
   - Represents a programming or input contract violation where quantity $\le 0$.
   - Validated eagerly before any stock checks or state mutations occur.

3. **Exception Chaining (`OrderProcessingException`)**:
   - Wraps lower-level inventory exceptions at a high-level service boundary.
   - Preserves the causal chain using `new OrderProcessingException(message, cause)`.
   - Demonstrates inspection through `getCause()` and root cause diagnostics.

4. **Multi-Catch Handling**:
   - Employs Java 7+ multi-catch syntax (`catch (TypeA | TypeB ex)`) for disjoint runtime exceptions (`IllegalArgumentException | IllegalStateException`).
   - Explains the Java language restriction disallowing subclass-related types in multi-catch alternatives.

5. **Guaranteed Execution via `finally` Block**:
   - Guarantees an audit log entry is written for every single order attempt (`SUCCESS` or `FAILED`).
   - Employs defensive I/O: ensures parent directories exist, closes resources safely, avoids returning from `finally`, and never allows logging failures to mask in-flight business exceptions.

6. **Reproducible Stack Traces & Copilot Chat Review**:
   - Generates two genuine, non-fabricated Java stack traces (Unchecked Scenario 1 and Chained Scenario 2).
   - Prepares reproducible CLI flags and structured review templates for GitHub Copilot Chat in `evidence/copilot-review.md`.

---

## 2. Project Structure

```text
Daily-Tasks---HCL/
└── Day-06/
    └── order-processor/
        ├── src/
        │   └── com/
        │       └── technova/
        │           └── orders/
        │               ├── exception/
        │               │   ├── InsufficientStockException.java # Checked business exception
        │               │   ├── InvalidQuantityException.java   # Unchecked validation exception
        │               │   └── OrderProcessingException.java   # Checked wrapper exception (Chaining)
        │               ├── model/
        │               │   └── Order.java                      # Order entity with auto-ID
        │               ├── service/
        │               │   └── OrderProcessor.java             # Core validation, stock & audit logic
        │               └── app/
        │                   └── OrderApplication.java           # Demo suite & standalone trace runners
        ├── test/
        │   └── com/
        │       └── technova/
        │           └── orders/
        │               └── OrderProcessorTest.java             # 9 JUnit 5 unit tests
        ├── audit/
        │   ├── .gitkeep                                        # Preserves folder in Git
        │   └── order-audit.log                                 # Generated at runtime (git-ignored)
        ├── evidence/
        │   ├── README.md                                       # Reproduction and Copilot instructions
        │   └── copilot-review.md                               # Real traces, prompts & verification
        ├── pom.xml                                             # Maven build (JUnit 5 & Exec plugin)
        ├── run.bat                                             # One-click Windows runner
        ├── README.md                                           # Project documentation
        └── .gitignore                                          # Excludes out/, target/, and audit/*.log
```

---

## 3. Exception Architecture & Core Concepts

### A. Checked vs. Unchecked Exceptions in Java

| Dimension | Checked Exceptions (`Exception`) | Unchecked Exceptions (`RuntimeException`) |
|---|---|---|
| **Base Class** | Direct subclasses of `java.lang.Exception` (excluding `RuntimeException`). | Direct subclasses of `java.lang.RuntimeException`. |
| **Compiler Enforcement** | **Mandatory**. The compiler enforces the "Catch or Declare" rule (`try-catch` or `throws`). | **Optional**. The compiler does not enforce catching or declaring. |
| **Semantic Meaning** | Anticipated, recoverable contingency conditions external to program logic (e.g., file not found, inventory shortage, network timeout). | Contract violations, programming defects, or unrecoverable parameter errors (e.g., `NullPointerException`, invalid index, negative quantity). |
| **Implementation** | `InsufficientStockException extends Exception` | `InvalidQuantityException extends RuntimeException` |
| **When to Use** | When the caller can reasonably be expected to recover (e.g., notify customer, prompt for alternate quantity, backorder). | When an illegal argument is passed that violates the method precondition (`quantity <= 0`). |

### B. Custom Checked Exception: `InsufficientStockException`
```java
public class InsufficientStockException extends Exception {
    private final String product;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(String product, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product '%s': requested %d item(s), but only %d in stock.",
                product, requestedQuantity, availableStock));
        this.product = product;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }
    // Constructor with cause also provided
}
```
- Forces client code to address inventory shortages explicitly.
- Provides getters (`getRequestedQuantity()`, `getAvailableStock()`) so callers can inspect exact quantities without parsing string messages.

### C. Custom Unchecked Exception: `InvalidQuantityException`
```java
public class InvalidQuantityException extends RuntimeException {
    private final int invalidQuantity;

    public InvalidQuantityException(int invalidQuantity) {
        super(String.format("Invalid order quantity: %d. Requested quantity must be strictly greater than zero (> 0).", invalidQuantity));
        this.invalidQuantity = invalidQuantity;
    }
}
```
- Eagerly validates preconditions at the very start of `processOrder()`.
- Does not clutter client signatures with mandatory `throws` declarations when passing bad arguments.

### D. Exception Chaining & `getCause()`
In enterprise architectures, lower-level exceptions (e.g., database SQL errors or warehouse inventory failures) should often be wrapped in higher-level domain exceptions (e.g., `OrderProcessingException`) before reaching application boundaries or presentation tiers.

**Why Chaining is Essential**:
1. **Abstraction**: Prevents exposing low-level internal subsystem details to high-level callers.
2. **Preservation**: Standard Java exception chaining preserves the root cause via `Throwable.initCause()` or constructor `super(message, cause)`.
3. **Diagnostics**: `ex.getCause()` and `ex.printStackTrace()` retain the full causal hierarchy, including the line numbers where the original exception arose.

```java
// Boundary in OrderProcessor.java:
try {
    return processOrder(product, quantity);
} catch (InsufficientStockException ex) {
    String msg = String.format("Checkout workflow failed for item '%s' due to warehouse inventory shortage.", product);
    throw new OrderProcessingException(msg, ex); // Original cause preserved!
}
```

### E. Multi-Catch Syntax & Java Language Restrictions
Java 7 introduced multi-catch syntax to eliminate redundant duplicate catch blocks:
```java
try {
    processOrder(product, quantity);
} catch (IllegalArgumentException | IllegalStateException ex) {
    System.err.printf("[MULTI-CATCH] Validation / state error intercepted: %s%n", ex.getMessage());
}
```

**Language Restriction (Disjoint Types)**:
Java strictly prohibits combining alternative exception types in a multi-catch block if one is a subclass of another (e.g., `catch (FileNotFoundException | IOException ex)` will trigger compile-time error `Types in multi-catch must be alternatives with no inheritance relationship`). Because `IllegalArgumentException` and `IllegalStateException` are siblings extending `RuntimeException`, they form valid disjoint alternatives.

---

## 4. Guaranteed Execution: `finally` Block & Audit Log

Every single order processing attempt must write an audit record, regardless of whether the transaction succeeds, throws `InvalidQuantityException`, throws `InsufficientStockException`, or encounters an unexpected runtime error.

### Why `finally` is Used
The `finally` block in Java guarantees execution immediately before control leaves the `try` block, whether through normal completion, a thrown exception, or a `break`/`continue` statement.

### Defensive Audit Writing Rules
1. **Never return from `finally`**: Returning inside `finally` discards any in-flight exception that was being propagated.
2. **Never allow `finally` to swallow original errors**: If writing to disk fails with an `IOException`, the error is reported to `System.err` without throwing a new exception that would suppress the customer's `InsufficientStockException`.
3. **Directory Auto-Creation**: `Files.createDirectories(auditLogPath.getParent())` ensures the `audit/` folder exists even on fresh checkouts.
4. **Try-With-Resources inside Finally**: Automatically closes `BufferedWriter` streams.

### Sample Audit Log Output (`audit/order-audit.log`)
```text
[2026-10-12 13:58:35] | Product: Keyboard     | Qty:   2 | Outcome: SUCCESS | Reason: Order placed. Remaining 'Keyboard' stock: 3
[2026-10-12 13:58:35] | Product: Keyboard     | Qty:   0 | Outcome: FAILED  | Reason: Invalid order quantity: 0. Requested quantity must be strictly greater than zero (> 0).
[2026-10-12 13:58:35] | Product: Keyboard     | Qty:  10 | Outcome: FAILED  | Reason: Insufficient stock for product 'Keyboard': requested 10 item(s), but only 3 in stock.
[2026-10-12 13:58:35] | Product: Keyboard     | Qty:   7 | Outcome: FAILED  | Reason: Insufficient stock for product 'Keyboard': requested 7 item(s), but only 3 in stock.
[2026-10-12 13:58:35] | Product: N/A          | Qty:   1 | Outcome: FAILED  | Reason: Product name cannot be null or blank
[2026-10-12 13:58:35] | Product: Keyboard     | Qty:   1 | Outcome: FAILED  | Reason: Store order processing subsystem is currently paused
[2026-10-12 13:56:28] | Product: Keyboard     | Qty:  -3 | Outcome: FAILED  | Reason: Invalid order quantity: -3. Requested quantity must be strictly greater than zero (> 0).
[2026-10-12 13:56:34] | Product: Keyboard     | Qty:  10 | Outcome: FAILED  | Reason: Insufficient stock for product 'Keyboard': requested 10 item(s), but only 5 in stock.
```

---

## 5. Compilation and Execution

All operations can be performed using either standard JDK command-line tools, Maven, or the provided batch script.

### Option 1: Direct JDK Toolchain (`javac` and `java`)
```bash
# Navigate to the project directory
cd Day-06/order-processor

# Compile all source files into out/
javac -d out src/com/technova/orders/exception/*.java src/com/technova/orders/model/*.java src/com/technova/orders/service/*.java src/com/technova/orders/app/*.java

# Run full demonstration
java -cp out com.technova.orders.app.OrderApplication --demo

# Run Scenario 1 (Unchecked Invalid Quantity stack trace)
java -cp out com.technova.orders.app.OrderApplication --scenario1

# Run Scenario 2 (Chained Insufficient Stock stack trace)
java -cp out com.technova.orders.app.OrderApplication --scenario2
```

### Option 2: Windows Batch Runner (`run.bat`)
```cmd
# Run standard demo
run.bat

# Run Scenario 1
run.bat --scenario1

# Run Scenario 2
run.bat --scenario2
```

### Option 3: Apache Maven
```bash
# Run tests
mvn test

# Execute demo via exec-maven-plugin
mvn clean compile exec:java
```

---

## 6. Reproducing Real Stack Traces

### Scenario 1 — Invalid Quantity (`InvalidQuantityException`)
- **Trigger**: Attempt to order non-positive quantity (`-3` items).
- **Command**: `java -cp out com.technova.orders.app.OrderApplication --scenario1`
- **Actual Genuine Output**:
```text
================================================================================
                 SCENARIO 1: UNCHECKED InvalidQuantityException                 
================================================================================
[STACK TRACE GENERATED]:
com.technova.orders.exception.InvalidQuantityException: Invalid order quantity: -3. Requested quantity must be strictly greater than zero (> 0).
	at com.technova.orders.service.OrderProcessor.processOrder(OrderProcessor.java:89)
	at com.technova.orders.app.OrderApplication.runScenario1InvalidQuantity(OrderApplication.java:151)
	at com.technova.orders.app.OrderApplication.main(OrderApplication.java:29)
```

### Scenario 2 — Chained Insufficient Stock (`OrderProcessingException`)
- **Trigger**: Attempt to order 10 items when initial inventory is 5.
- **Command**: `java -cp out com.technova.orders.app.OrderApplication --scenario2`
- **Actual Genuine Output**:
```text
================================================================================
         SCENARIO 2: EXCEPTION CHAINING (OrderProcessingException)             
================================================================================
[STACK TRACE GENERATED]:
com.technova.orders.exception.OrderProcessingException: Checkout workflow failed for item 'Keyboard' due to warehouse inventory shortage.
	at com.technova.orders.service.OrderProcessor.processOrderWithChaining(OrderProcessor.java:141)
	at com.technova.orders.app.OrderApplication.runScenario2ChainedInsufficientStock(OrderApplication.java:171)
	at com.technova.orders.app.OrderApplication.main(OrderApplication.java:30)
Caused by: com.technova.orders.exception.InsufficientStockException: Insufficient stock for product 'Keyboard': requested 10 item(s), but only 5 in stock.
	at com.technova.orders.service.OrderProcessor.processOrder(OrderProcessor.java:96)
	at com.technova.orders.service.OrderProcessor.processOrderWithChaining(OrderProcessor.java:136)
	... 2 more
```

---

## 7. Test Suite & Verification Results

Unit tests are implemented with **JUnit 5** in `test/com/technova/orders/OrderProcessorTest.java`:

| # | Test Method | Scenario Verified | Expected Outcome | Actual Result |
|:---:|---|---|---|:---:|
| 1 | `testSuccessfulOrderReducesStock` | Valid order for 2 keyboards | Stock reduces $5 \to 3$, Order marked `CONFIRMED` | **PASS** |
| 2 | `testZeroQuantityThrowsInvalidQuantityException` | Quantity = 0 | Throws `InvalidQuantityException`, stock unchanged (5) | **PASS** |
| 3 | `testNegativeQuantityThrowsInvalidQuantityException` | Quantity = -4 | Throws `InvalidQuantityException`, stock unchanged (5) | **PASS** |
| 4 | `testExcessQuantityThrowsInsufficientStockException` | Quantity = 10 (available = 5) | Throws `InsufficientStockException`, stock unchanged (5) | **PASS** |
| 5 | `testExceptionChainingPreservesCause` | Chained workflow with shortage | Throws `OrderProcessingException` with `InsufficientStockException` as cause | **PASS** |
| 6 | `testFinallyAuditWrittenOnSuccess` | Audit logging on successful order | `audit/order-audit.log` contains line with `SUCCESS` | **PASS** |
| 7 | `testFinallyAuditWrittenOnInvalidQuantityFailure` | Audit logging on invalid quantity | `audit/order-audit.log` contains line with `FAILED` | **PASS** |
| 8 | `testFinallyAuditWrittenOnInsufficientStockFailure` | Audit logging on inventory shortage | `audit/order-audit.log` contains line with `FAILED` & shortage details | **PASS** |
| 9 | `testMultiCatchSafety` | Multi-catch handling blank name & paused store | Intercepts `IllegalArgumentException` and `IllegalStateException` without terminating | **PASS** |

**Maven Surefire Results**:
```text
[INFO] Running com.technova.orders.OrderProcessorTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.403 s
[INFO] BUILD SUCCESS
```

---

## 8. Manual GitHub Copilot Chat Review

The stack trace review requires interacting with GitHub Copilot Chat in your IDE:

1. Open `evidence/copilot-review.md`.
2. Copy the paste-ready prompt for **Trace 1** and paste it into GitHub Copilot Chat in VS Code / IntelliJ IDEA.
3. Paste Copilot's verbatim response into `evidence/copilot-review.md`.
4. Review Copilot's claims against `OrderProcessor.java` and `OrderApplication.java`, marking each claim as **Correct**, **Partially correct**, or **Incorrect**.
5. Repeat the exact procedure for **Trace 2** (Chained Exception).
6. Save and commit `evidence/copilot-review.md`.

*Note: In accordance with academic honesty and task rules, Copilot responses have not been fabricated; the file is structured with the exact terminal stack traces and awaits manual interaction in your IDE.*

---

## 9. Status & Completion Summary

- **Core Exception Architecture**: 100% Completed & Verified.
- **Checked & Unchecked Custom Exceptions**: 100% Completed & Verified.
- **Exception Chaining**: 100% Completed & Verified (`getCause()` confirmed).
- **Multi-Catch**: 100% Completed & Verified.
- **Audit Logging via Finally**: 100% Completed & Verified (`audit/order-audit.log` actively writing).
- **Genuine Stack Traces**: 100% Captured from actual JVM executions.
- **Copilot Chat Interaction**: Scaffolded and ready for manual student submission in the IDE.
