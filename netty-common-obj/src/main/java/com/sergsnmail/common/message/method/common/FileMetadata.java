package com.sergsnmail.common.message.method.common;

public class FileMetadata {
    private String fileName;
    private String filePath;
    private String fileRelativePath;
    private String created_at;
    private String modified_at;
    private long size;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getModified_at() {
        return modified_at;
    }

    public void setModified_at(String modified_at) {
        this.modified_at = modified_at;
    }

    public String getFileRelativePath() {
        return fileRelativePath;
    }

    public void setFileRelativePath(String fileRelativePath) {
        this.fileRelativePath = fileRelativePath;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileRelativePath='" + fileRelativePath + '\'' +
                ", created_at='" + created_at + '\'' +
                ", modified_at='" + modified_at + '\'' +
                ", size=" + size +
                '}';
    }
}
