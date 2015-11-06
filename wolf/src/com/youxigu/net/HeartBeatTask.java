package com.youxigu.net;

public class HeartBeatTask implements WolfTask{
	private static final long serialVersionUID = -8115224489470674080L;

	public static HeartBeatTask instance = new HeartBeatTask();
	@Override
	public WolfTask execute(Response response) {

		return HeartBeatResponse.instance;
	}

}
