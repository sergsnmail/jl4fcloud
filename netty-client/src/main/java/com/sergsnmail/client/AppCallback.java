package com.sergsnmail.client;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.common.message.common.UserSession;
import javafx.stage.Window;

public interface AppCallback {
    void openMainWindowCallback();
    void closeAppCallback();
    ClientNetwork getNetwork();
    Window getStage();
}
