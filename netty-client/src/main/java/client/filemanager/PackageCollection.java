package client.filemanager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class PackageCollection implements Iterator<FilePackage> {

    private int DEFAULT_PACKAGE_SIZE = 3000 * 1024;
    private final String packageId;
    private final Path filePath;
    private int currentPackage = 0;
    private int totalPackage = 1;

    private ByteBuffer buffer;
    private FileChannel inChannel;
    private RandomAccessFile aFile;

    public PackageCollection(Path filePath){//}, FileMetadata fileMetadata) {
        this.filePath = filePath;
        this.packageId = UUID.randomUUID().toString();//(filePath.toString()).toString();
        init();
    }

    private void init(){
        try {
            aFile = new RandomAccessFile(String.valueOf(this.filePath), "r");
            inChannel = aFile.getChannel();
            long fileSize = inChannel.size();
            if (fileSize > DEFAULT_PACKAGE_SIZE) {
                this.totalPackage = (int) (fileSize % DEFAULT_PACKAGE_SIZE > 0 ? (fileSize/DEFAULT_PACKAGE_SIZE) + 1: fileSize/DEFAULT_PACKAGE_SIZE);
            }
            buffer = ByteBuffer.allocate(DEFAULT_PACKAGE_SIZE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            closeFile();
        }
    }

    private void closeFile() {
        try {
            inChannel.close();
            aFile.close();
            System.out.println("file closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        return currentPackage < totalPackage;
    }

    @Override
    public FilePackage next() {
        FilePackage nextPackage = null;
        try {
            int length;
            if ((length = inChannel.read(buffer)) > 0){
                buffer.flip();

                currentPackage++;
                nextPackage = new FilePackage();
                nextPackage.setPackageId(this.packageId);
                nextPackage.setPackageNumber(this.currentPackage);
                nextPackage.setTotalPackageCount(this.totalPackage);
                nextPackage.setBody(Arrays.copyOfRange(this.buffer.array(),0,length));

                buffer.clear();
            } else {
                closeFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextPackage;
    }

    public String getPackageId() {
        return packageId;
    }
}
