package com.sergsnmail.client.transfer;
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

public class PackageCollection implements Iterator<FilePackage> {

    private int DEFAULT_PACKAGE_SIZE = 3072 * 1024;
    private final String packageId;
    private final Path filePath;
    private int currentPackage = 0;
    private int totalPackage = 1;

    private ByteBuffer buffer;
    private FileChannel inChannel;
    private RandomAccessFile aFile;

    public PackageCollection(Path filePath){
        this.filePath = filePath;
        this.packageId = UUID.randomUUID().toString();
        init();
    }

    private void init(){
        try {
            boolean isLocked = false;
            /**
             * ожидание разблокировки файла
             */
            while(!isLocked){
                try{
                    aFile = new RandomAccessFile(String.valueOf(this.filePath), "r");
                    isLocked = true;
                } catch (Exception e){
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

    /**
     * Метод возвращает true если еще есть пакеты для отправки
     * @return
     */
    @Override
    public boolean hasNext() {
        if (!(currentPackage != -1 && currentPackage < totalPackage)){
            closeFile();
        }
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
