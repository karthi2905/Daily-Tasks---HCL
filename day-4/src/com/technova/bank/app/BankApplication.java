package com.technova.bank.app;

import com.technova.bank.model.BankAccount;
import com.technova.bank.service.BankAccountService;

/**
 * Console application demonstrating:
 * - Constructor chaining (3 chained constructors)
 * - Class-level static account numbering counter
 * - Encapsulated state and business validations
 * - Deposit and withdrawal workflows
 * - Exception handling for boundary violations
 * - Object contract implementations (equals, hashCode, toString)
 */
public class BankApplication {

    public static void main(String[] args) {
        BankAccountService service = new BankAccountService();

        System.out.println("==================================================");
        System.out.println("       TECHNOVA BANK ACCOUNT SYSTEM — DAY 4       ");
        System.out.println("==================================================");
        System.out.println();

        // ---------------------------------------------------------------------
        // 1. CONSTRUCTOR CHAINING & STATIC COUNTER DEMONSTRATION
        // ---------------------------------------------------------------------
        System.out.println("----- 1. CREATING ACCOUNTS (CONSTRUCTOR CHAINING) -----");

        // Constructor 1: Default -> calls this("Unknown") -> calls this("Unknown", 0.0)
        BankAccount acc1 = new BankAccount();
        System.out.println("Account 1 (Default constructor):");
        service.printAccountDetails(acc1);
        System.out.println();

        // Constructor 2: 1-parameter -> calls this("Karthik", 0.0)
        BankAccount acc2 = new BankAccount("Karthik");
        System.out.println("Account 2 (1-argument constructor):");
        service.printAccountDetails(acc2);
        System.out.println();

        // Constructor 3: 2-parameter -> direct initialization with validation
        BankAccount acc3 = new BankAccount("Arun", 5000.00);
        System.out.println("Account 3 (2-argument constructor with $5000.00 initial balance):");
        service.printAccountDetails(acc3);
        System.out.println();

        System.out.printf("Total Accounts Created (Static Counter): %d%n%n", BankAccount.getAccountCounter());

        // ---------------------------------------------------------------------
        // 2. DEPOSIT OPERATIONS
        // ---------------------------------------------------------------------
        System.out.println("----- 2. DEPOSIT WORKFLOW -----");
        System.out.printf("Initial Balance for Account 3 (%s): $%.2f%n", acc3.getAccountHolderName(), acc3.getBalance());
        System.out.println("Depositing $1000.00...");
        service.deposit(acc3, 1000.00);
        System.out.printf("Updated Balance: $%.2f (Expected: $6000.00)%n%n", acc3.getBalance());

        // ---------------------------------------------------------------------
        // 3. WITHDRAWAL OPERATIONS & DEBUGGING SCENARIO
        // ---------------------------------------------------------------------
        System.out.println("----- 3. WITHDRAWAL WORKFLOW & DEBUGGING TARGET -----");
        System.out.printf("Current Balance before withdrawal: $%.2f%n", acc3.getBalance());
        double withdrawAmount = 2000.00;
        System.out.printf("Withdrawing $%.2f...%n", withdrawAmount);
        service.withdraw(acc3, withdrawAmount);
        System.out.printf("Balance after $%.2f withdrawal: $%.2f%n%n", withdrawAmount, acc3.getBalance());

        // Reproduction scenario specifically outlined in Day 4 specs:
        System.out.println("----- DEBUGGING REPRODUCTION TEST CASE -----");
        BankAccount debugAccount = new BankAccount("Test Subject", 5000.00);
        System.out.printf("Account created: %s, Initial Balance: $%.2f%n", debugAccount.getAccountHolderName(), debugAccount.getBalance());
        System.out.println("Triggering withdraw($1000.00)...");
        service.withdraw(debugAccount, 1000.00);
        System.out.printf("Expected Balance : $4000.00%n");
        System.out.printf("Actual Balance   : $%.2f%n", debugAccount.getBalance());
        if (Math.abs(debugAccount.getBalance() - 4000.00) < 0.001) {
            System.out.println("Status: [PASS] - Correct withdrawal logic verified!");
        } else {
            System.out.println("Status: [BUG DETECTED] - withdraw() executed double subtraction!");
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 4. BOUNDARY & VALIDATION EXCEPTION TESTS
        // ---------------------------------------------------------------------
        System.out.println("----- 4. EXCEPTION & VALIDATION BOUNDARIES -----");

        // Invalid account name
        try {
            System.out.print("Testing blank account name: ");
            new BankAccount("   ", 100.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }

        // Negative initial balance
        try {
            System.out.print("Testing negative initial balance: ");
            new BankAccount("Priya", -250.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }

        // Invalid deposit (zero or negative)
        try {
            System.out.print("Testing zero deposit: ");
            service.deposit(acc3, 0.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }

        try {
            System.out.print("Testing negative deposit: ");
            service.deposit(acc3, -500.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }

        // Invalid withdrawal (negative amount)
        try {
            System.out.print("Testing negative withdrawal: ");
            service.withdraw(acc3, -100.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }

        // Insufficient funds withdrawal
        try {
            System.out.printf("Testing overdraft withdrawal of $15000.00 (Current Balance: $%.2f): ", acc3.getBalance());
            service.withdraw(acc3, 15000.0);
        } catch (IllegalArgumentException e) {
            System.out.printf("[Caught Expected Exception] %s%n", e.getMessage());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // 5. EQUALS, HASHCODE & TOSTRING CONTRACTS
        // ---------------------------------------------------------------------
        System.out.println("----- 5. OBJECT IDENTITY (EQUALS / HASHCODE / TOSTRING) -----");
        System.out.printf("Account 1: %s%n", acc1);
        System.out.printf("Account 2: %s%n", acc2);
        System.out.printf("Account 3: %s%n", acc3);
        System.out.println();

        System.out.printf("Account 1 equals Account 2: %b%n", acc1.equals(acc2));
        System.out.printf("Account 2 equals Account 2 (reflexive): %b%n", acc2.equals(acc2));
        System.out.printf("Account 1 equals null: %b%n", acc1.equals(null));
        System.out.println();

        System.out.printf("Account 1 hashCode: %d%n", acc1.hashCode());
        System.out.printf("Account 2 hashCode: %d%n", acc2.hashCode());
        System.out.printf("Account 3 hashCode: %d%n", acc3.hashCode());
        System.out.println();

        System.out.println("==================================================");
        System.out.println("         ALL TESTS COMPLETED SUCCESSFULLY         ");
        System.out.println("==================================================");
    }
}
