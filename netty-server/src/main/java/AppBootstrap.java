import input.InputParameter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import server.Server;
import server.codec.MessageEncoder;
import server.codec.MessageDecoder;
import server.handler.AuthorizationServerHandler;
import server.handler.MessageServerHandler;

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
                ch.pipeline().addLast(
                        new StringDecoder(),
                        new JsonObjectDecoder(),
                        new StringEncoder(),
                        new MessageDecoder(),
                        new MessageEncoder(),
                        new AuthorizationServerHandler(),
                        new MessageServerHandler(appParam));
            }
        }).run();
    }
}
