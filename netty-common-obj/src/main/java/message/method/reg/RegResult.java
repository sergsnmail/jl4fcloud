package message.method.reg;

import com.fasterxml.jackson.annotation.JsonProperty;
import message.common.Result;

public class RegResult extends Result {

    @JsonProperty("isAuth")
    private boolean isAuth;

    @JsonProperty("isRegistered")
    private boolean isRegistered;

    @JsonProperty("message")
    private String message;

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
