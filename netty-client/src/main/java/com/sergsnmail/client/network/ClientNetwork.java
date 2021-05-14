package com.sergsnmail.client.network;

import com.sergsnmail.client.network.codec.MessageDecoder;
import com.sergsnmail.client.network.handler.MessageClientHandler;
import com.sergsnmail.client.network.codec.MessageEncoder;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.network.NetworkListener;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import com.sergsnmail.common.message.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientNetwork implements Network {

    private SocketChannel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 8989;

    private MessageClientHandler messageHandler = new MessageClientHandler();

    public ClientNetwork() {
        new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        channel = socketChannel;
                        socketChannel.pipeline().addLast(
                                new LengthFieldBasedFrameDecoder(5120 * 1024,0,3,0,3), // new
                                new LengthFieldPrepender(3),
                                new MessageDecoder(),
                                new MessageEncoder(),
                                messageHandler);
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(HOST, PORT).sync();
                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Could not connection to com.sergsnmail.server");
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
    @Override
    public void sendCommand(Message msg){
        channel.writeAndFlush(msg);
    }

    @Override
    public void addChannelListener(NetworkListener listener){
        this.messageHandler.addListener(listener);
    }

    @Override
    public void removeChannelListener(NetworkListener listener){
        this.messageHandler.removeListener(listener);
    }

    @Override
    public void close() {
        channel.close();
        System.out.println("channel close");
    }
}
