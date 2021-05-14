package com.sergsnmail.client;

import com.sergsnmail.client.network.transfer.TransferEvent;
import com.sergsnmail.client.network.transfer.TransferListener;
import com.sergsnmail.client.network.transfer.TransferMachine;
import com.sergsnmail.common.message.method.common.FileDbMetadata;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.transfer.DownloadTask;
import com.sergsnmail.common.transfer.UploadTask;
import com.sergsnmail.client.watcher.FileWatcher;
import com.sergsnmail.client.watcher.WatchRepo;
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
import com.sergsnmail.client.watcher.FileListener;
import com.sergsnmail.common.network.NetworkListener;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.time.Instant;

public class ClientController implements Initializable, NetworkListener, FileListener, TransferListener, WatchRepoListener {

    @FXML
    public ProgressBar downloadProgress;
    @FXML
    public Label downloadFileName;
    @FXML
    public Label downloadPackage;
    @FXML
    public ProgressBar uploadProgress;
    @FXML
    public Label uploadFileName;
    @FXML
    public Label uploadPackage;
    //@FXML
    //private TextArea fileList;

    private Network network;
    private TransferMachine transferMachine;
    private AppCallback appCallback;
    private FileWatcher fileWatcher;
    private Path localFolder;


    //private Map<Path, Set<Path>> watchingFiles = new HashMap<>();
    //private Set<Path> awaitingResponseFiles = new HashSet<>();
    private Set<Path> awaitingResponseFiles1 = Collections.synchronizedSet(new HashSet<>());
    //private Set<Path> awaitingTransferFiles = new HashSet<>();;
    private Set<FileDbMetadata> inStorageFiles = new HashSet<>();

    private WatchRepo watchRepo = new WatchRepo();

    //private Object monO= new Object();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        downloadProgress.setProgress(0.0);
    }

    public void init(AppCallback callable) {
        this.appCallback = callable;

        this.network = callable.getNetwork();
        this.network.addChannelListener(this);

        this.transferMachine = new TransferMachine(this.network);
        this.transferMachine.addUploadListener(this);
        this.transferMachine.addDownloadListener(this);
        this.transferMachine.start();
        Set<Path> ttt = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public void messageReceive(Message msg) {
        Platform.runLater(() -> processingMessage(msg));
    }

    /**
     * Обработка сообщений от сервера
     * @param msg
     */
    private void processingMessage(Message msg) {

        /**
         * Обработка запросов сервера
         */
        if (msg instanceof Request) {
            // code here
        }

        /**
         * Обработка ответов сервера
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

    /**
     * Обработчик ответов метода GetFilesMethod
     * @param response
     * @param method
     */
    private void getFileResponseHandler(Response response, GetFilesMethod method) {
        GetFilesResult result = method.getResult();
        if (result != null && result.getFiles() != null && result.getFiles().size() > 0) {
            //this.fileList.clear();
            for (FileDbMetadata fileDbMetadata : result.getDbmetadata()) {
                inStorageFiles.add(fileDbMetadata);
//                //this.fileList.appendText(fileDbMetadata.getLocation() +
//                        File.separator +
//                        fileDbMetadata.getFileName() + "\n");
            }
        }

        downloadFileFromServer();
    }

    private void downloadFileFromServer() {
        for (FileDbMetadata inStorageFile : inStorageFiles) {
            DownloadTask task = new DownloadTask();
            task.setFileDbMetadata(inStorageFile);
            task.setUserFolder(localFolder.toString());
            transferMachine.addDownloadTask(task);
        }
    }

    /**
     * Обработчик ответов метода GetFileInfo
     * @param response
     * @param method
     */
    private void getFileInfoHandler(Response response, GetFileInfo method) {
        FileInfoResult result = method.getResult();
        FileInfoParam param = method.getParameter();
        Path localFilePath = getLocalPath(Paths.get(param.getMetadata().getFileRelativePath() + File.separator + param.getMetadata().getFileName()));

        if (result == null) {
            sendFile(localFilePath);
        } else if (result != null && awaitingResponseFiles1.contains(localFilePath)) {
            try {
                FileMetadata localMetadata = createMetadata(localFilePath);
                FileMetadata serverMetadata = result.getMetadata();
                if (serverMetadata != null && localMetadata != null) {
                    Instant localModified = Instant.parse(localMetadata.getModified_at());
                    Instant serverModified = Instant.parse(serverMetadata.getModified_at());
                    if (localModified.isAfter(serverModified)) {
                        sendFile(localFilePath);
                    }
                }
            }catch (Exception e){}
        }
        removeAwaitingResponse(localFilePath);
    }

    private Path getLocalPath(Path path) {
        return Paths.get(localFolder + File.separator + path);
    }

    private Path getRelativePath(Path path){
        return Paths.get(path.getParent().toString().replace(localFolder.toString(), ""));
    }

    /**
     * Обработчик нажатия кнопки в диалоге
     * @param actionEvent
     */
    public void OnPressedAction(ActionEvent actionEvent) {
        try {
            if (localFolder == null) {
                localFolder = getLocalFolder();
            }
            if (localFolder != null && fileWatcher == null) {
                fileWatcher = new FileWatcher();
                fileWatcher.register(localFolder);
                fileWatcher.addListener(this);
                fileWatcher.start();

                watchRepo = new WatchRepo();
                watchRepo.addListener(this);
                watchRepo.start();
            }

            Request getFilesRequest = Request.builder()
                    .setMethod(GetFilesMethod.builder().build())
                    .build();

            network.sendCommand(getFilesRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Запуск диалога выбора директории для синхронизации
     * @return
     */
    private Path getLocalFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folder to sync file");
        File file = directoryChooser.showDialog(appCallback.getStage());
        if (file != null) {
            return file.toPath();
        }
        throw new IllegalArgumentException("Folder to sync file not defined");
    }

    public void close() {
        //System.out.println("Start transfer machine shutdown");
        if (transferMachine != null){
            transferMachine.shutdown();
        }
        if (watchRepo != null)
            watchRepo.shutdown();
    }

    /**
     * Обработка событий от FileWatcher
     * @param path
     */
    @Override
    public void createEvent(Path path) {
        //System.out.println("[createEvent]" + path);
        watchRepo.add(path);
    }

    @Override
    public void deleteEvent(Path path) {
        //System.out.println("[deleteEvent]" + path);
        try {
            watchRepo.remove(path);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifyEvent(Path path) {
        //System.out.println("[modifyEvent]" + path);
        watchRepo.add(path);
    }


    /**
     * Отправка запроса на сервер по файлу
     * @param path
     */
    private void getFileInfoFromServer(Path path) {
        if (!transferMachine.isLocked(Paths.get(getRelativePath(path) + File.separator + path.getFileName())) && !awaitingResponseFiles1.contains(path)) {
            System.out.println("[DEBUG] get info from server: " + path);
            addAwaitingResp(path);

            FileInfoParam fileInfoParam = new FileInfoParam();

            try {
                fileInfoParam.setMetadata(createMetadata(path));
                Request getFilesStateRequest = Request.builder()
                        .setMethod(GetFileInfo.builder()
                                .setParameter(fileInfoParam)
                                .build())
                        .build();
                network.sendCommand(getFilesStateRequest);
            } catch (Exception e){
                removeAwaitingResponse(path);
            }
        }
    }

    private void removeAwaitingResponse(Path localFilePath) {
        //synchronized (monO) {
            System.out.printf("remove awaiting for responce %s\n ",localFilePath);
            //awaitingResponseFiles.remove(localFilePath);
            awaitingResponseFiles1.remove(localFilePath);
        //}
    }

    private void addAwaitingResp(Path path) {
        //synchronized (monO) {
            System.out.printf("Add awaiting for responce %s\n ",path);
            //awaitingResponseFiles.add(path);
            awaitingResponseFiles1.add(path);
        //}
    }

    /**
     * Регистрация файла для передачи в TransferMachine
     * @param file
     */
    public void sendFile(Path file) {
        try {
            System.out.println("[DEBUG] to transfer: " + file);
            transferMachine.addUploadTask(new UploadTask(file, createMetadata(file)));
        }catch (Exception e){
            removeAwaitingResponse(file);
        }
    }

    private FileMetadata createMetadata(Path file) throws IOException {
        FileMetadata metadata;
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
            metadata = new FileMetadata();
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            metadata.setCreated_at(attr.creationTime().toString());
            metadata.setModified_at(attr.lastModifiedTime().toString());
            metadata.setFileName(file.getFileName().toString());
            metadata.setFilePath(file.toString());
            metadata.setFileRelativePath(localPath);
            metadata.setSize(Files.size(file));
        return metadata;
    }

    @Override
    public void onTransfer(TransferEvent event) {
        Platform.runLater(() -> {
            transferHandle(event);
        });
    }

    /**
     * Отрисовка в нтерфейсе процесса передачи
     * @param event
     */
    private void transferHandle(TransferEvent event) {
        if ("UPLOAD".equals(event.getType())) {
            uploadFileName.setText(event.getFileName());
            uploadPackage.setText(event.getCurrentNumber() + " of " + event.getTotalNumber());
            double currNum = event.getCurrentNumber();
            double totalNum = event.getTotalNumber();
            double currProgress = currNum / totalNum;
            uploadProgress.setProgress(currProgress);
        }

        if ("DOWNLOAD".equals(event.getType())){
            downloadFileName.setText(event.getFileName());
            downloadPackage.setText(event.getCurrentNumber() + " of " + event.getTotalNumber());
            double currNum = event.getCurrentNumber();
            double totalNum = event.getTotalNumber();
            double currProgress = currNum / totalNum;
            downloadProgress.setProgress(currProgress);
        }

    }

    @Override
    public void onWatchRepoEvent(Path file) {
        getFileInfoFromServer(file);
    }
}
