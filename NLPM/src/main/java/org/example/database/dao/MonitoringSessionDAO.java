package org.example.database.dao;

import org.example.database.DatabaseManager;
import org.example.models.MonitoringSession;
import org.example.models.SessionSnapshot;
import org.example.models.SessionStatistics;
import org.example.models.SecurityAlert;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO for managing monitoring sessions and related data
 */
public class MonitoringSessionDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public MonitoringSessionDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Create a new monitoring session
     * 
     * @return the session ID, or -1 if failed
     */
    public int createSession(String sessionName, String interfaceName, String username) {
        String sql = "INSERT INTO " + schema + ".monitoring_sessions " +
                "(session_name, interface_name, start_time, status, created_by) " +
                "VALUES (?, ?, ?, 'ACTIVE', ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sessionName);
            pstmt.setString(2, interfaceName);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(4, username);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int sessionId = keys.getInt(1);
                    System.out.println("✅ Created monitoring session ID: " + sessionId);
                    return sessionId;
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("❌ Error creating session: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * End a monitoring session - set end_time, duration, and status
     */
    public boolean endSession(int sessionId) {
        String sql = "UPDATE " + schema + ".monitoring_sessions SET " +
                "end_time = ?, " +
                "duration_seconds = EXTRACT(EPOCH FROM (? - start_time))::INTEGER, " +
                "status = 'COMPLETED' " +
                "WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            pstmt.setTimestamp(1, now);
            pstmt.setTimestamp(2, now);
            pstmt.setInt(3, sessionId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Ended session ID: " + sessionId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Error ending session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get session by ID
     */
    public MonitoringSession getSessionById(int sessionId) {
        String sql = "SELECT * FROM " + schema + ".monitoring_sessions WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSessionFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching session: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all monitoring sessions
     */
    public List<MonitoringSession> getAllSessions() {
        List<MonitoringSession> sessions = new ArrayList<>();
        String sql = "SELECT ms.*, " +
                "COALESCE(ss.total_packets_captured, 0) as total_packets, " +
                "COALESCE(ss.total_alerts, 0) as total_alerts " +
                "FROM " + schema + ".monitoring_sessions ms " +
                "LEFT JOIN " + schema + ".session_statistics ss ON ms.session_id = ss.session_id " +
                "ORDER BY ms.start_time DESC";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MonitoringSession session = extractSessionFromResultSet(rs);
                session.setTotalPackets(rs.getLong("total_packets"));
                session.setTotalAlerts(rs.getInt("total_alerts"));
                sessions.add(session);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    /**
     * Get sessions by date range
     */
    public List<MonitoringSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<MonitoringSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".monitoring_sessions " +
                "WHERE start_time BETWEEN ? AND ? ORDER BY start_time DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(extractSessionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sessions by date: " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    /**
     * Delete a session and all related data (cascading)
     */
    public boolean deleteSession(int sessionId) {
        String sql = "DELETE FROM " + schema + ".monitoring_sessions WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Update or insert session statistics
     */
    public boolean updateSessionStatistics(int sessionId, SessionStatistics stats) {
        // Check if stats exist
        String checkSql = "SELECT stat_id FROM " + schema + ".session_statistics WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, sessionId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return updateStats(conn, sessionId, stats);
            } else {
                return insertStats(conn, sessionId, stats);
            }
        } catch (SQLException e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean insertStats(Connection conn, int sessionId, SessionStatistics stats) throws SQLException {
        String sql = "INSERT INTO " + schema + ".session_statistics (" +
                "session_id, total_packets_captured, total_bytes_processed, " +
                "tcp_packets, udp_packets, icmp_packets, http_packets, https_packets, dns_packets, ssh_packets, other_packets, "
                +
                "total_alerts, critical_alerts, high_alerts, medium_alerts, low_alerts, info_alerts, " +
                "inbound_packets, outbound_packets, local_packets, " +
                "port_scan_alerts, ddos_alerts, suspicious_port_alerts, brute_force_alerts, other_threats, " +
                "average_packet_size, peak_packet_rate, average_packet_rate" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatsParameters(pstmt, sessionId, stats);
            return pstmt.executeUpdate() > 0;
        }
    }

    private boolean updateStats(Connection conn, int sessionId, SessionStatistics stats) throws SQLException {
        String sql = "UPDATE " + schema + ".session_statistics SET " +
                "total_packets_captured = ?, total_bytes_processed = ?, " +
                "tcp_packets = ?, udp_packets = ?, icmp_packets = ?, http_packets = ?, https_packets = ?, dns_packets = ?, ssh_packets = ?, other_packets = ?, "
                +
                "total_alerts = ?, critical_alerts = ?, high_alerts = ?, medium_alerts = ?, low_alerts = ?, info_alerts = ?, "
                +
                "inbound_packets = ?, outbound_packets = ?, local_packets = ?, " +
                "port_scan_alerts = ?, ddos_alerts = ?, suspicious_port_alerts = ?, brute_force_alerts = ?, other_threats = ?, "
                +
                "average_packet_size = ?, peak_packet_rate = ?, average_packet_rate = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE session_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int idx = 1;
            pstmt.setLong(idx++, stats.getTotalPacketsCaptured());
            pstmt.setLong(idx++, stats.getTotalBytesProcessed());
            pstmt.setInt(idx++, stats.getTcpPackets());
            pstmt.setInt(idx++, stats.getUdpPackets());
            pstmt.setInt(idx++, stats.getIcmpPackets());
            pstmt.setInt(idx++, stats.getHttpPackets());
            pstmt.setInt(idx++, stats.getHttpsPackets());
            pstmt.setInt(idx++, stats.getDnsPackets());
            pstmt.setInt(idx++, stats.getSshPackets());
            pstmt.setInt(idx++, stats.getOtherPackets());
            pstmt.setInt(idx++, stats.getTotalAlerts());
            pstmt.setInt(idx++, stats.getCriticalAlerts());
            pstmt.setInt(idx++, stats.getHighAlerts());
            pstmt.setInt(idx++, stats.getMediumAlerts());
            pstmt.setInt(idx++, stats.getLowAlerts());
            pstmt.setInt(idx++, stats.getInfoAlerts());
            pstmt.setInt(idx++, stats.getInboundPackets());
            pstmt.setInt(idx++, stats.getOutboundPackets());
            pstmt.setInt(idx++, stats.getLocalPackets());
            pstmt.setInt(idx++, stats.getPortScanAlerts());
            pstmt.setInt(idx++, stats.getDdosAlerts());
            pstmt.setInt(idx++, stats.getSuspiciousPortAlerts());
            pstmt.setInt(idx++, stats.getBruteForceAlerts());
            pstmt.setInt(idx++, stats.getOtherThreats());
            pstmt.setDouble(idx++, stats.getAveragePacketSize());
            pstmt.setInt(idx++, stats.getPeakPacketRate());
            pstmt.setDouble(idx++, stats.getAveragePacketRate());
            pstmt.setInt(idx, sessionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private void setStatsParameters(PreparedStatement pstmt, int sessionId, SessionStatistics stats)
            throws SQLException {
        int idx = 1;
        pstmt.setInt(idx++, sessionId);
        pstmt.setLong(idx++, stats.getTotalPacketsCaptured());
        pstmt.setLong(idx++, stats.getTotalBytesProcessed());
        pstmt.setInt(idx++, stats.getTcpPackets());
        pstmt.setInt(idx++, stats.getUdpPackets());
        pstmt.setInt(idx++, stats.getIcmpPackets());
        pstmt.setInt(idx++, stats.getHttpPackets());
        pstmt.setInt(idx++, stats.getHttpsPackets());
        pstmt.setInt(idx++, stats.getDnsPackets());
        pstmt.setInt(idx++, stats.getSshPackets());
        pstmt.setInt(idx++, stats.getOtherPackets());
        pstmt.setInt(idx++, stats.getTotalAlerts());
        pstmt.setInt(idx++, stats.getCriticalAlerts());
        pstmt.setInt(idx++, stats.getHighAlerts());
        pstmt.setInt(idx++, stats.getMediumAlerts());
        pstmt.setInt(idx++, stats.getLowAlerts());
        pstmt.setInt(idx++, stats.getInfoAlerts());
        pstmt.setInt(idx++, stats.getInboundPackets());
        pstmt.setInt(idx++, stats.getOutboundPackets());
        pstmt.setInt(idx++, stats.getLocalPackets());
        pstmt.setInt(idx++, stats.getPortScanAlerts());
        pstmt.setInt(idx++, stats.getDdosAlerts());
        pstmt.setInt(idx++, stats.getSuspiciousPortAlerts());
        pstmt.setInt(idx++, stats.getBruteForceAlerts());
        pstmt.setInt(idx++, stats.getOtherThreats());
        pstmt.setDouble(idx++, stats.getAveragePacketSize());
        pstmt.setInt(idx++, stats.getPeakPacketRate());
        pstmt.setDouble(idx, stats.getAveragePacketRate());
    }

    /**
     * Get statistics for a session
     */
    public SessionStatistics getSessionStatistics(int sessionId) {
        String sql = "SELECT * FROM " + schema + ".session_statistics WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStatisticsFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ==================== SNAPSHOTS ====================

    /**
     * Insert a session snapshot for time-series data
     */
    public boolean insertSnapshot(SessionSnapshot snapshot) {
        String sql = "INSERT INTO " + schema + ".session_snapshots " +
                "(session_id, snapshot_time, packets_count, bytes_count, alerts_count, packet_rate, tcp_count, udp_count, http_count) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, snapshot.getSessionId());
            pstmt.setTimestamp(2, Timestamp.valueOf(snapshot.getSnapshotTime()));
            pstmt.setInt(3, snapshot.getPacketsCount());
            pstmt.setLong(4, snapshot.getBytesCount());
            pstmt.setInt(5, snapshot.getAlertsCount());
            pstmt.setInt(6, snapshot.getPacketRate());
            pstmt.setInt(7, snapshot.getTcpCount());
            pstmt.setInt(8, snapshot.getUdpCount());
            pstmt.setInt(9, snapshot.getHttpCount());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting snapshot: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all snapshots for a session
     */
    public List<SessionSnapshot> getSessionSnapshots(int sessionId) {
        List<SessionSnapshot> snapshots = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".session_snapshots " +
                "WHERE session_id = ? ORDER BY snapshot_time ASC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                snapshots.add(extractSnapshotFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching snapshots: " + e.getMessage());
            e.printStackTrace();
        }
        return snapshots;
    }

    // ==================== ALERT LINKING ====================

    /**
     * Link an alert to a session
     */
    public boolean linkAlertToSession(int sessionId, SecurityAlert alert) {
        String sql = "INSERT INTO " + schema + ".session_alerts_summary " +
                "(session_id, alert_id, severity, alert_type, source_ip, destination_ip, direction, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            // alert.getId() returns String, store it as integer if possible, else 0
            int alertId = 0;
            try {
                alertId = Integer.parseInt(alert.getId());
            } catch (NumberFormatException ignored) {
            }
            pstmt.setInt(2, alertId);
            pstmt.setString(3, alert.getSeverity());
            pstmt.setString(4, alert.getType());
            pstmt.setString(5, alert.getSourceIP());
            pstmt.setString(6, alert.getDestinationIP());
            pstmt.setString(7, alert.getDirection());
            pstmt.setTimestamp(8, Timestamp.valueOf(alert.getTimestamp()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error linking alert to session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get count of alerts linked to a session
     */
    public int getSessionAlertCount(int sessionId) {
        String sql = "SELECT COUNT(*) FROM " + schema + ".session_alerts_summary WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting session alerts: " + e.getMessage());
        }
        return 0;
    }

    // ==================== AGGREGATIONS FOR CHARTS ====================

    /**
     * Get sessions count by day for line chart
     */
    public Map<LocalDate, Long> getSessionsByDay(LocalDateTime start, LocalDateTime end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(start_time) as day, COUNT(*) as count " +
                "FROM " + schema + ".monitoring_sessions " +
                "WHERE start_time BETWEEN ? AND ? " +
                "GROUP BY DATE(start_time) ORDER BY day";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate day = rs.getDate("day").toLocalDate();
                result.put(day, rs.getLong("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sessions by day: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get global protocol distribution across all sessions
     */
    public Map<String, Long> getGlobalProtocolDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = "SELECT " +
                "SUM(tcp_packets) as tcp, " +
                "SUM(udp_packets) as udp, " +
                "SUM(http_packets) as http, " +
                "SUM(https_packets) as https, " +
                "SUM(dns_packets) as dns, " +
                "SUM(icmp_packets) as icmp, " +
                "SUM(ssh_packets) as ssh, " +
                "SUM(other_packets) as other " +
                "FROM " + schema + ".session_statistics";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                if (rs.getLong("tcp") > 0)
                    result.put("TCP", rs.getLong("tcp"));
                if (rs.getLong("udp") > 0)
                    result.put("UDP", rs.getLong("udp"));
                if (rs.getLong("http") > 0)
                    result.put("HTTP", rs.getLong("http"));
                if (rs.getLong("https") > 0)
                    result.put("HTTPS", rs.getLong("https"));
                if (rs.getLong("dns") > 0)
                    result.put("DNS", rs.getLong("dns"));
                if (rs.getLong("icmp") > 0)
                    result.put("ICMP", rs.getLong("icmp"));
                if (rs.getLong("ssh") > 0)
                    result.put("SSH", rs.getLong("ssh"));
                if (rs.getLong("other") > 0)
                    result.put("Other", rs.getLong("other"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting protocol distribution: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get alerts by day for bar chart
     */
    public Map<LocalDate, Integer> getAlertsByDay(LocalDateTime start, LocalDateTime end) {
        Map<LocalDate, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(created_at) as day, COUNT(*) as count " +
                "FROM " + schema + ".session_alerts_summary " +
                "WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY DATE(created_at) ORDER BY day";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate day = rs.getDate("day").toLocalDate();
                result.put(day, rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting alerts by day: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get total packets across all sessions
     */
    public long getTotalPacketsAcrossAllSessions() {
        String sql = "SELECT COALESCE(SUM(total_packets_captured), 0) FROM " + schema + ".session_statistics";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total packets: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get total alerts across all sessions
     */
    public int getTotalAlertsAcrossAllSessions() {
        String sql = "SELECT COALESCE(SUM(total_alerts), 0) FROM " + schema + ".session_statistics";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total alerts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get average session duration in seconds
     */
    public double getAverageSessionDuration() {
        String sql = "SELECT AVG(duration_seconds) FROM " + schema + ".monitoring_sessions WHERE status = 'COMPLETED'";
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting average duration: " + e.getMessage());
        }
        return 0;
    }

    // ==================== HELPER METHODS ====================

    private MonitoringSession extractSessionFromResultSet(ResultSet rs) throws SQLException {
        MonitoringSession session = new MonitoringSession();
        session.setSessionId(rs.getInt("session_id"));
        session.setSessionName(rs.getString("session_name"));
        session.setInterfaceName(rs.getString("interface_name"));

        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null)
            session.setStartTime(startTs.toLocalDateTime());

        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null)
            session.setEndTime(endTs.toLocalDateTime());

        session.setDurationSeconds(rs.getObject("duration_seconds") != null ? rs.getInt("duration_seconds") : null);
        session.setStatus(rs.getString("status"));
        session.setCreatedBy(rs.getString("created_by"));
        session.setNotes(rs.getString("notes"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null)
            session.setCreatedAt(createdTs.toLocalDateTime());

        return session;
    }

    private SessionStatistics extractStatisticsFromResultSet(ResultSet rs) throws SQLException {
        SessionStatistics stats = new SessionStatistics();
        stats.setStatId(rs.getInt("stat_id"));
        stats.setSessionId(rs.getInt("session_id"));
        stats.setTotalPacketsCaptured(rs.getLong("total_packets_captured"));
        stats.setTotalBytesProcessed(rs.getLong("total_bytes_processed"));
        stats.setTcpPackets(rs.getInt("tcp_packets"));
        stats.setUdpPackets(rs.getInt("udp_packets"));
        stats.setIcmpPackets(rs.getInt("icmp_packets"));
        stats.setHttpPackets(rs.getInt("http_packets"));
        stats.setHttpsPackets(rs.getInt("https_packets"));
        stats.setDnsPackets(rs.getInt("dns_packets"));
        stats.setSshPackets(rs.getInt("ssh_packets"));
        stats.setOtherPackets(rs.getInt("other_packets"));
        stats.setTotalAlerts(rs.getInt("total_alerts"));
        stats.setCriticalAlerts(rs.getInt("critical_alerts"));
        stats.setHighAlerts(rs.getInt("high_alerts"));
        stats.setMediumAlerts(rs.getInt("medium_alerts"));
        stats.setLowAlerts(rs.getInt("low_alerts"));
        stats.setInfoAlerts(rs.getInt("info_alerts"));
        stats.setInboundPackets(rs.getInt("inbound_packets"));
        stats.setOutboundPackets(rs.getInt("outbound_packets"));
        stats.setLocalPackets(rs.getInt("local_packets"));
        stats.setPortScanAlerts(rs.getInt("port_scan_alerts"));
        stats.setDdosAlerts(rs.getInt("ddos_alerts"));
        stats.setSuspiciousPortAlerts(rs.getInt("suspicious_port_alerts"));
        stats.setBruteForceAlerts(rs.getInt("brute_force_alerts"));
        stats.setOtherThreats(rs.getInt("other_threats"));
        stats.setAveragePacketSize(rs.getDouble("average_packet_size"));
        stats.setPeakPacketRate(rs.getInt("peak_packet_rate"));
        stats.setAveragePacketRate(rs.getDouble("average_packet_rate"));

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null)
            stats.setUpdatedAt(updatedTs.toLocalDateTime());

        return stats;
    }

    private SessionSnapshot extractSnapshotFromResultSet(ResultSet rs) throws SQLException {
        SessionSnapshot snapshot = new SessionSnapshot();
        snapshot.setSnapshotId(rs.getInt("snapshot_id"));
        snapshot.setSessionId(rs.getInt("session_id"));

        Timestamp snapshotTs = rs.getTimestamp("snapshot_time");
        if (snapshotTs != null)
            snapshot.setSnapshotTime(snapshotTs.toLocalDateTime());

        snapshot.setPacketsCount(rs.getInt("packets_count"));
        snapshot.setBytesCount(rs.getLong("bytes_count"));
        snapshot.setAlertsCount(rs.getInt("alerts_count"));
        snapshot.setPacketRate(rs.getInt("packet_rate"));
        snapshot.setTcpCount(rs.getInt("tcp_count"));
        snapshot.setUdpCount(rs.getInt("udp_count"));
        snapshot.setHttpCount(rs.getInt("http_count"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null)
            snapshot.setCreatedAt(createdTs.toLocalDateTime());

        return snapshot;
    }
}
