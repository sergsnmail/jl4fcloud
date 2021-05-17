package com.sergsnmail.client.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private ExecutorService SERVICE;

    private WatchService watchService;
    private List<FileListener> listeners = new ArrayList<>();
    private final Map<WatchKey,Path> keys;

    public FileWatcher() throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
    }

    public void register(Path path) throws IOException {
        WatchKey key =  path.register(this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE );
        keys.put(key, path);
    }

    public void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void run() {
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {

                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                Path watchable = (Path) key.watchable();

                for (WatchEvent<?> event : key.pollEvents()) {
                    //System.out.format("%s: %s\n", event.kind().name(), watchable.resolve((Path) event.context()));
                    Path file = watchable.resolve((Path) event.context());
                    if (Files.isDirectory(file, NOFOLLOW_LINKS)) {
                         registerAll(file);
                         //fireNotify(event, watchable.resolve((Path) event.context()));
                    //}
                    //fireNotify(event, watchable.resolve((Path) event.context()));
                    } else {
                        fireNotify(event, watchable.resolve((Path) event.context()));
                    }
                }
                key.reset();
            }
        }catch (Exception e){}
    }

    private void fireNotify(WatchEvent<?> event, Path file){
        WatchEvent.Kind<?> kind = event.kind();
        for (FileListener listener : listeners) {
            //System.out.format("%s: %s\n", event.kind().name(), file);
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
        SERVICE = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(this);
        SERVICE.execute(this);
        //thread.setDaemon(true);
        //thread.start();
    }

    public void shutdown(){
        SERVICE.shutdownNow();
    }
    public void printWatchable() {
        for (Map.Entry<WatchKey, Path> watchKeyPathEntry : keys.entrySet()) {
            System.out.println(watchKeyPathEntry.getValue());
        }
    }
}
