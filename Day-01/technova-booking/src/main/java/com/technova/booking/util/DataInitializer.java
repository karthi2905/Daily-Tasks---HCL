package com.technova.booking.util;

import com.technova.booking.model.Admin;
import com.technova.booking.model.Approver;
import com.technova.booking.model.Employee;
import com.technova.booking.model.Equipment;
import com.technova.booking.model.MeetingRoom;
import com.technova.booking.model.Resource;
import com.technova.booking.model.User;
import com.technova.booking.repository.InMemoryRepository;
import com.technova.booking.service.ResourceService;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialises all in-memory sample data for the TechNova application.
 *
 * Demonstrates:
 *  - Set<String> for unique employee IDs — duplicates are silently rejected by HashSet
 *  - Map<String, Resource> populated via InMemoryRepository (backed by HashMap)
 *  - Multiple concrete User and Resource subclasses (polymorphism)
 *
 * Separation of concern: data initialisation lives here, not in Main.
 */
public class DataInitializer {

    /**
     * Set<String> — stores registered employee IDs.
     * HashSet is chosen because:
     *  - Membership testing (contains) is O(1) on average.
     *  - Duplicates are automatically rejected — Set contract guarantees uniqueness.
     *  - Order of IDs is not needed.
     * Demonstrates: Set collection to prevent duplicate employee IDs.
     */
    private final Set<String> registeredEmployeeIds = new HashSet<>();

    /**
     * Populates the resource repository with the standard TechNova resources.
     *
     * @param resourceService the service to register resources with
     */
    public void initResources(ResourceService resourceService) {
        resourceService.addResource(
                new MeetingRoom("RM001", "Conference Room A", false, 10, "Block A - Floor 1"));
        resourceService.addResource(
                new MeetingRoom("RM002", "Conference Room B", false, 8, "Block A - Floor 2"));
        resourceService.addResource(
                new MeetingRoom("RM003", "Training Room", false, 20, "Block B - Floor 1"));
        resourceService.addResource(
                new MeetingRoom("LAB01", "Innovation Lab", true, 15, "Block C - Floor 3"));
        resourceService.addResource(
                new Equipment("EQ001", "Projector 1", false, "HD Projector"));
        resourceService.addResource(
                new Equipment("EQ002", "Projector 2", false, "4K Projector"));
    }

    /**
     * Populates the user repository with the standard TechNova users.
     * Also seeds the registeredEmployeeIds Set to demonstrate uniqueness enforcement.
     *
     * @param userRepository the repository to register users in
     */
    public void initUsers(InMemoryRepository<User, String> userRepository) {
        registerUser(userRepository, new Employee("E101", "Karthik",
                "karthik@technova.com", "Engineering"));
        registerUser(userRepository, new Employee("E102", "Arun",
                "arun@technova.com", "Product"));
        registerUser(userRepository, new Approver("A101", "Priya",
                "priya@technova.com", "Senior"));
        registerUser(userRepository, new Admin("AD01", "Admin User",
                "admin@technova.com", "SYS-ADM"));

        // Demonstrate Set uniqueness: attempting to add E101 again is silently ignored
        boolean added = registeredEmployeeIds.add("E101");
        System.out.println("[Init] Attempted duplicate registration of E101 — "
                + (added ? "added (unexpected)" : "correctly rejected by Set"));
    }

    /**
     * Registers a user in both the repository and the uniqueness Set.
     * If the ID is already in the Set, the user is not registered again.
     *
     * @param repository the user repository
     * @param user       the user to register
     */
    private void registerUser(InMemoryRepository<User, String> repository, User user) {
        boolean isNew = registeredEmployeeIds.add(user.getId());
        if (isNew) {
            repository.save(user.getId(), user);
        } else {
            System.out.println("[Init] Duplicate ID detected: " + user.getId()
                    + " — registration skipped.");
        }
    }

    /**
     * Returns the Set of registered employee IDs.
     * Used by the UI layer to validate employee input.
     */
    public Set<String> getRegisteredEmployeeIds() {
        return registeredEmployeeIds;
    }
}
