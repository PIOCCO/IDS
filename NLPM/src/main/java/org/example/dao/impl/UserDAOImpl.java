package org.example.dao.impl;

import org.example.utils.DatabaseManager;
import org.example.exception.DAOException;
import org.example.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for User entity.
 * Handles all database operations for users.
 */
public class UserDAOImpl {

    private final DatabaseManager dbManager;
    private final String schema;

    // ========================================
    // SQL QUERIES (Constants)
    // ========================================
    private static final String INSERT_SQL = "INSERT INTO %s.users (username, password_hash, role, email, is_active) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = "UPDATE %s.users SET email = ?, password_hash = ? WHERE username = ?";

    private static final String DELETE_SQL = "DELETE FROM %s.users WHERE user_id = ?";

    private static final String DELETE_BY_USERNAME_SQL = "DELETE FROM %s.users WHERE username = ?";

    private static final String SELECT_BY_ID_SQL = "SELECT * FROM %s.users WHERE user_id = ?";

    private static final String SELECT_ALL_SQL = "SELECT * FROM %s.users ORDER BY created_at DESC";

    private static final String SELECT_BY_USERNAME_SQL = "SELECT * FROM %s.users WHERE username = ?";

    private static final String SELECT_BY_EMAIL_SQL = "SELECT * FROM %s.users WHERE email = ?";

    private static final String SELECT_BY_ROLE_SQL = "SELECT * FROM %s.users WHERE role = ? ORDER BY username";

    private static final String SELECT_ACTIVE_SQL = "SELECT * FROM %s.users WHERE is_active = true ORDER BY username";

    private static final String EXISTS_BY_USERNAME_SQL = "SELECT COUNT(*) FROM %s.users WHERE username = ?";

    private static final String EXISTS_BY_EMAIL_SQL = "SELECT COUNT(*) FROM %s.users WHERE email = ?";

    private static final String UPDATE_STATUS_SQL = "UPDATE %s.users SET is_active = ? WHERE username = ?";

    private static final String UPDATE_EMAIL_SQL = "UPDATE %s.users SET email = ? WHERE username = ?";

    private static final String UPDATE_LAST_LOGIN_SQL = "UPDATE %s.users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM %s.users";

    // ========================================
    // CONSTRUCTOR
    // ========================================

    public UserDAOImpl() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    // ========================================
    // BaseDAO IMPLEMENTATION
    // ========================================
    public User save(User user) {
        String sql = String.format(INSERT_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setBoolean(5, user.isActive());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Creating user failed, no rows affected");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }

            return user;

        } catch (SQLException e) {
            throw new DAOException("Error saving user: " + e.getMessage(), e);
        }
    }
    public User update(User user) {
        String sql = String.format(UPDATE_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getUsername());

            int result = pstmt.executeUpdate();
            System.out.println("updateUser: rows affected = " + result + " for user: " + user.getUsername());

            return user;

        } catch (SQLException e) {
            throw new DAOException("Error updating user: " + e.getMessage(), e);
        }
    }
    public boolean delete(Integer id) {
        String sql = String.format(DELETE_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error deleting user: " + e.getMessage(), e);
        }
    }

    /**
     * Delete user by username (legacy support).
     */
    public boolean deleteByUsername(String username) {
        String sql = String.format(DELETE_BY_USERNAME_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error deleting user: " + e.getMessage(), e);
        }
    }
    public Optional<User> findById(Integer id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DAOException("Error finding user by ID: " + e.getMessage(), e);
        }
    }
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = String.format(SELECT_ALL_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error fetching all users: " + e.getMessage(), e);
        }

        return users;
    }
    public long count() {
        String sql = String.format(COUNT_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            throw new DAOException("Error counting users: " + e.getMessage(), e);
        }

        return 0;
    }

    // ========================================
    // UserDAO SPECIFIC METHODS
    // ========================================
    public Optional<User> findByUsername(String username) {
        String sql = String.format(SELECT_BY_USERNAME_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DAOException("Error finding user by username: " + e.getMessage(), e);
        }
    }
    public Optional<User> findByEmail(String email) {
        String sql = String.format(SELECT_BY_EMAIL_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DAOException("Error finding user by email: " + e.getMessage(), e);
        }
    }
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = String.format(SELECT_BY_ROLE_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error finding users by role: " + e.getMessage(), e);
        }

        return users;
    }
    public List<User> findActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = String.format(SELECT_ACTIVE_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new DAOException("Error finding active users: " + e.getMessage(), e);
        }

        return users;
    }
    public boolean existsByUsername(String username) {
        String sql = String.format(EXISTS_BY_USERNAME_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error checking username existence: " + e.getMessage(), e);
        }

        return false;
    }
    public boolean existsByEmail(String email) {
        String sql = String.format(EXISTS_BY_EMAIL_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new DAOException("Error checking email existence: " + e.getMessage(), e);
        }

        return false;
    }
    public boolean updateStatus(String username, boolean isActive) {
        String sql = String.format(UPDATE_STATUS_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isActive);
            pstmt.setString(2, username);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error updating user status: " + e.getMessage(), e);
        }
    }
    public boolean updateEmail(String username, String email) {
        String sql = String.format(UPDATE_EMAIL_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, username);

            int result = pstmt.executeUpdate();
            System.out.println("updateUserEmail: rows affected = " + result + " for user: " + username);

            return result > 0;

        } catch (SQLException e) {
            throw new DAOException("Error updating user email: " + e.getMessage(), e);
        }
    }
    public boolean updateLastLogin(String username) {
        String sql = String.format(UPDATE_LAST_LOGIN_SQL, schema);

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error updating last login: " + e.getMessage(), e);
        }
    }

    // ========================================
    // LEGACY METHODS (for backward compatibility)
    // ========================================

    /**
     * Legacy method - use findByUsername instead.
     */
    public User getUserByUsername(String username) {
        return findByUsername(username).orElse(null);
    }

    /**
     * Legacy method - use findAll instead.
     */
    public List<User> getAllUsers() {
        return findAll();
    }

    /**
     * Legacy method - use save instead.
     */
    public boolean insertUser(User user) {
        try {
            save(user);
            return true;
        } catch (DAOException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Legacy method - use deleteByUsername instead.
     */
    public boolean deleteUser(String username) {
        return deleteByUsername(username);
    }

    /**
     * Legacy method - use updateStatus instead.
     */
    public boolean updateUserStatus(String username, boolean isActive) {
        return updateStatus(username, isActive);
    }

    /**
     * Legacy method - use update instead.
     */
    public boolean updateUser(User user) {
        try {
            update(user);
            return true;
        } catch (DAOException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Legacy method - use updateEmail instead.
     */
    public boolean updateUserEmail(String username, String email) {
        return updateEmail(username, email);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String role = rs.getString("role");

        User user = new User(username, passwordHash, role);
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }
}
