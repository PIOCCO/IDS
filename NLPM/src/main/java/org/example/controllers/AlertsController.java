package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.database.dao.AlertDAO;
import org.example.models.SecurityAlert;
import javafx.scene.control.Alert;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AlertsController implements Initializable {

    @FXML
    private TableView<SecurityAlert> alertsTable;

    @FXML
    private TableColumn<SecurityAlert, String> idColumn;

    @FXML
    private TableColumn<SecurityAlert, String> severityColumn;

    @FXML
    private TableColumn<SecurityAlert, String> typeColumn;

    @FXML
    private TableColumn<SecurityAlert, String> sourceColumn;

    @FXML
    private TableColumn<SecurityAlert, String> destinationColumn;

    @FXML
    private TableColumn<SecurityAlert, String> descriptionColumn;

    @FXML
    private TableColumn<SecurityAlert, String> timestampColumn;

    @FXML
    private TableColumn<SecurityAlert, String> statusColumn;

    @FXML
    private ComboBox<String> severityFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshBtn;

    private ObservableList<SecurityAlert> alertsList;
    private ObservableList<SecurityAlert> filteredAlertsList;
    private AlertDAO alertDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertDAO = new AlertDAO();

        initializeTable();
        initializeFilters();
        loadAlerts();

        refreshBtn.setOnAction(e -> loadAlerts());
        severityFilter.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        severityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeverity()));
        typeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sourceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));
        destinationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDestinationIP()));
        descriptionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        timestampColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimestamp().toString()));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void initializeFilters() {
        severityFilter.setItems(FXCollections.observableArrayList(
                "All", "Critical", "High", "Medium", "Low", "Info"
        ));
        severityFilter.setValue("All");
    }

    private void loadAlerts() {
        try {
            List<SecurityAlert> alerts = alertDAO.getAllAlerts();
            alertsList = FXCollections.observableArrayList(alerts);
            filteredAlertsList = FXCollections.observableArrayList(alerts);
            alertsTable.setItems(filteredAlertsList);

            System.out.println("Loaded " + alerts.size() + " alerts from database");
        } catch (Exception e) {
            System.err.println("Error loading alerts: " + e.getMessage());
            e.printStackTrace();

            alertsList = FXCollections.observableArrayList();
            filteredAlertsList = FXCollections.observableArrayList();
            alertsTable.setItems(filteredAlertsList);

            showError("Failed to load alerts from database");
        }
    }

    private void applyFilters() {
        if (alertsList == null) return;

        String severityValue = severityFilter.getValue();
        String searchText = searchField.getText().toLowerCase();

        List<SecurityAlert> filtered = alertsList.stream()
                .filter(alert -> {
                    // Severity filter
                    boolean severityMatch = severityValue.equals("All") ||
                            alert.getSeverity().equals(severityValue);

                    // Search filter
                    boolean searchMatch = searchText.isEmpty() ||
                            alert.getSourceIP().toLowerCase().contains(searchText) ||
                            alert.getDestinationIP().toLowerCase().contains(searchText) ||
                            alert.getType().toLowerCase().contains(searchText) ||
                            alert.getDescription().toLowerCase().contains(searchText);

                    return severityMatch && searchMatch;
                })
                .collect(Collectors.toList());

        filteredAlertsList = FXCollections.observableArrayList(filtered);
        alertsTable.setItems(filteredAlertsList);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}