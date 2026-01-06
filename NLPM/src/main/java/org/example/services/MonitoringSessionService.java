package org.example.services;

import org.example.database.dao.DAOFactory;
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
 * Service layer for MonitoringSession operations.
 * Provides business logic and uses DAOFactory for data access.
 */
public class MonitoringSessionService {

    private static MonitoringSessionService instance;
    private final MonitoringSessionDAO sessionDAO;

    private MonitoringSessionService() {
        this.sessionDAO = (MonitoringSessionDAO) DAOFactory.getInstance().getMonitoringSessionDAO();
    }

    public static synchronized MonitoringSessionService getInstance() {
        if (instance == null) {
            instance = new MonitoringSessionService();
        }
        return instance;
    }

    // ========== Session Management ==========

    public int createSession(String sessionName, String interfaceName, String username) {
        return sessionDAO.createSession(sessionName, interfaceName, username);
    }

    public boolean endSession(int sessionId) {
        return sessionDAO.endSession(sessionId);
    }

    public Optional<MonitoringSession> findById(Integer sessionId) {
        return sessionDAO.findById(sessionId);
    }

    public MonitoringSession getSessionById(int sessionId) {
        return sessionDAO.getSessionById(sessionId);
    }

    public List<MonitoringSession> getAllSessions() {
        return sessionDAO.findAll();
    }

    public List<MonitoringSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return sessionDAO.getSessionsByDateRange(start, end);
    }

    public boolean deleteSession(int sessionId) {
        return sessionDAO.deleteSession(sessionId);
    }

    // ========== Statistics ==========

    public boolean updateSessionStatistics(int sessionId, SessionStatistics stats) {
        return sessionDAO.updateSessionStatistics(sessionId, stats);
    }

    public SessionStatistics getSessionStatistics(int sessionId) {
        return sessionDAO.getSessionStatistics(sessionId);
    }

    // ========== Snapshots ==========

    public boolean insertSnapshot(SessionSnapshot snapshot) {
        return sessionDAO.insertSnapshot(snapshot);
    }

    public List<SessionSnapshot> getSessionSnapshots(int sessionId) {
        return sessionDAO.getSessionSnapshots(sessionId);
    }

    // ========== Alerts ==========

    public boolean linkAlertToSession(int sessionId, SecurityAlert alert) {
        return sessionDAO.linkAlertToSession(sessionId, alert);
    }

    public int getSessionAlertCount(int sessionId) {
        return sessionDAO.getSessionAlertCount(sessionId);
    }

    // ========== Analytics ==========

    public Map<LocalDate, Long> getSessionsByDay(LocalDateTime start, LocalDateTime end) {
        return sessionDAO.getSessionsByDay(start, end);
    }

    public Map<String, Long> getGlobalProtocolDistribution() {
        return sessionDAO.getGlobalProtocolDistribution();
    }

    public Map<LocalDate, Integer> getAlertsByDay(LocalDateTime start, LocalDateTime end) {
        return sessionDAO.getAlertsByDay(start, end);
    }

    public long getTotalPacketsAcrossAllSessions() {
        return sessionDAO.getTotalPacketsAcrossAllSessions();
    }

    public long getTotalAlertsAcrossAllSessions() {
        return sessionDAO.getTotalAlertsAcrossAllSessions();
    }

    public double getAverageSessionDuration() {
        return sessionDAO.getAverageSessionDuration();
    }

    public long getSessionCount() {
        return sessionDAO.count();
    }
}
