package message.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Parameter;
import message.common.Result;

import java.util.List;

public abstract class Method<T extends Parameter, E extends Result> {

    @JsonProperty("name")
    protected String name;

    @JsonProperty("parameter")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    protected T parameter;

    @JsonProperty("result")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    protected E result;

    /*public Method(@JsonProperty("name") String name) {
        this.name = name;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getParameter() {
        return parameter;
    }

    public void setParameter(T parameter) {
        this.parameter = parameter;
    }

   /* public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }*/

    public E getResult() {
        return result;
    }

    public void setResult(E result) {
        this.result = result;
    }

    /*public <T> T getParamImpl(Class<T> clazz) {
        for (Parameter param: parameters){
            if (clazz.isInstance(param)) {
                return (T) param;
            }
        }
        return null;
    }

    public <T> T getResultImpl(Class<T> clazz){
        if (result == null){
            throw new NullPointerException("result is null");
        }
        return (T) result;
    }*/
}
