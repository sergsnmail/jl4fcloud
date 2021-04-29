package server.handler;

import db.DbStorage;
import db.file.FileDataSourceImpl;
import db.file.FileServiceImpl;
import db.file.StorageFile;
import db.user.User;
import db.user.UserDataSourceImpl;
import db.user.UserServiceImpl;
import input.HandlerParameter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.Request;
import message.Response;
import message.common.Message;
import message.common.UserSession;
import message.method.getuserfile.GetFilesMethod;
import message.method.getuserfile.GetFilesParam;
import message.method.getuserfile.GetFilesResult;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;
import message.method.putfile.PutFilesResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Base64.Decoder;

public class MessageServerHandler extends SimpleChannelInboundHandler<Message> {

    private HandlerParameter appParam;
    private final String HANDLER_ID = "Message";
    private ChannelHandlerContext ctx;
    private UserServiceImpl userService;
    private FileServiceImpl fileService;

    public MessageServerHandler(HandlerParameter appParam) {
        this.appParam = appParam;

        DbStorage db= new DbStorage();
        this.userService = new UserServiceImpl(new UserDataSourceImpl(db));
        this.fileService = new FileServiceImpl(new FileDataSourceImpl(db));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " active");
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(HANDLER_ID + " inactive");
    }

    /*@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(HANDLER_ID + " handler");
        System.out.println(msg.toString());
        ctx.writeAndFlush(msg);
    }*/

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        this.ctx = channelHandlerContext;
        if (message instanceof Request){
            handleRequest((Request)message);
        }
        if (message instanceof Response){
            handleResponse((Response) message);
        }
    }

    private void handleResponse(Response response) {
    }

    private void handleRequest(Request request) {
        if (request.getMethod() instanceof GetFilesMethod){
            GetFilesMethod method = (GetFilesMethod) request.getMethod();
            GetFilesParam param = method.getParameter();;
            GetFilesResult result = new GetFilesResult();
            UserSession session = request.getSession();

            User user = userService.getUser(session.getUsername());
            if (user == null){
                throw new IllegalArgumentException("User error");
            }

            List<StorageFile> files = fileService.getFiles(user);
            List<String> filename = files.stream()
                    .map(storageFile -> storageFile.getUser_location() + "\\" + storageFile.getFile_name())
                    .collect(Collectors.toList());

            result.setFiles(filename);
            Response response = Response.builder()
                    .setMethod(request.getMethod())
                    .build();
            this.ctx.writeAndFlush(response);
        }

        if (request.getMethod() instanceof PutFilesMethod){
            PutFilesMethod method = (PutFilesMethod) request.getMethod();
            PutFilesParam param = method.getParameter();
            PutFilesResult result = new PutFilesResult();

            /*byte[] base64Decoded = DatatypeConverter.parseBase64Binary(param.getBody());*/
            System.out.println("Encoded file in Json:\n");
            System.out.println(param.getBody());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
