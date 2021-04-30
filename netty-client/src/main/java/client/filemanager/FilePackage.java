package client.filemanager;

import java.util.concurrent.atomic.AtomicBoolean;

public class FilePackage {

    private int packageNumber;
    private int totalPackageCount;
    private byte[] body;
    private String fileName;
    private String filePath;
    private AtomicBoolean isReceived = new AtomicBoolean(false);

    public int getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(int packageNumber) {
        this.packageNumber = packageNumber;
    }

    public int getTotalPackageCount() {
        return totalPackageCount;
    }

    public void setTotalPackageCount(int totalPackageCount) {
        this.totalPackageCount = totalPackageCount;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public AtomicBoolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived.set(received);
    }
}
