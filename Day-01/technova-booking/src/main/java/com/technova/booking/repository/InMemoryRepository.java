package com.technova.booking.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic in-memory repository backed by a HashMap.
 *
 * Demonstrates:
 *  - Generic class with bounded type parameters (T, ID)
 *  - Map<ID, T> — efficient O(1) lookup by key
 *  - HashMap chosen because key insertion order is irrelevant here
 *    (we look up resources by ID, not iterate in order)
 *  - Encapsulation: the internal map is private and final
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    /**
     * HashMap<ID, T> is used here because:
     *  - Resource lookups by ID are O(1) on average.
     *  - Ordering by insertion is not a requirement for this lookup table.
     *
     * (A LinkedHashMap would be used if insertion-order iteration were needed.)
     */
    private final Map<ID, T> data = new HashMap<>();

    @Override
    public void save(T entity) {
        // Subclasses or callers supply a key extractor; this simplified
        // version requires the caller to use the keyed variant below.
        throw new UnsupportedOperationException(
                "Use save(ID, T) for InMemoryRepository.");
    }

    /**
     * Saves an entity under the given key.
     * Method overloading: same name as save(T), different signature.
     *
     * @param id     the key
     * @param entity the value
     */
    public void save(ID id, T entity) {
        data.put(id, entity);
    }

    @Override
    public T findById(ID id) {
        return data.get(id);
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(data.values()));
    }

    @Override
    public void delete(ID id) {
        data.remove(id);
    }

    /**
     * Returns the number of entities currently stored.
     */
    public int size() {
        return data.size();
    }

    /**
     * Returns true if the given ID already exists in the store.
     *
     * @param id the key to check
     */
    public boolean exists(ID id) {
        return data.containsKey(id);
    }
}
