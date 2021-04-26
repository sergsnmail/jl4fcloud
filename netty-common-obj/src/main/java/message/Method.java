package message;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Result;

import java.util.List;

public class Method {

    @JsonProperty("name")
    private String name;

    @JsonProperty("request")
    private MethodRequest request = new MethodRequest();

    @JsonProperty("response")
    private MethodResponse response = new MethodResponse();

    public Method(@JsonProperty("name") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodRequest getRequest() {
        return request;
    }

    public void setRequest(MethodRequest request) {
        this.request = request;
    }

    public MethodResponse getResponse() {
        return response;
    }

    public void setResponse(MethodResponse response) {
        this.response = response;
    }

    public <T> T getParamImpl(Class<T> clazz) {
        for (MethodRequestParameter param: request.getParams()){
            if (clazz.isInstance(param.getParamImpl())) {
                return (T) param.getParamImpl();
            }
        }
        return null;
    }

    public <T> T getResultImpl(Class<T> clazz){
        if (response.getResult() == null){
            throw new NullPointerException("result is null");
        }
        return (T) response.getResult().getResultImpl();
    }
}
