package client.network;

import client.network.codec.MessageDecoder;
import client.network.handler.MessageClientHandler;
import client.network.codec.MessageEncoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import message.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ClientNetwork {

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
                //b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        channel = socketChannel;
                        socketChannel.pipeline().addLast(
                                new LineBasedFrameDecoder(5000 * 1024,true,false),
                                new StringDecoder(),
                                new StringEncoder(),
                                new JsonObjectDecoder(),
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
                throw new RuntimeException("Could not connection to server");
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    public void sendCommand(Request msg){
        channel.writeAndFlush(msg);
    }

    public void addChannelListener(NetworkListener listener){
        this.messageHandler.addListener(listener);
    }

    public void removeChannelListener(NetworkListener listener){
        this.messageHandler.removeListener(listener);
    }

    public void close() throws InterruptedException {
        channel.close();
    }
}
