package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.controllers.MainController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();
        controller.initializeStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
