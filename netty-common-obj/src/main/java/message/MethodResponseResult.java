package message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Result;

public class MethodResponseResult<T extends Result> {

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private T resultImpl;

    public T getResultImpl() {
        return resultImpl;
    }

    public void setResultImpl(T resultImpl) {
        this.resultImpl = resultImpl;
    }
}
