package com.plc.core;

public class Common {
    //数据包大小
    public static int BYTE_SIZE = 1024 * 4;
    //缓冲区大小
    public static long BUFF_SIZE = 1024 * 1024 * 1024 * 1L;
    //缓冲数据长度
    public static int BUFF_ARR_SIZE = (int)BUFF_SIZE / BYTE_SIZE;
    //起始端口号
    public static int INIT_PORT = 30000;

}
