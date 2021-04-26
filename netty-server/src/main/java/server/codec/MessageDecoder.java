package server.codec;

import message.JSONConverter;
import message.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import message.common.Message;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        String message = (String) msg;
        System.out.println(message);
        Message inMessage = JSONConverter.Json2Object(message, Message.class);
        out.add(inMessage);
    }
}
