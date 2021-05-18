package com.sergsnmail.common.message.method.transferfile;

import com.sergsnmail.common.message.common.Method;

public class DownloadFile extends Method<DownloadFileParam, DownloadFileResult> {
    private DownloadFile(){
    }

    public static DownloadFileBuilder builder () { return new DownloadFileBuilder();}

    public static class DownloadFileBuilder {

        private DownloadFileParam param;
        private DownloadFileResult result;

        public DownloadFileBuilder setParameter(DownloadFileParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public DownloadFileBuilder setResult(DownloadFileResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public DownloadFile build() {
            DownloadFile method = new DownloadFile();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }

    }
}
