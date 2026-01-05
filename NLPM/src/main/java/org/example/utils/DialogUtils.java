package org.example.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;

/**
 * Utility class for creating styled dialogs that match the application's dark
 * theme
 * All dialogs use a unified dark color scheme without bright colors
 */
public class DialogUtils {

    // Unified dark style for ALL dialog types - no bright colors
    private static final String DARK_STYLE = """
                .dialog-pane {
                    -fx-background-color: #1e1e1e;
                    -fx-border-color: #404040;
                    -fx-border-width: 1px;
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                }
                .dialog-pane > .content.label {
                    -fx-text-fill: #c0c0c0;
                    -fx-font-size: 13px;
                    -fx-padding: 10 0 10 0;
                }
                .dialog-pane > .header-panel {
                    -fx-background-color: #252525;
                    -fx-background-radius: 8px 8px 0 0;
                    -fx-padding: 15;
                }
                .dialog-pane > .header-panel .label {
                    -fx-text-fill: #e0e0e0;
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                }
                .dialog-pane > .header-panel .graphic-container {
                    -fx-padding: 0 10 0 0;
                }
                .dialog-pane .button {
                    -fx-background-color: #3a3a3a;
                    -fx-text-fill: #e0e0e0;
                    -fx-font-size: 12px;
                    -fx-padding: 8 20;
                    -fx-background-radius: 4px;
                    -fx-border-radius: 4px;
                    -fx-cursor: hand;
                }
                .dialog-pane .button:hover {
                    -fx-background-color: #4a4a4a;
                }
                .dialog-pane .button:default {
                    -fx-background-color: #505050;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                }
                .dialog-pane .button:default:hover {
                    -fx-background-color: #606060;
                }
                .dialog-pane .button:cancel {
                    -fx-background-color: #353535;
                }
                .dialog-pane .button:cancel:hover {
                    -fx-background-color: #454545;
                }
                .dialog-pane > .button-bar > .container {
                    -fx-background-color: #1e1e1e;
                    -fx-padding: 10 15 15 15;
                }
                .dialog-pane .text-field, .dialog-pane .password-field {
                    -fx-background-color: #2a2a2a;
                    -fx-text-fill: #e0e0e0;
                    -fx-border-color: #505050;
                    -fx-border-width: 1px;
                    -fx-border-radius: 4px;
                    -fx-background-radius: 4px;
                    -fx-padding: 8px;
                    -fx-prompt-text-fill: #707070;
                }
                .dialog-pane .text-field:focused, .dialog-pane .password-field:focused {
                    -fx-border-color: #707070;
                }
                .dialog-pane .label {
                    -fx-text-fill: #c0c0c0;
                }
                .dialog-pane .combo-box {
                    -fx-background-color: #2a2a2a;
                    -fx-border-color: #505050;
                }
            """;

    /**
     * Apply dark theme styling to any existing Alert
     */
    public static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e;");
        dialogPane.getStylesheets().add("data:text/css," + DARK_STYLE.replace("\n", "").replace("    ", ""));
    }

    /**
     * Apply dark theme styling to any Dialog
     */
    public static void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e;");
        dialogPane.getStylesheets().add("data:text/css," + DARK_STYLE.replace("\n", "").replace("    ", ""));
    }

    /**
     * Create a styled confirmation alert
     */
    public static Alert createConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create a styled warning alert
     */
    public static Alert createWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create a styled error alert
     */
    public static Alert createError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create a styled information alert
     */
    public static Alert createInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create a styled text input dialog
     */
    public static TextInputDialog createTextInput(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        styleDialog(dialog);
        return dialog;
    }
}
