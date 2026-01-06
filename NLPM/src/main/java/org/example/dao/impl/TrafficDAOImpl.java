package org.example.dao.impl;

import org.example.utils.DatabaseManager;
import org.example.exception.DAOException;
import org.example.models.TrafficData;

import java.sql.*;
import java.util.*;

/**
 * DAO for Traffic entity.
 * Handles traffic log operations.
 */
public class TrafficDAOImpl {

    private final DatabaseManager dbManager;
    private final String schema;

    // SQL Constants
    private static final String INSERT_SQL = "INSERT INTO %s.traffic_logs (protocol, source_ip, source_port, destination_ip, destination_port, packet_size, status, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM %s.traffic_logs ORDER BY timestamp DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM %s.traffic_logs WHERE log_id = ?";
    private static final String SELECT_BY_PROTOCOL_SQL = "SELECT * FROM %s.traffic_logs WHERE protocol = ? ORDER BY timestamp DESC";
    private static final String SELECT_RECENT_SQL = "SELECT * FROM %s.traffic_logs WHERE timestamp > NOW() - INTERVAL '%d minutes' ORDER BY timestamp DESC";
    private static final String DELETE_ALL_SQL = "DELETE FROM %s.traffic_logs";
    private static final String DELETE_OLD_SQL = "DELETE FROM %s.traffic_logs WHERE timestamp < NOW() - INTERVAL '%d days'";
    private static final String DELETE_BY_PROTOCOL_SQL = "DELETE FROM %s.traffic_logs WHERE protocol = ?";
    private static final String DELETE_BY_IP_SQL = "DELETE FROM %s.traffic_logs WHERE source_ip = ? OR destination_ip = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM %s.traffic_logs";

    public TrafficDAOImpl() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    // ========== BaseDAO Implementation ==========
    public TrafficData save(TrafficData traffic) {
        String sql = String.format(INSERT_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, traffic.getProtocol());
            pstmt.setString(2, traffic.getSourceIP());
            pstmt.setInt(3, traffic.getSourcePortAsInt());
            pstmt.setString(4, traffic.getDestinationIP());
            pstmt.setInt(5, traffic.getDestinationPortAsInt());
            pstmt.setLong(6, traffic.getPacketSize());
            pstmt.setString(7, traffic.getStatus());
            pstmt.setTimestamp(8,
                    traffic.getTimestampAsLocalDateTime() != null
                            ? Timestamp.valueOf(traffic.getTimestampAsLocalDateTime())
                            : new Timestamp(System.currentTimeMillis()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    traffic.setLogId(rs.getLong(1));
                }
            }
            return traffic;
        } catch (SQLException e) {
            throw new DAOException("Error saving traffic: " + e.getMessage(), e);
        }
    }
    public TrafficData update(TrafficData entity) {
        throw new UnsupportedOperationException("Traffic records are immutable");
    }
    public boolean delete(Long id) {
        String sql = String.format("DELETE FROM %s.traffic_logs WHERE log_id = ?", schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error deleting traffic: " + e.getMessage(), e);
        }
    }
    public Optional<TrafficData> findById(Long id) {
        String sql = String.format(SELECT_BY_ID_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(extractTrafficFromResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DAOException("Error finding traffic: " + e.getMessage(), e);
        }
    }
    public List<TrafficData> findAll() {
        List<TrafficData> results = new ArrayList<>();
        String sql = String.format(SELECT_ALL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(extractTrafficFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching traffic: " + e.getMessage(), e);
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
            throw new DAOException("Error counting traffic: " + e.getMessage(), e);
        }
        return 0;
    }

    // ========== TrafficDAO Specific Methods ==========
    public List<TrafficData> getByProtocol(String protocol) {
        List<TrafficData> results = new ArrayList<>();
        String sql = String.format(SELECT_BY_PROTOCOL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, protocol);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(extractTrafficFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching traffic by protocol: " + e.getMessage(), e);
        }
        return results;
    }
    public List<TrafficData> getRecentTraffic(int minutes) {
        List<TrafficData> results = new ArrayList<>();
        String sql = String.format(SELECT_RECENT_SQL, schema, minutes);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(extractTrafficFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error fetching recent traffic: " + e.getMessage(), e);
        }
        return results;
    }
    public boolean deleteAllTraffic() {
        String sql = String.format(DELETE_ALL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            throw new DAOException("Error deleting all traffic: " + e.getMessage(), e);
        }
    }
    public int deleteOldTraffic(int days) {
        String sql = String.format(DELETE_OLD_SQL, schema, days);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DAOException("Error deleting old traffic: " + e.getMessage(), e);
        }
    }
    public int deleteByProtocol(String protocol) {
        String sql = String.format(DELETE_BY_PROTOCOL_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, protocol);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error deleting traffic by protocol: " + e.getMessage(), e);
        }
    }
    public int deleteByIP(String ipAddress) {
        String sql = String.format(DELETE_BY_IP_SQL, schema);
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ipAddress);
            pstmt.setString(2, ipAddress);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error deleting traffic by IP: " + e.getMessage(), e);
        }
    }
    public long getTotalPacketsAnalyzed() {
        return count();
    }
    public int getActiveConnectionsCount() {
        String sql = String.format(
                "SELECT COUNT(DISTINCT source_ip || ':' || source_port || '-' || destination_ip || ':' || destination_port) "
                        +
                        "FROM %s.traffic_logs WHERE timestamp > NOW() - INTERVAL '5 minutes'",
                schema);
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active connections: " + e.getMessage());
        }
        return 0;
    }
    public Map<String, Object> getTrafficStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPackets", getTotalPacketsAnalyzed());
        stats.put("activeConnections", getActiveConnectionsCount());

        // Protocol distribution
        String sql = String.format("SELECT protocol, COUNT(*) as count FROM %s.traffic_logs GROUP BY protocol", schema);
        Map<String, Long> protocolCounts = new HashMap<>();
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                protocolCounts.put(rs.getString("protocol"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting protocol stats: " + e.getMessage());
        }
        stats.put("protocolDistribution", protocolCounts);

        return stats;
    }

    // ========== Legacy Methods ==========

    public List<TrafficData> getAllTraffic() {
        return findAll();
    }

    public List<TrafficData> getTrafficByProtocol(String protocol) {
        return getByProtocol(protocol);
    }

    public boolean insertTraffic(TrafficData traffic) {
        try {
            save(traffic);
            return true;
        } catch (DAOException e) {
            System.err.println("Error inserting traffic: " + e.getMessage());
            return false;
        }
    }

    public int deleteTrafficByProtocol(String protocol) {
        return deleteByProtocol(protocol);
    }

    public int deleteTrafficByIP(String ipAddress) {
        return deleteByIP(ipAddress);
    }

    // ========== Helper Methods ==========

    private TrafficData extractTrafficFromResultSet(ResultSet rs) throws SQLException {
        TrafficData td = new TrafficData();
        td.setLogId(rs.getLong("log_id"));
        td.setProtocol(rs.getString("protocol"));
        td.setSourceIP(rs.getString("source_ip"));
        td.setSourcePort(rs.getInt("source_port"));
        td.setDestinationIP(rs.getString("destination_ip"));
        td.setDestinationPort(rs.getInt("destination_port"));
        td.setPacketSize(rs.getLong("packet_size"));
        td.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            td.setTimestamp(ts.toLocalDateTime());
        }
        return td;
    }
}
