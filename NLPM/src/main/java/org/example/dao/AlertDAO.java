package org.example.dao;

import org.example.utils.DatabaseManager;
import org.example.models.SecurityAlert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AlertDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public AlertDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
        ensureDirectionColumn();
    }

    /**
     * Ensure the direction column exists in the alerts table
     */
    private void ensureDirectionColumn() {
        String checkColumnSQL = "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = 'alerts' AND column_name = 'direction'";

        String addColumnSQL = "ALTER TABLE " + schema + ".alerts " +
                "ADD COLUMN IF NOT EXISTS direction VARCHAR(20) DEFAULT 'UNKNOWN'";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkColumnSQL)) {

            checkStmt.setString(1, schema);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                // Column doesn't exist, add it
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(addColumnSQL);
                    System.out.println("Added 'direction' column to alerts table");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking/adding direction column: " + e.getMessage());
        }
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

    public List<SecurityAlert> getAlertsByDirection(String direction) {
        List<SecurityAlert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".alerts WHERE direction = ? ORDER BY created_at DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, direction);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching alerts by direction: " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    public List<SecurityAlert> getInboundThreats() {
        return getAlertsByDirection("INBOUND");
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
                "(severity, alert_type, source_ip, destination_ip, description, status, direction, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alert.getSeverity());
            pstmt.setString(2, alert.getType());
            pstmt.setString(3, alert.getSourceIP());
            pstmt.setString(4, alert.getDestinationIP());
            pstmt.setString(5, alert.getDescription());
            pstmt.setString(6, alert.getStatus());
            pstmt.setString(7, alert.getDirection());
            pstmt.setTimestamp(8, Timestamp.valueOf(alert.getTimestamp()));

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

    public boolean deleteAllAlerts() {
        String sql = "DELETE FROM " + schema + ".alerts";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            System.out.println("All alerts deleted from database");
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting all alerts: " + e.getMessage());
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

    public int getInboundThreatCount() {
        String sql = "SELECT COUNT(*) FROM " + schema + ".alerts WHERE direction = 'INBOUND' AND status = 'Active'";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting inbound threats: " + e.getMessage());
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

    public Map<String, Integer> getAlertStatsByDirection() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT direction, COUNT(*) as count FROM " + schema + ".alerts " +
                "WHERE status = 'Active' GROUP BY direction";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("direction"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting alert stats by direction: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
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

        // Handle direction column (may not exist in older databases)
        try {
            String direction = rs.getString("direction");
            if (direction != null) {
                alert.setDirection(direction);
            }
        } catch (SQLException e) {
            // Direction column doesn't exist, use default
            alert.setDirection("UNKNOWN");
        }

        return alert;
    }
}