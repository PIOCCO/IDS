package org.example.models;

public class TrafficData {
    private String protocol;
    private String sourceIP;
    private String sourcePort;
    private String destinationIP;
    private String destinationPort;
    private long packetSize;
    private String timestamp;
    private String status;

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
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getSourceIP() { return sourceIP; }
    public void setSourceIP(String sourceIP) { this.sourceIP = sourceIP; }

    public String getSourcePort() { return sourcePort; }
    public void setSourcePort(String sourcePort) { this.sourcePort = sourcePort; }

    public String getDestinationIP() { return destinationIP; }
    public void setDestinationIP(String destinationIP) { this.destinationIP = destinationIP; }

    public String getDestinationPort() { return destinationPort; }
    public void setDestinationPort(String destinationPort) { this.destinationPort = destinationPort; }

    public long getPacketSize() { return packetSize; }
    public void setPacketSize(long packetSize) { this.packetSize = packetSize; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
