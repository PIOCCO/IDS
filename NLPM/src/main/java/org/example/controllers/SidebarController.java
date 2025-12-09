package org.example.controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML
    private VBox sidebarRoot;

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

    private MainController mainController;
    private Button selectedButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedButton = dashboardBtn;
        setButtonSelected(dashboardBtn, true);

        dashboardBtn.setOnAction(e -> switchView(dashboardBtn, () -> mainController.showDashboard()));
        alertsBtn.setOnAction(e -> switchView(alertsBtn, () -> mainController.showAlerts()));
        trafficBtn.setOnAction(e -> switchView(trafficBtn, () -> mainController.showTraffic()));
        settingsBtn.setOnAction(e -> switchView(settingsBtn, () -> mainController.showSettings()));
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
}
