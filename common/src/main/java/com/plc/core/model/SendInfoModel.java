package com.plc.core.model;

import java.io.Serializable;
import java.util.List;

public class SendInfoModel implements Serializable {
    private String fileName;
    private int threadNum;   //发送所需的线程数
    private long threadBlock; //每个线程文件的大小
    private long fileSize;  //文件总大小
    private int byteLength; //byte数组的长度

    private int[] portArr;

    public SendInfoModel() {
    }

    public SendInfoModel(String fileName, int threadNum, long threadBlock, long fileSize ,int byteLength) {
        this.fileName = fileName;
        this.threadNum = threadNum;
        this.threadBlock = threadBlock;
        this.fileSize = fileSize;
        this.byteLength = byteLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public long getThreadBlock() {
        return threadBlock;
    }

    public void setThreadBlock(long threadBlock) {
        this.threadBlock = threadBlock;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public int[] getPortArr() {
        return portArr;
    }

    public void setPortArr(int[] portArr) {
        this.portArr = portArr;
    }

    @Override
    public String toString() {
        return "SendInfoModel{" +
                "fileName='" + fileName + '\'' +
                ", threadNum=" + threadNum +
                ", threadBlock=" + threadBlock +
                ", fileSize=" + fileSize +
                ", byteLength=" + byteLength +
                '}';
    }
}
