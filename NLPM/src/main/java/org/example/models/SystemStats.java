package org.example.models;


public class SystemStats {
    private int totalAlerts;
    private int criticalAlerts;
    private int warningAlerts;
    private int infoAlerts;
    private double cpuUsage;
    private double memoryUsage;
    private long packetsAnalyzed;
    private long threatsBlocked;

    public SystemStats() {
        this.totalAlerts = 0;
        this.criticalAlerts = 0;
        this.warningAlerts = 0;
        this.infoAlerts = 0;
        this.cpuUsage = 0.0;
        this.memoryUsage = 0.0;
        this.packetsAnalyzed = 0;
        this.threatsBlocked = 0;
    }

    // Getters and Setters
    public int getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(int totalAlerts) { this.totalAlerts = totalAlerts; }

    public int getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(int criticalAlerts) { this.criticalAlerts = criticalAlerts; }

    public int getWarningAlerts() { return warningAlerts; }
    public void setWarningAlerts(int warningAlerts) { this.warningAlerts = warningAlerts; }

    public int getInfoAlerts() { return infoAlerts; }
    public void setInfoAlerts(int infoAlerts) { this.infoAlerts = infoAlerts; }

    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }

    public long getPacketsAnalyzed() { return packetsAnalyzed; }
    public void setPacketsAnalyzed(long packetsAnalyzed) { this.packetsAnalyzed = packetsAnalyzed; }

    public long getThreatsBlocked() { return threatsBlocked; }
    public void setThreatsBlocked(long threatsBlocked) { this.threatsBlocked = threatsBlocked; }
}
