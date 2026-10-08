package com.technova.bank.model;

import java.util.Objects;

/**
 * Represents a bank account with encapsulated state, unique account numbering,
 * constructor chaining, and input validation.
 */
public class BankAccount {

    // Private fields demonstrating strict encapsulation
    private final int accountNumber;
    private String accountHolderName;
    private double balance;

    // Static counter shared across all BankAccount instances to generate unique account numbers
    private static int accountCounter = 1000;

    /**
     * Constructor 1: Default constructor.
     * Chains to the single-parameter constructor using default holder "Unknown".
     */
    public BankAccount() {
        this("Unknown");
    }

    /**
     * Constructor 2: Single-parameter constructor.
     * Chains to the two-parameter constructor using default initial balance 0.0.
     *
     * @param accountHolderName the name of the account holder
     */
    public BankAccount(String accountHolderName) {
        this(accountHolderName, 0.0);
    }

    /**
     * Constructor 3: Full-parameter constructor.
     * Performs input validation, assigns unique account number, and initializes fields.
     *
     * @param accountHolderName the name of the account holder
     * @param initialBalance    the opening account balance (must be non-negative)
     */
    public BankAccount(String accountHolderName, double initialBalance) {
        if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid account holder name: name cannot be null or blank.");
        }
        if (initialBalance < 0.0) {
            throw new IllegalArgumentException("Initial balance cannot be negative: " + initialBalance);
        }

        // Increment class-level static counter to guarantee unique ID
        this.accountNumber = ++accountCounter;
        this.accountHolderName = accountHolderName.trim();
        this.balance = initialBalance;
    }

    /**
     * Deposits a positive amount into the account.
     *
     * @param amount the amount to deposit (must be > 0)
     */
    public void deposit(double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero: " + amount);
        }
        this.balance += amount;
    }

    /**
     * Withdraws a positive amount from the account if sufficient funds are available.
     *
     * @param amount the amount to withdraw (must be > 0 and <= balance)
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

        // Fix applied: Single deduction ensures correct accounting
        this.balance -= amount;
    }

    // Encapsulated getters (no direct setters to preserve data integrity)
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

    /**
     * Two bank accounts are logically equal if they share the same unique account number.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BankAccount that = (BankAccount) obj;
        return this.accountNumber == that.accountNumber;
    }

    /**
     * Hash code based consistently on the logical identifier (accountNumber).
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
