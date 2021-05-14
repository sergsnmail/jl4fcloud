package com.sergsnmail.client;

import com.sergsnmail.client.network.ClientNetwork;
import com.sergsnmail.common.message.common.UserSession;
import com.sergsnmail.common.network.Network;
import javafx.stage.Window;

public interface AppCallback {
    void openMainWindowCallback();
    void closeAppCallback();
    Network getNetwork();
    Window getStage();
}
