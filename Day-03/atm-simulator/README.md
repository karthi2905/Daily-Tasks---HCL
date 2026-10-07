# ATM Simulator

A clean, menu-driven Java 17 console application built for **Day 3 of the HCL Core Java Training Assessment**.

---

## Description

A simple Java console-based ATM simulator demonstrating menu-driven programming, loops, switch statements, input validation, PIN authentication, transaction handling, and Maven profiles.

---

## Features

- **3 PIN Attempts**: Maximum of 3 authentication attempts with card blocking on repeated failures.
- **Account Authentication**: Secure PIN check breaking early upon successful entry.
- **Check Balance**: Live display of current account balance.
- **Withdraw Money**: Safe cash withdrawals with validation against zero, negative amounts, and insufficient balance.
- **Deposit Money**: Deposit processing with validation against zero and negative amounts.
- **Mini Statement**: Formatted transaction history displayed using an enhanced-for loop.
- **Input Validation**: Safe handling of non-numeric and out-of-range user inputs.
- **Invalid Menu Handling**: Explicit use of `continue` to refresh the menu when invalid choices are entered.
- **do-while Loop**: Clean interactive menu loop structure.
- **switch Statement**: Clear routing of user choices (1 to 5).
- **Control Flow Statements**: Appropriate application of `break` and `continue`.
- **Enhanced-for Loop**: Iteration over transaction history.
- **Maven dev/prod Profiles**: Dynamic build profiles changing the `environment` property.

---

## Technologies

- **Java**: 17
- **Build Tool**: Apache Maven

---

## How to Run

### Standard Build & Run
```bash
# Navigate to project folder
cd Day-03/atm-simulator

# Compile and package
mvn clean package

# Run the packaged JAR
java -jar target/atm-simulator-1.0-SNAPSHOT.jar
```

Or run via Maven Exec Plugin:
```bash
mvn exec:java
```

Or on Windows:
```cmd
run.bat
```

---

## Maven Profiles (`dev` & `prod`)

The project supports environment-specific configurations via Maven profiles that update the `environment` property:

### Development Profile
```bash
mvn clean package -Pdev
```
- Sets `environment=development`.
- Filters into `application.properties` and the JAR manifest.

### Production Profile
```bash
mvn clean package -Pprod
```
- Sets `environment=production`.
- Filters into `application.properties` and the JAR manifest.
