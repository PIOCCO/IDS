package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.database.dao.AlertDAO;
import org.example.models.SecurityAlert;

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
    private TableColumn<SecurityAlert, String> directionColumn; // NEW

    @FXML
    private ComboBox<String> severityFilter;

    @FXML
    private ComboBox<String> directionFilter; // NEW

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshBtn;

    @FXML
    private Label inboundThreatsLabel; // NEW - show inbound threat count

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
        if (directionFilter != null) {
            directionFilter.setOnAction(e -> applyFilters());
        }
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

        severityColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeverity()));

        // Color code severity
        severityColumn.setCellFactory(column -> new TableCell<SecurityAlert, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item.toLowerCase()) {
                        case "critical" -> "-fx-text-fill: #F44336; -fx-font-weight: bold;";
                        case "high" -> "-fx-text-fill: #FF9800; -fx-font-weight: bold;";
                        case "medium" -> "-fx-text-fill: #FFC107;";
                        case "low" -> "-fx-text-fill: #4CAF50;";
                        default -> "-fx-text-fill: #00BCD4;";
                    };
                    setStyle(color);
                }
            }
        });

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

        // NEW: Direction column with emoji
        if (directionColumn != null) {
            directionColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getDirectionWithEmoji()));

            // Color code direction
            directionColumn.setCellFactory(column -> new TableCell<SecurityAlert, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("INBOUND")) {
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                        } else if (item.contains("OUTBOUND")) {
                            setStyle("-fx-text-fill: #4CAF50;");
                        } else if (item.contains("LOCAL")) {
                            setStyle("-fx-text-fill: #00BCD4;");
                        } else {
                            setStyle("-fx-text-fill: #808080;");
                        }
                    }
                }
            });
        }
    }

    private void initializeFilters() {
        severityFilter.setItems(FXCollections.observableArrayList(
                "All", "Critical", "High", "Medium", "Low", "Info"
        ));
        severityFilter.setValue("All");

        // NEW: Direction filter
        if (directionFilter != null) {
            directionFilter.setItems(FXCollections.observableArrayList(
                    "All", "INBOUND", "OUTBOUND", "LOCAL", "UNKNOWN"
            ));
            directionFilter.setValue("All");
        }
    }

    private void loadAlerts() {
        try {
            List<SecurityAlert> alerts = alertDAO.getAllAlerts();
            alertsList = FXCollections.observableArrayList(alerts);
            filteredAlertsList = FXCollections.observableArrayList(alerts);
            alertsTable.setItems(filteredAlertsList);

            // Update inbound threats count
            if (inboundThreatsLabel != null) {
                long inboundCount = alerts.stream()
                        .filter(SecurityAlert::isInboundThreat)
                        .count();
                inboundThreatsLabel.setText("⚠️ Inbound Threats: " + inboundCount);
                inboundThreatsLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 14px; -fx-font-weight: bold;");
            }

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
        String directionValue = directionFilter != null ? directionFilter.getValue() : "All";
        String searchText = searchField.getText().toLowerCase();

        List<SecurityAlert> filtered = alertsList.stream()
                .filter(alert -> {
                    // Severity filter
                    boolean severityMatch = severityValue.equals("All") ||
                            alert.getSeverity().equals(severityValue);

                    // Direction filter
                    boolean directionMatch = directionValue.equals("All") ||
                            alert.getDirection().equals(directionValue);

                    // Search filter
                    boolean searchMatch = searchText.isEmpty() ||
                            alert.getSourceIP().toLowerCase().contains(searchText) ||
                            alert.getDestinationIP().toLowerCase().contains(searchText) ||
                            alert.getType().toLowerCase().contains(searchText) ||
                            alert.getDescription().toLowerCase().contains(searchText) ||
                            alert.getDirection().toLowerCase().contains(searchText);

                    return severityMatch && directionMatch && searchMatch;
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