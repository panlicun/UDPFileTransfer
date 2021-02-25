package com.plc.client.sendDatas10M;

import com.plc.client.coordinator.ClientCoordinator;
import com.plc.core.Common;
import com.plc.core.model.ResultModel;
import com.plc.core.model.SecureModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

public class SendData10MThread implements Runnable{

    private boolean sendFlag = false;

    private int port;
    private long startPos;   //起始位置
    private int threadIndex;   //线程的索引
    private File file;
    private long threadBlock;
    private CountDownLatch latch;

    private int byteLength;

    public static void main(String[] args) {
        long a = 11423744;
        long b = 1024*1024*1024*1L;
        System.out.println(a%b);
    }

    public SendData10MThread(int port, long startPos, int threadIndex, File file, long threadBlock, int byteLength, CountDownLatch latch) {
        this.port = port;
        this.startPos = startPos;
        this.threadIndex = threadIndex;
        this.file = file;
        this.threadBlock = threadBlock;
        this.byteLength = byteLength;
        this.latch = latch;
    }

    @Override
    public void run() {
        RandomAccessFile randomAccessFile = null;
        DatagramSocket ds = null;//通过DatagramSocket对象创建udp服务
        try {
            randomAccessFile = new RandomAccessFile(file,"r");
            ds = new DatagramSocket();
            byte[] bytes = new byte[byteLength];
            long len = 0;   //累计发送大小
            int c = 0;   //每次读取的大小
            int i = 0;  //循环的次数
            randomAccessFile.seek(startPos);
            System.out.println("线程" +threadIndex + ",threadBlock" + threadBlock);
            while((c = randomAccessFile.read(bytes))!= -1){
                len += c;
                if(len > threadBlock){  //超过了要发送的大小,表示该线程已经发送完
                    //关闭服务端
                    System.out.println("线程发送完成");
                    ResultModel resultModel = new ResultModel(ResultStatusEnum.THREAD_SEND_FINISH);
                    resultModel.setThreadIndex(threadIndex);
                    resultModel.setSendTimes(i);
                    ClientCoordinator.channel.writeAndFlush(resultModel);
                    break;
                }
                i++;   //统计发送次数
                TextModel textModel = new TextModel(threadIndex,i,(startPos + bytes.length*i),c,bytes);
                System.out.println(textModel.toString());
                byte[] bArr = objectToByteArray(textModel);
                DatagramPacket dp = new DatagramPacket(bArr, 0, bArr.length, InetAddress.getByName("192.168.3.14"), port);
                ds.send(dp);
                if(len % Common.BUFF_SIZE == 0){  //发送到buffsize大小,提示服务端进行校验
                    System.out.println("发送暂停。。。");
                    textModel = new TextModel(threadIndex,0,0,bytes.length,null);
                    ClientCoordinator.channel.writeAndFlush(textModel);
                    while (true){
                        if(sendFlag){
                            sendFlag = false;
                            System.out.println("继续发送。。。");
                            i = 0;   //重新计算发送的次数
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            System.out.println("线程" + threadIndex + ":" + i);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            ds.close();
            if(randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
        }
    }


    /**
     * 对象转Byte数组
     * @param obj
     * @return
     */
    public byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }


    public void cSend() {
        this.sendFlag = true;
    }
}
