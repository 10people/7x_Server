package com.youxigu.test;

import com.youxigu.boot.WolfConfig;
import com.youxigu.net.WolfClient;

public class WolfClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WolfConfig wolfConfig = WolfConfig.create("./conf/backend0.cfg");
			
		final WolfClient client = WolfClient.create("localhost", 8737,wolfConfig);
		final SayHello sh = new SayHello();
		sh.setServerId(101);
		
		client.start();
		
	//	wc.asynSendTask(new TestTask());
//		
		
				Thread t = new Thread(){
			public void run(){
				for(int i = 0; i < 1;i++){
					client.asynSendTask(sh);
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
							e.printStackTrace();
					}
				}
			}
	};
		
		t.start();
	}

}
