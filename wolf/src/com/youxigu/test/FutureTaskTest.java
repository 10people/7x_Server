package com.youxigu.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.youxigu.concurrent.FutureRequest;

public class FutureTaskTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Callable call = new Callable(){

			@Override
			public Object call() throws Exception {
				return "test";
			}
			
		};
		for (int i = 0; i < 1;i++) {
			FutureRequest<Object> tm = new FutureRequest<Object>(call);
			Object o = null;
			try {
				tm.setException(new RuntimeException("test"));
				
				o = tm.get(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("future:" + o);
		}

	}

}
