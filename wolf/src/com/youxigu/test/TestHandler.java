package com.youxigu.test;

import java.util.concurrent.atomic.AtomicInteger;

import com.youxigu.concurrent.Handler;
import com.youxigu.concurrent.Request;

public class TestHandler extends Handler {
	public static AtomicInteger taskNum = new AtomicInteger(0);
	@Override
	protected Object handle(Request request) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	System.out.println("do task:" + request.getDestinationId() + "->" + request.getCommandType() + " " + taskNum.incrementAndGet());
		return request.getDestinationId() + "_" + request.getCommandType();
	}

}
