package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration loader for database properties.
 * Provides type-safe access to configuration values.
 */
public class DatabaseConfig {

    private static final String CONFIG_FILE = "database.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }

            properties.load(input);
            System.out.println("✓ Database configuration loaded successfully");

        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }

    // ========================================
    // DATABASE PROPERTIES
    // ========================================

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsername() {
        return properties.getProperty("db.username");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }

    public static String getDriver() {
        return properties.getProperty("db.driver");
    }

    public static String getSchema() {
        return properties.getProperty("db.schema", "ids");
    }

    public static boolean isAutoCreateTables() {
        return getBooleanProperty("db.auto-create-tables", true);
    }

    // ========================================
    // HIKARICP POOL PROPERTIES
    // ========================================

    public static int getPoolMaxSize() {
        return getIntProperty("db.pool.max-size", 10);
    }

    public static int getPoolMinIdle() {
        return getIntProperty("db.pool.min-idle", 2);
    }

    public static long getConnectionTimeout() {
        return getLongProperty("db.pool.connection-timeout", 30000);
    }

    public static long getIdleTimeout() {
        return getLongProperty("db.pool.idle-timeout", 600000);
    }

    public static long getMaxLifetime() {
        return getLongProperty("db.pool.max-lifetime", 1800000);
    }

    // ========================================
    // GENERIC PROPERTY GETTERS
    // ========================================

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    public static long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid long value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }
}
