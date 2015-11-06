package com.youxigu.test;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.youxigu.concurrent.Request;
import com.youxigu.concurrent.RequestDispatcher;

public class RequestDispatcherTest {
	public static void main(String[] args){
		RequestDispatcher dispatcher = RequestDispatcher.getInstance();
		
		dispatcher.registerHandler(1, TestHandler.class);
		dispatcher.registerHandler(2, TestHandler.class);
		
		Random r = new Random();
		for (int i = 0; i < 10000; i++) {
			Request req = new Request();
			req.setCommandType(2);
			req.setDestinationId(i);
			
			Future<Object> ret = dispatcher.dispatch(req);
			
			try {
			//	Object o = ret.get(30, TimeUnit.SECONDS);
			//	System.out.println("result:" + o);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("==================================start sleep==============================");
		try {
			Thread.sleep(10000);
			dispatcher.stop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("==================================finished==============================");
	}
}
