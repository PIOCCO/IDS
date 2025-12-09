package org.example.controllers;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.views.MainView;

public class MainController {
    private MainView mainView;
    private SidebarController sidebarController;
    private AccountListController accountListController;
    private AccountDetailsController accountDetailsController;

    public MainController() {
        this.sidebarController = new SidebarController();
        this.accountListController = new AccountListController();
        this.accountDetailsController = new AccountDetailsController();
        this.mainView = new MainView();
    }

    public void initializeStage(Stage primaryStage) {
        BorderPane root = mainView.createMainLayout(
                sidebarController.createSidebar(),
                accountListController.createAccountList(this),
                accountDetailsController.createAccountDetails()
        );

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Settings");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void onAccountSelected(org.example.models.Account account) {
        accountDetailsController.updateAccountDetails(account);
    }
}