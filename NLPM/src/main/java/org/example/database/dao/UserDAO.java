package org.example.database.dao;

import org.example.database.DatabaseManager;
import org.example.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".users ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM " + schema + ".users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean insertUser(User user) {
        String sql = "INSERT INTO " + schema + ".users (username, password_hash, role, email, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Already hashed
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setBoolean(5, user.isActive());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(String username) {
        String sql = "DELETE FROM " + schema + ".users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserStatus(String username, boolean isActive) {
        String sql = "UPDATE " + schema + ".users SET is_active = ? WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isActive);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE " + schema + ".users SET email = ?, password_hash = ? WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword()); // Already hashed
            pstmt.setString(3, user.getUsername());
            int result = pstmt.executeUpdate();
            System.out.println("updateUser: rows affected = " + result + " for user: " + user.getUsername());
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update only the email field (does not change password)
     */
    public boolean updateUserEmail(String username, String email) {
        String sql = "UPDATE " + schema + ".users SET email = ? WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, username);
            int result = pstmt.executeUpdate();
            System.out.println(
                    "updateUserEmail: rows affected = " + result + " for user: " + username + ", email: " + email);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String role = rs.getString("role");

        User user = new User(username, passwordHash, role);
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }
}