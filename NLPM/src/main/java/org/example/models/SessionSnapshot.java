package org.example.models;

import java.time.LocalDateTime;

/**
 * Represents a point-in-time snapshot of monitoring session data for
 * time-series charts
 */
public class SessionSnapshot {
    private int snapshotId;
    private int sessionId;
    private LocalDateTime snapshotTime;

    private int packetsCount;
    private long bytesCount;
    private int alertsCount;
    private int packetRate;

    private int tcpCount;
    private int udpCount;
    private int httpCount;

    private LocalDateTime createdAt;

    public SessionSnapshot() {
    }

    public SessionSnapshot(int sessionId) {
        this.sessionId = sessionId;
        this.snapshotTime = LocalDateTime.now();
    }

    // Getters and Setters
    public int getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(int snapshotId) {
        this.snapshotId = snapshotId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }

    public void setSnapshotTime(LocalDateTime snapshotTime) {
        this.snapshotTime = snapshotTime;
    }

    public int getPacketsCount() {
        return packetsCount;
    }

    public void setPacketsCount(int packetsCount) {
        this.packetsCount = packetsCount;
    }

    public long getBytesCount() {
        return bytesCount;
    }

    public void setBytesCount(long bytesCount) {
        this.bytesCount = bytesCount;
    }

    public int getAlertsCount() {
        return alertsCount;
    }

    public void setAlertsCount(int alertsCount) {
        this.alertsCount = alertsCount;
    }

    public int getPacketRate() {
        return packetRate;
    }

    public void setPacketRate(int packetRate) {
        this.packetRate = packetRate;
    }

    public int getTcpCount() {
        return tcpCount;
    }

    public void setTcpCount(int tcpCount) {
        this.tcpCount = tcpCount;
    }

    public int getUdpCount() {
        return udpCount;
    }

    public void setUdpCount(int udpCount) {
        this.udpCount = udpCount;
    }

    public int getHttpCount() {
        return httpCount;
    }

    public void setHttpCount(int httpCount) {
        this.httpCount = httpCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SessionSnapshot{" +
                "sessionId=" + sessionId +
                ", time=" + snapshotTime +
                ", packets=" + packetsCount +
                ", rate=" + packetRate +
                '}';
    }
}
