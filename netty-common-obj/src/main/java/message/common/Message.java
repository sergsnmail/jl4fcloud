package message.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import message.Request;
import message.Response;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Request.class, name = "request"),
        @JsonSubTypes.Type(value = Response.class, name = "response")
})
public abstract class Message {
}
