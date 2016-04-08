package com.qx.hero;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattlePveInit.Hero;
import qxmobile.protobuf.BattlePveInit.HeroWeapon;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.WuJiangProtos.BuZhenHero;
import qxmobile.protobuf.WuJiangProtos.BuZhenHeroList;
import qxmobile.protobuf.WuJiangProtos.HeroActivatReq;
import qxmobile.protobuf.WuJiangProtos.HeroActivatResp;
import qxmobile.protobuf.WuJiangProtos.HeroDate;
import qxmobile.protobuf.WuJiangProtos.HeroGrowReq;
import qxmobile.protobuf.WuJiangProtos.HeroGrowResp;
import qxmobile.protobuf.WuJiangProtos.HeroInfoResp;
import qxmobile.protobuf.WuJiangProtos.JingPoRefreshResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.HeroGrow;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.HeroStar;
import com.manu.dynasty.template.JingPo;
import com.manu.dynasty.template.Keji;
import com.manu.dynasty.template.KejiChengzhang;
import com.manu.dynasty.template.ShengXing;
import com.manu.dynasty.template.ShuXingXiShu;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.achievement.AchievementCondition;
import com.qx.achievement.AchievementConstants;
import com.qx.bag.Bag;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.jingmai.JmBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.pve.PveMgr;
import com.qx.world.Mission;

/**
 * 注意：
 * 1 若干精魄合成一个武将（数据来自JingPo.xml）
 * 2 武将升星消耗若干精魄（数据来自JingPo.xml和HeroStar.xml）
 * 3 如是否拥有一个武将将取决于武将的一个判定字段combine
 *
 */
public class HeroMgr implements Runnable{
	public static Logger log = LoggerFactory.getLogger(HeroMgr.class);
	public static int maxGridCount = 200;//目前的配置有不到100个武将
	public static int spaceFactor = 1000;
	public static final int ATTACK = 1; 
	public static final int DEFENSE = 2;
	public static final int HP = 3;
	public static final int ZHIMOU = 4;
	public static final int WUYI = 5;
	public static final int TONGSHUAI = 6;
	
	//30001 30002 30003 30101 30102 30103
	public static final int DAO_FEMALE 		= 30001;
	public static final int QIANG_FEMALE 	= 30002;
	public static final int GONG_FEMALE 	= 30003;
	public static final int DAO_MALE 		= 30101;
	public static final int QIANG_MALE		= 30102;
	public static final int GONG_MALE		= 30103;
	
	public static final int WEAPON_HEAVY	= 1;
	public static final int WEAPON_LIGHT	= 2;
	public static final int WEAPON_RANGED	= 3;
	
	//策划说的，分为百分比和数值.最好统一一下，方便之后数据的使用。
	public static final int VALUE = 1;
	public static final int PERCENT = 2;
	public static HeroWeapon.Builder nullZiTai;
	
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static Mission exit = new Mission(0,null,null);
	
	
	public static HeroMgr inst;
	/**
	 * key:heroId
	 * value:Hero
	 */
	public static Map<Integer, HeroProtoType> id2Hero = new HashMap<Integer, HeroProtoType>();;
	public static Map<Integer, HeroProtoType> tempId2HeroProto = new HashMap<Integer, HeroProtoType>();
	/**
	 * key:id
	 * value:HeroGrow
	 */
	public static Map<Integer, HeroGrow> id2HeroGrow = new HashMap<Integer, HeroGrow>();;
	/**
	 * key:装备id
	 * value：装备
	 */
	public static Map<Integer, ZhuangBei> id2ZhuangBei = new HashMap<Integer, ZhuangBei>();
	
	/**
	 * key:heroId
	 * value:对应这个heroId的所有（星级）的武将
	 */
	public static Map<Integer, List<HeroGrow>> heroId2HeroGrow = new HashMap<Integer, List<HeroGrow>>();
	
	/**
	 * key:heroId
	 * value:ShuXingXiShu
	 */
	public static Map<Integer, ShuXingXiShu> heroId2ShuXingXiShu = new HashMap<Integer, ShuXingXiShu>();
	
	/**
	 * 升星。
	 */
	public Map<Integer, HeroStar> id2HeroStar;
	
	/**
	 * 武将升星获取经脉点数信息。
	 */
	public Map<Integer, ShengXing> id2ShengXing;
	
	/**
	 * 精魄。
	 */
	public static Map<Integer, JingPo> id2JingPo;
	
	/**
	 * 科技成长。<qualityId, List<KejiChengzhang>>
	 */
	public Map<Integer, List<KejiChengzhang>> qualityToKejiChengzhang; 
	
	public HeroMgr(){
		inst = this;
		initData();
	}
	
	public void startMissionThread(){
		//线程的命名规则待定
//		new Thread(this, "HeroMgr No.1").start();
	}
	
	public void exec(int code, IoSession session, Builder builder) {
//		Mission mission = new Mission(code,session,builder);
//		missions.add(mission);
	}
	
	public void initData() {
		/*
		List heros = TempletService.listAll("HeroProtoType");
		Map<Integer, HeroProtoType> id2Hero = new HashMap<Integer, HeroProtoType>();;
		for(Object o : heros){
			HeroProtoType hero = (HeroProtoType)o;
			id2Hero.put(hero.getHeroId(), hero);
			tempId2HeroProto.put(hero.getTempId(), hero);
		}
		this.id2Hero = id2Hero;
		
		List heroGrows = TempletService.listAll("HeroGrow");
		Map<Integer, HeroGrow> id2HeroGrow = new HashMap<Integer, HeroGrow>();;
		for(Object o : heroGrows){
			HeroGrow heroGrow = (HeroGrow)o;
			id2HeroGrow.put(heroGrow.getId(), heroGrow);
			
			List<HeroGrow> list = heroId2HeroGrow.get(heroGrow.getHeroId());
			if (list == null) {
				list = new ArrayList<HeroGrow>();
				heroId2HeroGrow.put(heroGrow.getHeroId(), list);
			}
			list.add(heroGrow);
		}
		this.id2HeroGrow = id2HeroGrow;
		*/
		List Zhuangbeis = TempletService.listAll("ZhuangBei");
		Map<Integer, ZhuangBei> id2ZhuangBei = new HashMap<Integer, ZhuangBei>();
		for(Object o : Zhuangbeis){
			ZhuangBei zhuangBei = (ZhuangBei)o;
			id2ZhuangBei.put(zhuangBei.getId(), zhuangBei);
		}
		this.id2ZhuangBei = id2ZhuangBei;
		/*
		List tmp = TempletService.listAll(ShuXingXiShu.class.getSimpleName());
		Map<Integer, ShuXingXiShu> heroId2ShuXingXiShu = new HashMap<Integer, ShuXingXiShu>();
		for(Object o : tmp){
			ShuXingXiShu shuXingXiShu = (ShuXingXiShu)o;
			heroId2ShuXingXiShu.put(shuXingXiShu.getHeroId(), shuXingXiShu);
		}
		this.heroId2ShuXingXiShu = heroId2ShuXingXiShu;
		
		List shengXing = TempletService.listAll(ShengXing.class.getSimpleName());
		Map<Integer, ShengXing> id2ShengXing = new HashMap<Integer, ShengXing>();
		for(Object o : shengXing){
			ShengXing sXing = (ShengXing)o;
			id2ShengXing.put(sXing.getQuality(), sXing);
		}
		this.id2ShengXing = id2ShengXing;
		
		List stars = TempletService.listAll(HeroStar.class.getSimpleName());
		Map<Integer, HeroStar> id2HeroStar = new HashMap<Integer, HeroStar>();
		if (stars == null || stars.size() == 0) {
			log.error("武将升星数据初始化异常");
		}else{
			for(Object o : stars){
				HeroStar star = (HeroStar)o;
				id2HeroStar.put(star.getId(), star);
			}
		}
		this.id2HeroStar = id2HeroStar;
		
		List<JingPo> jingPos = TempletService.listAll(JingPo.class.getSimpleName());
		Map<Integer, JingPo> id2JingPo = new HashMap<Integer, JingPo>();
		if (jingPos == null || jingPos.size() == 0) {
			log.error("精魄初始化数据异常");
		}else {
			for(Object o : jingPos){
				JingPo jingPo = (JingPo)o;
				id2JingPo.put(jingPo.getId(), jingPo);
			}
		}
		this.id2JingPo = id2JingPo;
		
		List<KejiChengzhang> kejiChengzhangs = TempletService.listAll(KejiChengzhang.class.getSimpleName());
		Map<Integer, List<KejiChengzhang>> qualityToKejiChengzhang = new HashMap<Integer, List<KejiChengzhang>>();
		if(kejiChengzhangs == null || kejiChengzhangs.size() == 0){
			log.error("科技成长数据初始化异常");
		}else {
			for(KejiChengzhang kjcz : kejiChengzhangs){
				List<KejiChengzhang> kjczList = qualityToKejiChengzhang.get(kjcz.getQuality());
				if(kjczList == null){
					kjczList = new ArrayList<KejiChengzhang>();
					qualityToKejiChengzhang.put(kjcz.getQuality(), kjczList);
				}
				kjczList.add(kjcz);
			}
		}
		this.qualityToKejiChengzhang = qualityToKejiChengzhang;
	*/
	}

	public static HeroMgr getInstance(){
		return inst;
	}
	
	public HeroProtoType getHeroTypeById(int heroId){
		return id2Hero.get(heroId);
	}
	
	/**
	 * 测试用，临时函数。
	 * @param heroId
	 * @param hero
	 * @return
	 */
	public boolean initLord(IoSession session, Hero.Builder hero){
		if (id2Hero.size() == 0) {
			initData();
			initLord(session, hero);
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		JunZhuMgr.inst.calcJunZhuTotalAtt(junZhu);//FIXME 后期要避免重复计算。
		hero.setHeroName(junZhu.name);
		
		int attack = junZhu.gongJi;
		int defense = junZhu.fangYu;
		int hp = junZhu.shengMingMax;
		log.info("01 君主攻击{}防御{}血量{}", attack,defense,hp);
		
		//List<Integer> zhuangBeiIds = new ArrayList<Integer>();
		
		/**
		 * 101001~102001~103001 是武器
		 */
		Bag<EquipGrid> bag = EquipMgr.inst.loadEquips(junZhu.id);
		/*String ids = "101001,102001,103001,111001,112001,113001,114001,115001,116001";
		String[]tmpids = ids.split(",");
		for(String tmpId : tmpids){
			zhuangBeiIds.add(Integer.parseInt(tmpId));
		}*/
		
		for(EquipGrid bg : bag.grids){
			if(bg == null)continue;
			ZhuangBei item = id2ZhuangBei.get(bg.itemId);
			if (item == null) {
				log.error("装备不存在，id是: " + String.valueOf(bg.itemId));
				continue;
			}
			//attack = attack+item.getGongji();
			//defense = defense + item.getFangyu();
			//hp = hp + item.getShengming();
			HeroWeapon.Builder ziTai;
			switch(item.getBuWei()){
				case WEAPON_HEAVY:
					ziTai = initLordZiTai(DAO_FEMALE, item);
					hero.setWeaponDun(ziTai);
					break;
				case WEAPON_LIGHT:
					ziTai = initLordZiTai(QIANG_FEMALE, item);
					hero.setWeaponQiang(ziTai);
					break;
				case WEAPON_RANGED:
					ziTai = initLordZiTai(GONG_FEMALE, item);
					hero.setWeaponGong(ziTai);
					break;
			}
		}
		if(!hero.hasWeaponDun())hero.setWeaponDun(HeroMgr.nullZiTai());
		if(!hero.hasWeaponQiang())hero.setWeaponQiang(HeroMgr.nullZiTai());
		if(!hero.hasWeaponGong())hero.setWeaponGong(HeroMgr.nullZiTai());
		hero.setAttackValue(attack);
		hero.setDefenceValue(defense);
		hero.setHpMax(hp);
		
		return true;
	}
	
	public HeroWeapon.Builder initLordZiTai(int growId, ZhuangBei weapon) {
		HeroWeapon.Builder ziTai = HeroWeapon.newBuilder();
		log.info("初始化君主的不同姿态。。。。{}", weapon.getBuWei());
		HeroGrow grow = id2HeroGrow.get(growId);
		if (grow == null) {
			log.error("没有找到相应的herogrow，本次的growId是 {}", growId);
			return nullZiTai();
		}
		
		int heroId = grow.getHeroId();
		HeroProtoType hero = id2Hero.get(heroId);
		if (hero == null) {
			log.error("没有找到相应的heroprototype，本次的heroid是  {}", heroId);
			return nullZiTai();
		}
		GongjiType gongjiType = PveMgr.inst.getId2GongjiType().get(hero.getGongjiType());
		ziTai.setAttackRange(gongjiType.getAttackRange());
		ziTai.setAttackSpeed(gongjiType.getAttackSpeed());
		ziTai.setMoveSpeed(gongjiType.getMoveSpeed());
		ziTai.setWeaponRatio(weapon.getXishuArray()[0]);//应该是数组，用不到暂时这样写，2015年6月15日 11:26:47
		log.info("初始化完成");
		return ziTai;
	}

	public static HeroWeapon.Builder nullZiTai() {
		if (nullZiTai == null) {
			nullZiTai = HeroWeapon.newBuilder();	
			nullZiTai.setAttackRange(0);
			nullZiTai.setAttackSpeed(0);
			nullZiTai.setMoveSpeed(0);
			nullZiTai.setWeaponRatio(0);
		}
		
		return nullZiTai;
	}

	/**
	 * 测试用，临时函数。
	 * @param heroGrowId
	 * @param hero
	 * @return
	 */
	public boolean initHero(WuJiang wujiang, Hero.Builder hero){
		int heroId = wujiang.getHeroId();
		HeroProtoType heroType = id2Hero.get(heroId);
		if (heroType == null) {
			log.error("没有这个id对应的heroprototype{}", heroId);
			return false;
		}
		GongjiType gongjiType = PveMgr.inst.getId2GongjiType().get(heroType.getGongjiType());
		
		hero.setAttackValue(wujiang.getAttack());
		hero.setDefenceValue(wujiang.getDefense());
		hero.setHpMax(wujiang.getHp());
		hero.setHeroId(wujiang.getHeroGrowId());
		hero.setHeroTempId(wujiang.getHeroGrowId());
		//eLogger.info("this hero ownerId {}, heroId {}, heroGrowId {}", wujiang.getOwnerId(), wujiang.getHeroId(), wujiang.getHeroGrowId());
		hero.setAttackRange(gongjiType.getAttackRange());
		hero.setAttackSpeed(gongjiType.getAttackSpeed());
		hero.setMoveSpeed(gongjiType.getMoveSpeed());
		hero.setZhimou(wujiang.getZhimou());
		hero.setWuyi(wujiang.getWuyi());
		hero.setTongshuai(wujiang.getTongshuai());
		
		return true;
	}

	public HeroGrow getHeroGrowById(int heroGrowId){
		return id2HeroGrow.get(heroGrowId);
	}
	
	//*************************工具方法****************************

	public void addWuJiangNum(WuJiang wuJiang) {
		int jingPoId = id2Hero.get(wuJiang.getHeroId()).getJingpoId();
		JingPo jingPo = id2JingPo.get(jingPoId);
		wuJiang.setNum(wuJiang.getNum() + jingPo.getFenjieNum());
		HibernateUtil.save(wuJiang);
	}
	
	public WuJiang createWuJiangBean(HeroGrow hg, long pid){
		WuJiang w = new WuJiang();
		w.setOwnerId(pid);
		w.setCombine(false);
		w.setHeroGrowId(hg.getId());
		w.setHeroId(hg.getHeroId());
		return w;
	}
	public void addNewWuJiang(WuJiang wuJiang, long junZhuId){
		long start = junZhuId * spaceFactor;
		
		String mcKey = "WuJiangCnt#" + junZhuId;
		Object mcO = MemcachedCRUD.getMemCachedClient().get(mcKey);
		if(mcO == null){ 
			MemcachedCRUD.getMemCachedClient().add(mcKey, Integer.valueOf(1));
			wuJiang.dbId = start;
		}else{
			Integer cnt = Integer.valueOf(mcO.toString());
			MemcachedCRUD.getMemCachedClient().set(mcKey, cnt+1);
			wuJiang.dbId = start + cnt;
		}
		calcZhanLi(wuJiang);
		MC.add(wuJiang, wuJiang.dbId);
		HibernateUtil.insert(wuJiang);
	}

	/**
	 * 由heroGrow获取武将信息。(武将此时是初始化状态，0经验最低星级等)
	 * @param heroGrow
	 * @param userid
	 * @return
	 */
	public void fillWuJiangAttInfo(WuJiang wj, long userid, WjKeJi keJi) {
		int heroGrowId = wj.getHeroGrowId();
		HeroGrow heroGrow = id2HeroGrow.get(heroGrowId);
		if (heroGrow == null) {
			log.error("武将信息异常，找不到对应的heroGrow:{}", heroGrowId);
			return;
		}
		
		HeroProtoType heroProtoType = id2Hero.get(heroGrow.getHeroId());
		if (heroProtoType == null) {
			log.error("{}武将不存在，heroId:", heroGrow.getHeroId());
			return ;
		}
		int wujiangQuality = heroProtoType.getQuality();
		int attack = getAttr(heroGrow, ATTACK, keJi, wujiangQuality);
		if (attack < 0) {
			log.error("获取武将攻击异常");
			attack = 0;
		}
		int defense = getAttr(heroGrow, DEFENSE, keJi, wujiangQuality);
		if (defense < 0) {
			log.error("获取武将防御异常");
			defense = 0;
		}
		
		int hp = getAttr(heroGrow, HP, keJi, wujiangQuality);
		if (hp < 0) {
			log.error("获取武将生命值异常");
			hp = 1;
		}
		int zhimou = getTmpAttr(heroGrow, ZHIMOU,  keJi);
		int wuyi = getTmpAttr(heroGrow, WUYI, keJi);
		int tongshuai = getTmpAttr(heroGrow, TONGSHUAI, keJi);
		
		WuJiang wuJiang = wj;
		//小心！这里指允许设置transient标记的属性。
		wuJiang.attack = attack;
		wuJiang.defense=defense;
		wuJiang.zhimou=zhimou;
		wuJiang.tongshuai=tongshuai;
		wuJiang.wuyi=wuyi;
		wuJiang.hp=hp;
		wuJiang.label=heroProtoType.getLabel();
		wuJiang.quality=heroProtoType.getQuality();
		log.info(
				"战斗武将heroId:{},attack:{},defense:{},zhimou:{},tongshuai:{},wuyi:{},hp:{}",
				heroGrow.getHeroId(), attack, defense, zhimou, tongshuai, wuyi,
				hp);		
	}

	/**
	 * 获取武将攻击、防御、生命等受到其他因素影响的属性。
	 * @param heroGrow
	 * @param type
	 * @return
	 */
	public int getAttr(HeroGrow heroGrow, int type, WjKeJi techs, int wujiangQuality) {
		
		ShuXingXiShu shuXingXiShu = heroId2ShuXingXiShu.get(heroGrow.getHeroId());
		if (shuXingXiShu == null) {
			log.error("{}武将没有对应的属性系数", heroGrow.getHeroId());
			return -1;
		}
		
		float buffRatio = 0f;		// 系数
		int baseAttr = 0;			// 基础属性
		float kejiJiacheng = 0F;	// 星级加成
		Keji keJi = null;
		switch (type) {
			case ATTACK:
				buffRatio = shuXingXiShu.getRatioA();
				baseAttr = heroGrow.getGongji();
				keJi = WuJiangKeJiMgr.inst.getKeJi(techs.getAttack());
				kejiJiacheng = heroGrow.getGongjijiacheng();
				break;
			case DEFENSE:
				buffRatio = shuXingXiShu.getRatioB();
				baseAttr = heroGrow.getFangyu();
				keJi = WuJiangKeJiMgr.inst.getKeJi(techs.getDefense());
				kejiJiacheng = heroGrow.getFangyujiacheng();
				break;
			case HP:
				buffRatio = shuXingXiShu.getRatioC();
				baseAttr = heroGrow.getShengming();
				keJi = WuJiangKeJiMgr.inst.getKeJi(techs.getHp());
				kejiJiacheng = heroGrow.getShengmingjiacheng();
				break;

			default:
				log.error("unkown type {}", type);
				return -1;
		}
		float grow = heroGrow.getChengzhang();// + getAttachChengzhang(wujiangQuality, keJi.getLevel());
		if(keJi != null){
//			eLogger.error("没有找到KeJi数据");
			grow += getAttachChengzhang(wujiangQuality, keJi.getLevel());
		}
		float attrBuff = 1.0f + kejiJiacheng;					// 属性加成
		int techLevel = (keJi == null) ? 0 : keJi.getValue();	// 科技等级
		// 【最终面版属性】=(【基础属性】+【科技等级】*【成长】*【系数】)*【属性加成】
		float tmpFinalAttr = (baseAttr + techLevel * grow * buffRatio) * attrBuff;
		int finalAttr = (int)tmpFinalAttr;
		return finalAttr;
	}
	
	/**
	 * 获取武将智谋、武艺、统帅等受到其他因素影响的属性。
	 * @param heroGrow
	 * @param type
	 * @param tech
	 * @return
	 */
	public int getTmpAttr(HeroGrow heroGrow,int type, WjKeJi tech) {
		int tmpAttr = -1;
		Keji keji = null;
		switch (type) {
			case ZHIMOU:
				keji = WuJiangKeJiMgr.inst.getKeJi(tech.getZhiMou());
				tmpAttr = heroGrow.getZhimou();
				break;
			case WUYI:
				keji = WuJiangKeJiMgr.inst.getKeJi(tech.getWuYi());
				tmpAttr = heroGrow.getWuyi();
				break;
			case TONGSHUAI:
				keji = WuJiangKeJiMgr.inst.getKeJi(tech.getTongShuai());
				tmpAttr = heroGrow.getTongshui();
				break;

			default:
				log.error("unknown type {}", type);
				break;
		}
		
		if (tmpAttr != -1) {
			//WuJiangKeJi tech = WuJiangKeJiMgr.inst.getTech(accId, type);
			if (tech == null) {
				log.error("该类型的科技不存在{}", type);
			}else{
				if (keji == null) {
					log.error("不存在对应武将科技{}", tech.getJunZhuId());
					keji = new Keji();
				}
				tmpAttr = tmpAttr + keji.value;
			}
		}
		
		return tmpAttr;
	}
	
	/**
	 * 从链接信息中获取用户id。
	 * @param session
	 * @return
	 */
	public long getJunZhuId(IoSession session) {
		Long junZhuId = (Long)session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null || junZhuId < 0) {
			return -1;
		}
		return junZhuId;
	}
	public void shutdown(){
		missions.add(exit);
	}
	@Override
	public void run() {
		while(GameServer.shutdown==false){
			Mission mission = null;
			try {
				mission = missions.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}
			if(mission==exit){
				break;
			}
				try {
					completeMission(mission);
				} catch (Throwable e) {
					log.error("completeMission error {}", mission.code);
					log.error("run方法异常", e);
				}
		}
	}

	public void completeMission(Mission mission) {
		if (mission == null) {
			log.error("mission is null...");
			return;
		}
		
		int code = mission.code;
		Builder builder = mission.builer;
		IoSession session = mission.session;
		switch(code){
			case PD.HERO_INFO_REQ:
				sendWuJiangList(session);
				break;
			case PD.WUJIANG_LEVELUP_REQ:
				wuJiangLevelUp(code, session, builder);
				break;
		}
	}

	/**
	 * 武将升级。
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void wuJiangLevelUp(int code, IoSession session, Builder builder) {
		HeroGrowReq.Builder req = (HeroGrowReq.Builder)builder;
		long junZhuId = getJunZhuId(session);
		int heroGrowId = req.getHeroGrowId();
		WuJiang wuJiang = getWuJiangByHeroGrowId(junZhuId, heroGrowId);
		if (wuJiang == null) {
			error(session, code, "你没有这个武将");
			return;
		}
		// 是否有对应的 HeroGrow数据
		HeroGrow currentHeroGrow = id2HeroGrow.get(heroGrowId);
		if (currentHeroGrow == null) {
			log.error("武将数据异常，没有对应 heroGorw数据，Id:{}", wuJiang.getHeroGrowId());
			error(session, code, "武将数据异常，没有对应heroGorw数据");
			return;
		}
		// 检查武将星级是否已满
		HeroStar currentHeroStar = id2HeroStar.get(currentHeroGrow.getStarId());
		if(currentHeroStar == null){
			log.error("武将星级已满");
			error(session, code, "武将星级已满");
			return;
		}
		int nextStar = currentHeroStar.getNextStar();
		HeroGrow nextHeroGrow = getHeroGrowByHeroIdAndStar(wuJiang.getHeroId(), nextStar);
		// 武将下一星级的数据
		if(nextHeroGrow == null){
			log.error("武将数据异常，没有对应 heroGrow数据，star:{}", nextStar);
			error(session, code, "武将数据异常，没有对应heroGrow数据 by nextStar:" + nextStar);
			return;
		}
		
		boolean ret = false;
		JmBean jm = HibernateUtil.find(JmBean.class, wuJiang.getOwnerId());
		if (jm == null) {
			jm = new JmBean();
			jm.dbId = wuJiang.getOwnerId();
			jm.daMai = 1;
			jm.xueWei = new int[20];
			jm.zhouTian = 1;
			jm.point = 0;
			// 添加到缓存中
			MC.add(jm, jm.dbId);
			HibernateUtil.insert(jm);
		}
		ret = WuJiangShengXing(code, session, wuJiang, nextHeroGrow, currentHeroStar, jm);

		if (ret) {
			wuJiang.setHeroGrowId(nextHeroGrow.getId());
			HibernateUtil.save(jm);
			HibernateUtil.save(wuJiang);
			log.info("武将升星成功");
		}else {
			log.error("武将升星失败");
			return;
		}
		
		HeroGrowResp.Builder resp = HeroGrowResp.newBuilder();
		// 武将属性计算
		WjKeJi keJi = HibernateUtil.find(WjKeJi.class,  junZhuId);
		if (keJi == null) {
			keJi = WuJiangKeJiMgr.inst.createDefaultBean(junZhuId);
		}
		fillWuJiangAttInfo(wuJiang, junZhuId ,keJi);
		HeroDate.Builder hero = getHeroDataByWuJiang(wuJiang, wuJiang);
		resp.setHero(hero);
		resp.setJingmai(jm.point);
		session.write(resp.build());
		
	}
	
	/**
	 * 根据heroId和星级star获取对应的 HeroGrow信息
	 * 
	 * @param heroId
	 * @param star
	 * @return
	 */
	public HeroGrow getHeroGrowByHeroIdAndStar(int heroId, int star) {
		List<HeroGrow> heroGrowList = heroId2HeroGrow.get(heroId);
		if(heroGrowList == null || heroGrowList.size() == 0){
			log.error("获取不到heroId:{}的 heroGrow列表配置信息", heroId);
			return null;
		}
		HeroGrow heroGrow = null;
		for(HeroGrow grow : heroGrowList){
			if(grow.getStar() == star){
				heroGrow = grow;
				break;
			}
		}
		return heroGrow;
	}
	
	/**
	 * 武将升星带来的数据变化。
	 * @param wuJiang
	 */
	public boolean WuJiangShengXing(int code, IoSession session, WuJiang wuJiang, HeroGrow grow, HeroStar star, JmBean jm) {
		HeroProtoType type = id2Hero.get(grow.getHeroId());
		if (type == null) {
			log.error("没有对应的herotype数据，heroId为{}", grow.getHeroId());
			return false;
		}
		
		JingPo jingPo = id2JingPo.get(type.getJingpoId());
		if (jingPo == null) {
			log.error("该武将没有对应的精魄id，武将的heroId为{}", type.getHeroId());
			return false;
		}
		
		if (wuJiang.getNum() < star.getExp()) {
			log.error("精魄不足");
			error(session, code, "升星失败，精魄不足");
			return false;
		}
		jm.point = jm.point + star.getJingmaidian();
		wuJiang.setNum(wuJiang.getNum() - star.getExp());
		log.info("玩家id为{}的玩家获得了{}经脉点", wuJiang.getOwnerId(), star.getJingmaidian());
		return true;
	}

	public HeroDate.Builder getHeroDataByWuJiang(WuJiang ret, WuJiang wuJiang) {
		HeroDate.Builder hero  = HeroDate.newBuilder();
		
		hero.setHeroGrowId(ret.getHeroGrowId());
		hero.setGongji(ret.getAttack());
		hero.setFangyu(ret.getDefense());
		hero.setShengming(ret.getHp());
		hero.setTongshuai(ret.getTongshuai());
		hero.setWuyi(ret.getWuyi());
		hero.setZhimou(ret.getZhimou());
		hero.setExp(wuJiang.getExp());
		hero.setNum(wuJiang.getNum());
		hero.setLoyal(100);
		hero.setActivated(wuJiang.isCombine());
		
		return hero;
	}

	public WuJiang getWuJiangByHeroId(long junZhuId, int heroId) {
//		WuJiang w = HibernateUtil.find(WuJiang.class, "where ownerId = " + accId + " and heroId = " + heroId, false);
//		if(w != null){
//			w = HibernateUtil.find(WuJiang.class, w.dbId);
//		}
//		return w;
		List<WuJiang> list = getWuJiangList(junZhuId);
		for(WuJiang w :list){
			if(w.heroId == heroId){
				return w;
			}
		}
		return null;
	}
	public WuJiang getWuJiangByHeroGrowId(long junZhuId, int heroGrowId) {
		List<WuJiang> list = getWuJiangList(junZhuId);
		for(WuJiang w :list){
			if(w.heroGrowId == heroGrowId){
				return w;
			}
		}
		return null;
	}

	/**
	 * 错误处理。（完成）
	 * @param session
	 * @param code
	 * @param msg
	 */
	public void error(IoSession session,int code,String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(code);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
	
	/**
	 * 请求所有武将信息
	 * @param session
	 * @return
	 */
	public void sendWuJiangList(IoSession session) {
		List<WuJiang> myWuJiangs = getWuJiangList(session);

		
		HeroInfoResp.Builder resp = HeroInfoResp.newBuilder();
		HeroDate.Builder hero = HeroDate.newBuilder();
		
		for(WuJiang wuJiang : myWuJiangs){
			hero.setHeroGrowId(wuJiang.getHeroGrowId());
			hero.setGongji(wuJiang.getAttack());
			hero.setFangyu(wuJiang.getDefense());
			hero.setShengming(wuJiang.getHp());
			hero.setTongshuai(wuJiang.getTongshuai());
			hero.setWuyi(wuJiang.getWuyi());
			hero.setZhimou(wuJiang.getZhimou());
			hero.setExp(wuJiang.getExp());
			hero.setNum(wuJiang.getNum());
			hero.setLoyal(100);
			hero.setActivated(wuJiang.isCombine());
			hero.setZhanLi(wuJiang.zhanLi);
			resp.addHeros(hero);
		}
		long junZhuId = getJunZhuId(session);
		JmBean jmBean = HibernateUtil.find(JmBean.class, junZhuId);
		if(jmBean == null){
			resp.setJingmai(0);
		}else{
			resp.setJingmai(jmBean.point);
		}
		session.write(resp.build());
	}
	
	public void calcZhanLi(WuJiang wuJiang) {
		if(wuJiang.combine==false){
			return;
		}
		double m = CanShu.ZHANLI_M;
		double c = CanShu.ZHANLI_C;
		double r = CanShu.ZHANLI_R;
		int 攻击 = wuJiang.attack;
		int 防御 = wuJiang.defense;
		int 生命 = wuJiang.hp;
		int 智谋 = wuJiang.zhimou;
		int 神佑 = wuJiang.tongshuai;
		int 武艺 = wuJiang.wuyi;
		double  技能倍数 = CanShu.WUJIANG_JINENG_BEISHU;
		double  技能权重 = CanShu.WUJIANG_JINENG_QUANZHONG;
		double 普攻权重 = CanShu.WUJIANG_PUGONG_QUANZHONG;
		double 普攻倍数 = CanShu.WUJIANG_PUGONG_BEISHU;
		double 暴击率 = 0;//2014年9月23日17:45:03，峻宇说预留，现在按0算。
		double 元素倍数 = 0;//实际跟品质有关。
		//【个体战力】=m*（攻击+防御+生命/c）
		//	*（【普攻权重】*【普攻倍数】
				// *（1+暴击率*0.5）
				// *（1+武艺/2000+元素倍数*（1+神佑/2000））
		//		+【技能权重】*【技能倍数】*（1+智谋/2000））
		double 个体战力=m*(攻击+防御+生命/c);
		double base = (普攻权重 * 普攻倍数 * (1 + 暴击率 * 0.5)
				* (1 + 武艺 / 2000 + 元素倍数 * (1 + 神佑 / 2000)) + 技能权重 * 技能倍数
				* (1 + 智谋 / 2000));
		double result = 个体战力 * Math.pow(base, 1/r);
		wuJiang.zhanLi = (int)Math.round(result);
	}

	/**
	 * 获取君主拥有的武将列表
	 * @param session
	 * @return
	 */
	public List<WuJiang> getWuJiangList(IoSession session) {
		long junZhuId = getJunZhuId(session);
		return getWuJiangList(junZhuId);
	}
	public List<WuJiang> getWuJiangList(long junZhuId) {
		long start = junZhuId * spaceFactor;
		long end = start + maxGridCount - 1;
		String bagCntKey = "WuJiangCnt#"+junZhuId;
		Object mcO = MemcachedCRUD.getMemCachedClient().get(bagCntKey);
		List<WuJiang> wuJiangs = null;
		if(mcO == null){
			wuJiangs = HibernateUtil.list(WuJiang.class, "where dbId>=" + start + " and dbId<="+end);
			for(WuJiang wuJiang : wuJiangs){
				if(wuJiang.zhanLi == 0){
					calcZhanLi(wuJiang);
					HibernateUtil.save(wuJiang);
				}
				MC.add(wuJiang, wuJiang.dbId);
			}
			MC.addKeyValue(bagCntKey, Integer.valueOf(wuJiangs.size()));
		}else{
			Integer cnt = Integer.valueOf(mcO.toString());
			wuJiangs = new ArrayList<WuJiang>(cnt);
			if(cnt>0){
				String[] keys = new String[cnt];
				for(int i=0; i<cnt; i++){
					keys[i] = "WuJiang#"+(start+i);
				}
				Object[] mcArr = MemcachedCRUD.getMemCachedClient().getMultiArray(keys);
				for(Object o : mcArr){
					if(o==null){
						continue;
					}
					wuJiangs.add((WuJiang)o);
				}
			}
		}
		fillWuJiangList(wuJiangs,junZhuId);
		return wuJiangs;
	}

	public void fillWuJiangList(List<WuJiang> wuJiangs, long junZhuId) {
		WjKeJi keJi = HibernateUtil.find(WjKeJi.class, junZhuId);
		if (keJi == null) {
			keJi = WuJiangKeJiMgr.inst.createDefaultBean(junZhuId);
		}
		for(WuJiang w : wuJiangs){
			fillWuJiangAttInfo(w, junZhuId, keJi);
			if(w.zhanLi==0 && w.combine){
				calcZhanLi(w);
				HibernateUtil.save(w);
			}
		}
	}
	
	/**
	 * 武将激活
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void activateHero(int id, IoSession session, Builder builder) {
		HeroActivatReq.Builder activateReq = (qxmobile.protobuf.WuJiangProtos.HeroActivatReq.Builder) builder;
		int heroGrowId = activateReq.getHeroGrowId();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		WuJiang wuJiang = getWuJiangByHeroGrowId(junZhuId, heroGrowId);
		if(wuJiang == null){
			log.error("找不到对应的武将，HeroGrowId:{}", heroGrowId);
			error(session, id, "找不到对应的武将，HeroGrowId:" + heroGrowId);
			return;
		}
		JingPo jingPo = null;
		for(Map.Entry<Integer, JingPo> entry : id2JingPo.entrySet()){
			JingPo jp = entry.getValue();
			if(jp.getHeroId() == wuJiang.getHeroId()){
				jingPo = jp;
				break;
			}
		}
		
		if(jingPo == null){
			log.error("找不到对应的精魄配置，HeroId:{}", wuJiang.getHeroId());
			error(session, id, "找不到对应的精魄配置，HeroId:" + wuJiang.getHeroId());
			return;
		}
		int jingPoNum = wuJiang.getNum();
		if(jingPoNum < jingPo.getHechengNum()){
			log.error("所需的精魄数量不足");
			error(session, id, "所需的精魄数量不足");
			return;
		}
		
		wuJiang.setNum(jingPoNum - jingPo.getHechengNum());
		wuJiang.setCombine(true);
		HibernateUtil.save(wuJiang);
		log.info("junzhuid:{},激活武将id：{}",wuJiang.getOwnerId(),wuJiang.getHeroId());
		
		HeroActivatResp.Builder activatResp = HeroActivatResp.newBuilder();
		HeroDate.Builder hero = HeroDate.newBuilder();
		hero.setHeroGrowId(wuJiang.getHeroGrowId());
		hero.setGongji(wuJiang.getAttack());
		hero.setFangyu(wuJiang.getDefense());
		hero.setShengming(wuJiang.getHp());
		hero.setTongshuai(wuJiang.getTongshuai());
		hero.setWuyi(wuJiang.getWuyi());
		hero.setZhimou(wuJiang.getZhimou());
		hero.setExp(wuJiang.getExp());
		hero.setNum(wuJiang.getNum());
		hero.setLoyal(100);
		hero.setActivated(wuJiang.isCombine());
		activatResp.setHero(hero);
		session.write(activatResp.build());
		int heroId = id2HeroGrow.get(heroGrowId).getHeroId();
		addAcheJindu(junZhuId, id2Hero.get(heroId).getQuality());
	}
	
	public void addAcheJindu(long junzhuId, int quality) {
		int acheType = -1;//白绿蓝紫橙,12345
		switch (quality) {
		case 1:
			acheType = AchievementConstants.type_wujiang_white_nums;
			break;
		case 2:
			acheType = AchievementConstants.type_wujiang_green_nums;
			break;
		case 3:
			acheType = AchievementConstants.type_wujiang_blue_nums;
			break;
		case 4:
			acheType = AchievementConstants.type_wujiang_purple_nums;
			break;
		case 5:
			acheType = AchievementConstants.type_wujiang_orange_nums;
			break;
		default:
			break;
		}
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS,
				new AchievementCondition(junzhuId, acheType, 1));
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS,
				new AchievementCondition(junzhuId, AchievementConstants.type_wujiang_total_nums, 1));
	}
	
	/**
	 * 添加金色精魄
	 * 
	 * @param junZhuId
	 * @param session
	 * @param number
	 */
	public void addGoldenJingPo(int junZhuId, IoSession session, int number) {
		WjKeJi wjKeJi = HibernateUtil.find(WjKeJi.class, junZhuId);
		if(wjKeJi == null){
			wjKeJi = WuJiangKeJiMgr.inst.createDefaultBean(junZhuId);
			MC.add(wjKeJi, junZhuId);
			HibernateUtil.insert(wjKeJi);
		}
		int goldenJingPo = wjKeJi.getGoldenJingPo();
		wjKeJi.setGoldenJingPo(goldenJingPo + number);
		HibernateUtil.save(wjKeJi);
		log.info("君主:{},获得金色精魄数量:{}",junZhuId, number);
		
		JingPoRefreshResp.Builder resp = JingPoRefreshResp.newBuilder();
		resp.setGoldenJingPo(goldenJingPo);
		session.write(resp.build());
	}
	
	/**
	 * 获取科技成长附加成长值
	 * @param quality		武将品质
	 * @param level			武将当前科技等级
	 * @return
	 */
	public float getAttachChengzhang(int quality, int level){
		float attachChengzhang = 0;
		List<KejiChengzhang> kjczList = qualityToKejiChengzhang.get(quality);
		for(KejiChengzhang kjcz : kjczList){
			if(level <= kjcz.getLevel()){
				attachChengzhang = kjcz.getChengzhang();
			}
		}
		return attachChengzhang;
	}

	public void sendBuZhenHeroList(int id, IoSession session, Builder builder) {
		BuZhenHeroList.Builder ret = BuZhenHeroList.newBuilder();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("junzhu not found");
			return;
		}
		List<WuJiang> wuJiangList = HibernateUtil.list(WuJiang.class, 
				" where owner_id = " + junZhuId + " and combine = 1");
		for(WuJiang w : wuJiangList){
			HeroProtoType proto = id2Hero.get(w.getHeroId());
			BuZhenHero.Builder h = BuZhenHero.newBuilder();
			h.setProtoId(w.getHeroId());
			h.setGrowId(w.getHeroGrowId());
			h.setName(HeroService.getNameById(proto.getName()));
			h.setPinZhi(proto.getPinZhi());
			h.setZhiYe(proto.getHeroType());
			HeroGrow grow = id2HeroGrow.get(w.getHeroGrowId());
			h.setStar(grow.getStar());
			ret.addList(h);
		}
		session.write(ret.build());
	}
	
	public boolean isWuQi(int buWeiId) {
		return WEAPON_HEAVY == buWeiId
				|| WEAPON_LIGHT == buWeiId
				|| WEAPON_RANGED == buWeiId;
	}
}
