package org.example.database.dao;

import org.example.database.DatabaseManager;
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
                "(protocol, source_ip, source_port, destination_ip, destination_port, packet_size, status, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, traffic.getProtocol());
            pstmt.setString(2, traffic.getSourceIP());
            pstmt.setInt(3, Integer.parseInt(traffic.getSourcePort()));
            pstmt.setString(4, traffic.getDestinationIP());
            pstmt.setInt(5, Integer.parseInt(traffic.getDestinationPort()));
            pstmt.setLong(6, traffic.getPacketSize());
            pstmt.setString(7, traffic.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting traffic data: " + e.getMessage());
            e.printStackTrace();
            return false;
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

    private TrafficData extractTrafficFromResultSet(ResultSet rs) throws SQLException {
        String protocol = rs.getString("protocol");
        String sourceIP = rs.getString("source_ip");
        String sourcePort = String.valueOf(rs.getInt("source_port"));
        String destIP = rs.getString("destination_ip");
        String destPort = String.valueOf(rs.getInt("destination_port"));
        long packetSize = rs.getLong("packet_size");
        String timestamp = rs.getTimestamp("timestamp").toLocalDateTime().toLocalTime().toString();
        String status = rs.getString("status");

        return new TrafficData(protocol, sourceIP, sourcePort, destIP, destPort, packetSize, timestamp, status);
    }
}