package com.technova.booking;

import com.technova.booking.model.User;
import com.technova.booking.repository.InMemoryRepository;
import com.technova.booking.service.ApprovalService;
import com.technova.booking.service.BookingService;
import com.technova.booking.service.ResourceService;
import com.technova.booking.ui.ConsoleMenu;
import com.technova.booking.util.ConfigFileLoader;
import com.technova.booking.util.DataInitializer;

/**
 * Application entry point.
 *
 * Responsibilities (Single Responsibility Principle):
 *  1. Bootstrap all components (repositories, services, initialiser)
 *  2. Delegate data initialisation to DataInitializer
 *  3. Hand control to ConsoleMenu
 *
 * Main does NOT contain business logic.
 */
public class Main {

    public static void main(String[] args) {

        // ---- 1. Print startup banner (demonstrates try-with-resources via ConfigFileLoader) ----
        ConfigFileLoader configLoader = new ConfigFileLoader();
        configLoader.printBanner();

        // ---- 2. Create repositories ----
        //
        // InMemoryRepository<Resource, String> — Map<String, Resource>
        //   key   = resourceId  (String)
        //   value = Resource    (abstract — actual objects are MeetingRoom / Equipment)
        //
        // InMemoryRepository<User, String> — Map<String, User>
        //   key   = userId  (String)
        //   value = User    (abstract — actual objects are Employee / Approver / Admin)
        //
        // HashMap chosen internally because lookups by ID are the primary operation.
        // No ordering requirement for the resource store.

        InMemoryRepository<com.technova.booking.model.Resource, String> resourceRepository =
                new InMemoryRepository<>();
        InMemoryRepository<User, String> userRepository =
                new InMemoryRepository<>();

        // ---- 3. Create services ----
        ResourceService resourceService = new ResourceService(resourceRepository);
        BookingService bookingService = new BookingService(resourceService);
        ApprovalService approvalService = new ApprovalService(bookingService);

        // ---- 4. Load sample data ----
        DataInitializer initializer = new DataInitializer();
        initializer.initResources(resourceService);
        initializer.initUsers(userRepository);

        System.out.println();
        System.out.println("  System initialised. "
                + resourceService.getAllResources().size() + " resources loaded.");
        System.out.println();

        // ---- 5. Start the console menu ----
        ConsoleMenu menu = new ConsoleMenu(
                resourceService,
                bookingService,
                approvalService,
                userRepository,
                initializer.getRegisteredEmployeeIds()
        );
        menu.start();
    }
}
