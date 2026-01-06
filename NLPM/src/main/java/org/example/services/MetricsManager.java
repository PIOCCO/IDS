package org.example.services;

import org.example.models.ChartMetric;
import org.example.models.ChartMetric.MetricType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton manager for all available chart metrics.
 * Provides access to predefined metrics for the Grafana-style charting system.
 */
public class MetricsManager {

        private static volatile MetricsManager instance;
        private final List<ChartMetric> availableMetrics;

        // Schema prefix for SQL queries
        private static final String SCHEMA = "ids";

        private MetricsManager() {
                this.availableMetrics = new ArrayList<>();
                initializeMetrics();
        }

        /**
         * Get singleton instance (double-check locking for thread safety)
         */
        public static MetricsManager getInstance() {
                if (instance == null) {
                        synchronized (MetricsManager.class) {
                                if (instance == null) {
                                        instance = new MetricsManager();
                                }
                        }
                }
                return instance;
        }

        /**
         * Initialize all available metrics by category
         */
        private void initializeMetrics() {
                addProtocolMetrics();
                addAlertMetrics();
                addSessionMetrics();
                addTrafficMetrics();

                System.out.println("📊 MetricsManager initialized with " + availableMetrics.size() + " metrics");
        }

        // ==================== PROTOCOL METRICS ====================

        private void addProtocolMetrics() {
                String baseQuery = "SELECT COUNT(*) FROM " + SCHEMA + ".traffic_logs " +
                                "WHERE protocol = '%s' AND timestamp BETWEEN ? AND ?";

                // TCP Traffic - Blue
                availableMetrics.add(new ChartMetric(
                                "tcp_traffic",
                                "TCP Traffic",
                                "Protocol",
                                "#5294e0",
                                String.format(baseQuery, "TCP"),
                                MetricType.COUNT));

                // UDP Traffic - Orange
                availableMetrics.add(new ChartMetric(
                                "udp_traffic",
                                "UDP Traffic",
                                "Protocol",
                                "#ff7f0e",
                                String.format(baseQuery, "UDP"),
                                MetricType.COUNT));

                // HTTP Traffic - Green
                availableMetrics.add(new ChartMetric(
                                "http_traffic",
                                "HTTP Traffic",
                                "Protocol",
                                "#73bf69",
                                String.format(baseQuery, "HTTP"),
                                MetricType.COUNT));

                // DNS Queries - Cyan
                availableMetrics.add(new ChartMetric(
                                "dns_queries",
                                "DNS Queries",
                                "Protocol",
                                "#5dc9e2",
                                String.format(baseQuery, "DNS"),
                                MetricType.COUNT));

                // ICMP Traffic - Rose
                availableMetrics.add(new ChartMetric(
                                "icmp_traffic",
                                "ICMP Traffic",
                                "Protocol",
                                "#ff5896",
                                String.format(baseQuery, "ICMP"),
                                MetricType.COUNT));
        }

        // ==================== ALERT METRICS ====================

        private void addAlertMetrics() {
                String severityQuery = "SELECT COUNT(*) FROM " + SCHEMA + ".alerts " +
                                "WHERE severity = '%s' AND created_at BETWEEN ? AND ?";

                // Critical Alerts - Red
                availableMetrics.add(new ChartMetric(
                                "critical_alerts",
                                "Critical Alerts",
                                "Alert",
                                "#e24d42",
                                String.format(severityQuery, "Critical"),
                                MetricType.COUNT));

                // High Priority Alerts - Orange
                availableMetrics.add(new ChartMetric(
                                "high_alerts",
                                "High Priority Alerts",
                                "Alert",
                                "#ff7f0e",
                                String.format(severityQuery, "High"),
                                MetricType.COUNT));

                // Medium Alerts - Yellow
                availableMetrics.add(new ChartMetric(
                                "medium_alerts",
                                "Medium Alerts",
                                "Alert",
                                "#f2cc0c",
                                String.format(severityQuery, "Medium"),
                                MetricType.COUNT));

                // Low Priority Alerts - Green
                availableMetrics.add(new ChartMetric(
                                "low_alerts",
                                "Low Alerts",
                                "Alert",
                                "#73bf69",
                                String.format(severityQuery, "Low"),
                                MetricType.COUNT));
        }

        // ==================== SESSION METRICS ====================

        private void addSessionMetrics() {
                // Active Sessions - Violet
                availableMetrics.add(new ChartMetric(
                                "active_sessions",
                                "Active Sessions",
                                "Session",
                                "#a352cc",
                                "SELECT COUNT(*) FROM " + SCHEMA + ".monitoring_sessions " +
                                                "WHERE status = 'ACTIVE' AND start_time <= ? AND (end_time IS NULL OR end_time >= ?)",
                                MetricType.COUNT));

                // Completed Sessions - Green
                availableMetrics.add(new ChartMetric(
                                "completed_sessions",
                                "Completed Sessions",
                                "Session",
                                "#73bf69",
                                "SELECT COUNT(*) FROM " + SCHEMA + ".monitoring_sessions " +
                                                "WHERE status = 'COMPLETED' AND end_time BETWEEN ? AND ?",
                                MetricType.COUNT));
        }

        // ==================== TRAFFIC AGGREGATE METRICS ====================

        private void addTrafficMetrics() {
                // Packet Rate - Blue
                availableMetrics.add(new ChartMetric(
                                "packet_rate",
                                "Packet Rate",
                                "Traffic",
                                "#5294e0",
                                "SELECT COALESCE(COUNT(*), 0) / 60.0 FROM " + SCHEMA + ".traffic_logs " +
                                                "WHERE timestamp BETWEEN ? AND ?",
                                MetricType.RATE));

                // Throughput (Bytes/s) - Orange
                availableMetrics.add(new ChartMetric(
                                "byte_rate",
                                "Throughput",
                                "Traffic",
                                "#ff7f0e",
                                "SELECT COALESCE(SUM(packet_size), 0) / 60.0 FROM " + SCHEMA + ".traffic_logs " +
                                                "WHERE timestamp BETWEEN ? AND ?",
                                MetricType.BYTES));

                // Unique Source IPs - Green
                availableMetrics.add(new ChartMetric(
                                "unique_src_ips",
                                "Unique Source IPs",
                                "Traffic",
                                "#73bf69",
                                "SELECT COUNT(DISTINCT source_ip) FROM " + SCHEMA + ".traffic_logs " +
                                                "WHERE timestamp BETWEEN ? AND ?",
                                MetricType.COUNT));

        }

        // ==================== PUBLIC METHODS ====================

        /**
         * Get all available metrics
         */
        public List<ChartMetric> getAllMetrics() {
                return new ArrayList<>(availableMetrics);
        }

        /**
         * Get only enabled metrics
         */
        public List<ChartMetric> getEnabledMetrics() {
                return availableMetrics.stream()
                                .filter(ChartMetric::isEnabled)
                                .collect(Collectors.toList());
        }

        /**
         * Get metrics by category
         */
        public List<ChartMetric> getMetricsByCategory(String category) {
                return availableMetrics.stream()
                                .filter(m -> m.getCategory().equalsIgnoreCase(category))
                                .collect(Collectors.toList());
        }

        /**
         * Get a specific metric by ID
         */
        public ChartMetric getMetricById(String id) {
                return availableMetrics.stream()
                                .filter(m -> m.getId().equals(id))
                                .findFirst()
                                .orElse(null);
        }

        /**
         * Enable or disable a metric by ID
         */
        public void setMetricEnabled(String id, boolean enabled) {
                ChartMetric metric = getMetricById(id);
                if (metric != null) {
                        metric.setEnabled(enabled);
                        System.out.println("📊 Metric '" + id + "' " + (enabled ? "enabled" : "disabled"));
                }
        }

        /**
         * Enable multiple metrics by ID
         */
        public void enableMetrics(String... ids) {
                for (String id : ids) {
                        setMetricEnabled(id, true);
                }
        }

        /**
         * Disable all metrics
         */
        public void disableAllMetrics() {
                availableMetrics.forEach(m -> m.setEnabled(false));
        }

        /**
         * Get count of enabled metrics
         */
        public int getEnabledCount() {
                return (int) availableMetrics.stream()
                                .filter(ChartMetric::isEnabled)
                                .count();
        }

        /**
         * Get all available categories
         */
        public List<String> getCategories() {
                return availableMetrics.stream()
                                .map(ChartMetric::getCategory)
                                .distinct()
                                .collect(Collectors.toList());
        }
}
