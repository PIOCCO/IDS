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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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
    private Label statusLabel;

    @FXML
    private Label packetsLabel;

    @FXML
    private Label bytesLabel;

    @FXML
    private Label threatsLabel;

    private ObservableList<TrafficData> trafficList;
    private TrafficDAO trafficDAO;
    private PacketCaptureService captureService;
    private Timer refreshTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trafficDAO = new TrafficDAO();
        captureService = PacketCaptureService.getInstance();

        initializeTable();
        initializeControls();
        loadTrafficData();
        loadNetworkInterfaces();
    }

    private void initializeTable() {
        protocolColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProtocol()));
        sourceIPColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));
        sourcePortColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourcePort()));
        destIPColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDestinationIP()));
        destPortColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDestinationPort()));
        packetSizeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(cellData.getValue().getPacketSize()).asObject());
        timestampColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestamp()));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void initializeControls() {
        protocolFilter.setItems(FXCollections.observableArrayList(
                "All", "TCP", "UDP", "HTTP", "HTTPS", "ICMP", "DNS", "SSH", "FTP"
        ));
        protocolFilter.setValue("All");
        protocolFilter.setOnAction(e -> applyProtocolFilter());

        startMonitorBtn.setOnAction(e -> startMonitoring());
        stopMonitorBtn.setOnAction(e -> stopMonitoring());
        stopMonitorBtn.setDisable(true);

        // Initialize statistics labels
        if (packetsLabel != null) packetsLabel.setText("0");
        if (bytesLabel != null) bytesLabel.setText("0");
        if (threatsLabel != null) threatsLabel.setText("0");
    }

    private void loadNetworkInterfaces() {
        String[] interfaces = PacketCaptureService.getAvailableInterfaces();
        interfaceSelector.setItems(FXCollections.observableArrayList(interfaces));
        if (interfaces.length > 0) {
            interfaceSelector.setValue(interfaces[0]);
        }
    }

    private void loadTrafficData() {
        try {
            List<TrafficData> traffic = trafficDAO.getRecentTraffic(5); // Last 5 minutes
            trafficList = FXCollections.observableArrayList(traffic);
            trafficTable.setItems(trafficList);

            System.out.println("Loaded " + traffic.size() + " traffic records from database");
        } catch (Exception e) {
            System.err.println("Error loading traffic data: " + e.getMessage());
            e.printStackTrace();

            trafficList = FXCollections.observableArrayList();
            trafficTable.setItems(trafficList);

            showError("Failed to load traffic data from database");
        }
    }

    private void applyProtocolFilter() {
        String protocol = protocolFilter.getValue();

        if (protocol.equals("All")) {
            loadTrafficData();
        } else {
            try {
                List<TrafficData> filtered = trafficDAO.getTrafficByProtocol(protocol);
                trafficList = FXCollections.observableArrayList(filtered);
                trafficTable.setItems(trafficList);
            } catch (Exception e) {
                System.err.println("Error filtering traffic: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void startMonitoring() {
        String selectedInterface = interfaceSelector.getValue();
        if (selectedInterface == null || selectedInterface.isEmpty()) {
            showError("Please select a network interface");
            return;
        }

        // Extract interface name (before the " - " separator)
        String interfaceName = selectedInterface.split(" - ")[0];

        boolean started = captureService.startCapture(interfaceName);

        if (started) {
            statusLabel.setText("Status: Monitoring Active");
            statusLabel.setStyle("-fx-text-fill: #4caf50;");
            startMonitorBtn.setDisable(true);
            stopMonitorBtn.setDisable(false);
            interfaceSelector.setDisable(true);

            // Start auto-refresh timer
            startAutoRefresh();

            System.out.println("Network monitoring started on: " + interfaceName);
        } else {
            showError("Failed to start packet capture. Make sure you have administrator privileges.");
        }
    }

    private void stopMonitoring() {
        captureService.stopCapture();

        statusLabel.setText("Status: Monitoring Stopped");
        statusLabel.setStyle("-fx-text-fill: #f44336;");
        startMonitorBtn.setDisable(false);
        stopMonitorBtn.setDisable(true);
        interfaceSelector.setDisable(false);

        // Stop auto-refresh
        stopAutoRefresh();

        System.out.println("Network monitoring stopped");
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    loadTrafficData();
                    updateStatistics();
                });
            }
        }, 0, 2000); // Refresh every 2 seconds
    }

    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void updateStatistics() {
        if (packetsLabel != null) {
            packetsLabel.setText(String.format("%,d", captureService.getPacketsAnalyzed()));
        }
        if (bytesLabel != null) {
            long bytes = captureService.getBytesProcessed();
            String formatted = bytes > 1_000_000 ?
                    String.format("%.2f MB", bytes / 1_000_000.0) :
                    String.format("%.2f KB", bytes / 1_000.0);
            bytesLabel.setText(formatted);
        }
        if (threatsLabel != null) {
            threatsLabel.setText(String.valueOf(
                    org.example.services.DetectionEngine.getInstance().getTotalThreatsDetected()));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshTrafficData() {
        loadTrafficData();
        updateStatistics();
    }

    public void cleanup() {
        stopAutoRefresh();
        if (captureService.isCapturing()) {
            captureService.stopCapture();
        }
    }
}