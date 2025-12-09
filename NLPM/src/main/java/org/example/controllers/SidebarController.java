package org.example.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.models.MenuItem;
import java.util.ArrayList;
import java.util.List;

public class SidebarController {
    private List<MenuItem> menuItems;

    public SidebarController() {
        initializeMenuItems();
    }

    private void initializeMenuItems() {
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("System", false));
        menuItems.add(new MenuItem("Bluetooth", false));
        menuItems.add(new MenuItem("Network", false));
        menuItems.add(new MenuItem("Accounts", true));
        menuItems.add(new MenuItem("Time & Language", false));
        menuItems.add(new MenuItem("Gaming", false));
        menuItems.add(new MenuItem("Accessibility", false));
        menuItems.add(new MenuItem("Privacy", false));
        menuItems.add(new MenuItem("Windows Update", false));
    }

    public VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #2d2d2d;");
        sidebar.setPrefWidth(200);

        Label settingsLabel = new Label("⚙ Settings");
        settingsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        VBox.setMargin(settingsLabel, new Insets(10, 0, 20, 0));

        sidebar.getChildren().add(settingsLabel);

        for (MenuItem item : menuItems) {
            Button btn = createSidebarButton(item.getName(), item.isSelected());
            sidebar.getChildren().add(btn);
        }

        return sidebar;
    }

    private Button createSidebarButton(String text, boolean selected) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(8, 10, 8, 10));

        if (selected) {
            btn.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; " +
                    "-fx-background-radius: 4; -fx-font-size: 13px; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; " +
                    "-fx-background-radius: 4; -fx-font-size: 13px; -fx-cursor: hand;");
        }

        btn.setOnMouseEntered(e -> {
            if (!selected) {
                btn.setStyle("-fx-background-color: #3d3d3d; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-font-size: 13px; -fx-cursor: hand;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!selected) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; " +
                        "-fx-background-radius: 4; -fx-font-size: 13px; -fx-cursor: hand;");
            }
        });

        return btn;
    }
}
