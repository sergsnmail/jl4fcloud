package com.sergsnmail.server;

import com.sergsnmail.server.input.ServerParameter;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    private int port;
    private ServerParameter parameter;
    private ChannelHandler channelInitializer;

    public Server(ServerParameter inputParam) {
        this.port = inputParam.getPort();
        this.parameter = inputParam;
    }

    public Server addChildHandler(ChannelInitializer<?> channelInitializer) {
        this.channelInitializer = channelInitializer;
        return this;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            System.out.printf("The server is started with the following parameters:\n" +
                    "Port: %d\n" +
                    "Storage: %s\n", parameter.getPort(), parameter.getStorage());
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
