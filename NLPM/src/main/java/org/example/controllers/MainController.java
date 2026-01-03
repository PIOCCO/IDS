package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import org.example.models.Account;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private VBox sidebarContainer;

    @FXML
    private StackPane contentContainer;

    private SidebarController sidebarController;
    private DashboardController dashboardController;
    private AlertsController alertsController;
    private TrafficController trafficController;
    private SettingsController settingsController;
    private ThreatSimulatorController threatSimulatorController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadSidebar();
            loadDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSidebar() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Sidebar.fxml"));
        VBox sidebar = loader.load();
        sidebarController = loader.getController();
        sidebarController.setMainController(this);
        mainContainer.setLeft(sidebar);
    }

    private void loadDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
        VBox dashboard = loader.load();
        dashboardController = loader.getController();
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(dashboard);
    }

    public void showDashboard() {
        try {
            loadDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlerts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Alerts.fxml"));
            VBox alerts = loader.load();
            alertsController = loader.getController();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(alerts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTraffic() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Traffic.fxml"));
            VBox traffic = loader.load();
            trafficController = loader.getController();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(traffic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
            VBox settings = loader.load();
            settingsController = loader.getController();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateUser.fxml"));
            VBox createUser = loader.load();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(createUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * NEW: Show Threat Simulator view
     */
    public void showThreatSimulator() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ThreatSimulator.fxml"));
            ScrollPane threatSimulator = loader.load();
            threatSimulatorController = loader.getController();
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(threatSimulator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when an account is selected in the account list
     */
    public void onAccountSelected(Account account) {
        if (account == null) {
            System.err.println("Warning: Null account selected");
            return;
        }

        System.out.println("Account selected: " + account.getName() + " (" + account.getEmail() + ")");

        if (dashboardController != null) {
            // dashboardController.updateForAccount(account);
        }

        if (alertsController != null) {
            // alertsController.filterByAccount(account);
        }

        if (trafficController != null) {
            // trafficController.filterByAccount(account);
        }
    }

    /**
     * Optional: Show account-specific details view
     */
    private void showAccountDetails(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AccountDetails.fxml"));
            VBox accountDetails = loader.load();

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(accountDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}