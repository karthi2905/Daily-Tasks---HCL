package com.technova.booking.repository;

import java.util.List;

/**
 * Generic repository interface.
 *
 * Demonstrates: generics with two type parameters — T (entity type) and ID (key type).
 * This interface decouples the service layer from specific storage implementations.
 *
 * @param <T>  the type of entity managed by this repository
 * @param <ID> the type of the entity's identifier
 */
public interface Repository<T, ID> {

    /**
     * Saves (inserts or updates) an entity.
     *
     * @param entity the entity to save
     */
    void save(T entity);

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the identifier to look up
     * @return the entity, or null if not found
     */
    T findById(ID id);

    /**
     * Returns all entities currently stored.
     *
     * @return immutable view of all entities
     */
    List<T> findAll();

    /**
     * Removes an entity by its identifier.
     *
     * @param id the identifier of the entity to remove
     */
    void delete(ID id);
}
