package com.youxigu.net;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

public class WolfHandler extends IoHandlerAdapter implements IWolfService{

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if (message instanceof WolfTask) {
			WolfTask task = (WolfTask)message;
			WolfTask next = task.execute(new Response(session));
			if (next != null)
				session.write(next);
		}
	}

	public boolean handleMessage(Response response, Object message) {
		if (message instanceof WolfTask) {
			try {
				this.messageReceived(response.getSession(), message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}

}
