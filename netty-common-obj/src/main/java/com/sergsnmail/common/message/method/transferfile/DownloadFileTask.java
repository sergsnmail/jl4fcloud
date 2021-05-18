package com.sergsnmail.common.message.method.transferfile;

import com.sergsnmail.common.message.common.Method;

public class DownloadFileTask extends Method<DownloadFileTaskParam, DownloadFileTaskResult> {

    private DownloadFileTask(){
    }

    public static DownloadFileTaskBuilder builder () { return new DownloadFileTaskBuilder();}

    public static class DownloadFileTaskBuilder {

        private DownloadFileTaskParam param;
        private DownloadFileTaskResult result;

        public DownloadFileTaskBuilder setParameter(DownloadFileTaskParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public DownloadFileTaskBuilder setResult(DownloadFileTaskResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public DownloadFileTask build() {
            DownloadFileTask method = new DownloadFileTask();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }

    }

}
