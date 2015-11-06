package com.youxigu.net.routing;

import com.youxigu.net.IWolfService;
import com.youxigu.net.Response;

public class SimpleWolfService implements IWolfService {

	@Override
	public boolean handleMessage(Response response, Object message) {
		System.out.println("SimpleWolfService:" + message.getClass().getName());
		if (message instanceof RoutingPacket) {
			RoutingPacket rp = (RoutingPacket)message;
			System.out.println("received message from:" + (rp).getFromId());
			
			rp.setToId(rp.getFromId());
			rp.setServerId(0);
			
			response.write(message);
			
		}
		
		return true;
	}

}
