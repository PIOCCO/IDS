package org.example.services;

import org.example.models.ChartMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for ChartMetric operations.
 * Provides business logic for chart metric management.
 */
public class ChartMetricService {

    private static ChartMetricService instance;
    private final MetricsManager metricsManager;

    private ChartMetricService() {
        this.metricsManager = MetricsManager.getInstance();
    }

    public static synchronized ChartMetricService getInstance() {
        if (instance == null) {
            instance = new ChartMetricService();
        }
        return instance;
    }

    /**
     * Get all available metrics.
     */
    public List<ChartMetric> getAllMetrics() {
        return metricsManager.getAllMetrics();
    }

    /**
     * Get metrics by category.
     */
    public List<ChartMetric> getMetricsByCategory(String category) {
        return metricsManager.getMetricsByCategory(category);
    }

    /**
     * Find metric by ID.
     */
    public Optional<ChartMetric> findById(String metricId) {
        return metricsManager.getAllMetrics().stream()
                .filter(m -> m.getId().equals(metricId))
                .findFirst();
    }

    /**
     * Get enabled metrics only.
     */
    public List<ChartMetric> getEnabledMetrics() {
        return metricsManager.getAllMetrics().stream()
                .filter(ChartMetric::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * Toggle metric enabled state.
     */
    public void toggleMetric(String metricId, boolean enabled) {
        findById(metricId).ifPresent(m -> m.setEnabled(enabled));
    }

    /**
     * Get all available categories.
     */
    public List<String> getAllCategories() {
        return metricsManager.getAllMetrics().stream()
                .map(ChartMetric::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Count metrics by category.
     */
    public long countByCategory(String category) {
        return metricsManager.getMetricsByCategory(category).size();
    }
}
