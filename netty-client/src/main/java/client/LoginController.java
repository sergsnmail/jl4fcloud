package client;

import client.network.ClientNetwork;
import client.network.NetworkListener;
import message.*;
import message.common.Message;
import message.common.Method;
import message.method.auth.AuthMethod;
import message.method.auth.AuthParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import message.method.auth.AuthResult;
import message.method.registration.RegParam;
import message.method.registration.RegResult;
import message.method.registration.RegMethod;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, NetworkListener {

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;

    private InterfaceCallback callable;
    private ClientNetwork clientNetwork;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(InterfaceCallback callable) {
        this.callable = callable;
    }

    public void setNetwork(ClientNetworkCallable networkCallable) {
        this.clientNetwork = networkCallable.getNetwork();
        this.clientNetwork.addChannelListener(this);
    }

    @Override
    public void messageReceive(Message msg) {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            Method method = response.getMethod();

            if (method instanceof AuthMethod){
                AuthResult authResult = (AuthResult)method.getResult();
                if (authResult != null && authResult.isAuth()){
                    callable.openMainWindowCallback(authResult.getSession());
                }
            }

            if (method instanceof RegMethod){
                RegResult regResult = (RegResult) method.getResult();
                if (regResult != null && regResult.isAuth()) {
                    callable.openMainWindowCallback(regResult.getSession());
                }
            }
            /*Response msgAuthResp = (Response) msg;
            AuthResult authResult = msgAuthResp.getMethod().getResultImpl(AuthResult.class);
            AuthParam authParam = msgAuthResp.getMethod().getParamImpl(AuthParam.class);

            if ("/auth".equals(msgAuthResp.getMethod().getName())) {
                if (authResult != null && authResult.isAuth()) {
                    callable.openMainWindowCallback(new UserSession(authParam.getUsername()));
                }
            } else if ("/register".equals(msgAuthResp.getMethod().getName())) {
                RegResult regResult = msgAuthResp.getMethod().getResultImpl(RegResult.class);
                if (regResult != null && regResult.isAuth()) {
                    callable.openMainWindowCallback(new UserSession(authParam.getUsername()));
                }
            }*/
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
        AuthMethod auth = AuthMethod.builder()
                .setParameter(new AuthParam(username.getText(), password.getText()))
                .build();

        Request request = Request.builder()
                .setMethod(auth)
                .build();

        clientNetwork.sendCommand(request);
    }

    public void onRegisterButton(ActionEvent actionEvent) {
        RegMethod registerMethod = RegMethod.builder()
                .setParameter(new RegParam(username.getText(), password.getText()))
                .build();

        Request request = Request.builder()
                .setMethod(registerMethod)
                .build();

        clientNetwork.sendCommand(request);
    }

    public void keyListener(KeyEvent keyEvent) {
    }

}
