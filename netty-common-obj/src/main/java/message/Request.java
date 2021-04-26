package message;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Message;
import message.common.Parameter;
import message.method.auth.AuthParam;

import java.util.ArrayList;
import java.util.List;

public class Request extends Message {

    @JsonProperty("method")
    private Method method;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public static Request.RequestBuilder builder (){ return new Request.RequestBuilder();}

    public static class RequestBuilder {
        private Method method;
        private List<Parameter> params = new ArrayList<>();

        public Request.RequestBuilder setMethod(Method method){
            if (method == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.method = method;
                return this;
            }
        }

        public Request.RequestBuilder addParameter(Parameter param){
            if (param == null) {
                throw new NullPointerException("param must be non-null but is null");
            } else {
                params.add(param);
                return this;
            }
        }

        public Request build() {
            Request request = new Request();
            request.setMethod(this.method);

            for (Parameter param : this.params) {
                method.getRequest().addParameter(new MethodRequestParameter(param));
            }
            return request;
        }
    }
}
