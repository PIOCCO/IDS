package org.example.dao.impl;

import org.example.models.MonitoringSession;
import org.example.models.SecurityAlert;
import org.example.models.SessionSnapshot;
import org.example.models.SessionStatistics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO for MonitoringSession entity.
 * Delegates to the existing MonitoringSessionDAO class for complex logic.
 */
public class MonitoringSessionDAOImpl {

    // Delegate to existing implementation to preserve all complex logic
    private final org.example.dao.MonitoringSessionDAO delegate;

    public MonitoringSessionDAOImpl() {
        this.delegate = new org.example.dao.MonitoringSessionDAO();
    }

    // ========== BaseDAO Implementation ==========
    public MonitoringSession save(MonitoringSession session) {
        int id = createSession(session.getSessionName(), session.getInterfaceName(), session.getCreatedBy());
        session.setSessionId(id);
        return session;
    }
    public MonitoringSession update(MonitoringSession session) {
        // Sessions are not typically updated, only ended
        return session;
    }
    public boolean delete(Integer id) {
        return deleteSession(id);
    }
    public Optional<MonitoringSession> findById(Integer id) {
        MonitoringSession session = delegate.getSessionById(id);
        return Optional.ofNullable(session);
    }
    public List<MonitoringSession> findAll() {
        return delegate.getAllSessions();
    }
    public long count() {
        return delegate.getAllSessions().size();
    }

    // ========== MonitoringSessionDAO Interface Methods ==========
    public int createSession(String sessionName, String interfaceName, String username) {
        return delegate.createSession(sessionName, interfaceName, username);
    }
    public boolean endSession(int sessionId) {
        return delegate.endSession(sessionId);
    }
    public MonitoringSession getSessionById(int sessionId) {
        return delegate.getSessionById(sessionId);
    }
    public List<MonitoringSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return delegate.getSessionsByDateRange(start, end);
    }
    public boolean deleteSession(int sessionId) {
        return delegate.deleteSession(sessionId);
    }
    public boolean updateSessionStatistics(int sessionId, SessionStatistics stats) {
        return delegate.updateSessionStatistics(sessionId, stats);
    }
    public SessionStatistics getSessionStatistics(int sessionId) {
        return delegate.getSessionStatistics(sessionId);
    }
    public boolean insertSnapshot(SessionSnapshot snapshot) {
        return delegate.insertSnapshot(snapshot);
    }
    public List<SessionSnapshot> getSessionSnapshots(int sessionId) {
        return delegate.getSessionSnapshots(sessionId);
    }
    public boolean linkAlertToSession(int sessionId, SecurityAlert alert) {
        return delegate.linkAlertToSession(sessionId, alert);
    }
    public int getSessionAlertCount(int sessionId) {
        return delegate.getSessionAlertCount(sessionId);
    }
    public Map<LocalDate, Long> getSessionsByDay(LocalDateTime start, LocalDateTime end) {
        return delegate.getSessionsByDay(start, end);
    }
    public Map<String, Long> getGlobalProtocolDistribution() {
        return delegate.getGlobalProtocolDistribution();
    }
    public Map<LocalDate, Integer> getAlertsByDay(LocalDateTime start, LocalDateTime end) {
        return delegate.getAlertsByDay(start, end);
    }
    public long getTotalPacketsAcrossAllSessions() {
        return delegate.getTotalPacketsAcrossAllSessions();
    }
    public long getTotalAlertsAcrossAllSessions() {
        return delegate.getTotalAlertsAcrossAllSessions();
    }
    public double getAverageSessionDuration() {
        return delegate.getAverageSessionDuration();
    }

    // ========== Legacy Access ==========

    /**
     * Get the underlying delegate for legacy code access.
     */
    public org.example.dao.MonitoringSessionDAO getDelegate() {
        return delegate;
    }
}
