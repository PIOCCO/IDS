package org.example.services;

import org.example.dao.DAOFactory;
import org.example.dao.impl.TrafficDAOImpl;
import org.example.models.TrafficData;

import java.util.List;
import java.util.Map;

/**
 * Service layer for Traffic operations.
 * Provides business logic and uses DAOFactory for data access.
 */
public class TrafficService {

    private static TrafficService instance;
    private final TrafficDAOImpl trafficDAO;

    private TrafficService() {
        this.trafficDAO = DAOFactory.getInstance().getTrafficDAO();
    }

    public static synchronized TrafficService getInstance() {
        if (instance == null) {
            instance = new TrafficService();
        }
        return instance;
    }

    // ========== CRUD Operations ==========

    public TrafficData saveTraffic(TrafficData traffic) {
        return trafficDAO.save(traffic);
    }

    public List<TrafficData> getAllTraffic() {
        return trafficDAO.findAll();
    }

    public List<TrafficData> getTrafficByProtocol(String protocol) {
        return trafficDAO.getByProtocol(protocol);
    }

    public List<TrafficData> getRecentTraffic(int minutes) {
        return trafficDAO.getRecentTraffic(minutes);
    }

    // ========== Delete Operations ==========

    public boolean deleteAllTraffic() {
        return trafficDAO.deleteAllTraffic();
    }

    public int deleteOldTraffic(int days) {
        return trafficDAO.deleteOldTraffic(days);
    }

    public int deleteByProtocol(String protocol) {
        return trafficDAO.deleteByProtocol(protocol);
    }

    public int deleteByIP(String ipAddress) {
        return trafficDAO.deleteByIP(ipAddress);
    }

    // ========== Statistics ==========

    public long getTotalPacketsAnalyzed() {
        return trafficDAO.getTotalPacketsAnalyzed();
    }

    public int getActiveConnectionsCount() {
        return trafficDAO.getActiveConnectionsCount();
    }

    public Map<String, Object> getTrafficStatistics() {
        return trafficDAO.getTrafficStatistics();
    }

    public long getTrafficCount() {
        return trafficDAO.count();
    }
}
