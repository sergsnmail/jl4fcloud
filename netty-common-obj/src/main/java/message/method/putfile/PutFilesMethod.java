package message.method.putfile;

import message.common.Method;

public class PutFilesMethod extends Method<PutFilesParam, PutFilesResult> {

    private PutFilesMethod(){
    }

    public static PutFilesMethod.PutFilesBuilder builder () { return new PutFilesMethod.PutFilesBuilder();}

    public static class PutFilesBuilder {

        private PutFilesParam param;
        private PutFilesResult result;

        public PutFilesMethod.PutFilesBuilder setParameter(PutFilesParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public PutFilesMethod.PutFilesBuilder setResult(PutFilesResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public PutFilesMethod build() {
            PutFilesMethod method = new PutFilesMethod();
            method.parameter = this.param;
            method.result = this.result;
            return method;
        }

    }

}
