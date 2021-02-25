package com.plc.client;

import java.io.File;

public interface SendData {


    /**
     * 发送文件
     * @param file
     * @return
     */
    boolean send(File file);
}
