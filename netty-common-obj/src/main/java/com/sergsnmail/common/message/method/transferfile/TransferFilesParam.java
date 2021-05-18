package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.common.Parameter;
import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.common.TransferPackage;

public class TransferFilesParam extends Parameter {

    @JsonProperty("transferPackage")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private TransferPackage transferPackage;

    public TransferPackage getTransferPackage() {
        return transferPackage;
    }

    public void setTransferPackage(TransferPackage transferPackage) {
        this.transferPackage = transferPackage;
    }


//    @JsonProperty("packageId")
//    private String packageId;
//
//    @JsonProperty("partNumber")
//    private int partNumber;
//
//    @JsonProperty("totalNumber")
//    private int totalNumber;
//
//    @JsonProperty("metadata")
//    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
//    private FileMetadata metadata;
//
//    @JsonProperty("body")
//    private String body;
//
//    public String getBody() {
//        return body;
//    }
//
//    public void setBody(String body) {
//        this.body = body;
//    }
//
//    public String getPackageId() {
//        return packageId;
//    }
//
//    public void setPackageId(String packageid) {
//        this.packageId = packageid;
//    }
//
//    public int getPartNumber() {
//        return partNumber;
//    }
//
//    public void setPartNumber(int partnumber) {
//        this.partNumber = partnumber;
//    }
//
//    public int getTotalNumber() {
//        return totalNumber;
//    }
//
//    public void setTotalNumber(int totalnumber) {
//        this.totalNumber = totalnumber;
//    }
//
//    public void setMetadata(FileMetadata metadata) {
//        this.metadata = metadata;
//    }
//
//    public FileMetadata getMetadata() {
//        return metadata;
//    }
}
