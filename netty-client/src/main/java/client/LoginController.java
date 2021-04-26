package client;

import client.network.ClientNetwork;
import client.network.Listeners;
import message.*;
import message.common.Message;
import message.method.auth.AuthParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import message.method.auth.AuthResult;
import message.method.reg.RegParam;
import message.method.reg.RegResult;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, Listeners {

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;

    private Callable callable;
    private ClientNetwork clientNetwork;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(Callable callable) {
        this.callable = callable;
    }

    public void setNetwork(ClientNetworkCallable networkCallable) {
        this.clientNetwork = networkCallable.getNetwork();
        this.clientNetwork.addChannelListener(this);
    }

    @Override
    public void messageReceive(Message msg) {
        if (msg instanceof Response) {
            Response msgAuthResp = (Response) msg;
            if ("/auth".equals(msgAuthResp.getMethod().getName())) {
                AuthResult authResult = msgAuthResp.getMethod().getResultImpl(AuthResult.class);
                if (authResult != null && authResult.isAuth()) {
                    callable.openMainWindowCallback();
                }

            } else if ("/register".equals(msgAuthResp.getMethod().getName())) {
                RegResult regResult = msgAuthResp.getMethod().getResultImpl(RegResult.class);
                if (regResult != null && regResult.isAuth()) {
                    callable.openMainWindowCallback();
                }
            }
        }
    }

    /**
     * Interface actions
     */
    public void onCloseButton(ActionEvent actionEvent) throws InterruptedException {
        clientNetwork.close();
        callable.closeAppCallback();
    }

    public void onLoginButton(ActionEvent actionEvent) {
        Request authRequest = Request.builder()
                .setMethod(new Method("/auth"))
                .addParameter(new AuthParam(username.getText(), password.getText()))
                .build();

        clientNetwork.sendCommand(authRequest);
    }

    public void onRegisterButton(ActionEvent actionEvent) {
        Request authRequest = Request.builder()
                .setMethod(new Method("/register"))
                .addParameter(new RegParam(username.getText(), password.getText()))
                .build();
        clientNetwork.sendCommand(authRequest);
    }

    public void keyListener(KeyEvent keyEvent) {
    }

}
