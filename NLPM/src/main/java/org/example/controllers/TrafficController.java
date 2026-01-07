package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.services.TrafficService;
import org.example.models.TrafficData;
import org.example.services.PacketCaptureService;
import org.example.services.AuthenticationService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;

public class TrafficController implements Initializable {

    @FXML
    private TableView<TrafficData> trafficTable;

    @FXML
    private TableColumn<TrafficData, String> protocolColumn;

    @FXML
    private TableColumn<TrafficData, String> sourceIPColumn;

    @FXML
    private TableColumn<TrafficData, String> sourcePortColumn;

    @FXML
    private TableColumn<TrafficData, String> destIPColumn;

    @FXML
    private TableColumn<TrafficData, String> destPortColumn;

    @FXML
    private TableColumn<TrafficData, Long> packetSizeColumn;

    @FXML
    private TableColumn<TrafficData, String> timestampColumn;

    @FXML
    private TableColumn<TrafficData, String> statusColumn;

    @FXML
    private ComboBox<String> protocolFilter;

    @FXML
    private ComboBox<String> interfaceSelector;

    @FXML
    private Button startMonitorBtn;

    @FXML
    private Button stopMonitorBtn;

    @FXML
    private Button clearTrafficBtn;

    @FXML
    private Button exportBtn;

    @FXML
    private Button reloadBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label packetsLabel;

    @FXML
    private Label bytesLabel;

    @FXML
    private Label threatsLabel;

    @FXML
    private Label activeConnectionsLabel;

    private ObservableList<TrafficData> trafficList;
    private TrafficService trafficService;
    private PacketCaptureService captureService;
    private AuthenticationService authService;
    private Timer refreshTimer;

    // Session tracking for Reports
    private int currentSessionId = -1;
    private Timer snapshotTimer;
    private org.example.services.MonitoringSessionService sessionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            trafficService = TrafficService.getInstance();
            captureService = PacketCaptureService.getInstance();
            authService = AuthenticationService.getInstance();
            sessionService = org.example.services.MonitoringSessionService.getInstance();

            initializeTable();
            initializeControls();
            loadTrafficData();
            loadNetworkInterfaces();
            updateStatistics();

            // ===== CRITICAL: Restore session state from singleton =====
            // This ensures session state persists across page navigations
            restoreSessionState();

            System.out.println("TrafficController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing TrafficController: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to initialize Traffic Monitor: " + e.getMessage());
        }
    }

    /**
     * Restore session state from PacketCaptureService singleton.
     * This is crucial for maintaining session continuity when user navigates
     * away from Traffic page and returns.
     */
    private void restoreSessionState() {
        // Check if capture is still running (user navigated away without stopping)
        if (captureService.isCapturing()) {
            // Restore session ID from the singleton
            currentSessionId = captureService.getCurrentSessionId();

            if (currentSessionId > 0) {
                System.out.println("🔄 Restored active session: " + currentSessionId);

                // Sync UI to reflect active capture state
                syncUIWithCaptureState();

                // Restart snapshot timer for this controller instance
                startSnapshotTimer();

                // Start auto-refresh timer
                startAutoRefresh();
            }
        } else {
            // No active capture, reset session ID
            currentSessionId = -1;
        }
    }

    private void initializeTable() {
        try {
            // Make columns fill entire table width
            trafficTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            protocolColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProtocol()));
            sourceIPColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));
            sourcePortColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourcePort()));
            destIPColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDestinationIP()));
            destPortColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getDestinationPort()));
            packetSizeColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().getPacketSize())
                            .asObject());
            timestampColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestamp()));
            statusColumn.setCellValueFactory(
                    cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

            System.out.println("Table columns initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeControls() {
        try {
            // Initialize protocol filter
            if (protocolFilter != null) {
                protocolFilter.setItems(FXCollections.observableArrayList(
                        "All", "TCP", "UDP", "HTTP", "HTTPS", "ICMP", "DNS", "SSH", "FTP"));
                protocolFilter.setValue("All");
                protocolFilter.setOnAction(e -> applyProtocolFilter());
            }

            // Initialize buttons
            if (startMonitorBtn != null) {
                startMonitorBtn.setOnAction(e -> startMonitoring());
            }
            if (stopMonitorBtn != null) {
                stopMonitorBtn.setOnAction(e -> stopMonitoring());
                stopMonitorBtn.setDisable(true);
            }
            if (clearTrafficBtn != null) {
                clearTrafficBtn.setOnAction(e -> handleClearTraffic());
            }
            if (exportBtn != null) {
                exportBtn.setOnAction(e -> handleExportTraffic());
            }
            if (reloadBtn != null) {
                reloadBtn.setOnAction(e -> {
                    loadTrafficData();
                    updateStatistics();
                    System.out.println("Traffic data reloaded manually");
                });
            }

            // Initialize statistics labels
            if (packetsLabel != null)
                packetsLabel.setText("0");
            if (bytesLabel != null)
                bytesLabel.setText("0");
            if (threatsLabel != null)
                threatsLabel.setText("0");
            if (activeConnectionsLabel != null)
                activeConnectionsLabel.setText("0");

            // IMPORTANT: Sync UI state with PacketCaptureService
            // This handles the case when user navigates away and returns
            syncUIWithCaptureState();

            System.out.println("Controls initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing controls: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Synchronize UI buttons with the actual state of PacketCaptureService
     * This is crucial when navigating back to this page while capture is running
     */
    private void syncUIWithCaptureState() {
        boolean isCurrentlyCapturing = captureService.isCapturing();

        if (isCurrentlyCapturing) {
            // Capture is running in background - update UI to reflect this
            System.out.println("Sync: Capture is already running - updating UI state");

            if (statusLabel != null) {
                statusLabel.setText("Status: Monitoring Active");
                statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
            }
            if (startMonitorBtn != null)
                startMonitorBtn.setDisable(true);
            if (stopMonitorBtn != null)
                stopMonitorBtn.setDisable(false);
            if (interfaceSelector != null)
                interfaceSelector.setDisable(true);

            // Restart auto-refresh since this is a new controller instance
            startAutoRefresh();
        } else {
            // Capture is not running - ensure UI is in default state
            if (statusLabel != null) {
                statusLabel.setText("Status: Stopped");
                statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            }
            if (startMonitorBtn != null)
                startMonitorBtn.setDisable(false);
            if (stopMonitorBtn != null)
                stopMonitorBtn.setDisable(true);
            if (interfaceSelector != null)
                interfaceSelector.setDisable(false);
        }
    }

    private void loadNetworkInterfaces() {
        try {
            if (interfaceSelector != null) {
                String[] interfaces = PacketCaptureService.getAvailableInterfaces();
                interfaceSelector.setItems(FXCollections.observableArrayList(interfaces));
                if (interfaces.length > 0) {
                    interfaceSelector.setValue(interfaces[0]);
                }
                System.out.println("Loaded " + interfaces.length + " network interfaces");
            }
        } catch (Exception e) {
            System.err.println("Error loading network interfaces: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load network interfaces. Make sure you have administrator privileges.");
        }
    }

    private void loadTrafficData() {
        try {
            List<TrafficData> traffic = trafficService.getRecentTraffic(5); // Last 5 minutes
            trafficList = FXCollections.observableArrayList(traffic);

            if (trafficTable != null) {
                trafficTable.setItems(trafficList);
            }

            System.out.println("Loaded " + traffic.size() + " traffic records from database");
        } catch (Exception e) {
            System.err.println("Error loading traffic data: " + e.getMessage());
            e.printStackTrace();

            trafficList = FXCollections.observableArrayList();
            if (trafficTable != null) {
                trafficTable.setItems(trafficList);
            }

            showError("Failed to load traffic data from database: " + e.getMessage());
        }
    }

    private void applyProtocolFilter() {
        try {
            if (protocolFilter == null)
                return;

            String protocol = protocolFilter.getValue();

            if (protocol.equals("All")) {
                loadTrafficData();
            } else {
                List<TrafficData> filtered = trafficService.getTrafficByProtocol(protocol);
                trafficList = FXCollections.observableArrayList(filtered);
                if (trafficTable != null) {
                    trafficTable.setItems(trafficList);
                }
            }
        } catch (Exception e) {
            System.err.println("Error filtering traffic: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to filter traffic: " + e.getMessage());
        }
    }

    private void startMonitoring() {
        try {
            // Check 1: Interface selector exists
            if (interfaceSelector == null || interfaceSelector.getValue() == null) {
                showError("Erreur: Veuillez sélectionner une interface réseau");
                return;
            }

            String selectedInterface = interfaceSelector.getValue();

            // Check 2: Interface is selected
            if (selectedInterface.isEmpty()) {
                showError("Erreur: Aucune interface réseau sélectionnée");
                return;
            }

            // Check 3: Capture already running?
            if (captureService.isCapturing()) {
                showError("Erreur: La capture est déjà en cours. Utilisez le bouton Stop pour arrêter.");
                // Sync UI since it might be out of sync
                syncUIWithCaptureState();
                return;
            }

            // Extract interface name (before the " - " separator)
            String interfaceName = selectedInterface.split(" - ")[0];

            // ===== SESSION TRACKING: Create session BEFORE starting capture =====
            String username = authService.getCurrentUser() != null ? authService.getCurrentUser().getUsername()
                    : "unknown";
            String sessionName = "Session " + java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            currentSessionId = sessionService.createSession(sessionName, interfaceName, username);

            if (currentSessionId <= 0) {
                System.err.println("⚠️ Warning: Failed to create session in database");
            } else {
                System.out.println("✅ Created monitoring session ID: " + currentSessionId);
                // Set session ID in capture service for alert linking
                captureService.setCurrentSessionId(currentSessionId);
            }

            // Check 4: Try to start capture
            boolean started = captureService.startCapture(interfaceName);

            if (started) {
                if (statusLabel != null) {
                    statusLabel.setText("Status: Monitoring Active");
                    statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                }
                if (startMonitorBtn != null)
                    startMonitorBtn.setDisable(true);
                if (stopMonitorBtn != null)
                    stopMonitorBtn.setDisable(false);
                if (interfaceSelector != null)
                    interfaceSelector.setDisable(true);

                // Log the action
                logAction("START_MONITORING", "Started packet capture on interface: " + interfaceName);

                // ===== SESSION TRACKING: Start snapshot timer =====
                startSnapshotTimer();

                // Start auto-refresh timer
                startAutoRefresh();

                showSuccess("Network monitoring started successfully" +
                        (currentSessionId > 0 ? " (Session #" + currentSessionId + ")" : ""));
                System.out.println("Network monitoring started on: " + interfaceName);
            } else {
                // Check 5: Capture failed - distinct error message
                // Mark session as failed if created
                if (currentSessionId > 0) {
                    sessionService.endSession(currentSessionId);
                    currentSessionId = -1;
                }
                showError(
                        "Erreur: Échec du démarrage de la capture. Vérifiez les privilèges administrateur ou l'interface sélectionnée.");
            }
        } catch (Exception e) {
            System.err.println("Error starting monitoring: " + e.getMessage());
            e.printStackTrace();
            // Check 6: Exception during start
            showError("Erreur système: " + e.getMessage());
        }
    }

    private void stopMonitoring() {
        try {
            // ===== SESSION TRACKING: Stop snapshot timer FIRST =====
            stopSnapshotTimer();

            captureService.stopCapture();

            if (statusLabel != null) {
                statusLabel.setText("Status: Monitoring Stopped");
                statusLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
            }
            if (startMonitorBtn != null)
                startMonitorBtn.setDisable(false);
            if (stopMonitorBtn != null)
                stopMonitorBtn.setDisable(true);
            if (interfaceSelector != null)
                interfaceSelector.setDisable(false);

            // Log the action
            logAction("STOP_MONITORING", "Stopped packet capture");

            // ===== SESSION TRACKING: Finalize session =====
            if (currentSessionId > 0) {
                finalizeSession();
                currentSessionId = -1;
            }

            // Stop auto-refresh
            stopAutoRefresh();

            showInfo("Network monitoring stopped. Report available in Reports tab.");
            System.out.println("Network monitoring stopped");
        } catch (Exception e) {
            System.err.println("Error stopping monitoring: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to stop monitoring: " + e.getMessage());
        }
    }

    // ==================== SESSION TRACKING METHODS ====================

    private void startSnapshotTimer() {
        stopSnapshotTimer(); // Stop any existing timer

        snapshotTimer = new Timer(true); // daemon thread
        snapshotTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (currentSessionId > 0 && captureService.isCapturing()) {
                        captureSessionSnapshot();
                    }
                } catch (Exception e) {
                    System.err.println("❌ Snapshot error: " + e.getMessage());
                }
            }
        }, 30000, 30000); // Delay 30s, Period 30s

        System.out.println("📸 Snapshot timer started (every 30s)");
    }

    private void stopSnapshotTimer() {
        if (snapshotTimer != null) {
            snapshotTimer.cancel();
            snapshotTimer = null;
            System.out.println("📸 Snapshot timer stopped");
        }
    }

    private void captureSessionSnapshot() {
        try {
            org.example.models.SessionSnapshot snapshot = new org.example.models.SessionSnapshot();
            snapshot.setSessionId(currentSessionId);
            snapshot.setSnapshotTime(java.time.LocalDateTime.now());
            snapshot.setPacketsCount((int) captureService.getPacketsAnalyzed());
            snapshot.setBytesCount(captureService.getBytesProcessed());
            snapshot.setPacketRate(calculatePacketRate());

            // Protocol counts from current table data
            if (trafficList != null) {
                snapshot.setTcpCount((int) trafficList.stream()
                        .filter(t -> "TCP".equals(t.getProtocol())).count());
                snapshot.setUdpCount((int) trafficList.stream()
                        .filter(t -> "UDP".equals(t.getProtocol())).count());
                snapshot.setHttpCount((int) trafficList.stream()
                        .filter(t -> "HTTP".equals(t.getProtocol())).count());
            }

            boolean saved = sessionService.insertSnapshot(snapshot);
            if (saved) {
                System.out.println("📸 Snapshot captured for session " + currentSessionId);
            }
        } catch (Exception e) {
            System.err.println("Error capturing snapshot: " + e.getMessage());
        }
    }

    private int calculatePacketRate() {
        // Simple approximation: packets per second based on current stats
        try {
            return (int) (captureService.getPacketsAnalyzed() / 30); // Average over 30 seconds
        } catch (Exception e) {
            return 0;
        }
    }

    private void finalizeSession() {
        try {
            System.out.println("🔄 Finalizing session " + currentSessionId + "...");

            // 1. End session (set end_time, duration, status)
            boolean ended = sessionService.endSession(currentSessionId);
            if (!ended) {
                System.err.println("❌ Failed to end session");
                return;
            }

            // 2. Collect statistics
            org.example.models.SessionStatistics stats = new org.example.models.SessionStatistics();
            stats.setSessionId(currentSessionId);

            // Packet stats
            stats.setTotalPacketsCaptured(captureService.getPacketsAnalyzed());
            stats.setTotalBytesProcessed(captureService.getBytesProcessed());

            // Protocol distribution from traffic table
            if (trafficList != null) {
                stats.setTcpPackets((int) trafficList.stream()
                        .filter(t -> "TCP".equals(t.getProtocol())).count());
                stats.setUdpPackets((int) trafficList.stream()
                        .filter(t -> "UDP".equals(t.getProtocol())).count());
                stats.setHttpPackets((int) trafficList.stream()
                        .filter(t -> "HTTP".equals(t.getProtocol())).count());
                stats.setHttpsPackets((int) trafficList.stream()
                        .filter(t -> "HTTPS".equals(t.getProtocol())).count());
                stats.setDnsPackets((int) trafficList.stream()
                        .filter(t -> "DNS".equals(t.getProtocol())).count());
                stats.setIcmpPackets((int) trafficList.stream()
                        .filter(t -> "ICMP".equals(t.getProtocol())).count());
                stats.setSshPackets((int) trafficList.stream()
                        .filter(t -> "SSH".equals(t.getProtocol())).count());
            }

            // Alert stats from session
            int alertCount = sessionService.getSessionAlertCount(currentSessionId);
            stats.setTotalAlerts(alertCount);

            // Average packet size
            if (stats.getTotalPacketsCaptured() > 0) {
                stats.setAveragePacketSize(
                        (double) stats.getTotalBytesProcessed() / stats.getTotalPacketsCaptured());
            }

            // 3. Save statistics
            boolean saved = sessionService.updateSessionStatistics(currentSessionId, stats);

            if (saved) {
                System.out.println("✅ Session " + currentSessionId + " finalized successfully");
                System.out.println("   📊 " + stats.getTotalPacketsCaptured() + " packets");
                System.out.println("   📦 " + stats.getTotalBytesProcessed() + " bytes");
                System.out.println("   🚨 " + stats.getTotalAlerts() + " alerts");
            } else {
                System.err.println("❌ Failed to save session statistics");
            }

        } catch (Exception e) {
            System.err.println("❌ Error finalizing session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle clearing traffic data from the dashboard
     */
    private void handleClearTraffic() {
        try {
            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Clear Traffic Data");
            confirmAlert.setHeaderText("Are you sure you want to clear traffic data?");
            confirmAlert.setContentText("This action cannot be undone. Choose an option:");
            org.example.utils.DialogUtils.styleAlert(confirmAlert);

            ButtonType clearAllBtn = new ButtonType("Clear All Traffic");
            ButtonType clearVisibleBtn = new ButtonType("Clear Visible Only");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirmAlert.getButtonTypes().setAll(clearAllBtn, clearVisibleBtn, cancelBtn);

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == clearAllBtn) {
                    clearAllTraffic();
                } else if (result.get() == clearVisibleBtn) {
                    clearVisibleTraffic();
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling clear traffic: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to clear traffic: " + e.getMessage());
        }
    }

    /**
     * Clear all traffic data from database
     */
    private void clearAllTraffic() {
        try {
            boolean success = trafficService.deleteAllTraffic();

            if (success) {
                // Clear the table view
                if (trafficList != null) {
                    trafficList.clear();
                }
                if (trafficTable != null) {
                    trafficTable.setItems(trafficList);
                }

                // Reset statistics
                if (packetsLabel != null)
                    packetsLabel.setText("0");
                if (bytesLabel != null)
                    bytesLabel.setText("0");
                if (activeConnectionsLabel != null)
                    activeConnectionsLabel.setText("0");

                // Log the action
                logAction("CLEAR_ALL_TRAFFIC", "Cleared all traffic data from database");

                showSuccess("All traffic data has been cleared successfully");
                System.out.println("All traffic data cleared");
            } else {
                showError("Failed to clear traffic data");
            }
        } catch (Exception e) {
            System.err.println("Error clearing traffic data: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred while clearing traffic data: " + e.getMessage());
        }
    }

    /**
     * Clear only visible (filtered) traffic data
     */
    private void clearVisibleTraffic() {
        try {
            int count = trafficList != null ? trafficList.size() : 0;

            // Clear visible items from table
            if (trafficList != null) {
                trafficList.clear();
            }
            if (trafficTable != null) {
                trafficTable.setItems(trafficList);
            }

            // Log the action
            logAction("CLEAR_VISIBLE_TRAFFIC", "Cleared " + count + " visible traffic records");

            showSuccess(count + " visible traffic records cleared from view");
            System.out.println("Cleared " + count + " visible records");
        } catch (Exception e) {
            System.err.println("Error clearing visible traffic: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred while clearing visible traffic: " + e.getMessage());
        }
    }

    /**
     * Export traffic data to CSV
     */
    private void handleExportTraffic() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Traffic Data");
            fileChooser.setInitialFileName("traffic_export_" +
                    java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".csv");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            javafx.stage.Window window = null;
            if (trafficTable != null && trafficTable.getScene() != null) {
                window = trafficTable.getScene().getWindow();
            }

            java.io.File file = fileChooser.showSaveDialog(window);

            if (file != null) {
                exportToCSV(file);

                // Log the action
                logAction("EXPORT_TRAFFIC", "Exported traffic data to: " + file.getAbsolutePath());

                showSuccess("Traffic data exported successfully to: " + file.getName());
            }
        } catch (Exception e) {
            System.err.println("Error exporting traffic data: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to export traffic data: " + e.getMessage());
        }
    }

    /**
     * Export traffic list to CSV file
     */
    private void exportToCSV(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write header
            writer.println(
                    "Protocol,Source IP,Source Port,Destination IP,Destination Port,Packet Size,Timestamp,Status");

            // Write data
            if (trafficList != null) {
                for (TrafficData traffic : trafficList) {
                    writer.println(String.format("%s,%s,%s,%s,%s,%d,%s,%s",
                            traffic.getProtocol(),
                            traffic.getSourceIP(),
                            traffic.getSourcePort(),
                            traffic.getDestinationIP(),
                            traffic.getDestinationPort(),
                            traffic.getPacketSize(),
                            traffic.getTimestamp(),
                            traffic.getStatus()));
                }
            }
        }
    }

    private void startAutoRefresh() {
        try {
            stopAutoRefresh(); // Stop any existing timer

            refreshTimer = new Timer(true);
            refreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            // Use applyProtocolFilter() instead of loadTrafficData()
                            // to respect the currently selected protocol filter
                            applyProtocolFilter();
                            updateStatistics();
                        } catch (Exception e) {
                            System.err.println("Error in auto-refresh: " + e.getMessage());
                        }
                    });
                }
            }, 0, 2000); // Refresh every 2 seconds
        } catch (Exception e) {
            System.err.println("Error starting auto-refresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void updateStatistics() {
        try {
            if (packetsLabel != null) {
                long packets = captureService.getPacketsAnalyzed();
                packetsLabel.setText(String.format("%,d", packets));
            }
            if (bytesLabel != null) {
                long bytes = captureService.getBytesProcessed();
                String formatted = bytes > 1_000_000 ? String.format("%.2f MB", bytes / 1_000_000.0)
                        : String.format("%.2f KB", bytes / 1_000.0);
                bytesLabel.setText(formatted);
            }
            if (threatsLabel != null) {
                long threats = org.example.services.DetectionEngine.getInstance().getTotalThreatsDetected();
                threatsLabel.setText(String.valueOf(threats));
            }
            if (activeConnectionsLabel != null) {
                int connections = trafficService.getActiveConnectionsCount();
                activeConnectionsLabel.setText(String.valueOf(connections));
            }
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }

    /**
     * Log user actions (with fallback if AuditLogger is not available)
     */
    private void logAction(String action, String details) {
        try {
            if (authService != null && authService.getCurrentUser() != null) {
                String username = authService.getCurrentUser().getUsername();
                // Try to use AuditLogger if available
                try {
                    Class.forName("org.example.utils.AuditLogger");
                    org.example.utils.AuditLogger.log(username, action, details);
                } catch (ClassNotFoundException e) {
                    // AuditLogger not available, just log to console
                    System.out.println("ACTION: " + username + " | " + action + " | " + details);
                }
            }
        } catch (Exception e) {
            System.err.println("Error logging action: " + e.getMessage());
        }
    }

    private void showError(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            org.example.utils.DialogUtils.styleAlert(alert);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing error dialog: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            org.example.utils.DialogUtils.styleAlert(alert);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing success dialog: " + e.getMessage());
        }
    }

    private void showInfo(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            org.example.utils.DialogUtils.styleAlert(alert);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing info dialog: " + e.getMessage());
        }
    }

    public void refreshTrafficData() {
        try {
            // Use applyProtocolFilter() to respect the selected filter
            applyProtocolFilter();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Error refreshing traffic data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanup() {
        try {
            stopAutoRefresh();
            if (captureService != null && captureService.isCapturing()) {
                captureService.stopCapture();
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Add this method to TrafficController.java

    /**
     * Handle show details button - displays detailed information about selected traffic
     */
    @FXML
    private void handleShowDetails() {
        TrafficData selectedTraffic = trafficTable.getSelectionModel().getSelectedItem();

        if (selectedTraffic == null) {
            showError("Please select a traffic entry to view details");
            return;
        }

        showTrafficDetails(selectedTraffic);
    }

    /**
     * Display detailed traffic information dialog
     */
    private void showTrafficDetails(TrafficData traffic) {
        Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
        detailsDialog.setTitle("Traffic Details");
        detailsDialog.setHeaderText("📊 Detailed Traffic Information");

        // Build detailed information
        StringBuilder details = new StringBuilder();

        // Basic Information
        details.append("═══════════════════════════════════════\n");
        details.append("📋 BASIC INFORMATION\n");
        details.append("═══════════════════════════════════════\n");
        details.append(String.format("Protocol:           %s\n", traffic.getProtocol()));
        details.append(String.format("Status:             %s\n", traffic.getStatus()));
        details.append(String.format("Timestamp:          %s\n", traffic.getTimestamp()));
        details.append(String.format("Packet Size:        %,d bytes (%.2f KB)\n",
                traffic.getPacketSize(), traffic.getPacketSize() / 1024.0));

        // Source Information
        details.append("\n═══════════════════════════════════════\n");
        details.append("📤 SOURCE INFORMATION\n");
        details.append("═══════════════════════════════════════\n");
        details.append(String.format("IP Address:         %s\n", traffic.getSourceIP()));
        details.append(String.format("Port:               %s\n", traffic.getSourcePort()));
        details.append(String.format("Location:           %s\n", getIPLocation(traffic.getSourceIP())));
        details.append(String.format("Type:               %s\n", getIPType(traffic.getSourceIP())));

        // Destination Information
        details.append("\n═══════════════════════════════════════\n");
        details.append("📥 DESTINATION INFORMATION\n");
        details.append("═══════════════════════════════════════\n");
        details.append(String.format("IP Address:         %s\n", traffic.getDestinationIP()));
        details.append(String.format("Port:               %s\n", traffic.getDestinationPort()));
        details.append(String.format("Location:           %s\n", getIPLocation(traffic.getDestinationIP())));
        details.append(String.format("Type:               %s\n", getIPType(traffic.getDestinationIP())));

        // Port Information
        details.append("\n═══════════════════════════════════════\n");
        details.append("🔌 PORT INFORMATION\n");
        details.append("═══════════════════════════════════════\n");
        try {
            int srcPort = Integer.parseInt(traffic.getSourcePort());
            int dstPort = Integer.parseInt(traffic.getDestinationPort());

            details.append(String.format("Source Port:        %d (%s)\n",
                    srcPort, getPortDescription(srcPort)));
            details.append(String.format("Destination Port:   %d (%s)\n",
                    dstPort, getPortDescription(dstPort)));
            details.append(String.format("Port Category:      %s\n",
                    getPortCategory(dstPort)));
        } catch (NumberFormatException e) {
            details.append("Port information unavailable\n");
        }

        // Traffic Direction
        details.append("\n═══════════════════════════════════════\n");
        details.append("🔄 TRAFFIC DIRECTION\n");
        details.append("═══════════════════════════════════════\n");
        String direction = determineTrafficDirection(traffic.getSourceIP(), traffic.getDestinationIP());
        details.append(String.format("Direction:          %s\n", direction));
        details.append(String.format("Flow:               %s → %s\n",
                traffic.getSourceIP(), traffic.getDestinationIP()));

        // Protocol-Specific Information
        details.append("\n═══════════════════════════════════════\n");
        details.append("🔍 PROTOCOL ANALYSIS\n");
        details.append("═══════════════════════════════════════\n");
        details.append(getProtocolAnalysis(traffic));

        // Security Assessment
        details.append("\n═══════════════════════════════════════\n");
        details.append("🛡️ SECURITY ASSESSMENT\n");
        details.append("═══════════════════════════════════════\n");
        details.append(getSecurityAssessment(traffic));

        detailsDialog.setContentText(details.toString());

        // Style the dialog
        org.example.utils.DialogUtils.styleAlert(detailsDialog);

        // Make the dialog resizable and larger
        detailsDialog.setResizable(true);
        detailsDialog.getDialogPane().setPrefSize(700, 600);

        // Style the content text as monospace for better alignment
        detailsDialog.getDialogPane().lookup(".content.label").setStyle(
                "-fx-font-family: 'Courier New', monospace; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-fill: #e0e0e0;"
        );

        detailsDialog.showAndWait();
    }

    /**
     * Determine traffic direction
     */
    private String determineTrafficDirection(String srcIP, String dstIP) {
        boolean srcIsLocal = isLocalIP(srcIP);
        boolean dstIsLocal = isLocalIP(dstIP);

        if (!srcIsLocal && dstIsLocal) {
            return "⬇️ INBOUND (External → Local)";
        } else if (srcIsLocal && !dstIsLocal) {
            return "⬆️ OUTBOUND (Local → External)";
        } else if (srcIsLocal && dstIsLocal) {
            return "🔄 LOCAL (Internal Traffic)";
        } else {
            return "↔️ TRANSIT (External → External)";
        }
    }

    /**
     * Check if IP is local
     */
    private boolean isLocalIP(String ip) {
        return ip.startsWith("127.") || ip.startsWith("192.168.") ||
                ip.startsWith("10.") || ip.startsWith("172.16.") ||
                ip.equals("0.0.0.0") || ip.equals("::1");
    }

    /**
     * Get IP location/type description
     */
    private String getIPLocation(String ip) {
        if (ip.startsWith("127.")) return "Loopback (This Computer)";
        if (ip.startsWith("192.168.")) return "Private Network (Class C)";
        if (ip.startsWith("10.")) return "Private Network (Class A)";
        if (ip.startsWith("172.16.") || ip.startsWith("172.31.")) return "Private Network (Class B)";
        if (ip.equals("0.0.0.0")) return "Any Address";
        if (ip.startsWith("169.254.")) return "Link-Local (APIPA)";
        if (ip.startsWith("224.") || ip.startsWith("239.")) return "Multicast";
        return "Internet (Public)";
    }

    /**
     * Get IP type
     */
    private String getIPType(String ip) {
        if (isLocalIP(ip)) return "Private";
        return "Public";
    }

    /**
     * Get port description
     */
    private String getPortDescription(int port) {
        return switch (port) {
            case 20, 21 -> "FTP (File Transfer)";
            case 22 -> "SSH (Secure Shell)";
            case 23 -> "Telnet";
            case 25 -> "SMTP (Email)";
            case 53 -> "DNS (Domain Name)";
            case 67, 68 -> "DHCP";
            case 80 -> "HTTP (Web)";
            case 110 -> "POP3 (Email)";
            case 143 -> "IMAP (Email)";
            case 443 -> "HTTPS (Secure Web)";
            case 445 -> "SMB (File Sharing)";
            case 465 -> "SMTPS (Secure Email)";
            case 587 -> "SMTP Submission";
            case 993 -> "IMAPS (Secure Email)";
            case 995 -> "POP3S (Secure Email)";
            case 1433 -> "MS SQL Server";
            case 3306 -> "MySQL Database";
            case 3389 -> "RDP (Remote Desktop)";
            case 5432 -> "PostgreSQL Database";
            case 5900 -> "VNC (Remote Desktop)";
            case 8080 -> "HTTP Alternate";
            case 8443 -> "HTTPS Alternate";
            default -> port < 1024 ? "Well-Known Port" :
                    port < 49152 ? "Registered Port" : "Dynamic Port";
        };
    }

    /**
     * Get port category
     */
    private String getPortCategory(int port) {
        if (port < 1024) return "System/Well-Known";
        if (port < 49152) return "User/Registered";
        return "Dynamic/Private";
    }

    /**
     * Get protocol-specific analysis
     */
    private String getProtocolAnalysis(TrafficData traffic) {
        StringBuilder analysis = new StringBuilder();
        String protocol = traffic.getProtocol();

        analysis.append(String.format("Protocol Type:      %s\n", protocol));

        switch (protocol.toUpperCase()) {
            case "TCP":
                analysis.append("Connection Type:    Connection-oriented\n");
                analysis.append("Reliability:        Reliable delivery\n");
                analysis.append("Use Case:           Web, Email, File Transfer\n");
                break;
            case "UDP":
                analysis.append("Connection Type:    Connectionless\n");
                analysis.append("Reliability:        Best-effort delivery\n");
                analysis.append("Use Case:           DNS, Streaming, Gaming\n");
                break;
            case "HTTP":
                analysis.append("Application:        Web Traffic\n");
                analysis.append("Security:           ⚠️ Unencrypted\n");
                analysis.append("Recommendation:     Use HTTPS when possible\n");
                break;
            case "HTTPS":
                analysis.append("Application:        Secure Web Traffic\n");
                analysis.append("Security:           ✅ Encrypted (TLS/SSL)\n");
                analysis.append("Recommendation:     Secure protocol\n");
                break;
            case "DNS":
                analysis.append("Application:        Domain Name Resolution\n");
                analysis.append("Security:           Usually unencrypted\n");
                analysis.append("Recommendation:     Consider DNS over HTTPS\n");
                break;
            case "SSH":
                analysis.append("Application:        Secure Remote Access\n");
                analysis.append("Security:           ✅ Encrypted\n");
                analysis.append("Recommendation:     Secure protocol\n");
                break;
            case "ICMP":
                analysis.append("Type:               Control/Diagnostic\n");
                analysis.append("Use Case:           Ping, Traceroute\n");
                analysis.append("Security:           Monitor for floods\n");
                break;
            default:
                analysis.append("Description:        Standard network protocol\n");
                analysis.append("Security:           Review based on context\n");
        }

        return analysis.toString();
    }

    /**
     * Get security assessment
     */
    private String getSecurityAssessment(TrafficData traffic) {
        StringBuilder assessment = new StringBuilder();
        int threatLevel = 0;
        List<String> concerns = new ArrayList<>();

        try {
            int dstPort = Integer.parseInt(traffic.getDestinationPort());

            // Check for suspicious ports
            Set<Integer> suspiciousPorts = Set.of(
                    1337, 31337, 12345, 12346, 20034,
                    6667, 6668, 6669, 9996, 9997, 9998, 9999
            );

            if (suspiciousPorts.contains(dstPort)) {
                threatLevel += 3;
                concerns.add("⚠️ Trojan/Backdoor port detected");
            }

            // Check protocol security
            if (traffic.getProtocol().equals("HTTP")) {
                threatLevel += 1;
                concerns.add("ℹ️ Unencrypted protocol");
            }

            // Check direction
            String direction = determineTrafficDirection(
                    traffic.getSourceIP(),
                    traffic.getDestinationIP()
            );

            if (direction.contains("INBOUND") && !isLocalIP(traffic.getSourceIP())) {
                threatLevel += 1;
                concerns.add("ℹ️ External inbound connection");
            }

            // Assess overall threat level
            String riskLevel;
            String emoji;
            if (threatLevel >= 3) {
                riskLevel = "HIGH RISK";
                emoji = "🔴";
            } else if (threatLevel >= 2) {
                riskLevel = "MEDIUM RISK";
                emoji = "🟡";
            } else if (threatLevel >= 1) {
                riskLevel = "LOW RISK";
                emoji = "🟢";
            } else {
                riskLevel = "NORMAL";
                emoji = "✅";
            }

            assessment.append(String.format("Threat Level:       %s %s\n", emoji, riskLevel));
            assessment.append(String.format("Risk Score:         %d/5\n", threatLevel));

            if (!concerns.isEmpty()) {
                assessment.append("\nSecurity Concerns:\n");
                for (String concern : concerns) {
                    assessment.append(String.format("  • %s\n", concern));
                }
            } else {
                assessment.append("\nNo immediate concerns detected.\n");
            }

            // Recommendations
            assessment.append("\nRecommendations:\n");
            if (threatLevel >= 3) {
                assessment.append("  • Investigate this connection immediately\n");
                assessment.append("  • Consider blocking the source IP\n");
                assessment.append("  • Review firewall rules\n");
            } else if (threatLevel >= 1) {
                assessment.append("  • Monitor this connection\n");
                assessment.append("  • Verify legitimacy if suspicious\n");
            } else {
                assessment.append("  • Continue normal monitoring\n");
            }

        } catch (Exception e) {
            assessment.append("Unable to perform security assessment\n");
        }

        return assessment.toString();
    }
}