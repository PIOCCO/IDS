package org.example.dao.impl;

import org.example.utils.DatabaseManager;
import org.example.exception.DAOException;
import org.example.models.SecurityAlert;

import java.sql.*;
import java.util.*;

/**
 * DAO for Alert entity.
 */
public class AlertDAOImpl {

    private final DatabaseManager dbManager;
    private final String schema;

    // SQL Constants
    private static final String INSERT_SQL = "INSERT INTO %s.alerts (severity, alert_type, source_ip, destination_ip, description, status, direction) VALUES (?, ?, ?, ?, ?, 'Active', ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM %s.alerts ORDER BY created_at DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM %s.alerts WHERE alert_id = ?";
    private static final String SELECT_BY_SEVERITY_SQL = "SELECT * FROM %s.alerts WHERE severity = ? ORDER BY created_at DESC";
    private static final String SELECT_BY_DIRECTION_SQL = "SELECT * FROM %s.alerts WHERE direction = ? ORDER BY created_at DESC";
    private static final String SELECT_RECENT_SQL = "SELECT * FROM %s.alerts ORDER BY created_at DESC LIMIT ?";
    private static final String UPDATE_STATUS_SQL = "UPDATE %s.alerts SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE alert_id = ?";
    private static final String DELETE_SQL = "DELETE FROM %s.alerts WHERE alert_id = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM %s.alerts";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM %s.alerts";

    public AlertDAOImpl() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
        ensureDirectionColumn();
    }

    private void ensureDirectionColumn() {
        String checkSql = String.format(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = '%s' AND table_name = 'alerts' AND column_name = 'direction'",
                schema);
        String addSql = String.format(
                "ALTER TABLE %s.alerts ADD COLUMN IF NOT EXISTS direction VARCHAR(20) DEFAULT 'UNKNOWN'", schema);

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(checkSql)) {
            if (!rs.next()) {
                stmt.execute(addSql);
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not check/add direction column: " + e.getMessage());
        }
    }

    // ========== BaseDAO Implementation ==========
    public SecurityAlert save(SecurityAlert alert) {
        String sql = String.format(INSERT_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, alert.getSeverity());
            pstmt.setString(2, alert.getType());
            pstmt.setString(3, alert.getSourceIP());
            pstmt.setString(4, alert.getDestinationIP());
            pstmt.setString(5, alert.getDescription());
            pstmt.setString(6, alert.getDirection() != null ? alert.getDirection() : "UNKNOWN");

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    alert.setId(String.valueOf(rs.getInt(1)));
                }
            }
            return alert;
        } catch (SQLException e) {
            throw new DAOException("Error saving alert: " + e.getMessage(), e);
        }
    }
    public SecurityAlert update(SecurityAlert alert) {
        updateStatus(alert.getId(), alert.getStatus());
        return alert;
    }
    public boolean delete(Integer id) {
        String sql = String.format(DELETE_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error deleting alert: " + e.getMessage(), e);
        }
    }
    public Optional<SecurityAlert> findById(Integer id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractAlertFromResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DAOException("Error finding alert: " + e.getMessage(), e);
        }
    }
    public List<SecurityAlert> findAll() {
        List<SecurityAlert> results = new ArrayList<>();
        String sql = String.format(SELECT_ALL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching alerts: " + e.getMessage(), e);
        }
        return results;
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
            throw new DAOException("Error counting alerts: " + e.getMessage(), e);
        }
        return 0;
    }

    // ========== AlertDAO Specific Methods ==========
    public List<SecurityAlert> getBySeverity(String severity) {
        List<SecurityAlert> results = new ArrayList<>();
        String sql = String.format(SELECT_BY_SEVERITY_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, severity);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching alerts by severity: " + e.getMessage(), e);
        }
        return results;
    }
    public List<SecurityAlert> getByDirection(String direction) {
        List<SecurityAlert> results = new ArrayList<>();
        String sql = String.format(SELECT_BY_DIRECTION_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, direction);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching alerts by direction: " + e.getMessage(), e);
        }
        return results;
    }
    public List<SecurityAlert> getInboundThreats() {
        return getByDirection("INBOUND");
    }
    public List<SecurityAlert> getRecentAlerts(int limit) {
        List<SecurityAlert> results = new ArrayList<>();
        String sql = String.format(SELECT_RECENT_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(extractAlertFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching recent alerts: " + e.getMessage(), e);
        }
        return results;
    }
    public boolean updateStatus(String alertId, String newStatus) {
        String sql = String.format(UPDATE_STATUS_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, Integer.parseInt(alertId));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error updating alert status: " + e.getMessage(), e);
        }
    }
    public boolean deleteAlertById(String alertId) {
        return delete(Integer.parseInt(alertId));
    }
    public boolean deleteAllAlerts() {
        String sql = String.format(DELETE_ALL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            throw new DAOException("Error deleting all alerts: " + e.getMessage(), e);
        }
    }
    public int getCountBySeverity(String severity) {
        String sql = String.format("SELECT COUNT(*) FROM %s.alerts WHERE severity = ?", schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, severity);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting alerts by severity: " + e.getMessage());
        }
        return 0;
    }
    public int getInboundThreatCount() {
        String sql = String.format("SELECT COUNT(*) FROM %s.alerts WHERE direction = 'INBOUND'", schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting inbound threats: " + e.getMessage());
        }
        return 0;
    }
    public int getTotalAlertsCount() {
        return (int) count();
    }
    public Map<String, Integer> getStatsByDirection() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = String.format("SELECT direction, COUNT(*) as count FROM %s.alerts GROUP BY direction", schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("direction"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting alert stats: " + e.getMessage());
        }
        return stats;
    }

    // ========== Legacy Methods ==========

    public List<SecurityAlert> getAllAlerts() {
        return findAll();
    }

    public List<SecurityAlert> getAlertsBySeverity(String severity) {
        return getBySeverity(severity);
    }

    public List<SecurityAlert> getAlertsByDirection(String direction) {
        return getByDirection(direction);
    }

    public boolean insertAlert(SecurityAlert alert) {
        try {
            save(alert);
            return true;
        } catch (DAOException e) {
            System.err.println("Error inserting alert: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAlertStatus(String alertId, String newStatus) {
        return updateStatus(alertId, newStatus);
    }

    public boolean deleteAlert(String alertId) {
        return deleteAlertById(alertId);
    }

    public int getAlertCountBySeverity(String severity) {
        return getCountBySeverity(severity);
    }

    public Map<String, Integer> getAlertStatsByDirection() {
        return getStatsByDirection();
    }

    // ========== Helper Methods ==========

    private SecurityAlert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        java.time.LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now();

        SecurityAlert alert = new SecurityAlert(
                String.valueOf(rs.getInt("alert_id")),
                rs.getString("severity"),
                rs.getString("alert_type"),
                rs.getString("source_ip"),
                rs.getString("destination_ip"),
                rs.getString("description"),
                timestamp);

        alert.setStatus(rs.getString("status"));

        try {
            alert.setDirection(rs.getString("direction"));
        } catch (SQLException e) {
            alert.setDirection("UNKNOWN");
        }

        return alert;
    }
}
