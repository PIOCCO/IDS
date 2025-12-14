package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.database.dao.AlertDAO;
import org.example.database.dao.TrafficDAO;
import org.example.models.SecurityAlert;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalAlertsLabel;

    @FXML
    private Label criticalAlertsLabel;

    @FXML
    private Label warningAlertsLabel;

    @FXML
    private Label packetsAnalyzedLabel;

    @FXML
    private TableView<SecurityAlert> recentAlertsTable;

    @FXML
    private TableColumn<SecurityAlert, String> severityColumn;

    @FXML
    private TableColumn<SecurityAlert, String> typeColumn;

    @FXML
    private TableColumn<SecurityAlert, String> sourceColumn;

    @FXML
    private TableColumn<SecurityAlert, String> timestampColumn;

    private ObservableList<SecurityAlert> alertsList;
    private AlertDAO alertDAO;
    private TrafficDAO trafficDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertDAO = new AlertDAO();
        trafficDAO = new TrafficDAO();

        initializeTable();
        loadStatistics();
        loadRecentAlerts();
    }

    private void initializeTable() {
        severityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeverity()));
        typeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sourceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));
        timestampColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestamp().toString()));
    }

    private void loadStatistics() {
        try {
            // Load alert statistics from database
            int totalAlerts = alertDAO.getTotalAlertsCount();
            int criticalAlerts = alertDAO.getAlertCountBySeverity("Critical");
            int warningAlerts = alertDAO.getAlertCountBySeverity("High") + alertDAO.getAlertCountBySeverity("Medium");
            long packetsAnalyzed = trafficDAO.getTotalPacketsAnalyzed();

            // Update labels
            totalAlertsLabel.setText(String.valueOf(totalAlerts));
            criticalAlertsLabel.setText(String.valueOf(criticalAlerts));
            warningAlertsLabel.setText(String.valueOf(warningAlerts));
            packetsAnalyzedLabel.setText(String.format("%,d", packetsAnalyzed));
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();

            // Set default values on error
            totalAlertsLabel.setText("0");
            criticalAlertsLabel.setText("0");
            warningAlertsLabel.setText("0");
            packetsAnalyzedLabel.setText("0");
        }
    }

    private void loadRecentAlerts() {
        try {
            alertsList = FXCollections.observableArrayList(alertDAO.getRecentAlerts(10));
            recentAlertsTable.setItems(alertsList);
        } catch (Exception e) {
            System.err.println("Error loading recent alerts: " + e.getMessage());
            e.printStackTrace();
            alertsList = FXCollections.observableArrayList();
            recentAlertsTable.setItems(alertsList);
        }
    }

    public void refreshDashboard() {
        loadStatistics();
        loadRecentAlerts();
    }
}