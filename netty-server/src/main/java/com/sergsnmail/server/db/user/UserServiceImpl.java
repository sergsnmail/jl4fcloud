package com.sergsnmail.server.db.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserServiceImpl {

    private static final String SECRET_SALT = "PASSWORD_SALT";
    UserDataSource userDS;

    public UserServiceImpl(UserDataSource userStorage){
        this.userDS = userStorage;
    }

    public User registerUser(String username, String email, String pass) {
        if (username == null || username.equals("")) {
            throw new IllegalArgumentException();
        }

        if (email == null || email.equals("")) {
            throw new IllegalArgumentException();
        }

        if (pass == null || pass.equals("")) {
            throw new IllegalArgumentException();
        }

        User newUser = User.builder().username(username)
                    .email(email)
                    .hashedPass(getHashedPassword(pass))
                    .build();
        try{
           userDS.createUser(newUser);
        }catch (Exception e){
            newUser = null;
            e.printStackTrace();
        }
        return newUser;
    }

    public boolean authorizedUser(String username, String pass){
        if (username == null || username.equals("")) {
            throw new IllegalArgumentException();
        }
        User verifiedUser = userDS.getUser(username);
        if (verifiedUser != null) {
            return verifiedUser.getHashedPass().equals(getHashedPassword(pass));
        }
        return false;
    }

    private String getHashedPassword(String plainPassword) {
        String hashedPassword = null;
        String passwordWithSalt = plainPassword + SECRET_SALT;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hashedPassword = bytesToHex(md.digest(passwordWithSalt.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public User getUser(String username) {
        return userDS.getUser(username);
    }

    public boolean createUser(User user) {
        return userDS.createUser(user);
    }

    public boolean updateUser(User user) {
        return userDS.updateUser(user);
    }

    public boolean deleteUser(User user) {
        return userDS.deleteUser(user);
    }
}
