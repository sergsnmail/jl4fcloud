package message;

import java.util.Base64;

public class Base64Converter {

    public static byte[] decodeBase64ToByte(String encodedContent) {
        return Base64.getDecoder().decode(encodedContent);
    }

    public static String encodeByteToBase64(byte[] decodedContent) {
        return Base64.getEncoder().encodeToString(decodedContent);
    }
}
