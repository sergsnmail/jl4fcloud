package com.sergsnmail.server.db.file;

import com.sergsnmail.server.db.user.User;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class StorageFile {

    @NonNull
    private User user;

    //@NonNull
    private int id;

    @NonNull
    private String created_at;

    @NonNull
    private String modified_at;

    @NonNull
    private String file_name;

    @NonNull
    private String user_location;

    @NonNull
    private String storage;
}
