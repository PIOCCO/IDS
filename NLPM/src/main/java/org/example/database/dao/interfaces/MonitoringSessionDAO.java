package org.example.database.dao.interfaces;

import org.example.database.dao.base.BaseDAO;
import org.example.models.MonitoringSession;
import org.example.models.SecurityAlert;
import org.example.models.SessionSnapshot;
import org.example.models.SessionStatistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DAO interface for MonitoringSession entity operations.
 */
public interface MonitoringSessionDAO extends BaseDAO<MonitoringSession, Integer> {

    /**
     * Create a new monitoring session.
     */
    int createSession(String sessionName, String interfaceName, String username);

    /**
     * End a monitoring session.
     */
    boolean endSession(int sessionId);

    /**
     * Get session by ID.
     */
    MonitoringSession getSessionById(int sessionId);

    /**
     * Get sessions within date range.
     */
    List<MonitoringSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Delete a session and all related data.
     */
    boolean deleteSession(int sessionId);

    /**
     * Update or insert session statistics.
     */
    boolean updateSessionStatistics(int sessionId, SessionStatistics stats);

    /**
     * Get statistics for a session.
     */
    SessionStatistics getSessionStatistics(int sessionId);

    /**
     * Insert a session snapshot for time-series data.
     */
    boolean insertSnapshot(SessionSnapshot snapshot);

    /**
     * Get all snapshots for a session.
     */
    List<SessionSnapshot> getSessionSnapshots(int sessionId);

    /**
     * Link an alert to a session.
     */
    boolean linkAlertToSession(int sessionId, SecurityAlert alert);

    /**
     * Get count of alerts linked to a session.
     */
    int getSessionAlertCount(int sessionId);

    /**
     * Get sessions count by day for line chart.
     */
    Map<LocalDate, Long> getSessionsByDay(LocalDateTime start, LocalDateTime end);

    /**
     * Get global protocol distribution across all sessions.
     */
    Map<String, Long> getGlobalProtocolDistribution();

    /**
     * Get alerts by day for bar chart.
     */
    Map<LocalDate, Integer> getAlertsByDay(LocalDateTime start, LocalDateTime end);

    /**
     * Get total packets across all sessions.
     */
    long getTotalPacketsAcrossAllSessions();

    /**
     * Get total alerts across all sessions.
     */
    long getTotalAlertsAcrossAllSessions();

    /**
     * Get average session duration in seconds.
     */
    double getAverageSessionDuration();
}
