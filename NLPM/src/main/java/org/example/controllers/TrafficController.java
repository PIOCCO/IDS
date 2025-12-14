package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.database.dao.TrafficDAO;
import org.example.models.TrafficData;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
    private Button startMonitorBtn;

    @FXML
    private Button stopMonitorBtn;

    @FXML
    private Label statusLabel;

    private ObservableList<TrafficData> trafficList;
    private TrafficDAO trafficDAO;
    private boolean isMonitoring = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trafficDAO = new TrafficDAO();

        initializeTable();
        initializeControls();
        loadTrafficData();
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
                "All", "TCP", "UDP", "HTTP", "HTTPS", "ICMP", "DNS"
        ));
        protocolFilter.setValue("All");
        protocolFilter.setOnAction(e -> applyProtocolFilter());

        startMonitorBtn.setOnAction(e -> startMonitoring());
        stopMonitorBtn.setOnAction(e -> stopMonitoring());
        stopMonitorBtn.setDisable(true);
    }

    private void loadTrafficData() {
        try {
            List<TrafficData> traffic = trafficDAO.getAllTraffic();
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
        isMonitoring = true;
        statusLabel.setText("Status: Monitoring Active");
        statusLabel.setStyle("-fx-text-fill: #4caf50;");
        startMonitorBtn.setDisable(true);
        stopMonitorBtn.setDisable(false);

        // TODO: Implement actual network monitoring
        System.out.println("Network monitoring started");
    }

    private void stopMonitoring() {
        isMonitoring = false;
        statusLabel.setText("Status: Monitoring Stopped");
        statusLabel.setStyle("-fx-text-fill: #f44336;");
        startMonitorBtn.setDisable(false);
        stopMonitorBtn.setDisable(true);

        System.out.println("Network monitoring stopped");
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
    }
}