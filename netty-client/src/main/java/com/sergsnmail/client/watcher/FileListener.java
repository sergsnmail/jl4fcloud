package com.sergsnmail.client.watcher;

import java.nio.file.Path;

public interface FileListener {
    void createEvent(Path path);
    void deleteEvent(Path path);
    void modifyEvent(Path path);
}
