package client;

import message.common.UserSession;

public interface Callable {
    void openMainWindowCallback(UserSession session);
    void closeAppCallback();
}
