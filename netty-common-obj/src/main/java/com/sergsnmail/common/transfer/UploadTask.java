package com.sergsnmail.common.transfer;

import com.sergsnmail.common.message.method.common.FileMetadata;

import java.nio.file.Path;
import java.util.Objects;

public class UploadTask {
    private Path file;
    private FileMetadata metadata;

    public UploadTask(Path file, FileMetadata metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public Path getFile() {
        return file;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "TransferTask{" +
                "file=" + file +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadTask that = (UploadTask) o;
        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getFileName());
    }
}
