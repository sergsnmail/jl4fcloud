package com.sergsnmail.client;

import com.sergsnmail.client.transfer.v1.TransferEvent;
import com.sergsnmail.client.transfer.v1.TransferListener;
import com.sergsnmail.client.transfer.v1.TransferMachine;
import com.sergsnmail.client.transfer.v1.TransferTask;
import com.sergsnmail.client.watcher.FileWatcher;
import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoParam;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoResult;
import com.sergsnmail.common.message.method.getfileinfo.GetFileInfo;
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
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.time.Instant;

public class ClientController implements Initializable, NetworkListener, NotifyCallback, FileListener, TransferListener {

    @FXML
    public ProgressBar transferProgress;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Label legendLabel;
    @FXML
    private TextArea fileList;

    private ClientNetwork network;
//    private TransferFileManager transferFileManager;
    private TransferMachine transferMachine;
    private AppCallback appCallback;
    private FileWatcher fileWatcher;
    private Path localFolder;

    //private List<String> files = new ArrayList<>();
    private Set<Path> files = new HashSet<>();
    private Set<Path> filesFromServer = new HashSet<>();


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
        this.network = callable.getNetwork();
        this.network.addChannelListener(this);
        //transferFileManager = new TransferFileManager(this.clientNetwork);
        //transferFileManager.setNotifyCallback(this);

        transferMachine = new TransferMachine(this.network);
        transferMachine.addListener(this);
        transferMachine.start();
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

    private void processingMessage(Message msg) {

        /**
         * Обработка запросов с сервера
         */
        if (msg instanceof Request) {
            // обработка запроса
        }

        /**
         * Обработка ответов с сервера
         */
        if (msg instanceof Response) {
            Response response = (Response) msg;
            if (response.getMethod() instanceof GetFilesMethod) {
                getFileResponseHandler(response, (GetFilesMethod) response.getMethod());
            }
            if (response.getMethod() instanceof GetFileInfo){
                getFileInfoHandler(response, (GetFileInfo) response.getMethod());
            }
        }
    }

    private void getFileResponseHandler(Response response, GetFilesMethod method) {
        GetFilesResult result = method.getResult();
        if (result != null && result.getFiles() != null && result.getFiles().size() > 0) {
            this.fileList.clear();
            for (String file : result.getFiles()) {
                System.out.println(Paths.get(file));
                filesFromServer.add(Paths.get(file));
                this.fileList.appendText(file + "\n");
            }
        }
    }

    private void getFileInfoHandler(Response response, GetFileInfo method) {
        FileInfoResult result = method.getResult();
        FileInfoParam param = method.getParameter();
        Path localFilePath = getLocalPath(Paths.get(param.getMetadata().getFilePath() + param.getMetadata().getFileName()));

        if (result == null) {
            sendFile(localFilePath);
        } else if (result != null && files.contains(localFilePath)) {
            FileMetadata localMetadata = createMetadata(localFilePath);
            FileMetadata serverMetadata = result.getMetadata();
            if (serverMetadata != null && localMetadata != null) {
                Instant localModified = Instant.parse(localMetadata.getModified_at());
                Instant serverModified = Instant.parse(serverMetadata.getModified_at());
                if (localModified.isAfter(serverModified)){
                    sendFile(localFilePath);
                }
            }
        }
        files.remove(localFilePath);
    }

    private Path getLocalPath(Path path) {
        return Paths.get(localFolder + File.separator + path);
    }

    private Path getRelativePath(Path path){
        return Paths.get(path.getParent().toString().replace(localFolder.toString(), ""));
    }

    public void OnPressedAction(ActionEvent actionEvent) {
        try {
            if (localFolder == null) {
                localFolder = getLocalFolder();
            }
            if (fileWatcher == null) {
                fileWatcher = new FileWatcher();
                fileWatcher.register(localFolder);
                fileWatcher.addListener(this);
                fileWatcher.start();
                fileWatcher.registerAll(localFolder);
                //registerDirAndSubDirs(localFolder);
            }

            Request getFilesRequest = Request.builder()
                    .setMethod(GetFilesMethod.builder().build())
                    .build();

            network.sendCommand(getFilesRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (notifyObj instanceof TransferFileManager.TransferNotifyObject) {
            TransferFileManager.TransferNotifyObject nObj = (TransferFileManager.TransferNotifyObject) notifyObj;
            fileNameLabel.setText(nObj.getFileName());
            legendLabel.setText(nObj.getCurrentNumber() + " of " + nObj.getTotalNumber());
            double currNum = nObj.getCurrentNumber();
            double totalNum = nObj.getTotalNumber();
            double currProgress = currNum / totalNum;
            transferProgress.setProgress(currProgress);
        }
    }

    public void close() {
        System.out.println("Start transfer machine shutdown");
        fileWatcher.printWatchable();
//        if (transferFileManager != null) {
//            transferFileManager.transferShutdown();
//        }
        if (transferMachine != null){
            transferMachine.shutdown();
        }
    }

    @Override
    public void createEvent(Path path) {
        System.out.println("[createEvent]" + path);
        System.out.println(localFolder);
        System.out.println(localFolder.relativize(path));

        getFileInfoFromServer(path);
        //if(filesFromServer.contains())
        //files.add(path);

        //sendFile(path);
    }

    private void getFileInfoFromServer(Path path) {
        files.add(path);
        FileInfoParam fileInfoParam = new FileInfoParam();

        fileInfoParam.setMetadata(createMetadata(path));
        Request getFilesStateRequest = Request.builder()
                .setMethod(GetFileInfo.builder()
                        .setParameter(fileInfoParam)
                        .build())
                .build();
        network.sendCommand(getFilesStateRequest);
    }

    private void registerDirAndSubDirs(Path dir){
        try {
            fileWatcher.register(dir);
            //System.out.println("Add watching dir: " + dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*List<File> subDirs = new ArrayList<>();
        collectSubFolder(dir.toFile(), subDirs);
        for (File subDir : subDirs) {
            try {
                fileWatcher.registerDir(subDir.toPath());
                //System.out.println("Add watching dir: " + subDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
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
        System.out.println("[deleteEvent]" + path);
    }

    @Override
    public void modifyEvent(Path path) {
        System.out.println("[modifyEvent]" + path);

        if (files.contains("\\" + localFolder.relativize(path))){
            // файл уже отправлен в этой сесии,
            // получить информацию на сервере по данному файлу
        } else {
            files.add(path);
            //sendFile(path);
        }
    }

    public void sendFile(Path file) {
        transferMachine.addFile(new TransferTask(file,createMetadata(file)));
    }

    private FileMetadata createMetadata(Path file){
        FileMetadata metadata = null;
        try {
            /**
             * Определяем путь относительно корневой директории
             * Пример:
             *  Полный путь файла - C:\temp\folder\example.txt
             *  Корневая диерктория - C:\temp
             *  Путь относительно корневой директории - \folder
             */
            //String localPath = file.getParent().toString().replace(localFolder.toString(), "");
            String localPath = getRelativePath(file).toString();

            /**
             * Собираем метаданыне файла
             */
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            metadata = new FileMetadata();
            metadata.setFileName(file.getFileName().toString());
            metadata.setCreated_at(attr.creationTime().toString());
            metadata.setModified_at(attr.lastModifiedTime().toString());
            metadata.setFilePath(localPath);
            metadata.setSize(Files.size(file));
        } catch (Exception e){

        }
        return metadata;
    }

    @Override
    public void onTransfer(TransferEvent event) {
        Platform.runLater(() -> {
            transferHandle(event);
        });
    }

    private void transferHandle(TransferEvent event) {
        fileNameLabel.setText(event.getFileName());
        legendLabel.setText(event.getCurrentNumber() + " of " + event.getTotalNumber());
        double currNum = event.getCurrentNumber();
        double totalNum = event.getTotalNumber();
        double currProgress = currNum / totalNum;
        transferProgress.setProgress(currProgress);
    }

}
