# Day 4 — BankAccount OOP and Debugging

## Objective

Implement a robust Java `BankAccount` domain model and service architecture demonstrating fundamental Object-Oriented Programming (OOP) concepts:
- **Private Fields & Encapsulation**: Restricting direct field access and enforcing state mutations through validated methods.
- **Static Counter**: Maintaining a class-level counter (`accountCounter = 1000`) for generating auto-incremented, unique account numbers across all instances.
- **Constructor Chaining**: Chaining three constructors using `this(...)` to avoid duplicate initialization logic.
- **Strict Business Validation**: Enforcing non-null/non-blank account holder names, non-negative initial balances, positive deposit/withdrawal amounts, and preventing account overdrafts.
- **Object Contracts (`equals()`, `hashCode()`, `toString()`)**: Implementing value identity based on the unique `accountNumber`.

And demonstrate advanced Java debugging techniques:
- **Conditional Breakpoints**: Pausing thread execution only when a specific boolean condition evaluates to true (`amount == 1000`).
- **Watches**: Inspecting live state variables (`balance`, `amount`) during step-by-step debugging.
- **Hot Code Replace (HCR)**: Explaining the dynamic JVM bytecode replacement mechanism and providing concrete IDE execution steps.

---

## Package Structure

The project follows a standard multi-tiered package structure:

```text
day-4/
├── src/
│   └── com/
│       └── technova/
│           └── bank/
│               ├── model/
│               │   └── BankAccount.java          # Core domain model with encapsulated state
│               ├── service/
│               │   └── BankAccountService.java   # Business operations & account orchestration
│               └── app/
│                   └── BankApplication.java      # Application entrypoint & demonstration suite
├── README.md
└── .gitignore
```

- **`com.technova.bank.model`**: Contains [`BankAccount.java`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/model/BankAccount.java), the entity responsible for state preservation, invariants, and equality semantics.
- **`com.technova.bank.service`**: Contains [`BankAccountService.java`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/service/BankAccountService.java), encapsulating service-level actions (deposits, withdrawals, account details formatting) without bypassing domain validations.
- **`com.technova.bank.app`**: Contains [`BankApplication.java`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/app/BankApplication.java), orchestrating the console demonstration, test scenarios, and exception boundary verifications.

---

## Constructor Chaining

Constructor chaining is achieved using `this(...)` to funnel initialization into a single canonical constructor, preventing repetitive assignment and validation code:

```text
BankAccount()
    │  calls this("Unknown")
    ▼
BankAccount(String accountHolderName)
    │  calls this(accountHolderName, 0.0)
    ▼
BankAccount(String accountHolderName, double initialBalance)
    │
    ├─► Validates accountHolderName (non-null & non-blank)
    ├─► Validates initialBalance (non-negative)
    ├─► Generates unique accountNumber via ++accountCounter
    └─► Sets instance fields
```

### Implementation:
```java
public BankAccount() {
    this("Unknown");
}

public BankAccount(String accountHolderName) {
    this(accountHolderName, 0.0);
}

public BankAccount(String accountHolderName, double initialBalance) {
    if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
        throw new IllegalArgumentException("Invalid account holder name: name cannot be null or blank.");
    }
    if (initialBalance < 0.0) {
        throw new IllegalArgumentException("Initial balance cannot be negative: " + initialBalance);
    }
    this.accountNumber = ++accountCounter;
    this.accountHolderName = accountHolderName.trim();
    this.balance = initialBalance;
}
```

---

## Static Counter

```java
private static int accountCounter = 1000;
```

### Why the Counter is Static:
- **Class-Level State**: Non-static fields belong to a single object instance and reinitialize with each `new` instantiation. A `static` field belongs to the `BankAccount` class itself and is shared across all instances loaded in the JVM.
- **Sequential ID Generation**: By incrementing `++accountCounter` inside the canonical constructor, each account is guaranteed a unique, sequential number starting at 1001 (`1001`, `1002`, `1003`, ...).
- **Encapsulated Generator**: The counter is `private` and accessed only via `public static int getAccountCounter()`, protecting the counter from external manipulation.

---

## Validation Rules

All state mutations enforce explicit validation boundaries, throwing `IllegalArgumentException` on invalid inputs:

| Operation | Boundary Condition | Error Message |
|---|---|---|
| **Account Creation** | `accountHolderName == null` or whitespace | `"Invalid account holder name: name cannot be null or blank."` |
| **Account Creation** | `initialBalance < 0.0` | `"Initial balance cannot be negative: <amount>"` |
| **Deposit** | `amount <= 0.0` | `"Deposit amount must be greater than zero: <amount>"` |
| **Withdrawal** | `amount <= 0.0` | `"Withdrawal amount must be greater than zero: <amount>"` |
| **Withdrawal** | `amount > balance` | `"Insufficient balance. Current balance: <balance>, Requested: <amount>"` |

---

## Object Identity (`equals` & `hashCode`)

In enterprise banking domains, account identity is dictated by the unique account number, not mutable attributes like the account holder name or current balance:

- **`equals(Object obj)`**: Two accounts are identical if and only if their `accountNumber` values match.
  ```java
  @Override
  public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      BankAccount that = (BankAccount) obj;
      return this.accountNumber == that.accountNumber;
  }
  ```
- **`hashCode()`**: Computes the hash directly using `Integer.hashCode(accountNumber)`, honoring the Java contract where `a.equals(b) => a.hashCode() == b.hashCode()`.

---

## Debugging Workflow

### 1. The Intentionally Planted Bug
During development, a logical defect was introduced into `withdraw()` causing double deductions:
```java
// INTENTIONAL BUG:
this.balance -= amount;
this.balance -= amount; // Duplicate line erroneously executed
```

### 2. Reproduction Scenario
- **Initial Account Balance**: `$5000.00`
- **Withdrawal Amount**: `$1000.00`
- **Expected Result**: `$5000.00 - $1000.00 = $4000.00`
- **Buggy Output Observed**:
  ```text
  Triggering withdraw($1000.00)...
  Expected Balance : $4000.00
  Actual Balance   : $3000.00
  Status: [BUG DETECTED] - withdraw() executed double subtraction!
  ```

### 3. Conditional Breakpoint
- **Breakpoint Location**: Inside `BankAccount.java` on the line `this.balance -= amount;`.
- **Condition**:
  ```java
  amount == 1000.0
  ```
- **Rationale**: If the application executes thousands of withdrawals, an unconditional breakpoint interrupts every transaction. A conditional breakpoint pauses the JVM thread *only* when the target reproduction value (`amount == 1000`) is processed.

### 4. Variables Watched
In the IDE debugger **Watches** panel:
- `balance`: Observed at `5000.00` before deduction, transitioning to `4000.00`, then unexpectedly to `3000.00`.
- `amount`: Confirmed constant at `1000.00`.

### 5. Root Cause & Fix
The second `this.balance -= amount;` statement was identified as redundant and deleted:
```java
// FIXED CODE:
this.balance -= amount;
```

### 6. Final Verified Output
```text
Triggering withdraw($1000.00)...
Expected Balance : $4000.00
Actual Balance   : $4000.00
Status: [PASS] - Correct withdrawal logic verified!
```

---

## Hot Code Replace (HCR)

> **Environment Note:** Hot Code Replace requires an active IDE debugger session attached to a JVM via the Java Debug Wire Protocol (JDWP). The source was fixed and the exact manual debugger steps are provided below for demonstration.

### Step-by-Step Hot Code Replace Instructions:

#### In IntelliJ IDEA:
1. Place a conditional breakpoint on line 85 of [`BankAccount.java`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/model/BankAccount.java) with condition `amount == 1000.0`.
2. Add `balance` and `amount` to the **Watches** window.
3. Run [`BankApplication`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/app/BankApplication.java) via **Debug** (`Shift + F9`).
4. When the debugger halts at the breakpoint, step over line 85 (`F8`) and observe the duplicate subtraction.
5. In the editor, delete the duplicate `this.balance -= amount;` line.
6. Click **Run > Debugging Actions > Reload Changed Classes** (or `Ctrl + Shift + F9` to recompile the file).
7. IntelliJ displays: *"1 class reloaded"*.
8. Resume program execution (`F9`). The remaining tests execute using the newly replaced bytecode without restarting the application!

#### In VS Code:
1. Open [`BankAccount.java`](file:///d:/projects/Daily-Tasks---HCL/day-4/src/com/technova/bank/model/BankAccount.java) and set a breakpoint on the `withdraw` deduction line.
2. Right-click the red breakpoint dot and enter Expression: `amount == 1000`.
3. Press `F5` to start debugging.
4. When halted, open the **Run & Debug** side panel and add `balance` to **Watch**.
5. Remove the duplicate subtraction line in the editor and save (`Ctrl + S`).
6. VS Code's Java extension automatically triggers Hot Code Replace.
7. Click the **Continue** button (`F5`) to verify the fixed output.

---

## Compilation & Execution

### Compile:
```bash
cd day-4
javac -d out src/com/technova/bank/model/BankAccount.java src/com/technova/bank/service/BankAccountService.java src/com/technova/bank/app/BankApplication.java
```

### Run:
```bash
java -cp out com.technova.bank.app.BankApplication
```

### Verified Console Output:
```text
==================================================
       TECHNOVA BANK ACCOUNT SYSTEM — DAY 4       
==================================================

----- 1. CREATING ACCOUNTS (CONSTRUCTOR CHAINING) -----
Account 1 (Default constructor):
  Account Number: 1001
  Account Holder: Unknown
  Current Balance: $0.00

Account 2 (1-argument constructor):
  Account Number: 1002
  Account Holder: Karthik
  Current Balance: $0.00

Account 3 (2-argument constructor with $5000.00 initial balance):
  Account Number: 1003
  Account Holder: Arun
  Current Balance: $5000.00

Total Accounts Created (Static Counter): 1003

----- 2. DEPOSIT WORKFLOW -----
Initial Balance for Account 3 (Arun): $5000.00
Depositing $1000.00...
Updated Balance: $6000.00 (Expected: $6000.00)

----- 3. WITHDRAWAL WORKFLOW & DEBUGGING TARGET -----
Current Balance before withdrawal: $6000.00
Withdrawing $2000.00...
Balance after $2000.00 withdrawal: $4000.00

----- DEBUGGING REPRODUCTION TEST CASE -----
Account created: Test Subject, Initial Balance: $5000.00
Triggering withdraw($1000.00)...
Expected Balance : $4000.00
Actual Balance   : $4000.00
Status: [PASS] - Correct withdrawal logic verified!

----- 4. EXCEPTION & VALIDATION BOUNDARIES -----
Testing blank account name: [Caught Expected Exception] Invalid account holder name: name cannot be null or blank.
Testing negative initial balance: [Caught Expected Exception] Initial balance cannot be negative: -250.0
Testing zero deposit: [Caught Expected Exception] Deposit amount must be greater than zero: 0.0
Testing negative deposit: [Caught Expected Exception] Deposit amount must be greater than zero: -500.0
Testing negative withdrawal: [Caught Expected Exception] Withdrawal amount must be greater than zero: -100.0
Testing overdraft withdrawal of $15000.00 (Current Balance: $4000.00): [Caught Expected Exception] Insufficient balance. Current balance: 4000.00, Requested withdrawal: 15000.00

----- 5. OBJECT IDENTITY (EQUALS / HASHCODE / TOSTRING) -----
Account 1: BankAccount[accountNumber=1001, holder='Unknown', balance=0.00]
Account 2: BankAccount[accountNumber=1002, holder='Karthik', balance=0.00]
Account 3: BankAccount[accountNumber=1003, holder='Arun', balance=4000.00]

Account 1 equals Account 2: false
Account 2 equals Account 2 (reflexive): true
Account 1 equals null: false

Account 1 hashCode: 1001
Account 2 hashCode: 1002
Account 3 hashCode: 1003

==================================================
         ALL TESTS COMPLETED SUCCESSFULLY         
==================================================
```
