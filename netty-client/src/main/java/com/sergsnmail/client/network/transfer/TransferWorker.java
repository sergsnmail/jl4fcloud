package com.sergsnmail.client.network.transfer;

import com.sergsnmail.common.message.method.common.TransferPackage;
import com.sergsnmail.common.network.NetworkListener;
import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.transferfile.UploadFilesMethod;
import com.sergsnmail.common.message.method.transferfile.TransferFilesParam;
import com.sergsnmail.common.message.method.transferfile.TransferFilesResult;
import com.sergsnmail.common.transfer.FilePackage;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.transfer.PackageCollection;
import com.sergsnmail.common.transfer.UploadTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Класс представляет рабочий поток для передачи файла
 */
public class TransferWorker implements Runnable, NetworkListener {
    private final Network network;
    private final ConcurrentLinkedDeque<UploadTask> tasks;
    private final List<TransferListener> listeners;
    private final Object mon;
    private final Object resultMon = new Object();
    private final ConcurrentLinkedDeque<String> lockedFiles;
    private FilePackage currPackage;
    private TransferResult currentTransferResult;

    public TransferWorker(Network network, ConcurrentLinkedDeque<UploadTask> tasks, List<TransferListener> listeners, Object mon, ConcurrentLinkedDeque<String> lockedFiles) {
        this.network = network;
        this.tasks = tasks;
        this.listeners = listeners;
        this.mon = mon;
        this.lockedFiles = lockedFiles;
        this.network.addChannelListener(this);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                while (tasks.size() == 0 && !Thread.currentThread().isInterrupted()) {
                    /**
                     * если заданий нет, то поток спит
                     */
                    synchronized (mon) {
                        mon.wait();
                    }
                }
                UploadTask task;
                if ((task = tasks.poll()) != null) {
                    String relPath = task.getMetadata().getFileRelativePath() + File.separator + task.getMetadata().getFileName();
                    while(lockedFiles.contains(relPath)){}

                    lockedFiles.add(relPath);

                    PackageCollection packageCollection = new PackageCollection(task.getFile());
                    FileMetadata fullMeta = appendMetadataInfo(task.getMetadata());
                    if (packageCollection.hasNext()) {
                        while (!Thread.currentThread().isInterrupted() && packageCollection.hasNext()) {
                            currPackage = packageCollection.next();
                            currPackage.setReceived(false);
                            currPackage.setFileMetadata(fullMeta);
                            TransferResult result = sendPackageAndWaitResult(currPackage);
                            if (result == TransferResult.RECEIVED) {
                                createTransferEvent();
                            }
                        }
                    }
                    lockedFiles.remove(relPath);
                }
            }
            System.out.println(Thread.currentThread().getName() + "shutdown");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавляем атрибуты файла
     */
    private FileMetadata appendMetadataInfo(FileMetadata metadata) throws IOException {
        Path file = Paths.get(metadata.getFilePath());
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        metadata.setCreated_at(attr.creationTime().toString());
        metadata.setModified_at(attr.lastModifiedTime().toString());
        metadata.setSize(Files.size(file));
        return metadata;
    }

    private TransferResult sendPackageAndWaitResult(FilePackage currPackage) {
        setCurrentTransferResult(TransferResult.WAIT);
        sendPackage(currPackage);
        while(getCurrentTransferResult() == TransferResult.WAIT){
        }
        return getCurrentTransferResult();
    }

    private void sendPackage(FilePackage filePackage) {
        TransferPackage transferPackage = new TransferPackage();
        transferPackage.setPackageId(filePackage.getPackageId());
        transferPackage.setPartNumber(filePackage.getPackageNumber());
        transferPackage.setTotalNumber(filePackage.getTotalPackageCount());
        transferPackage.setMetadata(filePackage.getFileMetadata());
        transferPackage.setBody(Base64Converter.encodeByteToBase64Str(filePackage.getBody()));
        TransferFilesParam param = new TransferFilesParam();
        param.setTransferPackage(transferPackage);
//        param.setPackageId(filePackage.getPackageId());
//        param.setPartNumber(filePackage.getPackageNumber());
//        param.setTotalNumber(filePackage.getTotalPackageCount());
//        param.setMetadata(filePackage.getFileMetadata());
//        param.setBody(Base64Converter.encodeByteToBase64Str(filePackage.getBody()));
//
       // System.out.printf("id: %s, %d/%d [%s]%n ", filePackage.getPackageId(), filePackage.getPackageNumber(), filePackage.getTotalPackageCount(), filePackage.getFileMetadata().getFileName());

        UploadFilesMethod putMethod = UploadFilesMethod.builder()
                .setParameter(param)
                .build();

        this.network.sendCommand(Request.builder()
                .setMethod(putMethod)
                .build());
    }

    @Override
    public void messageReceive(Message msg) {
        if (msg instanceof Response) {
            Response resp = (Response) msg;
            if (resp.getMethod() instanceof UploadFilesMethod) {
                handlePutFilesMethod((UploadFilesMethod) resp.getMethod());
            }
        }
    }

    private void handlePutFilesMethod(UploadFilesMethod putFilesMethod){
        TransferFilesResult putResult = putFilesMethod.getResult();
        if (putResult != null) {

            String receivedPackageId = putFilesMethod.getParameter().getTransferPackage().getPackageId();
            if (currPackage != null && currPackage.getPackageId().equals(receivedPackageId)) {
                if ("1".equals(putResult.getStatus())) {
                    setCurrentTransferResult(TransferResult.RECEIVED);
                }
            }
        }
    }

    private void setCurrentTransferResult(TransferResult result){
        synchronized (resultMon){
            currentTransferResult = result;
        }
    }

    private TransferResult getCurrentTransferResult(){
        synchronized (resultMon){
            return currentTransferResult;
        }
    }

    private void createTransferEvent() {
        TransferEvent transferEvent = new TransferEvent();
        transferEvent.setType("UPLOAD");
        transferEvent.setFileName(currPackage.getFileMetadata().getFileName());
        transferEvent.setCurrentNumber(currPackage.getPackageNumber());
        transferEvent.setTotalNumber(currPackage.getTotalPackageCount());
        fireNotify(transferEvent);
    }

    private void fireNotify(TransferEvent transferEvent) {
        for (TransferListener listener : listeners) {
            listener.onTransfer(transferEvent);
        }
    }
}
