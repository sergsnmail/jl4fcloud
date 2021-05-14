package com.sergsnmail.client.network.transfer;

import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.transfer.DownloadTask;
import com.sergsnmail.common.transfer.UploadTask;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TransferMachine {
    private Network network;
    private final int DEFAULT_UPLOAD_WORKER_COUNT = 1;
    private final int DEFAULT_DOWNLOAD_WORKER_COUNT = 1;
    private int uploadWorkerCount;
    private int downloadWorkerCount;
    private ExecutorService UPLOAD_SERVICE;
    private ExecutorService DOWNLOAD_SERVICE;
    private ConcurrentLinkedDeque<UploadTask> uploadTasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<DownloadTask> downloadTasks = new ConcurrentLinkedDeque<>();

    private ConcurrentLinkedDeque<String> lockedFiles = new ConcurrentLinkedDeque<>();

    private List<TransferListener> uploadListeners = new ArrayList<>();
    private List<TransferListener> downloadListeners = new ArrayList<>();
    private List<TransferWorker> uploadWorkers = new ArrayList<>();
    private List<DownloadWorker> downloadWorkers = new ArrayList<>();
    private final Object uploadMon = new Object();
    private final Object downloadMon = new Object();

    public TransferMachine(Network network) {
        this.network = network;
    }

    public void addUploadTask(UploadTask task){
        synchronized (uploadMon) {
            if (!uploadTasks.contains(task)){
                uploadTasks.add(task);
                uploadMon.notifyAll();
            }
        }
    }

    public void addDownloadTask(DownloadTask task){
        synchronized (downloadMon) {
            if (!downloadTasks.contains(task)){
                downloadTasks.add(task);
                downloadMon.notifyAll();
            }
        }
    }

    public void addUploadListener(TransferListener listener){
        uploadListeners.add(listener);
    }

    public void addDownloadListener(TransferListener listener){
        downloadListeners.add(listener);
    }

    public void start(){
        if (uploadWorkerCount == 0){
            uploadWorkerCount = DEFAULT_UPLOAD_WORKER_COUNT;
        }

        if (downloadWorkerCount == 0){
            downloadWorkerCount = DEFAULT_DOWNLOAD_WORKER_COUNT;
        }

        UPLOAD_SERVICE = Executors.newFixedThreadPool(uploadWorkerCount);
        DOWNLOAD_SERVICE = Executors.newFixedThreadPool(downloadWorkerCount);
        for (int i = 0; i< uploadWorkerCount; i++){
            TransferWorker worker = new TransferWorker(network, uploadTasks, uploadListeners, uploadMon, lockedFiles);
            uploadWorkers.add(worker);
            UPLOAD_SERVICE.execute(worker);
        }

        for (int i = 0; i< downloadWorkerCount; i++){
            DownloadWorker worker = new DownloadWorker(network, downloadTasks, downloadListeners, downloadMon, lockedFiles);
            downloadWorkers.add(worker);
            DOWNLOAD_SERVICE.execute(worker);
        }
    }

    public void shutdown(){
        UPLOAD_SERVICE.shutdownNow();
        DOWNLOAD_SERVICE.shutdownNow();
        synchronized (uploadMon) {
            uploadMon.notifyAll();
        }
    }

    public boolean isLocked(Path path) {
        return lockedFiles.contains(path.toString()) ? true : false;
    }
}
