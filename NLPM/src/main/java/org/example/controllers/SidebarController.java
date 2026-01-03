package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.services.AuthenticationService;
import org.example.models.User;
import org.example.utils.AuditLogger;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML
    private Button threatSimulatorBtn;

    @FXML
    private VBox sidebarRoot;

    @FXML
    private Button createUserBtn;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button alertsBtn;

    @FXML
    private Button trafficBtn;

    @FXML
    private Button reportsBtn;

    @FXML
    private Button rulesBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Label currentUserLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private VBox adminSection;

    private MainController mainController;
    private Button selectedButton;
    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthenticationService.getInstance();

        // Apply role-based access control
        applyRoleBasedAccess();

        selectedButton = dashboardBtn;
        setButtonSelected(dashboardBtn, true);

        dashboardBtn.setOnAction(e -> switchView(dashboardBtn, () -> mainController.showDashboard()));
        alertsBtn.setOnAction(e -> switchView(alertsBtn, () -> mainController.showAlerts()));
        trafficBtn.setOnAction(e -> switchView(trafficBtn, () -> mainController.showTraffic()));
        settingsBtn.setOnAction(e -> switchView(settingsBtn, () -> mainController.showSettings()));

        // NEW: Threat Simulator button
        if (threatSimulatorBtn != null) {
            threatSimulatorBtn.setOnAction(e -> switchView(threatSimulatorBtn, () -> mainController.showThreatSimulator()));
        }

        // Only initialize createUserBtn if user is admin
        if (createUserBtn != null && !createUserBtn.isDisabled()) {
            createUserBtn.setOnAction(e -> switchView(createUserBtn, () -> mainController.showCreateUser()));
        }

        // Display current user information
        updateUserDisplay();
    }

    /**
     * Apply role-based access control to sidebar buttons
     */
    private void applyRoleBasedAccess() {
        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            System.err.println("No user logged in!");
            return;
        }

        String role = currentUser.getRole().toUpperCase();

        switch (role) {
            case "ADMIN":
                // Admin voit tout
                adminSection.setVisible(true);
                adminSection.setManaged(true);

                if (settingsBtn != null) settingsBtn.setDisable(false);
                if (rulesBtn != null) rulesBtn.setDisable(false);
                break;

            case "USER":
                // User : PAS d'administration
                adminSection.setVisible(false);
                adminSection.setManaged(false);

                if (settingsBtn != null) settingsBtn.setDisable(false);
                if (rulesBtn != null) rulesBtn.setDisable(true);
                break;

            case "VIEWER":
                // Viewer : lecture seule
                adminSection.setVisible(false);
                adminSection.setManaged(false);

                if (settingsBtn != null) settingsBtn.setDisable(true);
                if (rulesBtn != null) rulesBtn.setDisable(true);
                break;

            default:
                // Sécurité maximale
                adminSection.setVisible(false);
                adminSection.setManaged(false);

                if (settingsBtn != null) settingsBtn.setDisable(true);
                if (rulesBtn != null) rulesBtn.setDisable(true);
                break;
        }
    }


    /**
     * Update user display with current user information
     */
    private void updateUserDisplay() {
        User currentUser = authService.getCurrentUser();

        if (currentUser != null) {
            // Update username display with role indicator
            String roleEmoji = getRoleEmoji(currentUser.getRole());
            currentUserLabel.setText(roleEmoji + " " + currentUser.getUsername());

            // Update role label if it exists
            if (userRoleLabel != null) {
                userRoleLabel.setText(formatRole(currentUser.getRole()));
                userRoleLabel.setStyle(getRoleStyle(currentUser.getRole()));
            }

            // Set color based on role
            currentUserLabel.setStyle(getRoleStyle(currentUser.getRole()));
        } else {
            currentUserLabel.setText("👤 Guest");
            if (userRoleLabel != null) {
                userRoleLabel.setText("No Access");
            }
        }
    }

    /**
     * Get emoji for user role
     */
    private String getRoleEmoji(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "👑";
            case "USER" -> "👤";
            case "VIEWER" -> "👁️";
            default -> "❓";
        };
    }

    /**
     * Format role name for display
     */
    private String formatRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Administrator";
            case "USER" -> "Standard User";
            case "VIEWER" -> "Read-Only";
            default -> "Unknown";
        };
    }

    /**
     * Get style for role display
     */
    private String getRoleStyle(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "-fx-text-fill: #FFD700; -fx-font-size: 13px; -fx-font-weight: bold;";
            case "USER" -> "-fx-text-fill: #4CAF50; -fx-font-size: 13px; -fx-font-weight: bold;";
            case "VIEWER" -> "-fx-text-fill: #2196F3; -fx-font-size: 13px; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #808080; -fx-font-size: 13px;";
        };
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void switchView(Button button, Runnable action) {
        if (selectedButton != null) {
            setButtonSelected(selectedButton, false);
        }
        setButtonSelected(button, true);
        selectedButton = button;

        try {
            action.run();
        } catch (Exception e) {
            System.err.println("Error switching view: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load view: " + e.getMessage());
        }
    }

    private void setButtonSelected(Button button, boolean selected) {
        if (selected) {
            button.getStyleClass().add("sidebar-button-selected");
        } else {
            button.getStyleClass().remove("sidebar-button-selected");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to access the system.\n\n" +
                "Any active monitoring will be stopped.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Log the logout action
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                AuditLogger.log(currentUser.getUsername(), "LOGOUT",
                        "User logged out from the system");
            }

            // Logout user
            authService.logout();

            // Load login screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                StackPane root = loader.load();

                Scene scene = new Scene(root, 600, 700);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setTitle("IDS Monitor - Login");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.centerOnScreen();

                System.out.println("User logged out successfully");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading login screen: " + e.getMessage());
            }
        }
    }

    /**
     * Show error dialog
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Refresh user display (useful after profile updates)
     */
    public void refreshUserDisplay() {
        updateUserDisplay();
        applyRoleBasedAccess();
    }
}