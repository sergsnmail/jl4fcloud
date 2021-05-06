package com.sergsnmail.common.message.method.putfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Result;

public class PutFilesResult extends Result {

    @JsonProperty("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
