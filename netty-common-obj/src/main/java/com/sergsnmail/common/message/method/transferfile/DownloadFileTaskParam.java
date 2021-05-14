package com.sergsnmail.common.message.method.transferfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sergsnmail.common.message.common.Parameter;

public class DownloadFileTaskParam extends Parameter {

    @JsonProperty("taskQuery")
    private String taskQuery;

    public String getTaskQuery() {
        return taskQuery;
    }

    public void setTaskQuery(String taskQuery) {
        this.taskQuery = taskQuery;
    }
}
