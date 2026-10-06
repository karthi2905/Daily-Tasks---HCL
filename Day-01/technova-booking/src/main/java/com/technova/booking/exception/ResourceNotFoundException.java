package com.technova.booking.exception;

/**
 * Unchecked exception thrown when a requested resource cannot be found.
 *
 * Extends RuntimeException (unchecked) — callers are not forced to catch it,
 * but it must still be handled meaningfully by the UI layer.
 *
 * Demonstrates: unchecked exceptions, RuntimeException hierarchy.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceId;

    /**
     * Constructs a ResourceNotFoundException for the given resource ID.
     *
     * @param resourceId the ID that was not found
     */
    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: " + resourceId);
        this.resourceId = resourceId;
    }

    /**
     * Constructs a ResourceNotFoundException with a custom message.
     * Method overloading: same constructor name, different parameter list.
     *
     * @param resourceId the ID that was not found
     * @param message    custom error message
     */
    public ResourceNotFoundException(String resourceId, String message) {
        super(message);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
