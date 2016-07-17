package com.qx.pve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Chenghao;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.GuanQiaJunZhu;
import com.manu.dynasty.template.HeroGrow;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.JiNengPeiYang;
import com.manu.dynasty.template.LegendNpcTemp;
import com.manu.dynasty.template.LegendPveTemp;
import com.manu.dynasty.template.NpcTemp;
import com.manu.dynasty.template.PuGong;
import com.manu.dynasty.template.PveBigAward;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.ShiBing;
import com.manu.dynasty.template.SkillTemplate;
import com.manu.dynasty.template.XunHanCheng;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.qx.award.AwardMgr;
import com.qx.bag.EquipMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.hero.HeroMgr;
import com.qx.hero.WuJiang;
import com.qx.jinengpeiyang.JNBean;
import com.qx.jinengpeiyang.JiNengPeiYangMgr;
import com.qx.junzhu.ChenghaoMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.secure.AntiCheatMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.world.GameObject;

import log.ActLog;
import log.OurLog;
import qxmobile.protobuf.BattlePveInit.BattleInit;
import qxmobile.protobuf.BattlePveInit.BattlePveInitReq;
import qxmobile.protobuf.BattlePveInit.Hero;
import qxmobile.protobuf.BattlePveInit.HeroType;
import qxmobile.protobuf.BattlePveInit.Soldier;
import qxmobile.protobuf.BattlePveInit.Troop;
import qxmobile.protobuf.PveLevel.PveBattleOver;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.ZhanDou.DroppenItem;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.LevelType;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeSkill;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.PlayerWeapon;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitError;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

/**
 * 目前的目的是测试用。
 * 日后需要增加单线程处理。（具体可参考Scene）
 * @author hudali
 *
 */
public class PveMgr extends EventProc {
	public static PveMgr inst;
	public static long godId = 0;
	public static Logger logger = LoggerFactory.getLogger(PveMgr.class);
	public AtomicInteger troopIdMgr = new AtomicInteger(1);
	public static AtomicInteger battleIdMgr = new AtomicInteger(1);
	public static int PVE_CHONG_LOU = 100001;
	
	public static final int TYPE_DUN 	= 14;
	public static final int TYPE_QIANG 	= 12;
	public static final int TYPE_GONG 	= 11;
	public static final int TYPE_CHE 	= 13;
	public static final int TYPE_QI 	= 15;
	public static final int TYPE_KING 	= 1;
	public static int LEGEND_DAY_TIMES = 3;
	
	public Map<Integer, PveTemp> id2Pve;
	public Map<Integer, LegendPveTemp> legendId2Pve;
	public Map<Integer, List<NpcTemp>> id2NpcList;
	public Map<Integer, EnemyTemp> id2Enemy;
	public Map<Integer, GuanQiaJunZhu> id2GuanQiaJunZhu;
	public Map<Integer, ShiBing> id2Solder;
	public Map<Integer, GongjiType> id2GongjiType;
	public Map<Integer, SkillTemplate> id2SkillTemplate;
	public Map<Integer, List<LegendNpcTemp>> id2LegendNpcTemp;

	public static Map<Integer, Integer> lastGuanQiaOfZhang = new HashMap<Integer, Integer>();
	public static Map<Integer, PveBigAward> passAwardMap = new HashMap<Integer, PveBigAward>();
	// 玩家进入战斗前，提前算好怪物掉落奖励  <junZhuId, <postionId, List<AwardTemp>>>
	public Map<Long, Map<Integer, List<AwardTemp>>> dropAwardMapBefore = new HashMap<Long, Map<Integer,List<AwardTemp>>>();
	
	public PveMgr(){
		inst = this;
		initData();
	}
	
	public void initData() {
		List list = TempletService.listAll("PveTemp");
		
		Map<Integer, PveTemp> id2Pve = new HashMap<Integer, PveTemp>();
		for(Object o : list){
			PveTemp pveTemp = (PveTemp)o;
			id2Pve.put(pveTemp.getId(), pveTemp);

			/*
			 * 获取章节最大关卡id
			 */
			int big = pveTemp.bigId;
			if(big == 0) {continue;}		// 第0章的不算做章节奖励
			Integer guanqia = lastGuanQiaOfZhang.get(big);
			if(guanqia != null){
				lastGuanQiaOfZhang.put(big, MathUtils.getMax(pveTemp.id, guanqia));
			}else{
				lastGuanQiaOfZhang.put(big, pveTemp.id);
			}
		}
		this.id2Pve = id2Pve;
		
		list = TempletService.listAll("LegendPveTemp");
		Map<Integer, LegendPveTemp> legendId2Pve = new HashMap<Integer, LegendPveTemp>();
		for(Object o : list){
			LegendPveTemp pveTemp = (LegendPveTemp)o;
			legendId2Pve.put(pveTemp.getId(), pveTemp);
		}
		this.legendId2Pve = legendId2Pve;
		
		list = TempletService.listAll("NpcTemp");
		Map<Integer, List<NpcTemp>> id2NpcList = new HashMap<Integer, List<NpcTemp>>();
		for(Object o : list){
			NpcTemp npc = (NpcTemp)o;
			List<NpcTemp> tmp = id2NpcList.get(npc.getNpcId());
			if (tmp == null) {
				tmp = new ArrayList<NpcTemp>();
				id2NpcList.put(npc.getNpcId(), tmp);
			}
			tmp.add(npc);
		}
		this.id2NpcList = id2NpcList;
		
		list = TempletService.listAll("EnemyTemp");
		Map<Integer, EnemyTemp> id2Enemy = new HashMap<Integer, EnemyTemp>();
		for(Object o : list){
			EnemyTemp enemy = (EnemyTemp)o;
			id2Enemy.put((int)enemy.getId(), enemy);
		}
		this.id2Enemy = id2Enemy;

		list = TempletService.listAll(GuanQiaJunZhu.class.getSimpleName());
		Map<Integer, GuanQiaJunZhu> id2GuanQiaJunZhu = new HashMap<Integer, GuanQiaJunZhu>();
		for(Object o : list){
			GuanQiaJunZhu enemy = (GuanQiaJunZhu)o;
			id2GuanQiaJunZhu.put((int)enemy.id, enemy);
		}
		this.id2GuanQiaJunZhu = id2GuanQiaJunZhu;
		
//		list = TempletService.listAll(ShiBing.class.getSimpleName());
//		Map<Integer, ShiBing> id2Solder = new HashMap<Integer, ShiBing>();
//		for(Object o : list){
//			ShiBing solder = (ShiBing)o;
//			id2Solder.put(solder.getId(), solder);
//		}
//		this.id2Solder = id2Solder;
		
		list = TempletService.listAll(GongjiType.class.getSimpleName());
		Map<Integer, GongjiType> id2GongjiType = new HashMap<Integer, GongjiType>();
		for(Object o : list){
			GongjiType gongjiType = (GongjiType)o;
			id2GongjiType.put(gongjiType.getTypeId(), gongjiType);
		}
		this.id2GongjiType = id2GongjiType;
		
		list = TempletService.listAll(SkillTemplate.class.getSimpleName());
		Map<Integer, SkillTemplate> id2SkillTemplate = new HashMap<Integer, SkillTemplate>();
		for(Object o : list){
			SkillTemplate skill = (SkillTemplate) o;
			id2SkillTemplate.put(skill.getId(), skill);
		}
		this.id2SkillTemplate = id2SkillTemplate;

		list = TempletService.listAll(LegendNpcTemp.class.getSimpleName());
		Map<Integer, List<LegendNpcTemp>> id2LegendNpcTemp = new HashMap<Integer, List<LegendNpcTemp>>();
		for(Object o : list){
			LegendNpcTemp npcTemp = (LegendNpcTemp) o;
			List<LegendNpcTemp> legNpcList = id2LegendNpcTemp.get(npcTemp.npcId);
			if(legNpcList == null) {
				legNpcList = new ArrayList<LegendNpcTemp>();
				id2LegendNpcTemp.put(npcTemp.npcId, legNpcList);
			}
			legNpcList.add(npcTemp);
		}
		this.id2LegendNpcTemp = id2LegendNpcTemp;
		
		List listPassA = TempletService.listAll(PveBigAward.class.getSimpleName());
		for(Object o : listPassA){
			PveBigAward passA = (PveBigAward) o;
			passAwardMap.put(passA.bigId, passA);
		}
	}
	
	public void enQueueReq(int code, IoSession session, Builder builder){
		logger.debug("operation code: {}", code);
		
		initBattleInfo(code,session,builder);
	}

	protected void initBattleInfo(int code, IoSession session, Builder builder) {
		//以下代码仅为支持客户端测试
		BattlePveInitReq.Builder req = (BattlePveInitReq.Builder)builder;
		int pveId = 100000 + 100 * req.getBigId() + req.getSmaId();
		BattleInit.Builder battle = createBattleInfo(session,pveId,true);
		if(battle == null){
			return;
		}
		session.write(battle.build());
	}

	public BattleInit.Builder createBattleInfo(IoSession session, int pveId, boolean pve) {
		Hero.Builder hero = Hero.newBuilder();
		hero.setHeroId(0);
		hero.setHeroTempId(0);
		HeroMgr.getInstance().initLord(session, hero);
		logger.info("君主攻击{}防御{}血量{}", hero.getAttackValue(),hero.getDefenceValue(),hero.getHpMax());
		logger.info("确认君主是否真的被初始化了 弓形态 {}" , hero.hasWeaponGong());
		
		//Soldier.Builder soldier = Soldier.newBuilder();
		//soldier.setSoldierId(soldierIdMgr.getAndIncrement());
		
		Troop.Builder troop = Troop.newBuilder();
		troop.setTroopId(troopIdMgr.getAndIncrement());
		troop.setHero(hero);
		troop.setHeroType(1);
		//troop.setSoldiers(soldier);
		troop.setSoldierNum(5);
		
		BattleInit.Builder battle = BattleInit.newBuilder();
		battle.setBattleId(battleIdMgr.getAndIncrement());
		battle.addSelfs(troop);
		
		Soldier.Builder soldier ;
		int maxNum = 5;
		int curNum = 0;
		List<WuJiang> wujiangs = HeroMgr.inst.getWuJiangList(session);
		for(WuJiang w : wujiangs){
			if (curNum >= maxNum) {
				break;
			}
			hero = Hero.newBuilder();
			HeroProtoType type = HeroMgr.id2Hero.get(w.getHeroId());
			if (type == null) {
				logger.error("武将信息异常，没有对应的武将原型{}", w.getHeroId());
				continue;
			}
			hero.setHeroName(HeroService.getNameById(String.valueOf(type.getHeroName())));
			HeroMgr.getInstance().initHero(w, hero);
			
			troop = Troop.newBuilder();
			troop.setTroopId(troopIdMgr.getAndIncrement());
			troop.setHero(hero);
			troop.setHeroType(type.getHeroType());

			ShiBing shiBing = getSoldierByHeroGrowId(w.getHeroGrowId());
			if (shiBing != null) {
				soldier = setSoldierProperties(shiBing, hero);
				troop.setSoldiers(soldier);
				troop.setSoldierNum(shiBing.getNum());
			}
			
			battle.addSelfs(troop);
			curNum ++;
		}

		//测试援军用的
		battle.setYuanjun(troop);
		//enymy info init
		
		PveTemp pveTemp = id2Pve.get(pveId);
		if (pveTemp == null) {
			logger.error("不存在改关卡 {}",pveId);
			return null;
		}
		
		int npcId = pveTemp.getNpcId();
		List<NpcTemp> npcs = id2NpcList.get(npcId);
		if (npcs == null) {
			logger.error("没有对应这个id的NPC：" + String.valueOf(npcId));
			return null;
		}
		
		for(NpcTemp npc : npcs){
			int enemyId = npc.getEnemyId();
			if(enemyId == 0)continue;
			EnemyTemp enemy = id2Enemy.get(enemyId);
			if (enemy == null) {
				logger.error("没有对应这个id的enemy：" + String.valueOf(enemyId));
				continue;
			}
			
			hero = setEnemyHeroProperties(enemy);
			
//			ShiBing shiBing = id2Solder.get(enemy.getShiBingId());
			ShiBing shiBing = id2Solder.get(21010 );
			if (shiBing == null) {
				logger.error("没有找到敌人所带的士兵数据敌将id{}", enemy.getId());
				return null;
			}
			soldier = setSoldierProperties(shiBing, hero);
			
			troop = Troop.newBuilder();
			troop.setTroopId(troopIdMgr.getAndIncrement());
			troop.setHero(hero);
			
			HeroType type = checkType(enemy.getZhiye());
			troop.setHeroType(enemy.getZhiye());
			troop.setSoldiers(soldier);
			troop.setSoldierNum(shiBing.getNum());
			
			battle.addEnemys(troop);
			Random random = new Random();
			battle.setIconId(random.nextInt(4));
		}		
		if(pve){
			session.setAttribute(SessionAttKey.guanQiaId, pveId);
		}
		logger.info("君主的弓形态信息：{}",battle.getSelfs(0).getHero().getWeaponDun().getWeaponRatio());
		logger.info("君主的最大的生命值：{}",battle.getSelfs(0).getHero().getHpMax());
		return battle;
	}
	
	protected Hero.Builder setEnemyHeroProperties(EnemyTemp enemy) {
		GongjiType gongjiType = id2GongjiType.get(1/*enemy.getGongjiType()*/);
		Hero.Builder hero = Hero.newBuilder();
		hero.setAttackValue(enemy.getGongji());
		hero.setDefenceValue(enemy.getFangyu());
		hero.setHpMax(enemy.getShengming());
		hero.setHeroName(enemy.getName());
		hero.setHeroId((int)enemy.getId());
		hero.setHeroTempId((int)enemy.getId());
		hero.setAttackRange(gongjiType.getAttackRange());
		hero.setAttackSpeed(gongjiType.getAttackSpeed());
		hero.setMoveSpeed(gongjiType.getMoveSpeed());
		return hero;
	}

	/**士兵的攻击防御声明按照武将的百分比计算。
	 * @param soldier
	 * @param shiBing
	 */
	protected Soldier.Builder setSoldierProperties(ShiBing shiBing, Hero.Builder hero) {
		Soldier.Builder soldier = Soldier.newBuilder();
		soldier.setSoldierId(shiBing.getId());
		soldier.setAttackRange(shiBing.getAttackRange());
		soldier.setAttackSpeed(shiBing.getAttackSpeed());
		
		double attack = (double)shiBing.getAttackValue()/100 * hero.getAttackValue();
		soldier.setAttackValue((int) attack);
		
		double defense = (double)shiBing.getDefenceValue()/100 * hero.getDefenceValue();
		soldier.setDefenceValue((int)defense);
		
		double hp = (double)shiBing.getHpMax()/100 * hero.getHpMax();
		soldier.setHpMax((int)hp);
		
		soldier.setMoveSpeed(shiBing.getMoveSpeed());
		return soldier;
	}

	protected ShiBing getSoldierByHeroGrowId(int heroGrowId) {
		HeroGrow grow = HeroMgr.id2HeroGrow.get(heroGrowId);
		if (grow == null) {
			logger.error("heroGrow中没有这个id：{}", heroGrowId);
			return null;
		}
		int soldierId = grow.getShiBingId();
		ShiBing soldier = id2Solder.get(soldierId);
		if (soldier == null) {
			logger.error("shibing.xml中没有这个id：{}", soldierId);
			return null;
		}
		
		return soldier;
	}

	protected HeroType checkType(int zhiye) {
		HeroType type;
		switch (zhiye) {
			case TYPE_CHE:
				type = HeroType.TYPE_CHE;
				break;
			case TYPE_QI:
				type = HeroType.TYPE_QI;
				break;
			case TYPE_QIANG:
				type = HeroType.TYPE_QIANG;
				break;
			case TYPE_DUN:
				type = HeroType.TYPE_DUN;
				break;
			case TYPE_KING:
				type = HeroType.TYPE_KING;
				break;
			case TYPE_GONG:
				type = HeroType.TYPE_GONG;
				break;	
			default:
				type = HeroType.TYPE_DUN;
				break;
		}
		return type;
	}
	
	/*************** 以下是新版本的战斗  **********************/
	
	/**
	 * 请求 pve战斗数据
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void PVEDataInfoRequest(int id, IoSession session, Builder builder) {
		PveZhanDouInitReq.Builder req = (qxmobile.protobuf.ZhanDou.PveZhanDouInitReq.Builder) builder;
		int zhangJieId = req.getChapterId();
		
		boolean chuanQiMark = false;
		PveTemp pveTemp = null;
		if(req.getLevelType()==LevelType.LEVEL_TALE){
			pveTemp = legendId2Pve.get(zhangJieId);
			chuanQiMark = true;
		}else{
			pveTemp = id2Pve.get(zhangJieId);
		}
		if(pveTemp == null){
			sendZhanDouInitError(session, "数据配置错误1");
			logger.error("请求pve章节id错误，zhangJieId:{} 类型{}", zhangJieId,req.getLevelType());
			return;
		}

		List<? extends NpcTemp> npcList = null;
		if(req.getLevelType()==LevelType.LEVEL_TALE){
			npcList = id2LegendNpcTemp.get(pveTemp.getNpcId());
		}else{
			npcList = id2NpcList.get(pveTemp.getNpcId());
		}
		if(npcList == null || npcList.size() == 0){
			sendZhanDouInitError(session, "数据配置错误2");
			logger.error("章节怪物配置为空，zhangjieId:{}, npcId:{}", zhangJieId, pveTemp.getNpcId());
			return;
		}
		
		session.setAttribute(SessionAttKey.chuanQiMark, chuanQiMark);
		session.setAttribute(SessionAttKey.guanQiaId, zhangJieId);
		//表示在创建君主之前进入的战斗
		if(zhangJieId == PVE_CHONG_LOU) {
			pveInit4ChongLou(session, pveTemp, npcList);
			return;
		}
		
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			sendZhanDouInitError(session, "找不到君主");
			return;
		}
		
		int star = 0;
		PveRecord r = HibernateUtil.find(PveRecord.class, "where uid="+junZhu.id+" and guanQiaId="+zhangJieId);
		if(r == null){
			session.setAttribute(SessionAttKey.firstQuanQiaId, pveTemp.getId());
		} else {
			if(chuanQiMark && !r.chuanQiPass) {
				session.setAttribute(SessionAttKey.firstQuanQiaId, pveTemp.getId());
				star = r.cqStar;
			} else {
				star = r.achieve;
			}
		}
		
		if(chuanQiMark) {
			if(r != null && r.cqPassTimes >= CanShu.DAYTIMES_LEGENDPVE){
				sendZhanDouInitError(session, "传奇关卡每日只可挑战"+CanShu.DAYTIMES_LEGENDPVE+"次");
				logger.info("{}传奇关卡次数受限{}",junZhu.id, zhangJieId);
				return;
			}
		}
		
		int useTili = pveTemp.getUseHp();
		if(junZhu.tiLi < useTili) {
			logger.error("junzhuId:{}体力不足{},zhangJieId:{}", junZhu.id, useTili, zhangJieId);
			sendZhanDouInitError(session, "体力不足，无法进入战斗");
			return;
		}
		if(pveTemp.useHp > 0) {
			//进入战斗扣1点体力
			JunZhuMgr.inst.updateTiLi(junZhu, -1, "PVE");
			HibernateUtil.update(junZhu);
			JunZhuMgr.inst.sendMainInfo(session,junZhu,false);
			logger.info("junzhu:{}进入战斗，扣除1点体力，关卡类型{}", junZhu.name,req.getLevelType());
		}
		
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(battleIdMgr.incrementAndGet());//战斗id 后台使用
		resp.setMapId(pveTemp.getLandId());
		resp.setLimitTime(CanShu.MAXTIME_PVE);
		
		// 填充己方数据（战斗数据和秘宝信息数据）
		int selfFlagId = 1;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<Node>();
		BuZhenMibaoBean mibaoBean = HibernateUtil.find(BuZhenMibaoBean.class, junZhu.id);
		int zuheId = mibaoBean == null ? -1 : mibaoBean.zuheId;
		fillJunZhuDataInfo(resp, session, selfs, junZhu, selfFlagId, zuheId,selfTroop);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junZhu.id));
		
		// 填充敌方数据
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<Node>();
		fillEnemysDataInfo(pveTemp, npcList, enemys, selfs, selfFlagId++, junZhu.id);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);

		selfTroop.addAllNodes(selfs);
		resp.setSelfTroop(selfTroop);
		resp.setEnemyTroop(enemyTroop);
		
		resp.addStarTemp(pveTemp.getStar1());
		resp.addStarTemp(pveTemp.getStar2());
		resp.addStarTemp(pveTemp.getStar3());
		resp.setStarArrive(star/*r == null ? 0 : r.achieve*/);
		session.write(resp.build());
	}
	
	protected void pveInit4ChongLou(IoSession session, PveTemp pveTemp, List<? extends NpcTemp> enemyIdList) {
		XunHanCheng xunHanCheng = (XunHanCheng) TempletService.listAll(XunHanCheng.class.getSimpleName()).get(0);
		if(xunHanCheng == null) {
			logger.error("未找到‘重楼’关卡-xunHanCheng配置信息");
			sendZhanDouInitError(session, "数据配置错误1");
			return;
		}
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(battleIdMgr.incrementAndGet());
		resp.setMapId(pveTemp.getLandId());
		resp.setLimitTime(pveTemp.getTime());
		
		// 填充己方数据（战斗数据和秘宝信息数据）
		int selfFlagId = 1;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		Node.Builder junzhuNode = Node.newBuilder();
		List<Integer> zbIdList = new ArrayList<Integer>(3);
		zbIdList.add(xunHanCheng.getWeapon1());
		zbIdList.add(xunHanCheng.getWeapon2());
		zbIdList.add(xunHanCheng.getWeapon3());
		//1.填充己方武器数据
		fillZhuangbei4Npc(junzhuNode, zbIdList, xunHanCheng);
		//2.填充己方其它
		junzhuNode.addFlagIds(selfFlagId++);
		junzhuNode.setModleId(xunHanCheng.model);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setNodeName(xunHanCheng.getName());
		junzhuNode.setMoveSpeed(0);
		junzhuNode.setAttackSpeed(0);
		junzhuNode.setAttackRange(0);
		junzhuNode.setEyeRange(0);// 君主视野 全局，发0
		junzhuNode.setAttackValue(xunHanCheng.getGongji());
		junzhuNode.setDefenceValue(xunHanCheng.getFangyu());
		junzhuNode.setHp(xunHanCheng.getShengming());
		junzhuNode.setHpMax(xunHanCheng.getShengming());
		junzhuNode.setAttackAmplify(xunHanCheng.getWqSH());
		junzhuNode.setAttackReduction(xunHanCheng.getWqJM());
		junzhuNode.setAttackAmplifyCri(xunHanCheng.getWqBJ());
		junzhuNode.setAttackReductionCri(xunHanCheng.getWqRX());
		junzhuNode.setSkillAmplify(xunHanCheng.getJnSH());
		junzhuNode.setSkillReduction(xunHanCheng.getJnJM());
		junzhuNode.setSkillAmplifyCri(xunHanCheng.getJnBJ());
		junzhuNode.setSkillReductionCri(xunHanCheng.getJnRX());
		junzhuNode.setCriX(0);
		junzhuNode.setCriY(0);
		junzhuNode.setCriSkillX(0);
		junzhuNode.setCriSkillY(0);
		junzhuNode.setHpNum(1);
		junzhuNode.setAppearanceId(1);
		junzhuNode.setNuQiZhi(0);
		junzhuNode.setMibaoCount(0);
		junzhuNode.setMibaoPower(0);
		junzhuNode.setArmor(0);
		junzhuNode.setArmorMax(0);
		junzhuNode.setArmorRatio(0);
//3.填充己方秘宝列表
//		List<Integer> mibaoIdList = Arrays.asList(xunHanCheng.getMibao1(), 
//				xunHanCheng.getMibao2(), xunHanCheng.getMibao3());
//		
//		for(Integer mibaoId : mibaoIdList) {
//			if(mibaoId <= 0) {
//				continue;
//			}
//			MiBao mibaoCfg = MibaoMgr.mibaoMap.get(mibaoId);
//			selfTroop.addMibaoIcons(mibaoCfg.getIcon());
//			addNodeSkill(junzhuNode, 0);
//		}
		//4.填充秘宝组合技能信息
		fillNpcMibaoDataInfo(xunHanCheng.mibaoZuhe, junzhuNode, xunHanCheng.mibaoZuheLv);
		selfs.add(junzhuNode.build());
		selfTroop.setMaxLevel(0);
		
		// 填充敌方数据
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		fillEnemysDataInfo(pveTemp, enemyIdList, enemys, selfs, selfFlagId, 0);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);

		selfTroop.addAllNodes(selfs);
		resp.setSelfTroop(selfTroop);
		resp.setEnemyTroop(enemyTroop);
		resp.addStarTemp(pveTemp.getStar1());
		resp.addStarTemp(pveTemp.getStar2());
		resp.addStarTemp(pveTemp.getStar3());
		resp.setStarArrive(0);
		session.write(resp.build());
	}

	public void fillEnemysDataInfo(PveTemp pveTemp, List<? extends NpcTemp> npcList,
			List<Node> enemys, List<Node> selfs, int selfFlagId, long junzhuId) {
		Map<Integer, List<AwardTemp>> npcDropAward = new HashMap<Integer, List<AwardTemp>>();
		int index = 0;
		for (NpcTemp npcTemp : npcList) {
			if(310 == npcTemp.position) {
				System.out.println();
			}
			NodeType nodeType = NodeType.valueOf(npcTemp.type);
			if(nodeType == null){
				logger.error("nodeType与npcTemp的type值不一致，npcTemp.type:{}", npcTemp.type);
				continue;
			}
			NodeProfession nodeProfession = NodeProfession.valueOf(npcTemp.profession);
			if(nodeProfession == null) {
				logger.error("nodeProfession与npcTemp的Profession值不一致，npcTemp.Profession:{}", npcTemp.profession);
				continue;
			}
			
			if(nodeType == NodeType.PLAYER) {	//模拟玩家npc
				GuanQiaJunZhu guanQiaJunZhu = id2GuanQiaJunZhu.get(npcTemp.enemyId);
				if(guanQiaJunZhu == null) {
					logger.error("找不到id:{}的GuanQiaJunZhu配置", npcTemp.enemyId);
					return;
				}
				if(npcTemp.ifTeammate == 1) {
					PveMgr.inst.fillNPCPlayerDataInfo(selfs, guanQiaJunZhu, selfFlagId++, npcTemp);
				} else {
					PveMgr.inst.fillNPCPlayerDataInfo(enemys, guanQiaJunZhu, npcTemp.getPosition(), npcTemp);
				}
			} else {
				EnemyTemp enemyTemp = id2Enemy.get(npcTemp.getEnemyId());
				if(enemyTemp == null){
					logger.error("enemy表未发现id为:{}的配置", npcTemp.getEnemyId());
					continue;
				}
				Node.Builder node = Node.newBuilder();
				node.setModleId(npcTemp.modelId);//npc模型id
				node.setNodeType(nodeType);
				node.setNodeProfession(nodeProfession);
				node.setHp(enemyTemp.getShengming() * npcTemp.lifebarNum);
				node.setNodeName(npcTemp.name+"");
				node.setHpNum(npcTemp.lifebarNum);
				node.setAppearanceId(npcTemp.modelApID);
				node.setNuQiZhi(0);
				node.setMibaoCount(0);
				node.setMibaoPower(0);
				node.setArmor(npcTemp.armor);
				node.setArmorMax(npcTemp.armorMax);
				node.setArmorRatio(npcTemp.armorRatio);
				GongjiType gongjiType = id2GongjiType.get(npcTemp.gongjiType);
				fillDataByGongjiType(node, gongjiType);
				fillGongFangInfo(node, enemyTemp);
				String skills = npcTemp.skills;
				if(skills != null && !skills.equals("")){
					String[] skillList = skills.split(",");
					for (String s : skillList) {
						if(s.equals("")){
							continue;
						}
						int skillId = Integer.parseInt(s);
						addNodeSkill(node, skillId);
					}
				}
				List<AwardTemp> npcAwardList = AwardMgr.inst.getHitAwardList(
						npcTemp.award, ",", "=");
				npcDropAward.put(npcTemp.position, npcAwardList);
				int size = npcAwardList.size();
				for (int i = 0; i < size; i++) {
					AwardTemp awardTemp = npcAwardList.get(i);
					DroppenItem.Builder dropItem = DroppenItem.newBuilder();
					dropItem.setId(index);
					dropItem.setCommonItemId(awardTemp.getItemId());
					dropItem.setNum(awardTemp.getItemNum());
					node.addDroppenItems(dropItem);
					index++;
				}
				if(npcTemp.ifTeammate == 1) {
					node.addFlagIds(selfFlagId++);
					selfs.add(node.build());
				} else {
					node.addFlagIds(npcTemp.getPosition());
					enemys.add(node.build());
				}
			}
		}
		if(junzhuId > 0) {
			dropAwardMapBefore.put(junzhuId, npcDropAward);
		}
	}
	
	public void fillNPCPlayerDataInfo(List<Node> selfs, GuanQiaJunZhu guanQiaJunZhu, int flagIndex, NpcTemp npcTemp) {
		Node.Builder npcNode = Node.newBuilder();
		npcNode.addFlagIds(flagIndex);
		// 君主类型
		npcNode.setNodeType(NodeType.PLAYER);
		npcNode.setNodeProfession(NodeProfession.NULL);
		npcNode.setModleId(npcTemp.modelId);
		PveMgr.inst.fillDataByGongjiType(npcNode, null);
		// type
		npcNode.setNodeType(NodeType.valueOf(guanQiaJunZhu.type));
		npcNode.setNodeProfession(NodeProfession.valueOf(guanQiaJunZhu.profession));
		npcNode.setNodeName(npcTemp.name+"");
		npcNode.setHpNum(npcTemp.lifebarNum);
		npcNode.setAppearanceId(npcTemp.modelApID);
		npcNode.setNuQiZhi(0);
		npcNode.setMibaoCount(0);
		npcNode.setMibaoPower(0);
		npcNode.setArmor(npcTemp.armor);
		npcNode.setArmorMax(npcTemp.armorMax);
		npcNode.setArmorRatio(npcTemp.armorRatio);
		PveMgr.inst.fillGongFangInfo(npcNode, guanQiaJunZhu);
		// 添加装备
		List<Integer> weaps = Arrays.asList(guanQiaJunZhu.weapon1, guanQiaJunZhu.weapon2,
				guanQiaJunZhu.weapon3);
		PveMgr.inst.fillZhuangbei4Npc(npcNode, weaps, guanQiaJunZhu);
		// 添加秘宝信息
		List<Integer> skillIdList = MibaoMgr.inst.getSkillIdsFromConfig(guanQiaJunZhu.mibaoZuhe, guanQiaJunZhu.mibaoZuheLv);
		for(Integer skillId : skillIdList) {
			PveMgr.inst.addNodeSkill(npcNode, skillId);
		}
		npcNode.setHp(npcNode.getHpMax() * npcTemp.lifebarNum);
		selfs.add(npcNode.build());
	}

	public void fillJZMiBaoDataInfo( Node.Builder junzhuNode, 
			int skillZuheId, //Group.Builder selfTroop, //List<MiBaoDB> mibaoDBList,
			long jId){
//		for(MiBaoDB miBaoDB : mibaoDBList) {
//			if(miBaoDB.getLevel() <= 0) {//没有激活的秘宝，属性不计算在内
//				continue;
//			}
//			jId = miBaoDB.getOwnerId();
//			MiBao mibao = MibaoMgr.mibaoMap.get(miBaoDB.getMiBaoId());
//			if(mibao.zuheId == skillZuheId) {
//				zuheCount += 1;
//			}
//			// 0.97版本，君主的属性已经在进入主城时，加上所有激活的秘宝属性了
//			selfTroop.addMibaoIcons(mibao.getIcon());
//		} 
		List<Integer> skillIdList = MibaoMgr.inst.getBattleJunZhuSkillIds(skillZuheId, jId);
		for(Integer skillId : skillIdList) {
			addNodeSkill(junzhuNode, skillId);
		}
	}
	
	public void fillNpcMibaoDataInfo(int zuHeId, Node.Builder node, int zuHeLevel) {
//		for(Integer cfgId : mibaoCfgIdList) {
//			if(cfgId <= 0) {
//				continue;
//			}
//			MiBao mibao = MibaoMgr.mibaoMap.get(cfgId);
//			troop.addMibaoIcons(mibao.getIcon());
//			addNodeSkill(node, 0);
//		}
		// 添加秘宝组合技能信息
		List<Integer> skillIdList = MibaoMgr.inst.getSkillIdsFromConfig(zuHeId, zuHeLevel);
		for(Integer skillId : skillIdList) {
			addNodeSkill(node, skillId);
		}
	}
	
	/**
	 * 可以从GongjiType中获取到的战斗数据
	 * 
	 * @param node			
	 * @param gongjiType	为null表示是给君主填充数据
	 */
	public void fillDataByGongjiType(Node.Builder node, GongjiType gongjiType){
		if(gongjiType != null) {
			node.setCriX(gongjiType.getBaojiX());
			node.setCriY(gongjiType.getBaojiY());
			node.setCriSkillX(gongjiType.getBaojiX());
			node.setCriSkillY(gongjiType.getBaojiY());
			node.setMoveSpeed(gongjiType.getMoveSpeed());
			node.setAttackSpeed(gongjiType.getAttackSpeed());
			node.setAttackRange(gongjiType.getAttackRange());
			node.setEyeRange(gongjiType.getShiyeRange());
		} else {
			node.setCriX(0);
			node.setCriY(0);
			node.setCriSkillX(0);
			node.setCriSkillY(0);
			node.setMoveSpeed(0);
			node.setAttackSpeed(0);
			node.setAttackRange(0);
			node.setEyeRange(0);
		}
	}

	public void fillGongFangInfo(Node.Builder node, GameObject object){
		node.setAttackValue(object.getGongji());
		node.setDefenceValue(object.getFangyu());
		// 注意: hpmax == shenming (不用乘以lifebarNum)
		node.setHpMax(object.getShengming());
		node.setAttackAmplify(object.getWqSH());
		node.setAttackReduction(object.getWqJM());
		node.setAttackAmplifyCri(object.getWqBJ());
		node.setAttackReductionCri(object.getWqRX());
		node.setSkillAmplify(object.getJnSH());
		node.setSkillReduction(object.getJnJM());
		node.setSkillAmplifyCri(object.getJnBJ());
		node.setSkillReductionCri(object.getJnRX());
	}
	
	/**
	 * 填充战斗时游戏玩家的信息
	 * @param resp
	 * @param session
	 * @param selfs
	 * @param junZhu
	 * @param flagIndex
	 * @param skillZuheId
	 * @param selfTroop
	 */
	public void fillJunZhuDataInfo(ZhanDouInitResp.Builder resp, IoSession session, 
			List<Node> selfs, JunZhu junZhu, int flagIndex, int skillZuheId,
			Group.Builder selfTroop){
		JunZhuMgr.inst.calcJunZhuTotalAtt(junZhu);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei4Player(junzhuNode, zbIdList, junZhu);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝信息
		fillJZMiBaoDataInfo(junzhuNode, skillZuheId, junZhu.id);
		junzhuNode.setHp(junzhuNode.getHpMax());
		junzhuNode.setHpNum(1);
		junzhuNode.setAppearanceId(1);
		junzhuNode.setNuQiZhi(MibaoMgr.inst.getChuShiNuQi(junZhu.id));
		junzhuNode.setArmor(0);
		junzhuNode.setArmorMax(0);
		junzhuNode.setArmorRatio(0);
		Chenghao curChengHao = ChenghaoMgr.inst.getCurEquipCfg(junZhu.id);
		if(curChengHao == null) {
			junzhuNode.setFinalAmplify(0);
			junzhuNode.setFinalReduction(0);
		} else {
			junzhuNode.setFinalAmplify(curChengHao.add_injury_scale / 100);
			junzhuNode.setFinalReduction(curChengHao.reduce_injury_scale / 100);
		}
		junzhuNode.setMibaoPower(JunZhuMgr.inst.getAllMibaoProvideZhanli(junZhu));
		junzhuNode.setMibaoCount(0);
		selfs.add(junzhuNode.build());
	}
	/**
	 * 填充押镖战斗时押镖玩家的信息
	 * @param resp
	 * @param session
	 * @param selfs
	 * @param junZhu
	 * @param flagIndex
	 * @param skillZuheId
	 * @param selfTroop
	 */
	public void fillYaBiaoJunZhuDataInfo(ZhanDouInitResp.Builder resp, IoSession session, 
			List<Node> selfs, JunZhu junZhu, int flagIndex, int skillZuheId,int Hp,
			Group.Builder selfTroop){
		JunZhuMgr.inst.calcJunZhuTotalAtt(junZhu);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei4Player(junzhuNode, zbIdList, junZhu);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝信息
		fillJZMiBaoDataInfo(junzhuNode, skillZuheId, junZhu.id );
		junzhuNode.setHp(Hp);
		junzhuNode.setHpNum(1);
		junzhuNode.setAppearanceId(1);
		junzhuNode.setNuQiZhi(MibaoMgr.inst.getChuShiNuQi(junZhu.id));
		junzhuNode.setMibaoCount(0);
		junzhuNode.setMibaoPower(JunZhuMgr.inst.getAllMibaoProvideZhanli(junZhu));
		junzhuNode.setArmor(0);
		junzhuNode.setArmorMax(0);
		junzhuNode.setArmorRatio(0);
		selfs.add(junzhuNode.build());
	}
	public void fillYaBiaoJunZhuDataInfo4YB(ZhanDouInitResp.Builder resp, IoSession session, 
			List<Node> selfs, JunZhu junZhu, int flagIndex, int skillZuheId,int Hp,int hudun,int hudunMax,
			Group.Builder selfTroop){
		JunZhuMgr.inst.calcJunZhuTotalAtt(junZhu);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei4Player(junzhuNode, zbIdList, junZhu);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝技能信息
		fillJZMiBaoDataInfo(junzhuNode, skillZuheId, junZhu.id);
		junzhuNode.setHp(Hp);
		//护盾
		junzhuNode.setHudun(hudun);
		junzhuNode.setHudunMax(hudunMax);
		junzhuNode.setHpNum(1);
		junzhuNode.setAppearanceId(1);
		junzhuNode.setNuQiZhi(MibaoMgr.inst.getChuShiNuQi(junZhu.id));
		junzhuNode.setMibaoCount(0);
		junzhuNode.setMibaoPower(JunZhuMgr.inst.getAllMibaoProvideZhanli(junZhu));
		junzhuNode.setArmor(0);
		junzhuNode.setArmorMax(0);
		junzhuNode.setArmorRatio(0);
		selfs.add(junzhuNode.build());
	}
	
	public void addNodeSkill(Node.Builder node, int skillId) {
		if(skillId <= 0){
			return;
		}
		SkillTemplate skillCfg = id2SkillTemplate.get(skillId);
		if(skillCfg == null) {
			logger.error("SkillTemplate配置错误，未发现技能id:{}的相关信息", skillId);
			return;
		}
		NodeSkill.Builder nodeSkill = NodeSkill.newBuilder();
		nodeSkill.setId(skillId);
		nodeSkill.setName(skillCfg.getName());
		nodeSkill.setZhiye(skillCfg.getZhiye());
		nodeSkill.setSkillType(skillCfg.getSkillType());
		nodeSkill.setValue1(skillCfg.getValue1());
		nodeSkill.setValue2(skillCfg.getValue2());
		nodeSkill.setValue3(skillCfg.getValue3());
		nodeSkill.setValue4(skillCfg.getValue4());
		nodeSkill.setValue5(skillCfg.getValue5());
		nodeSkill.setValue6(skillCfg.getValue6());
		nodeSkill.setValue7(skillCfg.value7);
		nodeSkill.setEndtime(skillCfg.endTime);
		nodeSkill.setTimePeriod(skillCfg.getTimePeriod());
		nodeSkill.setZhudong(skillCfg.zhudong == 1 ? true : false);
		nodeSkill.setImmediately(skillCfg.immediately);
		node.addSkills(nodeSkill);
	}
	
	public void fillZhuangbei4Npc(Node.Builder junzhuNode, List<Integer> zbIdList, GameObject gb) {
		for(Integer zbid : zbIdList){
			ZhuangBei zhuangBei = HeroMgr.id2ZhuangBei.get(zbid);
			if (zhuangBei == null) {
				logger.error("装备不存在，id是: " + String.valueOf(zbid));
				continue;
			}
			SkillTemplate xiShuCarry = null;
			JiNengPeiYang py = null;
			switch(zhuangBei.getBuWei()){
			case HeroMgr.WEAPON_HEAVY:
				{
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(gb.getPugongHeavy());
				}
				break;
			case HeroMgr.WEAPON_LIGHT:
				{
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(gb.getPugongLight());
				}
				break;
			case HeroMgr.WEAPON_RANGED:
				{
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(gb.getPugongRange());
				}
				break;
			default:
				logger.error("填充装备数据出错，没有该部位id:{}的装备", zhuangBei.getBuWei());
				continue;
			}
			int skillId = py.skillId;
			if(py.isPuGong == 1) {//表示是普攻，伤害系数读的配置需要从PuGong表里读技能
				final int jiNengId = py.id;
				List<PuGong> pgList = TempletService.listAll(PuGong.class.getName());
				if(pgList != null) {
					Optional<PuGong> optional = pgList.stream()
							.filter(pg -> (pg.modelID == gb.getRoleId((int) gb.getId()) && pg.id == jiNengId))
							.findFirst();
					PuGong puGong = optional.get();
					if(puGong != null) {
						skillId = puGong.skillId;
					}
				}
			}
			xiShuCarry = id2SkillTemplate.get(skillId);
			if(xiShuCarry == null) {
				logger.error("填充装备数据出错，skillTemplete未找到");
				continue;
			}
			// FIXME 如果返回的是null是否有问题
			PlayerWeapon.Builder weaponBuilder = fillPlayerWeapon(zhuangBei, junzhuNode, xiShuCarry);
			switch(zhuangBei.getBuWei()){
				case HeroMgr.WEAPON_HEAVY:
					weaponBuilder.addSkillLevel(gb.getPugongHeavy());
					weaponBuilder.addSkillLevel(gb.getSkill1Heavy());
					weaponBuilder.addSkillLevel(gb.getSkill2Heavy());
					junzhuNode.setWeaponHeavy(weaponBuilder);
					break;
				case HeroMgr.WEAPON_LIGHT:
					weaponBuilder.addSkillLevel(gb.getPugongRange());
					weaponBuilder.addSkillLevel(gb.getSkill1Light());
					weaponBuilder.addSkillLevel(gb.getSkill2Light());
					junzhuNode.setWeaponLight(weaponBuilder);
					break;
				case HeroMgr.WEAPON_RANGED:
					weaponBuilder.addSkillLevel(gb.getPugongRange());
					weaponBuilder.addSkillLevel(gb.getSkill1Range());
					weaponBuilder.addSkillLevel(gb.getSkill2Range());
					junzhuNode.setWeaponRanged(weaponBuilder);
					break;
				default:
					logger.error("填充装备数据出错，没有该部位id:{}的装备", zhuangBei.getBuWei());
					break;
			}		
		}
	}
	
	
	public void fillZhuangbei4Player(Node.Builder junzhuNode, List<Integer> zbIdList, JunZhu junzhu) {
		// 因为获取新技能列表后就把新技能清空了，所以必须在填充装备数据前面获取出来
		int[] newSkillIds = JiNengPeiYangMgr.inst.getNewJNIds(junzhu.id);
		for(Integer zbid : zbIdList){
			ZhuangBei zhuangBei = HeroMgr.id2ZhuangBei.get(zbid);
			if (zhuangBei == null) {
				logger.error("装备不存在，id是: " + String.valueOf(zbid));
				continue;
			}
			
			JNBean bean = HibernateUtil.find(JNBean.class, junzhu.id);
			if(bean == null){
				bean = JiNengPeiYangMgr.inst.getDefaultBean();
			}
			JiNengPeiYangMgr.inst.fixOpenByLevel(bean, junzhu.id);
			JiNengPeiYang py = null;
			switch(zhuangBei.getBuWei()){
				case HeroMgr.WEAPON_HEAVY:
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq1_1);
					break;
				case HeroMgr.WEAPON_LIGHT:
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq2_1);
					break;
				case HeroMgr.WEAPON_RANGED:
					py = JiNengPeiYangMgr.inst.jiNengPeiYangMap.get(bean.wq3_1);
					break;
				default:
					continue;
			}
			int skillId = py.skillId;
			if(py.isPuGong == 1) {//表示是普攻，伤害系数读的配置需要从PuGong表里读技能
				final int jiNengId = py.id;
				List<PuGong> pgList = TempletService.listAll(PuGong.class.getName());
				if(pgList != null) {
					Optional<PuGong> optional = pgList.stream()
							.filter(pg -> (pg.modelID == junzhu.roleId && pg.id == jiNengId))
							.findFirst();
					PuGong puGong = optional.get();
					if(puGong != null) {
						skillId = puGong.skillId;
					}
				}
			}

			SkillTemplate xiShuCarry = null;
			xiShuCarry = id2SkillTemplate.get(skillId);
			if(xiShuCarry == null) {
				logger.error("战斗填充装备信息错误，找不到skillTemp配置id:{}", skillId);
				continue;
			}
			PlayerWeapon.Builder weaponBuilder = fillPlayerWeapon(zhuangBei, junzhuNode, xiShuCarry);
			switch(zhuangBei.getBuWei()){
				case HeroMgr.WEAPON_HEAVY:
					weaponBuilder.addSkillLevel(bean.wq1_1);
					weaponBuilder.addSkillLevel(bean.wq1_2);
					weaponBuilder.addSkillLevel(bean.wq1_3);
					addWeaponSkillFirstActive(weaponBuilder, newSkillIds);
					junzhuNode.setWeaponHeavy(weaponBuilder);
					break;
				case HeroMgr.WEAPON_LIGHT:
					weaponBuilder.addSkillLevel(bean.wq2_1);
					weaponBuilder.addSkillLevel(bean.wq2_2);
					weaponBuilder.addSkillLevel(bean.wq2_3);
					addWeaponSkillFirstActive(weaponBuilder, newSkillIds);
					junzhuNode.setWeaponLight(weaponBuilder);
					break;
				case HeroMgr.WEAPON_RANGED:
					weaponBuilder.addSkillLevel(bean.wq3_1);
					weaponBuilder.addSkillLevel(bean.wq3_2);
					weaponBuilder.addSkillLevel(bean.wq3_3);
					addWeaponSkillFirstActive(weaponBuilder, newSkillIds);
					junzhuNode.setWeaponRanged(weaponBuilder);
					break;
				default:
					logger.error("填充装备数据出错，没有该部位id:{}的装备", zhuangBei.getBuWei());
					break;
			}
		}
	}
	
	public void addWeaponSkillFirstActive(PlayerWeapon.Builder weaponBuilder, int[] newSkillIds) {
		List<Integer> skillLevelList = weaponBuilder.getSkillLevelList();
		for(int i = 0; i < newSkillIds.length; i++) {
			int index = skillLevelList.indexOf(newSkillIds[i]);
			if(index != -1) {
				weaponBuilder.addSkillFirstActive(index);
			}
		}
	}
	
	/**
	 * 填充君主携带的武器数据信息
	 * @param zhuangBei
	 * @param weaponHeavy
	 */
	protected PlayerWeapon.Builder fillPlayerWeapon(ZhuangBei zhuangBei, Node.Builder junzhuNode,
			SkillTemplate xiShuCarry) {
		PlayerWeapon.Builder weaponBuilder = PlayerWeapon.newBuilder();
		GongjiType gongjiType = id2GongjiType.get(zhuangBei.getGongjiType());
		if(gongjiType == null){
			logger.error("装备的GongjiType未找到，装备ID {}, GongjiType {}", zhuangBei.id, zhuangBei.getGongjiType());
			return weaponBuilder;
		}
		weaponBuilder.setWeaponId(zhuangBei.modelId);
		weaponBuilder.setMoveSpeed(gongjiType.getMoveSpeed());
		weaponBuilder.setAttackSpeed(gongjiType.getAttackSpeed());
		weaponBuilder.setAttackRange(gongjiType.getAttackRange());
		weaponBuilder.addWeaponRatio(xiShuCarry.value1);
		weaponBuilder.addWeaponRatio(xiShuCarry.value2);
		weaponBuilder.addWeaponRatio(xiShuCarry.value3);
		weaponBuilder.addWeaponRatio(xiShuCarry.value4);
		weaponBuilder.setCriX(gongjiType.getBaojiX());
		weaponBuilder.setCriY(gongjiType.getBaojiY());
		weaponBuilder.setCriSkillX(gongjiType.getJnbaojiX());
		weaponBuilder.setCriSkillY(gongjiType.getJnbaojiY());
		return weaponBuilder;
	}

	public void battleOver(int id, IoSession session, Builder builder) {
		PveBattleOver.Builder request = (qxmobile.protobuf.PveLevel.PveBattleOver.Builder) builder;
		boolean win = request.getSPass();
		Integer guanQiaId = (Integer) session
				.getAttribute(SessionAttKey.guanQiaId);
		if (guanQiaId == null || guanQiaId == PveMgr.PVE_CHONG_LOU) {
			AwardMgr.inst.getAward(guanQiaId, false, false, session, null);
			return;
		}
		// AntiCheatMgr.anti = true;
		if (AntiCheatMgr.anti) {// 检查作弊
			Object o = session.removeAttribute(SessionAttKey.antiCheatPass);
			if (o == null) {
				return;
			}
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		Long junZhuId = junZhu.id;
		Boolean chuanQiMark = (Boolean) session
				.getAttribute(SessionAttKey.chuanQiMark);
		if (junZhuId == null || guanQiaId == null) {
			logger.error("null value , pid {}, guanQiaId{}", junZhuId, guanQiaId);
			return;
		}
		logger.info("{}请求领取奖励{},类型{}", junZhuId, guanQiaId, chuanQiMark);

		PveTemp pveTemp = null;
		if (chuanQiMark != null && chuanQiMark) {
			pveTemp = PveMgr.inst.legendId2Pve.get(guanQiaId);
		} else {
			pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		}
		if (pveTemp == null) {
			logger.error("请求pve章节id错误，zhangJieId:{}", guanQiaId);
			return;
		}
		ActLog.log.HeroBattle(junZhuId, junZhu.name, ActLog.vopenid, guanQiaId, pveTemp.smaName, win?1:2, 1);
		List<AwardTemp> getNpcAwardList = new ArrayList<AwardTemp>();
		int resultForLog;//0 失败；2首次；3再次
		if (win) {
			PveRecord r = HibernateUtil.find(PveRecord.class,
					"where guanQiaId=" + guanQiaId + " and uid=" + junZhuId);
			if (r == null) {
				r = new PveRecord();
				// 改主键不自增
				r.dbId = TableIDCreator.getTableID(PveRecord.class, 1L);
				r.guanQiaId = guanQiaId;
				//wxh
				if(guanQiaId > junZhu.commonChptrMaxId){
					junZhu.setCommonChptrMaxId(guanQiaId);
				}
				r.uid = junZhuId;
				resultForLog = 2;
			}else{
				resultForLog = 3;
			}
			if (chuanQiMark != null && chuanQiMark) {
				r.chuanQiPass = true;
				//wxh
				if(guanQiaId >junZhu.legendChptrMaxId){
					junZhu.setLegendChptrMaxId(guanQiaId);
				}
				r.cqPassTimes += 1;
				r.cqWinLevel = Math.max(r.cqWinLevel, request.getStar());
				r.cqStar = r.cqStar | request.getAchievement();
				logger.info("{}传奇关卡{}", junZhuId, guanQiaId);
			} else {
				r.starLevel = request.getStar();
				r.star = Math.max(r.star, request.getStar());
				r.achieve = r.achieve | request.getAchievement();
				chuanQiMark = false;
			}
			logger.info("junZhuId {} 关卡{} 战斗结束 , 成功", junZhuId, guanQiaId);
			logger.info("获得星级 star {}", request.getStar());
			HibernateUtil.save(r);
			
			List<Integer> droppenList = request.getDropeenItemNpcsList();
			Set<Integer> giveSet = new HashSet<>(droppenList.size());
			Map<Integer, List<AwardTemp>> npcDropAwardMap = dropAwardMapBefore.get(junZhu.id);
			for (Integer npcPos : droppenList) {
				if(giveSet.contains(npcPos)) {
					continue;
				}
				List<AwardTemp> posNpcDropAward = npcDropAwardMap.get(npcPos);
				if(posNpcDropAward != null) {
					// 根据掉落类型获取应该获得的奖励
					getNpcAwardList.addAll(posNpcDropAward);
				}
				giveSet.add(npcPos);
			}
			
			//限时活动精英集星
			EventMgr.addEvent(ED.JINGYINGJIXING, junZhuId);
			// 战斗胜利扣除剩余应该消耗的体力
			int useTili = pveTemp.getUseHp();
			useTili -= 1;
			JunZhuMgr.inst.updateTiLi(junZhu, -useTili, "关卡胜利");
			HibernateUtil.update(junZhu);
			JunZhuMgr.inst.sendMainInfo(session,junZhu,false);
			logger.info("junzhu:{}在关卡zhangjieId:{}战斗胜利扣除体力:{}", junZhu.name,
					guanQiaId, useTili);
			Integer bigLastGuanQiaId = lastGuanQiaOfZhang.get(pveTemp.bigId);
			if(bigLastGuanQiaId != null) {
				if(!r.isGetAward && bigLastGuanQiaId.equals(guanQiaId)){
					FunctionID.pushCanShowRed(junZhuId, session, FunctionID.PVE_PASS_ZHANGJIE_GET_AWARD);
				}
			}
		} else {
			logger.info("{} pve fail at {}", junZhuId, guanQiaId);
			resultForLog = 0;
		}
		OurLog.log.RoundFlow(ActLog.vopenid,guanQiaId.intValue(), 2, request.getStar(), 0, resultForLog,String.valueOf(junZhuId));
		AwardMgr.inst.getAward(guanQiaId, chuanQiMark, win, session, getNpcAwardList);
		if (chuanQiMark != null && chuanQiMark) {
			if (win) {
				// 主线任务：完成传奇关卡并且胜利一次
				EventMgr.addEvent(ED.CHUANQI_GUANQIA_SUCCESS, new Object[] {
						junZhuId, guanQiaId });
			}
			// 每日任务中记录完成传奇关卡一次（不论输赢）
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junZhuId, DailyTaskConstants.chuanqi_guanqia_3, 1));
		}else{
			if(win){
				// 主线任务：完成普通关卡并且胜利一次
				EventMgr.addEvent(ED.PVE_GUANQIA, new Object[] { junZhuId,
						guanQiaId });
			}
			// 每日任务中记录完成过关斩将1次(无论输赢)
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junZhuId, DailyTaskConstants.guoguan_5_id, 1));
		}
		// 2015-7-22 15:46 过关榜刷新
		EventMgr.addEvent(ED.GUOGUAN_RANK_REFRESH, new Object[]{junZhu, junZhu.guoJiaId});
	}
	
	/**
	 * 发送战斗初始化错误信息
	 * @param session
	 * @param msg
	 */
	public void sendZhanDouInitError(IoSession session, String msg) {
		ZhanDouInitError.Builder errorResp = ZhanDouInitError.newBuilder();
		errorResp.setResult(msg);
		session.write(errorResp.build());
	}

	public Map<Integer, PveTemp> getId2Pve() {
		return id2Pve;
	}

	public Map<Integer, List<NpcTemp>> getId2NpcList() {
		return id2NpcList;
	}

	public Map<Integer, EnemyTemp> getId2Enemy() {
		return id2Enemy;
	}

	public Map<Integer, ShiBing> getId2Solder() {
		return id2Solder;
	}

	public Map<Integer, GongjiType> getId2GongjiType() {
		return id2GongjiType;
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.REFRESH_TIME_WORK:
			IoSession session=(IoSession) event.param;
			if(session==null){
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz==null){
				break;
			}
			Map<Integer, PveRecord> map = BigSwitch.pveGuanQiaMgr.recordMgr.getRecords(jz.id);
			for(Map.Entry<Integer, Integer> entry : PveMgr.lastGuanQiaOfZhang.entrySet()) {
				Integer guanqiaID = entry.getValue();
				if(guanqiaID == null){
					continue;
				}
				PveRecord r = map.get(guanqiaID);
				// 已经通关
				if(r == null){
					continue;
				}
				// 是不是已经领取
				if(!r.isGetAward){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.PVE_PASS_ZHANGJIE_GET_AWARD);
					return;
				}
			}
			break;
		default:
			logger.error("错误事件参数", event.id);
			break;
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
	
}
