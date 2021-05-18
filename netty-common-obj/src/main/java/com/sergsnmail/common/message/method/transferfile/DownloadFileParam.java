package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Parameter;

public class DownloadFileParam extends Parameter {

    @JsonProperty("file")
    private String fileName;

    @JsonProperty("location")
    private String location;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
