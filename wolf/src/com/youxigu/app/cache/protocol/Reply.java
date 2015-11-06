package com.youxigu.app.cache.protocol;

public class Reply {
	public static final int SUCCESS = 0;
	public static final int FAILED = 1;
	public static final int NOTEXIST = 2;
	public static final int INVALID_CMD = 3;
	public int code; // 0 success; 1 fail; 2 not exist
	
	public Reply(int code){
		this.code = code;
	}
	
	public String toString(){
		return code + "";
	}
}
