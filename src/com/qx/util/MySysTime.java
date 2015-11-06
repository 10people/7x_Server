package com.qx.util;

public class MySysTime {
	private  long timeCount;
	private static MySysTime instance;  
	private MySysTime (){
		this.setTimeCount(0);
	}
	public static synchronized MySysTime getInstance() {  
		if (instance == null) {  
			instance = new MySysTime();  
		}  
		return instance;  
	}
	public long getTimeCount() {
		return timeCount;
	}
	public void setTimeCount(long timeCount) {
		this.timeCount = timeCount;
	}  
}
