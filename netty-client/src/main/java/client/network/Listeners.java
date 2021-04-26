package client.network;

import message.common.Message;

public interface Listeners {
    void messageReceive(Message msg);
}
