package com.sergsnmail.client.transfer;

/**
 * Класс хранящий данные конкретной предачи
 */
public class TransferEvent {
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
