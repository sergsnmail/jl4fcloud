package com.sergsnmail.client;

import com.sergsnmail.client.transfer.TransferEvent;
import com.sergsnmail.client.transfer.TransferListener;
import com.sergsnmail.client.transfer.TransferMachine;
import com.sergsnmail.client.transfer.TransferTask;
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

public class ClientController implements Initializable, NetworkListener, FileListener, TransferListener {

    @FXML
    public ProgressBar transferProgress;
    @FXML
    public Label fileNameLabel;
    @FXML
    public Label legendLabel;
    @FXML
    private TextArea fileList;

    private ClientNetwork network;
    private TransferMachine transferMachine;
    private AppCallback appCallback;
    private FileWatcher fileWatcher;
    private Path localFolder;

    private Set<Path> awaitingResponseFiles = new HashSet<>();
    private Set<Path> awaitingTransferFiles = new HashSet<>();;
    private Set<Path> inStorageFiles = new HashSet<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        transferProgress.setProgress(0.0);
    }

    public void init(AppCallback callable) {
        this.appCallback = callable;

        this.network = callable.getNetwork();
        this.network.addChannelListener(this);

        this.transferMachine = new TransferMachine(this.network);
        this.transferMachine.addListener(this);
        this.transferMachine.start();
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
            this.fileList.clear();
            for (String file : result.getFiles()) {
                System.out.println(Paths.get(file));
                inStorageFiles.add(Paths.get(file));
                this.fileList.appendText(file + "\n");
            }
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
        } else if (result != null && awaitingResponseFiles.contains(localFilePath)) {
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
        awaitingResponseFiles.remove(localFilePath);
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
                fileWatcher.registerAll(localFolder);
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
    }

    /**
     * Обработка событий от FileWatcher
     * @param path
     */
    @Override
    public void createEvent(Path path) {
        //System.out.println("[createEvent]" + path);
        getFileInfoFromServer(path);
    }

    @Override
    public void deleteEvent(Path path) {
        System.out.println("[deleteEvent]" + path);
    }

    @Override
    public void modifyEvent(Path path) {
        //System.out.println("[modifyEvent]" + path);
        getFileInfoFromServer(path);
    }


    /**
     * Отправка запроса на сервер по файлу
     * @param path
     */
    private void getFileInfoFromServer(Path path) {
        if (!awaitingResponseFiles.contains(path)) {
            awaitingResponseFiles.add(path);
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
                awaitingResponseFiles.remove(path);
            }
        }
    }

    /**
     * Регистрация файла для передачи в TransferMachine
     * @param file
     */
    public void sendFile(Path file) {
        try {
            transferMachine.addFile(new TransferTask(file, createMetadata(file)));
        }catch (Exception e){
            awaitingResponseFiles.remove(file);
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
        fileNameLabel.setText(event.getFileName());
        legendLabel.setText(event.getCurrentNumber() + " of " + event.getTotalNumber());
        double currNum = event.getCurrentNumber();
        double totalNum = event.getTotalNumber();
        double currProgress = currNum / totalNum;
        transferProgress.setProgress(currProgress);
    }

}
