package com.sergsnmail.client.transfer;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.client.network.NetworkListener;
import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.method.putfile.PutFilesMethod;
import com.sergsnmail.common.message.method.putfile.PutFilesParam;
import com.sergsnmail.common.message.method.putfile.PutFilesResult;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Класс представляет рабочий поток для передачи файла
 */
public class TransferWorker implements Runnable, NetworkListener {
    private final ClientNetwork network;
    private final ConcurrentLinkedDeque<TransferTask> tasks;
    private final List<TransferListener> listeners;
    private final Object mon;
    private final Object resultMon = new Object();
    private FilePackage currPackage;
    private TransferResult currentTransferResult;

    public TransferWorker(ClientNetwork network, ConcurrentLinkedDeque<TransferTask> tasks, List<TransferListener> listeners, Object mon) {
        this.network = network;
        this.tasks = tasks;
        this.listeners = listeners;
        this.mon = mon;
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
                        System.out.println("worker sleep");
                        mon.wait();
                        System.out.println("worker awake");
                    }
                }
                TransferTask task;
                if ((task = tasks.poll()) != null) {
                    System.out.println("Transfer begin");
                    PackageCollection packageCollection = new PackageCollection(task.getFile());
                    if (packageCollection.hasNext()) {
                        while (!Thread.currentThread().isInterrupted() && packageCollection.hasNext()) {
                            currPackage = packageCollection.next();
                            currPackage.setReceived(false);
                            currPackage.setFileMetadata(task.getMetadata());
                            TransferResult result = sendPackageAndWaitResult(currPackage);
                            if (result == TransferResult.RECEIVED) {
                                createTransferEvent();
                            }
                        }
                        System.out.println("Transfer complete");
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + "shutdown");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private TransferResult sendPackageAndWaitResult(FilePackage currPackage) {
        setCurrentTransferResult(TransferResult.WAIT);
        sendPackage(currPackage);
        while(getCurrentTransferResult() == TransferResult.WAIT){
        }
        return getCurrentTransferResult();
    }

    private void sendPackage(FilePackage filePackage) {
        PutFilesParam param = new PutFilesParam();
        param.setPackageId(filePackage.getPackageId());
        param.setPartNumber(filePackage.getPackageNumber());
        param.setTotalNumber(filePackage.getTotalPackageCount());
        param.setMetadata(filePackage.getFileMetadata());
        param.setBody(Base64Converter.encodeByteToBase64Str(filePackage.getBody()));

        System.out.printf("id: %s, %d/%d [%s]%n ", filePackage.getPackageId(), filePackage.getPackageNumber(), filePackage.getTotalPackageCount(), filePackage.getFileMetadata().getFileName());

        PutFilesMethod putMethod = PutFilesMethod.builder()
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
            if (resp.getMethod() instanceof PutFilesMethod) {
                handlePutFilesMethod((PutFilesMethod) resp.getMethod());
            }
        }
    }

    private void handlePutFilesMethod(PutFilesMethod putFilesMethod){
        PutFilesResult putResult = putFilesMethod.getResult();
        if (putResult != null) {
            String receivedPackageId = putFilesMethod.getParameter().getPackageId();
            if (currPackage != null && currPackage.getPackageId().equals(receivedPackageId)) {
                if ("1".equals(putResult.getStatus())) {
                    setCurrentTransferResult(TransferResult.RECEIVED);
                    //currPackage.setReceived(true);
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
