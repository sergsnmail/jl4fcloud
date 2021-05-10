package com.sergsnmail.client.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import com.sergsnmail.common.json.JSONConverter;
import com.sergsnmail.common.message.common.Message;

import java.nio.ByteBuffer;
import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {//MessageToMessageDecoder<String> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(JSONConverter.Json2Object(ByteBufUtil.getBytes(msg), Message.class));
    }
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        ByteBuf buf = in;
//        try {
//            out.add(JSONConverter.Json2Object(ByteBufUtil.getBytes(buf.readBytes(in.readableBytes())), Message.class));
//        }finally {
//            buf.release();
//        }
//    }

//    @Override
//    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
//        //String message = msg;
//        //Message inMessage = JSONConverter.Json2Object(message, Message.class);
//        out.add(JSONConverter.Json2Object(msg, Message.class));
//    }
}
