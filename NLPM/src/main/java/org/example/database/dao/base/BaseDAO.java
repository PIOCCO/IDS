package org.example.database.dao.base;

import java.util.List;
import java.util.Optional;

/**
 * Generic Base DAO interface providing standard CRUD operations.
 * All entity-specific DAOs should extend this interface.
 *
 * @param <T>  Entity type
 * @param <ID> Primary key type
 */
public interface BaseDAO<T, ID> {

    /**
     * Save a new entity to the database.
     *
     * @param entity Entity to save
     * @return Saved entity with generated ID
     */
    T save(T entity);

    /**
     * Update an existing entity.
     *
     * @param entity Entity to update
     * @return Updated entity
     */
    T update(T entity);

    /**
     * Delete an entity by ID.
     *
     * @param id Entity ID
     * @return true if deleted, false otherwise
     */
    boolean delete(ID id);

    /**
     * Find entity by ID.
     *
     * @param id Entity ID
     * @return Optional containing entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities.
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Count total entities.
     *
     * @return Total count
     */
    long count();
}
