package com.sergsnmail.common.message.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Request.class, name = "request"),
        @JsonSubTypes.Type(value = Response.class, name = "response")
})
public abstract class Message {
}
