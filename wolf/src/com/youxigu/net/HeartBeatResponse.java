package com.youxigu.net;

import com.youxigu.net.SessionMgr.HeartBeatState;

public class HeartBeatResponse implements WolfTask {
	private static final long serialVersionUID = 624682589379288700L;
	
	public static HeartBeatResponse instance = new HeartBeatResponse();
	@Override
	public WolfTask execute(Response response) {
		HeartBeatState hbState = (HeartBeatState)response.getSession().getAttribute("hb");
		hbState.state = HeartBeatState.IDLE;
		return null;
	}

}
