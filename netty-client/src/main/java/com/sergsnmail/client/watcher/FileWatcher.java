package com.sergsnmail.client.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private WatchService watchService;
    private List<FileListener> listeners = new ArrayList<>();

    public FileWatcher() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    public void registerDir(Path path) throws IOException {
        path.register(this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );
    }

    public void run() {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                Path watchable = (Path) key.watchable();
                for (WatchEvent<?> event : key.pollEvents()) {
                    fireNotify(event, watchable.resolve((Path) event.context()));
                }
                key.reset();
            }
        }catch (Exception e){}
    }

    private void fireNotify(WatchEvent<?> event, Path file){
        WatchEvent.Kind<?> kind = event.kind();
        for (FileListener listener : listeners) {
            if (kind == ENTRY_CREATE){
                listener.createEvent(file);
            }else if (kind == ENTRY_MODIFY){
                listener.modifyEvent(file);
            }else if(kind == ENTRY_DELETE){
                listener.deleteEvent(file);
            }
        }
    }

    public void addListener(FileListener listener){
        listeners.add(listener);
    }

    public void start(){
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
    }
}
