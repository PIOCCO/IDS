package org.example.database.dao.interfaces;

import org.example.database.dao.base.BaseDAO;
import org.example.models.User;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for User entity operations.
 * Extends BaseDAO with User-specific methods.
 */
public interface UserDAO extends BaseDAO<User, Integer> {

    /**
     * Find user by username.
     *
     * @param username Username to search
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     *
     * @param email Email to search
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Get all users with a specific role.
     *
     * @param role Role to filter by
     * @return List of users with the role
     */
    List<User> findByRole(String role);

    /**
     * Get all active users.
     *
     * @return List of active users
     */
    List<User> findActiveUsers();

    /**
     * Check if username already exists.
     *
     * @param username Username to check
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists.
     *
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Update user's active status.
     *
     * @param username Username
     * @param isActive New status
     * @return true if updated
     */
    boolean updateStatus(String username, boolean isActive);

    /**
     * Update user's email only.
     *
     * @param username Username
     * @param email    New email
     * @return true if updated
     */
    boolean updateEmail(String username, String email);

    /**
     * Update user's last login timestamp.
     *
     * @param username Username
     * @return true if updated
     */
    boolean updateLastLogin(String username);
}
