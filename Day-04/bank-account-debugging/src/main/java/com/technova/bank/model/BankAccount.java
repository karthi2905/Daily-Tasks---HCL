package com.technova.bank.model;

/**
 * Bank Account Model
 * Day 4 Core Java Training Assessment
 *
 * Demonstrates:
 * 1. Strict encapsulation with private fields
 * 2. Class-level static counter (accountCounter) generating unique account numbers
 * 3. Three chained constructors using this(...) delegation
 * 4. Input validation (null/blank name check, non-negative balance)
 * 5. Validated deposit and withdrawal operations
 * 6. Protection against negative amounts and account overdrafts
 * 7. Encapsulated getters without direct balance/account setters
 * 8. Object identity contracts (equals and hashCode based on accountNumber)
 * 9. Clear toString representation for debugging and console logging
 */
public class BankAccount {

    // -------------------------------------------------------------------------
    // Private Instance Fields (Encapsulation)
    // -------------------------------------------------------------------------
    private final int accountNumber;
    private String accountHolderName;
    private double balance;

    // -------------------------------------------------------------------------
    // Static Class-Level Counter
    // -------------------------------------------------------------------------
    // Shared across all instances to assign unique sequential numbers (1001, 1002, ...)
    private static int accountCounter = 1000;

    // =========================================================================
    // Constructor 1: Default Constructor
    // =========================================================================
    /**
     * Default constructor.
     * Chains to the single-argument constructor using "Unknown" as the default holder.
     */
    public BankAccount() {
        this("Unknown");
    }

    // =========================================================================
    // Constructor 2: Single-Parameter Constructor
    // =========================================================================
    /**
     * Single-parameter constructor.
     * Chains to the two-parameter constructor with an initial balance of 0.0.
     *
     * @param accountHolderName the name of the account holder
     */
    public BankAccount(String accountHolderName) {
        this(accountHolderName, 0.0);
    }

    // =========================================================================
    // Constructor 3: Canonical Full-Parameter Constructor
    // =========================================================================
    /**
     * Canonical constructor performing input validation and state initialization.
     *
     * @param accountHolderName the name of the account holder (non-null, non-blank)
     * @param initialBalance    the starting balance (must be non-negative)
     */
    public BankAccount(String accountHolderName, double initialBalance) {
        // Validation: Account holder name cannot be null or blank
        if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid account holder name: name cannot be null or blank.");
        }

        // Validation: Initial balance cannot be negative
        if (initialBalance < 0.0) {
            throw new IllegalArgumentException("Initial balance cannot be negative: " + initialBalance);
        }

        // Increment static class-level counter to ensure unique account identity
        this.accountNumber = ++accountCounter;
        this.accountHolderName = accountHolderName.trim();
        this.balance = initialBalance;
    }

    // =========================================================================
    // Business Operations: Deposit & Withdraw
    // =========================================================================
    /**
     * Deposits money into the account.
     *
     * @param amount the deposit amount (must be strictly > 0)
     */
    public void deposit(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero: " + amount);
        }
        this.balance += amount;
    }

    /**
     * Withdraws money from the account after validating funds availability.
     *
     * @param amount the withdrawal amount (must be strictly > 0 and <= balance)
     */
    public void withdraw(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero: " + amount);
        }
        if (amount > this.balance) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient balance. Current balance: %.2f, Requested withdrawal: %.2f",
                    this.balance, amount));
        }

        // Validated deduction
        this.balance -= amount;
    }

    // =========================================================================
    // Encapsulated Getters
    // =========================================================================
    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public static int getAccountCounter() {
        return accountCounter;
    }

    // =========================================================================
    // Object Identity & Contracts (equals, hashCode, toString)
    // =========================================================================
    /**
     * Logical equality based strictly on the unique accountNumber.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BankAccount that = (BankAccount) obj;
        return this.accountNumber == that.accountNumber;
    }

    /**
     * Consistent hash code derived from the unique accountNumber.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(accountNumber);
    }

    @Override
    public String toString() {
        return String.format("BankAccount[accountNumber=%d, holder='%s', balance=%.2f]",
                accountNumber, accountHolderName, balance);
    }
}
