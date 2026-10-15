package com.technova.generics.resource;

/**
 * A mock AutoCloseable resource that logs its lifecycle events (open, work, close).
 *
 * Demonstrates:
 * 1. Automatic resource management via try-with-resources.
 * 2. Deterministic LIFO (reverse declaration) closing order.
 * 3. Guaranteed execution of {@link #close()} even if the try block aborts with an exception.
 * 4. Handling of suppressed exceptions when {@link #close()} throws while an in-flight exception is propagating.
 */
public class TrackedResource implements AutoCloseable {

    private final String name;
    private final boolean throwOnClose;
    private boolean closed = false;

    /**
     * Constructs a resource that closes normally without throwing.
     */
    public TrackedResource(String name) {
        this(name, false);
    }

    /**
     * Constructs a resource with configurable close failure behavior.
     *
     * @param name         descriptive name of the resource
     * @param throwOnClose if true, close() throws an exception to demonstrate suppressed exceptions
     */
    public TrackedResource(String name, boolean throwOnClose) {
        this.name = name;
        this.throwOnClose = throwOnClose;
        System.out.printf("  Opening: %s%n", name);
    }

    public String getName() {
        return name;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Simulates performing meaningful work with this resource.
     */
    public void doWork(String action) {
        if (closed) {
            throw new IllegalStateException(String.format("Cannot perform '%s': %s is already closed!", action, name));
        }
        System.out.printf("    [%s] Performing: %s%n", name, action);
    }

    @Override
    public void close() throws Exception {
        this.closed = true;
        System.out.printf("  Closing: %s%n", name);

        if (throwOnClose) {
            throw new Exception(String.format("Simulated close failure in resource '%s'", name));
        }
    }

    @Override
    public String toString() {
        return String.format("TrackedResource[%s, closed=%b]", name, closed);
    }
}
