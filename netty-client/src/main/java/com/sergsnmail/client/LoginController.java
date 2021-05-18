package com.sergsnmail.client;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.network.NetworkListener;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.common.Method;
import com.sergsnmail.common.message.method.auth.AuthMethod;
import com.sergsnmail.common.message.method.auth.AuthParam;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import com.sergsnmail.common.message.method.auth.AuthResult;
import com.sergsnmail.common.message.method.registration.RegParam;
import com.sergsnmail.common.message.method.registration.RegResult;
import com.sergsnmail.common.message.method.registration.RegMethod;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, NetworkListener {

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;

    private AppCallback callable;
    private Network clientNetwork;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(AppCallback callable) {
        this.callable = callable;
        this.clientNetwork = callable.getNetwork();
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
                    callable.openMainWindowCallback();
                }
            }

            if (method instanceof RegMethod){
                RegResult regResult = (RegResult) method.getResult();
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
