package org.example.controllers;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private TextField interfaceField;

    @FXML
    private TextField alertThresholdField;

    @FXML
    private CheckBox enableLoggingCheck;

    @FXML
    private CheckBox enableEmailAlertsCheck;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> logLevelCombo;

    @FXML
    private Button saveBtn;

    @FXML
    private Button resetBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeControls();
        loadSettings();

        saveBtn.setOnAction(e -> saveSettings());
        resetBtn.setOnAction(e -> resetSettings());
    }

    private void initializeControls() {
        logLevelCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"
        ));
        logLevelCombo.setValue("INFO");
    }

    private void loadSettings() {
        interfaceField.setText("eth0");
        alertThresholdField.setText("100");
        enableLoggingCheck.setSelected(true);
        enableEmailAlertsCheck.setSelected(false);
        emailField.setText("admin@company.com");
    }

    private void saveSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your settings have been saved successfully!");
        alert.showAndWait();
    }

    private void resetSettings() {
        loadSettings();
    }
}
