package com.sergsnmail.client.transfer;

import com.sergsnmail.common.message.method.common.FileMetadata;

public class FilePackage {

    private String packageId;
    private int packageNumber;
    private int totalPackageCount;
    private byte[] body;
    private FileMetadata fileMetadata;
    private boolean isReceived = false;

    public int getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(int packageNumber) {
        this.packageNumber = packageNumber;
    }

    public int getTotalPackageCount() {
        return totalPackageCount;
    }

    public void setTotalPackageCount(int totalPackageCount) {
        this.totalPackageCount = totalPackageCount;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
}
