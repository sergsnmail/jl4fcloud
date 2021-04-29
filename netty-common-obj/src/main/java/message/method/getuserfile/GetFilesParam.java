package message.method.getuserfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Parameter;

import java.util.List;

public class GetFilesParam extends Parameter {

    @JsonProperty("file")
    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

}
