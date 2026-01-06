package org.example.database.dao;

import org.example.database.dao.impl.AlertDAOImpl;
import org.example.database.dao.impl.MonitoringSessionDAOImpl;
import org.example.database.dao.impl.TrafficDAOImpl;
import org.example.database.dao.impl.UserDAOImpl;
import org.example.database.dao.interfaces.AlertDAO;
import org.example.database.dao.interfaces.MonitoringSessionDAO;
import org.example.database.dao.interfaces.TrafficDAO;
import org.example.database.dao.interfaces.UserDAO;

/**
 * Factory for creating DAO instances (Singleton pattern).
 * Provides centralized access to all DAOs in the application.
 */
public class DAOFactory {

    private static DAOFactory instance;

    // DAO instances (lazy initialization)
    private UserDAO userDAO;
    private TrafficDAO trafficDAO;
    private AlertDAO alertDAO;
    private MonitoringSessionDAO monitoringSessionDAO;

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
     * @return UserDAO
     */
    public synchronized UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOImpl();
        }
        return userDAO;
    }

    /**
     * Get TrafficDAO instance.
     *
     * @return TrafficDAO
     */
    public synchronized TrafficDAO getTrafficDAO() {
        if (trafficDAO == null) {
            trafficDAO = new TrafficDAOImpl();
        }
        return trafficDAO;
    }

    /**
     * Get AlertDAO instance.
     *
     * @return AlertDAO
     */
    public synchronized AlertDAO getAlertDAO() {
        if (alertDAO == null) {
            alertDAO = new AlertDAOImpl();
        }
        return alertDAO;
    }

    /**
     * Get MonitoringSessionDAO instance.
     *
     * @return MonitoringSessionDAO
     */
    public synchronized MonitoringSessionDAO getMonitoringSessionDAO() {
        if (monitoringSessionDAO == null) {
            monitoringSessionDAO = new MonitoringSessionDAOImpl();
        }
        return monitoringSessionDAO;
    }
}
