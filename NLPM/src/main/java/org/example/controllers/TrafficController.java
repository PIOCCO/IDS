package org.example.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.models.TrafficData;
import org.example.utils.DataGenerator;

import java.net.URL;
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
    private boolean isMonitoring = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        startMonitorBtn.setOnAction(e -> startMonitoring());
        stopMonitorBtn.setOnAction(e -> stopMonitoring());
        stopMonitorBtn.setDisable(true);
    }

    private void loadTrafficData() {
        trafficList = FXCollections.observableArrayList(DataGenerator.generateTrafficData(100));
        trafficTable.setItems(trafficList);
    }

    private void startMonitoring() {
        isMonitoring = true;
        statusLabel.setText("Status: Monitoring Active");
        statusLabel.setStyle("-fx-text-fill: #4caf50;");
        startMonitorBtn.setDisable(true);
        stopMonitorBtn.setDisable(false);
    }

    private void stopMonitoring() {
        isMonitoring = false;
        statusLabel.setText("Status: Monitoring Stopped");
        statusLabel.setStyle("-fx-text-fill: #f44336;");
        startMonitorBtn.setDisable(false);
        stopMonitorBtn.setDisable(true);
    }
}
