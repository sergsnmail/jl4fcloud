package com.sergsnmail.common.network;

import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.common.Message;

public interface Network {
    void sendCommand(Message msg);
    void addChannelListener(NetworkListener listener);
    void removeChannelListener(NetworkListener listener);
    void close();
}
