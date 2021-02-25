package com.plc.server;

import com.plc.core.model.ResultModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.Channel;

import java.io.*;

public class DataFactory {

    //接收总的数据
    private byte[][][] totalData;


    private static class DataFactoryHoler {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static DataFactory instance = new DataFactory();
    }

    private DataFactory() {
    }

    public static DataFactory getInstance() {
        return DataFactoryHoler.instance;
    }

    /**
     * 接收数据
     */
    public void receiveData(){

    }

    //数据校验
    public void checkData(TextModel textModel){
        System.out.println("线程"+textModel.getThreadIndex()+"正在检查数据完整性...");
        byte[][] threadBytes = totalData[textModel.getThreadIndex()];
        for (int i = 0; i< threadBytes.length; i++) {
            if(threadBytes[i] == null){
                TextModel returnTextModel = new TextModel(textModel.getThreadIndex(),i,0,textModel.getByteSize(),null);
                Channel channel = ClientChannel.getInstance().getChannel();
                while (true){
                    if(channel.isWritable()){
                        System.out.println("线程"+textModel.getThreadIndex() + "丢失了第"+i+"次的数据包，调用补发机制！" + returnTextModel);
                        channel.writeAndFlush(returnTextModel);
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
    }

    //数据封装
    public void packageData(TextModel textModel){
        int sendTime = textModel.getSendTimes();
        int threadIndex = textModel.getThreadIndex();
        byte[] bytes = textModel.getData();
        totalData[threadIndex][sendTime] = bytes;
    }

    /**
     * 检测全部的数据
     */
    public boolean checkAllData(){
        for (int i = 0; i< totalData.length; i++) {
            for (int j = 0; j < totalData[i].length; j++) {
                if(totalData[i][j] == null){
                    System.out.println("数据包不全,缺少" + i + "线程，"+j+"位置的数据");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean saveFile(String path){
        System.out.println("保存文件" + path);
        //验证数据的完整性
        while (true){
            boolean flag = checkAllData();
            if(flag){
                //告诉客户端已经全部接受
                ClientChannel.getInstance().getChannel().writeAndFlush(new ResultModel(ResultStatusEnum.SEND_INFO_SUCCESS));
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path,true);
            for (int i = 0; i< totalData.length; i++) {
                for (int j = 0; j < totalData[i].length; j++) {
                    outputStream.write(totalData[i][j]);
                    outputStream.flush();
                }
            }
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

    /**
     * Byte数组转对象
     * @param bytes
     * @return
     */
    public Object byteArrayToObject(byte[] bytes) {
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


    public byte[][][] getTotalData() {
        return totalData;
    }

    public void setTotalData(byte[][][] totalData) {
        this.totalData = totalData;
    }
}
