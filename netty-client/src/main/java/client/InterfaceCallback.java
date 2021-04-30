package client;

import message.common.UserSession;

public interface InterfaceCallback {
    void openMainWindowCallback(UserSession session);
    void closeAppCallback();
}
