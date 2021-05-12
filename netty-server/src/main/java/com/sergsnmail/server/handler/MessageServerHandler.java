package com.sergsnmail.server.handler;

import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoParam;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoResult;
import com.sergsnmail.common.message.method.getfileinfo.GetFileInfo;
import com.sergsnmail.server.db.DbStorage;
import com.sergsnmail.server.db.file.FileDataSourceImpl;
import com.sergsnmail.server.db.file.FileServiceImpl;
import com.sergsnmail.server.db.file.StorageFile;
import com.sergsnmail.server.db.user.User;
import com.sergsnmail.server.db.user.UserDataSourceImpl;
import com.sergsnmail.server.db.user.UserServiceImpl;
import com.sergsnmail.server.input.HandlerParameter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.common.UserSession;
import com.sergsnmail.common.message.method.getfile.GetFilesMethod;
import com.sergsnmail.common.message.method.getfile.GetFilesResult;
import com.sergsnmail.common.message.method.putfile.PutFilesMethod;
import com.sergsnmail.common.message.method.putfile.PutFilesParam;
import com.sergsnmail.common.message.method.putfile.PutFilesResult;

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

        /**
         * Обработка запроса на получения списка файлов пользователя
         */
        if (request.getMethod() instanceof GetFilesMethod){
            getFilesHandler(request);
        }

        /**
         * Обработка запроса инофрмации по переданному файлу
         */
        if (request.getMethod() instanceof GetFileInfo){
            getFileInfoHandler(request);
        }

        /**
         * Обработка загрузки пакетов файлов
         */
        if (request.getMethod() instanceof PutFilesMethod){
            PutFilesMethod method = (PutFilesMethod) request.getMethod();
            PutFilesParam param = method.getParameter();
            if (param != null && !param.getBody().isEmpty()){
                try {
                    //System.out.printf("id: %s, %d/%d [%s]%n ", param.getPackageId(), param.getPartNumber(), param.getTotalNumber(),param.getMetadata().getFileName());
                    String pId = param.getPackageId();
                    String transferFile = transfer.get(pId);
                    if (transferFile == null){
                        transferFile = getStorageFilePath(param);
                        transfer.put(pId, transferFile);
                    }

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

    /**
     * Обработчик запроса списка файлов
     * @param request
     */
    private void getFilesHandler(Request request) {
        GetFilesMethod method = (GetFilesMethod) request.getMethod();
        GetFilesResult result = new GetFilesResult();

        User user = userService.getUser(userSession.getUsername());
        if (user == null){
            throw new IllegalArgumentException("User error");
        }

        List<StorageFile> files = fileService.getAllFiles(user);
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

    /**
     * Обработчик запроса получения информации по файлу
     * @param request
     */
    private void getFileInfoHandler(Request request) {
        GetFileInfo method = (GetFileInfo) request.getMethod();
        FileInfoParam param = method.getParameter();
        if (param != null){
            User user = userService.getUser(userSession.getUsername());
            String uploadedFileName = param.getMetadata().getFileName();
            String uploadedFilePath = param.getMetadata().getFileRelativePath();
            System.out.printf("[DEBUG] GET_FILE_INFO uploadedFileName=%s uploadedFilePath=%s\n %s\n",uploadedFileName,uploadedFilePath, param.getMetadata());

            List<StorageFile> files = fileService.getFiles(user, uploadedFileName, uploadedFilePath);

            if (files != null && files.size() == 1){
                FileMetadata metadata = new FileMetadata();
                metadata.setFileName(param.getMetadata().getFileName());
                metadata.setFilePath(param.getMetadata().getFileRelativePath());
                metadata.setCreated_at(files.get(0).getCreated_at());
                metadata.setModified_at(files.get(0).getModified_at());

                System.out.println(metadata);

                FileInfoResult result = new FileInfoResult();
                result.setMetadata(metadata);
                method.setResult(result);
            }

            this.ctx.writeAndFlush(Response.builder()
                    .setMethod(method)
                    .build());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Функция возвращает путь к файлу в каталоге хранения.
     * Если данного файле не в базе, то создается новое имя файла в каталоге хранения и регистрируется в базе.
     */
    private String getStorageFilePath(PutFilesParam param){
        String newStorageFilePath = null;
        User user = userService.getUser(userSession.getUsername());
        String uploadedFileName = param.getMetadata().getFileName();
        String uploadedFilePath = param.getMetadata().getFileRelativePath();

        List<StorageFile> files = fileService.getFiles(user, uploadedFileName, uploadedFilePath);
        if (files != null && files.size() > 0) {
            for (StorageFile file : files) {
                if (uploadedFileName.equals(file.getFile_name()) && uploadedFilePath.equals(file.getUser_location())){
                    try {
                        /*Files.deleteIfExists(Paths.get(file.getStorage()));
                        fileService.deleteFile(file);*/
                        if (Files.deleteIfExists(Paths.get(file.getStorage()))) {
                            fileService.updateFile(StorageFile.builder()
                                    .user(file.getUser())
                                    .id(file.getId())
                                    .created_at(file.getCreated_at())
                                    .modified_at(param.getMetadata().getModified_at())
                                    .file_name(file.getFile_name())
                                    .user_location(file.getUser_location())
                                    .storage(file.getStorage())
                                    .build());
                            newStorageFilePath = file.getStorage();
                        } else {
                            fileService.deleteFile(file);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return newStorageFilePath == null ? getNewStagedFile(param, user) : newStorageFilePath;
    }

    /**
     * Регистрируем новый путь файла в базе
     */
    private String getNewStagedFile(PutFilesParam param, User user) {
        String newStorageFilePath = appParam.getLocation() + File.separator + genNewStorageFilePath();
        StorageFile storageFile = StorageFile.builder()
                .user(user)
                .file_name(param.getMetadata().getFileName())
                .created_at(param.getMetadata().getCreated_at())
                .modified_at(param.getMetadata().getModified_at())
                .user_location(param.getMetadata().getFileRelativePath())
                .storage(newStorageFilePath)
                .build();
        fileService.addFile(user, storageFile);
        return newStorageFilePath;
    }

    /**
     * Создание пути хранения файла на сервере для равномерного распределения по папкам в
     * файловой системе
     * @return
     */
    private String genNewStorageFilePath(){
        String uploadFileName = UUID.randomUUID().toString();

        /**
         * берем первые 6 символов имени файла
         */
        String pathSegment = uploadFileName.substring(0,6);

        /**
         * Вставляем File.separator после 2-го и 5-го симовла
         * Пример: r5f7ab -> r5\f7\ab
         */
        StringBuilder sb = new StringBuilder(pathSegment);
        sb.insert(2, File.separator).insert(5,File.separator);
        return sb + File.separator + uploadFileName;
    }
}
