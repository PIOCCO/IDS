package org.example.dao;

import org.example.utils.DatabaseManager;
import org.example.models.TrafficData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrafficDAO {
    private final DatabaseManager dbManager;
    private final String schema;

    public TrafficDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.schema = dbManager.getSchema();
    }

    public List<TrafficData> getAllTraffic() {
        List<TrafficData> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".traffic_logs ORDER BY timestamp DESC LIMIT 1000";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                trafficList.add(extractTrafficFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching traffic data: " + e.getMessage());
            e.printStackTrace();
        }

        return trafficList;
    }

    public List<TrafficData> getTrafficByProtocol(String protocol) {
        List<TrafficData> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".traffic_logs WHERE protocol = ? ORDER BY timestamp DESC LIMIT 500";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, protocol);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                trafficList.add(extractTrafficFromResultSet(rs));
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Error fetching traffic by protocol: " + e.getMessage());
            e.printStackTrace();
        }

        return trafficList;
    }

    public List<TrafficData> getRecentTraffic(int minutes) {
        List<TrafficData> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".traffic_logs " +
                "WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '" + minutes + " minutes' " +
                "ORDER BY timestamp DESC";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                trafficList.add(extractTrafficFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recent traffic: " + e.getMessage());
            e.printStackTrace();
        }

        return trafficList;
    }

    public boolean insertTraffic(TrafficData traffic) {
        String sql = "INSERT INTO " + schema + ".traffic_logs " +
                "(protocol, source_ip, source_port, destination_ip, destination_port, packet_size, status, timestamp) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, traffic.getProtocol());
            pstmt.setString(2, traffic.getSourceIP());

            // Handle port parsing safely
            try {
                pstmt.setInt(3, Integer.parseInt(traffic.getSourcePort()));
            } catch (NumberFormatException e) {
                pstmt.setInt(3, 0);
            }

            pstmt.setString(4, traffic.getDestinationIP());

            try {
                pstmt.setInt(5, Integer.parseInt(traffic.getDestinationPort()));
            } catch (NumberFormatException e) {
                pstmt.setInt(5, 0);
            }

            pstmt.setLong(6, traffic.getPacketSize());
            pstmt.setString(7, traffic.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting traffic data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete all traffic data from the database
     * WARNING: This is a destructive operation
     *
     * @return true if successful, false otherwise
     */
    public boolean deleteAllTraffic() {
        String sql = "DELETE FROM " + schema + ".traffic_logs";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {

            int rowsDeleted = stmt.executeUpdate(sql);
            System.out.println("Deleted " + rowsDeleted + " traffic records from database");
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting traffic data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete traffic data older than specified days
     *
     * @param days Number of days to keep
     * @return Number of records deleted
     */
    public int deleteOldTraffic(int days) {
        String sql = "DELETE FROM " + schema + ".traffic_logs " +
                "WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '" + days + " days'";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {

            int rowsDeleted = stmt.executeUpdate(sql);
            System.out.println("Deleted " + rowsDeleted + " old traffic records (older than " + days + " days)");
            return rowsDeleted;
        } catch (SQLException e) {
            System.err.println("Error deleting old traffic data: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Delete traffic data by protocol
     *
     * @param protocol Protocol to delete
     * @return Number of records deleted
     */
    public int deleteTrafficByProtocol(String protocol) {
        String sql = "DELETE FROM " + schema + ".traffic_logs WHERE protocol = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, protocol);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsDeleted + " " + protocol + " traffic records");
            return rowsDeleted;
        } catch (SQLException e) {
            System.err.println("Error deleting traffic by protocol: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Delete traffic data by IP address
     *
     * @param ipAddress IP address to delete traffic for
     * @return Number of records deleted
     */
    public int deleteTrafficByIP(String ipAddress) {
        String sql = "DELETE FROM " + schema + ".traffic_logs " +
                "WHERE source_ip = ? OR destination_ip = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ipAddress);
            pstmt.setString(2, ipAddress);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsDeleted + " traffic records for IP: " + ipAddress);
            return rowsDeleted;
        } catch (SQLException e) {
            System.err.println("Error deleting traffic by IP: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public long getTotalPacketsAnalyzed() {
        String sql = "SELECT COUNT(*) FROM " + schema + ".traffic_logs";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting packets: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public int getActiveConnectionsCount() {
        String sql = "SELECT COUNT(DISTINCT source_ip) FROM " + schema + ".traffic_logs " +
                "WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '5 minutes'";

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting active connections: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get traffic statistics for dashboard
     *
     * @return Map containing various statistics
     */
    public java.util.Map<String, Object> getTrafficStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement()) {

            // Total packets
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM " + schema + ".traffic_logs")) {
                if (rs.next()) {
                    stats.put("totalPackets", rs.getLong(1));
                }
            }

            // Packets in last hour
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM " + schema + ".traffic_logs " +
                            "WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '1 hour'")) {
                if (rs.next()) {
                    stats.put("packetsLastHour", rs.getLong(1));
                }
            }

            // Total bytes
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT SUM(packet_size) FROM " + schema + ".traffic_logs")) {
                if (rs.next()) {
                    stats.put("totalBytes", rs.getLong(1));
                }
            }

            // Most common protocol
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT protocol, COUNT(*) as count FROM " + schema + ".traffic_logs " +
                            "GROUP BY protocol ORDER BY count DESC LIMIT 1")) {
                if (rs.next()) {
                    stats.put("topProtocol", rs.getString("protocol"));
                    stats.put("topProtocolCount", rs.getLong("count"));
                }
            }

            // Active connections
            stats.put("activeConnections", getActiveConnectionsCount());

        } catch (SQLException e) {
            System.err.println("Error getting traffic statistics: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    private TrafficData extractTrafficFromResultSet(ResultSet rs) throws SQLException {
        String protocol = rs.getString("protocol");
        String sourceIP = rs.getString("source_ip");
        String sourcePort = String.valueOf(rs.getInt("source_port"));
        String destIP = rs.getString("destination_ip");
        String destPort = String.valueOf(rs.getInt("destination_port"));
        long packetSize = rs.getLong("packet_size");

        // Safely handle timestamp
        String timestamp;
        try {
            Timestamp ts = rs.getTimestamp("timestamp");
            if (ts != null) {
                timestamp = ts.toLocalDateTime().toLocalTime().toString();
            } else {
                timestamp = "N/A";
            }
        } catch (Exception e) {
            timestamp = "N/A";
        }

        String status = rs.getString("status");
        if (status == null) {
            status = "Unknown";
        }

        return new TrafficData(protocol, sourceIP, sourcePort, destIP, destPort, packetSize, timestamp, status);
    }
}