package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import org.example.services.AuthenticationService;
import org.example.database.dao.UserDAO;
import org.example.models.User;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateUserController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Button createUserBtn;

    @FXML
    private Button clearBtn;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private Button deleteUserBtn;

    @FXML
    private Button refreshBtn;

    @FXML
    private Label messageLabel;

    private AuthenticationService authService;
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthenticationService.getInstance();
        userDAO = new UserDAO();

        initializeRoleCombo();
        initializeTable();
        loadUsers();

        createUserBtn.setOnAction(e -> handleCreateUser());
        clearBtn.setOnAction(e -> clearForm());
        deleteUserBtn.setOnAction(e -> handleDeleteUser());
        refreshBtn.setOnAction(e -> loadUsers());

        messageLabel.setVisible(false);
    }

    private void initializeRoleCombo() {
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "USER", "VIEWER"));
        roleCombo.setValue("USER");
    }

    private void initializeTable() {
        usernameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        emailColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isActive() ? "Active" : "Inactive"));
    }

    private void loadUsers() {
        try {
            var users = userDAO.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
            System.out.println("Loaded " + users.size() + " users from database");
        } catch (Exception e) {
            showError("Failed to load users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateUser() {
        // Validate inputs
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty()) {
            showError("Username is required");
            return;
        }

        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }

        if (!email.contains("@")) {
            showError("Invalid email format");
            return;
        }

        // Check if user already exists in AuthService
        if (userDAO.getUserByUsername(username) != null) {
            showError("Username already exists");
            return;
        }

        // Create user
        boolean success = authService.registerUser(username, password, role);

        if (success) {
            // Also save to database
            User newUser = new User(username, password, role);
            newUser.setEmail(email);
            boolean dbSuccess = userDAO.insertUser(newUser);

            if (dbSuccess) {
                showSuccess("User '" + username + "' created successfully!");
                clearForm();
                loadUsers();
            } else {
                showError("User created in memory but failed to save to database");
            }
        } else {
            showError("Failed to create user");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Please select a user to delete");
            return;
        }

        // Prevent deleting current user
        if (authService.getCurrentUser() != null &&
                selectedUser.getUsername().equals(authService.getCurrentUser().getUsername())) {
            showError("Cannot delete currently logged-in user");
            return;
        }

        // Prevent deleting default admin
        if (selectedUser.getUsername().equals("admin")) {
            showError("Cannot delete default admin user");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText("Delete user: " + selectedUser.getUsername() + "?");
        confirmAlert.setContentText("This action cannot be undone.");

        var result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.deleteUser(selectedUser.getUsername());
            if (success) {
                showSuccess("User deleted successfully");
                loadUsers();
            } else {
                showError("Failed to delete user");
            }
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
        roleCombo.setValue("USER");
        messageLabel.setVisible(false);
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }
}