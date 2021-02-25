package com.plc.server;

import com.plc.core.Common;
import com.plc.core.model.ResultModel;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiveData {

    private List<ReceiveDataThread> receiveDataThreads = new ArrayList<>();
    private String fileName;

    private static class ReceiveDataHoler {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static ReceiveData instance = new ReceiveData();
    }

    private ReceiveData() {
    }

    public static ReceiveData getInstance() {
        return ReceiveDataHoler.instance;
    }


    public void receive(int threadNum){

        ExecutorService exec = Executors.newCachedThreadPool();

        try {
            CountDownLatch startLatch = new CountDownLatch(threadNum);
            CountDownLatch endLatch = new CountDownLatch(threadNum);

            //数据流拆分及传输
            for (int i = 0; i < threadNum; i++) {
                int port = Common.INIT_PORT + i;
                ReceiveDataThread receiveDataThread = new ReceiveDataThread(i,port,fileName,startLatch,endLatch);
                exec.execute(receiveDataThread);// 开始执行线程
                receiveDataThreads.add(receiveDataThread);
            }
            startLatch.await(); // 等待所有的线程都运行结束
            System.out.println("接收服务启动完成，等待数据传输。。。");
            Channel channel = ClientChannel.getInstance().getChannel();
            channel.writeAndFlush(new ResultModel(ResultStatusEnum.SERVER_START_SUCCESS));
            endLatch.await();
            System.out.println("接收完成");
            exec.shutdown();
        }  catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public List<ReceiveDataThread> getReceiveDataThreads() {
        return receiveDataThreads;
    }

    public void setReceiveDataThreads(List<ReceiveDataThread> receiveDataThreads) {
        this.receiveDataThreads = receiveDataThreads;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
