package com.sergsnmail.client.network.codec;

import com.sergsnmail.common.json.JSONConverter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import com.sergsnmail.common.message.common.Message;

import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        String jsonMessage = JSONConverter.object2Json(msg);
        //System.out.println(jsonMessage);
        out.add(jsonMessage);
    }
}
