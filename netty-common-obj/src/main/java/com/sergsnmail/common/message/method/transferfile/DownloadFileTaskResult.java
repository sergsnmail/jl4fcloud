package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.common.Result;
import com.sergsnmail.common.message.method.common.TransferPackage;

public class DownloadFileTaskResult extends Result {

    @JsonProperty("transferPackage")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private TransferPackage transferPackage;

    public TransferPackage getTransferPackage() {
        return transferPackage;
    }

    public void setTransferPackage(TransferPackage transferPackage) {
        this.transferPackage = transferPackage;
    }
}
