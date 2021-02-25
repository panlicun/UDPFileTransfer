package com.plc.core.model;

import com.plc.core.model.enums.ResultStatusEnum;

import java.io.Serializable;

public class ResultModel implements Serializable {
    private int threadIndex;

    /**
     * 发送次数
     */
    private int sendTimes;

    private String msg;
    private int code;

    public ResultModel(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public ResultModel(ResultStatusEnum resultStatusEnum) {
        this.msg = resultStatusEnum.getMsg();
        this.code = resultStatusEnum.getCode();
    }

    public ResultModel() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public void setThreadIndex(int threadIndex) {
        this.threadIndex = threadIndex;
    }

    public int getSendTimes() {
        return sendTimes;
    }

    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

    @Override
    public String toString() {
        return "ResultModel{" +
                "threadIndex=" + threadIndex +
                ", sendTimes=" + sendTimes +
                ", msg='" + msg + '\'' +
                ", code=" + code +
                '}';
    }
}
