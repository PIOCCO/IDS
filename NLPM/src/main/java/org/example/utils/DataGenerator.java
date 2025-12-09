package org.example.utils;


import org.example.models.Alert;
import org.example.models.TrafficData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final Random random = new Random();
    private static final String[] SEVERITIES = {"Critical", "High", "Medium", "Low", "Info"};
    private static final String[] ALERT_TYPES = {"Port Scan", "SQL Injection", "DDoS Attack",
            "Brute Force", "Malware", "Suspicious Traffic",
            "Unauthorized Access", "Data Exfiltration"};
    private static final String[] PROTOCOLS = {"TCP", "UDP", "HTTP", "HTTPS", "ICMP", "DNS", "FTP", "SSH"};
    private static final String[] STATUSES = {"Allowed", "Blocked", "Monitored"};

    /**
     * Generate a list of random alerts
     */
    public static List<Alert> generateAlerts(int count) {
        List<Alert> alerts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String id = "ALT-" + String.format("%05d", i + 1);
            String severity = SEVERITIES[random.nextInt(SEVERITIES.length)];
            String type = ALERT_TYPES[random.nextInt(ALERT_TYPES.length)];
            String sourceIP = generateIP();
            String destIP = generateIP();
            String description = "Detected " + type + " from " + sourceIP;
            LocalDateTime timestamp = LocalDateTime.now().minusMinutes(random.nextInt(1440));

            alerts.add(new Alert(id, severity, type, sourceIP, destIP, description, timestamp));
        }
        return alerts;
    }

    /**
     * Generate a list of random traffic data
     */
    public static List<TrafficData> generateTrafficData(int count) {
        List<TrafficData> trafficList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (int i = 0; i < count; i++) {
            String protocol = PROTOCOLS[random.nextInt(PROTOCOLS.length)];
            String sourceIP = generateIP();
            String sourcePort = String.valueOf(1024 + random.nextInt(64512));
            String destIP = generateIP();
            String destPort = String.valueOf(random.nextInt(65536));
            long packetSize = 64 + random.nextInt(1472);
            String timestamp = LocalDateTime.now().minusSeconds(random.nextInt(3600)).format(formatter);
            String status = STATUSES[random.nextInt(STATUSES.length)];

            trafficList.add(new TrafficData(protocol, sourceIP, sourcePort,
                    destIP, destPort, packetSize,
                    timestamp, status));
        }
        return trafficList;
    }

    /**
     * Generate a random IP address
     */
    public static String generateIP() {
        return random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
    }

    /**
     * Generate a random MAC address
     */
    public static String generateMAC() {
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            mac.append(String.format("%02X", random.nextInt(256)));
            if (i < 5) mac.append(":");
        }
        return mac.toString();
    }

    /**
     * Generate random port number
     */
    public static int generatePort() {
        return 1024 + random.nextInt(64512);
    }

    /**
     * Get random severity level
     */
    public static String getRandomSeverity() {
        return SEVERITIES[random.nextInt(SEVERITIES.length)];
    }

    /**
     * Get random protocol
     */
    public static String getRandomProtocol() {
        return PROTOCOLS[random.nextInt(PROTOCOLS.length)];
    }

    /**
     * Get random status
     */
    public static String getRandomStatus() {
        return STATUSES[random.nextInt(STATUSES.length)];
    }

    /**
     * Get random alert type
     */
    public static String getRandomAlertType() {
        return ALERT_TYPES[random.nextInt(ALERT_TYPES.length)];
    }
}