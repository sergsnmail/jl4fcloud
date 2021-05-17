package com.sergsnmail.client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * Параметры запуска
 * Порт: -port=8989
 * Имя сервера: -host=localhost
 */

public class ClientApp extends Application {
    
    private static final int DEFAULT_PORT=8989;
    private static final String DEFAULT_HOST = "localhost";

    private String host;
    private int port;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parameters params = getParameters();
        host = getStringParamValue("-host", params);
        if (host.isEmpty()){
            host = DEFAULT_HOST;
        }
        port = getIntegerParamValue("-port", params);
        if (port <= 0){
            port = DEFAULT_PORT;
        }
        new AppController(primaryStage, port, host).show();
    }

    private int getIntegerParamValue(String paramName, Parameters params) {
        String res;
        if (!(res = getParamValue(paramName,params)).isEmpty()){
            return Integer.parseInt(res);
        }
        return 0;
    }

    private String getStringParamValue(String paramName, Parameters params){
        return getParamValue(paramName, params);
    }

    private String getParamValue(String paramName, Parameters params){
        List<String> args = params.getRaw();
        for(String arg : args){
            if (arg.indexOf(paramName) != -1 ){
                return getValue(arg);
            }
        }
        return "";
    }

    private static String getValue(String argStr) throws IllegalArgumentException{
        String[] argVal= argStr.split("=");
        if (argVal.length == 2){
            return argVal[1];
        }
        throw new IllegalArgumentException("Could not parse param value");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
