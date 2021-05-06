package com.sergsnmail.common.message.method.getfile;

import com.sergsnmail.common.message.common.Method;

public class GetFilesMethod extends Method<GetFilesParam, GetFilesResult> {

    private GetFilesMethod(){
    }

    public static GetFilesMethod.GetFilesBuilder builder () { return new GetFilesMethod.GetFilesBuilder();}

    public static class GetFilesBuilder {

        private GetFilesParam param;
        private GetFilesResult result;

        public GetFilesMethod.GetFilesBuilder setParameter(GetFilesParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public GetFilesMethod.GetFilesBuilder setResult(GetFilesResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public GetFilesMethod build() {
            GetFilesMethod method = new GetFilesMethod();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }

    }

}
