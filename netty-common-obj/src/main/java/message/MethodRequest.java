package message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MethodRequest {

    @JsonProperty("params")
    private List<MethodRequestParameter> params = new ArrayList<>();

    public void addParameter(MethodRequestParameter mParam){
        params.add(mParam);
    }

    public List<MethodRequestParameter> getParams() {
        return params;
    }
}
