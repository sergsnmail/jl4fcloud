package com.sergsnmail.client.transfer;

import com.sergsnmail.client.NotifyCallback;
import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.common.message.method.putfile.FileMetadata;

import java.nio.file.Path;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransferFileManager {

    private final ClientNetwork clientNetwork;
    private ExecutorService SERVICE = Executors.newFixedThreadPool(1);
    private NotifyCallback notifyCallback;
    private Map<String, TransferFileTask> runningTasks = new ConcurrentHashMap<>();

    public TransferFileManager(ClientNetwork clientNetwork) {
        this.clientNetwork = clientNetwork;
    }

    public void transferFile(Path file, FileMetadata fileMetadata) {
        TransferFileTask newTask = new TransferFileTask(clientNetwork, this);
        newTask.setFilePath(file);
        newTask.setMetadata(fileMetadata);
        Thread worker = new Thread(newTask);
        worker.setDaemon(true);
        SERVICE.execute(worker);
    }

    public void setNotifyCallback(NotifyCallback clientController) {
        this.notifyCallback = clientController;
    }

    public void notify(Object transferNotifyObject){
        notifyCallback.notify(transferNotifyObject);
    }

    public void shutdownTasks() {
        for (TransferFileTask task : runningTasks.values()) {
            if (task.isActive()) {
                task.terminate(true);
            }
        }
        System.out.println("All tasks shutdown.");
    }

    public void transferShutdown() {
        SERVICE.shutdown();
        try {
            if (!SERVICE.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                SERVICE.shutdownNow();
            }
        } catch (InterruptedException e) {
            SERVICE.shutdownNow();
        }
        shutdownTasks();
        System.out.println("Shutdown complete");
    }

    public void addActiveTask(String packageId, TransferFileTask transferTask) {
        this.runningTasks.put(packageId,transferTask);
    }

    public void removeActiveTask(String packageId) {
        this.runningTasks.remove(packageId);
    }

    public static class TransferNotifyObject {
        private String fileName;
        private int currentNumber;
        private int totalNumber;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getCurrentNumber() {
            return currentNumber;
        }

        public void setCurrentNumber(int currentNumber) {
            this.currentNumber = currentNumber;
        }

        public int getTotalNumber() {
            return totalNumber;
        }

        public void setTotalNumber(int totalNumber) {
            this.totalNumber = totalNumber;
        }
    }
}
