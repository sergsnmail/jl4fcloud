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
import message.method.getuserfile.GetFilesParam;
import message.method.getuserfile.GetFilesResult;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;
import message.method.putfile.PutFilesResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
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
            //System.out.printf("package file %d of %d received\n",param.getPartNumber(), param.getTotalNumber());
            if (!param.getBody().isEmpty()){
                try {
                    Path newFile = Files.createFile(Paths.get(appParam.getLocation() +
                            "\\" + param.getFilename() +
                            "__" + param.getPartNumber()));
                    //Files.write(newFile, Base64Converter.decodeBase64ToByte(param.getBody()));

                    try(OutputStream writer = Files.newOutputStream(newFile)){
                        writer.write(Base64Converter.decodeBase64ToByte(param.getBody()));
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            param.setBody(null);
            result.setStatus("1");
           /* if (param.getPartNumber()==99999){
                System.out.println("stop");
            }*/

            method.setResult(result);
            Response response = Response.builder()
                    .setMethod(method)
                    .build();

            this.ctx.writeAndFlush(response);
            //decodeBase64ToFile(appParam.getLocation() + "\\" + param.getFilename(), param.getBody());
        }
    }

    private static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    private void decodeBase64ToFile(String filePathName, String fileEncodedContent) {
        try {
            byte[] fileContent = Base64.getDecoder().decode(fileEncodedContent);
            try (FileOutputStream fos = new FileOutputStream(filePathName)) {
                fos.write(fileContent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not create file " + filePathName, e);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
