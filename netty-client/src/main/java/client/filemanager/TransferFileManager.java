package client.filemanager;

import client.NotifyCallback;
import client.network.ClientNetwork;
import client.network.NetworkListener;

import message.Base64Converter;
import message.Request;
import message.Response;
import message.common.Message;
import message.common.UserSession;
import message.method.putfile.PutFilesMethod;
import message.method.putfile.PutFilesParam;
import message.method.putfile.PutFilesResult;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransferFileManager implements NetworkListener {

    private final ClientNetwork clientNetwork;
    private final UserSession session;
    private FilePackage currPackage;
    private ExecutorService SERVICE = Executors.newFixedThreadPool(1);

    private NotifyCallback notifyCallback;

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    public TransferFileManager(ClientNetwork clientNetwork, UserSession session) {
        this.clientNetwork = clientNetwork;
        this.clientNetwork.addChannelListener(this);
        this.session = session;
    }

    public void transferFile(Path file){
        PackageCollection packageCollection = new PackageCollection(file);
        Thread worker = new Thread(() -> {
            while(packageCollection.hasNext() && !shutdown.get()){
                if (currPackage == null){
                    currPackage = packageCollection.next();
                    currPackage.setReceived(false);
                    //System.out.printf("%d sending\n",currPackage.getPackageNumber());
                    sendPackage(currPackage);
                }

                if (currPackage != null && currPackage.isReceived().get()) {
                    currPackage = packageCollection.next();
                    currPackage.setReceived(false);
                    //System.out.printf("%d sending\n",currPackage.getPackageNumber());
                    /*if (currPackage.getPackageNumber() == 99999){
                        System.out.println("break");
                    }*/
                    sendPackage(currPackage);
                }
            }
            System.out.println("Transfer complete");
        });
        worker.setDaemon(true);
        SERVICE.execute(worker);
    }

    private void sendPackage(FilePackage filePackage){
        PutFilesParam param = new PutFilesParam();
        param.setPackageId("test_id");
        param.setPartNumber(filePackage.getPackageNumber());
        param.setTotalNumber(filePackage.getTotalPackageCount());
        param.setFilename(filePackage.getFileName());
        param.setPath(filePackage.getFilePath());
        param.setBody(Base64Converter.encodeByteToBase64(filePackage.getBody()));

        PutFilesMethod putMethod = PutFilesMethod.builder()
                .setParameter(param)
                .build();
        this.clientNetwork.sendCommand(Request.builder()
                .setMethod(putMethod)
                .setSession(this.session)
                .build());
    }

    @Override
    public void messageReceive(Message msg) {
        if (msg instanceof Response){
            Response resp = (Response) msg;
            if(resp.getMethod() instanceof PutFilesMethod){
                PutFilesMethod putFilesMethod = (PutFilesMethod) resp.getMethod();
                PutFilesResult putResult = putFilesMethod.getResult();
                if (putResult != null) {
                    if ("1".equals(putResult.getStatus())){
                        TransferNotifyObject transferNotifyObject = new TransferNotifyObject();
                        transferNotifyObject.fileName = currPackage.getFileName();
                        transferNotifyObject.currentNumber = currPackage.getPackageNumber();
                        transferNotifyObject.totalNumber = currPackage.getTotalPackageCount();
                        notifyCallback.notify(transferNotifyObject);
                        //System.out.printf("%d received\n",currPackage.getPackageNumber());
                        currPackage.setReceived(true);
                    }
                }
            }
        }
    }

    public void setNotifyCallback(NotifyCallback clientController) {
        this.notifyCallback = clientController;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown.set(shutdown);
    }

    public void transferShutdown(){
        setShutdown(true);
        SERVICE.shutdown();
    }

    public class TransferNotifyObject{
        private String fileName;
        private int currentNumber;
        private int totalNumber;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getCurrentNumber() {
            return currentNumber;
        }

        public void setCurrentNumber(int currentNumber) {
            this.currentNumber = currentNumber;
        }

        public int getTotalNumber() {
            return totalNumber;
        }

        public void setTotalNumber(int totalNumber) {
            this.totalNumber = totalNumber;
        }
    }
}
