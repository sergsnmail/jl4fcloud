package message.method.putfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Parameter;

public class PutFilesParam extends Parameter {

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("path")
    private String path;

    @JsonProperty("body")
    private String body;

    public String getFilename() {
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
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
