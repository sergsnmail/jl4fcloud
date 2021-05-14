package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Result;

public class DownloadFileResult extends Result {

    @JsonProperty("result")
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
