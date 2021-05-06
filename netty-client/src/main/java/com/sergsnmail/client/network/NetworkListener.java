package com.sergsnmail.client.network;

import com.sergsnmail.common.message.common.Message;

public interface NetworkListener {
    void messageReceive(Message msg);
}
