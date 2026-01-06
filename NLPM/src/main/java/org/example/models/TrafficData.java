package org.example.models;

import java.time.LocalDateTime;

public class TrafficData {
    private Long logId;
    private String protocol;
    private String sourceIP;
    private String sourcePort;
    private String destinationIP;
    private String destinationPort;
    private long packetSize;
    private String timestamp;
    private String status;

    // LocalDateTime version for database operations
    private LocalDateTime timestampLdt;

    public TrafficData() {
        // Default constructor for DAO operations
    }

    public TrafficData(String protocol, String sourceIP, String sourcePort,
            String destinationIP, String destinationPort,
            long packetSize, String timestamp, String status) {
        this.protocol = protocol;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
        this.packetSize = packetSize;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = String.valueOf(sourcePort);
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(String destinationIP) {
        this.destinationIP = destinationIP;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = String.valueOf(destinationPort);
    }

    public long getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(long packetSize) {
        this.packetSize = packetSize;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestampLdt = timestamp;
        this.timestamp = timestamp != null ? timestamp.toString() : null;
    }

    public LocalDateTime getTimestampAsLocalDateTime() {
        return timestampLdt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods for parsing ports
    public int getSourcePortAsInt() {
        try {
            return Integer.parseInt(sourcePort);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getDestinationPortAsInt() {
        try {
            return Integer.parseInt(destinationPort);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
