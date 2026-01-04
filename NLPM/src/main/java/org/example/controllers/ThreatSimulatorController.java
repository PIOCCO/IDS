package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.example.services.ThreatSimulatorService;
import org.example.services.DetectionEngine;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ThreatSimulatorController implements Initializable {

    @FXML
    private Button startSimulatorBtn;

    @FXML
    private Button stopSimulatorBtn;

    @FXML
    private Button portScanBtn;

    @FXML
    private Button synFloodBtn;

    @FXML
    private Button testConnectionBtn;

    @FXML
    private TextField targetIPField;

    @FXML
    private TextField targetPortField;

    @FXML
    private TextArea logArea;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox portsListBox;

    private ThreatSimulatorService simulator;
    private DetectionEngine detectionEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        simulator = ThreatSimulatorService.getInstance();
        detectionEngine = DetectionEngine.getInstance();

        // Pre-fill with localhost
        targetIPField.setText("127.0.0.1");
        targetPortField.setText("1337");

        // Setup buttons
        startSimulatorBtn.setOnAction(e -> startSimulator());
        stopSimulatorBtn.setOnAction(e -> stopSimulator());
        portScanBtn.setOnAction(e -> simulatePortScan());
        synFloodBtn.setOnAction(e -> simulateSynFlood());
        testConnectionBtn.setOnAction(e -> testConnection());

        stopSimulatorBtn.setDisable(true);

        // Display dangerous ports
        displayDangerousPorts();

        updateStatus();
    }

    private void displayDangerousPorts() {
        portsListBox.getChildren().clear();

        Label title = new Label("🎯 Dangerous Ports (Trojan/Backdoor)");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #F44336;");
        portsListBox.getChildren().add(title);

        Map<Integer, String> ports = simulator.getDangerousPorts();
        for (Map.Entry<Integer, String> entry : ports.entrySet()) {
            Label portLabel = new Label(String.format("Port %d - %s", entry.getKey(), entry.getValue()));
            portLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff; -fx-padding: 5 0;");
            portsListBox.getChildren().add(portLabel);
        }
    }

    private void startSimulator() {
        logArea.appendText("\n" + "=".repeat(50) + "\n");
        logArea.appendText("🚨 STARTING THREAT SIMULATOR\n");
        logArea.appendText("=".repeat(50) + "\n");

        boolean started = simulator.startSimulation();

        if (started) {
            logArea.appendText("✅ Simulator started successfully!\n");
            logArea.appendText("📊 Listening on dangerous ports...\n");
            logArea.appendText("🔍 Your IDS should detect these threats!\n\n");

            startSimulatorBtn.setDisable(true);
            stopSimulatorBtn.setDisable(false);

            showSuccess("Threat Simulator Started",
                    "Now listening on dangerous ports. Your IDS should generate alerts!");
        } else {
            logArea.appendText("❌ Failed to start simulator\n\n");
            showError("Failed to start simulator. Check console for details.");
        }

        updateStatus();
    }

    private void stopSimulator() {
        logArea.appendText("\n🛑 STOPPING THREAT SIMULATOR\n");

        simulator.stopSimulation();

        logArea.appendText("✅ Simulator stopped\n\n");

        startSimulatorBtn.setDisable(false);
        stopSimulatorBtn.setDisable(true);

        updateStatus();
    }

    private void simulatePortScan() {
        String targetIP = targetIPField.getText().trim();

        if (targetIP.isEmpty()) {
            showError("Please enter a target IP address");
            return;
        }

        logArea.appendText("\n" + "=".repeat(50) + "\n");
        logArea.appendText("🔍 SIMULATING PORT SCAN ATTACK\n");
        logArea.appendText("=".repeat(50) + "\n");
        logArea.appendText("Target: " + targetIP + "\n");
        logArea.appendText("Scanning 16 common ports...\n");
        logArea.appendText("⚠️ This should trigger PORT SCAN alerts!\n\n");

        simulator.simulatePortScan(targetIP);

        showInfo("Port Scan Simulation Started",
                "Scanning " + targetIP + ". Check your Alerts tab!");
    }

    private void simulateSynFlood() {
        String targetIP = targetIPField.getText().trim();
        String portStr = targetPortField.getText().trim();

        if (targetIP.isEmpty() || portStr.isEmpty()) {
            showError("Please enter target IP and port");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);

            logArea.appendText("\n" + "=".repeat(50) + "\n");
            logArea.appendText("💥 SIMULATING SYN FLOOD ATTACK\n");
            logArea.appendText("=".repeat(50) + "\n");
            logArea.appendText("Target: " + targetIP + ":" + port + "\n");
            logArea.appendText("Duration: 10 seconds\n");
            logArea.appendText("⚠️ This should trigger DDoS/FLOOD alerts!\n\n");

            simulator.simulateSynFlood(targetIP, port, 10);

            showInfo("SYN Flood Simulation Started",
                    "Flooding " + targetIP + ":" + port + ". Check your Alerts tab!");

        } catch (NumberFormatException e) {
            showError("Invalid port number");
        }
    }

    private void testConnection() {
        String targetIP = targetIPField.getText().trim();
        String portStr = targetPortField.getText().trim();

        if (targetIP.isEmpty() || portStr.isEmpty()) {
            showError("Please enter target IP and port");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);

            logArea.appendText("\n🔌 Testing connection to " + targetIP + ":" + port + "\n");

            new Thread(() -> {
                ThreatSimulatorService.testConnection(targetIP, port);
                javafx.application.Platform.runLater(() ->
                        logArea.appendText("✅ Connection test completed\n\n"));
            }).start();

        } catch (NumberFormatException e) {
            showError("Invalid port number");
        }
    }

    private void updateStatus() {
        if (simulator.isRunning()) {
            statusLabel.setText("🟢 ACTIVE - " + simulator.getActiveListenerCount() + " listeners");
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("🔴 STOPPED");
            statusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        }
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}