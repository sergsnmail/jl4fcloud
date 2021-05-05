import input.InputParameter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import message.common.UserSession;
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
                UserSession userSession = new UserSession();
                ch.pipeline().addLast(
                        new LineBasedFrameDecoder(5000 * 1024,true,false),
                        new StringDecoder(),
                        new StringEncoder(),
                        new JsonObjectDecoder(),
                        new MessageDecoder(),
                        new MessageEncoder(),
                        new AuthorizationServerHandler(userSession),
                        new MessageServerHandler(appParam, userSession));
            }
        }).run();
    }
}
