package com.sergsnmail.server;

import com.sergsnmail.server.input.InputParameter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import com.sergsnmail.common.message.common.UserSession;
import com.sergsnmail.server.codec.MessageEncoder;
import com.sergsnmail.server.codec.MessageDecoder;
import com.sergsnmail.server.handler.AuthorizationServerHandler;
import com.sergsnmail.server.handler.MessageServerHandler;

public class AppBootstrap {

    InputParameter appParam;

    public AppBootstrap(InputParameter inputParameter) {
        if (inputParameter == null){
            throw new IllegalArgumentException("missing App parameters");
        }
        this.appParam = inputParameter;
    }

    public void start() throws Exception {
        new Server(this.appParam).addChildHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                UserSession userSession = new UserSession();
                ch.pipeline().addLast(
                        new LengthFieldBasedFrameDecoder(5120 * 1024,0,3,0,3), // new
                        new LengthFieldPrepender(3), // new
                        new MessageDecoder(),
                        new MessageEncoder(),
                        new AuthorizationServerHandler(appParam, userSession),
                        new MessageServerHandler(appParam, userSession));
            }
        }).run();
    }
}
