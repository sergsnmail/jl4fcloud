package com.sergsnmail.common.message.method.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.common.Result;
import com.sergsnmail.common.message.common.UserSession;

public class RegResult extends Result {

    @JsonProperty("isAuth")
    private boolean isAuth;

    @JsonProperty("isRegistered")
    private boolean isRegistered;

    @JsonProperty("com/sergsnmail/common/message")
    private String message;

    @JsonProperty("session")
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
    private UserSession session;


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

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }
}
