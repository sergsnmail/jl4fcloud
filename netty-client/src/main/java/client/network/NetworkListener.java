package client.network;

import message.common.Message;

public interface NetworkListener {
    void messageReceive(Message msg);
}
