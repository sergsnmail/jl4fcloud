package message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Message;
import message.common.Method;
import message.common.Result;

public class Response extends Message {

    @JsonProperty("method")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private Method method;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public static Response.ResponseBuilder builder() {
        return new Response.ResponseBuilder();
    }

    public static class ResponseBuilder {

        private Method method;
        //private Result methodResult;

        public Response.ResponseBuilder setMethod(Method method){
            if (method == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.method = method;
                return this;
            }
        }

        public Response build() {
            Response resp = new Response();
            resp.setMethod(method);
            return resp;
        }


        /*public Response.ResponseBuilder setMethodResult(Result methodResult){
            if (methodResult == null) {
                throw new NullPointerException("result must be non-null but is null");
            } else {
                this.methodResult = methodResult;
                return this;
            }
        }

        public Response build() {
            MethodResponseResult methodResponseResult = new MethodResponseResult();
            methodResponseResult.setResultImpl(methodResult);
            method.getResult().setResult(methodResponseResult);
            Response resp = new Response();
            resp.setMethod(method);

            return resp;
        }*/

    }
}
