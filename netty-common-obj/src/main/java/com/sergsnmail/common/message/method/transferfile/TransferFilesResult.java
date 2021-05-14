package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Result;

public class TransferFilesResult extends Result {

    @JsonProperty("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
