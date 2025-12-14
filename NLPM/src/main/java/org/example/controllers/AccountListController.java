package org.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.database.dao.AccountDAO;
import org.example.models.Account;

import java.util.List;

public class AccountListController {
    private List<Account> accounts;
    private Account selectedAccount;
    private AccountDAO accountDAO;

    public AccountListController() {
        accountDAO = new AccountDAO();
        initializeAccounts();
    }

    private void initializeAccounts() {
        // Initialize default accounts if database is empty
        accountDAO.initializeDefaultAccounts();

        // Load accounts from database
        accounts = accountDAO.getAllAccounts();

        if (!accounts.isEmpty()) {
            selectedAccount = accounts.get(0);
            selectedAccount.setSelected(true);
        }

        System.out.println("Loaded " + accounts.size() + " accounts from database");
    }

    public VBox createAccountList(MainController mainController) {
        VBox accountList = new VBox(0);
        accountList.setPadding(new Insets(20));
        accountList.setStyle("-fx-background-color: #252525;");
        accountList.setPrefWidth(350);

        Label title = new Label("Accounts");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        accountList.getChildren().add(title);

        for (Account account : accounts) {
            HBox accountItem = createAccountItem(account, mainController);
            accountList.getChildren().add(accountItem);
        }

        return accountList;
    }

    private HBox createAccountItem(Account account, MainController mainController) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));

        if (account.isSelected()) {
            item.setStyle("-fx-background-color: #0078d4; -fx-cursor: hand;");
        } else {
            item.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        }

        Circle avatar = new Circle(20);
        avatar.setFill(javafx.scene.paint.Color.web(account.getColor()));

        VBox textBox = new VBox(2);
        Label nameLabel = new Label(account.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label emailLabel = new Label(account.getEmail());
        emailLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");

        textBox.getChildren().addAll(nameLabel, emailLabel);

        item.getChildren().addAll(avatar, textBox);

        item.setOnMouseEntered(e -> {
            if (!account.isSelected()) {
                item.setStyle("-fx-background-color: #3d3d3d; -fx-cursor: hand;");
            }
        });

        item.setOnMouseExited(e -> {
            if (!account.isSelected()) {
                item.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            }
        });

        item.setOnMouseClicked(e -> {
            if (selectedAccount != null) {
                selectedAccount.setSelected(false);
            }
            account.setSelected(true);
            selectedAccount = account;
            mainController.onAccountSelected(account);

            // Refresh the list
            VBox parent = (VBox) item.getParent();
            parent.getChildren().clear();
            Label title = new Label("Accounts");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            VBox.setMargin(title, new Insets(0, 0, 20, 0));
            parent.getChildren().add(title);

            for (Account acc : accounts) {
                parent.getChildren().add(createAccountItem(acc, mainController));
            }
        });

        return item;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void refreshAccounts() {
        accounts = accountDAO.getAllAccounts();
    }
}