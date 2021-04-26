package message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Parameter;

public class MethodRequestParameter<T extends Parameter> {

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private T paramImpl;

    public MethodRequestParameter() {
    }

    public MethodRequestParameter(T paramImpl) {
        this.paramImpl = paramImpl;
    }

    public T getParamImpl() {
        return paramImpl;
    }

    public void setParamImpl(T paramImpl) {
        this.paramImpl = paramImpl;
    }

}
