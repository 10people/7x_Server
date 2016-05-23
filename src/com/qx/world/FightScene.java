package com.qx.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.AllianceFightProtos.ABResult;
import qxmobile.protobuf.AllianceFightProtos.AOESkill;
import qxmobile.protobuf.AllianceFightProtos.BattleData;
import qxmobile.protobuf.AllianceFightProtos.BattlefieldInfoNotify;
import qxmobile.protobuf.AllianceFightProtos.BufferInfo;
import qxmobile.protobuf.AllianceFightProtos.CampInfo;
import qxmobile.protobuf.AllianceFightProtos.FightAttackReq;
import qxmobile.protobuf.AllianceFightProtos.FightAttackResp;
import qxmobile.protobuf.AllianceFightProtos.PlayerDeadNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.AllianceFightProtos.PlayerScore;
import qxmobile.protobuf.AllianceFightProtos.Result;
import qxmobile.protobuf.AllianceFightProtos.ScoreList;
import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResultAllianceFight;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Prompt.PromptMSGResp;
import qxmobile.protobuf.Prompt.SuBaoMSG;
import qxmobile.protobuf.Scene.EnterFightScene;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Yabiao.BuyXuePingReq;
import qxmobile.protobuf.Yabiao.BuyXuePingResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.Buff;
import com.manu.dynasty.template.JCZChenghao;
import com.manu.dynasty.template.JCZCity;
import com.manu.dynasty.template.JCZCommand;
import com.manu.dynasty.template.JCZNpcTemp;
import com.manu.dynasty.template.JCZPersonalAward;
import com.manu.dynasty.template.LMZBuildingTemp;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.Skill;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliancefight.AllianceFightMatch;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.alliancefight.BidMgr;
import com.qx.alliancefight.CampsiteInfo;
import com.qx.alliancefight.CityBean;
import com.qx.alliancefight.LMZAwardBean;
import com.qx.alliancefight.ScoreInfo;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.buff.BuffMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotSession;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * 拆外塔加181，内塔加191，就一个，不叠加
 * http://192.168.0.250:81/index.php/bug/44552
 * 联盟战场景
 * @author lzw
 *
 */
public class FightScene extends VisionScene {
	public int cityId;
	public int outTowerTaken=0;
	public int innerTowerTaken=0;
	public long preCalcCampMs = 0;
	public static int killOneJiFen = 10;
	public static int fixTakeTowerSpeed = 1;
	public static long calcCampInterval = 500;
	public static int reviveOnDeadPosTimes = 60;
	public static int AutoReviveRemainTime = 15;
	public static final byte TEAM_RED = 1;				//守方 红队	对应LMZBuildingTemp type=1 side=1
	public static final byte TEAM_BLUE = 2;			//攻方 蓝队	对应LMZBuildingTemp type=1 side=2
	public int redLmId = TEAM_RED;
	public int blueLmId = TEAM_BLUE;
	public static ConcurrentHashMap<Long, Integer> freeFuHuoUsedTimes = new ConcurrentHashMap<>();
	public Map<Long, Integer> hpCache = new HashMap<>();
	public static int dayFreeFuHuoTimes = 3;
	public static int fuHuoYuanBaoPrice = 20;
	public static int fuHuoShiId = 910010;//	复活石;
	public static int zhaoHuanFuId = 910011;//召唤令
	public static int xuePingId = 910012;//	血瓶;//
	/** 战斗结束时间 */
	public long fightEndTime;
	public long prepareMS = 0;
	
	
	public int winSide=0;//
	
	/** 联盟双方的积分、营地占领情况 <联盟id, 分数情况> */
	public Map<Integer, ScoreInfo> scoreInfoMap = null;
	
	/**
	 * key-->jzId
	 */
	public Map<Long, PlayerScore.Builder> personalScoreMap = new HashMap<>();
	
	/** 战斗场景内的营地信息 */
	public Map<Integer,CampsiteInfo> campsiteInfoList = null;

	/** 上次广播联盟战信息的时间，单位-毫秒 */
	public long lastBroadcastTime;	

	public Set<Byte> remainTeamSet = null;
	public Object teamLock = new Object();
	
	public static final byte CURSOR_TO_RED = 1;		// 游标向红方移动
	public static final byte CURSOR_TO_BLUE = 2;	// 游标向蓝方移动
	public static final byte CURSOR_TO_DEAD = 3;	// 游标处于中间
	
	public static final byte CURSOR_POS_RED = 1;	// 游标位置在红方
	public static final byte CURSOR_POS_BLUE = 2;	// 游标位置在蓝方
	public static final byte CURSOR_POS_DEAD = 0;	// 游标位置在中间
	
	public int id;
	public int step;//0准备； 10战斗中,。。。。; 500结束。
	public Buff jiDiBuff;
	public long lastBuffTime;
	public FightScene(String sceneName, long fightEndTime, int id){
		super(sceneName);
		this.id = id;
		long currentTimeMillis = System.currentTimeMillis();
		this.fightEndTime = fightEndTime;
		long startAt =fightEndTime - AllianceFightMgr.lmzConfig.countDown * 60 * 1000;
		long diff = startAt - currentTimeMillis;
		prepareMS = diff;
		if(prepareMS<=0){
			step = 10;
		}
		scoreInfoMap = new HashMap<Integer, ScoreInfo>();
		remainTeamSet = new HashSet<Byte>(Arrays.asList(TEAM_RED, TEAM_BLUE));
		lastBroadcastTime = currentTimeMillis;
		
		campsiteInfoList = new HashMap<>();
//		2 是外层营地
//		3 是内层营地
//		4 是基地
		initCampsite(2);
		initCampsite(3);
		initCampsite(4);
		//
		List<Buff> buffL = TempletService.listAll(Buff.class.getSimpleName());
		Optional<Buff> op = buffL.stream().filter(b->b.BuffId==171).findAny();
		jiDiBuff = op.isPresent() ? op.get() : null;
	}
	@Override
	public void completeMission(Mission mission) {
		if(mission instanceof CallbacMission){
			CallbacMission cm = (CallbacMission) mission;
			cm.doIt();
			return;
		}
		int code = mission.code;
		final Builder builder = mission.builer;
		final IoSession session = mission.session;
		switch(mission.code){
		case PD.LMZ_BUY_ZhaoHuan:
			sendZhaoHuanShiInfo(session, builder);
			break;
		case PD.LMZ_BUY_XueP:
			sendXuePingInfo(session, builder);
			break;
		case PD.C_ENTER_LMZ:
			prepareAdd(mission.session, mission.builer);
			break;
		case PD.POS_JUMP:
			jump(session,builder);
			break;
		case PD.AOE_SKILL:
			aoe(session, builder);
			break;
		case PD.SKILL_PREPARE:
			prepareSkill(session,builder);
			break;
		case PD.LMZ_SCORE_LIST:
			sendScoreList(session);
			break;
		case PD.LMZ_FuHuo:
			fuHuo(session,builder);
			break;
		case PD.LMZ_CMD_LIST:
			sendCmdList(session);
			break;
		case PD.LMZ_CMD_ONE:
			fireCmd(session,builder);
			break;
		case PD.LMZ_ZhaoHuan:
			zhaoHuan(session);
			break;
		default:
			super.completeMission(mission);
			break;
		}
	}
	public void sendZhaoHuanShiInfo(IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求购买召唤令出错：君主不存在");
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		int remainXuePing = BagMgr.inst.getItemCount(bag, zhaoHuanFuId);
		BuyXuePingReq.Builder req=(BuyXuePingReq.Builder)builder;
		int buyCode=req.getCode();
		int buyCnt = 1;
		if(buyCode>10000){
			buyCnt = buyCode - 10000;
			buyCode = 1;
		}
		BuyXuePingResp.Builder resp=BuyXuePingResp.newBuilder();
		switch (buyCode) {
		case 1:
			//buyBlood(jz, resp, session);
			BagMgr.inst.addItem(bag, zhaoHuanFuId, buyCnt, 0, 0, "购买召唤令lmz");
			sendBuyBloodResp(PD.LMZ_BUY_ZhaoHuan,session, resp, 10, 1, 5, remainXuePing+buyCnt, 1);
			break;
		case 2:
			//getBuyBloodInfo(jz, resp, session);
			sendBuyBloodResp(PD.LMZ_BUY_ZhaoHuan, session, resp, 40, 1, 5, remainXuePing, 1);
			break;
		default:
			break;
		}		
	}
	public void sendXuePingInfo(IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求购买血瓶出错：君主不存在");
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		int remainXuePing = BagMgr.inst.getItemCount(bag, xuePingId);
		BuyXuePingReq.Builder req=(BuyXuePingReq.Builder)builder;
		int buyCode=req.getCode();
		int buyCnt = 1;
		if(buyCode>10000){
			buyCnt = buyCode - 10000;
			buyCode = 1;
		}
		BuyXuePingResp.Builder resp=BuyXuePingResp.newBuilder();
		switch (buyCode) {
		case 1:
			//buyBlood(jz, resp, session);
			BagMgr.inst.addItem(bag, xuePingId, buyCnt, 0, 0, "购买血瓶lmz");
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 10, 1, 5, remainXuePing+buyCnt, 1);
			break;
		case 2:
			//getBuyBloodInfo(jz, resp, session);
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 40, 1, 5, remainXuePing, 1);
			break;
		default:
			break;
		}		
	}
	public void sendBuyBloodResp(short id,IoSession session,BuyXuePingResp.Builder resp,int code,int cost,int remainBuyTimes,int remainXuePing,int nextGet) {
		resp.setResCode(code);
		resp.setNextCost(cost);
		resp.setNextGet(nextGet);
		resp.setRemainTimes(remainBuyTimes);
		resp.setRemainXuePing(remainXuePing);
		ProtobufMsg p = new ProtobufMsg(id,resp);
		session.write(p);
	}
	public void zhaoHuan(IoSession session) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(jzId==null){
			return;
		}
		Integer uidObject = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uidObject==null){
			return;
		}
		Player who = players.get(uidObject);
		if(who == null){
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		int cnt = BagMgr.inst.getItemCount(bag, zhaoHuanFuId);
		if(cnt<1){
			log.warn("{}召唤符不足", jzId);
			return;
		}
		BagMgr.inst.removeItem(bag, zhaoHuanFuId, 1, "召唤", 1);
		BagMgr.inst.sendBagInfo(session,bag);
		ErrorMessage.Builder req = ErrorMessage.newBuilder();
		req.setErrorCode(uidObject);
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_ZhaoHuan, req);
		for(Player player : players.values()){
			if(player.allianceId != who.allianceId){
				continue;
			}
			player.session.write(p);
		}
	}
	public void fireCmd(IoSession session, Builder builder) {
		Integer uidObject = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uidObject==null){
			return;
		}
		Player pl = players.get(uidObject);
		if(pl == null){
			return;
		}
		if(pl.zhiWu != 2){
			return;
		}
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_CMD_ONE, req);
		broadCastEvent(p, 0);
	}
	public void sendCmdList(IoSession session) {
		PromptMSGResp.Builder ret = PromptMSGResp.newBuilder();
		List<JCZCommand> list = TempletService.listAll(JCZCommand.class.getSimpleName());
		SuBaoMSG.Builder s = SuBaoMSG.newBuilder();
		for(JCZCommand c : list){
			s.setSubaoId(c.ID);
			s.setOtherJzId(0);
			s.setSubao(c.desc);
			s.setEventId(0);
			s.setConfigId(0);
			ret.addMsgList(s.build());
			if(ret.getMsgListCount()==5)break;//delete this
		}
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_CMD_LIST, ret);
		session.write(p);
	}
	public void fuHuo(IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		Player player = getPlayerByJunZhuId(jz.id);
		if(player == null){
			log.error("{} player not found",jz.id);
			return;
		}
		PlayerReviveRequest.Builder req = (PlayerReviveRequest.Builder)builder;
		boolean ok = true;
		switch(req.getType()){
		case 20:{
			//使用免费次数
			Integer usedTimes = freeFuHuoUsedTimes.get(jz.id);
			if(usedTimes == null){
				freeFuHuoUsedTimes.put(jz.id, 1);
				log.info("{}首次免费复活",jz.id);
			}else if(usedTimes<dayFreeFuHuoTimes){
				freeFuHuoUsedTimes.put(jz.id, usedTimes+1);
				log.info("{}免费复活",jz.id);
			}else{
				ok = false;
				log.info("{}今日免费复活次数不足", jz.id);
			}
			break;
		}
		case 30:{//复活石
			Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			int cnt = BagMgr.inst.getItemCount(bag, fuHuoShiId);
			if(cnt>0){
				BagMgr.inst.removeItem(bag, fuHuoShiId, 1, "联盟战复活", jz.level);
				log.info("{}使用复活石",jz.id);
			}else{
				ok = false;
				log.warn("{}复活石不足", jz.id);
			}
			break;
		}
		case 40:{//元宝
			if(jz.yuanBao>=fuHuoYuanBaoPrice){
				YuanBaoMgr.inst.diff(jz, -fuHuoYuanBaoPrice, 0, fuHuoYuanBaoPrice, YBType.LMZ_FUHUO, "联盟战复活");
				HibernateUtil.update(jz);
				log.info("{}元宝复活",jz.id);
			}else{
				ok = false;
			}
			break;
		}
		case 10:{//免费墓地复活。
			log.info("{}墓地复活",jz.id);
			break;
		}
		}
		if(ok){
			player.currentLife = player.totalLife;
		}
		//
		//选择最近的墓地
				List<LMZBuildingTemp> buildList1 = TempletService.listAll(LMZBuildingTemp.class.getSimpleName());
				Iterator<LMZBuildingTemp> siteIt = buildList1.stream()
						.filter(b->b.type==2 || b.type==3 || b.type==1).iterator();
				int side = player.allianceId == redLmId ? 1 : 2;
				LMZBuildingTemp near = null;
				int distMin = Integer.MAX_VALUE;
				while(siteIt.hasNext()){
					if(side == TEAM_RED)break;//守方只在墓地
					LMZBuildingTemp bd = siteIt.next();
					if(bd.type!=1){//营地要看占领情况
						CampsiteInfo c = campsiteInfoList.get(bd.id);
						if(side != c.curHoldValue){
							continue;
						}
						int distance = (int) Math.sqrt(
								Math.pow(c.x - player.posX, 2)+
								Math.pow(c.z - player.posZ, 2));
						distance = Math.abs(distance);
						if(distance>distMin){
							continue;
						}
						near = bd;
						distMin = distance;
					}
				}
				LMZBuildingTemp bornAt;
				if(near == null){
					//修正坐标
					List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(1);
					bornAt = buildList.get(side-1);
				}else{
					bornAt = near;
				}
		//
		PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
		reviveNotify.setUid(player.userId);
		reviveNotify.setResult(ok?0:1);
		reviveNotify.setPosX(bornAt.x);
		reviveNotify.setPosZ(bornAt.y);
		reviveNotify.setLife(player.currentLife);
		if(ok){
			broadCastEvent(reviveNotify.build(), 0);
		}else{
			session.write(reviveNotify.build());
		}
	}
	public synchronized void sendScoreList(IoSession session) {
		ScoreList.Builder s = buildScoreInfo();
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_SCORE_LIST, s);
		//broadCastEvent(p, 0);
		session.write(p);
	}
	public void bornAllNPC(){
		
	}
	@Override
	public boolean checkSkill(JunZhu attacker, Player attackPlayer, Player targetPlayer, int skillId) {
		if(skillId!=121){
			return true;
		}
		long jzId = attacker.id;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		int cnt = BagMgr.inst.getItemCount(bag, xuePingId);
		if(cnt<1){
			log.warn("{}召唤符不足", jzId);
			return false;
		}
		BagMgr.inst.removeItem(bag, xuePingId, 1, "使用血瓶", 1);
		BagMgr.inst.sendBagInfo(attackPlayer.session,bag);
		return true;
	}
	public void prepareSkill(IoSession session, Builder builder) {
		FightAttackReq.Builder req = (FightAttackReq.Builder)builder;
		Integer attackUid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		JunZhu attacker = JunZhuMgr.inst.getJunZhu(session);
		if(attacker == null || attackUid == null){
			return;
		}
		//
		int skillId = req.getSkillId();
		Skill skill = BuffMgr.inst.getSkillById(skillId);
		switch(skillId){
		case 151:
			Player p = players.get(attackUid);
			if(p == null)return;
			BigSwitch.inst.buffMgr.calcSkillDamage(attacker, null, skill, attackUid);
			break;
		}
		//
		FightAttackResp.Builder response = FightAttackResp.newBuilder();
		response.setResult(Result.SUCCESS);
		response.setAttackUid(attackUid);
		response.setTargetUid(-1);
		response.setSkillId(skillId);
		response.setDamage(0);		
		response.setRemainLife(0);
		ProtobufMsg msg = new ProtobufMsg(PD.FIGHT_ATTACK_RESP, response);
		//session.write(msg);
		broadCastEvent(msg, 0);
	}
	/*
	 * 客户端计算打到了几个人，服务器计算伤害，然后广播给施法者和其他玩家。
	 */
	public void aoe(IoSession session, Builder builder) {
		//check CD
		
		AOESkill.Builder req = (AOESkill.Builder)builder;
		List<Integer> tgtIds = req.getAffectedUidsList();
		int size = tgtIds.size();
		for(int i=0; i<size; i++){
			Player p = players.get(tgtIds.get(i));
			if(p == null){
				req.addDamages(0);
				continue;
			}
			int dmg = i;
			p.currentLife -= dmg;
			//check die
			req.addDamages(dmg);
			p.currentLife = Math.max(0, p.currentLife);
			req.addHps(p.currentLife);
		}
		ProtobufMsg pmsg = new ProtobufMsg(PD.AOE_SKILL, req);
		broadCastEvent(pmsg, 0);
	}
	public void jump(IoSession session, Builder builder) {
		Integer uid = (Integer)session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			return;
		}
		ProtobufMsg pmsg = new ProtobufMsg(PD.POS_JUMP, builder);
		broadCastEvent(pmsg, uid);
	}
	public void prepareAdd(IoSession session, Builder builer) {
		EnterScene.Builder e = EnterScene.newBuilder();
		e.setUid(0);
		//TODO 计算适当的坐标
		e.setPosX(1);
		e.setPosY(2);
		e.setPosZ(3);
		e.setJzId(0);
		enterScene(session, e);
	}
	@Override
	public void postEnter(Player p) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(p.session);
		int shengMingMax = jz.shengMingMax;
		int percent = getTowerBuffHP();
		p.session.setAttribute("fixTowerBuff", percent);
		shengMingMax += shengMingMax * percent / 100;
		p.totalLife = shengMingMax;
//		p.currentLife = 5;//99999999;
//		p.currentLife = 20;
		p.safeArea = -1;
		Integer cacheHp = hpCache.get(p.jzId);
		if(cacheHp!=null){
			p.currentLife = cacheHp;
		}else{
			p.currentLife = p.totalLife;
		}
		int side = p.allianceId == redLmId ? 1 : 2;
		LMZBuildingTemp bornAt;
		List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(1);
		bornAt = buildList.get(side-1);
		
		p.posX = bornAt.x;
		p.posZ = bornAt.y;
		p.worth=0;
		//
		PlayerScore.Builder	o = personalScoreMap.get(p.jzId);
		if(o==null){
			o = PlayerScore.newBuilder();
			o.setJiFen(0);
			o.setKillCnt(0);
			o.setLianSha(0);
			o.setRank(personalScoreMap.size()+1);
			o.setRoleName(p.name);
			o.setSide(p.allianceId==redLmId ? 1 : 2);
			personalScoreMap.put(p.jzId,o);
		}
		EventMgr.addEvent(ED.done_junChengZhan_x, new Object[]{jz.id});
	}
	
	@Override
	public qxmobile.protobuf.Scene.EnterScene.Builder buildEnterInfo(Player p) {
		EnterScene.Builder enterSc = super.buildEnterInfo(p);
		enterSc.setCurrentLife(p.currentLife);
		enterSc.setTotalLife(p.totalLife);
		enterSc.setXuePingRemain(p.xuePingRemain);
		enterSc.setHorseType(p.allianceId == redLmId ? 1 : 2);//1守方，2攻击方
		return enterSc;
	}
	public IoBuffer buildEnterInfoCache(Player p) {
		EnterScene.Builder enterCity = buildEnterInfo(p);
		IoBuffer io = pack(enterCity.build(), PD.Enter_Scene);
		return io;
	}
	@Override
	public void bdEnterInfo(Player enterPlayer) {
		super.bdEnterInfo(enterPlayer);
		PlayerScore.Builder	o = personalScoreMap.get(enterPlayer.jzId);
		if(o!=null){
			sendOneScore(enterPlayer, o);
		}
	}
	
	public void initCampsite(int type) {
		List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(type);
		for(LMZBuildingTemp build : buildList) {
			CampsiteInfo campsite = new CampsiteInfo(build.side, build);
			campsiteInfoList.put(campsite.id,campsite);
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
					
					AllianceBean otherAlliance = HibernateUtil.find(AllianceBean.class, otherAllianceId);
					LMZBuildingTemp tempBlue = AllianceFightMgr.getTeamCamp(TEAM_BLUE);
					ScoreInfo otherScoreInfo = new ScoreInfo(otherAlliance.id, TEAM_BLUE, tempBlue.x, tempBlue.y, otherAlliance.name);
					scoreInfoMap.put(otherAlliance.id, otherScoreInfo);
					log.info("为联盟:{}-{},分配了到了蓝队", otherAlliance.id, otherAlliance.name);
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
//		final EnterFightScene.Builder enterResponse = getEnterFightSceneResponse(jz, player);
		
//		syncSceneExecutor.submit(new Runnable() {
//			@Override
//			public void run() {
//				broadCastEvent(enterResponse.build(), enterResponse.getUid());
//			}
//		});
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
	int cycleT = 80;
	@Override
	public void run() {
		while (true) {
			Mission mission = null;
			try {
				mission = missions.poll(cycleT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			update();
			if(mission==null){
				continue;
			}
			if(mission==exit){
				break;
			}
			try {
				completeMission(mission);
			} catch (Throwable e) {
				log.error("线程休眠异常", e);
			}
		}
		log.info("退出 {}",Thread.currentThread().getName());
	}
	public void update(){
		long ms = System.currentTimeMillis();
		if(ms - lastBroadcastTime > 1000) {
			lastBroadcastTime = ms;
			broadcastBattleInfo();
		}
		if(step == 500){
			return;
		}
		if(step == 0){
			prepareMS -= cycleT;
			if(prepareMS<=0){
				step = 10;
			}
		}
		if(ms>= fightEndTime){
			winSide = TEAM_RED;
			over();
			return;
		}
		checkJiDiBuff(ms);
		long diff = ms - preCalcCampMs;
		if(diff<calcCampInterval){
			return;
		}
		preCalcCampMs = ms;
		computeBattleData();
	}
	public void checkJiDiBuff(long ms) {
		if(jiDiBuff == null){
			return;
		}
		long diff = ms - lastBuffTime;
		if(diff<jiDiBuff.EffectCycle){
			return;
		}
		lastBuffTime = ms;
		int percent = jiDiBuff.Attr_1;
		float range = 8f;//skill.ET_P1;
		CampsiteInfo c = campsiteInfoList.get(402);//攻击方基地
		if(c==null){
			return;
		}
		for(Player cp : players.values()){
			if(cp.userId==51){
				cp.userId+=0;
			}
			if(cp.allianceId == blueLmId)continue;//只对守方作用
			if(cp.currentLife<=0){
				continue;
			}
			float dx = Math.abs(c.x - cp.posX);
			float dz = Math.abs(c.z - cp.posZ);
			if(dx>range || dz>range){
				continue;
			}
			int dmg = cp.totalLife * percent / 100;
			//
			cp.currentLife -= dmg;
			cp.currentLife = Math.max(cp.currentLife, 0);
			
			BufferInfo build = BuffMgr.inst.buildDmgInfo(null, cp, dmg);
//			build.toBuilder().setBufferId(101);
			broadCastEvent(build, 0);
			if(cp.currentLife<=0){
				JunZhu defender = JunZhuMgr.inst.getJunZhu(cp.session);
				playerDie(defender, cp.userId, 0);
			}
		}
	}
	public void over() {
		step = 500;	
		//broadcastBattleInfo();
		/*
		ScoreList.Builder s = buildScoreInfo();
		ProtobufMsg p1 = new ProtobufMsg(PD.LMZ_SCORE_OVER, s);
		broadCastEvent(p1, 0);
		*/
		int winReason;
		float sumScale = 0;
		if(winSide == TEAM_BLUE){//攻击方胜利，则只要一个情况，守方基地被占领
			winReason = 200;
		}else{//守方胜利，则显示防御塔的守卫情况。
			int remainTower = 0;
			int ids[] = new int[]{201,202,203,301,302,303};
			for(int id:ids){
				CampsiteInfo c = campsiteInfoList.get(id);
				if(c.curHoldValue == TEAM_RED){
					remainTower++;
				}else{
					sumScale += c.conf.scale;
				}
			}
			winReason = remainTower;
		}
		CampsiteInfo c = campsiteInfoList.get(401);//守方基地
		if(c.curHoldValue == TEAM_BLUE){
			sumScale += c.conf.scale;
		}
		//

		List<JCZCity> cityList = TempletService.listAll(JCZCity.class.getSimpleName());
		Optional<JCZCity> op = cityList.stream().filter(cc->cc.id==cityId).findAny();
		JCZCity city = op.get();
		String awardStr = city.award;
		List<AwardTemp> alist = AwardMgr.inst.parseAwardConf(awardStr);
		int cityGX = alist.get(0).getItemNum();
		int gxGongJi = Math.round( cityGX * sumScale );
		//Optional<AwardTemp> gx = alist.stream().filter(t->t.getItemId()==900027).findAny();
		//2016年5月9日14:49:40 峰清确认只给功勋。
		//
		//List<JCZPersonalAward> rankList = TempletService.listAll(JCZPersonalAward.class.getSimpleName());
		int winLmId = winSide == TEAM_RED ? redLmId : blueLmId;
		makeLMAward(winLmId, redLmId, cityGX, gxGongJi);
		makeLMAward(winLmId, blueLmId, cityGX, gxGongJi);
		makePersonalAward(winLmId);
		for(Player p : players.values()){
			if(p instanceof FightNPC)continue;
			ABResult.Builder ret = ABResult.newBuilder();
			ProtobufMsg pf = new ProtobufMsg(PD.LMZ_OVER, ret);
			ret.setIsSucceed(p.allianceId==winLmId);
			int AllianceResult;
			if(winReason==200){//守方基地被攻陷
				if(p.allianceId==winLmId){
					AllianceResult = 200;//攻陷敌方基地
				}else{
					AllianceResult =-200;//被敌方攻陷基地
				}
			}else{//守方胜利
				if(p.allianceId==winLmId){
					AllianceResult=10000+winReason;//守住N座营地
				}else{
					AllianceResult=20000+(6-winReason);//攻陷N座营地，
				}
			}
			ret.setAllianceResult(AllianceResult);
			PlayerScore.Builder rankO = personalScoreMap.get(p.jzId);
			ret.setPersonalScore(rankO.getJiFen());
			ret.setRank(rankO.getRank());
			ret.setKillNum(rankO.getKillCnt());
			/*
			int gx = rankO.getJiFen();
			if(p.allianceId == redLmId){
				gx += cityGX - gxGongJi;
			}else{
				gx += gxGongJi;
			}
			int rank = rankO.getRank();
			int rankIdx = rank - 1;
			StringBuffer sb = new StringBuffer("0:900027:");
			sb.append(gx);
			if(rankList != null && rankIdx<rankList.size()){
				JCZPersonalAward pa = rankList.get(rankIdx);
				sb.append("#");
				sb.append(pa.award);
			}
			String ss = sb.toString();
			List<AwardTemp> finalList = AwardMgr.inst.parseAwardConf(ss);
			JunZhu jz = JunZhuMgr.inst.getJunZhu(p.session);
			for(AwardTemp t : finalList){
				AwardMgr.inst.giveReward(p.session,t,jz,false,false);
			}
			JunZhuMgr.inst.sendMainInfo(p.session);
			 */
			ret.setGainItem("");
			p.session.write(pf);
		}
		///保存占领信息
		if(city.type==2){
			return;
		}
		
		int preHoldId=0;
		CityBean cityBean = HibernateUtil.find(CityBean.class,cityId);
		if(cityBean == null){
			cityBean = new CityBean();
			cityBean.cityId = cityId;
			HibernateUtil.insert(cityBean);
		}else{
			preHoldId = cityBean.lmId;
		}
		if(winSide == TEAM_RED && redLmId == TEAM_RED){
			//npc保卫战赢了
		}else{
			cityBean.lmId = winLmId;
		}
		Date date = new Date();
		cityBean.occupyTime = date;
		cityBean.atckLmId = -100;
		HibernateUtil.update(cityBean);
		BidMgr.inst.saveLog(cityBean.lmId,preHoldId,cityId,date); //保存战报
		log.info("{}从{}获得城池{}",cityBean.lmId,preHoldId,cityId);
	}
	public void makePersonalAward(int winLmId) {
		Iterator<Long> it = personalScoreMap.keySet().iterator();
		Date t = new Date();
		List<JCZPersonalAward> rankList = TempletService.listAll(JCZPersonalAward.class.getSimpleName());
		while(it.hasNext()){
			Long jzId = it.next();
			if(isLeaveLm(jzId)) continue; //不在联盟不发奖励
			PlayerScore.Builder rankO = personalScoreMap.get(jzId);
			//
			int rank = rankO.getRank();
			int rankIdx = rank - 1;
			int rankAdd = 0;
			if(rankList != null && rankIdx<rankList.size()){
				JCZPersonalAward pa = rankList.get(rankIdx);
				List<AwardTemp> a = AwardMgr.inst.parseAwardConf(pa.award);
				rankAdd = a.get(0).getItemNum();
			}
			//
			LMZAwardBean b = new LMZAwardBean();
			b.jzId = jzId;
			b.cityId = cityId;
			b.warType = rankO.getSide()==TEAM_RED ? 0 : 1;
			b.result = rankO.getKillCnt();
			b.rewardNum =  rankO.getJiFen()+rankAdd;
			b.getState = 0;
			b.dt = t;
			b.fromType = 2;
			HibernateUtil.insert(b);
		}
	}
	public void makeLMAward(int winLmId, int lmId, int cityGX, int gxGongJi) {
		if(lmId<100)return;//NPC 
		int num = lmId == redLmId ? cityGX-gxGongJi : gxGongJi;
		if(num<=0){
			return;
		}
		List<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(lmId);
		Date t = new Date();
		for (AlliancePlayer member : memberList) {
			LMZAwardBean b = new LMZAwardBean();
			b.jzId = member.junzhuId;
			b.cityId = cityId;
			b.warType = lmId == redLmId ? 0 : 1;
			b.result = lmId == winLmId ? 1 : 0;
			b.rewardNum =  num;
			b.getState = 0;
			b.dt = t;
			b.fromType = 1;
			HibernateUtil.insert(b);
		}
	}
	public void computeBattleData() {
		//if(players.size()>0)return;//暂时关闭代码
		// 1. 计算本次战斗是否结束
		if(isBattleOver()) {
			//暂时关闭
//			processBattleResult();
//			return; 
		}
		// 2. 计算营地信息情况
		for(CampsiteInfo campsite : campsiteInfoList.values()) {
			if(step == 0)break;
			switch(campsite.id){
			case 301:
			case 302:
			case 303:{//占领一塔后才能占领二塔
				int preId = campsite.id-100;
				CampsiteInfo preCamp = campsiteInfoList.get(preId);
				if(preCamp.curHoldValue != TEAM_BLUE){
					continue;
				}
				break;
			}
			case 401://二塔都占领后才能占领守方基地
				boolean oneWayTake = false;
				for(int i=301;i<=303;i++){
					CampsiteInfo preCamp = campsiteInfoList.get(i);
					if(preCamp.curHoldValue == TEAM_BLUE){
						oneWayTake = true;
						break;
					}
				}
				if(oneWayTake==false){
					continue;
				}
				break;
			}
			// 更新在营地占领范围内的玩家人数情况
			if(campsite.curHoldValue==TEAM_BLUE && campsite.id!=402){
				//攻击方基地可以被占领
				//已被攻击方占领
				continue;
			}
			int red=0;
			int blue=0;
			for(Map.Entry<Integer, Player> entry : players.entrySet()) {
				Player player = entry.getValue();
				if(player.currentLife <= 0) {
					continue;
				}
				int distance = (int) Math.sqrt(
						Math.pow(campsite.x - player.posX, 2)+
						Math.pow(campsite.z - player.posZ, 2));
				distance = Math.abs(distance);
				if(player.userId==8){
					player.userId+=0;
				}
				if(distance <= campsite.radius) {
					if(player.allianceId==redLmId){
						red++;
					}else{
						blue++;
					}
				}
			}
			// 更新营地占领值
			//{
				int teamRedPlayerNum = red;
				int teamBluePlayerNum = blue;
				
				int addValue = campsite.zhanlingzhiAdd;
				addValue = Math.max(addValue, fixTakeTowerSpeed);
				if(teamRedPlayerNum > teamBluePlayerNum) {
					if(campsite.id==402){//攻击方基地
						campsite.cursorPos += addValue;
						campsite.cursorDir = CURSOR_POS_BLUE;
					}else{
						campsite.cursorDir = CURSOR_POS_RED;
						campsite.cursorPos -= addValue;
						campsite.cursorPos = Math.max(0, campsite.cursorPos);
					}
				} else if(teamRedPlayerNum < teamBluePlayerNum) {
					campsite.cursorDir = CURSOR_POS_BLUE;
					if(campsite.id==402){//攻击方基地
						campsite.cursorPos -= addValue;
						campsite.cursorPos = Math.max(0, campsite.cursorPos);
					}else{
						campsite.cursorPos += addValue;
					}
				} else {
					campsite.cursorDir = CURSOR_POS_DEAD;
				}
				//设置为被攻击方占领
				if(campsite.cursorPos>=campsite.zhanlingzhiMax){
					campsite.cursorPos=campsite.zhanlingzhiMax;
					if(campsite.id==402){//攻击方基地
						campsite.curHoldValue = TEAM_RED;
						winSide = TEAM_RED;
						over();
						return;
					}else{
						campsite.curHoldValue = TEAM_BLUE;
						if(campsite.conf.type==2){
							outTowerTaken += 1;
							fixTowerBuff();
						}else if(campsite.conf.type==3){
							innerTowerTaken += 1;
							fixTowerBuff();
						}
					}
					if(campsite.id==401){//守方基地被拿下
						winSide = TEAM_BLUE;
						over();
						return;
					}
				}
//				System.out.println("营地:" + campsite.id + "当前占领值:"+ campsite.curHoldValue);//FIXME 调试打印
			//}
			
			int holdAllianceId = campsite.curHoldValue;
			
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
	}
	public void fixTowerBuff(){
		int percent = getTowerBuffHP();
		for(Player p : players.values()){
			if(p.allianceId == redLmId){
				continue;
			}
			int add = p.totalLife * percent / 100;
			p.session.setAttribute("fixTowerBuff", percent);
			p.totalLife += add;
			if(p.currentLife>0){
				p.currentLife += add;
			}
			IoBuffer data = makeLifeChange(p);
			broadCastEvent(0, data);
		}
	}
	public int getTowerBuffHP() {
		int percent = 3 * outTowerTaken + 5 * innerTowerTaken;
		return percent;
	}
	public IoBuffer makeLifeChange(Player p) {
		FightAttackResp.Builder response = FightAttackResp.newBuilder();
		response.setResult(Result.SUCCESS);
		response.setAttackUid(0);
		response.setTargetUid(p.userId);//谁的值
		response.setSkillId(0);
		response.setDamage(p.totalLife);//总血量		
		response.setRemainLife(p.currentLife);//剩余血量
		IoBuffer ret = pack(response.build(), PD.Life_Change);
		return ret;
	}
	public void broadcastBattleInfo() {
		BattlefieldInfoNotify.Builder response = BattlefieldInfoNotify.newBuilder();
		int endRemainTime = 0;
		long currentTimeMillis = System.currentTimeMillis();
		if(step == 0){//准备阶段
			endRemainTime = (int) (prepareMS/1000);
		}else{
			endRemainTime = (int) ((fightEndTime - currentTimeMillis) / 1000);
			endRemainTime = Math.max(endRemainTime, 0);
		}
		response.setEndRemainTime(endRemainTime);
		response.setWinSide(step==0 ? -1 : winSide);
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
		for(CampsiteInfo siteInfo : this.campsiteInfoList.values()) {
			CampInfo.Builder campInfo = CampInfo.newBuilder();
			campInfo.setId(siteInfo.id);
			campInfo.setCursorPos(siteInfo.cursorPos);
			campInfo.setCursorDir(siteInfo.cursorDir);
			campInfo.setPerSecondsHoldValue(100);//临界值
			campInfo.setCurHoldValue(1);
			response.addCampInfos(campInfo);
		}
		broadCastEvent(response.build(), 0);
	}
	public ScoreList.Builder buildScoreInfo(){
		ScoreList.Builder s = ScoreList.newBuilder();
		Iterator<qxmobile.protobuf.AllianceFightProtos.PlayerScore.Builder> it = personalScoreMap.values().iterator();
		while(it.hasNext()){
			PlayerScore.Builder b = it.next();
			s.addList(b.build());
		}
		return s;
	}
	public List<IoSession> getAllPlayerSession() {
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
	
	public void processBattleResult() {
		int winAllianceId = 0;
		if(winAllianceId==0)return;//FIXME temp code
		int redAllianceId = redLmId;
		int blueAllianceId = blueLmId;
		
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
			
		
		AwardTemp awardTemp = new AwardTemp();
		awardTemp.setItemType(0);
		awardTemp.setItemId(900001);
		awardTemp.setItemNum(100);
		
		BattleResultAllianceFight.Builder response = BattleResultAllianceFight.newBuilder();
		response.setCostTime(0);
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
		remainTeamSet.clear();// = null;
		campsiteInfoList.clear();//= null;
	}
	@Override
	public Player ExitScene(qxmobile.protobuf.Scene.ExitScene.Builder exit, IoSession session) {
		session.removeAttribute("fixTowerBuff");//
		Player p = super.ExitScene(exit, session);
		if(p!=null){
			int hp = p.currentLife;
			if(hp<=0){
				hp = 1;//防止重新进来时是死亡状态。
			}
			hpCache.put(p.jzId, hp);
		}
		return p;
	}
	@Override
	public void savePosInfo(Player ep) {
		//目前不保存坐标
	}
	
	@Override
	public void playerDie(JunZhu defender, int uid, int killerUid) {
		BuffMgr.inst.removeBuff(defender.id);
		Integer usedTimes = freeFuHuoUsedTimes.get(defender.id);
		if(usedTimes == null){
			usedTimes = 0;
		}
		int remainReviveTimes = Math.max(0, dayFreeFuHuoTimes - usedTimes);
		remainReviveTimes = remainReviveTimes * 1000 + dayFreeFuHuoTimes;
		//低3位是每日免费次数，其余高位时剩余次数。
		if(defender.id > 0) {// 表示是真实玩家
//			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YB_REVIVE_DEAD_POS, reviveOnDeadPosTimes+1);
//			if(purchase == null) {
//				log.error("找不到类型为:{}的purchase配置", PurchaseConstants.YB_REVIVE_DEAD_POS);
//			} else {
//				onSiteReviveCost = purchase.getYuanbao();
//			}
//			remainReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(defender);
		}
		
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(uid);
		deadNotify.setKillerUid(killerUid);
		deadNotify.setAutoReviveRemainTime(AutoReviveRemainTime);//自动复活倒计时
		deadNotify.setRemainAllLifeTimes(remainReviveTimes);//立即复活剩余次数
		deadNotify.setOnSiteReviveCost(fuHuoYuanBaoPrice);//立即复活消耗元宝
		broadCastEvent(deadNotify.build(), 0);
		//
		if(step != 10){
			return;
		}
		addPersonalScore(killerUid, uid);
		Player die = players.get(uid);
		if(die == null){
			return;
		}
		PlayerScore.Builder o = personalScoreMap.get(die.jzId);
		if(o!=null){
			o.setLianSha(0);//
		}
	}
	public synchronized void addPersonalScore(int killerUid, int dead) {
		Player killer = players.get(killerUid);
		Player die = players.get(dead);
		if(killer == null || die == null){
			return;
		}
		PlayerScore.Builder o = personalScoreMap.get(killer.jzId);
		if(o==null){
			//进入场景时应已经创建了。
			return;
		}
		o.setLianSha(o.getLianSha()+1);
		o.setKillCnt(o.getKillCnt()+1);
		
		int jiFen = killOneJiFen+die.worth;
		o.setJiFen(o.getJiFen()+jiFen);
		//
		sortScore();
		sendOneScore(killer, o);
		//检查称号
		checkChengHao(killerUid,o.getLianSha());
	}
	public void sortScore() {
		ArrayList<PlayerScore.Builder> list = new ArrayList<>(personalScoreMap.values());
		Collections.sort(list, new Comparator<PlayerScore.Builder>(){

			@Override
			public int compare(qxmobile.protobuf.AllianceFightProtos.PlayerScore.Builder o1,
					qxmobile.protobuf.AllianceFightProtos.PlayerScore.Builder o2) {
				return o1.getJiFen() - o2.getJiFen();
			}
			
		});
		int i = 1;
		for(PlayerScore.Builder b : list){
			b.setRank(i);
			i++;
		}
	}
	public void sendOneScore(Player killer, PlayerScore.Builder o) {
		ErrorMessage.Builder eb = ErrorMessage.newBuilder();
		eb.setCmd(o.getJiFen());
		eb.setErrorCode(o.getLianSha());
		killer.session.write(new ProtobufMsg(PD.LMZ_SCORE_ONE, eb));
	}
	public void checkChengHao(int killerUid, int lianSha) {
		Player killer = players.get(killerUid);
		if(killer == null){
			return;
		}
		List<JCZChenghao> list = TempletService.listAll(JCZChenghao.class.getSimpleName());
		if(list == null){
			return;
		}
		JCZChenghao conf=null;
		Optional<JCZChenghao> op = list.stream().filter(c->c.condition==lianSha).findAny();
		if(op.isPresent()){
			conf = op.get();
		}else{
			conf = list.get(0);
//			return;
		}
		//
		killer.worth = conf.killedAward;
		ErrorMessage.Builder eb = ErrorMessage.newBuilder();
		eb.setCmd(1);//1获得, 0消失
		eb.setErrorCode(killerUid);//谁，uid
		eb.setErrorDesc("称号名称#玩家名字七个字连杀20人，获得XXXX.称号！");
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_ChengHao, eb);
		broadCastEvent(p, 0);
	}
	
	/**
	 * 校验是否还在联盟里面
	 * @param jzId 君主Id
	 * @return true 已经离开联盟
	 */
	public boolean isLeaveLm(long jzId){
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,jzId);
		if (member == null || member.lianMengId <= 0) {
			return true;
		}else{
			return false;
		}
		
	}
}
