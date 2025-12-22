package org.example.services;

import org.example.models.SecurityAlert;
import org.example.models.TrafficData;
import org.example.database.dao.AlertDAO;
import org.pcap4j.packet.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advanced detection engine for identifying network threats
 * Implements multiple detection strategies:
 * - Port Scan Detection
 * - DDoS Detection
 * - Brute Force Detection
 * - SQL Injection Detection (in HTTP traffic)
 * - Suspicious Traffic Patterns
 */
public class DetectionEngine {
    private static DetectionEngine instance;
    private AlertDAO alertDAO;
    private AlertNotificationService notificationService;

    // Detection thresholds
    private static final int PORT_SCAN_THRESHOLD = 20; // ports per IP in time window
    private static final int BRUTE_FORCE_THRESHOLD = 10; // failed attempts
    private static final int DDOS_PACKET_THRESHOLD = 1000; // packets per second
    private static final int CONNECTION_THRESHOLD = 100; // connections per IP
    private static final long TIME_WINDOW_MS = 60000; // 1 minute window

    // Tracking maps
    private Map<String, Set<Integer>> portScanTracker; // IP -> ports accessed
    private Map<String, List<Long>> connectionTracker; // IP -> timestamps
    private Map<String, Integer> bruteForceTracker; // IP -> failed attempts
    private Map<String, Long> lastAlertTime; // IP -> last alert timestamp

    // Blacklisted IPs
    private Set<String> blacklistedIPs;

    // Statistics
    private long totalThreatsDetected = 0;
    private long totalPacketsAnalyzed = 0;

    private ScheduledExecutorService cleanupScheduler;

    private DetectionEngine() {
        this.alertDAO = new AlertDAO();
        this.notificationService = AlertNotificationService.getInstance();

        this.portScanTracker = new ConcurrentHashMap<>();
        this.connectionTracker = new ConcurrentHashMap<>();
        this.bruteForceTracker = new ConcurrentHashMap<>();
        this.lastAlertTime = new ConcurrentHashMap<>();
        this.blacklistedIPs = ConcurrentHashMap.newKeySet();

        // Start cleanup task to remove old entries
        startCleanupTask();
    }

    public static synchronized DetectionEngine getInstance() {
        if (instance == null) {
            instance = new DetectionEngine();
        }
        return instance;
    }

    /**
     * Main analysis method - checks packet against all detection rules
     */
    public void analyzeTraffic(TrafficData traffic, Packet packet) {
        totalPacketsAnalyzed++;

        String srcIp = traffic.getSourceIP();
        String dstIp = traffic.getDestinationIP();

        // Skip analysis for local/private traffic if configured
        if (isLocalIP(srcIp)) {
            return;
        }

        // Check if IP is blacklisted
        if (blacklistedIPs.contains(srcIp)) {
            generateAlert("Blacklisted IP", "Critical", srcIp, dstIp,
                    "Traffic from blacklisted IP address");
            return;
        }

        // Run detection algorithms
        detectPortScan(traffic);
        detectDDoS(traffic);
        detectBruteForce(traffic);
        detectSuspiciousTraffic(traffic);

        // Analyze packet payload for signatures
        analyzePacketPayload(packet, traffic);
    }

    /**
     * Detect port scanning activity
     */
    private void detectPortScan(TrafficData traffic) {
        String srcIp = traffic.getSourceIP();
        int dstPort;

        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            return;
        }

        // Track ports accessed by this IP
        portScanTracker.computeIfAbsent(srcIp, k -> ConcurrentHashMap.newKeySet())
                .add(dstPort);

        Set<Integer> portsAccessed = portScanTracker.get(srcIp);

        // If IP has accessed many different ports in short time
        if (portsAccessed.size() >= PORT_SCAN_THRESHOLD) {
            if (shouldGenerateAlert(srcIp, "PortScan")) {
                generateAlert(
                        "Port Scan",
                        "High",
                        srcIp,
                        traffic.getDestinationIP(),
                        String.format("Port scan detected: %d unique ports accessed",
                                portsAccessed.size())
                );

                // Reset counter after alert
                portsAccessed.clear();
            }
        }
    }

    /**
     * Detect DDoS attacks based on packet rate
     */
    private void detectDDoS(TrafficData traffic) {
        String srcIp = traffic.getSourceIP();
        long currentTime = System.currentTimeMillis();

        // Track connection timestamps
        connectionTracker.computeIfAbsent(srcIp, k -> new ArrayList<>())
                .add(currentTime);

        List<Long> timestamps = connectionTracker.get(srcIp);

        // Remove old timestamps outside time window
        timestamps.removeIf(ts -> currentTime - ts > TIME_WINDOW_MS);

        // Check if rate exceeds threshold
        if (timestamps.size() >= DDOS_PACKET_THRESHOLD) {
            if (shouldGenerateAlert(srcIp, "DDoS")) {
                generateAlert(
                        "DDoS Attack",
                        "Critical",
                        srcIp,
                        traffic.getDestinationIP(),
                        String.format("Possible DDoS attack: %d packets in 60 seconds",
                                timestamps.size())
                );

                // Add to blacklist for repeated offenders
                blacklistedIPs.add(srcIp);
                timestamps.clear();
            }
        }
    }

    /**
     * Detect brute force attacks on authentication services
     */
    private void detectBruteForce(TrafficData traffic) {
        String protocol = traffic.getProtocol();
        String srcIp = traffic.getSourceIP();

        // Check if targeting authentication services
        int dstPort;
        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            return;
        }

        // Common auth ports: SSH(22), RDP(3389), FTP(21), Telnet(23)
        if (dstPort == 22 || dstPort == 3389 || dstPort == 21 || dstPort == 23) {
            bruteForceTracker.merge(srcIp, 1, Integer::sum);

            int attempts = bruteForceTracker.get(srcIp);

            if (attempts >= BRUTE_FORCE_THRESHOLD) {
                if (shouldGenerateAlert(srcIp, "BruteForce")) {
                    generateAlert(
                            "Brute Force",
                            "High",
                            srcIp,
                            traffic.getDestinationIP(),
                            String.format("Brute force attack detected on port %d: %d attempts",
                                    dstPort, attempts)
                    );

                    bruteForceTracker.put(srcIp, 0);
                }
            }
        }
    }

    /**
     * Detect suspicious traffic patterns
     */
    private void detectSuspiciousTraffic(TrafficData traffic) {
        int dstPort;
        try {
            dstPort = Integer.parseInt(traffic.getDestinationPort());
        } catch (NumberFormatException e) {
            return;
        }

        // Check for suspicious ports
        List<Integer> suspiciousPorts = Arrays.asList(
                1337, 31337, // Common trojan ports
                6667, 6668, 6669, // IRC (often used by botnets)
                12345, 12346, // NetBus
                20034, // NetBus Pro
                9996, 9997, 9998, 9999 // Various backdoors
        );

        if (suspiciousPorts.contains(dstPort)) {
            if (shouldGenerateAlert(traffic.getSourceIP(), "Suspicious")) {
                generateAlert(
                        "Suspicious Traffic",
                        "Medium",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("Traffic detected on suspicious port: %d", dstPort)
                );
            }
        }

        // Check for unusual packet sizes (could indicate data exfiltration)
        if (traffic.getPacketSize() > 60000) { // Very large packets
            if (shouldGenerateAlert(traffic.getSourceIP(), "LargePacket")) {
                generateAlert(
                        "Data Exfiltration",
                        "Medium",
                        traffic.getSourceIP(),
                        traffic.getDestinationIP(),
                        String.format("Unusually large packet detected: %d bytes",
                                traffic.getPacketSize())
                );
            }
        }
    }

    /**
     * Analyze packet payload for attack signatures
     */
    private void analyzePacketPayload(Packet packet, TrafficData traffic) {
        // Get payload
        byte[] payload = packet.getPayload() != null ?
                packet.getPayload().getRawData() : null;

        if (payload == null || payload.length == 0) {
            return;
        }

        String payloadStr = new String(payload).toLowerCase();

        // SQL Injection patterns
        String[] sqlPatterns = {
                "' or '1'='1",
                "' or 1=1--",
                "union select",
                "drop table",
                "insert into",
                "delete from",
                "exec(",
                "execute("
        };

        for (String pattern : sqlPatterns) {
            if (payloadStr.contains(pattern)) {
                if (shouldGenerateAlert(traffic.getSourceIP(), "SQLInjection")) {
                    generateAlert(
                            "SQL Injection",
                            "Critical",
                            traffic.getSourceIP(),
                            traffic.getDestinationIP(),
                            "SQL injection attempt detected in packet payload"
                    );
                }
                break;
            }
        }

        // XSS patterns
        String[] xssPatterns = {
                "<script>",
                "javascript:",
                "onerror=",
                "onload=",
                "<iframe"
        };

        for (String pattern : xssPatterns) {
            if (payloadStr.contains(pattern)) {
                if (shouldGenerateAlert(traffic.getSourceIP(), "XSS")) {
                    generateAlert(
                            "XSS Attack",
                            "High",
                            traffic.getSourceIP(),
                            traffic.getDestinationIP(),
                            "Cross-Site Scripting attempt detected"
                    );
                }
                break;
            }
        }

        // Command injection patterns
        String[] cmdPatterns = {
                "; ls",
                "| cat",
                "&& dir",
                "| whoami",
                "; wget",
                "| curl"
        };

        for (String pattern : cmdPatterns) {
            if (payloadStr.contains(pattern)) {
                if (shouldGenerateAlert(traffic.getSourceIP(), "CmdInjection")) {
                    generateAlert(
                            "Command Injection",
                            "Critical",
                            traffic.getSourceIP(),
                            traffic.getDestinationIP(),
                            "Command injection attempt detected"
                    );
                }
                break;
            }
        }
    }

    /**
     * Generate and store security alert
     */
    private void generateAlert(String type, String severity, String srcIp,
                               String dstIp, String description) {
        totalThreatsDetected++;

        String alertId = "ALT-" + String.format("%05d",
                (int)(System.currentTimeMillis() % 100000));

        SecurityAlert alert = new SecurityAlert(
                alertId,
                severity,
                type,
                srcIp,
                dstIp,
                description,
                LocalDateTime.now()
        );

        // Store in database
        alertDAO.insertAlert(alert);

        // Send notification
        notificationService.sendAlert(alert);

        System.out.println(String.format(
                "[ALERT] %s | %s | %s -> %s | %s",
                severity, type, srcIp, dstIp, description
        ));
    }

    /**
     * Check if alert should be generated (rate limiting)
     */
    private boolean shouldGenerateAlert(String ip, String alertType) {
        String key = ip + ":" + alertType;
        long currentTime = System.currentTimeMillis();
        Long lastAlert = lastAlertTime.get(key);

        // Only generate alert if enough time has passed (5 seconds)
        if (lastAlert == null || currentTime - lastAlert > 5000) {
            lastAlertTime.put(key, currentTime);
            return true;
        }

        return false;
    }

    /**
     * Check if IP is local/private
     */
    private boolean isLocalIP(String ip) {
        return ip.startsWith("127.") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.equals("0.0.0.0") ||
                ip.equals("::1");
    }

    /**
     * Periodic cleanup of old tracking data
     */
    private void startCleanupTask() {
        cleanupScheduler = Executors.newScheduledThreadPool(1);
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();

                // Clean connection tracker
                connectionTracker.forEach((ip, timestamps) -> {
                    timestamps.removeIf(ts -> currentTime - ts > TIME_WINDOW_MS);
                });

                // Clean brute force tracker
                bruteForceTracker.entrySet()
                        .removeIf(entry -> entry.getValue() == 0);

                // Clean alert rate limiter
                lastAlertTime.entrySet()
                        .removeIf(entry -> currentTime - entry.getValue() > 300000); // 5 min

            } catch (Exception e) {
                System.err.println("Error in cleanup task: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public long getTotalThreatsDetected() {
        return totalThreatsDetected;
    }

    public long getTotalPacketsAnalyzed() {
        return totalPacketsAnalyzed;
    }

    public void addToBlacklist(String ip) {
        blacklistedIPs.add(ip);
    }

    public void removeFromBlacklist(String ip) {
        blacklistedIPs.remove(ip);
    }

    public Set<String> getBlacklistedIPs() {
        return new HashSet<>(blacklistedIPs);
    }

    /**
     * Shutdown detection engine
     */
    public void shutdown() {
        if (cleanupScheduler != null) {
            cleanupScheduler.shutdown();
        }
    }
}