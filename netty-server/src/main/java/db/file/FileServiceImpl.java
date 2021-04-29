package db.file;

import db.user.User;

import java.util.List;

public class FileServiceImpl {

    private FileDataSource fileDataSource;

    public FileServiceImpl(FileDataSource fileDataSource){
        this.fileDataSource = fileDataSource;
    }

    public List<StorageFile> getFiles(User user) {
        return this.fileDataSource.getFiles(user);
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
