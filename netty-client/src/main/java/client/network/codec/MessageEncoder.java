package client.network.codec;

import message.JSONConverter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import message.common.Message;

import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        String jsonMessage = JSONConverter.object2Json(msg);
        //System.out.println(jsonMessage);
        out.add(jsonMessage);
    }
}
