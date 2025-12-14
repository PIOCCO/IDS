package org.example.database.dao;

import org.example.database.DatabaseManager;
import org.example.models.SecurityAlert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public AlertDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    public List<SecurityAlert> getAllAlerts() {
        List<SecurityAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".alerts ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching alerts: " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    public List<SecurityAlert> getAlertsBySeverity(String severity) {
        List<SecurityAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".alerts WHERE severity = ? ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, severity);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching alerts by severity: " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    public List<SecurityAlert> getRecentAlerts(int limit) {
        List<SecurityAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".alerts ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recent alerts: " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    public boolean insertAlert(SecurityAlert alert) {
        String sql = "INSERT INTO " + schema + ".alerts " +
                "(severity, alert_type, source_ip, destination_ip, description, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alert.getSeverity());
            pstmt.setString(2, alert.getType());
            pstmt.setString(3, alert.getSourceIP());
            pstmt.setString(4, alert.getDestinationIP());
            pstmt.setString(5, alert.getDescription());
            pstmt.setString(6, alert.getStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(alert.getTimestamp()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting alert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAlertStatus(String alertId, String newStatus) {
        String sql = "UPDATE " + schema + ".alerts SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE alert_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, Integer.parseInt(alertId.replace("ALT-", "")));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating alert status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAlert(String alertId) {
        String sql = "DELETE FROM " + schema + ".alerts WHERE alert_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(alertId.replace("ALT-", "")));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting alert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int getAlertCountBySeverity(String severity) {
        String sql = "SELECT COUNT(*) FROM " + schema + ".alerts WHERE severity = ? AND status = 'Active'";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, severity);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting alerts: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public int getTotalAlertsCount() {
        String sql = "SELECT COUNT(*) FROM " + schema + ".alerts";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total alerts: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    private SecurityAlert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        String id = "ALT-" + String.format("%05d", rs.getInt("alert_id"));
        String severity = rs.getString("severity");
        String type = rs.getString("alert_type");
        String sourceIP = rs.getString("source_ip");
        String destIP = rs.getString("destination_ip");
        String description = rs.getString("description");
        LocalDateTime timestamp = rs.getTimestamp("created_at").toLocalDateTime();

        SecurityAlert alert = new SecurityAlert(id, severity, type, sourceIP, destIP, description, timestamp);
        alert.setStatus(rs.getString("status"));
        return alert;
    }
}