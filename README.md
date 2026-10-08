# Daily Tasks — HCL Training

Repository to track daily learning, assessments, and tasks during the HCL training program.

## Daily Progress Tracker

| Day | Topic / Task | Project / Code | Status |
|:---:|---|---|:---:|
| **Day 01** | Core Java Assessment: Java JDK Command-Line Basics | [Day-01/day1-java-platform-info](./Day-01/day1-java-platform-info) | Completed |
| **Day 02** | Core Java Assessment: Monthly Usage Analyser | [Day-02/monthly-usage-analyser](./Day-02/monthly-usage-analyser) | Completed |
| **Day 03** | Core Java Assessment: ATM Simulator | [Day-03/atm-simulator](./Day-03/atm-simulator) | Completed |
| **Day 04** | Core Java Assessment: Bank Account OOP & Debugging | [Day-04/bank-account-debugging](./Day-04/bank-account-debugging) | Completed |

### Day 04 Overview: Bank Account OOP & Debugging
A comprehensive **Core Java OOP and debugging task** demonstrating encapsulation and IDE diagnostic techniques:
- **Encapsulation & Validation**: Private fields, validated deposits/withdrawals, non-blank name checks, and non-negative initial balances.
- **Constructor Chaining**: Chaining 3 constructors with `this(...)` to eliminate redundant field initialization.
- **Static Counter**: Class-level auto-incrementing ID generator producing sequential unique account numbers starting at 1001.
- **Object Contracts**: `equals()`, `hashCode()`, and `toString()` implementations based on the unique `accountNumber` identity.
- **Debugging & HCR**: Identification of an intentional double-subtraction defect in `withdraw()`, conditional breakpoint (`amount == 1000`), watch expressions (`balance`, `amount`), and Hot Code Replace workflow.

### Day 03 Overview: ATM Simulator
A clean, menu-driven **Java 17 console application** demonstrating control flow and build configuration:
- **Control Flow**: `do-while` menu loop, `switch` statement for action routing, `break` and `continue` statements.
- **PIN Authentication**: Maximum 3 security attempts with early `break` on verification and card blocking.
- **Input Validation**: Safe `Scanner` reading handling invalid characters, negative amounts, and overdrafts without crashing.
- **Enhanced-for Loop**: Processing and displaying mini-statement transaction records.
- **Maven Profiles**: `dev` and `prod` profiles injecting environment properties (`development` vs `production`) into runtime configuration and packaging.

### Day 02 Overview: Monthly Usage Analyser
A pure **Core Java console application** demonstrating array processing, primitive operations, and type safety:
- **1-D & 2-D Arrays**: Single-house 12-month usage and 3-house comparative multi-dimensional array analysis.
- **Constants**: `static final` slab thresholds and billing rate definitions.
- **Arithmetic & Casting**: Integer division truncation vs explicit `(double)` casting for floating-point precision.
- **Manual Min/Max & Aggregations**: Manual iterative loops to identify min/max values and month names without stream shortcuts.
- **Ternary Operator**: Multi-level ternary expression for usage grading (`'A'`, `'B'`, `'C'`, `'D'`).
- **Integer Overflow & Long Promotion**: Demonstration of 32-bit `int` arithmetic wraparound and resolution using 64-bit `long` casting and accumulator.
- **Modular Code**: Small, focused static methods with clean console presentation.

### Day 01 Overview: Java JDK Command-Line Basics & BookMg Backlog Analysis
A fundamental **Java command-line toolchain task** and **Agile requirements breakdown** for the BookMg enterprise booking platform:
- **Runtime Platform Diagnostics**: [`PlatformInfo.java`](./Day-01/day1-java-platform-info/PlatformInfo.java) querying `java.version`, `os.name`, available processors, and max/free heap memory via `System.getProperty()` and `Runtime.getRuntime()`.
- **Direct Compilation & Execution**: `javac PlatformInfo.java` and `java PlatformInfo` without an IDE.
- **Bytecode & Class Loading Inspection**: `javap -c PlatformInfo` disassembling JVM bytecode and `java -verbose:class` verifying dynamic class loading.
- **BookMg Product Backlog Analysis**: 8 Functional Requirements (FR1–FR8) converted into user stories (US-01 to US-08) with Fibonacci story points (36 SP total) and a Definition of Done for the enterprise booking platform, tracked in the dedicated [BookMg repository](https://github.com/karthi2905/bookmg).
