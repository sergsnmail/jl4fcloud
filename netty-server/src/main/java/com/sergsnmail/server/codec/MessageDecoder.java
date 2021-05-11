package com.sergsnmail.server.codec;

import com.sergsnmail.common.json.JSONConverter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import com.sergsnmail.common.message.common.Message;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(JSONConverter.Json2Object(ByteBufUtil.getBytes(msg), Message.class));
    }
}
