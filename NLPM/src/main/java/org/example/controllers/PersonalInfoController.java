package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.services.UserService;
import org.example.models.User;
import org.example.services.AuthenticationService;
import org.example.utils.AuditLogger;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;

public class PersonalInfoController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField roleField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdLabel;
    @FXML
    private Label lastLoginLabel;
    @FXML
    private Button saveBtn;
    @FXML
    private Button resetBtn;
    @FXML
    private Button deleteAccountBtn;

    private AuthenticationService authService;
    private UserService userService;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthenticationService.getInstance();
        userService = UserService.getInstance();

        // Load current user data
        loadUserData();

        // Setup button handlers
        saveBtn.setOnAction(e -> handleSave());
        resetBtn.setOnAction(e -> handleReset());
        deleteAccountBtn.setOnAction(e -> handleDeleteAccount());

        // Clear message initially
        messageLabel.setText("");
    }

    private void loadUserData() {
        User authUser = authService.getCurrentUser();

        if (authUser == null) {
            showError("No user logged in");
            return;
        }

        // IMPORTANT: Load the COMPLETE user from DATABASE (not from in-memory cache)
        // The AuthenticationService only stores basic user info without email
        currentUser = userService.findByUsername(authUser.getUsername()).orElse(null);

        if (currentUser == null) {
            // Fallback to auth user if not in database
            currentUser = authUser;
            System.err.println("Warning: User not found in database, using in-memory data");
        }

        // Populate fields with database data
        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        roleField.setText(formatRole(currentUser.getRole()));

        // Status
        if (currentUser.isActive()) {
            statusLabel.setText("● Active");
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
        } else {
            statusLabel.setText("● Inactive");
            statusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 13px;");
        }

        // Created and Last Login (placeholder - would need DB fields)
        createdLabel.setText("Account active");
        lastLoginLabel.setText("Current session");

        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private String formatRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Administrator";
            case "USER" -> "Standard User";
            case "VIEWER" -> "Viewer (Read-only)";
            default -> role;
        };
    }

    private void handleSave() {
        // Validate current password
        String currentPassword = currentPasswordField.getText();
        if (currentPassword.isEmpty()) {
            showError("Current password is required to save changes");
            return;
        }

        // Verify current password
        if (!authService.authenticate(currentUser.getUsername(), currentPassword, false)) {
            showError("Current password is incorrect");
            return;
        }

        // Validate new password if provided
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                showError("New password must be at least 6 characters");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords do not match");
                return;
            }
        }

        try {
            // Update email
            String newEmail = emailField.getText().trim();
            boolean success;

            if (!newPassword.isEmpty()) {
                // Password is being changed - update both email and password
                String hashedPassword = hashPassword(newPassword);
                currentUser.setPassword(hashedPassword);
                currentUser.setEmail(newEmail);
                success = userService.updateUser(currentUser) != null;
                System.out.println("Updating email AND password for user: " + currentUser.getUsername());
            } else {
                // Only email is being changed - use email-only update
                try {
                    success = userService.updateEmail(currentUser.getUsername(), newEmail);
                } catch (Exception e) {
                    success = false;
                }
                currentUser.setEmail(newEmail); // Update local object
                System.out.println("Updating email ONLY for user: " + currentUser.getUsername());
            }

            if (success) {
                AuditLogger.log(currentUser.getUsername(), "PROFILE_UPDATE", "User updated their profile");
                showSuccess("Profile updated successfully!");

                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                showError("Failed to save changes to database");
            }

        } catch (Exception e) {
            showError("Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleReset() {
        loadUserData();
        showInfo("Form reset to current values");
    }

    private void handleDeleteAccount() {
        // Prevent deleting admin account
        if (currentUser.getUsername().equals("admin")) {
            showError("Cannot delete the default admin account");
            return;
        }

        // First confirmation
        Alert confirmAlert = org.example.utils.DialogUtils.createWarning(
                "Delete Account",
                "⚠️ WARNING: This action is irreversible!",
                "You are about to permanently delete your account:\n\n" +
                        "• Username: " + currentUser.getUsername() + "\n" +
                        "• Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "N/A") + "\n" +
                        "• Role: " + currentUser.getRole() + "\n\n" +
                        "All your data will be permanently deleted.\n\n" +
                        "Are you absolutely sure?");

        ButtonType deleteBtn = new ButtonType("Yes, Delete My Account", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(deleteBtn, cancelBtn);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == deleteBtn) {
            // Second confirmation - password required
            TextInputDialog passwordDialog = org.example.utils.DialogUtils.createTextInput(
                    "Confirm Deletion",
                    "🔐 Enter your password to confirm",
                    "Password:");

            Optional<String> passwordResult = passwordDialog.showAndWait();

            if (passwordResult.isPresent()) {
                String password = passwordResult.get();

                // Verify password
                if (!authService.authenticate(currentUser.getUsername(), password, false)) {
                    showError("Incorrect password. Account deletion cancelled.");
                    return;
                }

                try {
                    // Delete from database
                    boolean success = userService.deleteUser(currentUser.getUsername());

                    if (success) {
                        // Log action before logout
                        AuditLogger.log(currentUser.getUsername(), "ACCOUNT_DELETED",
                                "User deleted their own account");

                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Account Deleted");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText(
                                "Your account has been permanently deleted.\n\nYou will now be redirected to the login page.");
                        org.example.utils.DialogUtils.styleAlert(successAlert);
                        successAlert.showAndWait();

                        // Logout and redirect to login
                        authService.logout();

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                        StackPane root = loader.load();

                        Scene scene = new Scene(root, 600, 700);
                        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                        Stage stage = (Stage) deleteAccountBtn.getScene().getWindow();
                        stage.setTitle("IDS Monitor - Login");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.centerOnScreen();

                    } else {
                        showError("Failed to delete account from database");
                    }
                } catch (Exception e) {
                    showError("Failed to delete account: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 13px; -fx-font-weight: bold;");
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px; -fx-font-weight: bold;");
    }

    private void showInfo(String message) {
        messageLabel.setText("ℹ️ " + message);
        messageLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 13px;");
    }
}
