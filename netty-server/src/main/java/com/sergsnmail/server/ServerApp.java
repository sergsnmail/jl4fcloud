package com.sergsnmail.server;

import com.sergsnmail.server.input.InputParameter;

public class ServerApp {
    private static final String STORAGE_LOCATION = "E:\\temp\\storage";
    public static void main(String[] args) {
        InputParameter inputParameter = new InputParameter();
        try {
            inputParameter.setLocation(STORAGE_LOCATION).setPort(8989);
            new AppBootstrap(inputParameter).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
