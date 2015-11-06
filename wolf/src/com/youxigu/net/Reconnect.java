package com.youxigu.net;

public class Reconnect implements IWolfService, ISessionListener {

	@Override
	public boolean handleMessage(Response response, Object message) {
		return false;
	}

	/**
	 * 发现连接关闭，自动进行重连
	 */
	public void close(Response response) {
		SessionDaemon.notifyDeamon(response.getSession());	
	}

	@Override
	public void open(Response response) {
		SessionDaemon.startReconnectDaemon((WolfClient)response.getSession().getAttribute("wolfClient"));	
	}

}
