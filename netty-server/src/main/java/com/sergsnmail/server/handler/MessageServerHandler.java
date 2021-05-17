package com.sergsnmail.server.handler;

import com.sergsnmail.common.message.method.common.FileDbMetadata;
import com.sergsnmail.common.message.method.common.FileMetadata;
import com.sergsnmail.common.message.method.common.TransferPackage;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoParam;
import com.sergsnmail.common.message.method.getfileinfo.FileInfoResult;
import com.sergsnmail.common.message.method.getfileinfo.GetFileInfo;
import com.sergsnmail.common.message.method.transferfile.*;
import com.sergsnmail.common.network.Network;
import com.sergsnmail.common.network.NetworkListener;
import com.sergsnmail.common.transfer.FilePackage;
import com.sergsnmail.common.transfer.PackageCollection;
import com.sergsnmail.server.db.DbStorage;
import com.sergsnmail.server.db.file.FileDataSourceImpl;
import com.sergsnmail.server.db.file.FileServiceImpl;
import com.sergsnmail.server.db.file.StorageFile;
import com.sergsnmail.server.db.user.User;
import com.sergsnmail.server.db.user.UserDataSourceImpl;
import com.sergsnmail.server.db.user.UserServiceImpl;
import com.sergsnmail.server.input.ServerParameter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.sergsnmail.common.json.Base64Converter;
import com.sergsnmail.common.message.Request;
import com.sergsnmail.common.message.Response;
import com.sergsnmail.common.message.common.Message;
import com.sergsnmail.common.message.common.UserSession;
import com.sergsnmail.common.message.method.getfile.GetFilesMethod;
import com.sergsnmail.common.message.method.getfile.GetFilesResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MessageServerHandler extends SimpleChannelInboundHandler<Message> implements Network {

    private ServerParameter appParam;
    private final String HANDLER_ID = "Message";
    private ChannelHandlerContext ctx;
    private UserServiceImpl userService;
    private FileServiceImpl fileService;
    private UserSession userSession;

    private Map<String, String> transfer = new HashMap<>();

    private List<NetworkListener> listeners = new ArrayList<>();

    PackageCollection currentPackageCollection;
    StorageFile currentDownloadStorageFile;

    public MessageServerHandler(ServerParameter appParam, UserSession userSession) {
        this.appParam = appParam;
        this.userSession = userSession;

        DbStorage db= new DbStorage(this.appParam.getDbLocation());
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

        fireNotify(message);

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
        if (request.getMethod() instanceof UploadFilesMethod){
            UploadFilesMethod method = (UploadFilesMethod) request.getMethod();
            TransferFilesParam param = method.getParameter();
            if (param != null && param.getTransferPackage() != null && !param.getTransferPackage().getBody().isEmpty()){
                try {
                    //System.out.printf("id: %s, %d/%d [%s]%n ", param.getPackageId(), param.getPartNumber(), param.getTotalNumber(),param.getMetadata().getFileName());
                    String pId = param.getTransferPackage().getPackageId();
                    String transferFile = transfer.get(pId);
                    if (transferFile == null){
                        transferFile = getStorageFilePath(param);
                        transfer.put(pId, transferFile);
                    }

                    Files.createDirectories(Paths.get(transferFile).getParent());
                    try(OutputStream writer = new BufferedOutputStream(Files.newOutputStream(Paths.get(transferFile), CREATE, APPEND))){
                        byte[] data = Base64Converter.decodeBase64ToByte(param.getTransferPackage().getBody());
                        writer.write(data, 0 , data.length);
                        writer.flush();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }

                    if (param.getTransferPackage().getPartNumber() == param.getTransferPackage().getTotalNumber()){
                        transfer.remove(pId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            param.getTransferPackage().setBody(null);
            TransferFilesResult result = new TransferFilesResult();
            result.setStatus("1");

            method.setResult(result);
            Response response = Response.builder()
                    .setMethod(method)
                    .build();

            this.ctx.writeAndFlush(response);
        }

        /**
         * Обработка запроса на выгрузку файла
         */
        if (request.getMethod() instanceof DownloadFile){
            getDownloadFileRequestHandler(request);
        }

        /**
         * Обработка выгрузки файла
         */
        if (request.getMethod() instanceof DownloadFileTask){
            downloadFileTaskHandler((DownloadFileTask) request.getMethod());
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

        List<FileDbMetadata> dbMetadataList = files.stream()
                .map(storageFile -> {
                    FileDbMetadata db = new FileDbMetadata();
                    db.setFileName(storageFile.getFile_name());
                    db.setCreated_at(storageFile.getCreated_at());
                    db.setLocation(storageFile.getUser_location());
                    db.setModified_at(storageFile.getModified_at());
                    return db;})
                .collect(Collectors.toList());

        result.setFiles(filename);
        result.setDbmetadata(dbMetadataList);
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

    /**
     * Обработчик запроса на выгрузку файла
     * @param request
     */
    private void getDownloadFileRequestHandler(Request request) {
        DownloadFile method = (DownloadFile)request.getMethod();
        DownloadFileParam param = method.getParameter();
        param.getFileName();
        User user = userService.getUser(userSession.getUsername());
        List<StorageFile> file = fileService.getFiles(user, param.getFileName(), param.getLocation());
        if (file != null && file.size() > 0){
            currentDownloadStorageFile = file.get(0);
            DownloadFileResult result = new DownloadFileResult();
            method.setResult(result);
            currentPackageCollection = new PackageCollection(Paths.get(currentDownloadStorageFile.getStorage()));
            if (currentPackageCollection.hasNext()) {
                result.setResult("1");
            }else{
                result.setResult("0");
            }
        }

        sendCommand(Response.builder()
                .setMethod(method)
                .build());
    }

    /**
     * Обработчик выгрузи файла
     * @param method
     */
    private void downloadFileTaskHandler(DownloadFileTask method) {
        DownloadFileTaskParam param = method.getParameter();
        if (param.getTaskQuery().equalsIgnoreCase("next")){
            if (currentPackageCollection.hasNext()){
                DownloadFileTaskResult result = new DownloadFileTaskResult();
                FilePackage currPackage = currentPackageCollection.next();
                TransferPackage transferPackage = new TransferPackage();
                transferPackage.setPackageId(currPackage.getPackageId());
                transferPackage.setPartNumber(currPackage.getPackageNumber());
                transferPackage.setTotalNumber(currPackage.getTotalPackageCount());

                FileDbMetadata dbMeta = new FileDbMetadata();
                dbMeta.setFileName(currentDownloadStorageFile.getFile_name());
                dbMeta.setCreated_at(currentDownloadStorageFile.getCreated_at());
                dbMeta.setLocation(currentDownloadStorageFile.getUser_location());
                dbMeta.setModified_at(currentDownloadStorageFile.getModified_at());
                transferPackage.setMetadata(dbMeta);

                transferPackage.setBody(Base64Converter.encodeByteToBase64Str(currPackage.getBody()));
                result.setTransferPackage(transferPackage);
                method.setResult(result);

                sendCommand(Response.builder()
                        .setMethod(method)
                        .build());
            } else {
                currentPackageCollection = null;
                currentDownloadStorageFile = null;
            }
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
    private String getStorageFilePath(TransferFilesParam param){
        String newStorageFilePath = null;
        User user = userService.getUser(userSession.getUsername());

        FileMetadata fMeta= (FileMetadata) param.getTransferPackage().getMetadata();

        String uploadedFileName = fMeta.getFileName();
        String uploadedFilePath = fMeta.getFileRelativePath();

        List<StorageFile> files = fileService.getFiles(user, uploadedFileName, uploadedFilePath);
        if (files != null && files.size() > 0) {
            for (StorageFile file : files) {
                if (uploadedFileName.equals(file.getFile_name()) && uploadedFilePath.equals(file.getUser_location())){
                    try {
                        if (Files.deleteIfExists(Paths.get(file.getStorage()))) {
                            fileService.updateFile(StorageFile.builder()
                                    .user(file.getUser())
                                    .id(file.getId())
                                    .created_at(file.getCreated_at())
                                    .modified_at(fMeta.getModified_at())
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
    private String getNewStagedFile(TransferFilesParam param, User user) {
        FileMetadata fMeta = (FileMetadata) param.getTransferPackage().getMetadata();
        String newStorageFilePath = appParam.getStorageRootDir() + File.separator + genNewStorageFilePath();
        StorageFile storageFile = StorageFile.builder()
                .user(user)
                .file_name(fMeta.getFileName())
                .created_at(fMeta.getCreated_at())
                .modified_at(fMeta.getModified_at())
                .user_location(fMeta.getFileRelativePath())
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

    @Override
    public void sendCommand(Message msg) {
        this.ctx.writeAndFlush(msg);
    }

    @Override
    public void addChannelListener(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChannelListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    private void fireNotify(Message msg){
        for (NetworkListener listener: listeners) {
            listener.messageReceive(msg);
        }
    }

    @Override
    public void close() {

    }
}
