package com.sergsnmail.common.message.method.getfileinfo;

import com.sergsnmail.common.message.common.Method;


public class GetFileInfo extends Method<FileInfoParam, FileInfoResult> {

    public static GetFileInfo.GetFileInfoBuilder builder () { return new GetFileInfo.GetFileInfoBuilder();}

    public static class GetFileInfoBuilder {
        private FileInfoParam param;
        private FileInfoResult result;

        public GetFileInfo.GetFileInfoBuilder setParameter(FileInfoParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public GetFileInfo.GetFileInfoBuilder setResult(FileInfoResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public GetFileInfo build() {
            GetFileInfo method = new GetFileInfo();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }
    }
}
