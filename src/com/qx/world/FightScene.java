package com.qx.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.AllianceFightProtos.BattleData;
import qxmobile.protobuf.AllianceFightProtos.BattlefieldInfoNotify;
import qxmobile.protobuf.AllianceFightProtos.CampInfo;
import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResultAllianceFight;
import qxmobile.protobuf.Scene.EnterFightScene;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;

import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.LMZBuildingTemp;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliancefight.AllianceFightMatch;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.alliancefight.CampsiteInfo;
import com.qx.alliancefight.ScoreInfo;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.ranking.RankingMgr;

/**
 * 联盟战场景
 * @author lzw
 *
 */
public class FightScene extends Scene {

	public final byte TEAM_RED = 1;				// 红队	对应LMZBuildingTemp type=1 side=1
	public final byte TEAM_BLUE = 2;			// 蓝队	对应LMZBuildingTemp type=1 side=2
	
	/** 战斗开始时间 */
	public long fightStartTime;
	/** 战斗结束时间 */
	public long fightEndTime;
	
	/** 联盟双方的积分、营地占领情况 <联盟id, 分数情况> */
	public Map<Integer, ScoreInfo> scoreInfoMap = null;
	
	/** 队伍分配情况，<红方或者蓝方, 联盟id> */
	public Map<Byte, Integer> teamMap = null;
	
	/** 战斗场景内的营地信息 */
	public List<CampsiteInfo> campsiteInfoList = null;

	/** 上次广播联盟战信息的时间，单位-毫秒 */
	private long lastBroadcastTime;	

	public Set<Byte> remainTeamSet = null;
	public Object teamLock = new Object();
	
	public static final byte CURSOR_TO_RED = 1;		// 游标向红方移动
	public static final byte CURSOR_TO_BLUE = 2;	// 游标向蓝方移动
	public static final byte CURSOR_TO_DEAD = 3;	// 游标处于中间
	
	public static final byte CURSOR_POS_RED = 1;	// 游标位置在红方
	public static final byte CURSOR_POS_BLUE = 2;	// 游标位置在蓝方
	public static final byte CURSOR_POS_DEAD = 0;	// 游标位置在中间
	
	public int id;
	
	public FightScene(String sceneName, long fightEndTime, int id){
		super(sceneName);
		this.id = id;
		fightStartTime = System.currentTimeMillis(); // FIXME 开始时间由策划确定
		this.fightEndTime = fightEndTime;
		scoreInfoMap = new HashMap<Integer, ScoreInfo>();
		teamMap = new HashMap<Byte, Integer>();
		remainTeamSet = new HashSet<Byte>(Arrays.asList(TEAM_RED, TEAM_BLUE));
		lastBroadcastTime = System.currentTimeMillis();
		initCampsite();
	}

	private void initCampsite() {
		List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(2);
		campsiteInfoList = new ArrayList<CampsiteInfo>(buildList.size());
		for(LMZBuildingTemp build : buildList) {
			CampsiteInfo campsite = new CampsiteInfo(CURSOR_TO_DEAD, CURSOR_POS_DEAD, build);
			campsiteInfoList.add(campsite);
		}
	}

	public void enterFightScene(IoSession session, EnterScene.Builder enterFightScene) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null) {
			return;
		}
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		session.setAttribute(SessionAttKey.Scene, this);
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;;
		session.setAttribute(SessionAttKey.playerId, jz.id);
		
		AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(alliance == null) {
			log.error("进入联盟战失败，玩家:{}没有联盟", jz.id);
			return;
		}
		AllianceFightMatch fightMatch = HibernateUtil.find(AllianceFightMatch.class, 
				" where allianceId1="+alliance.id + " or allianceId2="+alliance.id);
		if(fightMatch == null) {
			log.error("进入联盟战失败，找不到联盟:{}的匹配信息", alliance.id);
			return;
		}
		int otherAllianceId = 0;
		if(fightMatch.allianceId1 == alliance.id) {
			otherAllianceId = fightMatch.allianceId2;
		} else if(fightMatch.allianceId2 == alliance.id) {
			otherAllianceId = fightMatch.allianceId1;
		}
		if(otherAllianceId <= 0) {
			log.info("联盟:{}本轮比赛轮空，不需要进入联盟战场景", alliance.id);
			return;
		}
		
		ScoreInfo scoreInfo = scoreInfoMap.get(alliance.id);
		if(scoreInfo == null) {
			// 进行红蓝方队伍分配
			synchronized (teamLock) {
				if(scoreInfoMap.size() == 0) {
					LMZBuildingTemp tempRed = AllianceFightMgr.getTeamCamp(TEAM_RED);
					scoreInfo = new ScoreInfo(alliance.id, TEAM_RED, tempRed.x, tempRed.y, alliance.name);
					scoreInfoMap.put(alliance.id, scoreInfo);
					log.info("为联盟:{}-{},分配了到了红队", alliance.id, alliance.name);
					teamMap.put(TEAM_RED, alliance.id);
					
					AllianceBean otherAlliance = HibernateUtil.find(AllianceBean.class, otherAllianceId);
					LMZBuildingTemp tempBlue = AllianceFightMgr.getTeamCamp(TEAM_BLUE);
					ScoreInfo otherScoreInfo = new ScoreInfo(otherAlliance.id, TEAM_BLUE, tempBlue.x, tempBlue.y, otherAlliance.name);
					scoreInfoMap.put(otherAlliance.id, otherScoreInfo);
					log.info("为联盟:{}-{},分配了到了蓝队", otherAlliance.id, otherAlliance.name);
					teamMap.put(TEAM_BLUE, otherAlliance.id); 
				}
			}
		}
		scoreInfo.addJunZhuId(jz.id);
		
		final Player player = new Player();
		player.userId = userId;
		player.session = session;
		player.setName(jz == null ? enterFightScene.getSenderName() : jz.name);
		player.setPosX(scoreInfo.bornPointX);
		player.setPosY(enterFightScene.getPosY());
		player.setPosZ(scoreInfo.bornPointZ);
		player.jzId = (jz == null ? 0 : jz.id);
		player.allianceId = AllianceMgr.inst.getAllianceId(player.jzId);
		player.roleId = (jz == null ? 1: jz.roleId);
		player.totalLife = jz.shengMingMax;
		player.currentLife = jz.shengMingMax;
		players.put(userId, player);
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		log.info("名字:{},uid:{},进入场景 {},总血量:{}", player.getName(), userId, this.name, jz.shengMingMax);
		
		//告诉当前玩家，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());
		
		//告诉场景内其他玩家，谁进来了。
		final EnterFightScene.Builder enterResponse = getEnterFightSceneResponse(jz, player);
		
		syncSceneExecutor.submit(new Runnable() {
			@Override
			public void run() {
				broadCastEvent(enterResponse.build(), enterResponse.getUid());
			}
		});
	}
	
	public EnterFightScene.Builder getEnterFightSceneResponse(JunZhu jz, Player player) {
		String allianceName = AllianceMgr.inst.getAlliance(jz);
		EnterFightScene.Builder response = EnterFightScene.newBuilder();
		response.setUid(player.userId);
		response.setJunZhuId(jz.id);
		response.setSenderName(jz.name);
		response.setPosX(player.getPosX());
		response.setPosY(player.getPosY());
		response.setPosZ(player.getPosZ());
		response.setRoleId(jz.roleId);
		response.setAllianceName(allianceName);
		response.setRemainLife(player.currentLife);
		response.setTotalLife(player.totalLife);
		return response;
	}
	
	public void informOtherFightScene(IoSession session, Player skip) {
		log.warn("告知刚进入联盟战的玩家地图内其他玩家信息，其他玩家数量:{}： " + (players.size()-1));
		for(Player player : players.values()){
			if(player.equals(skip) ){
				continue;
			}
			
			JunZhu jz = JunZhuMgr.inst.getJunZhu(player.session);
			if(jz == null) {
				log.error("进入联盟战场景通知失败，找不到君主");
				continue;
			}
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			EnterFightScene.Builder response = getEnterFightSceneResponse(jz, player);
			session.write(response.build());
			log.info("进入联盟战通知，告诉玩家:{}谁:{}在场景:{}里", skip.jzId, player.jzId, scene.name);
		}
	}
	
	protected void processStateOnFight(IoSession session, Player p) {
		informOtherFightScene(session, p);
		log.info("同步打架信息给 {}", p.getName());
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		EnterFightScene.Builder playerInfo = getEnterFightSceneResponse(jz, p);;
		broadCastEvent(playerInfo.build(), p.userId);
	}
	
	public void computeBattleData() {
		// 1. 计算本次战斗是否结束
		if(isBattleOver()) {
			// TODO 战斗结束操作
			processBattleResult();
			return; 
		}
		// 2. 计算营地信息情况
		for(CampsiteInfo campsite : campsiteInfoList) {
			// 更新在营地占领范围内的玩家人数情况
			campsite.allianceNumMap.clear();
			for(Map.Entry<Integer, Player> entry : players.entrySet()) {
				Player player = entry.getValue();
				if(player.currentLife <= 0) {
					continue;
				}
				int distance = (int) Math.sqrt(
						Math.pow(campsite.x - player.posX, 2)+
						Math.pow(campsite.z - player.posZ, 2));
				distance = Math.abs(distance);
				int allianceId = getAllianceIdByJzId(player.jzId);
				Set<Integer> playerSet = campsite.allianceNumMap.get(allianceId);
				if(playerSet == null) {
					playerSet = new HashSet<Integer>();
					campsite.allianceNumMap.put(allianceId, playerSet);
				}
				if(distance <= campsite.radius) {
					playerSet.add(allianceId);
				} 
			}
			// 更新营地占领值
			int interval = (int) ((System.currentTimeMillis() - campsite.lastHoldValueChangeTime)/ 1000);
			if(interval >= 1) {
				int teamRedPlayerNum = 0;
				int teamBluePlayerNum = 0;
				Integer redAllianceId = teamMap.get(TEAM_RED);
				if(redAllianceId != null && campsite.allianceNumMap.get(teamMap.get(TEAM_RED)) != null) {
					teamRedPlayerNum = campsite.allianceNumMap.get(teamMap.get(TEAM_RED)).size();
				}
				Integer blueAllianceId = teamMap.get(TEAM_BLUE);
				if(blueAllianceId != null && campsite.allianceNumMap.get(teamMap.get(TEAM_BLUE)) != null) {
					teamBluePlayerNum = campsite.allianceNumMap.get(teamMap.get(TEAM_BLUE)).size();
				}
				
				int addValue = interval / 1 * campsite.zhanlingzhiAdd;
				if(teamRedPlayerNum > teamBluePlayerNum) {
					campsite.cursorDir = CURSOR_POS_RED;
					campsite.curHoldValue += addValue;
				} else if(teamRedPlayerNum < teamBluePlayerNum) {
					campsite.cursorDir = CURSOR_POS_BLUE;
					campsite.curHoldValue -= addValue;
				} else {
					campsite.cursorDir = CURSOR_POS_DEAD;
				}
				
				int sign = 1; // -1蓝方 或 1红方，代表当前占领值属于哪方
				if(campsite.curHoldValue != 0) {
					sign = campsite.curHoldValue / Math.abs(campsite.curHoldValue);
				}	
				campsite.curHoldValue = Math.min(campsite.zhanlingzhiMax, Math.abs(campsite.curHoldValue));
				campsite.curHoldValue = campsite.curHoldValue * sign;
				if(campsite.curHoldValue > 0) {
					campsite.perSecondsHoldValue = campsite.zhanlingzhiAdd;
					campsite.cursorPos = CURSOR_POS_RED;
				} else if(campsite.curHoldValue < 0) {
					campsite.perSecondsHoldValue = campsite.zhanlingzhiAdd;
					campsite.cursorPos = CURSOR_POS_BLUE;
				} else {
					campsite.cursorPos = CURSOR_POS_DEAD;
					campsite.perSecondsHoldValue = 0;
				}
				campsite.lastHoldValueChangeTime = System.currentTimeMillis();
//				System.out.println("营地:" + campsite.id + "当前占领值:"+ campsite.curHoldValue);//FIXME 调试打印
			}
			
			int holdAllianceId = 0;
			if(Math.abs(campsite.curHoldValue) >= 100 ) {
				if(campsite.curHoldValue < 0) {
					holdAllianceId = teamMap.get(TEAM_BLUE);
				} else if(campsite.curHoldValue > 0) {
					holdAllianceId = teamMap.get(TEAM_RED);
				}
			}
			
			// 更新积分每秒增长值
			if(holdAllianceId > 0) {
				for(Map.Entry<Integer,ScoreInfo> entry : scoreInfoMap.entrySet()) {
					ScoreInfo scoreInfo = entry.getValue();
					if(entry.getKey() == holdAllianceId) {
						scoreInfo.holdCampsite.add(campsite);
					} else {
						scoreInfo.holdCampsite.remove(campsite);
					}
					scoreInfo.perSecondAddRate = AllianceFightMgr.getScorePerSecondAdd(scoreInfo.holdCampsite.size());
				}
			}
		}
		
		if((System.currentTimeMillis() - lastBroadcastTime)/ 1000 > 1) {
			broadcastBattleInfo();
			lastBroadcastTime = System.currentTimeMillis();
		}
		
	}
	
	private void broadcastBattleInfo() {
		List<IoSession> sessionList = getAllPlayerSession();
		if(sessionList.size() == 0) {
			return;
		}
		int endRemainTime = (int) ((fightEndTime - System.currentTimeMillis()) / 1000);
		endRemainTime = Math.max(endRemainTime, 0);
		BattlefieldInfoNotify.Builder response = BattlefieldInfoNotify.newBuilder();
		response.setEndRemainTime(endRemainTime);
		for(Map.Entry<Integer, ScoreInfo> entry : this.scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			BattleData.Builder battleData = BattleData.newBuilder();
			battleData.setAllianceId(scoreInfo.allianceId);
			battleData.setAllianceName(scoreInfo.allianceName);
			battleData.setScore(scoreInfo.score);
			battleData.setScoreMax(AllianceFightMgr.lmzConfig.scoreMax);
			battleData.setTeam(scoreInfo.teamId);
			battleData.setHoldNum(scoreInfo.holdCampsite.size());
			response.addBattleDatas(battleData);
		}
		for(CampsiteInfo siteInfo : this.campsiteInfoList) {
			CampInfo.Builder campInfo = CampInfo.newBuilder();
			campInfo.setId(siteInfo.id);
			campInfo.setCursorPos(siteInfo.cursorPos);
			campInfo.setCursorDir(siteInfo.cursorDir);
			campInfo.setPerSecondsHoldValue(siteInfo.perSecondsHoldValue);
			campInfo.setCurHoldValue(Math.abs(siteInfo.curHoldValue));
			response.addCampInfos(campInfo);
		}
		
		for(IoSession session : sessionList) {
			session.write(response.build());
		}
	}

	protected List<IoSession> getAllPlayerSession() {
		List<IoSession> sessionList = new ArrayList<IoSession>();
		for(Map.Entry<Integer, ScoreInfo> entry : scoreInfoMap.entrySet()) {
			Set<Long> set = entry.getValue().junzhuIdSet;
			for(Long jzId : set) {
				IoSession session = SessionManager.inst.getIoSession(jzId);
				if(session != null) {
					sessionList.add(session);
				}
			}
		}
		return sessionList;
	}
	
	private void processBattleResult() {
		int winAllianceId = 0;
		int redAllianceId = teamMap.get(TEAM_RED);
		int blueAllianceId = teamMap.get(TEAM_BLUE);
		
		int redScore = scoreInfoMap.get(redAllianceId).score;
		int blueScore = scoreInfoMap.get(blueAllianceId).score;
		if(redScore > blueScore) {
			winAllianceId = redAllianceId;
		} else if(redScore < blueScore) {
			winAllianceId = blueAllianceId;
		} else {
			int redHoldCampsiteNum =  scoreInfoMap.get(redAllianceId).holdCampsite.size();
			int blueHoldCampsiteNum =  scoreInfoMap.get(blueAllianceId).holdCampsite.size();
			if(redHoldCampsiteNum > blueHoldCampsiteNum) {
				winAllianceId = redAllianceId;
			} else if(redHoldCampsiteNum < blueHoldCampsiteNum) {
				winAllianceId = blueAllianceId;
			} else {
				long redRank = RankingMgr.inst.getRankById(RankingMgr.LIANMENG_RANK, redAllianceId);
				long blueRank = RankingMgr.inst.getRankById(RankingMgr.LIANMENG_RANK, blueAllianceId);
				if(redRank > blueRank) {
					winAllianceId = redAllianceId;
				} else if(redRank < blueRank) {
					winAllianceId = blueAllianceId;
				}
			}
		}
			
		
		long costTime = fightEndTime - fightStartTime;
		if(System.currentTimeMillis() < fightEndTime) {
			costTime = System.currentTimeMillis() - fightStartTime;
		}
		
		AwardTemp awardTemp = new AwardTemp();
		awardTemp.setItemType(0);
		awardTemp.setItemId(900001);
		awardTemp.setItemNum(100);
		
		BattleResultAllianceFight.Builder response = BattleResultAllianceFight.newBuilder();
		response.setCostTime((int) (costTime/1000));
		for(Map.Entry<Integer, ScoreInfo> entry : scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			if(scoreInfo.allianceId == winAllianceId) {
				response.setResult(true);
			} else {
				response.setResult(false);
			}
			AwardItem.Builder award = AwardItem.newBuilder();
			award.setAwardId(awardTemp.getItemId());
			award.setAwardNum(awardTemp.getItemNum());
			award.setAwardItemType(awardTemp.getItemType());
			int iconId = AwardMgr.inst.getItemIconid(awardTemp.getItemType(), awardTemp.getItemId());
			award.setAwardIconId(iconId);
			response.addAwardItems(award.build());
			
			Set<Long> set = entry.getValue().junzhuIdSet;
			for(Long jzId : set) {
				IoSession session = SessionManager.inst.getIoSession(jzId);
				if(session != null) {
					session.write(response.build());
				}
			}
		}
		
		// 释放资源， 移除战斗场景
		FightScene fightScene = BigSwitch.inst.cdTimeMgr.removeFightScene(this);
		BigSwitch.inst.scMgr.fightScenes.remove(this.id);
		fightScene.destory();
		fightScene = null;
	}

	protected boolean isBattleOver() {
		boolean battleOver = false;
		for(Map.Entry<Integer,ScoreInfo> entry : scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			if(scoreInfo.score >= AllianceFightMgr.lmzConfig.scoreMax) {
				battleOver = true;
				break;
			}
		}
		if(!battleOver) {
			if(System.currentTimeMillis() >= fightEndTime) {
				battleOver = true;
			}
		}
		return battleOver;
	}
	
	/**
	 * 更新双方的分数值
	 */
	public void updateScore() {
		for(Map.Entry<Integer,ScoreInfo> entry : scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			scoreInfo.changeScore();
		}
	}
	
	public int getAllianceIdByJzId(long junzhuId) {
		int allianceId = 0;
		for(Map.Entry<Integer, ScoreInfo> entry : scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			if(scoreInfo.containJunZhu(junzhuId)){
				allianceId = entry.getKey();
				break;
			}
		}
		return allianceId;
	}
	
	public void destory () {
		scoreInfoMap.clear(); //= null;
		teamMap.clear();// = null;
		remainTeamSet.clear();// = null;
		campsiteInfoList.clear();//= null;
	}

}
