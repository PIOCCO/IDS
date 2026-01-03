package org.example.services;

import org.example.models.SecurityAlert;
import org.example.models.TrafficData;
import org.example.database.dao.AlertDAO;
import org.pcap4j.packet.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Detection Engine with Direction-Aware Intelligence
 * Features:
 * - Traffic direction detection (INBOUND vs OUTBOUND)
 * - Rate-based detection with thresholds
 * - Stateful TCP/UDP analysis
 * - Intelligent blacklist handling
 * - Port and protocol awareness
 * - Whitelist support for known good IPs
 */
public class DetectionEngine {
    private static DetectionEngine instance;
    private AlertDAO alertDAO;
    private AlertNotificationService notificationService;

    // Local IP addresses (detected automatically)
    private Set<String> localIPAddresses;

    // Whitelist for known safe IPs (ISP gateway, local network, etc.)
    private Set<String> whitelistedIPs;

    // Blacklisted IPs (but now with intelligent handling)
    private Set<String> blacklistedIPs;

    // Detection thresholds
    private static final int PORT_SCAN_THRESHOLD = 10; // ports per IP in time window
    private static final int PACKET_RATE_THRESHOLD = 50; // packets per second
    private static final int SYN_SCAN_THRESHOLD = 15; // SYN packets per second
    private static final int ICMP_FLOOD_THRESHOLD = 50; // ICMP packets per second
    private static final long TIME_WINDOW_MS = 60000; // 1 minute window
    private static final long RATE_WINDOW_MS = 1000; // 1 second for rate calculation

    // Suspicious ports to monitor
    private static final Set<Integer> SUSPICIOUS_PORTS = Set.of(
            22,    // SSH
            23,    // Telnet
            445,   // SMB
            3389,  // RDP
            5900,  // VNC
            1433,  // MS SQL
            3306,  // MySQL
            5432,  // PostgreSQL
            1337, 31337, // Common trojan ports
            6667, 6668, 6669, // IRC (botnets)
            12345, 12346, 20034, // Backdoors
            9996, 9997, 9998, 9999 // Various backdoors
    );

    // Normal ports to ignore (unless there's flooding)
    private static final Set<Integer> NORMAL_PORTS = Set.of(
            80,    // HTTP
            443,   // HTTPS
            53,    // DNS
            67, 68, // DHCP
            123    // NTP
    );

    // Tracking maps with enhanced data
    private Map<String, Set<Integer>> portScanTracker; // IP -> ports accessed
    private Map<String, List<Long>> packetRateTracker; // IP -> packet timestamps
    private Map<String, List<Long>> synPacketTracker; // IP -> SYN timestamps
    private Map<String, List<Long>> icmpPacketTracker; // IP -> ICMP timestamps
    private Map<String, TCPConnectionState> tcpStateTracker; // Connection -> state
    private Map<String, Integer> halfOpenConnections; // IP -> count
    private Map<String, Long> lastAlertTime; // IP:Type -> last alert timestamp

    // Statistics
    private long totalThreatsDetected = 0;
    private long totalPacketsAnalyzed = 0;
    private long inboundPackets = 0;
    private long outboundPackets = 0;

    private ScheduledExecutorService cleanupScheduler;

    private DetectionEngine() {
        this.alertDAO = new AlertDAO();
        this.notificationService = AlertNotificationService.getInstance();

        this.localIPAddresses = ConcurrentHashMap.newKeySet();
        this.whitelistedIPs = ConcurrentHashMap.newKeySet();
        this.blacklistedIPs = ConcurrentHashMap.newKeySet();

        this.portScanTracker = new ConcurrentHashMap<>();
        this.packetRateTracker = new ConcurrentHashMap<>();
        this.synPacketTracker = new ConcurrentHashMap<>();
        this.icmpPacketTracker = new ConcurrentHashMap<>();
        this.tcpStateTracker = new ConcurrentHashMap<>();
        this.halfOpenConnections = new ConcurrentHashMap<>();
        this.lastAlertTime = new ConcurrentHashMap<>();

        initializeLocalIPs();
        initializeWhitelist();
        startCleanupTask();
    }

    public static synchronized DetectionEngine getInstance() {
        if (instance == null) {
            instance = new DetectionEngine();
        }
        return instance;
    }

    /**
     * Initialize local IP addresses for direction detection
     */
    private void initializeLocalIPs() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && !iface.isLoopback()) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof java.net.Inet4Address) {
                            String ip = addr.getHostAddress();
                            // Skip APIPA addresses (169.254.x.x)
                            if (!ip.startsWith("169.254.")) {
                                localIPAddresses.add(ip);
                            }
                        }
                    }
                }
            }
            System.out.println("Local IPs detected: " + localIPAddresses);
        } catch (SocketException e) {
            System.err.println("Error detecting local IPs: " + e.getMessage());
            // Fallback: add common private ranges
            localIPAddresses.add("192.168.1.1"); // Example - should be detected
        }
    }

    /**
     * Initialize whitelist with common safe IPs
     */
    private void initializeWhitelist() {
        // Add localhost
        whitelistedIPs.add("127.0.0.1");
        whitelistedIPs.add("::1");

        // Add local IPs to whitelist
        whitelistedIPs.addAll(localIPAddresses);

        // Users can add their ISP gateway, DNS servers, etc.
        // Example: whitelistedIPs.add("8.8.8.8"); // Google DNS
    }

    /**
     * Determine traffic direction
     */
    private String determineDirection(String srcIP, String dstIP) {
        boolean srcIsLocal = isLocalIP(srcIP) || localIPAddresses.contains(srcIP);
        boolean dstIsLocal = isLocalIP(dstIP) || localIPAddresses.contains(dstIP);

        if (!srcIsLocal && dstIsLocal) {
            inboundPackets++;
            return "INBOUND";
        } else if (srcIsLocal && !dstIsLocal) {
            outboundPackets++;
            return "OUTBOUND";
        } else if (srcIsLocal && dstIsLocal) {
            return "LOCAL";
        } else {
            return "TRANSIT"; // Neither local (shouldn't happen in normal capture)
        }
    }

    /**
     * Main analysis method with direction awareness
     */
    public void analyzeTraffic(TrafficData traffic, Packet packet) {
        totalPacketsAnalyzed++;

        String srcIp = traffic.getSourceIP();
        String dstIp = traffic.getDestinationIP();
        String direction = determineDirection(srcIp, dstIp);

        // Skip LOCAL and OUTBOUND traffic for most checks
        if (direction.equals("LOCAL") || direction.equals("OUTBOUND")) {
            return;
        }

        // For INBOUND traffic, the source is the remote IP
        String remoteIP = direction.equals("INBOUND") ? srcIp : dstIp;

        // Skip whitelisted IPs
        if (whitelistedIPs.contains(remoteIP)) {
            return;
        }

        // Track packet rate for this IP
        trackPacketRate(remoteIP);

        // Run detection algorithms (only for INBOUND traffic)
        if (direction.equals("INBOUND")) {
            detectHighPacketRate(remoteIP, traffic, direction);
            detectPortScan(traffic, remoteIP, direction);
            detectSuspiciousPortAccess(traffic, remoteIP, direction);

            // Protocol-specific detection
            if (packet.get(TcpPacket.class) != null) {
                analyzeTCPPacket(packet, traffic, remoteIP, direction);
            } else if (packet.get(IcmpV4CommonPacket.class) != null) {
                analyzeICMPPacket(packet, traffic, remoteIP, direction);
            }

            // Blacklist check (with additional conditions)
            if (blacklistedIPs.contains(remoteIP)) {
                handleBlacklistedIP(traffic, remoteIP, direction);
            }
        }

        // Analyze packet payload for attack signatures
        analyzePacketPayload(packet, traffic, direction);
    }

    /**
     * Track packet rate per IP
     */
    private void trackPacketRate(String ip) {
        long currentTime = System.currentTimeMillis();
        packetRateTracker.computeIfAbsent(ip, k -> new ArrayList<>()).add(currentTime);

        List<Long> timestamps = packetRateTracker.get(ip);
        timestamps.removeIf(ts -> currentTime - ts > RATE_WINDOW_MS);
    }

    /**
     * Detect high packet rate (potential DDoS)
     */
    private void detectHighPacketRate(String remoteIP, TrafficData traffic, String direction) {
        List<Long> timestamps = packetRateTracker.get(remoteIP);
        if (timestamps == null) return;

        // Calculate packets per second
        int packetsPerSecond = timestamps.size();

        if (packetsPerSecond >= PACKET_RATE_THRESHOLD) {
            if (shouldGenerateAlert(remoteIP, "HighRate")) {
                generateAlert(
                        "High Packet Rate",
                        "High",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("High packet rate detected: %d packets/sec from %s (%s traffic)",
                                packetsPerSecond, remoteIP, direction),
                        direction
                );
            }
        }
    }

    /**
     * Enhanced port scan detection with direction awareness
     */
    private void detectPortScan(TrafficData traffic, String remoteIP, String direction) {
        int dstPort;
        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            return;
        }

        // Track ports accessed by this IP
        portScanTracker.computeIfAbsent(remoteIP, k -> ConcurrentHashMap.newKeySet()).add(dstPort);
        Set<Integer> portsAccessed = portScanTracker.get(remoteIP);

        // Alert if scanning many ports
        if (portsAccessed.size() >= PORT_SCAN_THRESHOLD) {
            if (shouldGenerateAlert(remoteIP, "PortScan")) {
                generateAlert(
                        "Port Scan",
                        "High",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("Port scan detected: %d unique ports accessed by %s (%s traffic)",
                                portsAccessed.size(), remoteIP, direction),
                        direction
                );
                portsAccessed.clear();
            }
        }
    }

    /**
     * Detect access to suspicious ports
     */
    private void detectSuspiciousPortAccess(TrafficData traffic, String remoteIP, String direction) {
        int dstPort;
        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            return;
        }

        // Only alert on suspicious ports, ignore normal ones
        if (SUSPICIOUS_PORTS.contains(dstPort) && !NORMAL_PORTS.contains(dstPort)) {
            if (shouldGenerateAlert(remoteIP, "SuspiciousPort_" + dstPort)) {
                String portDescription = getPortDescription(dstPort);
                generateAlert(
                        "Suspicious Port Access",
                        "Medium",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("Access to suspicious port %d (%s) from %s (%s traffic)",
                                dstPort, portDescription, remoteIP, direction),
                        direction
                );
            }
        }
    }

    /**
     * Analyze TCP packets for stateful detection
     */
    private void analyzeTCPPacket(Packet packet, TrafficData traffic, String remoteIP, String direction) {
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        if (tcpPacket == null) return;

        TcpPacket.TcpHeader header = tcpPacket.getHeader();
        boolean isSyn = header.getSyn();
        boolean isAck = header.getAck();
        boolean isFin = header.getFin();
        boolean isRst = header.getRst();

        // Track SYN packets for SYN scan detection
        if (isSyn && !isAck) {
            long currentTime = System.currentTimeMillis();
            synPacketTracker.computeIfAbsent(remoteIP, k -> new ArrayList<>()).add(currentTime);

            List<Long> synTimestamps = synPacketTracker.get(remoteIP);
            synTimestamps.removeIf(ts -> currentTime - ts > RATE_WINDOW_MS);

            // Detect SYN scan
            if (synTimestamps.size() >= SYN_SCAN_THRESHOLD) {
                if (shouldGenerateAlert(remoteIP, "SynScan")) {
                    generateAlert(
                            "SYN Scan",
                            "High",
                            traffic.getSourceIP(),
                            traffic.getDestinationIP(),
                            String.format("SYN scan detected: %d SYN packets/sec from %s (%s traffic)",
                                    synTimestamps.size(), remoteIP, direction),
                            direction
                    );
                    synTimestamps.clear();
                }
            }

            // Track half-open connections
            String connKey = remoteIP + ":" + traffic.getDestinationPort();
            halfOpenConnections.merge(remoteIP, 1, Integer::sum);
        }

        // Track connection state
        if (isSyn && isAck) {
            // SYN-ACK received - connection establishing
            String connKey = remoteIP + ":" + traffic.getSourcePort();
            tcpStateTracker.put(connKey, TCPConnectionState.SYN_ACK_RECEIVED);
        } else if (isFin || isRst) {
            // Connection closing
            String connKey = remoteIP + ":" + traffic.getSourcePort();
            tcpStateTracker.remove(connKey);
            halfOpenConnections.computeIfPresent(remoteIP, (k, v) -> v > 0 ? v - 1 : 0);
        }
    }

    /**
     * Analyze ICMP packets for flood detection
     */
    private void analyzeICMPPacket(Packet packet, TrafficData traffic, String remoteIP, String direction) {
        long currentTime = System.currentTimeMillis();
        icmpPacketTracker.computeIfAbsent(remoteIP, k -> new ArrayList<>()).add(currentTime);

        List<Long> icmpTimestamps = icmpPacketTracker.get(remoteIP);
        icmpTimestamps.removeIf(ts -> currentTime - ts > RATE_WINDOW_MS);

        // Detect ICMP flood
        if (icmpTimestamps.size() >= ICMP_FLOOD_THRESHOLD) {
            if (shouldGenerateAlert(remoteIP, "IcmpFlood")) {
                generateAlert(
                        "ICMP Flood",
                        "High",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("ICMP flood detected: %d packets/sec from %s (%s traffic)",
                                icmpTimestamps.size(), remoteIP, direction),
                        direction
                );
                icmpTimestamps.clear();
            }
        }
    }

    /**
     * Handle blacklisted IP with intelligent checks
     */
    private void handleBlacklistedIP(TrafficData traffic, String remoteIP, String direction) {
        // Only alert if:
        // 1. Direction is INBOUND
        // 2. Port is suspicious OR packet rate is high

        int dstPort;
        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            dstPort = 0;
        }

        List<Long> timestamps = packetRateTracker.get(remoteIP);
        int packetRate = timestamps != null ? timestamps.size() : 0;

        if (SUSPICIOUS_PORTS.contains(dstPort) || packetRate > 10) {
            if (shouldGenerateAlert(remoteIP, "Blacklist")) {
                generateAlert(
                        "Blacklisted IP Activity",
                        "Critical",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("Traffic from blacklisted IP %s to port %d (%s traffic, rate: %d pkt/s)",
                                remoteIP, dstPort, direction, packetRate),
                        direction
                );
            }
        }
    }

    /**
     * Analyze packet payload for attack signatures
     */
    private void analyzePacketPayload(Packet packet, TrafficData traffic, String direction) {
        byte[] payload = packet.getPayload() != null ? packet.getPayload().getRawData() : null;
        if (payload == null || payload.length == 0) return;

        String payloadStr = new String(payload).toLowerCase();
        String remoteIP = direction.equals("INBOUND") ? traffic.getSourceIP() : traffic.getDestinationIP();

        // SQL Injection patterns
        String[] sqlPatterns = {"' or '1'='1", "' or 1=1--", "union select", "drop table"};
        for (String pattern : sqlPatterns) {
            if (payloadStr.contains(pattern)) {
                if (shouldGenerateAlert(remoteIP, "SQLInjection")) {
                    generateAlert("SQL Injection", "Critical",
                            traffic.getSourceIP(), traffic.getDestinationIP(),
                            "SQL injection attempt detected in payload (" + direction + " traffic)", direction);
                }
                break;
            }
        }

        // XSS patterns
        String[] xssPatterns = {"<script>", "javascript:", "onerror=", "onload="};
        for (String pattern : xssPatterns) {
            if (payloadStr.contains(pattern)) {
                if (shouldGenerateAlert(remoteIP, "XSS")) {
                    generateAlert("XSS Attack", "High",
                            traffic.getSourceIP(), traffic.getDestinationIP(),
                            "Cross-Site Scripting attempt detected (" + direction + " traffic)", direction);
                }
                break;
            }
        }
    }

    /**
     * Generate alert with direction information
     */
    private void generateAlert(String type, String severity, String srcIp, String dstIp,
                               String description, String direction) {
        totalThreatsDetected++;

        String alertId = "ALT-" + String.format("%05d", (int)(System.currentTimeMillis() % 100000));
        String enhancedDescription = String.format("[%s] %s", direction, description);

        SecurityAlert alert = new SecurityAlert(alertId, severity, type, srcIp, dstIp,
                enhancedDescription, LocalDateTime.now());
        alert.setDirection(direction);

        alertDAO.insertAlert(alert);
        notificationService.sendAlert(alert);

        System.out.println(String.format("[ALERT] %s | %s | %s -> %s | %s | Direction: %s",
                severity, type, srcIp, dstIp, description, direction));
    }

    /**
     * Check if alert should be generated (rate limiting per IP and type)
     */
    private boolean shouldGenerateAlert(String ip, String alertType) {
        String key = ip + ":" + alertType;
        long currentTime = System.currentTimeMillis();
        Long lastAlert = lastAlertTime.get(key);

        // Only generate alert if enough time has passed (10 seconds for same IP/type)
        if (lastAlert == null || currentTime - lastAlert > 10000) {
            lastAlertTime.put(key, currentTime);
            return true;
        }
        return false;
    }

    /**
     * Check if IP is in private/local range
     */
    private boolean isLocalIP(String ip) {
        return ip.startsWith("127.") || ip.startsWith("192.168.") ||
                ip.startsWith("10.") || ip.startsWith("172.16.") ||
                ip.equals("0.0.0.0") || ip.equals("::1");
    }

    /**
     * Get human-readable port description
     */
    private String getPortDescription(int port) {
        return switch (port) {
            case 22 -> "SSH";
            case 23 -> "Telnet";
            case 445 -> "SMB";
            case 3389 -> "RDP";
            case 5900 -> "VNC";
            case 1433 -> "MS SQL";
            case 3306 -> "MySQL";
            case 5432 -> "PostgreSQL";
            default -> "Unknown Service";
        };
    }

    /**
     * Periodic cleanup of old tracking data
     */
    private void startCleanupTask() {
        cleanupScheduler = Executors.newScheduledThreadPool(1);
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();

                // Clean packet rate trackers
                packetRateTracker.values().forEach(list ->
                        list.removeIf(ts -> currentTime - ts > TIME_WINDOW_MS));
                synPacketTracker.values().forEach(list ->
                        list.removeIf(ts -> currentTime - ts > TIME_WINDOW_MS));
                icmpPacketTracker.values().forEach(list ->
                        list.removeIf(ts -> currentTime - ts > TIME_WINDOW_MS));

                // Clean port scan tracker
                portScanTracker.entrySet().removeIf(entry -> entry.getValue().isEmpty());

                // Clean alert rate limiter
                lastAlertTime.entrySet().removeIf(entry ->
                        currentTime - entry.getValue() > 300000); // 5 minutes

                // Clean half-open connections
                halfOpenConnections.entrySet().removeIf(entry -> entry.getValue() == 0);

            } catch (Exception e) {
                System.err.println("Error in cleanup task: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    // Public API methods
    public void addToWhitelist(String ip) { whitelistedIPs.add(ip); }
    public void removeFromWhitelist(String ip) { whitelistedIPs.remove(ip); }
    public void addToBlacklist(String ip) { blacklistedIPs.add(ip); }
    public void removeFromBlacklist(String ip) { blacklistedIPs.remove(ip); }

    public Set<String> getWhitelistedIPs() { return new HashSet<>(whitelistedIPs); }
    public Set<String> getBlacklistedIPs() { return new HashSet<>(blacklistedIPs); }
    public Set<String> getLocalIPAddresses() { return new HashSet<>(localIPAddresses); }

    public long getTotalThreatsDetected() { return totalThreatsDetected; }
    public long getTotalPacketsAnalyzed() { return totalPacketsAnalyzed; }
    public long getInboundPackets() { return inboundPackets; }
    public long getOutboundPackets() { return outboundPackets; }

    public void shutdown() {
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
        }
    }

    // Inner class for TCP connection state tracking
    private enum TCPConnectionState {
        SYN_SENT, SYN_ACK_RECEIVED, ESTABLISHED, FIN_WAIT, CLOSED
    }
}