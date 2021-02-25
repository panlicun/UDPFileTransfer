package com.plc.client;

import com.plc.client.coordinator.ClientCoordinator;
import com.plc.client.sendDataAllImpl.SendDataAll;
import com.plc.client.sendDatas10M.SendData10M;

import java.io.*;

/**
 * Hello world!
 */
public class ClientApp {



    public static void main(String[] args) {
        ClientApp clientApp = new ClientApp();

        ClientCoordinator clientCoordinator = new ClientCoordinator();
        Thread coordinatorThread = new Thread(clientCoordinator, "协调器");
        coordinatorThread.start();

        //启动协调器
        while (true) {
            if (ClientCoordinator.channel != null) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //发送文件
        File file = new File("E:\\scala-2.10.5.rar");
        try {
            SendData10M.getInstance().send(file);
        } finally {

        }
    }



}
