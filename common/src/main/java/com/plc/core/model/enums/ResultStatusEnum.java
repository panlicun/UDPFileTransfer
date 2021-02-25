package com.plc.core.model.enums;

public enum ResultStatusEnum {

    SEND_INFO_SUCCESS(200,"接受文件基本信息成功"),
    SEND_FILE_SUCCESS(300,"文件发送成功"),
    THREAD_SEND_FINISH(400,"线程发送完成"),
    SERVER_START_SUCCESS(500,"服务端启动完成"),
    DATA_CHECK_SUCCESS(600,"数据校验完成");

    private int code;
    private String msg;

    ResultStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
