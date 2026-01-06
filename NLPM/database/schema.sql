-- ============================================
-- IDS MONITOR DATABASE SCHEMA
-- PostgreSQL Database
-- ============================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS ids;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- ============================================
-- ALERTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.alerts (
    alert_id SERIAL PRIMARY KEY,
    severity VARCHAR(20) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    source_ip VARCHAR(45) NOT NULL,
    destination_ip VARCHAR(45) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_by INTEGER REFERENCES ids.users(user_id),
    acknowledged_at TIMESTAMP
);

-- ============================================
-- TRAFFIC LOGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.traffic_logs (
    log_id BIGSERIAL PRIMARY KEY,
    protocol VARCHAR(20) NOT NULL,
    source_ip VARCHAR(45) NOT NULL,
    source_port INTEGER,
    destination_ip VARCHAR(45) NOT NULL,
    destination_port INTEGER,
    packet_size BIGINT,
    status VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- MONITORING SESSIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.monitoring_sessions (
    session_id SERIAL PRIMARY KEY,
    session_name VARCHAR(200),
    interface_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_seconds INTEGER,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- SESSION STATISTICS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.session_statistics (
    stat_id SERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL REFERENCES ids.monitoring_sessions(session_id) ON DELETE CASCADE,
    total_packets_captured BIGINT DEFAULT 0,
    total_bytes_processed BIGINT DEFAULT 0,
    tcp_packets INTEGER DEFAULT 0,
    udp_packets INTEGER DEFAULT 0,
    icmp_packets INTEGER DEFAULT 0,
    http_packets INTEGER DEFAULT 0,
    https_packets INTEGER DEFAULT 0,
    dns_packets INTEGER DEFAULT 0,
    ssh_packets INTEGER DEFAULT 0,
    other_packets INTEGER DEFAULT 0,
    total_alerts INTEGER DEFAULT 0,
    critical_alerts INTEGER DEFAULT 0,
    high_alerts INTEGER DEFAULT 0,
    medium_alerts INTEGER DEFAULT 0,
    low_alerts INTEGER DEFAULT 0,
    info_alerts INTEGER DEFAULT 0,
    inbound_packets INTEGER DEFAULT 0,
    outbound_packets INTEGER DEFAULT 0,
    local_packets INTEGER DEFAULT 0,
    port_scan_alerts INTEGER DEFAULT 0,
    ddos_alerts INTEGER DEFAULT 0,
    suspicious_port_alerts INTEGER DEFAULT 0,
    brute_force_alerts INTEGER DEFAULT 0,
    other_threats INTEGER DEFAULT 0,
    average_packet_size DECIMAL(10,2),
    peak_packet_rate INTEGER,
    average_packet_rate DECIMAL(10,2),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- SESSION SNAPSHOTS TABLE (for time-series charts)
-- ============================================
CREATE TABLE IF NOT EXISTS ids.session_snapshots (
    snapshot_id SERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL REFERENCES ids.monitoring_sessions(session_id) ON DELETE CASCADE,
    snapshot_time TIMESTAMP NOT NULL,
    packets_count INTEGER,
    bytes_count BIGINT,
    alerts_count INTEGER,
    packet_rate INTEGER,
    tcp_count INTEGER,
    udp_count INTEGER,
    http_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- SESSION ALERTS SUMMARY TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS ids.session_alerts_summary (
    summary_id SERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL REFERENCES ids.monitoring_sessions(session_id) ON DELETE CASCADE,
    alert_id INTEGER NOT NULL,
    severity VARCHAR(20),
    alert_type VARCHAR(50),
    source_ip VARCHAR(45),
    destination_ip VARCHAR(45),
    direction VARCHAR(20),
    created_at TIMESTAMP
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX IF NOT EXISTS idx_alerts_severity ON ids.alerts(severity);
CREATE INDEX IF NOT EXISTS idx_alerts_status ON ids.alerts(status);
CREATE INDEX IF NOT EXISTS idx_alerts_created ON ids.alerts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_traffic_timestamp ON ids.traffic_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_traffic_source_ip ON ids.traffic_logs(source_ip);
CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON ids.monitoring_sessions(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON ids.monitoring_sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_user ON ids.monitoring_sessions(created_by);
CREATE INDEX IF NOT EXISTS idx_session_stats_session ON ids.session_statistics(session_id);
CREATE INDEX IF NOT EXISTS idx_snapshots_session ON ids.session_snapshots(session_id, snapshot_time);
CREATE INDEX IF NOT EXISTS idx_session_alerts_session ON ids.session_alerts_summary(session_id);

-- ============================================
-- SAMPLE DATA
-- ============================================
INSERT INTO ids.users (username, password_hash, role, email)
SELECT 'admin', 'jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=', 'ADMIN', 'admin@idsmonitor.local'
WHERE NOT EXISTS (SELECT 1 FROM ids.users WHERE username = 'admin');
