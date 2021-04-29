package message.method.auth;

import message.common.Method;

public class AuthMethod extends Method<AuthParam, AuthResult> {

    private AuthMethod(){
    }

    public static AuthMethod.AuthBuilder builder () { return new AuthMethod.AuthBuilder();}

    public static class AuthBuilder {

        private AuthParam param;
        private AuthResult result;

        public AuthBuilder setParameter(AuthParam param){
            if (param == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.param = param;
                return this;
            }
        }

        public AuthBuilder setResult(AuthResult result){
            if (result == null) {
                throw new NullPointerException("method must be non-null but is null");
            } else {
                this.result = result;
                return this;
            }
        }

        public AuthMethod build() {
            AuthMethod auth = new AuthMethod();
            auth.parameter = this.param;
            auth.result = this.result;
            return auth;
        }

    }
}
