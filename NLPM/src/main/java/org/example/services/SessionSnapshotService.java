package org.example.services;

import org.example.models.SessionSnapshot;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for SessionSnapshot operations.
 * Provides business logic for session snapshot management.
 */
public class SessionSnapshotService {

    private static SessionSnapshotService instance;

    private SessionSnapshotService() {
    }

    public static synchronized SessionSnapshotService getInstance() {
        if (instance == null) {
            instance = new SessionSnapshotService();
        }
        return instance;
    }

    /**
     * Create a new snapshot for a session.
     */
    public SessionSnapshot createSnapshot(int sessionId, int packetsCount, long bytesCount, int alertCount) {
        SessionSnapshot snapshot = new SessionSnapshot(sessionId);
        snapshot.setPacketsCount(packetsCount);
        snapshot.setBytesCount(bytesCount);
        snapshot.setAlertsCount(alertCount);
        snapshot.setSnapshotTime(LocalDateTime.now());
        return snapshot;
    }

    /**
     * Calculate delta between two snapshots.
     */
    public SessionSnapshot calculateDelta(SessionSnapshot previous, SessionSnapshot current) {
        SessionSnapshot delta = new SessionSnapshot(current.getSessionId());
        delta.setPacketsCount(current.getPacketsCount() - previous.getPacketsCount());
        delta.setBytesCount(current.getBytesCount() - previous.getBytesCount());
        delta.setAlertsCount(current.getAlertsCount() - previous.getAlertsCount());
        delta.setSnapshotTime(current.getSnapshotTime());
        return delta;
    }

    /**
     * Calculate average from list of snapshots.
     */
    public double calculateAveragePacketsPerSnapshot(List<SessionSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return 0;
        }
        long total = snapshots.stream().mapToLong(SessionSnapshot::getPacketsCount).sum();
        return (double) total / snapshots.size();
    }

    /**
     * Get latest snapshot from list.
     */
    public SessionSnapshot getLatestSnapshot(List<SessionSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return null;
        }
        return snapshots.stream()
                .max((s1, s2) -> s1.getSnapshotTime().compareTo(s2.getSnapshotTime()))
                .orElse(null);
    }
}
