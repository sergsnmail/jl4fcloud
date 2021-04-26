package server;

import input.ServerParameter;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    private int port;
    private ChannelHandler channelInitializer;

    public Server(ServerParameter inputParam) {
        this.port = inputParam.getPort();
    }

    public Server addChildHandler(ChannelInitializer<?> channelInitializer) {
        this.channelInitializer = channelInitializer;
        return this;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(channelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /*public static void main(String[] args) throws Exception {
        input.InputParameter inputParameter = new input.InputParameter();
        try {
            inputParameter.setLocation("E:/temp").setPort(8989);

            //server.DiscardServer server = new server.DiscardServer(inputParameter);
            new AppBootstrap(inputParameter).start();
            //server.run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }*/
}
