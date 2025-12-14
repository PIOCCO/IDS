package org.example.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
    private Properties properties;

    private DatabaseManager() {
        loadProperties();
        initializeDataSource();
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("Unable to find database.properties");
                throw new RuntimeException("Database configuration not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database properties", e);
        }
    }

    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username"));
        config.setPassword(properties.getProperty("db.password"));
        config.setDriverClassName(properties.getProperty("db.driver"));

        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.max-size", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.min-idle", "2")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.pool.connection-timeout", "30000")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("db.pool.idle-timeout", "600000")));
        config.setMaxLifetime(Long.parseLong(properties.getProperty("db.pool.max-lifetime", "1800000")));

        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        System.out.println("Database connection pool initialized successfully");
    }

    private void initializeDatabase() {
        boolean autoCreate = Boolean.parseBoolean(properties.getProperty("db.auto-create-tables", "true"));
        if (autoCreate) {
            createTablesIfNotExist();
        }
    }

    private void createTablesIfNotExist() {
        String schema = properties.getProperty("db.schema", "ids");

        String[] sqlStatements = {
                // Create schema
                "CREATE SCHEMA IF NOT EXISTS " + schema,

                // Users table
                "CREATE TABLE IF NOT EXISTS " + schema + ".users (" +
                        "user_id SERIAL PRIMARY KEY, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "role VARCHAR(20) NOT NULL DEFAULT 'USER', " +
                        "email VARCHAR(100), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "last_login TIMESTAMP, " +
                        "is_active BOOLEAN DEFAULT TRUE)",

                // Alerts table
                "CREATE TABLE IF NOT EXISTS " + schema + ".alerts (" +
                        "alert_id SERIAL PRIMARY KEY, " +
                        "severity VARCHAR(20) NOT NULL, " +
                        "alert_type VARCHAR(50) NOT NULL, " +
                        "source_ip VARCHAR(45) NOT NULL, " +
                        "destination_ip VARCHAR(45) NOT NULL, " +
                        "description TEXT, " +
                        "status VARCHAR(20) DEFAULT 'Active', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "acknowledged_by INTEGER REFERENCES " + schema + ".users(user_id), " +
                        "acknowledged_at TIMESTAMP)",

                // Traffic logs table
                "CREATE TABLE IF NOT EXISTS " + schema + ".traffic_logs (" +
                        "log_id BIGSERIAL PRIMARY KEY, " +
                        "protocol VARCHAR(20) NOT NULL, " +
                        "source_ip VARCHAR(45) NOT NULL, " +
                        "source_port INTEGER, " +
                        "destination_ip VARCHAR(45) NOT NULL, " +
                        "destination_port INTEGER, " +
                        "packet_size BIGINT, " +
                        "status VARCHAR(20), " +
                        "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // System stats table
                "CREATE TABLE IF NOT EXISTS " + schema + ".system_stats (" +
                        "stat_id SERIAL PRIMARY KEY, " +
                        "cpu_usage DECIMAL(5,2), " +
                        "memory_usage DECIMAL(5,2), " +
                        "packets_analyzed BIGINT, " +
                        "threats_blocked INTEGER, " +
                        "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Accounts table (for the account management feature)
                "CREATE TABLE IF NOT EXISTS " + schema + ".accounts (" +
                        "account_id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL, " +
                        "color VARCHAR(20), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

                // Create indexes
                "CREATE INDEX IF NOT EXISTS idx_alerts_severity ON " + schema + ".alerts(severity)",
                "CREATE INDEX IF NOT EXISTS idx_alerts_status ON " + schema + ".alerts(status)",
                "CREATE INDEX IF NOT EXISTS idx_alerts_created ON " + schema + ".alerts(created_at DESC)",
                "CREATE INDEX IF NOT EXISTS idx_traffic_timestamp ON " + schema + ".traffic_logs(timestamp DESC)",
                "CREATE INDEX IF NOT EXISTS idx_traffic_source_ip ON " + schema + ".traffic_logs(source_ip)",

                // Insert default admin user if not exists
                "INSERT INTO " + schema + ".users (username, password_hash, role, email) " +
                        "SELECT 'admin', 'jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=', 'ADMIN', 'admin@idsmonitor.local' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM " + schema + ".users WHERE username = 'admin')"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("Database tables created/verified successfully");
        } catch (SQLException e) {
            System.err.println("Error creating database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }

    public String getSchema() {
        return properties.getProperty("db.schema", "ids");
    }
}