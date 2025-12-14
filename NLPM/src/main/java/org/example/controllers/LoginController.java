package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.services.AuthenticationService;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink forgotPasswordLink;

    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthenticationService.getInstance();

        // Pre-fill username if remember me was enabled
        if (authService.isRememberMeEnabled()) {
            usernameField.setText(authService.getSavedUsername());
            rememberMeCheckBox.setSelected(true);
        }

        // Add enter key listener for password field
        passwordField.setOnAction(e -> handleLogin());

        // Add enter key listener for username field
        usernameField.setOnAction(e -> passwordField.requestFocus());

        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckBox.isSelected();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        if (authService.authenticate(username, password, rememberMe)) {
            // Login successful
            openMainApplication();
        } else {
            showError("Invalid username or password");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Default Credentials");
        alert.setContentText("Admin Login:\nUsername: admin\nPassword: admin123\n\nUser Login:\nUsername: user\nPassword: user123");
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Hide error after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openMainApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("IDS Monitor - Intrusion Detection System");
            stage.setScene(scene);
            stage.centerOnScreen();

            System.out.println("User logged in: " + authService.getCurrentUser().getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading main application");
        }
    }
}