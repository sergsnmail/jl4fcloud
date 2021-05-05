package client;

import client.filemanager.TransferFileManager;
import client.network.ClientNetwork;
import client.network.NetworkListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import message.method.putfile.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable, NetworkListener, NotifyCallback {

    @FXML
    public ProgressBar transferProgress;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Label legendLabel;
    @FXML
    private TextArea fileList;

    private ClientNetwork clientNetwork;
    private UserSession session;

    TransferFileManager transferFileManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        transferProgress.setProgress(0.0);
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

        String path = "e:\\temp\\watch\\";
        String fileName1 = "VID_20200927_160322.mp4";
        String fileName2 = "Старикам тут не место.2007.BDRip.1080p.Rus.mkv";
        String fileName3 = "My_speech.txt";

        transferFileManager = new TransferFileManager(this.clientNetwork, this.session);
        transferFileManager.setNotifyCallback(this);

        List<String> filesForTransfer = new ArrayList<>();
        filesForTransfer.add(path + fileName1);
        filesForTransfer.add(path + fileName2);
        filesForTransfer.add(path + fileName3);

        for (String file : filesForTransfer) {
            Path filePath = Paths.get(file);
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(filePath.getFileName().toString());
            metadata.setFilePath(filePath.getParent().toString());
            transferFileManager.transferFile(filePath, metadata);
        }
    }

//    private static String encodeFileToBase64(File file) {
//        try {
//            byte[] fileContent = Files.readAllBytes(file.toPath());
//            return Base64.getEncoder().encodeToString(fileContent);
//        } catch (IOException e) {
//            throw new IllegalStateException("could not read file " + file, e);
//        }
//    }
//
//    private void decodeBase64ToFile(String filePathName, String fileEncodedContent) {
//        try {
//            byte[] fileContent = Base64.getDecoder().decode(fileEncodedContent);
//            try (FileOutputStream fos = new FileOutputStream(filePathName)) {
//                fos.write(fileContent);
//            }
//        } catch (IOException e) {
//            throw new IllegalStateException("could not create file " + filePathName, e);
//        }
//    }

    @Override
    public void notify(Object notifyObj) {
        Platform.runLater(() -> {
            handleNotify(notifyObj);
        });
    }

    private void handleNotify(Object notifyObj) {
        if (notifyObj instanceof TransferFileManager.TransferNotifyObject){
            TransferFileManager.TransferNotifyObject nObj = (TransferFileManager.TransferNotifyObject) notifyObj;
            fileNameLabel.setText(nObj.getFileName());
            legendLabel.setText(nObj.getCurrentNumber() + " of "+ nObj.getTotalNumber());
            double currNum = nObj.getCurrentNumber();
            double totalNum = nObj.getTotalNumber();
            double currProgress = currNum/totalNum;
            //System.out.printf("%f div %f = %f%n", currNum, totalNum, currProgress);
            transferProgress.setProgress(currProgress);
        }
    }

    public void close() {
        System.out.println("Start transfer shutdown");
        if (transferFileManager != null){
            transferFileManager.transferShutdown();
        }
    }
}
