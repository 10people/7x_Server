package com.qx.alliancefight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Action;
import com.manu.dynasty.template.CartNPCTemp;
import com.manu.dynasty.template.LMZBuildingTemp;
import com.manu.dynasty.template.Lianmengzhan;
import com.manu.dynasty.template.Skill;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.buff.BuffMgr;
import com.qx.buff.SkillActionType;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.ranking.RankingMgr;
import com.qx.world.FightScene;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.AllianceFightProtos.ApplyFightResp;
import qxmobile.protobuf.AllianceFightProtos.BattleData;
import qxmobile.protobuf.AllianceFightProtos.BattlefieldInfoResp;
import qxmobile.protobuf.AllianceFightProtos.CampInfo;
import qxmobile.protobuf.AllianceFightProtos.FightAttackReq;
import qxmobile.protobuf.AllianceFightProtos.FightAttackResp;
import qxmobile.protobuf.AllianceFightProtos.FightHistoryInfo;
import qxmobile.protobuf.AllianceFightProtos.FightHistoryResp;
import qxmobile.protobuf.AllianceFightProtos.FightLasttimeRankResp;
import qxmobile.protobuf.AllianceFightProtos.FightMatchInfo;
import qxmobile.protobuf.AllianceFightProtos.FightRankInfo;
import qxmobile.protobuf.AllianceFightProtos.PlayerDeadNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.AllianceFightProtos.RequestFightInfoResp;
import qxmobile.protobuf.AllianceFightProtos.Result;

public class AllianceFightMgr {
	public static AllianceFightMgr inst;
		
	public static Logger logger = LoggerFactory.getLogger(AllianceFightMgr.class);
	
	public final String RULE_FIGHT_SEASON = "season";
	
	public static Lianmengzhan lmzConfig = null;
	
	/** 联盟战中进入战斗的出生点和营地的坐标， key：1-出生点，2-营地 */
	public static Map<Integer, List<LMZBuildingTemp>> lmzBuildMap = null;
	
	/** 时间格式解析，联盟战文件配置的时间格式应是HH:mm，例如：9:00, 23:30 */
	public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
	
	/** 玩家技能冷却map  <junzhuId, map<skillId, endTime>> */
	public Map<Long, Map<Integer, Long>> skillCDTimeMap = new HashMap<Long, Map<Integer,Long>>();

	/** 玩家技能公共冷却结束时间 */
	public long skillGroupEndTime = System.currentTimeMillis();
	
	/** 玩家马车第一次被攻击记录<君主id, 第一次被攻击时间> */
	public Map<Long, Long> cartInjuredFirstRecord = new HashMap<Long, Long>();
	
	public int fightState = 8;
	
	/** 今日联盟战状态（0-未开始，1-正在进行中，2-已经结束） */
	public int dayFightState = 0;
	
	public AllianceFightMgr() {
		inst = this;
		initData();
	}
	
	public void initData() {
		List list = TempletService.listAll(Lianmengzhan.class.getSimpleName());
		if(list == null || list.size() <= 0) {
			logger.error("Lianmengzhan配置加载错误");
			return;
		}
		lmzConfig = (Lianmengzhan) list.get(0);
		
		Map<Integer, List<LMZBuildingTemp>> lmzBuildMap = new HashMap<Integer, List<LMZBuildingTemp>>();
		List<LMZBuildingTemp> buildList = TempletService.listAll(LMZBuildingTemp.class.getSimpleName());
		for(LMZBuildingTemp build : buildList) {
			List<LMZBuildingTemp> blist = lmzBuildMap.get(build.type);
			if(blist == null) {
				blist = new ArrayList<LMZBuildingTemp>();
				lmzBuildMap.put(build.type, blist);
			}
			blist.add(build);
		}
		this.lmzBuildMap = lmzBuildMap;
		
	}

	public void requestFightInfo(IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("联盟战信息请求失败，找不到君主信息");
			return;
		}
		
		AllianceFightRules fightRule = HibernateUtil.find(AllianceFightRules.class,
				" where ruleName='" + RULE_FIGHT_SEASON+"'");
		//如果数据库找不到该条记录，表示从没进行过联盟战，下一届应该是第一届
		if(fightRule == null) {		
			fightRule = new AllianceFightRules();
			fightRule.ruleName = RULE_FIGHT_SEASON;
			fightRule.ruleValue = String.valueOf(1);
			HibernateUtil.insert(fightRule);
		}
		int fightNum = HibernateUtil.getColumnValueMax(AllianceFightApply.class, "fightNum");
//		// FIXME 如何判断是当前这一届还是该下一届
//		AllianceFightApply fightApply = HibernateUtil.find(AllianceFightApply.class, " where fightNum=" + fightNum);
//		if(fightApply == null) {
//			logger.error("联盟战信息请求失败，找不到第{}届的报名数据", fightNum);
//			return;
//		}
		
		
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if(alliancePlayer == null) {
			logger.error("联盟战信息请求失败，君主:{}还不是联盟成员", junZhu.id);
			return;
		}
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean( alliancePlayer.lianMengId);
		if(alliance == null) {
			logger.error("联盟战信息请求失败，找不到君主:{}所在联盟:{}", junZhu.id, alliancePlayer.lianMengId);
			return;
		}
		
		int alliancFightstate = fightState;//getFightState(fightApply);		FIXME				
		boolean isCanApply = isCanApply(alliance);
		boolean isApply = isApply(alliance.id);
		boolean isCanFight = isCanFight(alliance);
		long applyRemainTime = getApplyRemainTime();
		List<AllianceFightMatch> matchList = getAllianceFightMatchList(1);//FIXME
		//int curFightingState = getFightingState(alliancFightstate);		// 若今日有比赛的战斗状态，0-未开始，1-正在进行中，2-已经结束
		//dayFightState = getFightingState(alliancFightstate);
		String startTime = lmzConfig.warStart;
		
		RequestFightInfoResp.Builder response = RequestFightInfoResp.newBuilder();
		response.setState(alliancFightstate);
		response.setIsApply(isApply);
		response.setIsCanApply(isCanApply);
		response.setIsCanFight(isCanFight);
		response.setApplyRemaintime(applyRemainTime);
		response.setFightState(dayFightState);//curFightingState);				
		response.setStartTime(startTime);
		for(AllianceFightMatch match : matchList) {
			FightMatchInfo.Builder matchInfo = FightMatchInfo.newBuilder();
			matchInfo.setLm1Id(match.allianceId1);
			matchInfo.setLm1Name(AllianceMgr.getAllianceName(match.allianceId1));
			matchInfo.setLm2Id(match.allianceId2);
			matchInfo.setLm2Name(AllianceMgr.getAllianceName(match.allianceId2));
			response.addMatchInfos(matchInfo);
		}
		session.write(response.build());
	}
	
	public List<AllianceFightMatch> getAllianceFightMatchList(int fightProgress) {
		List<AllianceFightMatch> list = new ArrayList<AllianceFightMatch>();
		if(fightProgress != FightProgress.NONE.getCode() &&
				fightProgress != FightProgress.APPLY.getCode()) {
			list = HibernateUtil.list(AllianceFightMatch.class, "");
		}
		return list;
	}

	// FIXME 考虑的还不太全面	
	public int getFightingState(int state) {
		try {
			int[] battleNum = {1, 2, 3,4,5,6,7};
			if(ArrayUtils.contains(battleNum, state)) {
				Calendar calendar = Calendar.getInstance();
				Date nowDate = calendar.getTime();
				
				Date startDate = null;
				Date parseStartDate = simpleDateFormat.parse(lmzConfig.warStart);
				calendar.set(Calendar.HOUR_OF_DAY, parseStartDate.getHours());
				calendar.set(Calendar.MINUTE, parseStartDate.getMinutes());
				calendar.set(Calendar.SECOND, 0);
				startDate = calendar.getTime();
				if(nowDate.before(startDate)) {
					return 0;
				}
				
				Date endDate = null;
				Date parseEndDate = simpleDateFormat.parse(lmzConfig.warStop);
				calendar.set(Calendar.HOUR_OF_DAY, parseEndDate.getHours());
				calendar.set(Calendar.MINUTE, parseEndDate.getMinutes());
				calendar.set(Calendar.SECOND, 0);
				endDate = calendar.getTime();
				if(nowDate.after(startDate) && nowDate.before(endDate)) {
					return 1;
				}
				if(nowDate.after(endDate)) {
					return 2;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 该联盟是否已经报名 
	 * @param allianceId
	 * @return
	 */
	public boolean isApply(int allianceId) {
		LMZBaoMingBean baoMingBean = HibernateUtil.find(LMZBaoMingBean.class, " where lmId=" + allianceId);
		if(baoMingBean == null) {
			return false;
		}
		return true;
	}

	/**
	 * 获取当前联盟战赛程
	 * @param fightApply
	 * @return  联盟战状态：赛程，0-无，1-32强，2-16强，3-8强，4-4强，5-半决赛，6-三四名比赛，7-决赛，8-报名
	 */
	public int getFightState(AllianceFightApply fightApply) {
		Set<Integer> applySet = new HashSet<Integer>(fightApply.getApplyIdSet());
		applySet.removeAll(fightApply.getOutIdSet());
		int num = applySet.size();
		int state = -1;
		if(isApplyTime()) {
			state = 8;
			return state;
		}
		if(num >= 32) {
			state = 1;
		} else if(num >= 16) {
			state = 2;
		} else if(num >= 8) {
			state = 3;
		} else if(num >= 4) {
			state = 4;
		} else if(num > 2) {
			state = 5;
		} else if(num == 2) {
			state = 7;
		}
		return state;
	}
	
	public Set<Integer> getFightRemainAlliance() {
		int fightNum = HibernateUtil.getColumnValueMax(AllianceFightApply.class, "fightNum");
		AllianceFightApply fightApply = HibernateUtil.find(AllianceFightApply.class, " where fightNum=" + fightNum);
		if(fightApply == null) {
			logger.error("联盟战信息请求失败，找不到第{}届的报名数据", fightNum);
			return Collections.emptySet();
		}
		
		Set<Integer> remainSet = new HashSet<Integer>(fightApply.getApplyIdSet());
		remainSet.removeAll(fightApply.getOutIdSet());
		return remainSet;
	}

	public long getApplyRemainTime() {
		long remainTime = -1;
		if(isApplyTime()) {
			Calendar calendar = Calendar.getInstance();
			Date nowDate = calendar.getTime();
			
			Date endDate = null;
			try {
				Date parseEndDate = simpleDateFormat.parse(lmzConfig.deadline);
				calendar.set(Calendar.HOUR_OF_DAY, parseEndDate.getHours());
				calendar.set(Calendar.MINUTE, parseEndDate.getMinutes());
				calendar.set(Calendar.SECOND, 0);
				endDate = calendar.getTime();
				
				remainTime = (endDate.getTime() - nowDate.getTime()) / 1000;
			} catch (ParseException e) {
				logger.error("联盟战斗报名错误，报名结束时间配置解析错误 exceptin:{}", e);
				e.printStackTrace();
			}
		}
		return remainTime;
	}
	
	/**
	 * 该联盟是否有参赛资格（与能否报名无关）
	 * @param alliance
	 * @return
	 */
	public boolean isCanFight(AllianceBean alliance) {
		long rank = RankingMgr.inst.getRankById(RankingMgr.LIANMENG_RANK, alliance.id);
		// 表示排名在最大允许参赛个数之后  TODO 有问题暂时先这样
		if(rank > lmzConfig.lianmengNumMax) { 
			return false;
		}
		return true;
	}
	
	/**
	 * 该联盟是否能够报名联盟战
	 * @param alliance
	 * @return
	 */
	public boolean isCanApply(AllianceBean alliance) {
		if(alliance == null) {
			return false;
		}
		if(alliance.level < lmzConfig.lianmengLvMin) {
			return false;
		}
		if(alliance.members < lmzConfig.memNumMin) {
			return false;
		}
		return true;
	}

	public void applyFight(IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("联盟战报名失败，找不到君主信息");
			return;
		}
		//报名结果0-成功，1-不是联盟成员，2-没有报名权限，3-找不到所在联盟，4-联盟等级不足，5-联盟成员数不足，
		//6-联盟建设值不足，7-现在不是报名时间，8-已经报名
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if(alliancePlayer == null) {
			sendApplyFightResult(session, 1);
			logger.error("联盟战报名失败，君主:{}还不是联盟成员", junZhu.id);
			return;
		}
		if(!AllianceMgr.vilidateTitle(alliancePlayer.title, AllianceMgr.SIGN_UP_ALLIANCEFIGHT)) {
			logger.error("联盟战报名失败，君主:{}的职位:{},没有权限报名", junZhu.id, alliancePlayer.title);
			sendApplyFightResult(session, 2);
			return;
		}
		
		AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(alliancePlayer.lianMengId);
		if(alliance == null) {
			logger.error("联盟战报名失败，找不到君主:{}所在联盟:{}", junZhu.id, alliancePlayer.lianMengId);
			sendApplyFightResult(session, 3);
			return;
		}
		if(alliance.level < lmzConfig.lianmengLvMin) {
			logger.error("联盟战报名失败，君主:{}所在联盟:{}的等级:{},条件不满足等级条件{}", junZhu.id, 
					alliancePlayer.lianMengId, alliance.level, lmzConfig.lianmengLvMin);
			sendApplyFightResult(session, 4);
			return;
		}
		if(alliance.members < lmzConfig.memNumMin) {
			logger.error("联盟战报名失败，君主:{}所在联盟:{}的人数:{},条件不满足人数条件{}", junZhu.id, 
					alliancePlayer.lianMengId, alliance.members, lmzConfig.memNumMin);
			sendApplyFightResult(session, 5);
			return;
		}
		if(alliance.build < lmzConfig.jianshezhi){
			logger.error("联盟战报名失败，君主:{}所在联盟:{}的建设值:{},条件不满足{}", junZhu.id, 
					alliancePlayer.lianMengId, alliance.build, lmzConfig.jianshezhi);
			sendApplyFightResult(session, 6);
			return;
		}
		
		LMZBaoMingBean baoMingBean = HibernateUtil.find(LMZBaoMingBean.class, " where lmId=" + alliance.id);
		if(baoMingBean != null) {
			logger.error("联盟战报名失败，君主:{}所在联盟:{}已经报名", junZhu.id, alliance.id);
			sendApplyFightResult(session, 8);
			return;
		}
		
		Date date = new Date();
		if(!isApplyTime()) {
			logger.error("联盟战报名失败，现在时间nowTime:{}不能报名", date);
			sendApplyFightResult(session, 7);
			return;
		}
		
		baoMingBean = new LMZBaoMingBean();
		baoMingBean.lmId = alliance.id;
		baoMingBean.lmName = alliance.name;
		baoMingBean.mengZhuId = junZhu.id;
		baoMingBean.mengZhuName = junZhu.name;
		baoMingBean.baoMingTime = date;
		baoMingBean.season = 1;
		HibernateUtil.save(baoMingBean);
		AllianceMgr.inst.changeAlianceBuild(alliance, -1 * lmzConfig.jianshezhi);
		logger.info("联盟战报名成功，君主:{}所在联盟:{}的建设值消耗:{}", junZhu.id, 
				alliancePlayer.lianMengId, lmzConfig.memNumMin);
		sendApplyFightResult(session, 0);
		
	}
	
	public void sendApplyFightResult(IoSession session, int result) {
		ApplyFightResp.Builder response = ApplyFightResp.newBuilder();
		response.setResult(result);
		session.write(response.build());
	}
	
	/**
	 * 当前是否可以报名
	 * 
	 * @return true-可以报名，false-不可以报名
	 */
	public boolean isApplyTime() {
		Calendar calendar = Calendar.getInstance();
		Date nowDate = calendar.getTime();
		
		Date startDate = null;
		try {
			Date parseStartTime = simpleDateFormat.parse(lmzConfig.startTime);
			calendar.set(Calendar.HOUR_OF_DAY, parseStartTime.getHours());
			calendar.set(Calendar.MINUTE, parseStartTime.getMinutes());
			calendar.set(Calendar.SECOND, 0);
			startDate = calendar.getTime();
		} catch (ParseException e) {
			logger.error("联盟战报名错误，报名开始时间配置解析错误 exceptin:{}", e);
			e.printStackTrace();
		}
		if(nowDate.equals(startDate)) {
			return true;
		} 
		
		Date endDate = null;
		try {
			Date parseEndDate = simpleDateFormat.parse(lmzConfig.deadline);
			calendar.set(Calendar.HOUR_OF_DAY, parseEndDate.getHours());
			calendar.set(Calendar.MINUTE, parseEndDate.getMinutes());
			calendar.set(Calendar.SECOND, 0);
			endDate = calendar.getTime();
		} catch (ParseException e) {
			logger.error("联盟战斗报名错误，报名结束时间配置解析错误 exceptin:{}", e);
			e.printStackTrace();
		}
		
		if(nowDate.after(startDate) && nowDate.before(endDate)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 战斗匹配
	 */
	public void matchFight() {
		List<LMZBaoMingBean> baoMingList = HibernateUtil.list(LMZBaoMingBean.class, "");
		if(baoMingList.size() <= 0) {
			logger.info("联盟战匹配失败，没有报名的联盟");
			return;
		}
		Set<Integer> remainAllianceSet = new HashSet<Integer>();//getFightRemainAlliance();
		for(LMZBaoMingBean bean : baoMingList) {
			remainAllianceSet.add(bean.lmId);
		}
		int size = remainAllianceSet.size();
		Integer[] idArray = remainAllianceSet.toArray(new Integer[size]);
				
		for(int i = 0; i < idArray.length; i+=2) {
			int allianceId1 = idArray[i];
			int allianceId2 = -1;
			if(i != idArray.length - 1) {
				allianceId2 = idArray[i+1];
			}
			AllianceFightMatch fightMatch = new AllianceFightMatch(allianceId1, allianceId2, 1);
			HibernateUtil.save(fightMatch);
		}
		
	}
	
	public boolean isCartOfTeammate(Player targetPlayer, Player attackPlayer) {
		int attackerLmId = attackPlayer.allianceId;
		int defenderLmId = targetPlayer.allianceId;
		if((attackerLmId > 0 && defenderLmId > 0) && attackerLmId == defenderLmId) {
			return true;
		} else if((attackerLmId <=0 || defenderLmId <= 0) && targetPlayer.jzId == attackPlayer.jzId) {
			return true;
		}
		return false;
	}

	public boolean isTargetIsSelf(int targetUid, int attackUid) {
		return attackUid== targetUid;
	}

	public boolean isTeammate(Player targetPlayer, Player attackPlayer) {
		int attackerLmId = attackPlayer.allianceId;
		int defenderLmId = targetPlayer.allianceId;
		if((attackerLmId > 0 && defenderLmId > 0) && attackerLmId == defenderLmId) {
			return true;
		} 
		return false;
	}
	
	public Result verifySkill(Skill skill, boolean targetIsSelf, float distance, boolean teammate, Long attackerId) {
		if(skill == null) {		
			return Result.SKILL_NOT_EXIST;
		}
		Action action = BigSwitch.inst.buffMgr.getActionById(skill.Action1);
		if(action == null) {
			logger.error("攻击失败，找不到action配置，skillId:{}", skill.SkillId);
			return Result.SKILL_NOT_EXIST;
		}
		SkillActionType skillActionType = SkillActionType.getSkillActionType(action.TypeKey);
		if(skillActionType == null) {
			logger.error("攻击失败，无效的ActionTypeKey配置，skillId:{}", skill.SkillId);
			return Result.SKILL_NOT_EXIST;
		}
		if(skill.SkillTarget == 0 && !targetIsSelf) {
			return Result.SKILL_TARGET_NOT_OTHER;
		} else if(skill.SkillTarget == 1 && targetIsSelf){
			return Result.SKILL_TARGET_NOT_SELF;
		}
		// 判断距离
		if(distance > skill.Range_Max || distance < skill.Range_Min) {
			return Result.SKILL_DISTANCE_ERROR;
		}
		
		long currentTime = System.currentTimeMillis();
		// 判断是否受公共cd影响 
		if(skill.IsInGCD == 1 && currentTime < skillGroupEndTime) {
			return Result.SKILL_COOL_TIME;
		}
		// 判断cd时间
		Map<Integer, Long> skillCDMap = skillCDTimeMap.get(attackerId);
		if(skillCDMap != null) {
			Long endTime = skillCDMap.get(skill.SkillId);
			if(endTime != null && endTime >= currentTime) {
				return Result.SKILL_COOL_TIME;
			}
		}
		
		if(!targetIsSelf) {
			// 判断敌友方
			if(skill.CRRejectU == 1 && !teammate) { //1.不可对敌对单位使用
				return Result.SKILL_TARGET_NOT_ENEMY;
			} else if(skill.CRRejectU == 2 && teammate) {//2.不可对友方单位使用
				return Result.SKILL_TARGET_NOT_TEAMMATE ;
			}
		}
		return Result.SUCCESS;
	}
	
	public void sendAttackError(Result result, Scene scene, int attackUid) {
		sendAttackResponse(attackUid, 0, result, 0, scene, 0, 0, false);
	}

	public void sendAttackResponse(int attackUid, int targetUid, Result result,
			int skillId, Scene scene, long damageValue, int remainLife, boolean succeed) {
		FightAttackResp.Builder response = FightAttackResp.newBuilder();
		response.setResult(result);
		response.setAttackUid(attackUid);
		response.setTargetUid(targetUid);
		response.setSkillId(skillId);
//		response.setDamage(damageValue);		
		response.setRemainLife(remainLife);
		
		if(succeed) {
			for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
				Player p = entry.getValue();
				p.session.write(response.build());
			}
		} else {
			Player p = scene.players.get(attackUid);
			p.session.write(response.build());
		}
	}
	
	/**
	 * 更新君主技能cd时间
	 * 
	 * @param junzhuId
	 * @param skillId
	 */
	public void updateSkillCdTime(long junzhuId, Skill skill) {
		Map<Integer, Long> skillCDMap = skillCDTimeMap.get(junzhuId);
		if(skillCDMap == null) {
			synchronized (skillCDTimeMap) {
				if(skillCDMap == null) {
					skillCDMap = new HashMap<Integer, Long>();
					skillCDTimeMap.put(junzhuId, skillCDMap);
				}
			}
		}
		skillCDMap.put(skill.SkillId, System.currentTimeMillis() + skill.BaseCD);
		if(skill.IsInGCD == 1) {
			skillGroupEndTime = System.currentTimeMillis() + skill.CDGroup;
		}
	}

	public void processPlayerDead(Scene scene, JunZhu defender, int uid) {
		CdTime cdTime = new CdTime(defender.id, System.currentTimeMillis() + lmzConfig.reviveTime * 1000);
		BigSwitch.inst.cdTimeMgr.addCdTime(cdTime);
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(uid);
		for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
			Player p = entry.getValue();
			p.session.write(deadNotify.build());
			//logger.info("通知玩家:{},{}死亡,剩余血量{}", player.jzId, attacker.id, remainLife);
		}
	}
	
	public void processYBPlayerDead(Scene scene, JunZhu defender, int uid, int killerUid) {
//		CdTime cdTime = new CdTime(defender.id, System.currentTimeMillis() + YunbiaoTemp.autoResurgenceTime * 1000);
//		BigSwitch.inst.cdTimeMgr.addCdTime(cdTime);
		
		BuffMgr.inst.removeBuff(defender.id);
		int onSiteReviveCost = 10;
		int remainReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(defender);
		
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(uid);
		deadNotify.setKillerUid(killerUid);
		deadNotify.setAutoReviveRemainTime(YunbiaoTemp.autoResurgenceTime);
		deadNotify.setRemainAllLifeTimes(remainReviveTimes);
		deadNotify.setOnSiteReviveCost(onSiteReviveCost);
		for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
			Player p = entry.getValue();
			p.session.write(deadNotify.build());
			//logger.info("通知玩家:{},{}死亡,剩余血量{}", player.jzId, attacker.id, remainLife);
		}
	}
	
	/**
	 * TODO 暂时随机计算， 没有按数值公式计算
	 * 计算攻击者对防守者造成的伤害值
	 * @param attacker		攻击者
	 * @param defender		防守者
	 * @return
	 */
	public int getDamage(JunZhu attacker, JunZhu defender) {
		return RandomUtils.nextInt(100);
	}
	
	
	/**
	 * 进入战斗场景后，请求战况信息
	 * @param session
	 */
	public void requestBattlefieldInfo(IoSession session) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("请求战场信息失败，找不到君主");
			return;
		}
		if(!(session.getAttribute(SessionAttKey.Scene) instanceof FightScene)){
			logger.error("请求战场信息失败，君主不在战场中");
			return;
		}
		FightScene scene = (FightScene) session.getAttribute(SessionAttKey.Scene);
		if(scene == null) {
			logger.error("请求战场信息失败，找不到君主:{}的场景信息", junzhu.id);
			return;
		}
		Player player = scene.getPlayerByJunZhuId(junzhu.id);
		if(player == null) {
			logger.error("请求战场信息失败，君主:{}不在场景:{}里", junzhu.id, scene.name);
			return;
		}
		
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(junzhu.id);
		if(alliancePlayer == null) {
			logger.error("请求战场信息失败，君主:{}还不是联盟成员", junzhu.id);
			return;
		}
		AllianceBean alliance =AllianceBeanDao.inst.getAllianceBean(alliancePlayer.lianMengId);
		if(alliance == null) {
			logger.error("请求战场信息失败，找不到君主:{}所在联盟:{}", junzhu.id, alliancePlayer.lianMengId);
			return;
		}
		
		Integer remainLife = player.currentLife;
		int fightEndRemainTime = (int) ((scene.fightEndTime - System.currentTimeMillis()) / 1000);
		String allianceName = alliance.name;
		
		BattlefieldInfoResp.Builder response = BattlefieldInfoResp.newBuilder();
		response.setUid(player.userId);
		response.setJunZhuId(junzhu.id);
		response.setSenderName(junzhu.name);
		response.setPosX(player.getPosX());
		response.setPosY(player.getPosY());
		response.setPosZ(player.getPosZ());
		response.setRoleId(junzhu.roleId);
		response.setAllianceName(allianceName);
		response.setRemainLife(remainLife == null ? 0 : remainLife);
		response.setTotalLife(junzhu.shengMingMax);
		response.setEndRemainTime(fightEndRemainTime);
		for(Map.Entry<Integer, ScoreInfo> entry : scene.scoreInfoMap.entrySet()) {
			ScoreInfo scoreInfo = entry.getValue();
			BattleData.Builder battleData = BattleData.newBuilder();
			battleData.setAllianceId(scoreInfo.allianceId);
			battleData.setAllianceName(scoreInfo.allianceName);
			battleData.setScore(scoreInfo.score);
			battleData.setScoreMax(lmzConfig.scoreMax);
			battleData.setTeam(scoreInfo.teamId);
			battleData.setHoldNum(scoreInfo.holdCampsite.size());
			response.addBattleDatas(battleData);
		}
		
		for(CampsiteInfo siteInfo : scene.campsiteInfoList.values()) {
			CampInfo.Builder campInfo = CampInfo.newBuilder();
			campInfo.setId(siteInfo.id);
			campInfo.setCursorPos(siteInfo.cursorPos);
			campInfo.setCursorDir(siteInfo.cursorDir);
			campInfo.setPerSecondsHoldValue(siteInfo.zhanlingzhiMax);
			campInfo.setCurHoldValue(Math.abs(siteInfo.curHoldValue));
			response.addCampInfos(campInfo);
		}
		session.write(response.build());
	}

	public void requestFightHistory(IoSession session) {
		List<AllianceFightHistory> historyList = HibernateUtil.list(AllianceFightHistory.class, "");
		FightHistoryResp.Builder response = FightHistoryResp.newBuilder();
		for(AllianceFightHistory history : historyList) {
			FightHistoryInfo.Builder historyInfo = FightHistoryInfo.newBuilder();
			historyInfo.setTimes(history.battleRound);
			historyInfo.setLm1Id(history.allianceId1);
			historyInfo.setLm1Name(history.alliance1Name);
			historyInfo.setLm2Id(history.allianceId2);
			historyInfo.setLm2Name(history.alliance2Name);
			historyInfo.setWinLmId(history.winAllianceId);
			response.addHistoryInfos(historyInfo);
		}
		
		session.write(response.build());
	}
	
	public void requestFightLasttimeRank(IoSession session) {
		FightLasttimeRankResp.Builder response = FightLasttimeRankResp.newBuilder();
		for(int i = 1; i < 5; i++) {
			FightRankInfo.Builder rankInfo = FightRankInfo.newBuilder();
			rankInfo.setLmId(i+10000);
			rankInfo.setLmName("假的联盟数据" + (i+10000));
			rankInfo.setRank(i);
			response.addRankInfos(rankInfo);
		}
		session.write(response.build());
	}
	
	public static long getFightEndTime() {
		Date d = DateUtils.parseHourMinute(BidMgr.city_war_fighting_startTime);
		return d.getTime() + lmzConfig.countDown * 60 * 1000;
	}
	
	/**
	 * 获取阵营配置
	 * @param side	1-红方，2-蓝方
	 * @return
	 */
	public static LMZBuildingTemp getTeamCamp(int side) {
		LMZBuildingTemp result = null;
		List<LMZBuildingTemp> list = lmzBuildMap.get(1);
		if(list == null) {
			return null;
		}
		for(LMZBuildingTemp temp : list) {
			if(temp.side == side) {
				result = temp;
				break;
			}
		}
		return result;
	}
	
	public static int getScorePerSecondAdd(int holdCampsiteNum) {
		int socrePerSecondAdd = 0;
		switch(holdCampsiteNum) {
			case 1:
				socrePerSecondAdd = lmzConfig.scoreAdd1;
				break;
			case 2:
				socrePerSecondAdd = lmzConfig.scoreAdd2;
				break;
			case 3:
				socrePerSecondAdd = lmzConfig.scoreAdd3;
				break;
			case 4:
				socrePerSecondAdd = lmzConfig.scoreAdd4;
				break;
			case 5:
				socrePerSecondAdd = lmzConfig.scoreAdd5;
				break;
			default:
				break;
		}
		return socrePerSecondAdd;
	}

	public void reviveRequest(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			return;
		}
		
		PlayerReviveRequest.Builder request = (qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest.Builder) builder;
		int type = request.getType();
		int allLifeReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(junzhu);
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		Player player = scene.getPlayerByJunZhuId(junzhu.id);
		PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
		reviveNotify.setUid(player.userId);
		if(type == 0) {//安全区复活
			int safeAreaId = YaBiaoHuoDongMgr.inst.getNearSafeArea(player.posX, player.posZ);
			YunBiaoSafe safeArea = YaBiaoHuoDongMgr.safeAreaMap.get(safeAreaId);
			if(safeArea == null) {
				reviveNotify.setPosX(player.posX);
				reviveNotify.setPosZ(player.posZ);
			} else {
				reviveNotify.setPosX(safeArea.saveAreaX);
				reviveNotify.setPosZ(safeArea.saveAreaZ);
				player.posX = safeArea.saveAreaX;
				player.posZ = safeArea.saveAreaZ;
			}
		} else if(type == 1) {//原地安全复活
			int costYuanBao = 10;
			if(junzhu.yuanBao < costYuanBao) {
				logger.error("郡城战场景，原地复活失败，元宝不足");
				return;
			}
			reviveNotify.setPosX(player.posX);
			reviveNotify.setPosZ(player.posZ);
			YuanBaoMgr.inst.diff(junzhu, -costYuanBao, 0, costYuanBao, YBType.LMZHAN_DEAD_POS_REVIVE, "郡城战场景原地安全复活");
			HibernateUtil.save(junzhu);
		}else{		
			logger.error("郡城战场景复活失败，没有该复活类型type:{}, jzId:{}", type, junzhu.id);
			return;
		}
		if(allLifeReviveTimes > 0) {
			reviveNotify.setLife(player.totalLife);
			player.currentLife = player.totalLife;
			YaBiaoHuoDongMgr.inst.kouchuFuhuoTimes(junzhu);
		} else {
			reviveNotify.setLife(1);
			player.currentLife = 1;
		}
		logger.info("junzhu:{}复活坐标x:{},y:{}",junzhu.name, reviveNotify.getPosX(), reviveNotify.getPosZ());
		for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
			Player p = entry.getValue();
			p.session.write(reviveNotify.build());
		}
		
		YaBiaoHuoDongMgr.inst.kouchuFuhuoTimes(junzhu);
		JunZhuMgr.inst.sendMainInfo(session,junzhu,false);
	}
}
