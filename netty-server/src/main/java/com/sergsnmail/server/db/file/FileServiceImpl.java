package com.sergsnmail.server.db.file;

import com.sergsnmail.server.db.user.User;

import java.util.List;

public class FileServiceImpl {

    private FileDataSource fileDataSource;

    public FileServiceImpl(FileDataSource fileDataSource){
        this.fileDataSource = fileDataSource;
    }

    public List<StorageFile> getAllFiles(User user) {
        return this.fileDataSource.getAllFiles(user);
    }

    public List<StorageFile> getFiles(User user, String filename, String userLocation) {
        return this.fileDataSource.getFiles(user, filename, userLocation);
    }

    public boolean addFile(User user, StorageFile storageFile) {
        return this.fileDataSource.addFile(user,storageFile);
    }

    public boolean updateFile(StorageFile updatedStorageFile) {
        return this.fileDataSource.updateFile(updatedStorageFile);
    }

    public boolean deleteFile(StorageFile deletedStorageFile) {
        return this.fileDataSource.deleteFile(deletedStorageFile);
    }
}
