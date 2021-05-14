package com.sergsnmail.common.message.method.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
public abstract class Metadata {
}
