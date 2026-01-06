package org.example.services;

import org.example.dao.DAOFactory;
import org.example.dao.impl.AlertDAOImpl;
import org.example.models.SecurityAlert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for Alert operations.
 * Provides business logic and uses DAOFactory for data access.
 */
public class AlertService {

    private static AlertService instance;
    private final AlertDAOImpl alertDAO;

    private AlertService() {
        this.alertDAO = DAOFactory.getInstance().getAlertDAO();
    }

    public static synchronized AlertService getInstance() {
        if (instance == null) {
            instance = new AlertService();
        }
        return instance;
    }

    // ========== CRUD Operations ==========

    public SecurityAlert createAlert(SecurityAlert alert) {
        return alertDAO.save(alert);
    }

    public Optional<SecurityAlert> findById(Integer id) {
        return alertDAO.findById(id);
    }

    public List<SecurityAlert> getAllAlerts() {
        return alertDAO.findAll();
    }

    public List<SecurityAlert> getAlertsBySeverity(String severity) {
        return alertDAO.getBySeverity(severity);
    }

    public List<SecurityAlert> getAlertsByDirection(String direction) {
        return alertDAO.getByDirection(direction);
    }

    public List<SecurityAlert> getInboundThreats() {
        return alertDAO.getInboundThreats();
    }

    public List<SecurityAlert> getRecentAlerts(int limit) {
        return alertDAO.getRecentAlerts(limit);
    }

    public boolean updateStatus(String alertId, String newStatus) {
        return alertDAO.updateStatus(alertId, newStatus);
    }

    public boolean deleteAlert(String alertId) {
        return alertDAO.deleteAlertById(alertId);
    }

    public boolean deleteAllAlerts() {
        return alertDAO.deleteAllAlerts();
    }

    // ========== Business Logic ==========

    public boolean acknowledgeAlert(String alertId) {
        return alertDAO.updateStatus(alertId, "Acknowledged");
    }

    public boolean resolveAlert(String alertId) {
        return alertDAO.updateStatus(alertId, "Resolved");
    }

    // ========== Statistics ==========

    public int getCountBySeverity(String severity) {
        return alertDAO.getCountBySeverity(severity);
    }

    public int getInboundThreatCount() {
        return alertDAO.getInboundThreatCount();
    }

    public int getTotalAlertsCount() {
        return alertDAO.getTotalAlertsCount();
    }

    public Map<String, Integer> getStatsByDirection() {
        return alertDAO.getStatsByDirection();
    }

    public long getAlertCount() {
        return alertDAO.count();
    }

    // ========== Severity Counts ==========

    public int getCriticalAlertCount() {
        return getCountBySeverity("CRITICAL");
    }

    public int getHighAlertCount() {
        return getCountBySeverity("HIGH");
    }

    public int getMediumAlertCount() {
        return getCountBySeverity("MEDIUM");
    }

    public int getLowAlertCount() {
        return getCountBySeverity("LOW");
    }
}
