package message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Message;
import message.common.Method;
import message.common.Parameter;
import message.common.UserSession;

import java.util.ArrayList;
import java.util.List;

public class Request extends Message {

    @JsonProperty("session")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private UserSession session;

    @JsonProperty("method")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private Method method;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public static Request.RequestBuilder builder (){ return new Request.RequestBuilder();}

    public static class RequestBuilder {

        private Method method;
        //private List<Parameter> params = new ArrayList<>();
        private UserSession session;

        public Request.RequestBuilder setMethod(Method method){
            if (method == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.method = method;
                return this;
            }
        }

/*        public Request.RequestBuilder addParameter(Parameter param){
            if (param == null) {
                throw new NullPointerException("param must be non-null but is null");
            } else {
                params.add(param);
                return this;
            }
        }
*/
        public Request.RequestBuilder setSession(UserSession session){
            if (session == null) {
                throw new IllegalArgumentException("User session not initialized");
            } else {
                this.session = session;
                return this;
            }
        }

        public Request build() {
            Request request = new Request();
            request.setMethod(this.method);
            request.setSession(this.session);
            /*for (Parameter param : this.params) {
                method.getParameter().addParameter(new MethodRequestParameter(param));
            }*/
            return request;
        }
    }
}
