package com.sergsnmail.server.db.file;

import com.sergsnmail.server.db.user.User;

import java.util.List;

public interface FileDataSource {
    List<StorageFile> getAllFiles(User user);
    List<StorageFile> getFiles(User user, String filename, String userLocation);
    boolean addFile(User user, StorageFile storageFile);
    boolean updateFile(StorageFile updatedStorageFile);
    boolean deleteFile(StorageFile deletedStorageFile);
}
