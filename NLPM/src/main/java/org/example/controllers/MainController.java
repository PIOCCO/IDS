package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import org.example.models.Account;

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

    /**
     * Called when an account is selected in the account list
     * This method can be used to update views or perform actions based on the selected account
     *
     * @param account The selected Account object
     */
    public void onAccountSelected(Account account) {
        if (account == null) {
            System.err.println("Warning: Null account selected");
            return;
        }

        // Log the selection
        System.out.println("Account selected: " + account.getName() + " (" + account.getEmail() + ")");

        // Update dashboard if it's currently loaded
        if (dashboardController != null) {
            // You can add methods to update dashboard based on selected account
            // dashboardController.updateForAccount(account);
        }

        // Update alerts view if it's currently loaded
        if (alertsController != null) {
            // Filter alerts by account if needed
            // alertsController.filterByAccount(account);
        }

        // Update traffic view if it's currently loaded
        if (trafficController != null) {
            // Filter traffic by account if needed
            // trafficController.filterByAccount(account);
        }

        // You can also switch to a specific view automatically
        // For example, show account details or account-specific dashboard
        // showAccountDetails(account);
    }

    /**
     * Optional: Show account-specific details view
     *
     * @param account The account to display details for
     */
    private void showAccountDetails(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AccountDetails.fxml"));
            VBox accountDetails = loader.load();

            // Pass the account to the details controller
            // AccountDetailsController controller = loader.getController();
            // controller.setAccount(account);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(accountDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}