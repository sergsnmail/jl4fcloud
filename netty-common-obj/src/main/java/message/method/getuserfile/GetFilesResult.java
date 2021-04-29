package message.method.getuserfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Result;

import java.util.List;

public class GetFilesResult extends Result {

    @JsonProperty("files")
    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
