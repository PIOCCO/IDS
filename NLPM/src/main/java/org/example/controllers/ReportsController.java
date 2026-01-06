package org.example.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.dao.MonitoringSessionDAO;
import org.example.models.MonitoringSession;
import org.example.utils.DialogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for Reports & Analytics page
 */
public class ReportsController implements Initializable {

    // ===== FXML Components =====
    @FXML
    private Label totalSessionsLabel;
    @FXML
    private Label totalPacketsLabel;
    @FXML
    private Label totalAlertsLabel;
    @FXML
    private Label avgDurationLabel;

    @FXML
    private LineChart<String, Number> sessionsOverTimeChart;
    @FXML
    private PieChart protocolDistributionChart;

    @FXML
    private ComboBox<String> timeRangeFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TextField searchField;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button exportBtn;
    @FXML
    private Button deleteBtn;

    @FXML
    private TableView<MonitoringSession> sessionsTable;
    @FXML
    private TableColumn<MonitoringSession, Integer> sessionIdColumn;
    @FXML
    private TableColumn<MonitoringSession, String> sessionNameColumn;
    @FXML
    private TableColumn<MonitoringSession, String> interfaceColumn;
    @FXML
    private TableColumn<MonitoringSession, String> startTimeColumn;
    @FXML
    private TableColumn<MonitoringSession, String> durationColumn;
    @FXML
    private TableColumn<MonitoringSession, Long> packetsColumn;
    @FXML
    private TableColumn<MonitoringSession, Integer> alertsColumn;
    @FXML
    private TableColumn<MonitoringSession, String> statusColumn;

    // ===== Data =====
    private MonitoringSessionDAO sessionDAO;
    private ObservableList<MonitoringSession> allSessions;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sessionDAO = new MonitoringSessionDAO();
            allSessions = FXCollections.observableArrayList();

            initializeTable();
            initializeFilters();
            initializeButtons();

            loadData();

            System.out.println("ReportsController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing ReportsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeTable() {
        sessionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        sessionIdColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getSessionId()).asObject());

        sessionNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSessionName()));

        interfaceColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInterfaceName()));

        startTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new SimpleStringProperty(startTime != null ? startTime.format(dateFormatter) : "");
        });

        durationColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDuration()));

        packetsColumn.setCellValueFactory(
                cellData -> new SimpleLongProperty(cellData.getValue().getTotalPackets()).asObject());

        alertsColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalAlerts()).asObject());

        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        // Status column with color styling
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String style = switch (status) {
                        case "ACTIVE" -> "-fx-text-fill: #4CAF50; -fx-font-weight: bold;";
                        case "COMPLETED" -> "-fx-text-fill: #2196F3;";
                        case "STOPPED" -> "-fx-text-fill: #FF9800;";
                        default -> "";
                    };
                    setStyle(style);
                }
            }
        });

        // Double-click to view details
        sessionsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MonitoringSession selected = sessionsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showSessionDetails(selected);
                }
            }
        });
    }

    private void initializeFilters() {
        // Time range filter
        timeRangeFilter.setItems(FXCollections.observableArrayList(
                "All Time", "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days"));
        timeRangeFilter.setValue("All Time");
        timeRangeFilter.setOnAction(e -> applyFilters());

        // Status filter
        statusFilter.setItems(FXCollections.observableArrayList(
                "All Status", "ACTIVE", "COMPLETED", "STOPPED"));
        statusFilter.setValue("All Status");
        statusFilter.setOnAction(e -> applyFilters());

        // Search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void initializeButtons() {
        if (refreshBtn != null) {
            refreshBtn.setOnAction(e -> loadData());
        }

        if (exportBtn != null) {
            exportBtn.setOnAction(e -> exportSelectedSession());
        }

        if (deleteBtn != null) {
            deleteBtn.setOnAction(e -> deleteSelectedSession());
        }
    }

    private void loadData() {
        try {
            // Load sessions
            allSessions.clear();
            allSessions.addAll(sessionDAO.getAllSessions());
            applyFilters();

            // Load statistics
            loadGlobalStatistics();

            // Load charts
            loadSessionsOverTimeChart();
            loadProtocolDistributionChart();

            System.out.println("Loaded " + allSessions.size() + " sessions");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGlobalStatistics() {
        Platform.runLater(() -> {
            try {
                int totalSessions = allSessions.size();
                totalSessionsLabel.setText(String.valueOf(totalSessions));

                long totalPackets = sessionDAO.getTotalPacketsAcrossAllSessions();
                totalPacketsLabel.setText(formatNumber(totalPackets));

                int totalAlerts = sessionDAO.getTotalAlertsAcrossAllSessions();
                totalAlertsLabel.setText(String.valueOf(totalAlerts));

                double avgDuration = sessionDAO.getAverageSessionDuration();
                avgDurationLabel.setText(formatDuration((int) avgDuration));
            } catch (Exception e) {
                System.err.println("Error loading statistics: " + e.getMessage());
            }
        });
    }

    private void loadSessionsOverTimeChart() {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sessions");

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(14); // Reduced to 14 days for better spacing

            Map<LocalDate, Long> data = sessionDAO.getSessionsByDay(
                    start.atStartOfDay(),
                    end.atTime(23, 59, 59));

            // Use shorter date format
            DateTimeFormatter chartFormatter = DateTimeFormatter.ofPattern("dd/MM");

            int dayCount = 0;
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                long count = data.getOrDefault(date, 0L);
                // Show label every 2 days to avoid crowding
                String label = (dayCount % 2 == 0) ? date.format(chartFormatter) : "";
                series.getData().add(new XYChart.Data<>(
                        date.format(chartFormatter),
                        count));
                dayCount++;
            }

            Platform.runLater(() -> {
                sessionsOverTimeChart.getData().clear();
                sessionsOverTimeChart.getData().add(series);
                sessionsOverTimeChart.setStyle("-fx-background-color: transparent;");

                // Style series line
                if (series.getNode() != null) {
                    series.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px;");
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading sessions chart: " + e.getMessage());
        }
    }

    private void loadProtocolDistributionChart() {
        try {
            Map<String, Long> protocols = sessionDAO.getGlobalProtocolDistribution();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            // Color palette for protocols
            String[] colors = { "#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0", "#00BCD4", "#FFEB3B",
                    "#795548" };

            if (protocols.isEmpty()) {
                pieData.add(new PieChart.Data("No Data", 1));
            } else {
                protocols.forEach((protocol, count) -> {
                    pieData.add(new PieChart.Data(protocol + ": " + count, count));
                });
            }

            Platform.runLater(() -> {
                protocolDistributionChart.setData(pieData);
                protocolDistributionChart.setStyle("-fx-background-color: transparent;");

                // Apply colors to pie slices
                int colorIndex = 0;
                for (PieChart.Data d : protocolDistributionChart.getData()) {
                    String color = colors[colorIndex % colors.length];
                    d.getNode().setStyle("-fx-pie-color: " + color + ";");
                    colorIndex++;
                }

                // Style legend items to match pie colors
                protocolDistributionChart.lookupAll(".chart-legend-item-symbol").forEach(node -> {
                    // Legend styling handled by CSS
                });
            });
        } catch (Exception e) {
            System.err.println("Error loading protocol chart: " + e.getMessage());
        }
    }

    private void applyFilters() {
        ObservableList<MonitoringSession> filtered = FXCollections.observableArrayList();

        String timeRange = timeRangeFilter.getValue();
        String status = statusFilter.getValue();
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase()
                : "";

        LocalDateTime cutoffDate = switch (timeRange) {
            case "Today" -> LocalDate.now().atStartOfDay();
            case "Last 7 Days" -> LocalDate.now().minusDays(7).atStartOfDay();
            case "Last 30 Days" -> LocalDate.now().minusDays(30).atStartOfDay();
            case "Last 90 Days" -> LocalDate.now().minusDays(90).atStartOfDay();
            default -> null;
        };

        for (MonitoringSession session : allSessions) {
            // Time filter
            if (cutoffDate != null && session.getStartTime() != null
                    && session.getStartTime().isBefore(cutoffDate)) {
                continue;
            }

            // Status filter
            if (!"All Status".equals(status) && !status.equals(session.getStatus())) {
                continue;
            }

            // Search filter
            if (!search.isEmpty()) {
                String sessionName = session.getSessionName() != null
                        ? session.getSessionName().toLowerCase()
                        : "";
                String interfaceName = session.getInterfaceName() != null
                        ? session.getInterfaceName().toLowerCase()
                        : "";
                if (!sessionName.contains(search) && !interfaceName.contains(search)) {
                    continue;
                }
            }

            filtered.add(session);
        }

        sessionsTable.setItems(filtered);
    }

    private void showSessionDetails(MonitoringSession session) {
        // For now, show a simple dialog with session info
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Details");
        alert.setHeaderText("Session #" + session.getSessionId() + ": " + session.getSessionName());

        StringBuilder content = new StringBuilder();
        content.append("Interface: ").append(session.getInterfaceName()).append("\n");
        content.append("Start: ").append(session.getStartTime() != null
                ? session.getStartTime().format(dateFormatter)
                : "N/A").append("\n");
        content.append("End: ").append(session.getEndTime() != null
                ? session.getEndTime().format(dateFormatter)
                : "Running").append("\n");
        content.append("Duration: ").append(session.getFormattedDuration()).append("\n");
        content.append("Packets: ").append(formatNumber(session.getTotalPackets())).append("\n");
        content.append("Alerts: ").append(session.getTotalAlerts()).append("\n");
        content.append("Status: ").append(session.getStatus()).append("\n");
        content.append("Created By: ").append(session.getCreatedBy());

        alert.setContentText(content.toString());
        DialogUtils.styleAlert(alert);
        alert.showAndWait();
    }

    private void exportSelectedSession() {
        MonitoringSession session = sessionsTable.getSelectionModel().getSelectedItem();

        // Export all if none selected
        if (session == null && !allSessions.isEmpty()) {
            exportAllSessions();
            return;
        }

        if (session == null) {
            showError("No sessions to export");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Session Report");
        fileChooser.setInitialFileName("session_" + session.getSessionId() + "_report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(sessionsTable.getScene().getWindow());
        if (file != null) {
            exportSessionToCSV(session, file);
        }
    }

    private void exportAllSessions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Sessions");
        fileChooser.setInitialFileName("all_sessions_report.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(sessionsTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(
                        "Session ID,Session Name,Interface,Start Time,End Time,Duration,Packets,Alerts,Status,Created By");

                for (MonitoringSession s : allSessions) {
                    writer.println(String.format("%d,%s,%s,%s,%s,%s,%d,%d,%s,%s",
                            s.getSessionId(),
                            escapeCSV(s.getSessionName()),
                            escapeCSV(s.getInterfaceName()),
                            s.getStartTime() != null ? s.getStartTime().format(dateFormatter) : "",
                            s.getEndTime() != null ? s.getEndTime().format(dateFormatter) : "",
                            s.getFormattedDuration(),
                            s.getTotalPackets(),
                            s.getTotalAlerts(),
                            s.getStatus(),
                            escapeCSV(s.getCreatedBy())));
                }

                showSuccess("Exported " + allSessions.size() + " sessions to " + file.getName());
            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
            }
        }
    }

    private void exportSessionToCSV(MonitoringSession session, File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Property,Value");
            writer.println("Session ID," + session.getSessionId());
            writer.println("Session Name," + escapeCSV(session.getSessionName()));
            writer.println("Interface," + escapeCSV(session.getInterfaceName()));
            writer.println("Start Time," + (session.getStartTime() != null
                    ? session.getStartTime().format(dateFormatter)
                    : ""));
            writer.println("End Time," + (session.getEndTime() != null
                    ? session.getEndTime().format(dateFormatter)
                    : ""));
            writer.println("Duration," + session.getFormattedDuration());
            writer.println("Packets," + session.getTotalPackets());
            writer.println("Alerts," + session.getTotalAlerts());
            writer.println("Status," + session.getStatus());
            writer.println("Created By," + escapeCSV(session.getCreatedBy()));

            showSuccess("Exported to " + file.getName());
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private void deleteSelectedSession() {
        MonitoringSession session = sessionsTable.getSelectionModel().getSelectedItem();
        if (session == null) {
            showError("Please select a session to delete");
            return;
        }

        if ("ACTIVE".equals(session.getStatus())) {
            showError("Cannot delete an active session. Stop monitoring first.");
            return;
        }

        Alert confirm = DialogUtils.createConfirmation(
                "Delete Session",
                "Delete Session #" + session.getSessionId() + "?",
                "This will permanently delete this session and all its data.\n" +
                        "Session: " + session.getSessionName());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = sessionDAO.deleteSession(session.getSessionId());
                if (deleted) {
                    showSuccess("Session deleted successfully");
                    loadData();
                } else {
                    showError("Failed to delete session");
                }
            }
        });
    }

    // ===== Utility Methods =====

    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    private String formatDuration(int seconds) {
        if (seconds <= 0)
            return "0 min";
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%d min", minutes > 0 ? minutes : 1);
    }

    private String escapeCSV(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogUtils.styleAlert(alert);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogUtils.styleAlert(alert);
        alert.showAndWait();
    }
}
