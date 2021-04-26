package server.handler;

import input.HandlerParameter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageServerHandler extends ChannelInboundHandlerAdapter {

    private HandlerParameter appParam;
    private final String HANDLER_ID = "Message";

    public MessageServerHandler(HandlerParameter appParam) {
        this.appParam = appParam;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " inactive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(HANDLER_ID + " handler");
        System.out.println(msg.toString());
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
