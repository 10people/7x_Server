package com.youxigu.test;

import com.youxigu.net.Response;
import com.youxigu.net.WolfTask;

@SuppressWarnings("serial")
public class TestTask implements WolfTask {
	public int counter = 1;
	@Override
	public WolfTask execute(Response response) {
		System.out.println("counter:" + counter++);
		
		return this;
	}

}
