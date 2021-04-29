package client;

import client.network.ClientNetwork;
import client.network.Listeners;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import message.Response;
import message.Request;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import message.common.Message;
import message.common.UserSession;
import message.method.getuserfile.GetFilesMethod;
import message.method.getuserfile.GetFilesResult;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ResourceBundle;

public class ClientController implements Initializable, Listeners {

    @FXML
    private TextArea fileList;
    private ClientNetwork clientNetwork;
    private UserSession session;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public ClientController setNetwork(ClientNetworkCallable networkCallable) {
        this.clientNetwork = networkCallable.getNetwork();
        this.clientNetwork.addChannelListener(this);
        return this;
    }

    public ClientController setSession(UserSession session) {
        this.session = session;
        return this;
    }

    public void init() {
        startWatching();
    }


    public void startWatching(){
        Request getFilesRequest = Request.builder()
                .setMethod(GetFilesMethod.builder().build())
                .setSession(this.session)
                .build();

        clientNetwork.sendCommand(getFilesRequest);
    }

    @Override
    public void messageReceive(Message msg) {
        Platform.runLater(() -> processingMessage(msg));
    }

    private void processingMessage(Message msg){

        if(msg instanceof Request){
            // обработка запроса
        }

        if(msg instanceof Response){
            // обработка ответа
            Response response = (Response) msg;
            if (response.getMethod() instanceof GetFilesMethod){
                getFileResponseHandler(response, (GetFilesMethod) response.getMethod());
            }
        }
    }

    private void getFileResponseHandler(Response response, GetFilesMethod method) {
        GetFilesResult result = method.getResult();
        if (result != null && result.getFiles() != null && result.getFiles().size() > 0){
            this.fileList.clear();
            for (String file : result.getFiles()) {
                this.fileList.appendText(file + "\n");
            }
        }
    }

    public void OnPressedAction(ActionEvent actionEvent) {
        String fileBody = encodeFileToBase64(new File("e:\\temp\\033\\codecs.txt"));
        PutFilesParam putParam = new PutFilesParam();
        putParam.setBody(fileBody);
        putParam.setFilename("codecs.txt");
        putParam.setPath("e:\\temp\\033\\");
        PutFilesMethod  putMethod = PutFilesMethod.builder()
                .setParameter(putParam)
                .build();
        Request putFileRequest = Request.builder()
                .setSession(this.session)
                .setMethod(putMethod)
                .build();
        clientNetwork.sendCommand(putFileRequest);
    }

    private static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }
}
