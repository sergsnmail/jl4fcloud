package com.sergsnmail.client.transfer.v1;

import com.sergsnmail.client.network.ClientNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TransferMachine {
    private ClientNetwork network;
    private final int DEFAULT_WORKER_COUNT = 1;
    private int workerCount;
    private ExecutorService SERVICE;
    private ConcurrentLinkedDeque<TransferTask> tasks = new ConcurrentLinkedDeque<>();
    private List<TransferListener> listeners = new ArrayList<>();
    private List<TransferWorker> workers = new ArrayList<>();
    private final Object mon = new Object();

    public TransferMachine(ClientNetwork network) {
        this.network = network;
    }

    public void addFile(TransferTask task){
        synchronized (mon) {
            tasks.add(task);
            mon.notifyAll();
        }
    }

    public void addListener(TransferListener listener){
        listeners.add(listener);
    }

    public void start(){
        if (workerCount == 0){
            workerCount = DEFAULT_WORKER_COUNT;
        }
        SERVICE = Executors.newFixedThreadPool(workerCount);
        for (int i = 0;i<workerCount;i++){
            TransferWorker worker = new TransferWorker(network, tasks, listeners, mon);
            workers.add(worker);
            SERVICE.execute(worker);
        }
    }

    public void shutdown(){
        synchronized (mon) {
            SERVICE.shutdownNow();
            mon.notifyAll();
        }
    }

}
