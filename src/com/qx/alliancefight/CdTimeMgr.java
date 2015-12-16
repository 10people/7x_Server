package com.qx.alliancefight;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;

import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.world.FightScene;
import com.qx.world.Player;
import com.qx.world.Scene;

public class CdTimeMgr implements Runnable {
	public CdTimeMgr inst;
	
	public Logger logger = LoggerFactory.getLogger(CdTimeMgr.class); 
	
	private ConcurrentLinkedQueue<CdTime> cdQueue = new ConcurrentLinkedQueue<CdTime>();
	private ConcurrentHashSet<FightScene> fightSceneSet = new ConcurrentHashSet<FightScene>();
	
	private boolean openCdTimeMgr = true;
	
	public CdTimeMgr() {
		inst = this;
	}
	
	@Override
	public void run() {
		while(openCdTimeMgr) {
			checkQueue();
			checkSceneQueue();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkSceneQueue() {
		for(FightScene scene : fightSceneSet) {
			scene.updateScore();
			scene.computeBattleData();
		}
	}

	protected void checkQueue() {
		while(!cdQueue.isEmpty()) {
			CdTime cdTime = cdQueue.peek();
			if(cdTime == null) {
				cdQueue.remove(cdTime);
				break;
			}
			if(!cdTime.isTimeOut()) {
				break;
			}
			IoSession session = SessionManager.inst.getIoSession(cdTime.getJunzhuId());
			Scene scene =  (Scene) session.getAttribute(SessionAttKey.Scene);
			if(scene == null) {
				cdQueue.remove(cdTime);
				break;
			}
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, cdTime.getJunzhuId());
			if(junzhu == null) {
				cdQueue.remove(cdTime);
				break;
			}
			Player player = scene.getPlayerByJunZhuId(junzhu.id);
			player.currentLife = junzhu.shengMingMax;
			AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(junzhu.id);
//			ScoreInfo scoreInfo = scene.scoreInfoMap.get(alliance.id);
			PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
			reviveNotify.setUid(player.userId);
//			reviveNotify.setPosX(scoreInfo.bornPointX);
//			reviveNotify.setPosZ(scoreInfo.bornPointZ);
			reviveNotify.setPosX(player.posX);
			reviveNotify.setPosZ(player.posZ);
			for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
				Player p = entry.getValue();
				p.session.write(reviveNotify.build());
				logger.info("通知玩家:{}, {}在场景:{}复活了", p.jzId, 
						cdTime.getJunzhuId(), scene.name);
			}
			cdQueue.remove(cdTime);
		}
	}
	
	public void start() {
		new Thread(this, "CdTimeMgr-Tread").start();
		logger.info("开启联盟战复活时间管理");
	} 
	
	public void addCdTime(CdTime cdTime) {
		cdQueue.add(cdTime);
	}

	public boolean isOpenCdTimeMgr() {
		return openCdTimeMgr;
	}

	public void setOpenCdTimeMgr(boolean openCdTimeMgr) {
		this.openCdTimeMgr = openCdTimeMgr;
	}
	
	public void addFightScene(FightScene fightScene) {
		fightSceneSet.add(fightScene);
	}
	
	public FightScene removeFightScene(FightScene fightScene) {
		fightSceneSet.remove(fightScene);
		return fightScene;
	}
	
	
}
