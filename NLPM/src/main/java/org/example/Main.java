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
import org.example.database.DatabaseSeeder;
import org.example.services.AuthenticationService;

import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("Database connected successfully");

            // Seed database with sample data if needed
            seedDatabaseIfNeeded();

            // Initialize authentication
            AuthenticationService authService = AuthenticationService.getInstance();

            // REMOVED AUTO-LOGIN - Always show login screen
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

    private void seedDatabaseIfNeeded() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Database Setup");
        confirmDialog.setHeaderText("Initialize Database with Sample Data?");
        confirmDialog.setContentText("Would you like to populate the database with sample alerts and traffic data?\n\n" +
                "This is recommended for first-time setup.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseSeeder.seedDatabase();
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

    private void loadMainApplication(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("IDS Monitor - Intrusion Detection System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
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
        alert.setContentText("The application will close and database connections will be terminated.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
        DatabaseManager.getInstance().close();
        System.out.println("Application closed successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }
}