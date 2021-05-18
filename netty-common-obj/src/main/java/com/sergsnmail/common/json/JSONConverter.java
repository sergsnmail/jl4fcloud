package com.sergsnmail.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONConverter {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static String object2StringJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj); //+ "\r\n";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] object2ByteJson(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj); //+ "\r\n";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> T Json2Object(String jsonInString, Class<T> clazz) {
        try {
            return (T) mapper.readValue(jsonInString, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T Json2Object(byte[] jsonInByte, Class<T> clazz) {
        try {
            return mapper.readValue(jsonInByte, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
