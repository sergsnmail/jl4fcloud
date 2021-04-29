package message.method.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.common.Result;
import message.common.UserSession;

public class AuthResult extends Result {

    @JsonProperty("isAuth")
    private boolean isAuth;

    @JsonProperty("message")
    private String message;

    @JsonProperty("session")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private UserSession session;

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean authState) {
        this.isAuth = authState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }
}
