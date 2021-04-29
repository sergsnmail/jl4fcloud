package db.file;

import db.user.User;

import java.util.List;

public interface FileDataSource {
    List<StorageFile> getFiles(User user);
    boolean addFile(User user, StorageFile storageFile);
    boolean updateFile(StorageFile updatedStorageFile);
    boolean deleteFile(StorageFile deletedStorageFile);
}
