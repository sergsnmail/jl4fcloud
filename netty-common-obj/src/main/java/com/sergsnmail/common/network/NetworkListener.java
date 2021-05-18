package com.sergsnmail.common.network;

import com.sergsnmail.common.message.common.Message;

public interface NetworkListener {
    void messageReceive(Message msg);
}
