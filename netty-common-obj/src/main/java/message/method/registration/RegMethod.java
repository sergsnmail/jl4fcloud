package message.method.registration;

import message.common.Method;

public class RegMethod extends Method<RegParam, RegResult> {

    private RegMethod(){
    }

    public static RegisterBuilder builder () { return new RegisterBuilder();}

    public static class RegisterBuilder {

        private RegParam param;
        private RegResult result;

        public RegisterBuilder setParameter(RegParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public RegisterBuilder setResult(RegResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public RegMethod build() {
            RegMethod reg = new RegMethod();
            reg.parameter = this.param;
            reg.result = this.result;
            return reg;
        }

    }
}
