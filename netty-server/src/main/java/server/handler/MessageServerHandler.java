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
import message.Base64Converter;
import message.Request;
import message.Response;
import message.common.Message;
import message.common.UserSession;
import message.method.getuserfile.GetFilesMethod;
import message.method.getuserfile.GetFilesResult;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;
import message.method.putfile.PutFilesResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.UUID;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MessageServerHandler extends SimpleChannelInboundHandler<Message> {

    private HandlerParameter appParam;
    private final String HANDLER_ID = "Message";
    private ChannelHandlerContext ctx;
    private UserServiceImpl userService;
    private FileServiceImpl fileService;
    private UserSession userSession;
    private Map<String, String> transfer = new HashMap<>();

    public MessageServerHandler(HandlerParameter appParam, UserSession userSession) {
        this.appParam = appParam;
        this.userSession = userSession;

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
            GetFilesResult result = new GetFilesResult();

            User user = userService.getUser(userSession.getUsername());
            if (user == null){
                throw new IllegalArgumentException("User error");
            }

            List<StorageFile> files = fileService.getFiles(user);
            List<String> filename = files.stream()
                    .map(storageFile -> storageFile.getUser_location() + "\\" + storageFile.getFile_name())
                    .collect(Collectors.toList());

            result.setFiles(filename);
            method.setResult(result);
            Response response = Response.builder()
                    .setMethod(method)
                    .build();

            this.ctx.writeAndFlush(response);
        }

        if (request.getMethod() instanceof PutFilesMethod){
            PutFilesMethod method = (PutFilesMethod) request.getMethod();
            PutFilesParam param = method.getParameter();
            if (param != null && !param.getBody().isEmpty()){
                try {
                    String pId = param.getPackageId();

                    String transferFile = transfer.getOrDefault(pId, appParam.getLocation() + "\\" + getUploadFilePath());
                    transfer.put(pId, transferFile);

                    Files.createDirectories(Paths.get(transferFile).getParent());
                    try(OutputStream writer = new BufferedOutputStream(Files.newOutputStream(Paths.get(transferFile), CREATE, APPEND))){
                        byte[] data = Base64Converter.decodeBase64ToByte(param.getBody());
                        writer.write(data, 0 , data.length);
                        writer.flush();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }

                    if (param.getPartNumber() == param.getTotalNumber()){
                        transfer.remove(pId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            param.setBody(null);
            PutFilesResult result = new PutFilesResult();
            result.setStatus("1");

            method.setResult(result);
            Response response = Response.builder()
                    .setMethod(method)
                    .build();

            this.ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String getUploadFilePath(){
        String uploadFileName = UUID.randomUUID().toString();

        /**
         * берем первые 6 символов имени файла
         */
        String pathSegment = uploadFileName.substring(0,6);

        StringBuilder sb = new StringBuilder(pathSegment);
        sb.insert(2, '\\').insert(5,'\\');
        return sb.toString() + '\\' + uploadFileName;
    }
}
