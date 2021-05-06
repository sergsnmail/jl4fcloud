package com.sergsnmail.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        new AppController(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
