package com.sergsnmail.server.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputParameter implements ServerParameter{

    private int DEFAULT_PORT = 8989;
    private String location;
    private int port;
    private String dbLocation;

    public InputParameter setLocation(String location) throws IOException {
        if (location == null){
            throw new NullPointerException("Location parameter not set");
        }
        Path locPath = Paths.get(location);
        if (!Files.exists(locPath)){
            Files.createDirectories(locPath);
        }
        this.location = location;
        return this;
    }

    public InputParameter setPort(int port) {
        this.port = port;
        return this;
    }

    public InputParameter setDbLocation(String dbLocation) throws IOException {
        Path dbPath = Paths.get(dbLocation);
        if (!Files.exists(dbPath)){
            Files.createDirectories(dbPath.getParent());
        }
        this.dbLocation = dbLocation;
        return this;
    }

    @Override
    public int getPort() {
        return port == 0 ?DEFAULT_PORT:port;
    }

    @Override
    public String getStorageRootDir() {
        return location;
    }

    @Override
    public String getDbLocation() {
        return dbLocation;
    }

}
