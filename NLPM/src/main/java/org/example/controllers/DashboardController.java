package org.example.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.models.Alert;
import org.example.utils.DataGenerator;

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
    private TableView<Alert> recentAlertsTable;

    @FXML
    private TableColumn<Alert, String> severityColumn;

    @FXML
    private TableColumn<Alert, String> typeColumn;

    @FXML
    private TableColumn<Alert, String> sourceColumn;

    @FXML
    private TableColumn<Alert, String> timestampColumn;

    private ObservableList<Alert> alertsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeStats();
        initializeTable();
        loadRecentAlerts();
    }

    private void initializeStats() {
        totalAlertsLabel.setText("247");
        criticalAlertsLabel.setText("18");
        warningAlertsLabel.setText("52");
        packetsAnalyzedLabel.setText("1,245,678");
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

    private void loadRecentAlerts() {
        alertsList = FXCollections.observableArrayList(DataGenerator.generateAlerts(10));
        recentAlertsTable.setItems(alertsList);
    }
}
