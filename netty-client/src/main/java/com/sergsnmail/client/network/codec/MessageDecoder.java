package com.sergsnmail.client.network.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import com.sergsnmail.common.json.JSONConverter;
import com.sergsnmail.common.message.common.Message;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder<String> {
    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        String message = msg;
        //System.out.println(message);
        Message inMessage = JSONConverter.Json2Object(message, Message.class);
        out.add(inMessage);
    }
}
