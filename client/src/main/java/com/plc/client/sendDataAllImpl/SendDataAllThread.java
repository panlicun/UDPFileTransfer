package com.plc.client.sendDataAllImpl;

import com.plc.client.coordinator.ClientCoordinator;
import com.plc.core.model.TextModel;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

public class SendDataAllThread implements Runnable{

    private String ip;
    private int port;
    private long startPos;   //起始位置
    private int threadIndex;   //线程的索引
    private File file;
    private long threadBlock;
    private String fileName;
    private int threadNum;
    private CountDownLatch latch;

    private int byteLength = 4096;

    public SendDataAllThread(String fileName, int threadNum, String ip, int port, long startPos, int threadIndex, File file, long threadBlock, CountDownLatch latch) {
        this.fileName = fileName;
        this.threadNum = threadNum;
        this.ip = ip;
        this.port = port;
        this.startPos = startPos;
        this.threadIndex = threadIndex;
        this.file = file;
        this.threadBlock = threadBlock;
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
            System.out.println("文件大小" +randomAccessFile.length());
            System.out.println("线程" +threadIndex + ",threadBlock" + threadBlock);
            while((c = randomAccessFile.read(bytes))!= -1){
                len += c;
                if(len > threadBlock){  //超过了要发送的大小,表示该线程已经发送完
                    //该线程已发送完，告诉服务端开始校验
                    TextModel textModel = new TextModel(threadIndex,0,0,bytes.length,null);
                    System.out.println(textModel.toString());
                    ClientCoordinator.channel.writeAndFlush(textModel);
                    break;
                }
                TextModel textModel = new TextModel(threadIndex,i,(startPos + bytes.length*i),c,bytes);
                System.out.println(textModel.toString());
                byte[] bArr = objectToByteArray(textModel);
                DatagramPacket dp = new DatagramPacket(bArr, 0, bArr.length, InetAddress.getByName("192.168.3.14"), 30000);
                ds.send(dp);
                i++;
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


}
