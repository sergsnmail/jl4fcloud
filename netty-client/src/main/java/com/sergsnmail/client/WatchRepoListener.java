package com.sergsnmail.client;

import java.nio.file.Path;

public interface WatchRepoListener {
    void onWatchRepoEvent(Path file);
}
