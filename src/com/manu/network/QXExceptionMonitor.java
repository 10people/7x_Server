package com.manu.network;

import org.apache.mina.util.ExceptionMonitor;

public class QXExceptionMonitor extends ExceptionMonitor {

	@Override
	public void exceptionCaught(Throwable arg0) {
		String msg = arg0.getMessage();
		if(msg != null && msg.startsWith("无法立即完成一个非阻止性套接字操作")){
			return;
		}
		arg0.printStackTrace();
	}

}
