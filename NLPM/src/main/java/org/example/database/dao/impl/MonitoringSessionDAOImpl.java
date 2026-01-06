package org.example.database.dao.impl;

import org.example.database.dao.interfaces.MonitoringSessionDAO;
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
 * Implementation of MonitoringSessionDAO interface.
 * Delegates to the existing MonitoringSessionDAO class for complex logic.
 */
public class MonitoringSessionDAOImpl implements MonitoringSessionDAO {

    // Delegate to existing implementation to preserve all complex logic
    private final org.example.database.dao.MonitoringSessionDAO delegate;

    public MonitoringSessionDAOImpl() {
        this.delegate = new org.example.database.dao.MonitoringSessionDAO();
    }

    // ========== BaseDAO Implementation ==========

    @Override
    public MonitoringSession save(MonitoringSession session) {
        int id = createSession(session.getSessionName(), session.getInterfaceName(), session.getCreatedBy());
        session.setSessionId(id);
        return session;
    }

    @Override
    public MonitoringSession update(MonitoringSession session) {
        // Sessions are not typically updated, only ended
        return session;
    }

    @Override
    public boolean delete(Integer id) {
        return deleteSession(id);
    }

    @Override
    public Optional<MonitoringSession> findById(Integer id) {
        MonitoringSession session = delegate.getSessionById(id);
        return Optional.ofNullable(session);
    }

    @Override
    public List<MonitoringSession> findAll() {
        return delegate.getAllSessions();
    }

    @Override
    public long count() {
        return delegate.getAllSessions().size();
    }

    // ========== MonitoringSessionDAO Interface Methods ==========

    @Override
    public int createSession(String sessionName, String interfaceName, String username) {
        return delegate.createSession(sessionName, interfaceName, username);
    }

    @Override
    public boolean endSession(int sessionId) {
        return delegate.endSession(sessionId);
    }

    @Override
    public MonitoringSession getSessionById(int sessionId) {
        return delegate.getSessionById(sessionId);
    }

    @Override
    public List<MonitoringSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return delegate.getSessionsByDateRange(start, end);
    }

    @Override
    public boolean deleteSession(int sessionId) {
        return delegate.deleteSession(sessionId);
    }

    @Override
    public boolean updateSessionStatistics(int sessionId, SessionStatistics stats) {
        return delegate.updateSessionStatistics(sessionId, stats);
    }

    @Override
    public SessionStatistics getSessionStatistics(int sessionId) {
        return delegate.getSessionStatistics(sessionId);
    }

    @Override
    public boolean insertSnapshot(SessionSnapshot snapshot) {
        return delegate.insertSnapshot(snapshot);
    }

    @Override
    public List<SessionSnapshot> getSessionSnapshots(int sessionId) {
        return delegate.getSessionSnapshots(sessionId);
    }

    @Override
    public boolean linkAlertToSession(int sessionId, SecurityAlert alert) {
        return delegate.linkAlertToSession(sessionId, alert);
    }

    @Override
    public int getSessionAlertCount(int sessionId) {
        return delegate.getSessionAlertCount(sessionId);
    }

    @Override
    public Map<LocalDate, Long> getSessionsByDay(LocalDateTime start, LocalDateTime end) {
        return delegate.getSessionsByDay(start, end);
    }

    @Override
    public Map<String, Long> getGlobalProtocolDistribution() {
        return delegate.getGlobalProtocolDistribution();
    }

    @Override
    public Map<LocalDate, Integer> getAlertsByDay(LocalDateTime start, LocalDateTime end) {
        return delegate.getAlertsByDay(start, end);
    }

    @Override
    public long getTotalPacketsAcrossAllSessions() {
        return delegate.getTotalPacketsAcrossAllSessions();
    }

    @Override
    public long getTotalAlertsAcrossAllSessions() {
        return delegate.getTotalAlertsAcrossAllSessions();
    }

    @Override
    public double getAverageSessionDuration() {
        return delegate.getAverageSessionDuration();
    }

    // ========== Legacy Access ==========

    /**
     * Get the underlying delegate for legacy code access.
     */
    public org.example.database.dao.MonitoringSessionDAO getDelegate() {
        return delegate;
    }
}
