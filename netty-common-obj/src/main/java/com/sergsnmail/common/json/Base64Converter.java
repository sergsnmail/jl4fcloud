package com.sergsnmail.common.json;

import java.util.Base64;

public class Base64Converter {

    public static byte[] decodeBase64ToByte(String encodedContent) {
        return Base64.getDecoder().decode(encodedContent);
    }

    public static String encodeByteToBase64Str(byte[] decodedContent) {
        return Base64.getEncoder().encodeToString(decodedContent);
    }

    public static byte[] encodeByteToBase64Byte(byte[] decodedContent) {
        return Base64.getEncoder().encode(decodedContent);
    }

}
