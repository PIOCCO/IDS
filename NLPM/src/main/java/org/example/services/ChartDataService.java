package org.example.services;

import org.example.database.DatabaseManager;
import org.example.models.ChartMetric;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for retrieving time-series chart data from the database.
 * Provides optimized queries for the Grafana-style charting system.
 */
public class ChartDataService {

    private final DatabaseManager dbManager;

    public ChartDataService() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Retrieve data for a single metric over a time range with specified intervals.
     * Uses optimized GROUP BY query for performance.
     */
    public Map<LocalDateTime, Double> getMetricData(
            ChartMetric metric,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int intervalMinutes) {

        Map<LocalDateTime, Double> dataPoints = new TreeMap<>();

        if (metric == null || metric.getSqlQuery() == null) {
            return dataPoints;
        }

        try (Connection conn = dbManager.getConnection()) {
            // For each interval, execute the metric's SQL query
            LocalDateTime currentTime = startTime;
            while (currentTime.isBefore(endTime)) {
                LocalDateTime intervalEnd = currentTime.plusMinutes(intervalMinutes);
                if (intervalEnd.isAfter(endTime)) {
                    intervalEnd = endTime;
                }

                double value = executeMetricQuery(conn, metric.getSqlQuery(),
                        currentTime, intervalEnd);
                dataPoints.put(currentTime, value);

                currentTime = intervalEnd;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching metric data for " + metric.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return dataPoints;
    }

    /**
     * Retrieve data for multiple metrics simultaneously.
     * Uses parallel execution for better performance.
     */
    public Map<ChartMetric, Map<LocalDateTime, Double>> getMultiMetricData(
            List<ChartMetric> metrics,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int intervalMinutes) {

        Map<ChartMetric, Map<LocalDateTime, Double>> allData = new ConcurrentHashMap<>();

        if (metrics == null || metrics.isEmpty()) {
            return allData;
        }

        // Execute in parallel for better performance
        metrics.parallelStream()
                .filter(ChartMetric::isEnabled)
                .forEach(metric -> {
                    try {
                        Map<LocalDateTime, Double> metricData = getMetricData(
                                metric, startTime, endTime, intervalMinutes);
                        allData.put(metric, metricData);
                    } catch (Exception e) {
                        System.err.println("Error fetching data for " + metric.getId() + ": " + e.getMessage());
                        allData.put(metric, new TreeMap<>());
                    }
                });

        return allData;
    }

    /**
     * Get the current value of a metric (last minute).
     */
    public double getCurrentValue(ChartMetric metric) {
        if (metric == null || metric.getSqlQuery() == null) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        try (Connection conn = dbManager.getConnection()) {
            return executeMetricQuery(conn, metric.getSqlQuery(), oneMinuteAgo, now);
        } catch (SQLException e) {
            System.err.println("Error getting current value for " + metric.getId() + ": " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate optimal interval based on time range to target ~50-100 data points.
     */
    public int calculateOptimalInterval(int timeRangeMinutes) {
        if (timeRangeMinutes <= 15)
            return 1; // 15 points max
        else if (timeRangeMinutes <= 60)
            return 2; // 30 points
        else if (timeRangeMinutes <= 180)
            return 5; // 36 points
        else if (timeRangeMinutes <= 360)
            return 10; // 36 points
        else if (timeRangeMinutes <= 720)
            return 15; // 48 points
        else
            return 30; // 48 points for 24h
    }

    /**
     * Get time range in minutes from a selection string.
     */
    public int getTimeRangeMinutes(String selection) {
        return switch (selection) {
            case "Last 5 minutes" -> 5;
            case "Last 15 minutes" -> 15;
            case "Last 30 minutes" -> 30;
            case "Last 1 hour" -> 60;
            case "Last 3 hours" -> 180;
            case "Last 6 hours" -> 360;
            case "Last 12 hours" -> 720;
            case "Last 24 hours" -> 1440;
            default -> 60;
        };
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * Execute a metric SQL query and return the result.
     */
    private double executeMetricQuery(Connection conn, String sqlQuery,
            LocalDateTime start, LocalDateTime end) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : value;
                }
            }
        }

        return 0.0;
    }

    /**
     * Fill missing intervals with zero values.
     */
    public void fillMissingIntervals(Map<LocalDateTime, Double> dataPoints,
            LocalDateTime start,
            LocalDateTime end,
            int intervalMinutes) {
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            dataPoints.putIfAbsent(current, 0.0);
            current = current.plusMinutes(intervalMinutes);
        }
    }

    // ==================== AGGREGATION METHODS ====================

    /**
     * Get protocol distribution for a time range.
     */
    public Map<String, Long> getProtocolDistribution(LocalDateTime start, LocalDateTime end) {
        Map<String, Long> distribution = new LinkedHashMap<>();

        String sql = "SELECT protocol, COUNT(*) as count FROM ids.traffic_logs " +
                "WHERE timestamp BETWEEN ? AND ? " +
                "GROUP BY protocol ORDER BY count DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    distribution.put(rs.getString("protocol"), rs.getLong("count"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting protocol distribution: " + e.getMessage());
        }

        return distribution;
    }

    /**
     * Get alert severity distribution for a time range.
     */
    public Map<String, Long> getAlertSeverityDistribution(LocalDateTime start, LocalDateTime end) {
        Map<String, Long> distribution = new LinkedHashMap<>();

        String sql = "SELECT severity, COUNT(*) as count FROM ids.alerts " +
                "WHERE timestamp BETWEEN ? AND ? " +
                "GROUP BY severity ORDER BY count DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    distribution.put(rs.getString("severity"), rs.getLong("count"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting alert severity distribution: " + e.getMessage());
        }

        return distribution;
    }

    /**
     * Get session-specific traffic data for Reports page.
     */
    public Map<LocalDateTime, Double> getSessionTrafficData(int sessionId, int intervalMinutes) {
        Map<LocalDateTime, Double> dataPoints = new TreeMap<>();

        // First, get session time range
        String sessionSql = "SELECT start_time, end_time FROM ids.monitoring_sessions WHERE session_id = ?";

        try (Connection conn = dbManager.getConnection()) {
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;

            try (PreparedStatement pstmt = conn.prepareStatement(sessionSql)) {
                pstmt.setInt(1, sessionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Timestamp startTs = rs.getTimestamp("start_time");
                        Timestamp endTs = rs.getTimestamp("end_time");

                        startTime = startTs != null ? startTs.toLocalDateTime() : null;
                        endTime = endTs != null ? endTs.toLocalDateTime() : LocalDateTime.now();
                    }
                }
            }

            if (startTime == null) {
                return dataPoints;
            }

            // Get packet counts per interval for this session
            String dataSql = "SELECT COUNT(*) FROM ids.traffic_logs " +
                    "WHERE session_id = ? AND timestamp BETWEEN ? AND ?";

            LocalDateTime current = startTime;
            while (current.isBefore(endTime)) {
                LocalDateTime intervalEnd = current.plusMinutes(intervalMinutes);
                if (intervalEnd.isAfter(endTime)) {
                    intervalEnd = endTime;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(dataSql)) {
                    pstmt.setInt(1, sessionId);
                    pstmt.setTimestamp(2, Timestamp.valueOf(current));
                    pstmt.setTimestamp(3, Timestamp.valueOf(intervalEnd));

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            dataPoints.put(current, (double) rs.getLong(1));
                        }
                    }
                }

                current = intervalEnd;
            }

        } catch (SQLException e) {
            System.err.println("Error getting session traffic data: " + e.getMessage());
        }

        return dataPoints;
    }
}
