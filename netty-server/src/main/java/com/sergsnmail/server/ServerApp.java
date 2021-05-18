package com.sergsnmail.server;

import com.sergsnmail.server.input.InputParameter;

/**
 * Параметры запуска
 * Порт: -port=8989
 * Папка хранения файлов: -storage=e:\temp\storage
 * Путь до БД: -db=e:\temp\cloud.db
 */
public class ServerApp {
    private static final String DEFAULT_STORAGE_LOCATION = "E:\\temp\\storage";
    private static final int DEFAULT_PORT = 8989;
    private static final String DEFAULT_DATABASE = "c:\\temp\\storage.db";

    private static int port;
    private static String storage;
    private static String database;

    public static void main(String[] args) {

        port = DEFAULT_PORT;
        storage = DEFAULT_STORAGE_LOCATION;
        database = DEFAULT_DATABASE;

        if (args.length > 0){

            try{
                int portFromArgs = getPort(args);
                if (portFromArgs > 0){
                    port = portFromArgs;
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            try{
                String storageFromArgs = getStorage(args);
                if (!storageFromArgs.isEmpty()){
                    storage = storageFromArgs;
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            try{
                String dbLocation = getDb(args);
                if (!dbLocation.isEmpty()){
                    database = dbLocation;
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

        }
        InputParameter inputParameter = new InputParameter();
        try {
            inputParameter
                    .setLocation(storage)
                    .setPort(port)
                    .setDbLocation(database);
            new AppBootstrap(inputParameter).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static String getStorage(String[] args) throws IllegalArgumentException {
        String argStr = getParamStr("-storage", args);
        return getValue(argStr);
    }

    private static int getPort(String[] args) throws IllegalArgumentException {
        String argStr = getParamStr("-port", args);
        return Integer.parseInt(getValue(argStr));
    }

    private static String getDb(String[] args) throws IllegalArgumentException {
        String argStr = getParamStr("-db", args);
        return getValue(argStr);
    }

    private static String getParamStr(String param, String[] args) throws IllegalArgumentException{
        for (String arg : args) {
            if (arg.indexOf(param) != -1 ){
                return arg;
            }
        }
        throw new IllegalArgumentException(String.format("No such startup parameter (%s), using default value", param));
    }

    private static String getValue(String argStr) throws IllegalArgumentException{
        String[] argVal= argStr.split("=");
        if (argVal.length == 2){
            return argVal[1];
        }
        throw new IllegalArgumentException("Could not parse param value");
    }
}
