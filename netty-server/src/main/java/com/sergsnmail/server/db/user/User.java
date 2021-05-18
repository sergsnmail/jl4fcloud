package com.sergsnmail.server.db.user;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {
    @NonNull
    private int userId;
    @NonNull
    private String created_at;
    @NonNull
    private String updated_at;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String hashedPass;
}
