package server.codec;

import message.JSONConverter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import message.common.Message;

import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List out) throws Exception {
        out.add(JSONConverter.object2Json(msg));
    }
}
