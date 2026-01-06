package org.example.services;

import org.example.database.dao.DAOFactory;
import org.example.database.dao.interfaces.UserDAO;
import org.example.exception.DataNotFoundException;
import org.example.exception.DuplicateDataException;
import org.example.models.User;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for User operations.
 * Provides business logic and uses DAOFactory for data access.
 */
public class UserService {

    private static UserService instance;
    private final UserDAO userDAO;

    private UserService() {
        this.userDAO = (UserDAO) DAOFactory.getInstance().getUserDAO();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    // ========== CRUD Operations ==========

    public User createUser(String username, String password, String role, String email)
            throws DuplicateDataException {
        if (userDAO.existsByUsername(username)) {
            throw new DuplicateDataException("Username already exists: " + username);
        }
        if (email != null && !email.isEmpty() && userDAO.existsByEmail(email)) {
            throw new DuplicateDataException("Email already exists: " + email);
        }

        User user = new User(username, password, role);
        user.setEmail(email);
        return userDAO.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public Optional<User> findById(Integer id) {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getActiveUsers() {
        return userDAO.findActiveUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userDAO.findByRole(role);
    }

    public User updateUser(User user) {
        return userDAO.update(user);
    }

    public boolean updateEmail(String username, String newEmail) throws DataNotFoundException {
        if (userDAO.findByUsername(username).isEmpty()) {
            throw new DataNotFoundException("User not found: " + username);
        }
        return userDAO.updateEmail(username, newEmail);
    }

    public boolean updateStatus(String username, boolean isActive) throws DataNotFoundException {
        if (userDAO.findByUsername(username).isEmpty()) {
            throw new DataNotFoundException("User not found: " + username);
        }
        return userDAO.updateStatus(username, isActive);
    }

    public boolean deleteUser(String username) {
        // Use the impl's deleteByUsername method via casting
        return ((org.example.database.dao.impl.UserDAOImpl) userDAO).deleteByUsername(username);
    }

    // ========== Business Logic ==========

    public boolean usernameExists(String username) {
        return userDAO.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userDAO.existsByEmail(email);
    }

    public void recordLogin(String username) {
        userDAO.updateLastLogin(username);
    }

    public long getUserCount() {
        return userDAO.count();
    }
}
