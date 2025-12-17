package org.example.database;

import org.example.database.dao.AlertDAO;
import org.example.database.dao.TrafficDAO;
import org.example.models.SecurityAlert;
import org.example.models.TrafficData;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Seeds the database with sample data for testing
 * Run this once to populate your database   with initial data
 */
public class DatabaseSeeder {
    private static final Random random = new Random();
    private static final String[] SEVERITIES = {"Critical", "High", "Medium", "Low", "Info"};
    private static final String[] ALERT_TYPES = {
            "Port Scan", "SQL Injection", "DDoS Attack",
            "Brute Force", "Malware", "Suspicious Traffic",
            "Unauthorized Access", "Data Exfiltration"
    };
    private static final String[] PROTOCOLS = {"TCP", "UDP", "HTTP", "HTTPS", "ICMP", "DNS", "FTP", "SSH"};
    private static final String[] STATUSES = {"Allowed", "Blocked", "Monitored"};

    public static void seedDatabase() {
        System.out.println("Starting database seeding...");

        AlertDAO alertDAO = new AlertDAO();
        TrafficDAO trafficDAO = new TrafficDAO();

        // Check if data already exists
        if (alertDAO.getTotalAlertsCount() > 0) {
            System.out.println("Database already contains data. Skipping seeding.");
            return;
        }

        // Seed alerts
        System.out.println("Seeding alerts...");
        for (int i = 0; i < 50; i++) {
            SecurityAlert alert = generateRandomAlert();
            alertDAO.insertAlert(alert);
        }
        System.out.println("Seeded 50 alerts");

        // Seed traffic data
        System.out.println("Seeding traffic data...");
        for (int i = 0; i < 200; i++) {
            TrafficData traffic = generateRandomTraffic();
            trafficDAO.insertTraffic(traffic);
        }
        System.out.println("Seeded 200 traffic records");

        System.out.println("Database seeding completed successfully!");
    }

    private static SecurityAlert generateRandomAlert() {
        String id = "ALT-" + String.format("%05d", random.nextInt(99999));
        String severity = SEVERITIES[random.nextInt(SEVERITIES.length)];
        String type = ALERT_TYPES[random.nextInt(ALERT_TYPES.length)];
        String sourceIP = generateIP();
        String destIP = generateIP();
        String description = "Detected " + type + " from " + sourceIP;
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(random.nextInt(1440));

        return new SecurityAlert(id, severity, type, sourceIP, destIP, description, timestamp);
    }

    private static TrafficData generateRandomTraffic() {
        String protocol = PROTOCOLS[random.nextInt(PROTOCOLS.length)];
        String sourceIP = generateIP();
        String sourcePort = String.valueOf(1024 + random.nextInt(64512));
        String destIP = generateIP();
        String destPort = String.valueOf(random.nextInt(65536));
        long packetSize = 64 + random.nextInt(1472);
        String timestamp = LocalDateTime.now().minusSeconds(random.nextInt(3600)).toLocalTime().toString();
        String status = STATUSES[random.nextInt(STATUSES.length)];

        return new TrafficData(protocol, sourceIP, sourcePort, destIP, destPort, packetSize, timestamp, status);
    }

    private static String generateIP() {
        return random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
    }

    public static void main(String[] args) {
        seedDatabase();
    }
}