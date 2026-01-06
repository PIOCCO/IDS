package org.example.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.example.models.ChartMetric;
import org.example.models.SecurityAlert;
import org.example.services.AlertService;
import org.example.services.ChartDataService;
import org.example.services.MetricsManager;
import org.example.services.TrafficService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Dashboard Controller - Restored with Stats, Table + Integrated Cockpit Chart
 */
public class DashboardController implements Initializable {

    // ==================== STATS CARDS ====================
    @FXML
    private Label totalAlertsLabel;
    @FXML
    private Label criticalAlertsLabel;
    @FXML
    private Label warningAlertsLabel;
    @FXML
    private Label packetsAnalyzedLabel;

    // ==================== ALERTS TABLE ====================
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

    // ==================== CHART COMPONENTS ====================
    @FXML
    private AreaChart<String, Number> metricsChart;
    @FXML
    private CategoryAxis chartXAxis;
    @FXML
    private NumberAxis chartYAxis;
    @FXML
    private ComboBox<String> timeRangeSelector;

    // ==================== DOCK ====================
    @FXML
    private ToggleButton tabProtocol;
    @FXML
    private ToggleButton tabAlert;
    @FXML
    private ToggleButton tabSession;
    @FXML
    private ToggleButton tabTraffic;
    @FXML
    private FlowPane chipsContainer;
    @FXML
    private Label enabledMetricsLabel;

    // ==================== SERVICES ====================
    private AlertService alertService;
    private TrafficService trafficService;
    private MetricsManager metricsManager;
    private ChartDataService chartDataService;

    // ==================== DATA ====================
    private ObservableList<SecurityAlert> alertsList;
    private final Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
    private ScheduledExecutorService chartRefreshScheduler;
    private int timeRangeMinutes = 60;
    private final int REFRESH_INTERVAL_SECONDS = 5;
    private ToggleGroup categoryToggleGroup;

    private static final Map<String, String> CATEGORY_COLORS = Map.of(
            "Protocol", "#5294e0",
            "Alert", "#e24d42",
            "Session", "#73bf69",
            "Traffic", "#a352cc");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertService = AlertService.getInstance();
        trafficService = TrafficService.getInstance();
        metricsManager = MetricsManager.getInstance();
        chartDataService = new ChartDataService();

        // Original Dashboard functionality
        initializeTable();
        loadStatistics();
        loadRecentAlerts();

        // Chart functionality
        setupChart();
        setupTimeRangeSelector();
        setupCategoryTabs();

        // Start with Protocol tab
        if (tabProtocol != null) {
            tabProtocol.setSelected(true);
            showChipsForCategory("Protocol");
        }

        startChartAutoRefresh();
        System.out.println("✅ Dashboard initialized with Stats + Chart");
    }

    // ==================== ORIGINAL DASHBOARD METHODS ====================

    private void initializeTable() {
        if (recentAlertsTable == null)
            return;
        recentAlertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        severityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSeverity()));
        typeColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sourceColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSourceIP()));
        timestampColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTimestamp().toString()));
    }

    private void loadStatistics() {
        try {
            int totalAlerts = alertService.getTotalAlertsCount();
            int criticalAlerts = alertService.getCountBySeverity("Critical");
            int warningAlerts = alertService.getCountBySeverity("High") + alertService.getCountBySeverity("Medium");
            long packetsAnalyzed = trafficService.getTotalPacketsAnalyzed();

            if (totalAlertsLabel != null)
                totalAlertsLabel.setText(String.valueOf(totalAlerts));
            if (criticalAlertsLabel != null)
                criticalAlertsLabel.setText(String.valueOf(criticalAlerts));
            if (warningAlertsLabel != null)
                warningAlertsLabel.setText(String.valueOf(warningAlerts));
            if (packetsAnalyzedLabel != null)
                packetsAnalyzedLabel.setText(String.format("%,d", packetsAnalyzed));
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    private void loadRecentAlerts() {
        try {
            alertsList = FXCollections.observableArrayList(alertService.getRecentAlerts(10));
            if (recentAlertsTable != null)
                recentAlertsTable.setItems(alertsList);
        } catch (Exception e) {
            System.err.println("Error loading recent alerts: " + e.getMessage());
        }
    }

    // ==================== CHART SETUP ====================

    private void setupChart() {
        if (metricsChart == null)
            return;
        metricsChart.setAnimated(false);
        metricsChart.setLegendVisible(false);
        metricsChart.setCreateSymbols(false);
        if (chartXAxis != null)
            chartXAxis.setAnimated(false);
        if (chartYAxis != null) {
            chartYAxis.setAnimated(false);
            chartYAxis.setAutoRanging(true);
        }
    }

    private void setupTimeRangeSelector() {
        if (timeRangeSelector == null)
            return;

        ObservableList<String> timeRanges = FXCollections.observableArrayList(
                "5 min", "15 min", "30 min", "1 hour", "6 hours", "24 hours", "7 days", "30 days");

        timeRangeSelector.setItems(timeRanges);
        timeRangeSelector.setValue("1 hour");

        timeRangeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                timeRangeMinutes = getTimeRangeMinutes(newVal);
                refreshChartData();
            }
        });
    }

    private int getTimeRangeMinutes(String selection) {
        return switch (selection) {
            case "5 min" -> 5;
            case "15 min" -> 15;
            case "30 min" -> 30;
            case "1 hour" -> 60;
            case "6 hours" -> 360;
            case "24 hours" -> 1440;
            case "7 days" -> 10080;
            case "30 days" -> 43200;
            default -> 60;
        };
    }

    // ==================== CATEGORY TABS ====================

    private void setupCategoryTabs() {
        categoryToggleGroup = new ToggleGroup();

        setupTab(tabProtocol, "Protocol");
        setupTab(tabAlert, "Alert");
        setupTab(tabSession, "Session");
        setupTab(tabTraffic, "Traffic");

        categoryToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });
    }

    private void setupTab(ToggleButton tab, String category) {
        if (tab == null)
            return;
        tab.setToggleGroup(categoryToggleGroup);
        tab.setOnAction(e -> showChipsForCategory(category));
    }

    // ==================== CHIPS ====================

    private void showChipsForCategory(String category) {
        if (chipsContainer == null)
            return;

        chipsContainer.getChildren().clear();
        List<ChartMetric> metrics = metricsManager.getMetricsByCategory(category);
        String color = CATEGORY_COLORS.getOrDefault(category, "#5294e0");

        for (ChartMetric metric : metrics) {
            ToggleButton chip = createChip(metric, color);
            chipsContainer.getChildren().add(chip);
        }
    }

    private ToggleButton createChip(ChartMetric metric, String color) {
        ToggleButton chip = new ToggleButton(metric.getDisplayName());
        chip.getStyleClass().add("metric-chip");
        chip.setSelected(metric.isEnabled());
        updateChipStyle(chip, metric.isEnabled(), color);

        chip.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            metric.setEnabled(isSelected);
            updateChipStyle(chip, isSelected, color);

            if (isSelected) {
                addMetricToChart(metric);
            } else {
                removeMetricFromChart(metric);
            }

            updateEnabledMetricsLabel();
            refreshChartData();
        });

        return chip;
    }

    private void updateChipStyle(ToggleButton chip, boolean active, String color) {
        if (active) {
            chip.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        } else {
            chip.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #aaa;");
        }
    }

    // ==================== CHART DATA ====================

    private void addMetricToChart(ChartMetric metric) {
        if (metricsChart == null || seriesMap.containsKey(metric.getId()))
            return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(metric.getDisplayName());
        seriesMap.put(metric.getId(), series);
        metricsChart.getData().add(series);
    }

    private void removeMetricFromChart(ChartMetric metric) {
        if (metricsChart == null)
            return;
        XYChart.Series<String, Number> series = seriesMap.remove(metric.getId());
        if (series != null)
            metricsChart.getData().remove(series);
    }

    private void refreshChartData() {
        if (metricsChart == null)
            return;

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(timeRangeMinutes);
        int intervalMinutes = calculateOptimalInterval(timeRangeMinutes);

        List<ChartMetric> enabledMetrics = metricsManager.getEnabledMetrics();
        if (enabledMetrics.isEmpty())
            return;

        CompletableFuture.runAsync(() -> {
            try {
                Map<LocalDateTime, Map<String, Double>> data = new TreeMap<>();

                for (ChartMetric metric : enabledMetrics) {
                    Map<LocalDateTime, Double> metricData = chartDataService.getMetricData(
                            metric, startTime, endTime, intervalMinutes);
                    for (var entry : metricData.entrySet()) {
                        data.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
                                .put(metric.getId(), entry.getValue());
                    }
                }

                Platform.runLater(() -> updateChartWithData(data, enabledMetrics));
            } catch (Exception e) {
                System.err.println("Chart refresh error: " + e.getMessage());
            }
        });
    }

    private int calculateOptimalInterval(int range) {
        if (range <= 30)
            return 1;
        if (range <= 180)
            return 5;
        if (range <= 720)
            return 15;
        if (range <= 1440)
            return 30;
        if (range <= 10080)
            return 120;
        return 480;
    }

    private void updateChartWithData(Map<LocalDateTime, Map<String, Double>> data, List<ChartMetric> metrics) {
        if (metricsChart == null || data.isEmpty())
            return;

        DateTimeFormatter fmt = timeRangeMinutes > 1440
                ? DateTimeFormatter.ofPattern("MM/dd HH:mm")
                : DateTimeFormatter.ofPattern("HH:mm");

        for (ChartMetric metric : metrics) {
            XYChart.Series<String, Number> series = seriesMap.get(metric.getId());
            if (series != null) {
                series.getData().clear();
                for (var entry : data.entrySet()) {
                    String label = entry.getKey().format(fmt);
                    double value = entry.getValue().getOrDefault(metric.getId(), 0.0);
                    series.getData().add(new XYChart.Data<>(label, value));
                }
            }
        }

        List<String> categories = data.keySet().stream()
                .map(t -> t.format(fmt))
                .collect(Collectors.toList());
        if (chartXAxis != null)
            chartXAxis.setCategories(FXCollections.observableArrayList(categories));
    }

    private void updateEnabledMetricsLabel() {
        if (enabledMetricsLabel == null)
            return;
        int count = metricsManager.getEnabledCount();
        enabledMetricsLabel.setText(count + " active");
    }

    // ==================== AUTO-REFRESH ====================

    private void startChartAutoRefresh() {
        chartRefreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        chartRefreshScheduler.scheduleAtFixedRate(
                this::refreshChartData,
                REFRESH_INTERVAL_SECONDS,
                REFRESH_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    public void stopChartAutoRefresh() {
        if (chartRefreshScheduler != null && !chartRefreshScheduler.isShutdown()) {
            chartRefreshScheduler.shutdown();
        }
    }

    // ==================== ACTIONS ====================

    @FXML
    private void handleRefresh() {
        loadStatistics();
        loadRecentAlerts();
        refreshChartData();
    }

    @FXML
    private void handleChartRefresh() {
        refreshChartData();
    }

    public void refreshDashboard() {
        loadStatistics();
        loadRecentAlerts();
        refreshChartData();
    }
}