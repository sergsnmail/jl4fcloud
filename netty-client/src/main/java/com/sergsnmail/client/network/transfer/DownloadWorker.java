package com.sergsnmail.client.network.transfer;

import com.sergsnmail.common.message.method.common.FileDbMetadata;
import com.sergsnmail.common.message.method.common.Metadata;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.network.NetworkListener;
import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.method.transferfile.*;
import com.sergsnmail.common.transfer.DownloadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class DownloadWorker implements Runnable, NetworkListener {

    private final Network network;
    private final ConcurrentLinkedDeque<DownloadTask> downloadTasks;
    private final List<TransferListener> downloadListeners;
    private final Object downloadMon;
    private final ConcurrentLinkedDeque<String> lockedFiles;
    //private volatile boolean downloadComplete = false;
    private AtomicBoolean downloadBegin = new AtomicBoolean(false);
    private volatile AtomicBoolean dataReceived = new AtomicBoolean(false);
    private String currentFileRelativePath;
    private String currentFileName;
    private String currentBody;
    private String currentFileAbsolutePath;
    private int currPkgNumber;
    private int totalPkgNumber;

    public DownloadWorker(Network network, ConcurrentLinkedDeque<DownloadTask> downloadTasks, List<TransferListener> downloadListeners, Object downloadMon, ConcurrentLinkedDeque<String> lockedFiles) {
        this.network = network;
        this.downloadTasks = downloadTasks;
        this.downloadListeners = downloadListeners;
        this.downloadMon = downloadMon;
        this.lockedFiles = lockedFiles;
        this.network.addChannelListener(this);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                while (downloadTasks.size() == 0 && !Thread.currentThread().isInterrupted()) {
                    /**
                     * если заданий нет, то поток спит
                     */
                    synchronized (downloadMon) {
                        downloadMon.wait();
                    }
                }
                DownloadTask task;
                if ((task = downloadTasks.poll()) != null) {
                    System.out.println("Download begin");

                    String relPath = task.getFileDbMetadata().getLocation() + File.separator + task.getFileDbMetadata().getFileName();
                    while(lockedFiles.contains(relPath)) {}

                    lockedFiles.add(relPath);

                    /**
                     * инициировать запрос на скачивание файла
                     */
                    currentFileName = task.getFileDbMetadata().getFileName();
                    currentFileRelativePath = task.getFileDbMetadata().getLocation();
                    currentFileAbsolutePath = task.getUserFolder() + currentFileRelativePath + File.separator + currentFileName;
                    downloadBegin.set(false);
                    sendFileRequestAndWaitResponse(currentFileName, currentFileRelativePath);

                    while(downloadBegin.get()){
                        //System.out.println("Wait next package...");
                        if (getNextPackage()) {

                            /**
                             * Записать данные в файл
                             */
                            Files.createDirectories(Paths.get(currentFileAbsolutePath).getParent());
                            try(OutputStream writer = new BufferedOutputStream(Files.newOutputStream(Paths.get(currentFileAbsolutePath), CREATE, APPEND))){
                                byte[] data = Base64Converter.decodeBase64ToByte(currentBody);
                                writer.write(data, 0 , data.length);
                                writer.flush();
                            } catch(IOException ex) {
                                ex.printStackTrace();
                            }
                            createTransferEvent();
                            dataReceived.set(false);

                            if (currPkgNumber == totalPkgNumber) {
                                Files.setLastModifiedTime(Paths.get(currentFileAbsolutePath), FileTime.from(Instant.parse(task.getFileDbMetadata().getModified_at())));
                                downloadBegin.set(false);
                                lockedFiles.remove(relPath);
                            }
                        }
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + "shutdown");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getNextPackage() {

        DownloadFileTaskParam param = new DownloadFileTaskParam();
        param.setTaskQuery("next");

        network.sendCommand(Request.builder()
                .setMethod(DownloadFileTask.builder()
                        .setParameter(param)
                        .build())
                .build());
        waitNextPackage();
        return true;
    }

    private void sendFileRequestAndWaitResponse(String currentFileName, String fileRelativePath) {
        DownloadFileParam param = new DownloadFileParam();
        param.setFileName(currentFileName);
        param.setLocation(fileRelativePath);
        DownloadFile method = DownloadFile.builder()
                .setParameter(param)
                .build();
        network.sendCommand(Request.builder()
                .setMethod(method)
                .build());
        waitResponse();
    }

    private void waitResponse() {
        while (!downloadBegin.get()){}
    }

    private void waitNextPackage() {
        while (!dataReceived.get()){}
    }

    @Override
    public void messageReceive(Message msg) {
        if (msg instanceof Response) {
            Response resp = (Response) msg;
            if (resp.getMethod() instanceof DownloadFileTask) {
                downloadFilesTaskHandler((DownloadFileTask) resp.getMethod());
            }
            if (resp.getMethod() instanceof DownloadFile) {
                getDownloadFileHandler((DownloadFile) resp.getMethod());
            }
        }
    }

    private void downloadFilesTaskHandler(DownloadFileTask method) {
        DownloadFileTaskResult result = method.getResult();
        Metadata metadata = result.getTransferPackage().getMetadata();
        if (metadata instanceof FileDbMetadata) {
            FileDbMetadata fDbMeta = (FileDbMetadata) metadata;
            if (currentFileRelativePath.equals(fDbMeta.getLocation()) && currentFileName.equals(fDbMeta.getFileName())) {
                /**
                 * Забираем данные из сообщения
                 */
                currentFileName = fDbMeta.getFileName();
                currPkgNumber = result.getTransferPackage().getPartNumber();
                totalPkgNumber = result.getTransferPackage().getTotalNumber();
                currentBody = result.getTransferPackage().getBody();

                dataReceived.set(true);
            }
        }

    }

    private void getDownloadFileHandler(DownloadFile method) {
        DownloadFileResult result = method.getResult();
        if (result != null){
            if ("1".equals(result.getResult())){
                downloadBegin.set(true);
            }
        }
    }

    private void createTransferEvent() {
        TransferEvent transferEvent = new TransferEvent();
        transferEvent.setType("DOWNLOAD");
        transferEvent.setFileName(currentFileName);
        transferEvent.setCurrentNumber(currPkgNumber);
        transferEvent.setTotalNumber(totalPkgNumber);
        fireNotify(transferEvent);
    }

    private void fireNotify(TransferEvent transferEvent) {
        for (TransferListener listener : downloadListeners) {
            listener.onTransfer(transferEvent);
        }
    }
}
