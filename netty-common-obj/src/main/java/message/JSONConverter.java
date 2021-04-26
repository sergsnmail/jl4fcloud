package message;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONConverter {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static String object2Json(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
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
}
