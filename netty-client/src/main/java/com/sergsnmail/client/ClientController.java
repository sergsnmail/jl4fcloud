package com.sergsnmail.client;

import com.sergsnmail.client.watcher.FileWatcher;
import com.sergsnmail.common.message.method.putfile.FileMetadata;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;

import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.method.getfile.GetFilesMethod;
import com.sergsnmail.common.message.method.getfile.GetFilesResult;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.client.transfer.TransferFileManager;
import com.sergsnmail.client.watcher.FileListener;
import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.client.network.NetworkListener;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable, NetworkListener, NotifyCallback, FileListener {

    @FXML
    public ProgressBar transferProgress;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Label legendLabel;
    @FXML
    private TextArea fileList;

    private ClientNetwork clientNetwork;
    private TransferFileManager transferFileManager;
    private AppCallback appCallback;
    private FileWatcher fileWatcher;
    //private UserSession session;

    //private String DEFAULT_LOCAL_FOLDER = "e:\\temp\\watch\\";
    private Path localFolder;

    private List<String> files = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        transferProgress.setProgress(0.0);
    }

    /*public ClientController setNetwork(ClientNetworkCallable networkCallable) {
        this.clientNetwork = networkCallable.getNetwork();
        this.clientNetwork.addChannelListener(this);
        return this;
    }*/

    /*public ClientController setSession(UserSession session) {
        this.session = session;
        return this;
    }*/

    /*public void init() {
        //startWatching();
        Request getFilesRequest = Request.builder()
                .setMethod(GetFilesMethod.builder().build())
                .setSession(this.session)
                .build();

        clientNetwork.sendCommand(getFilesRequest);
    }*/

    public void init(AppCallback callable) {
        this.appCallback = callable;
        this.clientNetwork = callable.getNetwork();
        this.clientNetwork.addChannelListener(this);
        transferFileManager = new TransferFileManager(this.clientNetwork);
        transferFileManager.setNotifyCallback(this);
        //fileWatcher = new FileWatcher();
    }

    public void startWatching() {
        /*try {
            FileWatcher fileWatcher = new FileWatcher();
            fileWatcher.registerDir(Paths.get(DEFAULT_WATCH_PATH));
            fileWatcher.addListener(this);
            fileWatcher.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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

        try {
            if (localFolder == null) {
                localFolder = getLocalFolder();
            }
            if (fileWatcher == null) {
                fileWatcher = new FileWatcher();
                fileWatcher.registerDir(localFolder);
                fileWatcher.addListener(this);
                fileWatcher.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*else {

            String fileName1 = "VID_20200927_160322.mp4";
            String fileName2 = "Старикам тут не место.2007.BDRip.1080p.Rus.mkv";
            String fileName3 = "My_speech.txt";

            transferFileManager = new TransferFileManager(this.clientNetwork);
            transferFileManager.setNotifyCallback(this);

            List<String> filesForTransfer = new ArrayList<>();
            filesForTransfer.add(localFolder + File.separator + fileName1);
            filesForTransfer.add(localFolder + File.separator + fileName2);
            filesForTransfer.add(localFolder + File.separator + fileName3);

            for (String file : filesForTransfer) {
                Path filePath = Paths.get(file);
                FileMetadata metadata = new FileMetadata();
                metadata.setFileName(filePath.getFileName().toString());
                metadata.setFilePath(filePath.getParent().toString());
                transferFileManager.transferFile(filePath, metadata);
            }
        }*/
    }

    private Path getLocalFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folder to sync file");
        File file = directoryChooser.showDialog(appCallback.getStage());
        if (file != null) {
            return file.toPath();
        }
        throw new IllegalArgumentException("Folder to sync file not defined");
    }


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
            transferProgress.setProgress(currProgress);
        }
    }

    public void close() {
        System.out.println("Start transfer shutdown");
        if (transferFileManager != null){
            transferFileManager.transferShutdown();
        }
    }

    @Override
    public void createEvent(Path path) {
        if (Files.isDirectory(path)){
            try {
                List<File> subfolder = new ArrayList<>();
                collectSubFolder(path.toFile(), subfolder);
                for (File path1 : subfolder) {
                    fileWatcher.registerDir(path1.toPath());
                    System.out.println("Add watching dir: " + path);
                }
                fileWatcher.registerDir(path);

                System.out.println("Add watching dir: " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendFile(path);
        }
    }

    private void collectSubFolder(File dir, List<File> dirList) {
        List<File> filesInDir = new ArrayList<>(Arrays.asList(dir.listFiles()));
        for (File file : filesInDir) {
            if (file.isDirectory()) {
                dirList.add(file);
                collectSubFolder(file, dirList);
            }
        }
    }

    @Override
    public void deleteEvent(Path path) {
    }

    @Override
    public void modifyEvent(Path path) {
    }

    public void sendFile(Path filePath){
        try {
            /**
             * Определяем путь относительно корневой директории
             * Пример:
             *  Полный путь файла - C:\temp\folder\example.txt
             *  Корневая диерктория - C:\temp
             *  Путь относительно корневой директории - \folder
             */
            String localPath = filePath.getParent().toString().replace(localFolder.toString(), "");

            /**
             * Собираем метаданыне файла
             */
            BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(filePath.getFileName().toString());
            metadata.setCreated_at(attr.creationTime().toString());
            metadata.setModified_at(attr.lastModifiedTime().toString());
            metadata.setFilePath(localPath);
            metadata.setSize(Files.size(filePath));
            transferFileManager.transferFile(filePath, metadata);
        }catch (Exception e){
        }

    }
    /*else {

            String fileName1 = "VID_20200927_160322.mp4";
            String fileName2 = "Старикам тут не место.2007.BDRip.1080p.Rus.mkv";
            String fileName3 = "My_speech.txt";

            transferFileManager = new TransferFileManager(this.clientNetwork);
            transferFileManager.setNotifyCallback(this);

            List<String> filesForTransfer = new ArrayList<>();
            filesForTransfer.add(localFolder + File.separator + fileName1);
            filesForTransfer.add(localFolder + File.separator + fileName2);
            filesForTransfer.add(localFolder + File.separator + fileName3);

            for (String file : filesForTransfer) {
                Path filePath = Paths.get(file);
                FileMetadata metadata = new FileMetadata();
                metadata.setFileName(filePath.getFileName().toString());
                metadata.setFilePath(filePath.getParent().toString());
                transferFileManager.transferFile(filePath, metadata);
            }
        }*/
}
