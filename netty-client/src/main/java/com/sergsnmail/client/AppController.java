package com.sergsnmail.client;

import com.sergsnmail.client.network.ClientNetwork;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.sergsnmail.common.message.common.UserSession;
import javafx.stage.Window;

import java.io.IOException;

public class AppController implements AppCallback {

    private Stage appWindow;
    private Scene scLogin;
    private Scene scMain;

    private ClientNetwork network;
    private ClientController clientController;
    private LoginController loginController;

    public AppController(Stage primaryStage) throws IOException {
        this.appWindow = primaryStage;
        init();
    }

    private void init() throws IOException {
        /**
         * Создаем клиенсткое подключение
         */
        this.network = new ClientNetwork();

        /**
         * Загружаем сцену с авторизацией
         */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        this.scLogin = new Scene(loader.load());
        this.appWindow.setTitle("Sign In");
        this.appWindow.setScene(scLogin);

        /**
         * Получаем контроллер и устанавливаем callback
         */
        this.loginController = loader.getController();
        this.loginController.init(this);

        this.appWindow.setOnCloseRequest(request -> {
            closeApp();
        });
    }

    private void closeApp() {
        try {
            this.network.close();
            if (this.clientController != null){
                this.clientController.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openMainWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        this.scMain = new Scene(loader.load());
        /**
         * Получаем контроллер и устанавливаем callback
         */
        this.clientController = loader.getController();
        this.clientController.init(this);
        this.appWindow.setScene(scMain);
    }

    public void show() {
        this.appWindow.show();
    }

    @Override
    public void openMainWindowCallback() {
        Platform.runLater(() -> {
            try {
                openMainWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void closeAppCallback() {
        Platform.runLater(() -> {
            closeApp();
        });
    }

    @Override
    public ClientNetwork getNetwork() {
        return network;
    }

    @Override
    public Window getStage() {
        return appWindow;
    }

}
