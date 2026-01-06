package org.example.models;

/**
 * Model class representing a chart metric configuration.
 * Used by the Grafana-style real-time charting system.
 */
public class ChartMetric {

    private String id; // Unique identifier (e.g., "tcp_traffic")
    private String displayName; // Display name (e.g., "TCP Traffic")
    private String category; // Category: "Protocol", "Alert", "Session", "Traffic"
    private String color; // Hex color (e.g., "#5294e0")
    private String sqlQuery; // SQL query template
    private boolean enabled; // Whether metric is currently enabled
    private MetricType type; // Type of metric

    /**
     * Metric type enumeration for formatting
     */
    public enum MetricType {
        COUNT, // Simple count (e.g., "42")
        RATE, // Rate per second (e.g., "5.2/s")
        PERCENTAGE, // Percentage (e.g., "85.5%")
        BYTES // Byte size (e.g., "1.5 MB")
    }

    /**
     * Full constructor
     */
    public ChartMetric(String id, String displayName, String category,
            String color, String sqlQuery, MetricType type) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        setColor(color); // Use setter for validation
        this.sqlQuery = sqlQuery;
        this.type = type;
        this.enabled = false; // Disabled by default
    }

    // ==================== GETTERS & SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    /**
     * Set color with validation (must be valid hex format)
     */
    public void setColor(String color) {
        if (color != null && !color.matches("^#([A-Fa-f0-9]{6})$")) {
            System.err.println("Warning: Invalid color format '" + color + "', using default");
            this.color = "#5294e0"; // Default blue
        } else {
            this.color = color;
        }
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MetricType getType() {
        return type;
    }

    public void setType(MetricType type) {
        this.type = type;
    }

    // ==================== FORMATTING ====================

    /**
     * Format a value according to the metric type
     */
    public String getFormattedValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }

        return switch (type) {
            case COUNT -> formatCount(value);
            case RATE -> formatRate(value);
            case PERCENTAGE -> formatPercentage(value);
            case BYTES -> formatBytes(value);
        };
    }

    private String formatCount(double value) {
        if (value < 1000) {
            return String.format("%.0f", value);
        } else if (value < 1_000_000) {
            return String.format("%.1fK", value / 1000);
        } else {
            return String.format("%.1fM", value / 1_000_000);
        }
    }

    private String formatRate(double value) {
        if (value < 0.01) {
            return "0/s";
        } else if (value < 10) {
            return String.format("%.2f/s", value);
        } else if (value < 1000) {
            return String.format("%.1f/s", value);
        } else {
            return String.format("%.1fK/s", value / 1000);
        }
    }

    private String formatPercentage(double value) {
        if (value < 0)
            value = 0;
        if (value > 100)
            value = 100;
        return String.format("%.1f%%", value);
    }

    private String formatBytes(double bytes) {
        if (bytes < 0)
            return "Invalid";
        if (bytes == 0)
            return "0 B";

        if (bytes < 1024) {
            return String.format("%.0f B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024 * 1024 * 1024));
        }
    }

    // ==================== UTILITY ====================

    @Override
    public String toString() {
        return "ChartMetric{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", category='" + category + '\'' +
                ", color='" + color + '\'' +
                ", enabled=" + enabled +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChartMetric that = (ChartMetric) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
