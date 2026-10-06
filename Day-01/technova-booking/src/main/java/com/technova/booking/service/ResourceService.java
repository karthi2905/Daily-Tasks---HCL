package com.technova.booking.service;

import com.technova.booking.exception.ResourceNotFoundException;
import com.technova.booking.model.MeetingRoom;
import com.technova.booking.model.Resource;
import com.technova.booking.repository.InMemoryRepository;

import java.util.List;

/**
 * Service responsible for resource management.
 *
 * Demonstrates:
 *  - Map (via InMemoryRepository backed by HashMap) for O(1) resource lookup
 *  - Bounded generics: the printResources method accepts any List<E extends Resource>
 *  - ResourceNotFoundException (unchecked) when a resource is not found
 *  - Service layer pattern: business logic separate from UI
 */
public class ResourceService {

    /**
     * InMemoryRepository<Resource, String> — maps resourceId (String) to Resource.
     * Using HashMap internally, which is ideal when ordering is not required
     * and fast key-based retrieval is the primary operation.
     */
    private final InMemoryRepository<Resource, String> resourceRepository;

    public ResourceService(InMemoryRepository<Resource, String> resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    /**
     * Registers a resource in the system.
     *
     * @param resource the resource to add
     */
    public void addResource(Resource resource) {
        resourceRepository.save(resource.getResourceId(), resource);
    }

    /**
     * Looks up a resource by ID.
     * Throws ResourceNotFoundException (unchecked) if not found.
     *
     * @param resourceId the ID to look up
     * @return the found resource
     * @throws ResourceNotFoundException if the ID does not exist
     */
    public Resource findResource(String resourceId) {
        Resource resource = resourceRepository.findById(resourceId);
        if (resource == null) {
            throw new ResourceNotFoundException(resourceId);
        }
        return resource;
    }

    /**
     * Returns all resources currently registered.
     */
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * Prints details for each resource in the provided list.
     *
     * Demonstrates: bounded generic method — accepts any List whose element
     * type E extends Resource (e.g., List<MeetingRoom>, List<Equipment>, List<Resource>).
     *
     * @param <E>       any subtype of Resource
     * @param resources the list of resources to display
     */
    public <E extends Resource> void printResources(List<E> resources) {
        if (resources.isEmpty()) {
            System.out.println("  No resources found.");
            return;
        }
        System.out.printf("  %-6s | %-25s | %-9s | %-30s | %s%n",
                "ID", "Name", "Type", "Details", "Status");
        System.out.println("  " + "-".repeat(90));
        for (E resource : resources) {
            resource.displayDetails();    // polymorphic dispatch
        }
    }
}
