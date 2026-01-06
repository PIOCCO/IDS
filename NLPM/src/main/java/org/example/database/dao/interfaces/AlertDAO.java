package org.example.database.dao.interfaces;

import org.example.database.dao.base.BaseDAO;
import org.example.models.SecurityAlert;

import java.util.List;
import java.util.Map;

/**
 * DAO interface for SecurityAlert entity operations.
 */
public interface AlertDAO extends BaseDAO<SecurityAlert, Integer> {

    /**
     * Get alerts filtered by severity.
     */
    List<SecurityAlert> getBySeverity(String severity);

    /**
     * Get alerts filtered by direction.
     */
    List<SecurityAlert> getByDirection(String direction);

    /**
     * Get inbound threats.
     */
    List<SecurityAlert> getInboundThreats();

    /**
     * Get recent alerts with limit.
     */
    List<SecurityAlert> getRecentAlerts(int limit);

    /**
     * Update alert status.
     */
    boolean updateStatus(String alertId, String newStatus);

    /**
     * Delete alert by ID string.
     */
    boolean deleteAlertById(String alertId);

    /**
     * Delete all alerts.
     */
    boolean deleteAllAlerts();

    /**
     * Get count by severity.
     */
    int getCountBySeverity(String severity);

    /**
     * Get inbound threat count.
     */
    int getInboundThreatCount();

    /**
     * Get total alerts count.
     */
    int getTotalAlertsCount();

    /**
     * Get alert stats by direction.
     */
    Map<String, Integer> getStatsByDirection();
}
