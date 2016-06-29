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

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.Buff;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.JCZChenghao;
import com.manu.dynasty.template.JCZCity;
import com.manu.dynasty.template.JCZCommand;
import com.manu.dynasty.template.JCZNpcTemp;
import com.manu.dynasty.template.JCZPersonalAward;
import com.manu.dynasty.template.LMZBuildingTemp;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.Skill;
import com.manu.dynasty.template.VIP;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.msg.ProtobufMsg;
import com.qx.activity.ActivityMgr;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.alliancefight.BidMgr;
import com.qx.alliancefight.CampsiteInfo;
import com.qx.alliancefight.CityBean;
import com.qx.alliancefight.LMZAwardBean;
import com.qx.alliancefight.ScoreInfo;
import com.qx.alliancefight.WildCityBean;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.buff.BuffMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.fight.FightMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseMgr;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotSession;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

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
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Yabiao.BuyXuePingReq;
import qxmobile.protobuf.Yabiao.BuyXuePingResp;

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
	public static long calcCampInterval = 1000;
	public static int reviveOnDeadPosTimes = 60;
	public static int AutoReviveRemainTime = 60;
	public static final byte TEAM_RED = 1;				//守方 红队	对应LMZBuildingTemp type=1 side=1
	public static final byte TEAM_BLUE = 2;			//攻方 蓝队	对应LMZBuildingTemp type=1 side=2
	public int redLmId = TEAM_RED;
	public int blueLmId = TEAM_BLUE;
	public static ConcurrentHashMap<Long, Integer> freeFuHuoUsedTimes = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Long, Integer> buyXPTimes = new ConcurrentHashMap<>();
	public Map<Long, Integer> hpCache = new HashMap<>();
	public static int dayFreeFuHuoTimes = 3;
	public static int fuHuoShiId = 910010;//	复活石;
	public static int zhaoHuanPrice = 200;
	public static int fenShenPrice = 300;
	public static int fenShenCD = 1000*60*5;
	public static int fenShenLifeP = 70;
	public static int fenShenDmgP = 70;
	public static int zhaoHuanFuId = 910011;//召唤令
	public static int xuePingId = 910012;//	血瓶;//
	public static int maxTakeSpeed = 10;
	public static int damage_CD = 200;
	public static int refillSec = 10*1000;
	public static int refillValue = 50;
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
	/**
	 * key->jzId, value: pre use zhao huan ms 
	 */
	public Map<Long, Long> zhaoHuanMap = new HashMap<>();
	/**
	 * key->jzId, value: pre use fenShen ms 
	 */
	public Map<Long, Long> fenShenCDMap = new HashMap<>();
	public static long zhaoHuanCD = 180*1000;//
	
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
	public static long cmdCD = 10*1000;
	public static long cmdTime[] = {0,0};
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
		case PD.LMZ_fenShen:
			fenShen(session,builder);
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
			int pay = zhaoHuanPrice*buyCnt;
			if(jz.yuanBao<pay){
				break;
			}
			YuanBaoMgr.inst.diff(jz,-pay, 0, zhaoHuanPrice, YBType.LMZ_zhaoHuan,"购买召唤令lmz");
			HibernateUtil.update(jz);
			JunZhuMgr.inst.sendMainInfo(session, jz);
			BagMgr.inst.addItem(bag, zhaoHuanFuId, buyCnt, 0, 0, "购买召唤令lmz");
			BagMgr.inst.sendBagInfo(session, bag);
			sendBuyBloodResp(PD.LMZ_BUY_ZhaoHuan,session, resp, 10, zhaoHuanPrice, -1, remainXuePing+buyCnt, 1);
			break;
		case 2:
			//getBuyBloodInfo(jz, resp, session);
			sendBuyBloodResp(PD.LMZ_BUY_ZhaoHuan, session, resp, 40, zhaoHuanPrice, -1, remainXuePing, 1);
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
		BuyXuePingReq.Builder req=(BuyXuePingReq.Builder)builder;
		int buyCode=req.getCode();
		sendXuePingInfo(session, jz, buyCode);
	}
	public void sendXuePingInfo(IoSession session, JunZhu jz,int buyCode){
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		int remainXuePing = BagMgr.inst.getItemCount(bag, xuePingId);
		int buyCnt = 1;
		if(buyCode>10000){
			buyCnt = buyCode - 10000;
			buyCode = 1;
		}
		Integer usedTimes = buyXPTimes.get(jz.id);
		if(usedTimes==null){
			usedTimes = 0;
		}
		List<VIP> list = TempletService.listAll(VIP.class.getSimpleName());
		int maxTimes = 0;
		Optional<VIP> op = list.stream().filter(t->t.lv==jz.vipLevel).findAny();
		if(op.isPresent()){
			VIP vip = op.get();
			maxTimes = vip.JCZBloodVial;
		}
		int leftTimes = Math.max(0, maxTimes - usedTimes);
		Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(33, usedTimes+1);
		int price=0;
		if(purchase == null) {
			log.error("BBB找不到类型为:{}的purchase配置, {}", 33,usedTimes+1);
		} else {
			price = purchase.getYuanbao();
		}
		BuyXuePingResp.Builder resp=BuyXuePingResp.newBuilder();
		if(leftTimes<=0){
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 20/*次数用完*/, price, leftTimes, remainXuePing, 1);
			return;
		}else if(price<=0){
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 50/*50没找到配置*/, price, leftTimes, remainXuePing, 1);
			return;
		}
		switch (buyCode) {
		case 1:
			//buyBlood(jz, resp, session);
			int pay = price*buyCnt;
			if(pay>0 && jz.yuanBao>pay){
				YuanBaoMgr.inst.diff(jz,-pay, 0, price, YBType.LMZ_XueP,"购买血瓶lmz");
				HibernateUtil.update(jz);
				JunZhuMgr.inst.sendMainInfo(session, jz);
				BagMgr.inst.addItem(bag, xuePingId, buyCnt, 0, 0, "购买血瓶lmz");
				BagMgr.inst.sendBagInfo(session, bag);
				buyXPTimes.put(jz.id, usedTimes+1);
			}
			sendXuePingInfo(session, jz, 3);//重复利用此方法来发送信息。
			break;
		case 2:
			//getBuyBloodInfo(jz, resp, session);
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 40, price, leftTimes, remainXuePing, 1);
			break;
		case 3:
			sendBuyBloodResp(PD.LMZ_BUY_XueP, session, resp, 10, price, leftTimes, remainXuePing, 1);
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
	public void fenShen(IoSession ss, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(ss);
		if(jz == null){
			return;
		}
		Integer uidObject = (Integer) ss.getAttribute(SessionAttKey.playerId_Scene);
		Player mp = players.get(uidObject);
		if(mp==null){
			return;
		}
		//
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		int cmpId = req.getErrorCode();
		CampsiteInfo cmp = campsiteInfoList.get(cmpId);
		if(cmp == null){
			return;
		}
		Player tower = players.get(req.getCmd());
		if(tower == null){
			return;
		}
		//
		if(jz.yuanBao<fenShenPrice){
			return;
		}
		long curMS = System.currentTimeMillis();
		
		Long preMS = fenShenCDMap.get(jz.id);
		if(preMS == null){
		}else if(preMS+fenShenCD>curMS){
			return;
		}
		fenShenCDMap.put(jz.id, curMS);
		YuanBaoMgr.inst.diff(jz, -fenShenPrice, 0, fenShenPrice, YBType.LMZ_FenShen, "分身");
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(ss, jz);
		//
		FenShenNPC p = new FenShenNPC();
		p.pState = State.State_LEAGUEOFCITY;
		p.safeArea=-1;
		final int userId = getUserId();
		JCZNpcTemp t = new JCZNpcTemp();//用于保存原始位置。
		t.positionX = mp.posX;
		t.positionY = mp.posZ;
		t.npcId = tower.userId;
		p.parentUid = mp.userId;
		p.temp = t;
		p.name = jz.name;
//		p.name = jz.name+"分身"+userId;
		p.posX = mp.posX;
		p.posY = mp.posY;
		p.posZ = mp.posZ;
		p.vip = mp.vip;
		p.jzlevel = mp.jzlevel;
		p.lmName = mp.lmName;
		RobotSession session = new RobotSession();
		session.setAttribute(SessionAttKey.Scene, this);
		p.session=session;
		p.userId = userId;
		p.roleId = mp.roleId;
		p.fakeJz = jz;//makeFakeJz(t);
//		p.fakeJz.id = -userId;
		p.jzId = -userId;
		p.totalLife = p.currentLife = mp.totalLife * fenShenLifeP / 100;
		p.allianceId = mp.allianceId;//守方
		p.pState = State.State_LEAGUEOFCITY;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		players.put(userId, p);
		//
		bdEnterInfo(p);
		if(mp.visbileUids != null && mp.visbileUids.contains(p.userId)==false){
			//强制主人可见。
			show(p, mp);
		}
		informComerOtherPlayers(session, p);//用于计算目标。
		sendFenShenCD(mp);
		findTarget(p);
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
		long curMS = System.currentTimeMillis();
		Long preMS = zhaoHuanMap.get(who.jzId);
		if(preMS != null && curMS - preMS<zhaoHuanCD){
			log.warn("{} in cd 召唤", who.jzId);
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
		//------------
//		ErrorMessage.Builder req = ErrorMessage.newBuilder();
//		req.setErrorCode(uidObject);
		//------------
		DescId desc = ActivityMgr.descMap.get(3050101);
		SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
		subao.setSubaoId(0);
		subao.setConfigId(48);
		subao.setOtherJzId(uidObject);
		subao.setSubao(desc == null ? "联盟官员发起了联盟召集令，是否前往？" : desc.getDescription());
		subao.setEventId(501);
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_ZhaoHuan, subao);
		for(Player player : players.values()){
			if(player.allianceId != who.allianceId){
				continue;
			}
			player.session.write(p);
		}
		zhaoHuanMap.put(who.jzId, curMS);
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
		if(pl.zhiWu != 2 && pl.zhiWu != 1){//1-副盟主，2-盟主
			return;
		}
		int side = pl.allianceId == redLmId ? 0 : 1;
		long curMS = System.currentTimeMillis();
		if(cmdTime[side] + cmdCD > curMS){
			return;
		}
		cmdTime[side] = curMS;
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		req.setCmd(pl.zhiWu);
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_CMD_ONE, req);
		for(Player cc : players.values()){
			if(cc.allianceId != pl.allianceId){
				continue;
			}
			cc.session.write(p);
		}
	}
	public void sendCmdList(IoSession session) {
		Integer pid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(pid == null){
			return;
		}
		Player pl = players.get(pid);
		if(pl==null){
			return;
		}
		int head = pl.allianceId == redLmId? 2 : 1;
		PromptMSGResp.Builder ret = PromptMSGResp.newBuilder();
		List<JCZCommand> list = TempletService.listAll(JCZCommand.class.getSimpleName());
		SuBaoMSG.Builder s = SuBaoMSG.newBuilder();
		for(JCZCommand c : list){
			if(c.ID/100 != head){
				continue;
			}
			s.setSubaoId(c.ID);
			s.setOtherJzId(0);
			s.setSubao(c.desc);
			s.setEventId(0);
			s.setConfigId(0);
			ret.addMsgList(s.build());
//			if(ret.getMsgListCount()==5)break;//delete this
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
		if(player.currentLife>0){
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
		/*
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
		*/
		case 40:{//元宝
			//计算复活次数；本来是有免费复活的，后来不要了，就用它来计算复活次数。
			Integer usedTimes = freeFuHuoUsedTimes.get(jz.id);
			if(usedTimes == null){
				log.info("{}首次复活",jz.id);
			}else if(usedTimes<dayFreeFuHuoTimes){
				log.info("{}复活",jz.id);
			}else{
				ok = false;
				log.info("{}今日复活次数不足", jz.id);
				break;
			}
			//
			usedTimes = freeFuHuoUsedTimes.get(jz.id);
			if(usedTimes == null){
				usedTimes = 0;
			}
			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(32, usedTimes+1);
			int fuHuoYuanBaoPrice=0;
			if(purchase == null) {
				log.error("找不到类型为:{}的purchase配置, {}", 32,usedTimes+1);
				break;
			} else {
				fuHuoYuanBaoPrice = purchase.getYuanbao();
			}
			if(jz.yuanBao>=fuHuoYuanBaoPrice && fuHuoYuanBaoPrice>0){
				YuanBaoMgr.inst.diff(jz, -fuHuoYuanBaoPrice, 0, fuHuoYuanBaoPrice, YBType.LMZ_FUHUO, "联盟战复活");
				HibernateUtil.update(jz);
				log.info("{}元宝复活",jz.id);
				freeFuHuoUsedTimes.put(jz.id, usedTimes+1);
				JunZhuMgr.inst.sendMainInfo(session,jz);
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
						.filter(b->b.type==2 || b.type==3 || b.type==4).iterator();
				//上面一行，4是基地，由于复活点不在campsiteInfoList里，所以用基地判断。
				int side = player.allianceId == redLmId ? 1 : 2;
				LMZBuildingTemp near = null;
				int distMin = Integer.MAX_VALUE;
				while(siteIt.hasNext()){
					if(step == 500)break;
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
				if(near == null || near.type == 4){//离基地近，则取复活点。
					//修正坐标
					List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(1);
					bornAt = buildList.get(side-1);
				}else{
					bornAt = near;
				}
		//
		player.posX = bornAt.x;
		player.posZ = bornAt.y;
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
		s.setCityId(cityId);
		ProtobufMsg p = new ProtobufMsg(PD.LMZ_SCORE_LIST, s);
		//broadCastEvent(p, 0);
		session.write(p);
	}
	public void bornAllNPC(){
		List<LMZBuildingTemp> buildList = TempletService.listAll(LMZBuildingTemp.class.getSimpleName());
		for(LMZBuildingTemp t : buildList){
			if(t.type<2 || t.type>4){
				//只取2 3 4
				continue;
			}
			TowerNPC p = new TowerNPC();
			p.pState = State.State_LEAGUEOFCITY;
			p.safeArea=-1;
			p.name = t.name==null ? "塔"+t.id : t.name;
			p.posX = t.x;
			p.posZ = t.y;
			p.lmName = "";
			p.conf = t;
			p.zhiWu = t.id;//用职务来表示塔的id
			RobotSession session = new RobotSession();
			session.setAttribute(SessionAttKey.Scene, this);
			final int userId = getUserId();
			p.session=session;
			p.userId = userId;
			p.roleId = TOWER_RoleId;
//			p.fakeJz = makeFakeJz(t);
//			p.fakeJz.id = -userId;
			p.totalLife = p.currentLife = t.zhanlingzhiMax;
			p.allianceId = redLmId;//守方
			if(t.id == 402){//攻方基地
				p.allianceId = blueLmId;
			}
			Object uidObject = session.setAttribute(SessionAttKey.playerId_Scene, userId);
			players.put(userId, p);
		}
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
		Player p = players.get(attackUid);
		if(p == null)return;
		JunZhu attacker = p.jz;
		if(p.jz == null){
			attacker = p.jz = JunZhuMgr.inst.getJunZhu(session);
		}
		if(attacker == null || attackUid == null){
			return;
		}
		//
		int skillId = req.getSkillId();
		Skill skill = BuffMgr.inst.getSkillById(skillId);
		switch(skillId){
		case 151://旋风斩
			BigSwitch.inst.buffMgr.calcSkillDamage(attacker, null, p, null, skill, attackUid, this);
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
		p.jzlevel = jz.level;
		p.zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		p.guojia = jz.guoJiaId;
		Integer cacheHp = hpCache.get(p.jzId);
		if(cacheHp!=null && step == 10){//战斗中才使用缓存血量。
			cacheHp = fixCacheHP(p, cacheHp);
			p.currentLife = cacheHp;
		}else{
			p.currentLife = p.totalLife;
		}
		int side = p.allianceId == redLmId ? 1 : 2;
//		side = 1;//DELETE this
		LMZBuildingTemp bornAt;
		List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(1);
		bornAt = buildList.get(side-1);
		
		p.posX = bornAt.x;
		p.posZ = bornAt.y;
		p.worth=0;
		//
		PlayerScore.Builder	o = personalScoreMap.get(p.jzId);
		if(o==null){
			o = initDefaultScore(p);
			personalScoreMap.put(p.jzId,o);
			sortScore();
		}else if(o.getSide() != side){
			o = initDefaultScore(p);
			personalScoreMap.put(p.jzId,o);
			sortScore();
		}
		EventMgr.addEvent(ED.done_junChengZhan_x, new Object[]{jz.id});
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(jz.id , DailyTaskConstants.junChengZhan, 1));
	}
	public PlayerScore.Builder initDefaultScore(Player p) {
		PlayerScore.Builder o;
		o = PlayerScore.newBuilder();
		o.setJiFen(0);
		o.setKillCnt(0);
		o.setLianSha(0);
		o.setRank(personalScoreMap.size()+1);
		o.setRoleName(p.name);
		o.setSide(p.allianceId==redLmId ? 1 : 2);
		o.setJzId(p.jzId);
		o.setRoleId(p.roleId);
		o.setGx(0);
		o.setLmName(p.lmName);
		return o;
	}
	public Integer fixCacheHP(Player p, Integer cacheHp) {
		if(cacheHp<=0){
			int deadAt = -cacheHp;
			int cur = (int) (System.currentTimeMillis() & Integer.MAX_VALUE);
			int diff = (deadAt+AutoReviveRemainTime*1000 - cur)/1000;
			if(diff<=0){//时间已到
				cacheHp = p.totalLife;
			}else{
				p.session.setAttribute("rebornSec",diff);
				cacheHp = 0;
			}
		}
		return cacheHp;
	}
	public void sendCommandCD(Player p, int side) {
		//发送下达指令的CD
		//if(p.zhiWu == 1 || p.zhiWu == 2){
			ErrorMessage.Builder req = ErrorMessage.newBuilder();
			req.setErrorCode(side);//要告诉客户端自己的阵营，判断旗子颜色用
			req.setCmd(20160530);
			long curMS = System.currentTimeMillis();
			long diff = curMS - cmdTime[side-1];
			if(diff>=cmdCD){
				diff = 0;
			}else{
				diff = cmdCD - diff;
			}
			req.setErrorDesc(String.valueOf(diff));
			ProtobufMsg cmdCD = new ProtobufMsg(PD.LMZ_CMD_ONE, req);
			p.session.write(cmdCD);
		//}
	}
	public void sendFenShenCD(Player p) {
		long curMS = System.currentTimeMillis();
		
		Long preMS = fenShenCDMap.get(p.jzId);
		long cd = 0;
		if(preMS != null){
			cd = fenShenCD - (curMS - preMS);
			cd = Math.max(0, cd);
		}
		ErrorMessage.Builder msg = ErrorMessage.newBuilder();
		msg.setCmd((int)cd);
		msg.setErrorCode(fenShenPrice);
		ProtobufMsg pcd = new ProtobufMsg(PD.LMZ_fenShen, msg);
		p.session.write(pcd);
	}
	public void sendZhaoHuanCD(Player p) {
		//一并计算是否打过联盟战
		String hql = "select count(1) from LMZAwardBean where jzId="+p.jzId;
		int cnt = HibernateUtil.getCount(hql);
		//发送召唤CD
		long curMS = System.currentTimeMillis();
		
		Long preMS = zhaoHuanMap.get(p.jzId);
		long cd = 0;
		if(preMS != null){
			cd = zhaoHuanCD - (curMS - preMS);
			cd = Math.max(0, cd);
		}
		SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
		subao.setSubaoId(0);
		subao.setConfigId(cnt>0 ? 1 : 0);
		subao.setOtherJzId(-999);
		subao.setSubao(String.valueOf(cd));
		subao.setEventId(-501);
		ProtobufMsg pcd = new ProtobufMsg(PD.LMZ_ZhaoHuan, subao);
		p.session.write(pcd);
	}
	
	@Override
	public qxmobile.protobuf.Scene.EnterScene.Builder buildEnterInfo(Player p) {
		EnterScene.Builder enterSc = super.buildEnterInfo(p);
		enterSc.setCurrentLife(p.currentLife);
		enterSc.setTotalLife(p.totalLife);
		enterSc.setXuePingRemain(p.xuePingRemain);
		enterSc.setHorseType(p.allianceId == redLmId ? 1 : 2);//1守方，2攻击方
		enterSc.setLevel(p.jzlevel);
		enterSc.setZhanli(p.zhanli);
		enterSc.setGuojia(p.guojia);
		if(p instanceof FenShenNPC){
			FenShenNPC fs = (FenShenNPC)p;
			enterSc.setWorth(fs.parentUid);
		}
		return enterSc;
	}
	public IoBuffer buildEnterInfoCache(Player p) {
		EnterScene.Builder enterCity = buildEnterInfo(p);
		IoBuffer io = pack(enterCity.build(), PD.Enter_Scene);
		return io;
	}
	@Override
	public void informComerOtherPlayers(IoSession session, Player enterPlayer) {
		sendCommandCD(enterPlayer, enterPlayer.allianceId==redLmId?1:2);
		sendZhaoHuanCD(enterPlayer);
		sendFenShenCD(enterPlayer);
		PlayerScore.Builder	o = personalScoreMap.get(enterPlayer.jzId);
		if(o!=null){
			sendOneScore(enterPlayer, o);
			checkChengHao(enterPlayer.userId, o.getLianSha(), 2);
		}
		//先发上面的，客户端才知道自己是攻还是守。要处理旗子的颜色。
		super.informComerOtherPlayers(session, enterPlayer);
	}
	@Override
	public void bdEnterInfo(Player enterPlayer) {
		super.bdEnterInfo(enterPlayer);
		/////////----------
		Integer rebornSec = (Integer)enterPlayer.session.removeAttribute("rebornSec");
		if(rebornSec==null){
			return;
		}
		//发送死亡通知
		bdDie(enterPlayer,0,rebornSec);
	}
	
	public void initCampsite(int type) {
		List<LMZBuildingTemp> buildList = AllianceFightMgr.lmzBuildMap.get(type);
		for(LMZBuildingTemp build : buildList) {
			CampsiteInfo campsite = new CampsiteInfo(build.side, build);
			campsiteInfoList.put(campsite.id,campsite);
		}
	}

	public void enterFightScene(IoSession session, EnterScene.Builder enterFightScene) {
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
			try {
				update();
			} catch (Throwable e) {
				log.error("线程update异常", e);
			}
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
			checkAllPlayerOut();
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
		updateNPC();
		checkJiDiBuff(ms);
		long diff = ms - preCalcCampMs;
//		if(diff<calcCampInterval){
		if(diff<refillSec){
			return;
		}
		preCalcCampMs = ms;
//		computeBattleData();
		refillTowers();
	}
	public void checkAllPlayerOut() {
		int cnt = 0;
		for(Player p : players.values()){
			if(p instanceof FightNPC){
				continue;
			}else if(p instanceof TowerNPC){
				continue;
			}
			cnt++;
		}
		if(cnt == 0){
			destory();
		}
	}
	public void updateNPC() {
		long ms = System.currentTimeMillis();
		if(ms - preUpdateTime>=updateInterval){
			preUpdateTime = ms;
		}else{
			return;
		}
		for(Player p : players.values()){
			if(p instanceof FightNPC == false){
				continue;
			}
			FightNPC fp = (FightNPC) p;
			if(fp.userId==2){
//				fp.posX = fp.posY =fp.posZ=0;
			}

			if(fp.state == 2){
				chaseTo(fp,fp.temp.positionX,fp.temp.positionY);
				continue;
			}
			if(fp.target==null){
				findTarget(fp);
				continue;
//				if(fp instanceof FenShenNPC){
//					FenShenNPC fs = (FenShenNPC) fp;
//					continue;
//				}else{
//					//野城里的NPC，一进来就产生了，所以它的目标会在玩家移动时产生。
//					continue;
//				}
			}
			if(fp.target.currentLife<=0 
					|| players.containsKey(fp.target.userId)==false){
				fp.target=null;
				fp.state = 2;
				continue;
			}
			if(fp.target != null){
				npcAttack(fp);
			}
		}		
	}
	public void findTarget(FightNPC fs) {
		/*Player tower = players.get(fs.temp.npcId);
//		tower = null;//delete
		if(tower != null && tower.allianceId != fs.allianceId && tower.currentLife>0){
			fs.target = tower;
			fs.state = 1;
		}else */if(fs.visbileUids == null){
		}else if(fs.visbileUids.size()==0){
		}else{
			Iterator<Integer> it = fs.visbileUids.iterator();
			while(it.hasNext()){
				Integer suid = it.next();
				Player see = players.get(suid);
				if(see==null)it.remove();
				if(see instanceof TowerNPC){
					TowerNPC tn = (TowerNPC)see;
					if(tn.userId != fs.temp.npcId){
						//不要跑去打其他塔。
						continue;
					}
				}
				if(see != null && see.allianceId != fs.allianceId && see.currentLife>0){
//					fs.target = see;
//					fs.state = 1;
//					break;
					checkDist(fs,see);
					if(fs.target!=null){
						break;
					}
				}
			}
		}		
	}
	public void refillTowers() {
		//检查基地回血
		long ms = System.currentTimeMillis();
		for(Player p : players.values()){
			if(p instanceof TowerNPC==false){
				continue;
			}
			if(p.currentLife<=0){
				continue;
			}
			TowerNPC t = (TowerNPC) p;
			if(t.preHurtMS+refillSec>ms){
				//一个间隔内被攻击了，不回血
				continue;
			}
			if(p.currentLife>=p.totalLife){
				continue;
			}
			p.currentLife += refillValue;
			p.currentLife = Math.min(p.currentLife, p.totalLife);
			IoBuffer data = makeLifeChange(p);
			broadCastEvent(0, data);
		}
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
		float range = 5f;//skill.ET_P1;
		CampsiteInfo c = campsiteInfoList.get(402);//攻击方基地
		if(c==null){
			return;
		}
		for(Player cp : players.values()){
//			if(cp.userId==51){
//				cp.userId+=0;
//			}
			if(cp.allianceId == blueLmId)continue;//只对守方作用
			if(cp.currentLife<=0){
				continue;
			}
			if(cp instanceof TowerNPC)continue;
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
				playerDie(cp, 0);
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
		int redLMGX = makeLMAward(winLmId, redLmId, cityGX, gxGongJi);
		int blueLMGX = makeLMAward(winLmId, blueLmId, cityGX, gxGongJi);
		makePersonalAward(winLmId);
		for(Player p : players.values()){
			if(p instanceof FightNPC)continue;
			if(p instanceof TowerNPC)continue;
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
			ret.setLmGX(p.allianceId==redLmId ? redLMGX : blueLMGX);
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
		//保存积分战报
		ScoreList.Builder scoreList = buildScoreInfo();
		scoreList.setDateTime(BidMgr.inst.dayStart().getTime() / 1000);
		scoreList.setCityId(cityId);
		scoreList.setIsNpc(redLmId < 100 || city.type == 2?1:0); //NPC
		if(city.type==2){
			Redis.getInstance().hset(BidMgr.REDIS_KEY_SCORE_RESULT.getBytes(),(BidMgr.HASHKEY_YECHENG + "_" + blueLmId).getBytes(),scoreList.build());
		}else{
			Redis.getInstance().hset(BidMgr.REDIS_KEY_SCORE_RESULT.getBytes(), (String.valueOf(cityId)).getBytes(),scoreList.build());
		}
		///保存占领信息
		int preHoldId=0;
		if(city.type==2){
			WildCityBean wildCityBean = HibernateUtil.find(WildCityBean.class,"where lmId=" + winLmId + " and cityId=" + cityId);
			if(wildCityBean == null){
				wildCityBean = new WildCityBean();
				wildCityBean.cityId = cityId;
				wildCityBean.lmId = winLmId;
				wildCityBean.isWin = 0;
				HibernateUtil.insert(wildCityBean);
			}
			if(winSide == TEAM_RED && redLmId == TEAM_RED){
				//npc保卫战赢了
			}else{
				wildCityBean.isWin = 1;
			}
			wildCityBean.winTime = new Date();
			HibernateUtil.update(wildCityBean);
		}else{
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
		}
		log.info("{}从{}获得城池{}",winLmId,preHoldId,cityId);
		//
		BigSwitch.inst.scMgr.fightScenes.remove(this.id);
	}
	public void makePersonalAward(int winLmId) {
		Iterator<Long> it = personalScoreMap.keySet().iterator();
		Date t = new Date();
		List<JCZPersonalAward> rankList = TempletService.listAll(JCZPersonalAward.class.getSimpleName());
		List<Long> jzIdList = new ArrayList<Long>();
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
			rankO.setGx(rankAdd);
			//
			LMZAwardBean b = new LMZAwardBean();
			b.jzId = jzId;
			b.cityId = cityId;
			b.warType = rankO.getSide()==TEAM_RED ? 0 : 1;
			b.result = rankO.getKillCnt();
			b.rewardNum =  /*rankO.getJiFen()+*/rankAdd;
			b.getState = 0;
			b.dt = t;
			b.fromType = 2;
			HibernateUtil.insert(b);
			jzIdList.add(jzId);
		}
		EventMgr.inst.addEvent(ED.CITY_WAR_FIGHT_JIESUAN,new Object[]{jzIdList,2});
	}
	public int makeLMAward(int winLmId, int lmId, int cityGX, int gxGongJi) {
		if(lmId<100)return 0;//NPC 
		int num = lmId == redLmId ? cityGX-gxGongJi : gxGongJi;
//		if(num<=0){
//			return 0;
//		}
		StringBuffer title = new StringBuffer();
		StringBuffer body = new StringBuffer();
		String tail = "";
		tail = combMail(winLmId, lmId, cityGX, gxGongJi, title, body);
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(110001);
		List<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(lmId);
		Date t = new Date();
		String mt = title.toString();
		String mb = body.toString();
		List<Long> jzIdList = new ArrayList<Long>();
		for (AlliancePlayer member : memberList) {
			if(num>0){
				LMZAwardBean b = new LMZAwardBean();
				b.jzId = member.junzhuId;
				b.cityId = cityId;
				b.warType = lmId == redLmId ? 0 : 1;
				b.result = lmId == winLmId ? 1 : 0;
				b.rewardNum =  num/memberList.size();
				b.getState = 0;
				b.dt = t;
				b.fromType = 1;
				HibernateUtil.insert(b);
			}
			//
			if (mailConfig == null) {
				continue;
			}
			PlayerScore.Builder	o = personalScoreMap.get(member.junzhuId);
			String fmb;
			if(o!=null){
				fmb = mb+"您在此战的排名为"+o.getRank()+"！" + tail;
			}else{
				fmb = mb + tail ;
			}
			JunZhu jz = HibernateUtil.find(JunZhu.class, member.junzhuId);
			if(jz == null)continue;
			String pre = mailConfig.title;
			String preT = mailConfig.taitou;
			try{
				mailConfig.taitou = "亲爱的"+jz.name+"：";
				mailConfig.title = mt.toString();
			EmailMgr.INSTANCE.sendMail(jz.name, fmb,
					"", mailConfig.sender, mailConfig, "");
			}catch(Exception e){
				log.error("发送邮件失败",e);
			}
			mailConfig.title = pre;
			mailConfig.taitou = preT;
			jzIdList.add(member.junzhuId);
		}
		EventMgr.inst.addEvent(ED.CITY_WAR_FIGHT_JIESUAN,new Object[]{jzIdList,1});
		return num;
	}
	public String combMail(int winLmId, int lmId, int cityGX, int gxGongJi, StringBuffer title, StringBuffer body
			) {
		String tail = "";
		do{//拼凑邮件
			JCZCity cityConf = BidMgr.inst.jczmap.get(cityId);
			if(cityConf == null)break;
			String cityName = HeroService.getNameById(String.valueOf(cityConf.name));
			if(cityConf.type == 1){//野城
				//cityName += "城";
			}else{
				cityName = "义渠"+cityName;
			}
			String shouLmName = "";
			boolean shou = lmId == redLmId;
			title.append(cityName).append(shou ? "守城" : "攻城");
			boolean win = winLmId == lmId;
			boolean withNpc = this instanceof FightNPCScene;
			if(!withNpc){
				shouLmName = AllianceMgr.getAllianceName(redLmId);
			}
			int nTower = innerTowerTaken + outTowerTaken;
			String gjLmName = AllianceMgr.getAllianceName(blueLmId);
			title.append(win ? "胜利" : "失败");
			//
			body.append("我盟");
			if(win){
				body.append("成功");
				if(cityConf.type == 2){//野城
					body.append("击垮");body.append(cityName);body.append("，");
					body.append("掠夺功勋");body.append(gxGongJi);body.append("!");
					tail = "义渠贼寇在我军的攻势下望风而逃!";
				}else if(shou){
					body.append("保卫");body.append(cityName);body.append("，");
					body.append("但被敌盟");body.append(cityName);body.append("攻下");body.append(nTower);body.append("座营地，");
					body.append("损失功勋");body.append(gxGongJi);body.append("!");
					tail = "请我盟将士不要放松警惕，坚守城池，防范敌寇来袭！";
				}else{
					body.append("占领");body.append(cityName);body.append("，");
					body.append("攻克敌盟");body.append(shouLmName);body.append("帅帐，");
					body.append("每日可产出功勋");body.append(cityGX);body.append("!");
					tail = "我盟需时刻警惕，防备敌人偷袭！";
				}
			}else{
				if(shou){
					body.append(cityName); body.append("失守");body.append("，");
					body.append("该城已被敌盟"); body.append(gjLmName);body.append("占领！");
					tail = "愿我盟发愤图强，再接再厉，夺回失地，壮我盟威！";
				}else {
					body.append("未能攻克"); body.append(cityName);body.append("，");
					body.append("但攻克敌盟");body.append(nTower);body.append("座营地，");
					tail = "请我盟将士稍事休整，来日再战！";
				}
			}
		}while(false);
		return tail;
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
//				if(player.userId==8){
//					player.userId+=0;
//				}
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
				addValue *= Math.abs(red-blue);
				addValue = Math.min(addValue, maxTakeSpeed);
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
		missions.add(exit);
		scoreInfoMap.clear(); //= null;
		remainTeamSet.clear();// = null;
		campsiteInfoList.clear();//= null;
	}
	@Override
	public Player ExitScene(qxmobile.protobuf.Scene.ExitScene.Builder exit, IoSession session) {
		session.removeAttribute("fixTowerBuff");//
		Player p = super.ExitScene(exit, session);
		if(p!=null){
			removeVisibleIds(p);
			int hp = p.currentLife;
			if(hp<=0){
				Long deadAtMS = (Long)session.getAttribute("deadAtMS");
				if(deadAtMS == null){
					deadAtMS = System.currentTimeMillis();
				}
				hp = (int) -(deadAtMS & Integer.MAX_VALUE);
			}
			hpCache.put(p.jzId, hp);
			//出战场
			BidMgr.inst.exitScene(p.jzId,cityId);
			//
			if(session.getAttribute(SessionAttKey.LM_ZHIWU)==null){
				//被踢出联盟
				personalScoreMap.remove(p.jzId);
			}
		}
		return p;
	}
	@Override
	public void savePosInfo(Player ep) {
		//目前不保存坐标
	}
	
	@Override
	public void playerDie(Player defender, int killerUid) {
		BuffMgr.inst.removeBuff(defender.jzId);
		bdDie(defender, killerUid,AutoReviveRemainTime);
		//
		if(defender instanceof TowerNPC){
			towerNPCDie(defender);
			return;
		}
		//
		if(defender instanceof FenShenNPC){
			players.remove(defender.userId);
			removeVisibleIds(defender);
			return;
		}
		if(step != 10){
			return;
		}
		addPersonalScore(killerUid, defender.userId);
		Player die = defender;
		PlayerScore.Builder o = personalScoreMap.get(die.jzId);
		if(o!=null){
			o.setLianSha(0);//
			sendOneScore(die, o);
		}
	}
	public void bdDie(Player defender, int killerUid,int rebornSec) {
		Integer usedTimes = freeFuHuoUsedTimes.get(defender.jzId);
		if(usedTimes == null){
			usedTimes = 0;
		}
		int remainReviveTimes = Math.max(0, dayFreeFuHuoTimes - usedTimes);
		if(defender.vip<4){
			remainReviveTimes = 0;
		}
		remainReviveTimes = remainReviveTimes * 1000;// + dayFreeFuHuoTimes;
		int fuHuoYuanBaoPrice = 0;
		//低3位是每日免费次数，其余高位时剩余次数。
		if(defender.jzId > 0 && remainReviveTimes>0) {// 表示是真实玩家
			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(32, usedTimes+1);
			if(purchase == null) {
				log.error("找不到类型为:{}的purchase配置", 32);
				remainReviveTimes = 0;
			} else {
				fuHuoYuanBaoPrice = purchase.getYuanbao();
			}
		}
		
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(defender.userId);
		deadNotify.setKillerUid(killerUid);
		deadNotify.setAutoReviveRemainTime(rebornSec);//自动复活倒计时
		deadNotify.setRemainAllLifeTimes(remainReviveTimes);//立即复活剩余次数
		deadNotify.setOnSiteReviveCost(fuHuoYuanBaoPrice);//立即复活消耗元宝
		broadCastEvent(deadNotify.build(), 0);
		defender.session.setAttribute("deadAtMS", System.currentTimeMillis());
		defender.session.setAttribute("fuHuoYuanBaoPrice", fuHuoYuanBaoPrice);
	}
	public void towerNPCDie(Player defender) {
		TowerNPC t = (TowerNPC) defender;
		if(t.conf.id==402){//攻击方基地
//				t.curHoldValue = TEAM_RED;
			campsiteInfoList.get(t.conf.id).curHoldValue = TEAM_RED;
			winSide = TEAM_RED;
			over();
			return;
		}else{
//				campsite.curHoldValue = TEAM_BLUE;
			campsiteInfoList.get(t.conf.id).curHoldValue = TEAM_BLUE;
			if(t.conf.type==2){
				outTowerTaken += 1;
				fixTowerBuff();
			}else if(t.conf.type==3){
				innerTowerTaken += 1;
				fixTowerBuff();
			}
		}
		if(t.conf.id==401){//守方基地被拿下
			winSide = TEAM_BLUE;
			over();
			return;
		}
	}
	public synchronized void addPersonalScore(int killerUid, int dead) {
		Player killer = players.get(killerUid);
		Player die = players.get(dead);
		if(killer == null || die == null){
			return;
		}
		long killerJzId = killer.jzId;
		if(killer instanceof FenShenNPC){
			FenShenNPC fs = (FenShenNPC)killer;
			killerJzId = fs.fakeJz.id;
			killer = getPlayerByJunZhuId(killerJzId);
		}
		PlayerScore.Builder o = personalScoreMap.get(killerJzId);
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
		if(killer != null){//可能是分身杀人，真身不在。
			sendOneScore(killer, o);
			//检查称号
			checkChengHao(killer.userId,o.getLianSha() , 1);
		}
	}
	public void sortScore() {
		ArrayList<PlayerScore.Builder> list = new ArrayList<>(personalScoreMap.values());
		Collections.sort(list, new Comparator<PlayerScore.Builder>(){

			@Override
			public int compare(qxmobile.protobuf.AllianceFightProtos.PlayerScore.Builder o1,
					qxmobile.protobuf.AllianceFightProtos.PlayerScore.Builder o2) {
				return o2.getJiFen() - o1.getJiFen();
			}
			
		});
		int red = 1;
		int blue = 1;
		for(PlayerScore.Builder b : list){
			if(b.getSide()==1){
				b.setRank(red++);
			}else{
				b.setRank(blue++);
			}
		}
	}
	public void sendOneScore(Player killer, PlayerScore.Builder o) {
		ErrorMessage.Builder eb = ErrorMessage.newBuilder();
		eb.setCmd(o.getJiFen());
		eb.setErrorCode(o.getLianSha());
		killer.session.write(new ProtobufMsg(PD.LMZ_SCORE_ONE, eb));
	}
	public void checkChengHao(int killerUid, int lianSha, int type) {
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
//			conf = list.get(0);
			return;
		}
		//
		killer.worth = conf.killedAward;
		ErrorMessage.Builder eb = ErrorMessage.newBuilder();
		eb.setCmd(type);//2再次进入场景，之前就有；1获得, 0消失
		eb.setErrorCode(killerUid);//谁，uid
		eb.setErrorDesc(String.valueOf(conf.ID));
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
	public boolean preTowerDie(TowerNPC t) {
		if(step == 500){
			return true;
		}
		switch(t.conf.id){
		case 301:
		case 302:
		case 303:{//占领一塔后才能占领二塔
			int preId = t.conf.id-100;
			CampsiteInfo preCamp = campsiteInfoList.get(preId);
			if(preCamp.curHoldValue != TEAM_BLUE){
				return false;
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
				return false;
			}
			break;
		}
		return true;
	}
	/*------------------------------------from figth npc scene--------------*/
	public long preUpdateTime;
	public long updateInterval = 80;
	@Override
	public Player spriteMove(IoSession session, SpriteMove.Builder move) {
		Player p = super.spriteMove(session, move);
		if(p instanceof FightNPC == false){
			//真实的玩家，则检查是否进入NPC的视野。
			checkNPCVision(p);
		}
		return p;
	}
	public void checkNPCVision(Player movedP) {
		for(Player p : players.values()){
			if(p instanceof FightNPC == false){
				continue;
			}
			if(movedP.allianceId == p.allianceId){
				//同联盟的，跳过
				continue;
			}
			FightNPC fp = (FightNPC) p;
			//p是NPC
			if(fp.target == movedP){
				checkOutRange(fp, movedP);
			}else if(fp.state == 2){
				
			}else if(fp.target==null){
				checkDist(fp, movedP);
			}
		}
	}
	public static int atkDist = 10;
	public boolean inAtkRange(Player cur, Player p2){
		if(cur==null ||p2==null){
			return false;
		}
		if(cur==p2){
			return true;
		}
		if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
			return true;
		}
			
//		visibleDist=20;
		float dx = Math.abs(cur.posX - p2.posX);
		float dz = Math.abs(cur.posZ - p2.posZ);
		boolean visible = false;
		if(dx>atkDist || dz>atkDist){
			
		}else{
			visible = true;
		}
		return visible;
	}
	public void checkDist(FightNPC fp, Player movedP) {
		if(movedP.visbileUids==null){
			return;
		}
		if(movedP.visbileUids.contains(fp.userId)==false){
			return;
		}
		if(Math.abs(fp.temp.positionX - movedP.posX)>maxChaseDist 
				|| Math.abs(fp.temp.positionY - movedP.posZ)>maxChaseDist){
			//超过最远追击距离，不追击。
			return;
		}
		fp.state=1;
		fp.target = movedP;
	}
	public void checkOutRange(FightNPC fp, Player movedP) {
		//追击
		float tgtX;// = movedP.posX;
		float tgtZ;// = movedP.posZ;
		if(fp.state == 2){
			tgtX = fp.temp.positionX;
			tgtZ = fp.temp.positionY;
		}else{
			tgtX = movedP.posX;
			tgtZ = movedP.posZ;
		}
		chaseTo(fp,tgtX,tgtZ);
	}
	public float maxChaseDist = 15;
	/**
	 * 距离太远就会归位
	 * @param fp
	 * @param tgtX
	 * @param tgtZ
	 */
	public void chaseTo(FightNPC fp, float tgtX, float tgtZ) {
		float rawDx = tgtX - fp.posX;
		float rawDy = tgtZ - fp.posZ;
		double dx = Math.abs(rawDx);
		double dy = Math.abs(rawDy);
		if(fp.state ==1 && 
				(
					Math.abs(fp.temp.positionX - fp.posX)>maxChaseDist 
					|| Math.abs(fp.temp.positionY - fp.posZ)>maxChaseDist)
				){
			fp.target = null;
			fp.state = 2;//归位
			return;
		}
		
		double atan = Math.atan2(rawDx,rawDy);
		{//为了避免重叠，尝试偏离一定角度。
			float delta = fp.userId%10*0.045f;
			if(fp.userId%2==0)delta = -delta;
			atan += delta;
		}
		double r = 0.5f;
		double sin = Math.sin(atan);
		double x = r * sin;
		double y = r * Math.cos(atan);
		double dir = atan/(Math.PI)*180;

//		x = y = 0;//测试转向
		float atkDis = 2;
		if(fp.state == 2){
			atkDis = 0.3f;//要返回原位
		}
		boolean stop = false;
		if(dx<atkDis && dy<atkDis){
			x = y = 0;//位于攻击范围内。
			stop = true;
			if(fp.state == 2){
				fp.state = 0;//空闲
			}else if(fp.state == 11){
				return;
			}else if(fp.state == 1){
				fp.state = 11;//站立攻击
//				return;//不return，因为需要发送停止
			}
		}else if(fp.state == 11){//之前是站立
			fp.state = 1;//追击
		}else{
		}
//		dir = 360-dir;
		///
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setUid(fp.userId);
		float nx = (float) (fp.posX+x);
		move.setPosX(nx);
		move.setPosY(fp.posY);
		float nz = (float) (fp.posZ+y);
		move.setPosZ(nz);
		move.setDir((float) dir);
		spriteMove(fp.session, move);
		if(stop){
			//发两遍表示停止移动，客户端的规则就是这样。
			spriteMove(fp.session, move);
		}
//		fp.target = null;
//		log.info("{}chase {} {} to {},{}",fp.userId,x,y,move.getPosX(), move.getPosZ());		
	}

	public void npcAttack(FightNPC fp) {
		if(fp.preSkillTime>0){
			fp.preSkillTime -= 80;
		}
		Player target = fp.target;
		checkOutRange(fp, target);
		if(fp.state != 11){
			return;
		}
		if(fp.preSkillTime>0){
			return;
		}
		boolean v=  inAtkRange(fp, target);
		if(v==false){
			return;
		}
		///
		long ms = System.currentTimeMillis();
		int[] skillIds = {151,/*181,*/171,101};
		int[] coolIds = {   0,  /*0,*/  0,  0 };
		Map<Integer, Long> skillCDMap = FightMgr.inst.skillCDTimeMap.get(fp.jzId);
		if(skillCDMap != null){
			Long gcd = skillCDMap.get(0);
			if(gcd != null && ms<gcd){
				return;
			}
		}
		int coolIdx = 0;
		for(int id : skillIds){
			if(target instanceof TowerNPC && id != 101){
				continue;
			}
			if(skillCDMap==null){
				coolIds[coolIdx++] = id;
				continue;
			}
			Long endTime = skillCDMap.get(id);
			if(endTime == null || endTime < ms) {
//				log.info("end {} AAA {}",id, endTime);
				coolIds[coolIdx++] = id;
			}else{
//				return;//有技能CD没到，则不攻击。
			}
		}
		if(coolIdx == 0){//没有可用的技能
			return;
		}
		int rnd = (int)(ms % coolIdx);
		int skillId = coolIds[rnd];
		
		if(skillId==0){
			return;
		}
		if(target==null)return;
		if(skillId == 151){
			target = fp;
			fp.preSkillTime = 3000;//此技能期间不能再使用其他技能
		}else{
			fp.preSkillTime = 1000;//此技能期间不能再使用其他技能
		}
		//触发技能
		FightMgr.inst.processAttackPlayer(fp.fakeJz, fp.session, this, fp, target, skillId);
	}

	/*------------------------------------from figth npc scene--------------*/
}
