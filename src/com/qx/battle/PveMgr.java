package com.qx.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattlePveInit.BattleInit;
import qxmobile.protobuf.BattlePveInit.BattlePveInitReq;
import qxmobile.protobuf.BattlePveInit.Hero;
import qxmobile.protobuf.BattlePveInit.HeroType;
import qxmobile.protobuf.BattlePveInit.Soldier;
import qxmobile.protobuf.BattlePveInit.Troop;
import qxmobile.protobuf.ZhanDou;
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

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.HeroGrow;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.LegendNpcTemp;
import com.manu.dynasty.template.LegendPveTemp;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.NpcTemp;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.ShiBing;
import com.manu.dynasty.template.SkillTemplate;
import com.manu.dynasty.template.XunHanCheng;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.qx.bag.EquipMgr;
import com.qx.hero.HeroMgr;
import com.qx.hero.WuJiang;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MiBaoSkillDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.BuZhenMibaoBean;
import com.qx.pve.PveRecord;
import com.qx.world.GameObject;

/**
 * 目前的目的是测试用。
 * 日后需要增加单线程处理。（具体可参考Scene）
 * @author hudali
 *
 */
public class PveMgr {
	public static PveMgr inst;
	public static long godId = 0;
	public static Logger eLogger = LoggerFactory.getLogger(PveMgr.class);
	public AtomicInteger troopIdMgr = new AtomicInteger(1);
	public static AtomicInteger battleIdMgr = new AtomicInteger(1);
	public static int PVE_CHONG_LOU = 100101;
	
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
	public Map<Integer, ShiBing> id2Solder;
	public Map<Integer, GongjiType> id2GongjiType;
	public Map<Integer, SkillTemplate> id2SkillTemplate;
	public Map<Integer, List<LegendNpcTemp>> id2LegendNpcTemp;
	
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
	}
	
	public void enQueueReq(int code, IoSession session, Builder builder){
		eLogger.debug("operation code: {}", code);
		
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
		eLogger.info("君主攻击{}防御{}血量{}", hero.getAttackValue(),hero.getDefenceValue(),hero.getHpMax());
		eLogger.info("确认君主是否真的被初始化了 弓形态 {}" , hero.hasWeaponGong());
		
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
				eLogger.error("武将信息异常，没有对应的武将原型{}", w.getHeroId());
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
			eLogger.error("不存在改关卡 {}",pveId);
			return null;
		}
		
		int npcId = pveTemp.getNpcId();
		List<NpcTemp> npcs = id2NpcList.get(npcId);
		if (npcs == null) {
			eLogger.error("没有对应这个id的NPC：" + String.valueOf(npcId));
			return null;
		}
		
		for(NpcTemp npc : npcs){
			int enemyId = npc.getEnemyId();
			if(enemyId == 0)continue;
			EnemyTemp enemy = id2Enemy.get(enemyId);
			if (enemy == null) {
				eLogger.error("没有对应这个id的enemy：" + String.valueOf(enemyId));
				continue;
			}
			
			hero = setEnemyHeroProperties(enemy);
			
//			ShiBing shiBing = id2Solder.get(enemy.getShiBingId());
			ShiBing shiBing = id2Solder.get(21010 );
			if (shiBing == null) {
				eLogger.error("没有找到敌人所带的士兵数据敌将id{}", enemy.getId());
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
		eLogger.info("君主的弓形态信息：{}",battle.getSelfs(0).getHero().getWeaponDun().getWeaponRatio());
		eLogger.info("君主的最大的生命值：{}",battle.getSelfs(0).getHero().getHpMax());
		return battle;
	}
	
	protected Hero.Builder setEnemyHeroProperties(EnemyTemp enemy) {
		GongjiType gongjiType = id2GongjiType.get(enemy.getGongjiType());
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
			eLogger.error("heroGrow中没有这个id：{}", heroGrowId);
			return null;
		}
		int soldierId = grow.getShiBingId();
		ShiBing soldier = id2Solder.get(soldierId);
		if (soldier == null) {
			eLogger.error("shibing.xml中没有这个id：{}", soldierId);
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
		JunZhu junZhu =  JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			eLogger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			sendZhanDouInitError(session, "找不到君主");
			return;
		}
		
		PveRecord r = HibernateUtil.find(PveRecord.class, "where uid="+junZhu.id+" and guanQiaId="+zhangJieId);
		boolean chuanQiMark = false;
		PveTemp pveTemp = null;
		List<? extends NpcTemp> npcList = null;
		if(req.getLevelType()==LevelType.LEVEL_TALE){
			if(r != null && r.cqPassTimes >= LEGEND_DAY_TIMES){
				sendZhanDouInitError(session, "传奇关卡每日只可挑战"+LEGEND_DAY_TIMES+"次");
				eLogger.info("{}传奇关卡次数受限{}",junZhu.id, zhangJieId);
				return;
			}
			pveTemp = legendId2Pve.get(zhangJieId);
			chuanQiMark = true;
		}else{
			pveTemp = id2Pve.get(zhangJieId);
		}
		if(pveTemp == null){
			sendZhanDouInitError(session, "数据配置错误1");
			eLogger.error("请求pve章节id错误，zhangJieId:{} 类型{}", zhangJieId,req.getLevelType());
			return;
		}
		if(req.getLevelType()==LevelType.LEVEL_TALE){
			npcList = id2LegendNpcTemp.get(pveTemp.getNpcId());
		}else{
			npcList = id2NpcList.get(pveTemp.getNpcId());
		}
		if(npcList == null || npcList.size() == 0){
			sendZhanDouInitError(session, "数据配置错误2");
			eLogger.error("章节怪物配置为空，zhangjieId:{}, npcId:{}", zhangJieId, pveTemp.getNpcId());
			return;
		}
		
		if(r == null){
			session.setAttribute(SessionAttKey.firstQuanQiaId, pveTemp.getId());
		} else {
			if(chuanQiMark && !r.chuanQiPass) {
				session.setAttribute(SessionAttKey.firstQuanQiaId, pveTemp.getId());
			}
		}
		
		session.setAttribute(SessionAttKey.chuanQiMark, chuanQiMark);
		session.setAttribute(SessionAttKey.guanQiaId, zhangJieId);
		if(zhangJieId == PVE_CHONG_LOU) {
			pveInit4ChongLou(session, pveTemp, npcList, junZhu);
			return;
		}
		
		int useTili = pveTemp.getUseHp();
		if(junZhu.tiLi < useTili) {
			eLogger.error("junzhuId:{}体力不足{},zhangJieId:{}", junZhu.id, useTili, zhangJieId);
			sendZhanDouInitError(session, "体力不足，无法进入战斗");
			return;
		}
		if(pveTemp.useHp > 0) {
			//进入战斗扣1点体力
			JunZhuMgr.inst.updateTiLi(junZhu, -1, "PVE");
			HibernateUtil.save(junZhu);
			JunZhuMgr.inst.sendMainInfo(session);
			eLogger.info("junzhu:{}进入战斗，扣除1点体力，关卡类型{}", junZhu.name,req.getLevelType());
		}
		
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(battleIdMgr.incrementAndGet());//战斗id 后台使用
		resp.setMapId(pveTemp.getLandId());
		resp.setLimitTime(CanShu.MAXTIME_PVE);
		
		// 填充敌方数据
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<Node>();
		fillEnemysDataInfo(pveTemp, npcList, enemys);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);
		resp.setEnemyTroop(enemyTroop);
		
		// 填充己方数据（战斗数据和秘宝信息数据）
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<Node>();
		BuZhenMibaoBean mibaoBean = HibernateUtil.find(BuZhenMibaoBean.class, junZhu.id);
		int zuheId = mibaoBean == null ? -1 : mibaoBean.zuheId;
		fillJunZhuDataInfo(resp, session, selfs, junZhu,1, zuheId,selfTroop);
		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junZhu.id));
		resp.setSelfTroop(selfTroop);
		
		resp.addStarTemp(pveTemp.getStar1());
		resp.addStarTemp(pveTemp.getStar2());
		resp.addStarTemp(pveTemp.getStar3());
		session.write(resp.build());
	}
	protected void pveInit4ChongLou(IoSession session, PveTemp pveTemp, List<? extends NpcTemp> enemyIdList,
			JunZhu junZhu) {
		XunHanCheng xunHanCheng = (XunHanCheng) TempletService.listAll(XunHanCheng.class.getSimpleName()).get(0);
		if(xunHanCheng == null) {
			eLogger.error("未找到‘重楼’关卡-xunHanCheng配置信息");
			sendZhanDouInitError(session, "数据配置错误1");
			return;
		}
		int zhandouId = battleIdMgr.incrementAndGet();    //战斗id 后台使用
		int mapId = pveTemp.getLandId();
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(zhandouId);
		resp.setMapId(mapId);
		resp.setLimitTime(pveTemp.getTime());
		// 填充敌方数据
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		fillEnemysDataInfo(pveTemp, enemyIdList, enemys);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);
		resp.setEnemyTroop(enemyTroop);
		
		// 填充己方数据（战斗数据和秘宝信息数据）
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		Node.Builder junzhuNode = Node.newBuilder();
		List<Integer> zbIdList = new ArrayList<Integer>();
		zbIdList.add(xunHanCheng.getWeapon1());
		zbIdList.add(xunHanCheng.getWeapon2());
		zbIdList.add(xunHanCheng.getWeapon3());
		//1.填充己方武器数据
		fillZhuangbei(junzhuNode, zbIdList);
		//2.填充己方其它
		junzhuNode.addFlagIds(1);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setNodeName(junZhu.getName());
		junzhuNode.setMoveSpeed(0);
		junzhuNode.setAttackSpeed(0);
		junzhuNode.setAttackRange(0);
		junzhuNode.setEyeRange(0);// 君主视野 全局，发0
		junzhuNode.setAttackValue(xunHanCheng.getGongji());
		junzhuNode.setDefenceValue(xunHanCheng.getFangyu());
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
		junzhuNode.setHp(junzhuNode.getHpMax());
		//3.填充己方秘宝列表
		List<Integer> mibaoIdList = Arrays.asList(xunHanCheng.getMibao1(), 
				xunHanCheng.getMibao2(), xunHanCheng.getMibao3());
		
		for(Integer mibaoId : mibaoIdList) {
			if(mibaoId <= 0) {
				continue;
			}
			MiBao mibaoCfg = MibaoMgr.mibaoMap.get(mibaoId);
			selfTroop.addMibaoIcons(mibaoCfg.getIcon());
			addNodeSkill(junzhuNode, 0);
		}
		//4.填充秘宝组合技能信息
		int mibaoZuheSkillId = -1;
		mibaoZuheSkillId = MibaoMgr.inst.getMibaoZuheSkillId(mibaoIdList);
		addNodeSkill(junzhuNode, mibaoZuheSkillId);
		selfs.add(junzhuNode.build());
		selfTroop.setMaxLevel(0);
		selfTroop.addAllNodes(selfs);
		resp.setSelfTroop(selfTroop);
		resp.addStarTemp(pveTemp.getStar1());
		resp.addStarTemp(pveTemp.getStar2());
		resp.addStarTemp(pveTemp.getStar3());
		session.write(resp.build());
	}

	public void fillEnemysDataInfo(PveTemp pveTemp,
			List<? extends NpcTemp> npcList, List<Node> enemys) {
		for (NpcTemp npcTemp : npcList) {
			Node.Builder node = Node.newBuilder();
			EnemyTemp enemyTemp = id2Enemy.get(npcTemp.getEnemyId());
			if(enemyTemp == null){
				eLogger.error("enemy表未发现id为:{}的配置", npcTemp.getEnemyId());
				continue;
			}
			NodeType nodeType = NodeType.valueOf(npcTemp.type);
			if(nodeType == null){
				eLogger.error("nodeType与npcTemp的type值不一致，npcTemp.type:{}", npcTemp.type);
				continue;
			}
			NodeProfession nodeProfession = NodeProfession.valueOf(npcTemp.profession);
			if(nodeProfession == null) {
				eLogger.error("nodeProfession与npcTemp的Profession值不一致，npcTemp.Profession:{}", npcTemp.profession);
				continue;
			}
			node.addFlagIds(npcTemp.getPosition());
			node.setModleId(npcTemp.modelId);//npc模型id
			node.setNodeType(nodeType);
			node.setNodeProfession(nodeProfession);
			node.setHp(enemyTemp.getShengming());
			node.setNodeName(npcTemp.name+"");
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
			enemys.add(node.build());
		}
	}
	
	public void fillJZMiBaoDataInfo(ZhanDouInitResp.Builder resp, Node.Builder junzhuNode, 
			int skillZuheId, Group.Builder selfTroop, //List<MiBaoDB> mibaoDBList,
			long jId){
		int zuheCount = 0;
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
		MiBaoSkillDB skillD = HibernateUtil.find(MiBaoSkillDB.class, jId * MibaoMgr.skill_db_space + skillZuheId);
		if(skillD != null){
			if(skillD.hasClear){
				zuheCount = 2;
			}
			if(skillD.hasJinjie){
				zuheCount = 3;
			}
		}
		List<Integer> skillIdList = MibaoMgr.inst.getBattleSkillIds(skillZuheId, zuheCount);
		for(Integer skillId : skillIdList) {
			addNodeSkill(junzhuNode, skillId);
		}
	}
	
	public void fillNpcMibaoDataInfo(List<Integer> mibaoCfgIdList, Node.Builder node, Group.Builder troop) {
		for(Integer cfgId : mibaoCfgIdList) {
			if(cfgId <= 0) {
				continue;
			}
			MiBao mibao = MibaoMgr.mibaoMap.get(cfgId);
			troop.addMibaoIcons(mibao.getIcon());
			addNodeSkill(node, 0);
		}
		// 添加秘宝组合技能信息
		int zuheSkillId = MibaoMgr.inst.getMibaoZuheSkillId(mibaoCfgIdList);
		addNodeSkill(node, zuheSkillId);
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
		List<MiBaoDB> miBaoDBList = HibernateUtil.list(MiBaoDB.class, " where ownerId=" + junZhu.id);
		JunZhuMgr.inst.cacMiBaoAtt(junZhu, miBaoDBList);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei(junzhuNode, zbIdList);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝信息
		fillJZMiBaoDataInfo(resp, junzhuNode, skillZuheId, selfTroop, junZhu.id);
		junzhuNode.setHp(junzhuNode.getHpMax());
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
		List<MiBaoDB> miBaoDBList = HibernateUtil.list(MiBaoDB.class, " where ownerId=" + junZhu.id);
		JunZhuMgr.inst.cacMiBaoAtt(junZhu, miBaoDBList);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei(junzhuNode, zbIdList);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝信息
		fillJZMiBaoDataInfo(resp, junzhuNode, skillZuheId, selfTroop, junZhu.id );
		junzhuNode.setHp(Hp);
		selfs.add(junzhuNode.build());
	}
	public void fillYaBiaoJunZhuDataInfo4YB(ZhanDouInitResp.Builder resp, IoSession session, 
			List<Node> selfs, JunZhu junZhu, int flagIndex, int skillZuheId,int Hp,int hudun,int hudunMax,
			Group.Builder selfTroop){
		List<MiBaoDB> miBaoDBList = HibernateUtil.list(MiBaoDB.class, " where ownerId=" + junZhu.id);
		JunZhuMgr.inst.cacMiBaoAtt(junZhu, miBaoDBList);
		Node.Builder junzhuNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(junZhu);
		fillZhuangbei(junzhuNode, zbIdList);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.addFlagIds(flagIndex);
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(junZhu.roleId);
		junzhuNode.setNodeName(junZhu.name);
		fillDataByGongjiType(junzhuNode, null);
		fillGongFangInfo(junzhuNode, junZhu);
		// 添加秘宝信息
		fillJZMiBaoDataInfo(resp, junzhuNode, skillZuheId, selfTroop, junZhu.id);
		junzhuNode.setHp(Hp);
		//护盾
		junzhuNode.setHudun(hudun);
		junzhuNode.setHudunMax(hudunMax);
		selfs.add(junzhuNode.build());
	}
	
	public void addNodeSkill(Node.Builder node, int skillId) {
		if(skillId <= 0){
			return;
		}
		SkillTemplate skillCfg = id2SkillTemplate.get(skillId);
		if(skillCfg == null) {
			eLogger.error("SkillTemplate配置错误，未发现技能id:{}的相关信息", skillId);
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
		node.addSkills(nodeSkill);
	}
	
	public void fillZhuangbei(Node.Builder junzhuNode,
			List<Integer> zbIdList) {
		for(Integer zbid : zbIdList){
			ZhuangBei zhuangBei = HeroMgr.id2ZhuangBei.get(zbid);
			if (zhuangBei == null) {
				eLogger.error("装备不存在，id是: " + String.valueOf(zbid));
				continue;
			}
			switch(zhuangBei.getBuWei()){
			case HeroMgr.WEAPON_HEAVY:
			case HeroMgr.WEAPON_LIGHT:
			case HeroMgr.WEAPON_RANGED:
				break;
			default:
				continue;
			}
			PlayerWeapon.Builder weaponBuilder = PlayerWeapon.newBuilder(); 
			fillPlayerWeapon(zhuangBei, weaponBuilder, junzhuNode);
			switch(zhuangBei.getBuWei()){
				case HeroMgr.WEAPON_HEAVY:
					junzhuNode.setWeaponHeavy(weaponBuilder);
					break;
				case HeroMgr.WEAPON_LIGHT:
					junzhuNode.setWeaponLight(weaponBuilder);
					break;
				case HeroMgr.WEAPON_RANGED:
					junzhuNode.setWeaponRanged(weaponBuilder);
					break;
				default:
					eLogger.error("填充装备数据出错，没有该部位id:{}的装备", zhuangBei.getBuWei());
					break;
			}		
		}
	}
	
	/**
	 * 填充君主携带的武器数据信息
	 * @param zhuangBei
	 * @param weaponHeavy
	 */
	protected void fillPlayerWeapon(ZhuangBei zhuangBei,
			qxmobile.protobuf.ZhanDou.PlayerWeapon.Builder weaponHeavy, Node.Builder junzhuNode) {
		GongjiType gongjiType = id2GongjiType.get(zhuangBei.getGongjiType());
		if(gongjiType == null){
			eLogger.error("装备的GongjiType未找到，装备ID {}, GongjiType {}",
					zhuangBei.id, zhuangBei.getGongjiType());
			return;
		}
		weaponHeavy.setWeaponId(zhuangBei.modelId);
		weaponHeavy.setMoveSpeed(gongjiType.getMoveSpeed());
		weaponHeavy.setAttackSpeed(gongjiType.getAttackSpeed());
		weaponHeavy.setAttackRange(gongjiType.getAttackRange());
		for(float xishu : zhuangBei.getXishuArray()) {
			weaponHeavy.addWeaponRatio(xishu);
		}
		weaponHeavy.setCriX(gongjiType.getBaojiX());
		weaponHeavy.setCriY(gongjiType.getBaojiY());
		weaponHeavy.setCriSkillX(gongjiType.getJnbaojiX());
		weaponHeavy.setCriSkillY(gongjiType.getJnbaojiY());
		if(zhuangBei.skill != null && !zhuangBei.skill.equals("")) {
			String[] skills = zhuangBei.skill.split(",");
			for(String skillId : skills) {
				addNodeSkill(junzhuNode, Integer.parseInt(skillId));
			}
		}
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
	
	
	
	
	
}
