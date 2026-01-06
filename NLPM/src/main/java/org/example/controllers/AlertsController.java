package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.services.AlertService;
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
    private Button exportBtn;

    @FXML
    private Button clearAlertsBtn;

    @FXML
    private Label inboundThreatsLabel; // NEW - show inbound threat count

    private ObservableList<SecurityAlert> alertsList;
    private ObservableList<SecurityAlert> filteredAlertsList;
    private AlertService alertService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertService = AlertService.getInstance();

        initializeTable();
        initializeFilters();
        loadAlerts();

        refreshBtn.setOnAction(e -> loadAlerts());
        if (exportBtn != null) {
            exportBtn.setOnAction(e -> handleExportAlerts());
        }
        if (clearAlertsBtn != null) {
            clearAlertsBtn.setOnAction(e -> handleClearAlerts());
        }
        severityFilter.setOnAction(e -> applyFilters());
        if (directionFilter != null) {
            directionFilter.setOnAction(e -> applyFilters());
        }
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initializeTable() {
        // Alerts table stays flexible with horizontal scroll

        idColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

        severityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeverity()));

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

        typeColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));

        sourceColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));

        destinationColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDestinationIP()));

        descriptionColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        timestampColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTimestamp().toString()));

        statusColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));

        // NEW: Direction column with emoji
        if (directionColumn != null) {
            directionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
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
                "All", "Critical", "High", "Medium", "Low", "Info"));
        severityFilter.setValue("All");

        // NEW: Direction filter
        if (directionFilter != null) {
            directionFilter.setItems(FXCollections.observableArrayList(
                    "All", "INBOUND", "OUTBOUND", "LOCAL", "UNKNOWN"));
            directionFilter.setValue("All");
        }
    }

    private void loadAlerts() {
        try {
            List<SecurityAlert> alerts = alertService.getAllAlerts();
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
        if (alertsList == null)
            return;

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
        org.example.utils.DialogUtils.styleAlert(alert);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        org.example.utils.DialogUtils.styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Export alerts data to CSV file
     */
    private void handleExportAlerts() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Export Alerts Data");
            fileChooser.setInitialFileName("alerts_export_" +
                    java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".csv");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            javafx.stage.Window window = null;
            if (alertsTable != null && alertsTable.getScene() != null) {
                window = alertsTable.getScene().getWindow();
            }

            java.io.File file = fileChooser.showSaveDialog(window);

            if (file != null) {
                exportAlertsToCSV(file);
                showSuccess("Alerts exported successfully to: " + file.getName());
            }
        } catch (Exception e) {
            System.err.println("Error exporting alerts: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to export alerts: " + e.getMessage());
        }
    }

    /**
     * Export alerts list to CSV file
     */
    private void exportAlertsToCSV(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write header
            writer.println("ID,Severity,Direction,Type,Source IP,Destination IP,Description,Timestamp,Status");

            // Write data - use filtered list if available, otherwise use main list
            var dataToExport = (filteredAlertsList != null && !filteredAlertsList.isEmpty())
                    ? filteredAlertsList
                    : alertsList;

            if (dataToExport != null) {
                for (SecurityAlert alert : dataToExport) {
                    writer.println(String.format("%s,%s,%s,%s,%s,%s,\"%s\",%s,%s",
                            alert.getId(),
                            alert.getSeverity(),
                            alert.getDirection(),
                            alert.getType(),
                            alert.getSourceIP(),
                            alert.getDestinationIP(),
                            alert.getDescription().replace("\"", "\"\""), // Escape quotes
                            alert.getTimestamp(),
                            alert.getStatus()));
                }
            }
        }
    }

    /**
     * Handle clear alerts button - shows confirmation dialog
     */
    private void handleClearAlerts() {
        try {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Clear Alerts");
            confirmDialog.setHeaderText("Clear all alerts?");
            confirmDialog.setContentText("Choose an option:");
            org.example.utils.DialogUtils.styleAlert(confirmDialog);

            ButtonType clearAllBtn = new ButtonType("Clear All (Database)");
            ButtonType clearVisibleBtn = new ButtonType("Clear Visible Only");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirmDialog.getButtonTypes().setAll(clearAllBtn, clearVisibleBtn, cancelBtn);

            var result = confirmDialog.showAndWait();
            if (result.isPresent()) {
                if (result.get() == clearAllBtn) {
                    clearAllAlerts();
                } else if (result.get() == clearVisibleBtn) {
                    clearVisibleAlerts();
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling clear alerts: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to clear alerts: " + e.getMessage());
        }
    }

    /**
     * Clear all alerts from database
     */
    private void clearAllAlerts() {
        try {
            boolean success = alertService.deleteAllAlerts();

            if (success) {
                // Clear the table view
                if (alertsList != null) {
                    alertsList.clear();
                }
                if (filteredAlertsList != null) {
                    filteredAlertsList.clear();
                }
                if (alertsTable != null) {
                    alertsTable.setItems(alertsList);
                }

                // Update inbound threats counter
                if (inboundThreatsLabel != null) {
                    inboundThreatsLabel.setText("⚠️ Inbound Threats: 0");
                }

                showSuccess("All alerts have been cleared successfully");
                System.out.println("All alerts cleared");
            } else {
                showError("Failed to clear alerts");
            }
        } catch (Exception e) {
            System.err.println("Error clearing alerts: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred while clearing alerts: " + e.getMessage());
        }
    }

    /**
     * Clear only visible (filtered) alerts from view
     */
    private void clearVisibleAlerts() {
        try {
            int count = filteredAlertsList != null ? filteredAlertsList.size()
                    : (alertsList != null ? alertsList.size() : 0);

            // Clear visible items from table
            if (filteredAlertsList != null) {
                filteredAlertsList.clear();
            }
            if (alertsTable != null) {
                alertsTable.setItems(filteredAlertsList);
            }

            showSuccess(count + " visible alerts cleared from view");
            System.out.println("Cleared " + count + " visible alerts");
        } catch (Exception e) {
            System.err.println("Error clearing visible alerts: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred while clearing visible alerts: " + e.getMessage());
        }
    }
}