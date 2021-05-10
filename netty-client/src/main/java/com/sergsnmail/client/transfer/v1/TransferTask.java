package com.sergsnmail.client.transfer.v1;

import com.sergsnmail.common.message.method.common.FileMetadata;

import java.nio.file.Path;

public class TransferTask {
    private Path file;
    private FileMetadata metadata;

    public TransferTask(Path file, FileMetadata metadata) {
        this.file = file;
        this.metadata = metadata;
    }

    public Path getFile() {
        return file;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }
}
