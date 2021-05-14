package com.sergsnmail.common.message.method.transferfile;

import com.sergsnmail.common.message.common.Method;

public class UploadFilesMethod extends Method<TransferFilesParam, TransferFilesResult> {

    private UploadFilesMethod(){
    }

    public static UploadFilesBuilder builder () { return new UploadFilesBuilder();}

    public static class UploadFilesBuilder {

        private TransferFilesParam param;
        private TransferFilesResult result;

        public UploadFilesBuilder setParameter(TransferFilesParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public UploadFilesBuilder setResult(TransferFilesResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public UploadFilesMethod build() {
            UploadFilesMethod method = new UploadFilesMethod();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }

    }

}
