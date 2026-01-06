package org.example.dao;

import org.example.dao.impl.AlertDAOImpl;
import org.example.dao.impl.MonitoringSessionDAOImpl;
import org.example.dao.impl.TrafficDAOImpl;
import org.example.dao.impl.UserDAOImpl;

/**
 * Factory for creating DAO instances (Singleton pattern).
 * Provides centralized access to all DAOs in the application.
 */
public class DAOFactory {

    private static DAOFactory instance;

    // DAO instances (lazy initialization)
    private UserDAOImpl userDAO;
    private TrafficDAOImpl trafficDAO;
    private AlertDAOImpl alertDAO;
    private MonitoringSessionDAOImpl monitoringSessionDAO;

    // Private constructor for singleton
    private DAOFactory() {
    }

    /**
     * Get singleton instance of DAOFactory.
     *
     * @return DAOFactory instance
     */
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    /**
     * Get UserDAO instance.
     *
     * @return UserDAOImpl
     */
    public synchronized UserDAOImpl getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOImpl();
        }
        return userDAO;
    }

    /**
     * Get TrafficDAO instance.
     *
     * @return TrafficDAOImpl
     */
    public synchronized TrafficDAOImpl getTrafficDAO() {
        if (trafficDAO == null) {
            trafficDAO = new TrafficDAOImpl();
        }
        return trafficDAO;
    }

    /**
     * Get AlertDAO instance.
     *
     * @return AlertDAOImpl
     */
    public synchronized AlertDAOImpl getAlertDAO() {
        if (alertDAO == null) {
            alertDAO = new AlertDAOImpl();
        }
        return alertDAO;
    }

    /**
     * Get MonitoringSessionDAO instance.
     *
     * @return MonitoringSessionDAOImpl
     */
    public synchronized MonitoringSessionDAOImpl getMonitoringSessionDAO() {
        if (monitoringSessionDAO == null) {
            monitoringSessionDAO = new MonitoringSessionDAOImpl();
        }
        return monitoringSessionDAO;
    }
}
