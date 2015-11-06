package com.youxigu.net;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 通过设置参数和结果的Task
 * 请求方设置Param，处理完毕后把结果设置完，调用execute;
 * 
 * @author wuliangzhu
 *
 */
public final class SyncWolfTask implements WolfTask {
	private static final long serialVersionUID = 181331562707770737L;
	public static int REQUEST = 1;
	public static int RESPONSE = 2;
	
	private Object[] params;
	private Object result;
	static AtomicLong requestIdCounter = new AtomicLong(1);
	private long requestId; // 作为请求和响应的标识
	private String methodName; // 用来标识一个独立的请求，比如sendChat getChat 都给赋值一个独立的id，服务端根据这个Id来调用对应逻辑
	private String serviceName;
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public SyncWolfTask() {
		this.requestId = requestIdCounter.getAndIncrement();
	}
	
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public void setParams(Object... params) {
		this.params = params;
	}
	
	public Object[] getParams() {
		return params;
	}
	
	/**
	 * 这个方法只能在调用方执行，Node上不能执行
	 * 
	 */
	public WolfTask execute(Response response) {
		
		return null;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
