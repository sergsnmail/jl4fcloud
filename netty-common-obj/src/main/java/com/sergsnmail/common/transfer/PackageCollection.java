package com.sergsnmail.common.transfer;
/**
 * Класс для формирования пакетов для отправки
 * Разбивает воходящий файл на сегменты размером, указанным в
 * константе DEFAULT_PACKAGE_SIZE
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PackageCollection implements Iterator<FilePackage> {

    private int DEFAULT_PACKAGE_SIZE = 3 * 1024 * 1024;
    private final String packageId;
    private final Path filePath;
    private int currentPackage = 0;
    private int totalPackage = 1;

    private ByteBuffer buffer;
    private FileChannel inChannel;
    private RandomAccessFile aFile;

    private long DEFAULT_TIMEOUT = 60 * 1000;

    public PackageCollection(Path filePath){
        this.filePath = filePath;
        this.packageId = UUID.randomUUID().toString();
        init();
    }

    private void init(){
        try {

            /**
             * ожидание разблокировки файла
             */
            long startTime = System.currentTimeMillis();
            long currentTime;
            while(true){
                try{
                    aFile = new RandomAccessFile(this.filePath.toString(), "r");
                    break;
                } catch (Exception e){
                    currentTime = System.currentTimeMillis();
                    if ((currentTime - startTime) > DEFAULT_TIMEOUT){
                        throw new TimeoutException(String.format("Unable to access file: %s", this.filePath));
                    }
                }
            }

            inChannel = aFile.getChannel();
            long fileSize = inChannel.size();
            if (fileSize == 0){
                currentPackage = -1;
                closeFile();
            }

            /**
             * Определяем количество пакетов для отправки
             */
            if (fileSize > DEFAULT_PACKAGE_SIZE) {
                this.totalPackage = (int) (fileSize % DEFAULT_PACKAGE_SIZE > 0 ? (fileSize/DEFAULT_PACKAGE_SIZE) + 1: fileSize/DEFAULT_PACKAGE_SIZE);
            }

            buffer = ByteBuffer.allocate(DEFAULT_PACKAGE_SIZE);
        } catch (FileNotFoundException | TimeoutException e) {
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
            //System.out.println("file closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод возвращает true если еще есть пакеты для отправки
     * @return
     */
    @Override
    public boolean hasNext() {
        return currentPackage != -1 && currentPackage < totalPackage;
    }

    /**
     * Получение следующей порции байтов файла
     * @return
     */
    @Override
    public FilePackage next() {
        FilePackage nextPackage = null;
        try {
            int length;
            if ((length = inChannel.read(buffer)) > 0) {
                buffer.flip();

                currentPackage++;
                nextPackage = new FilePackage();
                nextPackage.setPackageId(this.packageId);
                nextPackage.setPackageNumber(this.currentPackage);
                nextPackage.setTotalPackageCount(this.totalPackage);
                nextPackage.setBody(Arrays.copyOfRange(this.buffer.array(),0,length));

                buffer.clear();

                if (currentPackage == totalPackage){
                    closeFile();
                }
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
