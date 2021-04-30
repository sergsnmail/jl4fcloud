package server.codec;

import message.JSONConverter;
import message.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import message.common.Message;

import java.util.List;

public class MessageDecoder extends MessageToMessageDecoder {

    StringBuilder inputString = new StringBuilder();

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        String message = (String) msg;
        inputString.append(message);
        //System.out.println(message);
        Message objMessage = JSONConverter.Json2Object(inputString.toString(), Message.class);
        if (objMessage != null) {
            inputString.setLength(0);
            out.add(objMessage);
        }
    }
}
