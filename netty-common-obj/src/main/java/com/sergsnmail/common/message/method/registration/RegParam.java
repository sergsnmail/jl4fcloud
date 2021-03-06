package com.sergsnmail.common.message.method.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Parameter;

public class RegParam extends Parameter {

    @JsonProperty("username")
    private final String username;

    @JsonProperty("password")
    private final String password;

    public RegParam(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
