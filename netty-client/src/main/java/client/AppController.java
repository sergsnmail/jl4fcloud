package client;

import client.network.ClientNetwork;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import message.common.UserSession;

import java.io.IOException;

public class AppController implements InterfaceCallback, ClientNetworkCallable {

    private Stage appWindow;
    private Scene scLogin;
    private Scene scMain;

    private ClientNetwork clientNetwork;
    private ClientController clController;

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
            if (clController != null){
                clController.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openMainWindow(UserSession session) throws IOException {
        //Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        scMain = new Scene(loader.load());
        /**
         * Получаем контроллер и устанавливаем callback
         */
        clController = loader.getController();
        clController.setNetwork(this).setSession(session).init();
        this.appWindow.setScene(scMain);
    }

    public void show() {
        this.appWindow.show();
    }

    @Override
    public void openMainWindowCallback(UserSession session) {
        Platform.runLater(() -> {
            try {
                openMainWindow(session);
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
