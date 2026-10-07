# Monthly Usage Analyser

A modular, pure Core Java console application developed for **Day 2 of the HCL Core Java Training Assessment**.

---

## 1. Objective

The **Monthly Usage Analyser** evaluates electricity/utility consumption data across months and multiple households. It demonstrates core language fundamentals—primitive 1-D and 2-D arrays, static final slab constants, arithmetic operations, explicit type casting, ternary operator grading, method modularization, and how integer arithmetic overflows can be resolved using 64-bit `long` integers.

The implementation strictly uses **plain Java fundamentals** without collections, streams, lambdas, databases, or external frameworks.

---

## 2. Concepts Demonstrated

- **Primitive 1-D Arrays (`int[]`)**: Storing and iterating over 12 monthly usage values.
- **2-D Arrays (`int[][]`)**: Multi-dimensional modeling representing 3 households across 12 months.
- **Control Flow & Loops**: Standard indexed `for` loops, enhanced `for-each` loops, and nested loops for matrix processing.
- **Constants (`static final`)**: Meaningful named constants for billing slabs and unit rates.
- **Arithmetic Operators & Casting**: Integer division truncation vs explicit `(double)` casting for decimal precision.
- **Ternary Operator (`? :`)**: Compact conditional grading (`'A'`, `'B'`, `'C'`, `'D'`).
- **Primitive Data Types**: Practical usage and comparison of `char`, `int`, `long`, and `double`.
- **Integer Overflow & Promotion**: 32-bit register overflow prevention via explicit casting to `long` and `long` accumulators.
- **Method Decomposition**: Encapsulating logic into small, single-responsibility methods coordinated by `main()`.

---

## 3. Integer Overflow Explanation

In Java:
- The primitive `int` is a **32-bit signed two's complement integer**.
- Its maximum value is:
  $$\text{Integer.MAX\_VALUE} = 2^{31} - 1 = 2{,}147{,}483{,}647$$
- When evaluating arithmetic such as:
  ```java
  int usage1 = 2_000_000_000;
  int usage2 = 1_500_000_000;
  int incorrectTotal = usage1 + usage2; // Result: -794,967,296
  ```
  The mathematical sum of $3{,}500{,}000{,}000$ exceeds the maximum positive value of `int`. The high-order bit becomes 1, causing a wraparound to a negative number without throwing a runtime error.

### The Fix:
```java
long correctTotal = (long) usage1 + usage2; // Result: 3,500,000,000
```
Casting `usage1` to `long` promotes the entire addition expression into 64-bit signed integer arithmetic. Because `long` has an upper limit of $9{,}223{,}372{,}036{,}854{,}775{,}807$ ($2^{63}-1$), the sum fits accurately without overflow.

Similarly, when accumulating potentially large sums from an array, declare:
```java
long total = 0L;
```
rather than an `int` counter.

---

## 4. Project Structure

```text
Day-02/monthly-usage-analyser/
│
├── pom.xml
├── README.md
├── .gitignore
├── run.bat
│
└── src/
    └── main/
        └── java/
            └── com/
                └── technova/
                    └── usage/
                        └── MonthlyUsageAnalyser.java
```

---

## 5. How to Build & Run

### Prerequisites
- JDK 17 or higher
- Apache Maven 3.8+

### Using Maven
1. Navigate to the project directory:
   ```bash
   cd Day-02/monthly-usage-analyser
   ```

2. Compile the project:
   ```bash
   mvn clean compile
   ```

3. Run the application:
   ```bash
   mvn exec:java
   ```

### Using Windows Batch Runner
You can also run the preconfigured batch script directly:
```cmd
run.bat
```

---

## 6. Sample Console Output

```text
========================================================
             MONTHLY USAGE ANALYSER
========================================================

Monthly Usage
--------------------------------------------------------
January      : 120
February     : 150
March        : 180
April        : 220
May          : 250
June         : 300
July         : 280
August       : 260
September    : 210
October      : 190
November     : 160
December     : 140

========================================================
             MONTHLY ANALYSIS
========================================================
Total Usage       : 2460 units
Integer Average   : 205 units
Decimal Average   : 205.00 units
Maximum Usage     : 300 units
Maximum Month     : June
Minimum Usage     : 120 units
Minimum Month     : January
Usage Slab        : 3
Usage Grade       : B

========================================================
                 HOUSE USAGE
========================================================

House       Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec
House 1     120  150  180  220  250  300  280  260  210  190  160  140
House 2     100  130  160  190  220  240  230  210  180  160  140  120
House 3     200  220  240  260  280  300  320  310  290  270  250  230

========================================================
             THREE HOUSE ANALYSIS
========================================================
House 1 Total     : 2460 units
House 2 Total     : 2080 units
House 3 Total     : 3170 units

Highest Usage House: House 3
Annual Usage       : 3170 units

========================================================
             INTEGER OVERFLOW DEMO
========================================================

Usage 1: 2000000000
Usage 2: 1500000000

Using int:
Incorrect Total: -794967296

Using long:
Correct Total: 3500000000

Why this happens:
  - 32-bit int maximum value = 2,147,483,647 (Integer.MAX_VALUE)
  - Mathematical sum         = 3,500,000,000
  - Since 3,500,000,000 > 2,147,483,647, the 32-bit register wraps to negative.
  - Casting (long) usage1 promotes arithmetic to 64-bit, preserving accuracy.

Demonstrating calculateLargeTotal (long accumulator):
Sum of 3 large items (1B + 1.2B + 1.5B) = 3700000000

========================================================
             ANALYSIS COMPLETE
========================================================
```
