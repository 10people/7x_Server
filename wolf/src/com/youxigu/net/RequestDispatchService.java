package com.youxigu.net;

import java.util.concurrent.Future;

import com.youxigu.concurrent.Request;
import com.youxigu.concurrent.RequestDispatcher;

public class RequestDispatchService implements IWolfService {
	
	@Override
	public boolean handleMessage(Response response, Object message) {
		if (message instanceof Request) {
			RequestDispatcher rd = RequestDispatcher.getInstance();
			Future<Object> tmp = rd.dispatch((Request) message);
			Object o = null;
			try {
				o = tmp.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (o != null) {
				response.write(o);
			}else {
				
			}
			
			return true;
		}
		
		return false;
	}

}
