package org.example.services;

import org.example.models.SessionStatistics;

/**
 * Service layer for SessionStatistics operations.
 * Provides business logic for session statistics management.
 */
public class SessionStatisticsService {

    private static SessionStatisticsService instance;

    private SessionStatisticsService() {
    }

    public static synchronized SessionStatisticsService getInstance() {
        if (instance == null) {
            instance = new SessionStatisticsService();
        }
        return instance;
    }

    /**
     * Create a new SessionStatistics object.
     */
    public SessionStatistics createStatistics() {
        return new SessionStatistics();
    }

    /**
     * Calculate statistics from raw data.
     */
    public SessionStatistics calculateStatistics(int sessionId, long totalPackets, long totalBytes, int alertCount) {
        SessionStatistics stats = new SessionStatistics();
        stats.setSessionId(sessionId);
        stats.setTotalPacketsCaptured(totalPackets);
        stats.setTotalBytesProcessed(totalBytes);
        stats.setTotalAlerts(alertCount);
        return stats;
    }

    /**
     * Get formatted bytes display (KB, MB, GB).
     */
    public String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Get total packet count from statistics.
     */
    public long getTotalPackets(SessionStatistics stats) {
        return stats.getTotalPacketsCaptured();
    }

    /**
     * Get total bytes from statistics.
     */
    public long getTotalBytes(SessionStatistics stats) {
        return stats.getTotalBytesProcessed();
    }
}
