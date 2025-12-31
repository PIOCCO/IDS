package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Professional Audit Logger for tracking all system actions
 * Logs security-relevant events to file for compliance and monitoring
 */
public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String AUDIT_LOG_FILE = "audit.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Ensure log directory exists
        try {
            Path logPath = Paths.get(LOG_DIR);
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    /**
     * Log an audit event
     *
     * @param username The user performing the action
     * @param action The action being performed
     * @param details Additional details about the action
     */
    public static void log(String username, String action, String details) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] USER:%s | ACTION:%s | DETAILS:%s%n",
                timestamp, username, action, details);

        // Write to file
        writeToFile(logEntry);

        // Also log to console for monitoring
        System.out.println("AUDIT: " + logEntry.trim());
    }

    /**
     * Log a security event (authentication, authorization failures, etc.)
     *
     * @param username The user involved
     * @param event The security event type
     * @param details Event details
     * @param success Whether the event was successful
     */
    public static void logSecurity(String username, String event, String details, boolean success) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String status = success ? "SUCCESS" : "FAILED";
        String logEntry = String.format("[%s] SECURITY:%s | USER:%s | EVENT:%s | DETAILS:%s%n",
                timestamp, status, username, event, details);

        writeToFile(logEntry);
        System.out.println("SECURITY AUDIT: " + logEntry.trim());
    }

    /**
     * Log a data modification event
     *
     * @param username The user performing the modification
     * @param table The table being modified
     * @param operation The operation (INSERT, UPDATE, DELETE)
     * @param recordId The ID of the record affected
     */
    public static void logDataModification(String username, String table,
                                           String operation, String recordId) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format(
                "[%s] DATA_MODIFICATION | USER:%s | TABLE:%s | OPERATION:%s | RECORD:%s%n",
                timestamp, username, table, operation, recordId);

        writeToFile(logEntry);
        System.out.println("DATA AUDIT: " + logEntry.trim());
    }

    /**
     * Log system configuration changes
     *
     * @param username The user making the change
     * @param configItem The configuration item changed
     * @param oldValue The previous value
     * @param newValue The new value
     */
    public static void logConfigChange(String username, String configItem,
                                       String oldValue, String newValue) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format(
                "[%s] CONFIG_CHANGE | USER:%s | ITEM:%s | OLD:%s | NEW:%s%n",
                timestamp, username, configItem, oldValue, newValue);

        writeToFile(logEntry);
        System.out.println("CONFIG AUDIT: " + logEntry.trim());
    }

    /**
     * Write log entry to file
     */
    private static synchronized void writeToFile(String logEntry) {
        try {
            Path logFilePath = Paths.get(LOG_DIR, AUDIT_LOG_FILE);

            // Use append mode
            try (PrintWriter writer = new PrintWriter(
                    new FileWriter(logFilePath.toFile(), true))) {
                writer.print(logEntry);
            }

            // Rotate log if it gets too large (> 10MB)
            rotateLogs(logFilePath);

        } catch (IOException e) {
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    /**
     * Rotate log files if they exceed size limit
     */
    private static void rotateLogs(Path logFilePath) {
        try {
            long fileSize = Files.size(logFilePath);
            long maxSize = 10 * 1024 * 1024; // 10MB

            if (fileSize > maxSize) {
                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path archivedLog = Paths.get(LOG_DIR,
                        "audit_" + timestamp + ".log");

                Files.move(logFilePath, archivedLog);
                System.out.println("Audit log rotated to: " + archivedLog);
            }
        } catch (IOException e) {
            System.err.println("Failed to rotate audit log: " + e.getMessage());
        }
    }

    /**
     * Get the audit log file path
     */
    public static String getLogFilePath() {
        return Paths.get(LOG_DIR, AUDIT_LOG_FILE).toString();
    }
}