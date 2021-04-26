package message.method.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Result;

public class AuthResult extends Result {

    @JsonProperty("isAuth")
    private boolean isAuth;

    @JsonProperty("message")
    private String message;

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
