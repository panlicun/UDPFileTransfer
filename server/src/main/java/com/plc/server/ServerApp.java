package com.plc.server;

import com.plc.core.model.TextModel;
import com.plc.server.coordinator.ServerCoordinator;
import com.sun.security.ntlm.Client;
import io.netty.channel.Channel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerApp {

    public static boolean START_RECEIVE = false;
    public static int THREAD_NUM = 0;

    public static void main(String[] args) {
        //启动协调器
        ServerCoordinator serverCoordinator = new ServerCoordinator();
        Thread coordinatorThread = new Thread(serverCoordinator, "协调器");
        coordinatorThread.start();

        while (true){
            System.out.println("服务端已启动，等待接收数据");
            if(START_RECEIVE){
                ReceiveData.getInstance().receive(THREAD_NUM);
                START_RECEIVE = false;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
