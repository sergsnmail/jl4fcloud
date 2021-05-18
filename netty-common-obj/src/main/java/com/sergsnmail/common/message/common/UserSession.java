package com.sergsnmail.common.message.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSession {

    @JsonProperty("userid")
    private int userid;


    @JsonProperty("username")
    private String username;

    public UserSession(){}

    public UserSession(@JsonProperty("userid") int userid, @JsonProperty("username") String username) {
        this.userid = userid;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }
}
