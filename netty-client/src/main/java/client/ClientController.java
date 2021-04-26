package client;

import client.network.ClientNetwork;
import client.network.Listeners;
import message.JSONConverter;
import message.Request;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import message.common.Message;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable, Listeners {

    private ClientNetwork clientNetwork;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setNetwork(ClientNetworkCallable networkCallable) {
        this.clientNetwork = networkCallable.getNetwork();
        this.clientNetwork.addChannelListener(this);
    }

    @Override
    public void messageReceive(Message msg) {
        Platform.runLater(() -> processingMessage(msg));
    }

    private void processingMessage(Message msg){
        /*Request req = (Request) JSONConverter.Json2Object(msg, Message.class);
        System.out.println(req.toString());*/
    }

    public void OnPressedAction(ActionEvent actionEvent) {
        /*Ping body = new Ping();
        Request<Ping> pingRequest = new Request<>(body);
        this.clientNetwork.sendCommand(pingRequest);*/
    }
}
