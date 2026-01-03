package org.example.models;

import java.time.LocalDateTime;

public class SecurityAlert {
    private String id;
    private String severity;
    private String type;
    private String sourceIP;
    private String destinationIP;
    private String description;
    private LocalDateTime timestamp;
    private String status;
    private String direction; // NEW: INBOUND, OUTBOUND, LOCAL

    public SecurityAlert(String id, String severity, String type, String sourceIP,
                         String destinationIP, String description, LocalDateTime timestamp) {
        this.id = id;
        this.severity = severity;
        this.type = type;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.description = description;
        this.timestamp = timestamp;
        this.status = "Active";
        this.direction = "UNKNOWN"; // Default
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSourceIP() { return sourceIP; }
    public void setSourceIP(String sourceIP) { this.sourceIP = sourceIP; }

    public String getDestinationIP() { return destinationIP; }
    public void setDestinationIP(String destinationIP) { this.destinationIP = destinationIP; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    /**
     * Get direction with emoji for UI display
     */
    public String getDirectionWithEmoji() {
        return switch (direction) {
            case "INBOUND" -> "⬇️ " + direction;
            case "OUTBOUND" -> "⬆️ " + direction;
            case "LOCAL" -> "🔄 " + direction;
            default -> "❓ " + direction;
        };
    }

    /**
     * Check if this is an inbound threat
     */
    public boolean isInboundThreat() {
        return "INBOUND".equals(direction);
    }
}