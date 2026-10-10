# Day 5 — Payment Hierarchy, Polymorphism & Git Collaboration

Comprehensive assessment documentation for **Day 5** of the HCL Core Java Training program. This module demonstrates advanced Object-Oriented Programming (OOP) concepts in Java along with real-world, hands-on Git collaborative workflows including genuine merge conflict resolution and branch rebasing.

---

## 1. Task Objectives

1. **Design an Extensible Payment Hierarchy**:
   - Model an abstract base class `Payment` encapsulating essential transaction attributes (`paymentId`, `amount`).
   - Derive concrete payment channels: `CardPayment`, `UPIPayment`, and `CashPayment`.
2. **Contract-Based Design with Interfaces**:
   - Define a `Refundable` interface representing refundable payment channels.
   - Implement `Refundable` in `CardPayment` and `UPIPayment` while intentionally omitting it from `CashPayment`.
   - Implement strict refund boundary rules (positive amount, cumulative refunds never exceeding original transaction amount).
3. **Polymorphism Mastery**:
   - **Compile-Time Polymorphism**: Method overloading via multiple `pay()` signatures in `PaymentService`.
   - **Runtime Polymorphism**: Dynamic method dispatch invoking overridden `processPayment()` implementations via base-type references (`Payment` and `List<Payment>`).
4. **Git Collaboration Exercises**:
   - Create and resolve a **genuine Git merge conflict** on a shared training file (`collaboration-demo.txt`) across two divergent branches (`day5-conflict-student-a` and `day5-conflict-student-b`).
   - Execute a **Git branch rebase** of `day5-rebase-demo` onto `main`, demonstrating how rebase produces a clean, linear commit history.
5. **Zero-Dependency Plain Java Build**:
   - Direct command-line compilation and execution using standard JDK tools without IDE reliance.

---

## 2. Project Structure

```text
daily-task-hcl/
└── day-5/
    ├── src/
    │   └── com/
    │       └── technova/
    │           └── payment/
    │               ├── model/
    │               │   ├── Payment.java         # Abstract base class
    │               │   ├── CardPayment.java     # Extends Payment, implements Refundable
    │               │   ├── UPIPayment.java      # Extends Payment, implements Refundable
    │               │   ├── CashPayment.java     # Extends Payment (non-refundable)
    │               │   └── Refundable.java      # Interface for refund processing
    │               ├── service/
    │               │   └── PaymentService.java  # Overloaded pay() operations
    │               └── app/
    │                   └── PaymentApplication.java # Demonstration & test scenarios
    ├── collaboration-demo.txt                   # Shared Git conflict exercise artifact
    ├── rebase-demo-notes.txt                    # Git rebase exercise artifact
    ├── run.bat                                  # Windows compilation and execution script
    ├── README.md                                # Comprehensive module documentation
    └── .gitignore                               # Ignores out/, *.class, and IDE metadata
```

---

## 3. Object-Oriented Design & Principles

### A. Abstraction & Inheritance (`Payment.java`)
- **Abstraction**: `Payment` is declared `abstract` because a generic, unspecified payment cannot exist on its own in the real world—every actual transaction must be fulfilled through a specific mechanism (card, UPI, cash).
- **Encapsulation**:
  - `paymentId` (`String`) and `amount` (`double`) are declared `private final`.
  - Immutable state is assigned through the constructor with defensive validation:
    - `paymentId` must be non-null and non-blank.
    - `amount` must be strictly greater than zero (`> 0.0`).
  - Read-only access is provided through public getters (`getPaymentId()`, `getAmount()`). Direct mutation is strictly prohibited.
- **Abstract Method**:
  ```java
  public abstract void processPayment();
  ```
  Forces all concrete subclasses to define their specific fulfillment routine.

### B. Concrete Implementations
- **`CardPayment`**:
  - Encapsulates a non-sensitive card label (`cardLabel`, e.g., `"Visa Platinum **4321"`).
  - Implements `Refundable`. Tracks cumulative `refundedAmount`.
  - Overrides `processPayment()` to print card authorization details.
- **`UPIPayment`**:
  - Encapsulates virtual payment address (`upiId`, e.g., `"student@upi"`).
  - Validates VPA format ensuring presence of the `@` delimiter.
  - Implements `Refundable`. Tracks cumulative `refundedAmount`.
  - Overrides `processPayment()` to print instant UPI transfer confirmation.
- **`CashPayment`**:
  - Represents in-person physical cash settlement.
  - Extends `Payment` without unnecessary fields.
  - **Does NOT implement `Refundable`**, enforcing domain rules that physical cash payments cannot be reversed electronically.

### C. Interface Contract (`Refundable.java`)
```java
public interface Refundable {
    void refund(double amount);
    double getRefundedAmount();
    double getRemainingRefundableAmount();
}
```
**Refund Boundary Validation Rules**:
1. Refund amount must be strictly positive (`amount > 0.0`). Non-positive amounts throw `IllegalArgumentException`.
2. Partial refunds are tracked cumulatively (`refundedAmount += amount`).
3. Total refunds cannot exceed the original transaction amount:
   ```java
   if (amount > getRemainingRefundableAmount()) {
       throw new IllegalArgumentException(...);
   }
   ```
4. Repeated partial refunds remain valid as long as the cumulative threshold is not breached.

---

## 4. Method Overloading vs. Method Overriding

| Dimension | Method Overloading (Compile-Time Polymorphism) | Method Overriding (Runtime Polymorphism) |
|---|---|---|
| **Location** | Defined within the **same class** (`PaymentService`) | Defined across **parent & child classes** (`Payment` & subclasses) |
| **Method Signature** | Same method name, **different parameter list** (type, number, order) | **Identical name, parameters, and return type** (or covariant return) |
| **Resolution Time** | Resolved at **compile-time** (static binding by compiler based on argument types) | Resolved at **runtime** (dynamic method dispatch based on actual object instance) |
| **Annotation** | Optional (no annotation exists) | Recommended to annotate with `@Override` |
| **Inheritance Requirement** | No inheritance required | Requires inheritance or interface implementation |
| **Implementation in Day 5** | Three overloaded `pay()` variants in `PaymentService`:<br>1. `pay(Payment)`<br>2. `pay(Payment, String)`<br>3. `pay(Payment, int)` | Subclasses overriding `public void processPayment()` declared abstract in `Payment` |

---

## 5. Runtime Polymorphism Demonstration

Runtime polymorphism allows client code to treat diverse subclass instances uniformly through their shared supertype reference, while dynamic method dispatch ensures that each object invokes its own specialized logic at runtime.

### Example in `PaymentApplication.java`:
```java
// Upcasting: Storing subclass instances in base-type references
Payment cardPayment = new CardPayment("PAY-CRD-1001", 1500.00, "Visa Platinum **4321");
Payment upiPayment  = new UPIPayment("PAY-UPI-2002", 500.00, "student@upi");
Payment cashPayment = new CashPayment("PAY-CSH-3003", 200.00);

// Polymorphic collection
List<Payment> paymentList = List.of(cardPayment, upiPayment, cashPayment);

// Uniform iteration: Dynamic dispatch resolves the exact subclass implementation
for (Payment payment : paymentList) {
    payment.processPayment(); // Dispatches to CardPayment, UPIPayment, or CashPayment
}
```

### Safe Interface Querying via Pattern Matching:
Rather than blindly assuming all payments are refundable, the application inspects interface compliance at runtime:
```java
if (payment instanceof Refundable refundable) {
    refundable.refund(refundAmount); // Safe execution on CardPayment and UPIPayment
} else {
    // Graceful handling for CashPayment
    System.out.println("CashPayment does NOT implement Refundable.");
}
```

---

## 6. Compilation & Execution

### Direct Terminal Commands (from `daily-task-hcl/day-5/`):
```bash
# Compile all source packages into 'out' directory
javac -d out src/com/technova/payment/model/*.java src/com/technova/payment/service/*.java src/com/technova/payment/app/*.java

# Execute main class
java -cp out com.technova.payment.app.PaymentApplication
```

### Alternatively, using `run.bat`:
```cmd
run.bat
```

---

## 7. Observed Execution Output & Test Scenarios

```text
================================================================================
               TECHNOVA PAYMENT PROCESSING SYSTEM -- DAY 5                      
      Demonstrating Abstraction, Inheritance, Polymorphism & Interfaces        
================================================================================

>>> [1] CREATING INSTANCES USING BASE CLASS REFERENCES (UPCASTING)
Payment instances registered:
  * CardPayment[id='PAY-CRD-1001', amount=Rs. 1500.00, cardLabel='Visa Platinum **4321', refunded=Rs. 0.00]
  * UPIPayment[id='PAY-UPI-2002', amount=Rs. 500.00, upiId='student@upi', refunded=Rs. 0.00]
  * CashPayment[id='PAY-CSH-3003', amount=Rs. 200.00]

>>> [2] DEMONSTRATING RUNTIME POLYMORPHISM (ITERATING Payment[] / List<Payment>)
Calling payment.processPayment() dynamically dispatches to subclass implementations:
[1] Target class: CardPayment  | ID: PAY-CRD-1001 -> Processing card payment of Rs. 1500.00 [Card: Visa Platinum **4321, ID: PAY-CRD-1001]
[2] Target class: UPIPayment   | ID: PAY-UPI-2002 -> Processing UPI payment of Rs. 500.00 [UPI ID: student@upi, ID: PAY-UPI-2002]
[3] Target class: CashPayment  | ID: PAY-CSH-3003 -> Processing cash payment of Rs. 200.00 [ID: PAY-CSH-3003]

>>> [3] DEMONSTRATING COMPILE-TIME POLYMORPHISM (OVERLOADED pay() METHODS)
* Invocation 1: pay(Payment)
----------------------------------------------------------------
[PaymentService] Dispatching standard payment transaction: PAY-CRD-1001
Processing card payment of Rs. 1500.00 [Card: Visa Platinum **4321, ID: PAY-CRD-1001]

* Invocation 2: pay(Payment, String)
----------------------------------------------------------------
[PaymentService] Dispatching payment with note: "Quarterly Cloud Training Subscription - TechNova"
Processing UPI payment of Rs. 500.00 [UPI ID: student@upi, ID: PAY-UPI-2002]

* Invocation 3: pay(Payment, int)
----------------------------------------------------------------
[PaymentService] Splitting payment into 6 installments @ Rs. 2000.00/month
Processing card payment of Rs. 12000.00 [Card: Corporate Amex **9009, ID: PAY-CRD-1004]

>>> [4] DEMONSTRATING REFUND PROCESSING VIA Refundable INTERFACE
Inspecting payments for Refundable capability via type pattern checking:
----------------------------------------------------------------
Checking payment PAY-CRD-1001 (CardPayment, Amount: Rs. 1500.00):
  [VALID] This payment implements Refundable interface.
  Executing partial refund of 40% (Rs. 600.00)...
  -> Refund processed for Card Payment PAY-CRD-1001: Rs. 600.00 [Remaining Refundable: Rs. 900.00, Total Refunded: Rs. 600.00 / Rs. 1500.00]
  Executing second refund of 30% (Rs. 450.00)...
  -> Refund processed for Card Payment PAY-CRD-1001: Rs. 450.00 [Remaining Refundable: Rs. 450.00, Total Refunded: Rs. 1050.00 / Rs. 1500.00]
----------------------------------------------------------------
Checking payment PAY-UPI-2002 (UPIPayment, Amount: Rs. 500.00):
  [VALID] This payment implements Refundable interface.
  Executing partial refund of 40% (Rs. 200.00)...
  -> Refund processed for UPI Payment PAY-UPI-2002: Rs. 200.00 [Remaining Refundable: Rs. 300.00, Total Refunded: Rs. 200.00 / Rs. 500.00]
  Executing second refund of 30% (Rs. 150.00)...
  -> Refund processed for UPI Payment PAY-UPI-2002: Rs. 150.00 [Remaining Refundable: Rs. 150.00, Total Refunded: Rs. 350.00 / Rs. 500.00]
----------------------------------------------------------------
Checking payment PAY-CSH-3003 (CashPayment, Amount: Rs. 200.00):
  [INFO] CashPayment does NOT implement Refundable. Cash transactions cannot be refunded through digital gateways.

>>> [5] DEMONSTRATING VALIDATION & DEFENSIVE EXCEPTION HANDLING

--- Scenario A: Creating Payment with Negative Amount ---
Attempting: new CardPayment("PAY-ERR-01", -500.00, "Visa **1111")
CAUGHT EXPECTED EXCEPTION: Payment amount must be greater than zero. Received: -500.00

--- Scenario B: Creating Payment with Blank Payment ID ---
Attempting: new CashPayment("   ", 350.00)
CAUGHT EXPECTED EXCEPTION: Payment ID cannot be null or empty.

--- Scenario C: Creating UPI Payment with Invalid Format ---
Attempting: new UPIPayment("PAY-ERR-03", 250.00, "invalid-upi-handle")
CAUGHT EXPECTED EXCEPTION: Invalid UPI ID format. Expected format: user@bank, received: invalid-upi-handle

--- Scenario D: Refunding Negative Amount ---
Attempting: upiPayment refund of -50.00
CAUGHT EXPECTED EXCEPTION: Refund amount must be greater than zero. Received: Rs. -50.00

--- Scenario E: Excessive Refund Exceeding Remaining Balance ---
Attempting: upiPayment refund of Rs. 300.00 (Remaining is only Rs. 150.00)
CAUGHT EXPECTED EXCEPTION: Refund rejected for UPI Payment PAY-UPI-2002: requested Rs. 300.00 exceeds remaining refundable balance Rs. 150.00 (Original: Rs. 500.00, Already Refunded: Rs. 350.00)

--- Scenario F: Service Overload with Zero Installments ---
Attempting: paymentService.pay(cardPayment, 0)
CAUGHT EXPECTED EXCEPTION: Installment count must be at least 1. Provided: 0

================================================================================
      TECHNOVA PAYMENT PROCESSING APPLICATION COMPLETED SUCCESSFULLY           
================================================================================
```

---

## 8. Git Collaboration Exercise 1: Real Merge Conflict

### A. Context & Branch Setup
To simulate real-world pair programming, a shared training artifact was used:
```text
day-5/collaboration-demo.txt
```
The base version committed to `main` contained:
```ini
[GATEWAY_CONFIG]
API_VERSION = 2.4.0
GATEWAY_SECURITY_POLICY = Standard SHA-256 TLS 1.3
TRANSACTION_TIMEOUT_MS = 5000
RETRY_MAX_ATTEMPTS = 3
SUPPORT_EMAIL = dev-support@technova.com
```

### B. Conflicting Edits
1. **Branch `day5-conflict-student-a`** (Student A - Card specialist):
   - Changed Line 6 to:
     ```ini
     GATEWAY_SECURITY_POLICY = Student-A (Card Specialist): Enforcing AES-256 GCM Tokenization & PCI-DSS 4.0 Standard
     ```
   - Committed: `655b9d0 feat(gateway-config): student a enforces PCI-DSS AES tokenization policy`
2. **Branch `day5-conflict-student-b`** (Student B - UPI specialist):
   - Changed the exact same Line 6 to:
     ```ini
     GATEWAY_SECURITY_POLICY = Student-B (UPI Specialist): Enforcing NPCI UPI 2.0 Encrypted Virtual Private Addressing & Signed Mandates
     ```
   - Committed: `88014e9 feat(gateway-config): student b configures NPCI UPI 2.0 encrypted mandate policy`

### C. Triggering the Conflict
On branch `day5-conflict-student-a`:
```bash
git merge day5-conflict-student-b
```
**Git Output**:
```text
Auto-merging day-5/collaboration-demo.txt
CONFLICT (content): Merge conflict in day-5/collaboration-demo.txt
Automatic merge failed; fix conflicts and then commit the result.
```

### D. Captured Conflict Markers
Running `git diff` displayed the exact three-way conflict block:
```text
<<<<<<< HEAD
GATEWAY_SECURITY_POLICY = Student-A (Card Specialist): Enforcing AES-256 GCM Tokenization & PCI-DSS 4.0 Standard
=======
GATEWAY_SECURITY_POLICY = Student-B (UPI Specialist): Enforcing NPCI UPI 2.0 Encrypted Virtual Private Addressing & Signed Mandates
>>>>>>> day5-conflict-student-b
```

### E. Deliberate Resolution Process
1. Analyzed requirements from both developers: Student A's card tokenization standards and Student B's UPI mandate standards are both necessary for a multi-channel payment gateway.
2. Replaced the conflict block with a harmonized configuration:
   ```ini
   GATEWAY_SECURITY_POLICY = Unified Hybrid Policy: PCI-DSS 4.0 AES-256 GCM Tokenization (Card/Student-A) & NPCI UPI 2.0 Signed Mandates (UPI/Student-B)
   ```
3. Staged the resolved file:
   ```bash
   git add day-5/collaboration-demo.txt
   ```
4. Finalized the merge commit:
   ```bash
   git commit -m "merge: resolve gateway security policy conflict between card (student-a) and upi (student-b) specifications"
   ```
   Committed with hash `d934e5b`.
5. Fast-forwarded `main` to incorporate the resolved state:
   ```bash
   git switch main
   git merge day5-conflict-student-a
   ```

---

## 9. Git Collaboration Exercise 2: Branch Rebase onto Main

### A. Context & Purpose of Rebase
While `git merge` preserves non-linear branching history with explicit merge commits, `git rebase` replays feature commits sequentially on top of the latest upstream base, resulting in a clean, linear project history.

### B. Execution Sequence
1. Created feature branch `day5-rebase-demo` from `main` (`d934e5b`):
   ```bash
   git switch -c day5-rebase-demo
   ```
2. Added `day-5/rebase-demo-notes.txt` specifying asynchronous audit telemetry and committed:
   ```bash
   git add day-5/rebase-demo-notes.txt
   git commit -m "feat(telemetry): add asynchronous payment audit logging specification"
   ```
   (Original commit hash: `57eb355`)
3. Switched back to `main` and made an independent, non-overlapping commit:
   - Added `day-5/run.bat` and updated root `README.md` progress tracker.
   - Committed: `2048516 chore(day-5): add run.bat execution script and update progress tracker in root README`
4. Switched back to `day5-rebase-demo` and initiated rebase:
   ```bash
   git switch day5-rebase-demo
   git rebase main
   ```
   **Git Rebase Output**:
   ```text
   Rebasing (1/1)
   Successfully rebased and updated refs/heads/day5-rebase-demo.
   ```
   (Replayed commit received new hash: `e8a9688`)
5. Integrated rebased commits into `main`:
   ```bash
   git switch main
   git merge day5-rebase-demo
   ```

### C. Verified Git Graph (`git log --oneline --graph --all -10`)
```text
* e8a9688 (HEAD -> main, day5-rebase-demo) feat(telemetry): add asynchronous payment audit logging specification
* 2048516 chore(day-5): add run.bat execution script and update progress tracker in root README
*   d934e5b (day5-conflict-student-a) merge: resolve gateway security policy conflict between card (student-a) and upi (student-b) specifications
|\  
| * 88014e9 (day5-conflict-student-b) feat(gateway-config): student b configures NPCI UPI 2.0 encrypted mandate policy
* | 655b9d0 feat(gateway-config): student a enforces PCI-DSS AES tokenization policy
|/  
* ed044d4 feat(day-5): implement payment hierarchy, polymorphism, and initial collaboration demo
* 64838d2 (origin/main) docs: restore Day 03 table row and section header in README
* 5e0c5c3 chore: remove redundant day-1 and day-4 directories
* e103b7c feat: align Day 4 structure with Maven support, run script, and Day-04 folder convention
* 0577e94 day-4: bank-account-debugging
```

---

## 10. Summary of Git Commands Executed

```bash
# Day 5 Base Setup
git add day-5
git commit -m "feat(day-5): implement payment hierarchy, polymorphism, and initial collaboration demo"

# Conflict Simulation - Branch A
git switch -c day5-conflict-student-a
# [edit day-5/collaboration-demo.txt]
git commit -am "feat(gateway-config): student a enforces PCI-DSS AES tokenization policy"

# Conflict Simulation - Branch B
git switch main
git switch -c day5-conflict-student-b
# [edit same line in day-5/collaboration-demo.txt]
git commit -am "feat(gateway-config): student b configures NPCI UPI 2.0 encrypted mandate policy"

# Merge Conflict Generation & Resolution
git switch day5-conflict-student-a
git merge day5-conflict-student-b     # Triggered CONFLICT (content)
git status                             # Confirmed both modified: collaboration-demo.txt
git diff                               # Inspected conflict markers
# [resolve conflict in collaboration-demo.txt]
git add day-5/collaboration-demo.txt
git commit -m "merge: resolve gateway security policy conflict between card (student-a) and upi (student-b) specifications"
git switch main
git merge day5-conflict-student-a     # Fast-forward main

# Rebase Exercise
git switch -c day5-rebase-demo
# [add day-5/rebase-demo-notes.txt]
git add day-5/rebase-demo-notes.txt
git commit -m "feat(telemetry): add asynchronous payment audit logging specification"
git switch main
# [add day-5/run.bat, update root README.md]
git add day-5/run.bat README.md
git commit -m "chore(day-5): add run.bat execution script and update progress tracker in root README"
git switch day5-rebase-demo
git rebase main                       # Successfully replayed commit
git switch main
git merge day5-rebase-demo            # Fast-forward main
```

---

## 11. Pair Programming & Remote Push Status

- **Pair Coordination**: Both branches (`day5-conflict-student-a` and `day5-conflict-student-b`) were executed and documented to simulate the exact interaction between two independent contributors on a shared codebase.
- **Remote Push**: All local commits across `main` and feature branches (`day5-conflict-student-a`, `day5-conflict-student-b`, `day5-rebase-demo`) are prepared for pushing to `origin` without using force-push (`--force`), preserving full commit authenticity.
