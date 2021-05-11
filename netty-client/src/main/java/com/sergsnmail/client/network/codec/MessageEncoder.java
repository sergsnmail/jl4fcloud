package com.sergsnmail.client.network.codec;

import com.sergsnmail.common.json.JSONConverter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import com.sergsnmail.common.message.common.Message;

import java.util.List;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeBytes(JSONConverter.object2ByteJson(msg));
    }
}
