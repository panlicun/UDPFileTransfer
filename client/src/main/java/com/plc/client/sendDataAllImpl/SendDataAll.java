package com.plc.client.sendDataAllImpl;

import com.plc.client.SendData;
import com.plc.client.coordinator.ClientCoordinator;
import com.plc.core.model.ResultModel;
import com.plc.core.model.SendInfoModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendDataAll implements SendData {

    public boolean isSendBaseInfoSuccess = false;
    public int byteLength = 4096;
    public long threadBlock = 0;   //每个线程发送数据的大小

    File file = null;

    private static class SendDataHoler {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static SendDataAll instance = new SendDataAll();
    }

    private SendDataAll() {
    }

    public static SendDataAll getInstance() {
        return SendDataHoler.instance;
    }


    public boolean send(File file){

        this.file = file;

        //初始化线程池
//        ExecutorService exec = Executors.newSingleThreadExecutor();
        ExecutorService exec = Executors.newCachedThreadPool();


        try {
            long fileSize = file.length();;
            threadBlock = 0;
            //文件分析，分析文件大小，判断每个线程传输的大小
            if (fileSize > 1024 * 1024 * 1024) {  //1G
                threadBlock = 1024 * 1024 * 100L;   //500M
            } else if (fileSize > 1024 * 1024 * 500L) { //500M
                threadBlock = 1024 * 1024 * 100L;   //100M
            } else if (fileSize > 1024 * 1024 * 100L) { //100M
                threadBlock = 1024 * 1024 * 10L;
            } else {
                threadBlock = 1024 * 1024 * 1L;
            }

            int threadNum = (int) (fileSize / threadBlock + 1); //根据每块的大小获取线程数
            System.out.println("分成的线程个数" + threadNum);
            CountDownLatch latch = new CountDownLatch(threadNum);
            //向接收端发送文件的基本信息，接收端收到文件基本信息，则开始发送数据
            sendBaseInfo("scala-2.10.5.rar",threadNum,threadBlock,fileSize,byteLength);
            while (true){
                if(isSendBaseInfoSuccess){
                    break;
                }
                Thread.sleep(1000);
            }
            //数据流拆分及传输
            long[] startPos = new long[threadNum];
            for (int i = 0; i < threadNum; i++) {
                startPos[i] = i * threadBlock; // 设置每个线程开始发送文件的起始位置
                System.out.println(startPos[i]);
                //发送文件基本信息
                int sendCount = 0;   //发送的次数
                long everyThreadBlock = threadBlock;
                if(i == threadNum -1){
//                    sendCount = (int)(randomAccessFile.length() - startPos[i])/byteLength;
                    everyThreadBlock = fileSize - i * everyThreadBlock;
                }
//                else{
//                    sendCount = (int)Math.ceil(everyThreadBlock/byteLength);
//                }
//                System.out.println(sendCount);
                exec.execute(new SendDataAllThread("", threadNum, "192.168.3.14", 30000, startPos[i], i, file, everyThreadBlock,latch));// 开始执行线程
//                future.get();  //获取线程的异常
            }
            System.out.println("执行完数据发送");
            latch.await(); // 等待所有的线程都运行结束
            System.out.println("发送完成");
            ClientCoordinator.channel.writeAndFlush(new ResultModel(ResultStatusEnum.SEND_FILE_SUCCESS));
            exec.shutdown();

        }  catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 发送文件基本信息
     * @param fileName  文件名
     * @param threadNum 线程数
     * @param threadBlock   每个线程发送的大小
     * @param fileSize  文件总大小
     */
    private void sendBaseInfo(String fileName, int threadNum, long threadBlock, long fileSize,int byteLength) {
        SendInfoModel sendInfoModel = new SendInfoModel(fileName, threadNum, threadBlock, fileSize,byteLength);
        ClientCoordinator.channel.writeAndFlush(sendInfoModel);
    }

    /**
     * 补发机制
     * @return
     */
    public boolean replenish(TextModel textModel){
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file,"r");
            //获取开始位置
            //文件总大小
            long fileSize = randomAccessFile.length();
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
            Channel channel = ClientCoordinator.channel;
            while (true){
                if(channel.isWritable()){
                    System.out.println("补发数据包" + textModel.toString());
                    channel.writeAndFlush(textModel);
                    channel.flush();
                    break;
                }
                try {
                    System.err.println("补发的时候，等了一下");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

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

}
