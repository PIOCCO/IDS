package org.example.database.dao;

import org.example.database.DatabaseManager;
import org.example.models.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public AccountDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".accounts ORDER BY created_at ASC";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
            e.printStackTrace();
        }

        return accounts;
    }

    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM " + schema + ".accounts WHERE account_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAccountFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching account: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public boolean insertAccount(Account account) {
        String sql = "INSERT INTO " + schema + ".accounts (name, email, color) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getEmail());
            pstmt.setString(3, account.getColor());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAccount(int accountId, Account account) {
        String sql = "UPDATE " + schema + ".accounts SET name = ?, email = ?, color = ? WHERE account_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getName());
            pstmt.setString(2, account.getEmail());
            pstmt.setString(3, account.getColor());
            pstmt.setInt(4, accountId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAccount(int accountId) {
        String sql = "DELETE FROM " + schema + ".accounts WHERE account_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void initializeDefaultAccounts() {
        // Check if accounts exist
        if (getAllAccounts().isEmpty()) {
            // Insert default accounts
            insertAccount(new Account("Apple", "me@icloud.com", "#0078d4"));
            insertAccount(new Account("Dropbox", "dropbox@mail.com", "#4a90e2"));
            insertAccount(new Account("Facebook", "fb@company.com", "#3b5998"));
            insertAccount(new Account("Adobe", "adobe@mail.com", "#ff0000"));
            insertAccount(new Account("Amazon", "amazon@mail.com", "#ff9900"));
            insertAccount(new Account("Google", "google@mail.com", "#4285f4"));
            insertAccount(new Account("Ebay", "ebay@mail.com", "#e53238"));
            insertAccount(new Account("Yahoo", "yahoo@mail.com", "#6001d2"));
            System.out.println("Default accounts initialized");
        }
    }

    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String email = rs.getString("email");
        String color = rs.getString("color");

        return new Account(name, email, color);
    }
}