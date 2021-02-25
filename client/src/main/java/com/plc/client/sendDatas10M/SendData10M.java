package com.plc.client.sendDatas10M;

import com.plc.client.Constant;
import com.plc.client.SendData;
import com.plc.client.coordinator.ClientCoordinator;
import com.plc.client.sendDataAllImpl.SendDataAll;
import com.plc.core.Common;
import com.plc.core.model.*;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendData10M implements SendData {

    private List<SendData10MThread> sendData10MThreads = new ArrayList<>();
    private int byteLength = Constant.BYTE_LENGTH;

    private static class SendData10MHoler {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static SendData10M instance = new SendData10M();
    }
    private SendData10M() {
    }
    public static SendData10M getInstance() {
        return SendData10M.SendData10MHoler.instance;
    }


    File file = null;
    public long threadBlock = 0;   //每个线程发送数据的大小
    /**
     * 发送文件
     * @param file
     * @return
     */
    @Override
    public boolean send(File file) {

        this.file = file;

        ExecutorService exec = Executors.newCachedThreadPool();

        try {
            long fileSize = file.length();
            threadBlock = getThreadBlock(fileSize);
            int threadNum = (int) (fileSize / threadBlock + 1); //根据每块的大小获取线程数
            System.out.println("分成的线程个数" + threadNum);
            CountDownLatch latch = new CountDownLatch(threadNum);
            //向接收端发送文件的基本信息，接收端收到文件基本信息，则开始发送数据
            sendBaseInfo(file.getName(), threadNum, threadBlock, fileSize, byteLength);
            while (true) {
                if (Constant.IS_SEND_BASE_INFO_SUCCESS) {
                    break;
                }
                Thread.sleep(1000);
            }
            System.out.println("服务端启动成功，开始发送数据...");
            //数据流拆分及传输
            long[] startPos = new long[threadNum];
            for (int i = 0; i < threadNum; i++) {
                int port = Common.INIT_PORT + i;
                startPos[i] = i * threadBlock; // 设置每个线程开始发送文件的起始位置
                System.out.println(startPos[i]);
                //发送文件基本信息
                long everyThreadBlock = threadBlock;
                if (i == threadNum - 1) {
                    everyThreadBlock = fileSize - i * everyThreadBlock;
                }
                SendData10MThread sendData10MThread = new SendData10MThread(port,startPos[i], i, file, everyThreadBlock,byteLength,latch);
                exec.execute(sendData10MThread);// 开始执行线程
                sendData10MThreads.add(sendData10MThread);
            }
            System.out.println("执行完数据发送");
            latch.await(); // 等待所有的线程都运行结束
            System.out.println("发送完成");
            ClientCoordinator.channel.writeAndFlush(new ResultModel(ResultStatusEnum.SEND_FILE_SUCCESS));
            exec.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;

    }

    /**
     * 根据文件大小分析每个线程传输的数据大小
     * @param fileSize
     * @return
     */
    private long getThreadBlock(long fileSize){
        long threadBlock = 0;
        if (fileSize > 1024 * 1024 * 1024) {  //1G
            threadBlock = 1024 * 1024 * 100L;   //500M
        } else if (fileSize > 1024 * 1024 * 500L) { //500M
            threadBlock = 1024 * 1024 * 100L;   //100M
        } else if (fileSize > 1024 * 1024 * 100L) { //100M
            threadBlock = 1024 * 1024 * 10L;
        } else {
            threadBlock = 1024 * 1024 * 1L;
        }
        return threadBlock;
    }


    /**
     * 发送文件基本信息
     *
     * @param fileName    文件名
     * @param threadNum   线程数
     * @param threadBlock 每个线程发送的大小
     * @param fileSize    文件总大小
     */
    private void sendBaseInfo(String fileName, int threadNum, long threadBlock, long fileSize, int byteLength) {
        System.out.println("发送文件基本信息。。。");
        SendInfoModel sendInfoModel = new SendInfoModel(fileName, threadNum, threadBlock, fileSize, byteLength);
        ClientCoordinator.channel.writeAndFlush(sendInfoModel);
    }


    /**
     * 补发机制
     * @return
     */
    public boolean replenish(ReissueDataModel reissueDataModel){
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file,"r");
            //文件总大小
            long fileSize = randomAccessFile.length();

            List<TextModel> textModelList = reissueDataModel.getDatas();
            for (TextModel textModel : textModelList) {
                //每个线程的开始位置
                long threadStartPos = threadBlock * textModel.getThreadIndex();
                long startPos = 0;
                //判断是否是最后一个线程
                if(fileSize - threadStartPos > threadBlock){//不是最后一个线程
                    startPos = threadStartPos + textModel.getByteSize() * textModel.getSendTimes();
                }else{
                    if(textModel.getByteSize() * textModel.getSendTimes() < threadBlock){
                        startPos = threadStartPos + textModel.getByteSize() * textModel.getSendTimes();
                    }else{
                        startPos = 2 * threadBlock - (textModel.getByteSize() * textModel.getSendTimes() );
                    }
                }
                textModel.setSendBytePosition(startPos);
                randomAccessFile.seek(startPos);
                byte [] b = new byte[textModel.getByteSize()];
                randomAccessFile.read(b);
                textModel.setData(b);
            }
            Channel channel = ClientCoordinator.channel;
            channel.writeAndFlush(reissueDataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public List<SendData10MThread> getSendData10MThreads() {
        return sendData10MThreads;
    }

    public void setSendData10MThreads(List<SendData10MThread> sendData10MThreads) {
        this.sendData10MThreads = sendData10MThreads;
    }
}
