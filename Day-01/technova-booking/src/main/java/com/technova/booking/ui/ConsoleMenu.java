package com.technova.booking.ui;

import com.technova.booking.comparator.BookingByEmployeeComparator;
import com.technova.booking.comparator.BookingByResourceComparator;
import com.technova.booking.comparator.BookingByStartTimeComparator;
import com.technova.booking.exception.BookingConflictException;
import com.technova.booking.exception.InvalidBookingException;
import com.technova.booking.exception.ResourceNotFoundException;
import com.technova.booking.model.Booking;
import com.technova.booking.model.Employee;
import com.technova.booking.model.Resource;
import com.technova.booking.model.User;
import com.technova.booking.repository.InMemoryRepository;
import com.technova.booking.service.ApprovalService;
import com.technova.booking.service.BookingService;
import com.technova.booking.service.ResourceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * Console menu — the single user-facing class.
 *
 * Responsibilities:
 *  - Render menus and read validated input
 *  - Delegate all business logic to services
 *  - Handle and display exceptions meaningfully
 *
 * Demonstrates:
 *  - Input validation (empty, non-numeric, out-of-range)
 *  - Specific catch blocks for BookingConflictException,
 *    ResourceNotFoundException, InvalidBookingException
 *  - Collections.sort with Comparable (natural order)
 *  - list.sort with Comparator (alternate orders)
 *  - Queue iteration (peek/poll)
 *  - Set membership display
 */
public class ConsoleMenu {

    private static final String SEPARATOR =
            "=================================================";
    private static final String THIN_LINE =
            "-------------------------------------------------";

    private final ResourceService resourceService;
    private final BookingService bookingService;
    private final ApprovalService approvalService;
    private final InMemoryRepository<User, String> userRepository;
    private final Set<String> registeredIds;
    private final Scanner scanner;

    public ConsoleMenu(ResourceService resourceService,
                       BookingService bookingService,
                       ApprovalService approvalService,
                       InMemoryRepository<User, String> userRepository,
                       Set<String> registeredIds) {
        this.resourceService = resourceService;
        this.bookingService = bookingService;
        this.approvalService = approvalService;
        this.userRepository = userRepository;
        this.registeredIds = registeredIds;
        this.scanner = new Scanner(System.in);
    }

    // =========================================================================
    // MAIN LOOP
    // =========================================================================

    /**
     * Starts the interactive console session.
     */
    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ", 1, 8);
            switch (choice) {
                case 1 -> handleViewResources();
                case 2 -> handleCreateBooking();
                case 3 -> handleViewBookings();
                case 4 -> handleViewApprovalQueue();
                case 5 -> handleProcessApproval();
                case 6 -> handleSortBookings();
                case 7 -> handleViewEmployees();
                case 8 -> {
                    System.out.println("\nThank you for using TechNova Booking System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1-8.");
            }
        }
    }

    // =========================================================================
    // MENU HANDLERS
    // =========================================================================

    private void handleViewResources() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("          AVAILABLE RESOURCES");
        System.out.println(SEPARATOR);
        List<Resource> resources = resourceService.getAllResources();
        resourceService.printResources(resources);    // bounded generic in action
        System.out.println();
    }

    /**
     * Handles the Create Booking flow (Flow 1).
     *
     * Demonstrates:
     *  - Input validation
     *  - Specific catch blocks for each custom exception type
     *  - 'throw' indirectly via service layer
     *  - 'throws' declared in BookingService
     */
    private void handleCreateBooking() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("           CREATE BOOKING");
        System.out.println(SEPARATOR);

        // --- Employee ID ---
        String employeeId = readNonBlank("Enter Employee ID: ");
        if (!registeredIds.contains(employeeId)) {
            System.out.println("  ERROR: Employee not found: " + employeeId);
            return;
        }
        User user = userRepository.findById(employeeId);
        if (!(user instanceof Employee employee)) {
            System.out.println("  ERROR: '" + employeeId + "' is not an Employee account.");
            return;
        }

        // --- Resource ID ---
        String resourceId = readNonBlank("Enter Resource ID: ");

        // --- Date ---
        LocalDate date = readDate("Enter Date (YYYY-MM-DD): ");
        if (date == null) {
            return;
        }

        // --- Start and End time ---
        LocalTime startTime = readTime("Enter Start Time (HH:MM): ");
        if (startTime == null) {
            return;
        }
        LocalTime endTime = readTime("Enter End Time (HH:MM): ");
        if (endTime == null) {
            return;
        }

        System.out.println();
        System.out.println("  Checking resource...");

        // Specific catch blocks — no generic catch(Exception)
        try {
            Booking booking = bookingService.createBooking(
                    employee, resourceId, date, startTime, endTime);

            if (booking.getResource().isRestricted()) {
                System.out.println();
                System.out.println("  This resource requires approval.");
                System.out.println();
                System.out.println("  Booking request created.");
                System.out.println();
                System.out.println("  Booking ID : " + booking.getBookingId());
                System.out.println("  Status     : " + booking.getStatus());
                System.out.println();
                System.out.println("  Request added to approval queue.");
            } else {
                System.out.println();
                System.out.println("  Booking created successfully.");
                System.out.println();
                booking.printSummary();
            }

        } catch (ResourceNotFoundException e) {
            // Specific catch for unchecked ResourceNotFoundException
            System.out.println("  ERROR: " + e.getMessage());

        } catch (InvalidBookingException e) {
            // Specific catch for unchecked InvalidBookingException
            System.out.println("  ERROR: " + e.getMessage());

        } catch (BookingConflictException e) {
            // Specific catch for checked BookingConflictException
            System.out.println("  CONFLICT: " + e.getMessage());
        }

        System.out.println();
    }

    private void handleViewBookings() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("           ALL BOOKINGS");
        System.out.println(SEPARATOR);
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("  No bookings found.");
        } else {
            for (Booking b : bookings) {
                System.out.println("  " + b);
            }
        }
        System.out.println();
    }

    /**
     * Displays all pending bookings in the approval queue without removing them.
     */
    private void handleViewApprovalQueue() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("           APPROVAL QUEUE");
        System.out.println(SEPARATOR);

        Queue<Booking> queue = approvalService.getPendingQueue();
        if (queue.isEmpty()) {
            System.out.println("  Approval queue is empty. No pending requests.");
        } else {
            System.out.println("  Pending requests: " + queue.size());
            System.out.println(THIN_LINE);
            // Iterate over the queue without consuming it
            int position = 1;
            for (Booking b : queue) {
                System.out.println("  [" + position++ + "] " + b);
            }
        }
        System.out.println();
    }

    /**
     * Handles the Approval Processing flow (Flow 2).
     *
     * Demonstrates:
     *  - Queue FIFO: poll() removes and returns the head
     *  - Status mutation (CONFIRMED / REJECTED)
     */
    private void handleProcessApproval() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("          APPROVAL REQUEST");
        System.out.println(SEPARATOR);

        Booking next = approvalService.peekNext();
        if (next == null) {
            System.out.println("  Approval queue is empty. Nothing to process.");
            System.out.println();
            return;
        }

        System.out.println();
        next.printSummary();
        System.out.println();
        System.out.println("  1. Approve");
        System.out.println("  2. Reject");
        System.out.println("  3. Skip (return to menu)");
        System.out.println();

        int choice = readInt("Enter choice: ", 1, 3);
        if (choice == 1) {
            Booking approved = approvalService.approveNext();
            System.out.println();
            System.out.println("  Booking approved successfully.");
            System.out.println("  Status: " + approved.getStatus());

        } else if (choice == 2) {
            Booking rejected = approvalService.rejectNext();
            System.out.println();
            System.out.println("  Booking rejected.");
            System.out.println("  Status: " + rejected.getStatus());

        } else {
            System.out.println("  Skipped.");
        }
        System.out.println();
    }

    /**
     * Demonstrates Comparable (natural order) and Comparator (alternate orders).
     */
    private void handleSortBookings() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("           SORT BOOKINGS");
        System.out.println(SEPARATOR);
        System.out.println("  1. Sort by Date & Start Time (natural order)");
        System.out.println("  2. Sort by Resource Name");
        System.out.println("  3. Sort by Employee Name");
        System.out.println("  4. Sort by Start Time only");
        System.out.println();

        int choice = readInt("Enter sort option: ", 1, 4);
        List<Booking> bookings = bookingService.getAllBookings();

        if (bookings.isEmpty()) {
            System.out.println("  No bookings to sort.");
            System.out.println();
            return;
        }

        switch (choice) {
            case 1 -> {
                // Comparable natural order via Collections.sort
                Collections.sort(bookings);
                System.out.println("  Sorted by Date and Start Time (Comparable):");
            }
            case 2 -> {
                // Comparator: BookingByResourceComparator
                bookings.sort(new BookingByResourceComparator());
                System.out.println("  Sorted by Resource Name (Comparator):");
            }
            case 3 -> {
                // Comparator: BookingByEmployeeComparator
                bookings.sort(new BookingByEmployeeComparator());
                System.out.println("  Sorted by Employee Name (Comparator):");
            }
            case 4 -> {
                // Comparator: BookingByStartTimeComparator
                bookings.sort(new BookingByStartTimeComparator());
                System.out.println("  Sorted by Start Time (Comparator):");
            }
        }

        System.out.println();
        for (Booking b : bookings) {
            System.out.println("  " + b);
        }
        System.out.println();
    }

    /**
     * Displays all registered employees.
     * Also demonstrates Set: shows all registered IDs from the Set.
     */
    private void handleViewEmployees() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("          REGISTERED USERS");
        System.out.println(SEPARATOR);

        System.out.println("  Registered IDs (Set<String>, no duplicates):");
        for (String id : registeredIds) {
            User user = userRepository.findById(id);
            if (user != null) {
                System.out.print("  " + user);
                System.out.print("  | ");
                user.displayRole();
            }
        }
        System.out.println();
    }

    // =========================================================================
    // MENU RENDERING
    // =========================================================================

    private void printMainMenu() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("       TECHNOVA RESOURCE BOOKING SYSTEM");
        System.out.println(SEPARATOR);
        System.out.println("  1. View Resources");
        System.out.println("  2. Create Booking");
        System.out.println("  3. View All Bookings");
        System.out.println("  4. View Approval Queue");
        System.out.println("  5. Process Approval");
        System.out.println("  6. Sort Bookings");
        System.out.println("  7. View Employees");
        System.out.println("  8. Exit");
        System.out.println(THIN_LINE);
    }

    // =========================================================================
    // INPUT HELPERS — all validate input properly
    // =========================================================================

    /**
     * Reads an integer from the user within the inclusive range [min, max].
     * Repeats until a valid integer is entered.
     * Demonstrates: try-catch for NumberFormatException, input validation loop.
     */
    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isBlank()) {
                System.out.println("  Input cannot be empty. Please enter a number.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("  Please enter a number between " + min + " and " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input: '" + input + "' is not a valid number.");
            }
        }
    }

    /**
     * Reads a non-blank string from the user.
     * Demonstrates: empty input validation.
     */
    private String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("  Input cannot be empty.");
        }
    }

    /**
     * Reads and parses a LocalDate from the user.
     * Demonstrates: try-catch for DateTimeParseException.
     * Returns null if the user enters an invalid date after 3 attempts.
     */
    private LocalDate readDate(String prompt) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use YYYY-MM-DD (e.g. 2026-10-06).");
                attempts++;
            }
        }
        System.out.println("  Too many invalid attempts. Returning to menu.");
        return null;
    }

    /**
     * Reads and parses a LocalTime from the user.
     * Returns null if the user enters an invalid time after 3 attempts.
     */
    private LocalTime readTime(String prompt) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalTime.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time format. Use HH:MM (e.g. 09:00).");
                attempts++;
            }
        }
        System.out.println("  Too many invalid attempts. Returning to menu.");
        return null;
    }
}
