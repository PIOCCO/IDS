package org.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.models.Account;

public class AccountDetailsController {
    private VBox detailsPanel;
    private Circle avatarCircle;
    private Label accountNameLabel;
    private VBox emailField;
    private VBox passwordField;
    private VBox usernameField;
    private VBox displayNameField;

    public VBox createAccountDetails() {
        detailsPanel = new VBox(20);
        detailsPanel.setPadding(new Insets(20));
        detailsPanel.setStyle("-fx-background-color: #1e1e1e;");
        detailsPanel.setPrefWidth(350);

        // Header with avatar and name
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        avatarCircle = new Circle(30);
        avatarCircle.setFill(javafx.scene.paint.Color.web("#0078d4"));

        accountNameLabel = new Label("Apple");
        accountNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        header.getChildren().addAll(avatarCircle, accountNameLabel);

        // Form fields
        emailField = createFormField("Email", "me@icloud.com");
        passwordField = createFormField("Password", "••••••••");
        usernameField = createFormField("Username", "");
        displayNameField = createFormField("Display name", "");

        detailsPanel.getChildren().addAll(header, emailField, passwordField, usernameField, displayNameField);

        return detailsPanel;
    }

    private VBox createFormField(String labelText, String value) {
        VBox fieldBox = new VBox(5);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");

        TextField textField = new TextField(value);
        textField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; " +
                "-fx-border-color: #3d3d3d; -fx-border-width: 1; " +
                "-fx-background-radius: 4; -fx-border-radius: 4; " +
                "-fx-padding: 8;");

        fieldBox.getChildren().addAll(label, textField);

        return fieldBox;
    }

    public void updateAccountDetails(Account account) {
        avatarCircle.setFill(javafx.scene.paint.Color.web(account.getColor()));
        accountNameLabel.setText(account.getName());

        // Update text fields
        if (emailField != null && emailField.getParent() instanceof VBox) {
            VBox emailBox = (VBox) emailField.getParent();
            TextField emailTF = (TextField) emailBox.getChildren().get(1);
            emailTF.setText(account.getEmail());
        }
    }
}