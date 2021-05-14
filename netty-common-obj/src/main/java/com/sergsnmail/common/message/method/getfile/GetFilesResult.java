package com.sergsnmail.common.message.method.getfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.common.Result;
import com.sergsnmail.common.message.method.common.FileDbMetadata;
import com.sergsnmail.common.message.method.common.FileMetadata;

import java.util.List;

public class GetFilesResult extends Result {

    @JsonProperty("files")
    private List<String> files;


    @JsonProperty("dbmetadata")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private List<FileDbMetadata> dbmetadata;


    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }


    public List<FileDbMetadata> getDbmetadata() {
        return dbmetadata;
    }

    public void setDbmetadata(List<FileDbMetadata> dbmetadata) {
        this.dbmetadata = dbmetadata;
    }
}
