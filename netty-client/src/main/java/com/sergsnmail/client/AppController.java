package com.sergsnmail.client;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.common.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;

public class AppController implements AppCallback {

    private Stage appWindow;
    private Scene scLogin;
    private Scene scMain;

    private Network network;
    private ClientController clientController;
    private LoginController loginController;

    public AppController(Stage primaryStage, int port, String host) throws IOException {
        this.appWindow = primaryStage;
        init(port, host);
    }

    private void init(int port, String host) throws IOException {

        if (port < 0 && host.isEmpty()){
            throw new IllegalArgumentException("Wrong connection parameters");
        }
        /**
         * Создаем клиенсткое подключение
         */
        this.network = new ClientNetwork(port, host);

        /**
         * Загружаем сцену с авторизацией
         */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        this.scLogin = new Scene(loader.load());
        this.appWindow.setTitle("Sign In");
        this.appWindow.setResizable(false);
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

    /**
     * Обработчик закрытия приложения
     */
    private void closeApp() {
        this.network.close();
        if (this.clientController != null){
            this.clientController.close();
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
        this.appWindow.setTitle("Client");
        this.appWindow.setResizable(false);
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
    public Network getNetwork() {
        return network;
    }

    @Override
    public Window getStage() {
        return appWindow;
    }
}
