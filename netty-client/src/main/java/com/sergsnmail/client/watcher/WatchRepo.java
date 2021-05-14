package com.sergsnmail.client.watcher;

import com.sergsnmail.client.WatchRepoListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class WatchRepo {
    private static final long DEFAULT_UPDATE_TIMEOUT = 5000;
    private static final long WAIT_TIMEOUT = 3000;
    private static final long COPY_TIMEOUT = 2000;
    private ConcurrentHashMap<Path, RepoInfo> watchedFiles = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Path, Long> copyFolder = new ConcurrentHashMap<>();
    private List<WatchRepoListener> listeners = new ArrayList<>();

    private ExecutorService SERVICE;

    public void add(Path file){
        if (file == null && !Files.exists(file)){
            throw new IllegalArgumentException("File not exist");
        }

        long currentTime = System.currentTimeMillis();
        if (watchedFiles.containsKey(file)){
            RepoInfo repoInfo = watchedFiles.get(file);
            Long prevLastUpdate = repoInfo.getLastUpdate();
            if (currentTime - prevLastUpdate < COPY_TIMEOUT && currentTime - prevLastUpdate > 0){
                //System.out.println("Copy detected!!!");
                repoInfo.setState(WatchedFileState.UPDATE_WHILE_COPING);
            } else {
                repoInfo.setState(WatchedFileState.UPDATED);
            }
        } else {
            RepoInfo newRepoInfo = new RepoInfo();
            newRepoInfo.setRegistered(currentTime);
            newRepoInfo.setState(WatchedFileState.CREATE);
            newRepoInfo.setLastUpdate(currentTime);
            watchedFiles.put(file, newRepoInfo);
        }
    }

    private void addForce(Path file){
        if (file == null && !Files.exists(file)){
            throw new IllegalArgumentException("File not exist");
        }

        long currentTime = System.currentTimeMillis();
        if (watchedFiles.containsKey(file)){
            watchedFiles.remove(file);
        } else {
            RepoInfo newRepoInfo = new RepoInfo();
            newRepoInfo.setRegistered(currentTime);
            newRepoInfo.setState(WatchedFileState.UPDATED);
            newRepoInfo.setLastUpdate(currentTime);
            watchedFiles.put(file, newRepoInfo);
        }
    }


    public void remove(Path path) throws NoSuchMethodException {
        watchedFiles.remove(path);
    }

    public void addListener(WatchRepoListener watchRepoListener) {
        listeners.add(watchRepoListener);
    }

    private void fireNotify(Path file) {
        for (WatchRepoListener listener : listeners) {
            listener.onWatchRepoEvent(file);
        }
    }

    public void start() {
        SERVICE = Executors.newFixedThreadPool(1);
        SERVICE.execute(() ->{
            //System.out.println("RepoWatcher running...\n");
            while (!Thread.currentThread().isInterrupted()) {
                //System.out.println("checking file in repo");
                try {
                    long currentTime = System.currentTimeMillis();
                    for (Path path : watchedFiles.keySet()) {
                        RepoInfo repoInfo = watchedFiles.get(path);
                        if (repoInfo != null) {
                            //System.out.println(repoInfo);
                            if (WatchedFileState.UPDATED.equals(repoInfo.getState())) {
                                //System.out.printf("Transfer ready for %s\n", repoInfo);
                                repoInfo.setState(WatchedFileState.SYNC);
                                fireNotify(path);
                            } else if (WatchedFileState.UPDATE_WHILE_COPING.equals(repoInfo.getState())) {
                                if (currentTime - repoInfo.getRegistered() > DEFAULT_UPDATE_TIMEOUT) {
                                    repoInfo.setState(WatchedFileState.UPDATED);
                                    scanFolder(path.getParent());
                                }
                            }else if (WatchedFileState.CREATE.equals(repoInfo.getState())) {
                                if (currentTime - repoInfo.getRegistered() > DEFAULT_UPDATE_TIMEOUT) {
                                    repoInfo.setState(WatchedFileState.UPDATED);
                                }
                            }
                        }
                    }

                    long startWait = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startWait < WAIT_TIMEOUT && !Thread.currentThread().isInterrupted() ){}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("RepoWatcher shutdown...\n");
        });
    }

    private void scanFolder(Path folder){
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.filter(Files::isRegularFile).forEach((currFile) -> {
                if (!watchedFiles.containsKey(currFile)){
                    addForce(currFile);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown(){
        SERVICE.shutdownNow();
    }
}

