package client;

import client.network.ClientNetwork;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppController implements Callable, ClientNetworkCallable {

    private Stage appWindow;
    private Scene scLogin;
    private Scene scMain;

    private ClientNetwork clientNetwork;

    public AppController(Stage primaryStage) throws IOException {
        this.appWindow = primaryStage;
        init();
    }

    private void init() throws IOException {
        /**
         * Создаем клиенсткое подключение
         */
        this.clientNetwork = new ClientNetwork();

        /**
         * Загружаем сцену с авторизацией
         */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        scLogin = new Scene((Parent) loader.load());
        //scLogin = new Scene(loader.load());
        this.appWindow.setTitle("Sign In");
        this.appWindow.setScene(scLogin);

        /**
         * Получаем контроллер и устанавливаем callback
         */
        LoginController controller = loader.getController();
        controller.init(this);
        controller.setNetwork(this);

        this.appWindow.setOnCloseRequest(request -> {
            closeApp();
        });
    }

    private void closeApp() {
        try {
            clientNetwork.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openMainWindow() throws IOException {
        //Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        scMain = new Scene(loader.load());
        /**
         * Получаем контроллер и устанавливаем callback
         */
        ClientController clController = loader.getController();
        clController.setNetwork(this);

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
        return clientNetwork;
    }
}
