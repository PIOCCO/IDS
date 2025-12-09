package org.example.views;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainView {

    public BorderPane createMainLayout(VBox sidebar, VBox accountList, VBox accountDetails) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        root.setLeft(sidebar);
        root.setCenter(accountList);
        root.setRight(accountDetails);

        return root;
    }
}
