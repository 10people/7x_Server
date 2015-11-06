package com.youxigu.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class Request {
	public static final byte REQ_SYN = 1; // 需要返回结果
	public static final byte REQ_ASYN = 0; // 不需要返回结果
	
	private long requestId; // 用于区分不同的请求
	private int commandType;
	private int destinationId;
	private byte sync;
	
	AtomicInteger counter; // 被Handler调用
	FutureRequest<Object> result;
	
	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public int getCommandType() {
		return commandType;
	}

	public void setCommandType(int commandType) {
		this.commandType = commandType;
	}

	public byte getSync() {
		return sync;
	}

	public void setSync(int sync) {
		this.sync = (byte)sync;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
}
