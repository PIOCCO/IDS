package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.database.DatabaseManager;
import org.example.services.AuthenticationService;
import org.example.services.PacketCaptureService;
import org.example.services.DetectionEngine;
import org.example.services.AlertNotificationService;

import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("Database connected successfully");

            // Initialize services
            DetectionEngine.getInstance();
            AlertNotificationService.getInstance();

            // Initialize authentication
            AuthenticationService authService = AuthenticationService.getInstance();

            // Always show login screen
            loadLoginScreen(primaryStage);

            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                handleApplicationClose(primaryStage);
            });

        } catch (Exception e) {
            e.printStackTrace();
            showDatabaseError();
        }
    }

    private void loadLoginScreen(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            StackPane root = loader.load();

            Scene scene = new Scene(root, 600, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("IDS Monitor - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleApplicationClose(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("All monitoring will stop and database connections will be terminated.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Stop packet capture if running
            PacketCaptureService captureService = PacketCaptureService.getInstance();
            if (captureService.isCapturing()) {
                captureService.stopCapture();
            }
            captureService.shutdown();

            // Shutdown detection engine
            DetectionEngine.getInstance().shutdown();

            // Shutdown notification service
            AlertNotificationService.getInstance().shutdown();

            // Close database connections
            DatabaseManager.getInstance().close();

            primaryStage.close();
            System.exit(0);
        }
    }

    private void showDatabaseError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText("Failed to connect to database");
        alert.setContentText("Please ensure PostgreSQL is running and configured correctly.\n\n" +
                "Check database.properties file for connection details.");
        alert.showAndWait();
        System.exit(1);
    }

    @Override
    public void stop() {
        // Clean shutdown
        try {
            PacketCaptureService.getInstance().shutdown();
            DetectionEngine.getInstance().shutdown();
            AlertNotificationService.getInstance().shutdown();
            DatabaseManager.getInstance().close();
            System.out.println("Application closed successfully");
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}