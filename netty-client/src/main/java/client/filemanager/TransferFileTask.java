package client.filemanager;

import client.network.ClientNetwork;
import client.network.NetworkListener;

import message.Base64Converter;
import message.Request;
import message.Response;
import message.common.Message;
import message.method.putfile.FileMetadata;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;
import message.method.putfile.PutFilesResult;

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
        param.setBody(Base64Converter.encodeByteToBase64(filePackage.getBody()));

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
