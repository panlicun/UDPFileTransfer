package com.plc.core.model;

import java.io.Serializable;

public class TextModel implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1425307876096494974L;
	/**
	 * 线程号
	 */
	private int threadIndex;
	/**
	 * 发送次数
	 */
	private int sendTimes;
	/**
	 * 发送位置
	 */
	private long sendBytePosition;
	/**
	 * 字节大小
	 */
	private int byteSize;

	/**
	 * 发送的数据包
	 */
	private byte[] data;


	public TextModel(int threadIndex, int sendTimes, long sendBytePosition, int byteSize, byte[] data) {
		this.threadIndex = threadIndex;
		this.sendTimes = sendTimes;
		this.sendBytePosition = sendBytePosition;
		this.byteSize = byteSize;
		this.data = data;
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

	public long getSendBytePosition() {
		return sendBytePosition;
	}

	public void setSendBytePosition(long sendBytePosition) {
		this.sendBytePosition = sendBytePosition;
	}

	public int getByteSize() {
		return byteSize;
	}

	public void setByteSize(int byteSize) {
		this.byteSize = byteSize;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public TextModel(){}

	@Override
	public String toString() {
		return "TextModel{" +
				"threadIndex=" + threadIndex +
				", sendTimes=" + sendTimes +
				", sendBytePosition=" + sendBytePosition +
				", byteSize=" + byteSize +
				'}';
	}
}
