package com.plc.server;

import com.plc.core.Common;
import com.plc.core.model.ReissueDataModel;
import com.plc.core.model.ResultModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.Channel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiveDataThread implements Runnable{

    private byte[][] bytes = new byte[Common.BUFF_ARR_SIZE][];

    private int port;
    private int threadIndex;
    private CountDownLatch startLatch;
    private CountDownLatch endLatch;
    private String fileName;

    private boolean stopThread = false;

    public ReceiveDataThread(int threadIndex,int port,String fileName,CountDownLatch startLatch,CountDownLatch endLatch) {
        this.port = port;
        this.threadIndex = threadIndex;
        this.fileName = fileName + threadIndex;
        this.startLatch = startLatch;
        this.endLatch = endLatch;
    }

    @Override
    public void run() {
        System.out.println("接收服务" + threadIndex + "已启动...");
        DatagramSocket ds = null;//接收端监听指定端口
        try {
            ds = new DatagramSocket(port);
            //定义数据包,用于存储数据
            byte[] buf = new byte[5120];
            DatagramPacket dp = new DatagramPacket(buf, 0, buf.length);
            while(true){
                if(stopThread){
                    System.out.println("停止线程");
                    break;
                }
                //如果线程启动成功，调用，让外面知道已经启动完成
                startLatch.countDown();
                ds.receive(dp);//通过服务的receive方法将收到数据存入数据包中,receive()为阻塞式方法
                //通过数据包的方法获取其中的数据
                byte[] data = dp.getData();
                TextModel textModel = (TextModel)byteArrayToObject(data);
                System.out.println("线程"+threadIndex+  "::" + textModel.toString());
                bytes[textModel.getSendTimes()] = textModel.getData();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(ds != null){
                ds.close();
            }
            endLatch.countDown();
        }
    }

    /**
     * 检测数据
     */
    public boolean checkDataAndWrite(int sendTimes){
        System.out.println("线程"+threadIndex+"正在检查数据完整性...");
        int bytesLength = bytes.length;
        if(sendTimes != 0){
            bytesLength = sendTimes;
        }
        ReissueDataModel reissueDataModel = new ReissueDataModel();
        reissueDataModel.setThreadIndex(threadIndex);
        List<TextModel> datas = new ArrayList<>();
        for (int i = 0; i< bytesLength; i++) {
            if(bytes[i] == null){
                System.out.println("线程"+threadIndex + "丢失了第"+i+"次的数据包，调用补发机制！");
                TextModel returnTextModel = new TextModel(threadIndex,i,0,Common.BYTE_SIZE,null);
                datas.add(returnTextModel);
            }
        }
        if(datas.size() > 0){
            System.out.println("数据补发");
            reissueDataModel.setDatas(datas);
            Channel channel = ClientChannel.getInstance().getChannel();
            channel.writeAndFlush(reissueDataModel);
            return false;
        }else{
            System.out.println("线程"+threadIndex+"文件落地");
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream("F://aaa//" + fileName,true);
                for (int i = 0; i< bytes.length; i++) {
                    outputStream.write(bytes[i]);
                    outputStream.flush();
                }
                bytes = new byte[Common.BUFF_ARR_SIZE][];
                //通知继续发送
                Channel channel = ClientChannel.getInstance().getChannel();
                ResultModel resultModel = new ResultModel(ResultStatusEnum.DATA_CHECK_SUCCESS);
                resultModel.setThreadIndex(threadIndex);
                channel.writeAndFlush(resultModel);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return true;
        }
    }

    /**
     * 组装数据包
     * @param reissueDataModel
     */
    public void packagData(ReissueDataModel reissueDataModel){
        List<TextModel> datas = reissueDataModel.getDatas();
        if(datas != null){
            for (TextModel textModel : datas) {
                bytes[textModel.getSendTimes()] = textModel.getData();
            }
        }
    }

    public void closeThread() {
        this.stopThread = true;
    }

    /**
     * Byte数组转对象
     * @param bytes
     * @return
     */
    private Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }


}
