package com.sergsnmail.server.db.user;

public interface UserDataSource {
    User getUser(String username);
    boolean createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(User user);
}
