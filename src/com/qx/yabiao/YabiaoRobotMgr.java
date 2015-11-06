package com.qx.yabiao;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Yabiao.BiaoCheState;

import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.CartTemp;
import com.manu.network.BigSwitch;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.persistent.HibernateUtil;
import com.qx.world.Scene;
/**
 * 
 * @author yuquanshui 
 *
 */
public class YabiaoRobotMgr implements Runnable{
	public static Logger log = LoggerFactory.getLogger(YabiaoRobotMgr.class);
	public static int INTERVAL = 4500;
	public static boolean isShowLog = false;
	public static YabiaoRobotMgr inst;
	public ConcurrentHashMap<Long, Object> yabiaoRobotMap;

	public YabiaoRobotMgr(){
		inst = this;
		initData();
		new Thread(this, "ybrobot").start();
	}
	public void initData(){
		yabiaoRobotMap= new  ConcurrentHashMap<Long, Object>();
		
	}


	@Override
	public void run() {
		while (!GameServer.shutdown) {
			try {
				Thread.sleep(INTERVAL);
				robotsMove();
			} catch (Exception e) {
				//log.error("押镖机器人线程执行押镖移动出错 : {}", e);
			}
		}
		//log.info("退出押镖机器人线程");
	}
	//移动所有镖车
	public void robotsMove() {
		if(isShowLog){
			log.info("押镖机器人移动开始");
		}
		Iterator<?> it= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Integer scId =(Integer) entry.getKey();
			Scene ybsc =(Scene) entry.getValue();
			Iterator<?> itrobot=yabiaoRobotMap.entrySet().iterator();
			while (itrobot.hasNext()) {
				Map.Entry entryRobot = (Map.Entry) itrobot.next();
				YBRobot ybr = (YBRobot) entryRobot.getValue();
				long now = System.currentTimeMillis();
				long addTime=now -ybr.startTime;
				if(isShowLog){
					log.info("now-{},start---{},total---{},add---{},used--{}",now,ybr.startTime,ybr.totalTime,addTime,ybr.usedTime);
				}
				if(!ybr.isBattle){//未进入战斗移动镖车
					if(ybr.usedTime+addTime<ybr.totalTime){//时间耗完认为到达终点ybr.startTime+ybr.totalTime<=now&&
							move(ybsc,ybr);
					}else{
						log.info("{}镖车移动到终点，已用时间为{}-{},进度{}%",ybr.jzId,ybr.usedTime,ybr.totalTime,(ybr.usedTime/(double)ybr.totalTime)*100);
						BigSwitch.inst.ybMgr.settleYaBiaoSuccess(ybr.jzId, scId);
					}
				}else{//进入战斗保存镖车状态
					if(isShowLog){
						log.info("{}镖车进入战斗",ybr.jzId);
					}
					long battleTime=now-ybr.battleStart;
					long battleLimitTime=CanShu.JIEBIAO_RESULTBACK_MAXTIME*1000L;//需读取配置 按照毫秒计算CanShu.JIEBIAO_MAXTIME 
					if(battleTime>battleLimitTime){//超过最长战斗时间，扔掉此次战斗记录
						log.info("{}镖车战斗时间超时，抛弃战斗数据",ybr.jzId);
						ybr.isBattle=false;
						CartTemp cart = YabiaoMgr.cartMap.get(ybr.horseType);
						ybr.protectCD=cart.protectTime;//重置保护CD
						//重置开始押镖时间
						ybr.startTime=now;
						ybr.endBattleTime=now;
					}
				}
			}
		}
		if(isShowLog){
			log.info("押镖机器人移动结束");
		}
	}
	//移动实际为扣除已走的时间（路程已定，速度恒定，则可按照时间来计算坐标）
	public void move(Scene sc,YBRobot ybr) {
		if(isShowLog){
			log.info("{}镖车移动，已用时间为{}-{},进度{}%",ybr.jzId,ybr.usedTime,ybr.totalTime,(ybr.usedTime/(double)ybr.totalTime)*100);
		}
		long now = System.currentTimeMillis();
		long usedtime=now-ybr.startTime;
		ybr.startTime=now;
		ybr.usedTime+=usedtime;
		broadBiaoCheEvent(ybr,sc);
	}
	//广播押镖进度
	public void broadBiaoCheEvent(YBRobot ybr,Scene sc) {
		try {
			if(isShowLog){
				log.info("押镖机器人广播状态开始");
			}
			BiaoCheState.Builder resp=BiaoCheState.newBuilder();
			resp.setJunZhuId(ybr.jzId);
			resp.setUsedTime(ybr.usedTime);
			YaBiaoInfo ybBean = HibernateUtil.find(YaBiaoInfo.class, ybr.jzId);
			resp.setHp(ybBean.hp);
			resp.setWorth(ybBean.worth);
			int protectTime=ybr.protectCD-((int)(System.currentTimeMillis()-ybr.endBattleTime)/1000);
			resp.setBaohuCD(protectTime>0?protectTime:0);
			resp.setState(ybr.isBattle?20:(protectTime>0?30:10));//10押送中 20 战斗中 30 保护CD
			Integer scId=BigSwitch.inst.ybMgr.ybJzId2ScIdMap.get(ybr.jzId);
			if(scId==null){
				log.error("镖车所在场景未找到{}",ybr.jzId);
				return;
			}
			Set<Long> jbSet= BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
			if(jbSet==null){
				if(isShowLog){
					log.info("场景{}中无劫镖人员,无须广播",sc.name);
				}
				return;
			}
			for (Long jId : jbSet) {
				SessionUser su = SessionManager.inst.findByJunZhuId(jId);
				if(su!=null){//su为空时认为此账号已下线，不推送信息
					if(isShowLog){
						log.info("{}镖车移动广播进度给{}",ybr.jzId,jId);
					}
					su.session.write(resp.build());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
