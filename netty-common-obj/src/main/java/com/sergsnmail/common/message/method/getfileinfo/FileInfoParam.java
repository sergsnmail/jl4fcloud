package com.sergsnmail.common.message.method.getfileinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.common.Parameter;
import com.sergsnmail.common.message.method.common.FileMetadata;

public class FileInfoParam extends Parameter {

    @JsonProperty("metadata")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private FileMetadata metadata;

    public FileMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FileMetadata metadata) {
        this.metadata = metadata;
    }
}
