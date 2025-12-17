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

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

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

    private MainController mainController;
    private Button selectedButton;
    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthenticationService.getInstance();

        selectedButton = dashboardBtn;
        setButtonSelected(dashboardBtn, true);

        dashboardBtn.setOnAction(e -> switchView(dashboardBtn, () -> mainController.showDashboard()));
        alertsBtn.setOnAction(e -> switchView(alertsBtn, () -> mainController.showAlerts()));
        trafficBtn.setOnAction(e -> switchView(trafficBtn, () -> mainController.showTraffic()));
        settingsBtn.setOnAction(e -> switchView(settingsBtn, () -> mainController.showSettings()));
        createUserBtn.setOnAction(e -> switchView(createUserBtn, () -> mainController.showCreateUser()));

        // Display current user
        if (authService.getCurrentUser() != null) {
            currentUserLabel.setText("👤 " + authService.getCurrentUser().getUsername());
        }
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
        action.run();
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
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will need to login again to access the system.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
            }
        }
    }
}