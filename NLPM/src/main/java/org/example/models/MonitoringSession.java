package org.example.models;

import java.time.LocalDateTime;

/**
 * Represents a monitoring session for tracking traffic capture activities
 */
public class MonitoringSession {
    private int sessionId;
    private String sessionName;
    private String interfaceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private String status; // ACTIVE, COMPLETED, STOPPED
    private String createdBy;
    private String notes;
    private LocalDateTime createdAt;

    // Additional computed fields for display
    private long totalPackets;
    private int totalAlerts;

    public MonitoringSession() {
    }

    public MonitoringSession(String sessionName, String interfaceName, String createdBy) {
        this.sessionName = sessionName;
        this.interfaceName = interfaceName;
        this.createdBy = createdBy;
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalPackets() {
        return totalPackets;
    }

    public void setTotalPackets(long totalPackets) {
        this.totalPackets = totalPackets;
    }

    public int getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(int totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    /**
     * Get formatted duration string (e.g., "1h 30m 45s")
     */
    public String getFormattedDuration() {
        if (durationSeconds == null || durationSeconds == 0) {
            return "Running...";
        }
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public String toString() {
        return "MonitoringSession{" +
                "sessionId=" + sessionId +
                ", sessionName='" + sessionName + '\'' +
                ", interface='" + interfaceName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
