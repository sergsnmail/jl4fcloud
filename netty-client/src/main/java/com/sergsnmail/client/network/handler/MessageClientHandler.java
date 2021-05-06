package com.sergsnmail.client.network.handler;

import com.sergsnmail.client.network.NetworkListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.sergsnmail.common.message.common.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageClientHandler extends SimpleChannelInboundHandler<Message> {

    private List<NetworkListener> listeners = new ArrayList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) throws Exception {
        fireNotify(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void fireNotify(Message msg){
        for (NetworkListener listener: listeners) {
            listener.messageReceive(msg);
        }
    }

    public void addListener(NetworkListener listener){
        this.listeners.add(listener);;
    }

    public void removeListener(NetworkListener mlistener){
        for (NetworkListener listener: listeners){
            if (listener.equals(mlistener)){
                listeners.remove(mlistener);
                break;
            }
        }
    }
}
