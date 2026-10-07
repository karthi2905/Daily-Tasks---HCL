package com.technova.atm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * ATM Simulator
 * Day 3 Core Java Training Assessment
 *
 * Demonstrates:
 * 1. do-while loop for ATM menu
 * 2. switch statement for menu routing
 * 3. Robust input validation with Scanner
 * 4. Maximum 3 PIN attempts with break
 * 5. continue for invalid menu selection
 * 6. Enhanced for-loop for mini-statement display
 * 7. Dynamic balance and transaction history management
 * 8. Maven dev/prod profile property detection
 */
public class ATMSimulator {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final int CORRECT_PIN = 1234;
    private static final double INITIAL_BALANCE = 10000.00;
    private static final int MAX_PIN_ATTEMPTS = 3;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private static double balance = INITIAL_BALANCE;
    private static final List<String> transactions = new ArrayList<>();
    private static String environment = "development";

    public static void main(String[] args) {
        loadEnvironmentProperty();

        Scanner scanner = new Scanner(System.in);

        displayHeader();

        // 1. PIN Authentication (Max 3 attempts with break)
        boolean authenticated = authenticateUser(scanner);
        if (!authenticated) {
            scanner.close();
            return;
        }

        // Initialize sample transaction history
        initializeSampleTransactions();

        // 2. Main ATM Menu using do-while loop
        int choice;
        do {
            displayMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            // Validate numeric menu input
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue; // Demonstrates continue
            }

            // Validate menu range (1 to 5)
            if (choice < 1 || choice > 5) {
                System.out.println("Invalid choice. Please select an option from 1 to 5.");
                continue; // Demonstrates continue
            }

            // 3. Menu handling using switch statement
            switch (choice) {
                case 1:
                    checkBalance();
                    break;
                case 2:
                    withdrawMoney(scanner);
                    break;
                case 3:
                    depositMoney(scanner);
                    break;
                case 4:
                    displayMiniStatement();
                    break;
                case 5:
                    System.out.println();
                    System.out.println("Thank you for using TechNova ATM.");
                    System.out.println("Have a nice day!");
                    break;
                default:
                    // Guard default case
                    break;
            }

            System.out.println();

        } while (choice != 5);

        scanner.close();
    }

    // =========================================================================
    // Authentication
    // =========================================================================

    /**
     * Authenticates the user with a maximum of 3 attempts.
     * Uses break upon successful authentication.
     */
    private static boolean authenticateUser(Scanner scanner) {
        boolean authenticated = false;

        for (int attempt = 1; attempt <= MAX_PIN_ATTEMPTS; attempt++) {
            System.out.print("Enter your PIN: ");
            String pinInput = scanner.nextLine().trim();

            int enteredPin;
            try {
                enteredPin = Integer.parseInt(pinInput);
            } catch (NumberFormatException e) {
                enteredPin = -1;
            }

            if (enteredPin == CORRECT_PIN) {
                authenticated = true;
                System.out.println("PIN verified successfully.");
                System.out.println();
                break; // Break loop when PIN is verified
            } else {
                int attemptsRemaining = MAX_PIN_ATTEMPTS - attempt;
                if (attemptsRemaining > 0) {
                    System.out.println("Incorrect PIN. Attempts remaining: " + attemptsRemaining);
                    System.out.println();
                } else {
                    System.out.println("Incorrect PIN. Your card is blocked.");
                }
            }
        }

        return authenticated;
    }

    // =========================================================================
    // ATM Operations
    // =========================================================================

    /**
     * Displays the current account balance.
     */
    private static void checkBalance() {
        System.out.println();
        System.out.println("----- BALANCE -----");
        System.out.printf("Available Balance: ₹%.2f%n", balance);
    }

    /**
     * Handles cash withdrawal with validation for negatives, zero, and overdrafts.
     */
    private static void withdrawMoney(Scanner scanner) {
        System.out.println();
        System.out.print("Enter withdrawal amount: ");
        String input = scanner.nextLine().trim();

        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Invalid amount. Amount must be greater than zero.");
            return;
        }

        if (amount > balance) {
            System.out.println("Insufficient balance.");
            return;
        }

        balance -= amount;
        transactions.add(String.format("Withdrawal: ₹%.2f", amount));

        System.out.println("Withdrawal successful.");
        System.out.println("Please collect your cash.");
        System.out.printf("Remaining Balance: ₹%.2f%n", balance);
    }

    /**
     * Handles cash deposit with validation for zero, negatives, and invalid input.
     */
    private static void depositMoney(Scanner scanner) {
        System.out.println();
        System.out.print("Enter deposit amount: ");
        String input = scanner.nextLine().trim();

        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Invalid amount. Deposit must be greater than zero.");
            return;
        }

        balance += amount;
        transactions.add(String.format("Deposit: ₹%.2f", amount));

        System.out.println("Deposit successful.");
        System.out.printf("Updated Balance: ₹%.2f%n", balance);
    }

    /**
     * Displays the mini-statement using an enhanced for-loop.
     */
    private static void displayMiniStatement() {
        System.out.println();
        System.out.println("===== MINI STATEMENT =====");
        System.out.println();

        if (transactions.isEmpty()) {
            System.out.println("No recent transactions.");
        } else {
            // Mandatory: Enhanced for-loop to iterate and print transactions
            for (String transaction : transactions) {
                System.out.println(transaction);
            }
        }

        System.out.println();
        System.out.printf("Current Balance: ₹%.2f%n", balance);
    }

    // =========================================================================
    // UI Helpers
    // =========================================================================

    private static void displayHeader() {
        System.out.println("==================================================");
        System.out.printf("     TECHNOVA ATM  [Profile: %s]%n", environment);
        System.out.println("==================================================");
        System.out.println();
    }

    private static void displayMenu() {
        System.out.println("===== ATM MENU =====");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw Money");
        System.out.println("3. Deposit Money");
        System.out.println("4. Mini Statement");
        System.out.println("5. Exit");
    }

    private static void initializeSampleTransactions() {
        if (transactions.isEmpty()) {
            transactions.add("Withdrawal: ₹1000.00");
            transactions.add("Deposit: ₹500.00");
            transactions.add("Withdrawal: ₹2000.00");
        }
    }

    /**
     * Loads the Maven profile filtered property from application.properties.
     */
    private static void loadEnvironmentProperty() {
        Properties properties = new Properties();
        try (InputStream is = ATMSimulator.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                properties.load(is);
                String envVal = properties.getProperty("environment");
                if (envVal != null && !envVal.trim().isEmpty() && !envVal.startsWith("${")) {
                    environment = envVal.trim();
                }
            }
        } catch (Exception ignored) {
            // Keep default environment
        }
    }
}
