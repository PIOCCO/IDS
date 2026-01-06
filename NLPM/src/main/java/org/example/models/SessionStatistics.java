package org.example.models;

import java.time.LocalDateTime;

/**
 * Aggregated statistics for a monitoring session
 */
public class SessionStatistics {
    private int statId;
    private int sessionId;

    // Packet Statistics
    private long totalPacketsCaptured;
    private long totalBytesProcessed;

    // Protocol Distribution
    private int tcpPackets;
    private int udpPackets;
    private int icmpPackets;
    private int httpPackets;
    private int httpsPackets;
    private int dnsPackets;
    private int sshPackets;
    private int otherPackets;

    // Alert Statistics
    private int totalAlerts;
    private int criticalAlerts;
    private int highAlerts;
    private int mediumAlerts;
    private int lowAlerts;
    private int infoAlerts;

    // Direction Statistics
    private int inboundPackets;
    private int outboundPackets;
    private int localPackets;

    // Threat Statistics
    private int portScanAlerts;
    private int ddosAlerts;
    private int suspiciousPortAlerts;
    private int bruteForceAlerts;
    private int otherThreats;

    // Performance Metrics
    private double averagePacketSize;
    private int peakPacketRate;
    private double averagePacketRate;

    private LocalDateTime updatedAt;

    public SessionStatistics() {
    }

    // Getters and Setters
    public int getStatId() {
        return statId;
    }

    public void setStatId(int statId) {
        this.statId = statId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public long getTotalPacketsCaptured() {
        return totalPacketsCaptured;
    }

    public void setTotalPacketsCaptured(long totalPacketsCaptured) {
        this.totalPacketsCaptured = totalPacketsCaptured;
    }

    public long getTotalBytesProcessed() {
        return totalBytesProcessed;
    }

    public void setTotalBytesProcessed(long totalBytesProcessed) {
        this.totalBytesProcessed = totalBytesProcessed;
    }

    public int getTcpPackets() {
        return tcpPackets;
    }

    public void setTcpPackets(int tcpPackets) {
        this.tcpPackets = tcpPackets;
    }

    public int getUdpPackets() {
        return udpPackets;
    }

    public void setUdpPackets(int udpPackets) {
        this.udpPackets = udpPackets;
    }

    public int getIcmpPackets() {
        return icmpPackets;
    }

    public void setIcmpPackets(int icmpPackets) {
        this.icmpPackets = icmpPackets;
    }

    public int getHttpPackets() {
        return httpPackets;
    }

    public void setHttpPackets(int httpPackets) {
        this.httpPackets = httpPackets;
    }

    public int getHttpsPackets() {
        return httpsPackets;
    }

    public void setHttpsPackets(int httpsPackets) {
        this.httpsPackets = httpsPackets;
    }

    public int getDnsPackets() {
        return dnsPackets;
    }

    public void setDnsPackets(int dnsPackets) {
        this.dnsPackets = dnsPackets;
    }

    public int getSshPackets() {
        return sshPackets;
    }

    public void setSshPackets(int sshPackets) {
        this.sshPackets = sshPackets;
    }

    public int getOtherPackets() {
        return otherPackets;
    }

    public void setOtherPackets(int otherPackets) {
        this.otherPackets = otherPackets;
    }

    public int getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(int totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public int getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(int criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public int getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(int highAlerts) {
        this.highAlerts = highAlerts;
    }

    public int getMediumAlerts() {
        return mediumAlerts;
    }

    public void setMediumAlerts(int mediumAlerts) {
        this.mediumAlerts = mediumAlerts;
    }

    public int getLowAlerts() {
        return lowAlerts;
    }

    public void setLowAlerts(int lowAlerts) {
        this.lowAlerts = lowAlerts;
    }

    public int getInfoAlerts() {
        return infoAlerts;
    }

    public void setInfoAlerts(int infoAlerts) {
        this.infoAlerts = infoAlerts;
    }

    public int getInboundPackets() {
        return inboundPackets;
    }

    public void setInboundPackets(int inboundPackets) {
        this.inboundPackets = inboundPackets;
    }

    public int getOutboundPackets() {
        return outboundPackets;
    }

    public void setOutboundPackets(int outboundPackets) {
        this.outboundPackets = outboundPackets;
    }

    public int getLocalPackets() {
        return localPackets;
    }

    public void setLocalPackets(int localPackets) {
        this.localPackets = localPackets;
    }

    public int getPortScanAlerts() {
        return portScanAlerts;
    }

    public void setPortScanAlerts(int portScanAlerts) {
        this.portScanAlerts = portScanAlerts;
    }

    public int getDdosAlerts() {
        return ddosAlerts;
    }

    public void setDdosAlerts(int ddosAlerts) {
        this.ddosAlerts = ddosAlerts;
    }

    public int getSuspiciousPortAlerts() {
        return suspiciousPortAlerts;
    }

    public void setSuspiciousPortAlerts(int suspiciousPortAlerts) {
        this.suspiciousPortAlerts = suspiciousPortAlerts;
    }

    public int getBruteForceAlerts() {
        return bruteForceAlerts;
    }

    public void setBruteForceAlerts(int bruteForceAlerts) {
        this.bruteForceAlerts = bruteForceAlerts;
    }

    public int getOtherThreats() {
        return otherThreats;
    }

    public void setOtherThreats(int otherThreats) {
        this.otherThreats = otherThreats;
    }

    public double getAveragePacketSize() {
        return averagePacketSize;
    }

    public void setAveragePacketSize(double averagePacketSize) {
        this.averagePacketSize = averagePacketSize;
    }

    public int getPeakPacketRate() {
        return peakPacketRate;
    }

    public void setPeakPacketRate(int peakPacketRate) {
        this.peakPacketRate = peakPacketRate;
    }

    public double getAveragePacketRate() {
        return averagePacketRate;
    }

    public void setAveragePacketRate(double averagePacketRate) {
        this.averagePacketRate = averagePacketRate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get formatted bytes string (KB, MB, GB)
     */
    public String getFormattedBytes() {
        if (totalBytesProcessed < 1024) {
            return totalBytesProcessed + " B";
        } else if (totalBytesProcessed < 1024 * 1024) {
            return String.format("%.1f KB", totalBytesProcessed / 1024.0);
        } else if (totalBytesProcessed < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalBytesProcessed / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", totalBytesProcessed / (1024.0 * 1024 * 1024));
        }
    }
}
