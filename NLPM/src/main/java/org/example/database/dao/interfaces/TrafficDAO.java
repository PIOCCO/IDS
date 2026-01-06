package org.example.database.dao.interfaces;

import org.example.database.dao.base.BaseDAO;
import org.example.models.TrafficData;

import java.util.List;
import java.util.Map;

/**
 * DAO interface for Traffic entity operations.
 */
public interface TrafficDAO extends BaseDAO<TrafficData, Long> {

    /**
     * Get traffic filtered by protocol.
     */
    List<TrafficData> getByProtocol(String protocol);

    /**
     * Get traffic from the last N minutes.
     */
    List<TrafficData> getRecentTraffic(int minutes);

    /**
     * Delete all traffic data.
     */
    boolean deleteAllTraffic();

    /**
     * Delete traffic older than specified days.
     */
    int deleteOldTraffic(int days);

    /**
     * Delete traffic by protocol.
     */
    int deleteByProtocol(String protocol);

    /**
     * Delete traffic by IP address.
     */
    int deleteByIP(String ipAddress);

    /**
     * Get total packets analyzed.
     */
    long getTotalPacketsAnalyzed();

    /**
     * Get active connections count.
     */
    int getActiveConnectionsCount();

    /**
     * Get traffic statistics for dashboard.
     */
    Map<String, Object> getTrafficStatistics();
}
