package com.manu.dynasty.core.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import log.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.service.CommonService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.store.Redis;
import com.manu.network.BigSwitch;
import com.manu.network.SessionManager;
import com.manu.network.TXSocketMgr;
import com.qx.activity.XianShiActivityMgr;
import com.qx.event.EventMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.http.EndServ;
import com.qx.persistent.DBSaver;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMsgMgr;
import com.qx.pvp.LveDuoMgr;
import com.qx.quartz.SchedulerMgr;
import com.qx.task.GameTaskMgr;
import com.qx.util.DelayedSQLMgr;
import com.qx.util.TableIDCreator;
import com.qx.yuanbao.TXQueryMgr;

/**
 * 服务器启动、停止时须执行的东西
 */
public class InitServlet implements Servlet{
	public static Logger log = LoggerFactory.getLogger(InitServlet.class);

	public void init(ServletConfig config) throws ServletException {
		log.info("server start begin at {}",config.getServletContext().getRealPath("/"));
		CommonService.getInstance().init();
		log.info("初始化国家数据");
		GuoJiaMgr.inst.initGuoJiaBeanInfo();
		log.info("初始化国家数据完成");
		log.info("初始化服务器时间为准的活动数据");
		XianShiActivityMgr.instance.initGlobalActivityInfo();
		log.info("初始化服务器时间为准的活动数据完成");
		new DBHelper();//读取配置
		log.info("============server start success...");
	}
	
	public void destroy() {
		close();
	}
	public static void close(){
		closeNet();
		log.info("================game server begin to shutdown================");
		EventMgr.shutdown();
		BigSwitch.inst.houseMgr.shutdown();
		BigSwitch.inst.ybMgr.shutdown();
		BigSwitch.inst.gjMgr.shutdown();
		BigSwitch.inst.heroMgr.shutdown();
		BigSwitch.inst.cardMgr.shutdown();
		TXQueryMgr.inst.q.add(TXQueryMgr.stop);
		BigSwitch.inst.scMgr.shutdown();
		BigSwitch.inst.accMgr.shutdown();
		BigSwitch.inst.pvpMgr.shutdown();
		ChatMgr.getInst().shutdown();
		LveDuoMgr.inst.shutdown();
		SchedulerMgr.inst.stop();
		GameServer.shutdown();
		MemcachedCRUD.sockIoPool.shutDown();
		TableIDCreator.sockIoPool.shutDown();
		PromptMsgMgr.inst.shutdown();
		Redis.destroy(); 
		DBSaver.inst.shutdown();
		DelayedSQLMgr.es.shutdown();
		GameTaskMgr.shutdown();
		HibernateUtil.getSessionFactory().close();
		log.info("================game server shutdown ok================");
	}

	public static void closeNet() {
		// 通知登陆服：关服
		TXSocketMgr.getInst().acceptor.unbind();
		SessionManager.inst.closeAll();
		TXSocketMgr.getInst().acceptor.dispose();
		EndServ ser = new EndServ();
		ser.start();
	}

	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	
	
	public void initAllServerData(){
//		((EntityDataService)ServiceLocator.getSpringBean("entityDataService")).init();
	}
    
	
}
