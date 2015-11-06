package com.manu.dynasty.base.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.boot.GameServerInit;
import com.manu.dynasty.core.servlet.InitServlet;
//import com.manu.dynasty.pvp.job.NationalWarCityScheduleCleanJob;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.util.MMap;
import com.qx.persistent.HibernateUtil;

public class CommonService {
	public static Logger log = LoggerFactory.getLogger(CommonService.class);
	private static CommonService instance;
	
	public static CommonService getInstance(){
		if (instance == null) {
			instance = new CommonService();
		}
		
		return instance;
	}
	
	public void init(){
		try{
			log.info("载入服务器配置");
			GameServer.init();
			log.info("载入hibernate.");
			HibernateUtil.getSessionFactory();
//			MMap.create();
			log.info("初始化GameServerInit");
			GameServerInit.init();
			log.info("初始化Redis");
			final Redis r = Redis.getInstance();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					r.test();
				}
			}).start();
		}catch(Throwable e){
			InitServlet.log.error("初始化异常。", e);
		}		

		
	}


}
