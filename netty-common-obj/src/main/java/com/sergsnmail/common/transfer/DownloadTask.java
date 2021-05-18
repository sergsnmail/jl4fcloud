package com.sergsnmail.common.transfer;

import com.sergsnmail.common.message.method.common.FileDbMetadata;

public class DownloadTask {
    private FileDbMetadata fileDbMetadata;
    private String userFolder;

    public FileDbMetadata getFileDbMetadata() {
        return fileDbMetadata;
    }

    public void setFileDbMetadata(FileDbMetadata fileDbMetadata) {
        this.fileDbMetadata = fileDbMetadata;
    }

    public String getUserFolder() {
        return userFolder;
    }

    public void setUserFolder(String userFolder) {
        this.userFolder = userFolder;
    }
}
