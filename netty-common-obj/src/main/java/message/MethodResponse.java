package message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MethodResponse {

    @JsonProperty("result")
    private MethodResponseResult result;

    public MethodResponseResult getResult() {
        return result;
    }

    public void setResult(MethodResponseResult result) {
        this.result = result;
    }
}
