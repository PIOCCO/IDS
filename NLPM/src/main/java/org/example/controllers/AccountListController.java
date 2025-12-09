package org.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.models.Account;
import java.util.ArrayList;
import java.util.List;

public class AccountListController {
    private List<Account> accounts;
    private Account selectedAccount;

    public AccountListController() {
        initializeAccounts();
    }

    private void initializeAccounts() {
        accounts = new ArrayList<>();
        accounts.add(new Account("Apple", "me@icloud.com", "#0078d4"));
        accounts.add(new Account("Dropbox", "dropbox@mail.com", "#4a90e2"));
        accounts.add(new Account("Facebook", "fb@company.com", "#3b5998"));
        accounts.add(new Account("Adobe", "adobe@mail.com", "#ff0000"));
        accounts.add(new Account("Amazon", "amazon@mail.com", "#ff9900"));
        accounts.add(new Account("Google", "google@mail.com", "#4285f4"));
        accounts.add(new Account("Ebay", "ebay@mail.com", "#e53238"));
        accounts.add(new Account("Yahoo", "yahoo@mail.com", "#6001d2"));

        if (!accounts.isEmpty()) {
            selectedAccount = accounts.get(0);
            selectedAccount.setSelected(true);
        }
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
}
