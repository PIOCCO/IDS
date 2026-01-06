package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.example.utils.DatabaseManager;
import org.example.services.AuthenticationService;
import org.example.services.PacketCaptureService;
import org.example.services.DetectionEngine;
import org.example.services.AlertNotificationService;

import java.util.Optional;

/**
 * IDS Monitor Application Entry Point.
 * 
 * This class serves as the main entry point for the IDS Monitor application.
 * It follows a layered architecture with proper initialization order:
 * 1. Database Connection
 * 2. Core Services (DetectionEngine, AlertNotificationService)
 * 3. Authentication Service
 * 4. UI (Login Screen)
 */
public class IDSMonitorApplication extends Application {

    private static final String APP_NAME = "IDS Monitor";
    private static final String APP_VERSION = "1.0.0";

    @Override
    public void start(Stage primaryStage) {
        System.out.println("=".repeat(50));
        System.out.println(" " + APP_NAME + " v" + APP_VERSION);
        System.out.println("=".repeat(50));

        try {
            // Step 1: Initialize database connection
            System.out.println("[1/4] Initializing database connection...");
            if (!testDatabaseConnection()) {
                showDatabaseError();
                return;
            }
            System.out.println("      ✓ Database connected successfully");

            // Step 2: Initialize core services
            System.out.println("[2/4] Initializing core services...");
            DetectionEngine.getInstance();
            AlertNotificationService.getInstance();
            System.out.println("      ✓ Detection engine ready");
            System.out.println("      ✓ Alert notification service ready");

            // Step 3: Initialize authentication
            System.out.println("[3/4] Initializing authentication service...");
            AuthenticationService.getInstance();
            System.out.println("      ✓ Authentication service ready");

            // Step 4: Load UI
            System.out.println("[4/4] Loading user interface...");
            loadLoginScreen(primaryStage);
            System.out.println("      ✓ Application started successfully");
            System.out.println("=".repeat(50));

            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                handleApplicationClose(primaryStage);
            });

        } catch (Exception e) {
            System.err.println("✗ Startup failed: " + e.getMessage());
            e.printStackTrace();
            showDatabaseError();
        }
    }

    /**
     * Test database connection before proceeding.
     */
    private boolean testDatabaseConnection() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            return dbManager.getConnection() != null;
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load the login screen.
     */
    private void loadLoginScreen(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            StackPane root = loader.load();

            Scene scene = new Scene(root, 600, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle(APP_NAME + " - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle application close with confirmation.
     */
    private void handleApplicationClose(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("All monitoring will stop and database connections will be terminated.");
        org.example.utils.DialogUtils.styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            shutdown();
            primaryStage.close();
            System.exit(0);
        }
    }

    /**
     * Show database connection error dialog.
     */
    private void showDatabaseError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("Failed to connect to database");
        alert.setContentText(
                "Please ensure PostgreSQL is running and configured correctly.\n\n" +
                        "Check database.properties file for connection details.");
        org.example.utils.DialogUtils.styleAlert(alert);
        alert.showAndWait();
        System.exit(1);
    }

    /**
     * Clean shutdown of all services.
     */
    private void shutdown() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" Shutting down " + APP_NAME + "...");
        System.out.println("=".repeat(50));

        try {
            // Stop packet capture
            PacketCaptureService captureService = PacketCaptureService.getInstance();
            if (captureService.isCapturing()) {
                captureService.stopCapture();
            }
            captureService.shutdown();
            System.out.println("  ✓ Packet capture stopped");

            // Shutdown detection engine
            DetectionEngine.getInstance().shutdown();
            System.out.println("  ✓ Detection engine stopped");

            // Shutdown notification service
            AlertNotificationService.getInstance().shutdown();
            System.out.println("  ✓ Notification service stopped");

            // Close database connections
            DatabaseManager.getInstance().close();
            System.out.println("  ✓ Database connections closed");

            System.out.println("=".repeat(50));
            System.out.println(" Application closed successfully");
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        shutdown();
    }

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
