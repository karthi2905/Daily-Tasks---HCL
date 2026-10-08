package com.technova.bank.service;

import com.technova.bank.model.BankAccount;

/**
 * Service orchestrating operations on bank accounts.
 * Delegates core balance mutation and validation directly to the BankAccount domain model.
 */
public class BankAccountService {

    /**
     * Executes a deposit on the specified account.
     *
     * @param account the target bank account
     * @param amount  the deposit amount
     */
    public void deposit(BankAccount account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        account.deposit(amount);
    }

    /**
     * Executes a withdrawal on the specified account.
     *
     * @param account the target bank account
     * @param amount  the withdrawal amount
     */
    public void withdraw(BankAccount account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }
        account.withdraw(amount);
    }

    /**
     * Formats and prints comprehensive account details to the console.
     *
     * @param account the account to display
     */
    public void printAccountDetails(BankAccount account) {
        if (account == null) {
            System.out.println("No account details available (null).");
            return;
        }
        System.out.printf("  Account Number: %d%n", account.getAccountNumber());
        System.out.printf("  Account Holder: %s%n", account.getAccountHolderName());
        System.out.printf("  Current Balance: $%.2f%n", account.getBalance());
    }
}
