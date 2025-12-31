package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.database.dao.TrafficDAO;
import org.example.models.TrafficData;
import org.example.services.PacketCaptureService;
import org.example.services.AuthenticationService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Optional;

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
    private TrafficDAO trafficDAO;
    private PacketCaptureService captureService;
    private AuthenticationService authService;
    private Timer refreshTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            trafficDAO = new TrafficDAO();
            captureService = PacketCaptureService.getInstance();
            authService = AuthenticationService.getInstance();

            initializeTable();
            initializeControls();
            loadTrafficData();
            loadNetworkInterfaces();
            updateStatistics();

            System.out.println("TrafficController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing TrafficController: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to initialize Traffic Monitor: " + e.getMessage());
        }
    }

    private void initializeTable() {
        try {
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

            System.out.println("Controls initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing controls: " + e.getMessage());
            e.printStackTrace();
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
            List<TrafficData> traffic = trafficDAO.getRecentTraffic(5); // Last 5 minutes
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
                List<TrafficData> filtered = trafficDAO.getTrafficByProtocol(protocol);
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
            if (interfaceSelector == null || interfaceSelector.getValue() == null) {
                showError("Please select a network interface");
                return;
            }

            String selectedInterface = interfaceSelector.getValue();
            if (selectedInterface.isEmpty()) {
                showError("Please select a network interface");
                return;
            }

            // Extract interface name (before the " - " separator)
            String interfaceName = selectedInterface.split(" - ")[0];

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

                // Start auto-refresh timer
                startAutoRefresh();

                showSuccess("Network monitoring started successfully");
                System.out.println("Network monitoring started on: " + interfaceName);
            } else {
                showError("Failed to start packet capture. Make sure you have administrator privileges.");

            }
        } catch (Exception e) {
            System.err.println("Error starting monitoring: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to start monitoring: " + e.getMessage());
        }
    }

    private void stopMonitoring() {
        try {
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

            // Stop auto-refresh
            stopAutoRefresh();

            showInfo("Network monitoring stopped");
            System.out.println("Network monitoring stopped");
        } catch (Exception e) {
            System.err.println("Error stopping monitoring: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to stop monitoring: " + e.getMessage());
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
            boolean success = trafficDAO.deleteAllTraffic();

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
                            loadTrafficData();
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
                int connections = trafficDAO.getActiveConnectionsCount();
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
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing info dialog: " + e.getMessage());
        }
    }

    public void refreshTrafficData() {
        try {
            loadTrafficData();
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
}