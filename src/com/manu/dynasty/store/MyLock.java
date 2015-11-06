package com.manu.dynasty.store;

public class MyLock {
	private String upperLock;
	private String curLock;
	private Object value;
	private String debugMsg;
	private byte retCode;
	private long timeOut = 5 * 1000; 
	private boolean isLocked = false;
	
	public static final String NULLOREMPTY = "can't be null or empty!"; 
	public static final String NOTINGAME = "not in game..."; 
	
	public MyLock(){}
	
	public MyLock(String lock){
		this.curLock = lock;
	}
	
	public MyLock(String lock, String upperLock){
		this.curLock = lock;
		this.upperLock = upperLock;
	}
	
	public void setUpperLock(String upperLock){
		this.upperLock = upperLock;
	}
	
	public String getUpperLock(){
		return upperLock;
	}
	
	public void setKey(String key){
		this.curLock = key;
	}
	
	public String getKey(){
		return curLock;
	}
	
	public void setValue(Object value){
		this.value = value;
	}
	
	public Object getValue(){
		return value;
	}

	public void setDebugMsg(String msg){
		this.debugMsg = msg;
	}
	
	public String getDubugMsg(){
		return debugMsg;
	}

	public void setRetCode(byte retCode){
		this.retCode = retCode;
	}
	
	public byte getRetCode(){
		return retCode;
	}
	
	public boolean isCurLockEmptyOrNull(){
		return isNullOrEmpty(curLock);
	}
	
	public boolean getStatus(){
		return this.isLocked;
	}
	
	public void setTimeOut(long timeOut){
		this.timeOut = timeOut;
	}
	
	public long getTimeOut(){
		return this.timeOut;
	}
	
	public boolean lock(){
		if(isLocked){
			return false;
		}
		return isLocked = true;
	}
	
	public boolean releaseLock(){
		if(isLocked){
			isLocked = false;
			return true;
		}
		return false;
	}
	
	public boolean isUpperLockEmptyOrNull(){
		return this.isNullOrEmpty(upperLock);
	}

	public void notInGame() {
		debugMsg = NOTINGAME;
	}
	
	private boolean isNullOrEmpty(String str){
		boolean result = (curLock == null);
		if(result){
			debugMsg = NULLOREMPTY;
		}
		else{
			result = curLock.isEmpty();
			if(result)
			{
				debugMsg = NULLOREMPTY;
			}
		}
		
		return result;
	}

	public boolean unLock() {
		if(this.isLocked)
		{
			this.isLocked = false;
			return true;
		}
		return false;
	}
}

