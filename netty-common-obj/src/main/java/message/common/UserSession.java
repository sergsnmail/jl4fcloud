package message.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSession {

    @JsonProperty("username")
    private String username;

    public UserSession(@JsonProperty("username") String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
