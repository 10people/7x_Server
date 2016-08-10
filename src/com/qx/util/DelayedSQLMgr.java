package com.qx.util;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.manu.dynasty.boot.GameServer;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.JunzhuPveInfo;
import com.qx.pve.PveRecord;

import log.CunLiangLog;

public class DelayedSQLMgr {
	public static ThreadPoolExecutor es = new TPE(10, 10,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());
	public static DelayedSQLMgr inst = new DelayedSQLMgr();
	public void slowAct(int id, long ms){
		Date dt = new Date();
		es.submit(()->{
//			log.error("请求处理时间过长 id {} ms {}",mf.id, diff);
			SlowAct act = new SlowAct();
			act.costMS = (int)ms;
			act.reqId = id;
			act.dt = dt;
			act.serverId = GameServer.serverId;
			HibernateUtil.insert(act);
		});
	}
	
	public void cunLiang(long id, int level){
		es.submit(()->
			CunLiangLog.inst.levelChange(id, level)
		);
	}
	public void cunLiangAdd(long junZhuId, int channel, String roleName, int roleId) {
		es.submit(()->
			CunLiangLog.inst.add(junZhuId, channel, roleName, roleId)		
		);
	}
}
