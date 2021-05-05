package message.method.putfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Parameter;

public class PutFilesParam extends Parameter {

    @JsonProperty("packageId")
    private String packageId;

    @JsonProperty("partNumber")
    private int partNumber;

    @JsonProperty("totalNumber")
    private int totalNumber;

    @JsonProperty("metadata")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private FileMetadata metadata;


    /*@JsonProperty("filename")
    private String filename;*/

    /*@JsonProperty("path")
    private String path;*/

    @JsonProperty("body")
    private String body;

    /*public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }*/

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageid) {
        this.packageId = packageid;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partnumber) {
        this.partNumber = partnumber;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalnumber) {
        this.totalNumber = totalnumber;
    }

    public void setMetadata(FileMetadata metadata) {
        this.metadata = metadata;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }
}
