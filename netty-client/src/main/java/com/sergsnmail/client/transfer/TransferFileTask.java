package com.sergsnmail.client.transfer;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.client.network.NetworkListener;

import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.putfile.PutFilesMethod;
import com.sergsnmail.common.message.method.putfile.PutFilesParam;
import com.sergsnmail.common.message.method.putfile.PutFilesResult;

import java.nio.file.Path;

import java.util.concurrent.atomic.AtomicBoolean;

public class TransferFileTask implements Runnable, NetworkListener {

    private final ClientNetwork clientNetwork;
    private TransferFileManager transferFileManager;
    private FilePackage currPackage;
    private FileMetadata metadata;
    private Path file;
    private AtomicBoolean terminate = new AtomicBoolean(false);
    private boolean isActive = false;

    public TransferFileTask(ClientNetwork clientNetwork, TransferFileManager transferFileManager) {
        this.clientNetwork = clientNetwork;
        this.transferFileManager = transferFileManager;
    }

    @Override
    public void run() {
        isActive = true;
        PackageCollection packageCollection = new PackageCollection(file);
        if (packageCollection.hasNext()) {
            this.transferFileManager.addActiveTask(packageCollection.getPackageId(), this);
            this.clientNetwork.addChannelListener(this);

            /**
             * Отправляем первый пакет
             */
            currPackage = packageCollection.next();
            currPackage.setReceived(false);
            currPackage.setFileMetadata(metadata);
            sendPackage(currPackage);

            /**
             * отправляем остальные пакеты
             */
            while (!terminate.get()) {
                if (currPackage != null && currPackage.isReceived()) {
                    if (packageCollection.hasNext()) {
                        currPackage = packageCollection.next();
                        currPackage.setReceived(false);
                        currPackage.setFileMetadata(metadata);
                        sendPackage(currPackage);
                    } else {
                        break;
                    }
                }
            }
            isActive = false;
            this.transferFileManager.removeActiveTask(packageCollection.getPackageId());
            System.out.println("Transfer complete");
        }
    }

    public void setFilePath(Path file) {
        this.file = file;
    }

    public void setMetadata(FileMetadata fileMetadata) {
        this.metadata = fileMetadata;
    }

    private void sendPackage(FilePackage filePackage) {
        PutFilesParam param = new PutFilesParam();
        param.setPackageId(filePackage.getPackageId());
        param.setPartNumber(filePackage.getPackageNumber());
        param.setTotalNumber(filePackage.getTotalPackageCount());
        param.setMetadata(filePackage.getFileMetadata());
        //param.setBody(Base64Converter.encodeByteToBase64Str(filePackage.getBody()));
        param.setBody(Base64Converter.encodeByteToBase64Str(new byte[]{15}));
        //param.setBytebody(Base64Converter.encodeByteToBase64Str(filePackage.getBody()));

        System.out.printf("id: %s, %d/%d [%s]%n ", filePackage.getPackageId(), filePackage.getPackageNumber(), filePackage.getTotalPackageCount(),filePackage.getFileMetadata().getFileName());

        PutFilesMethod putMethod = PutFilesMethod.builder()
                .setParameter(param)
                .build();

        this.clientNetwork.sendCommand(Request.builder()
                .setMethod(putMethod)
                .build());
    }

    @Override
    public void messageReceive(Message msg) {
        if (isActive) {
            if (msg instanceof Response) {
                Response resp = (Response) msg;
                if (resp.getMethod() instanceof PutFilesMethod) {
                    PutFilesMethod putFilesMethod = (PutFilesMethod) resp.getMethod();
                    PutFilesResult putResult = putFilesMethod.getResult();
                    if (putResult != null) {
                        String receivedPackageId = putFilesMethod.getParameter().getPackageId();
                        if (currPackage != null && currPackage.getPackageId().equals(receivedPackageId)) {
                            if ("1".equals(putResult.getStatus())) {
                                TransferFileManager.TransferNotifyObject transferNotifyObject = new TransferFileManager.TransferNotifyObject();
                                transferNotifyObject.setFileName(currPackage.getFileMetadata().getFileName());
                                transferNotifyObject.setCurrentNumber(currPackage.getPackageNumber());
                                transferNotifyObject.setTotalNumber(currPackage.getTotalPackageCount());
                                transferFileManager.notify(transferNotifyObject);
                                currPackage.setReceived(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public void terminate(boolean shutdown) {
        this.terminate.set(shutdown);
    }

    public boolean isActive() {
        return isActive;
    }
}
