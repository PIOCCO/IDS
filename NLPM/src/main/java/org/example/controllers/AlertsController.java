package org.example.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.models.Alert;
import org.example.utils.DataGenerator;

import java.net.URL;
import java.util.ResourceBundle;

public class AlertsController implements Initializable {

    @FXML
    private TableView<Alert> alertsTable;

    @FXML
    private TableColumn<Alert, String> idColumn;

    @FXML
    private TableColumn<Alert, String> severityColumn;

    @FXML
    private TableColumn<Alert, String> typeColumn;

    @FXML
    private TableColumn<Alert, String> sourceColumn;

    @FXML
    private TableColumn<Alert, String> destinationColumn;

    @FXML
    private TableColumn<Alert, String> descriptionColumn;

    @FXML
    private TableColumn<Alert, String> timestampColumn;

    @FXML
    private TableColumn<Alert, String> statusColumn;

    @FXML
    private ComboBox<String> severityFilter;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshBtn;

    private ObservableList<Alert> alertsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
        initializeFilters();
        loadAlerts();

        refreshBtn.setOnAction(e -> loadAlerts());
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
        alertsList = FXCollections.observableArrayList(DataGenerator.generateAlerts(50));
        alertsTable.setItems(alertsList);
    }
}