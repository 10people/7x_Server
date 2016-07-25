package com.qx.award;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MiBaoNew;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.PveTemp;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.fuwen.FuwenMgr;
import com.qx.hero.HeroMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.TalentMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MiBaoDao;
import com.qx.mibao.MibaoMgr;
import com.qx.mibao.v2.MiBaoV2Mgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.PveMgr;
import com.qx.ranking.RankingGongJinMgr;
import com.qx.task.GameTaskMgr;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipMgr;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

public class AwardMgr {
	public static AwardMgr inst;
	public static boolean cheatHit = false;
	public static Logger log = LoggerFactory.getLogger(AwardMgr.class.getSimpleName());
	public static Random rnd = new Random();
	public Map<Integer, List<AwardTemp>> awardId2Award;
	public static int ITEM_TONGBI_ID = 900001; 			//铜币
	public static int ITEM_EXP_ID = 900006;				//经验
	public static int ITEM_HU_FU_ID = 910000;			//虎符itemId
	public static int ITEM_WEI_WANG = 900011;			// 威望
	public static int item_gong_jin = 900028; 			//贡金
	public static int item_huang_ye_bi = 900026;		//荒野币
	public static int vip_exp = 900029; 				// vip经验
	public static int item_yuan_bao = 900002;  			// 元宝
	public static int ITEM_ALLIANCE_EXP = 900032;  		// 联盟经验
	public static int ITEM_TILI_ID = 900003;  			// 体力
	public static int ITEM_GONG_XUN = 900027;			// 功勋
	public static int ITEM_LIAN_MENG_GONGXIAN = 900015;	// 联盟贡献
	
	/** 物品奖励 */
	public static final int TYPE_ITEM = 0;
	/** 装备奖励 */
	public static final int TYPE_ZHUANG_BEI = 2;
	/** 玉诀奖励 */
	public static final int TYPE_YU_JUE = 3;
	/** 秘宝奖励 */
	public static final int TYPE_MI_BAO = 4;
	/** 秘宝碎片奖励 */
	public static final int TYPE_MOBAI_SUIPIAN = 5;
	/** 装备进阶材料奖励 */
	public static final int TYPE_JIN_JIE = 6;
	/** 武将奖励 */
	public static final int TYPE_WU_JIANG = -9990;
	/** 精魄奖励 */
	public static final int TYPE_JING_PO = -9991;
	/** 装备强化材料奖励 */
	public static final int TYPE_QIANG_HUA = 9;
	
	public static final int type_fuWen = 8; // 符文
	public static final int type_baoShi = 7; // 宝石
	
	public static final int TYPE_NEW_MI_BAO = 22;
	public static final int TYPE_NEW_MOBAI_SUIPIAN = 23;
	
	public AwardMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<AwardTemp> list = TempletService.listAll("AwardTemp");
		Map<Integer, List<AwardTemp>> awardId2Award = new HashMap<Integer, List<AwardTemp>>();
		for (Object o : list) {
			AwardTemp award = (AwardTemp) o;
			List<AwardTemp> tmp = awardId2Award.get(award.awardId);
			if (tmp == null) {
				tmp = new ArrayList<AwardTemp>();
				awardId2Award.put(award.awardId, tmp);
			}
			tmp.add(award);
		}
		this.awardId2Award = awardId2Award;
	}

	// TOSEE
	public AwardTemp calcAwardTemp(int awardId) {
		List<AwardTemp> list = awardId2Award.get(awardId);
		if (list == null) {
			log.error("conf list is null for award id {}", awardId);
			return null;
		}
		int seed = rnd.nextInt(1000000);
		int sum = 0;
		AwardTemp hit = null;
		for (AwardTemp conf : list) {
			sum += conf.weight;
			if (seed < sum) {
				hit = conf;
				break;
			}
		}
		if (hit == null) {
			log.info("nothing hit, award id {}, seed {}", awardId, seed);
			return null;
		}
		return hit;
	}

	/**
	 * 战斗结束后领取奖励
	 * 
	 */
	public void getAward(Integer guanQiaId, Boolean chuanQiMark, boolean pass,
			IoSession session, List<AwardTemp> getNpcAwardList, JunZhu jz) {
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		BattleResult.Builder ret = BattleResult.newBuilder();
		if (!pass || guanQiaId == null) {
			ret.setExp(0);
			ret.setMoney(0);
			session.write(ret.build());
			return;
		}
		PveTemp conf = null;
		if (chuanQiMark != null && chuanQiMark) {
			conf = PveMgr.inst.legendId2Pve.get(guanQiaId);
		} else {
			conf = PveMgr.inst.id2Pve.get(guanQiaId);
		}
		if (conf == null) {
			sendError(session, "没有找到关卡" + guanQiaId);
			log.error("没有找到关卡:{}", guanQiaId);
			return;
		}
		//JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("没有找到君主");
			return;
		}

		jz.tongBi += conf.money;
		JunZhuMgr.inst.addExp(jz, conf.exp);
		log.info("{}打完关卡:{} 传奇:{} 得到金钱{} 经验{}", jz.name, guanQiaId,
				chuanQiMark, conf.money, conf.exp);
		//session.setAttribute(SessionAttKey.firstQuanQiaId,conf.id);
		if (session.getAttribute(SessionAttKey.firstQuanQiaId) == null) {
			log.info("不是首次挑战该关卡，关卡id:{} 传奇:{}，不能获得首次奖励", conf.id,
					chuanQiMark);
		} else if (conf.id != (Integer) session
				.getAttribute(SessionAttKey.firstQuanQiaId)) {
			log.info("打的id是{}，首打id是{} 传奇:{}，无首次奖励", conf.id,
					session.getAttribute(SessionAttKey.firstQuanQiaId),
					chuanQiMark);
		} else {
			if (conf.firstAwardId.equals("")) {
				log.info("该关卡没有配置首次奖励，关卡id:{} 传奇:{}", conf.id, chuanQiMark);
			} else {
				List<Integer> fistHitAwardIdList = getHitAwardId(conf.firstAwardConf, 0);
				for (Integer awardId : fistHitAwardIdList) {
					AwardTemp calcV = calcAwardTemp(awardId);
					if(calcV != null) {
						AwardMgr.inst.battleAwardCounting(getAwardList, calcV);
						log.info("给予 {}关卡首次奖励type {} id {}, 关卡id:{} 传奇:{} ",
								jz.name, calcV.itemType, calcV.itemId,
								conf.id, chuanQiMark);
					}
				}
			}
		}
		session.setAttribute(SessionAttKey.firstQuanQiaId, null);
		// 关卡奖励
		List<Integer> hitAwardIdList = getHitAwardId(conf.awardConf, jz.id);
		for (Integer awardId : hitAwardIdList) {
			AwardTemp calcV = calcAwardTemp(awardId);
			if(calcV != null) {
				AwardMgr.inst.battleAwardCounting(getAwardList, calcV);
			}
		}
		// npc掉落的物品
		AwardMgr.inst.battleAwardCounting(getAwardList, getNpcAwardList);
		
		int getTongBiTotal = conf.money;
		int getExpTotal = conf.exp;
		for (AwardTemp calcV : getAwardList) {
			if(calcV != null) {
				if (calcV.itemId == AwardMgr.ITEM_TONGBI_ID) {// 铜币
					getTongBiTotal += calcV.itemNum;
				} else if (calcV.itemId == AwardMgr.ITEM_EXP_ID) {// 经验
					getExpTotal += calcV.itemNum;
				} else {
					fillBattleAwardInfo(ret, calcV);
				}
			}
		}
		// 发送奖励
		for(AwardTemp award : getAwardList) {
			giveReward(session, award, jz, false,false);
		}
		ret.setMoney(getTongBiTotal);
		ret.setExp(getExpTotal);
		session.write(ret.build());
		//BagMgr.inst.sendBagInfo(0, session, null);
		//JunZhuMgr.inst.sendMainInfo(session,jz);
	}

	/**
	 * 获得命中奖励id列表，直接调用此方法，需要在外面保存dropRateMap里的DropRateBean
	 * 
	 * @param awardArray
	 *            奖励int数组，index=偶数表示awardId，index=之前偶数+1表示获得这个奖励的几率
	 * @param dropRateMap 
	 * @return
	 */
	public List<Integer> getHitAwardId(int[] awardArray, long jzId, Map<Integer, DropRateBean> dropRateMap) {
		List<Integer> awardIdList = new ArrayList<Integer>();
		int len = awardArray.length;
		for (int i = 0; i < len; i += 2) {
			int awardId = awardArray[i];
			final int hitNum = awardArray[i + 1];
			int fixRate = hitNum;
			DropRateBean bean = null;
			if(jzId>0){//继续掉率保底
				bean = dropRateMap.get(awardId);
				if(bean == null){
					bean = new DropRateBean();
					bean.jzId = jzId;
					bean.groupId = awardId;
					bean.dbOp = 'I';//插入
					dropRateMap.put(awardId, bean);//加入没有表需要往数据库的插入一条记录
				}else{
					if(bean.dbOp != 'I') {//为了确保有的确实需要插入记录，在第二次获取时把状态改变了
						bean.dbOp = 'U';//更新
					}
					fixRate += bean.fixScore;//累加之前积累的概率
				}
			}
			int ran = rnd.nextInt(100);// 总几率是100
			log.info("jzId {} group {} rnd get {}, need {}, base {}", jzId, awardId, ran, fixRate, hitNum);
			if (ran < fixRate || cheatHit) {
				cheatHit = false;
				awardIdList.add(awardId);
				if(bean != null){//掉了，概率降低100
					bean.win += 1;
					bean.fixScore += hitNum;
					bean.fixScore -= 100;
//					log.info("fix {}->{} v {} to {}",jzId,awardId, -100, bean.fixScore);
				}
			}else if(bean != null){
				bean.lose += 1;//不掉，概率累加
				bean.fixScore += hitNum;
//				log.info("fix {}->{} v {} to {}",jzId,awardId, hitNum, bean.fixScore);
			}
		}
		return awardIdList;
	}
	
	public List<Integer> getHitAwardId(int[] awardArray, long jzId) {
		List<Integer> awardIdList = Collections.EMPTY_LIST;
		Map<Integer, DropRateBean> dropRateMap = DropRateDao.inst.getMap(jzId);
		awardIdList =  getHitAwardId(awardArray, jzId, dropRateMap);
		for(DropRateBean bean : dropRateMap.values()) {
			if(bean.dbOp == 'I'){
				HibernateUtil.insert(bean);
				bean.dbOp = 'N';
			}else if(bean.dbOp == 'U'){
				HibernateUtil.update(bean);
				bean.dbOp = 'N';
			}
		}
		return awardIdList;
	}
	
	public Map<Integer, DropRateBean> getDropRateBeanMap(int[] awardConf, long jzId) {
		Set<String> awardIdSet = new HashSet<>();
		int len = awardConf.length;
		for (int i = 0; i < len; i += 2) {
			int awardId = awardConf[i];
			awardIdSet.add(awardId+"");
		}
		String awardIds = awardIdSet.stream().collect(Collectors.joining(","));
		String hqlWhere = " WHERE jzId="+jzId +" and groupId IN (" + awardIds +")";
		List<DropRateBean> list =  HibernateUtil.list(DropRateBean.class, hqlWhere);
		Map<Integer, DropRateBean> dropRateMap = list.stream()//
				.collect(Collectors.toMap(item -> item.groupId, (item) -> item));
		return dropRateMap;
	}

	/**
	 * 获得命中奖励列表，如果没有命中的奖励 list.size == 0
	 * 
	 * @param awardStr
	 *            格式化奖励字符串（awardId=gaiLv,awardId=gaiLv）
	 * @param awardsSeparator
	 *            奖励之间的分隔符号
	 * @param awardGailvSeparator
	 *            奖励与概率之间的分割符号
	 * @return 命中的奖励列表
	 */
	public List<AwardTemp> getHitAwardList(String awardStr,
			String awardsSeparator, String awardGailvSeparator) {
		List<AwardTemp> list = new ArrayList<AwardTemp>();
		if (awardStr != null && !awardStr.equals("")) {
			String[] awardList = awardStr.split(awardsSeparator);
			Random rnd = new Random();
			for (String award : awardList) {
				String[] awardInfo = award.split(awardGailvSeparator);
				int awardId = Integer.parseInt(awardInfo[0]);
				if (awardId <= 0) { // 表示没有奖励
					continue;
				}
				int roll = Integer.parseInt(awardInfo[1]);
				int ran = rnd.nextInt(100);
				if (ran < roll) {
					AwardTemp calcV = AwardMgr.inst.calcAwardTemp(awardId);
					if (calcV == null) {
						log.error("没有命中奖励:{}", award);
					} else {
						list.add(calcV);
//						log.info("命中奖励 awardId:{}, 添加", awardId);
					}
				}
			}
		}
		return list;
	}

	/*public void makeAwardInfo(IoSession session, JunZhu jz,
			BattleResult.Builder ret, AwardTemp calcV) {
		if (calcV != null) {
			giveReward(session, calcV, jz);
			List<AwardItem.Builder> haveAwardList = ret
					.getAwardItemsBuilderList();
			for (AwardItem.Builder item : haveAwardList) {
				if (item.getAwardItemType() == calcV.itemType
						&& item.awardId == calcV.itemId) {
					item.setAwardNum(item.getAwardNum() + calcV.itemNum);
					item.build();
					return;
				}
			}
			AwardItem.Builder award = AwardItem.newBuilder();
			log.info("hit award id {}", calcV.id);
			award.setAwardId(calcV.itemId);
			award.setAwardNum(calcV.itemNum);
			award.setAwardItemType(calcV.itemType);
			int iconId = getItemIconid(calcV.itemType, calcV.itemId);
			award.setAwardIconId(iconId);
			ret.addAwardItems(award.build());
		}
	}*/
	
	/**
	 * 填充战斗结束得到的奖励信息
	 * @param ret
	 * @param calcV
	 */
	public void fillBattleAwardInfo(BattleResult.Builder ret, AwardTemp calcV) {
		if (calcV != null) {
			// 如果有重复的奖励，则直接增加奖励的数量
			List<AwardItem.Builder> haveAwardList = ret.getAwardItemsBuilderList();
			for (AwardItem.Builder item : haveAwardList) {
				if (item.getAwardItemType() == calcV.itemType
						&& item.getAwardId() == calcV.itemId) {
					item.setAwardNum(item.getAwardNum() + calcV.itemNum);
					item.build();
					return;
				}
			}
			AwardItem.Builder award = AwardItem.newBuilder();
			log.info("hit award id {}", calcV.id);
			award.setAwardId(calcV.itemId);
			award.setAwardNum(calcV.itemNum);
			award.setAwardItemType(calcV.itemType);
			int iconId = getItemIconid(calcV.itemType, calcV.itemId);
			award.setAwardIconId(iconId);
			ret.addAwardItems(award.build());
		}
	}
	
	/**
	 * 战斗结果奖励计数，相同的奖励直接增加数量
	 * @param getAwardList
	 * @param addAward
	 */
	public void battleAwardCounting(List<AwardTemp> getAwardList, AwardTemp addAward) {
		if (addAward != null) {
			// 如果有重复的奖励，则直接增加奖励的数量
			for (AwardTemp getAward : getAwardList) {
				if (getAward.itemType == addAward.itemType 
						&&  getAward.itemId == addAward.itemId) {
					getAward.itemNum = getAward.itemNum + addAward.itemNum;
					return;
				}
			}
			// 特别提示：添加获得的奖励信息，必须创建一个新的对象，否则会出现奖励数量不断增加的bug
			AwardTemp cloneAward = addAward.clone();
			getAwardList.add(cloneAward);
		}
	}
	
	/**
	 * 战斗结果奖励计数，相同的奖励直接增加数量
	 * @param getAwardList
	 * @param addAwardList
	 */
	public void battleAwardCounting(List<AwardTemp> getAwardList, List<AwardTemp> addAwardList) {
		if(addAwardList != null) {
			for(AwardTemp addAward : addAwardList) {
				battleAwardCounting(getAwardList, addAward);
			}
		}
	}

	public int getItemIconid(int itemType, int itemId) {
		int iconId = 0;
		switch (itemType) {
		case TYPE_WU_JIANG:
			log.info("武将protoId {}", itemId);
			HeroProtoType proto = HeroMgr.tempId2HeroProto.get(itemId);
			if (proto == null) {
				log.error("武将没有找到{}", itemId);
				break;
			}
			iconId = proto.icon;
			break;
		case 21://玉玦
		case TYPE_ITEM:
		case TYPE_ZHUANG_BEI:// 装备 装进背包
		case TYPE_YU_JUE:// 玉玦
		case TYPE_JIN_JIE:// 进阶材料 装进背包
		case TYPE_QIANG_HUA:// 强化材料
		case type_fuWen:  // 符文
		case type_baoShi:{ // 宝石
			BaseItem t = TempletService.itemMap.get(itemId);
			if(t==null){
				log.error("物品没有找到,id {}", itemId);
				break;
			}
			iconId = t.getIconId();
			break;
		}
		case TYPE_MI_BAO:{ // 秘宝
			MiBao mb = MibaoMgr.mibaoMap.get(itemId);
			if(mb == null){
				log.error("秘宝没有找到,id {}", itemId);
				break;
			}
			iconId = mb.icon;
			break;
		}
		case TYPE_MOBAI_SUIPIAN:{ // 碎片
			MibaoSuiPian sp = MibaoMgr.inst.mibaoSuipianMap_2.get(itemId);
			if(sp == null){
				log.error("碎片没有找到,id {}", itemId);
				break;
			}
			iconId = sp.icon;
			break;
		}
		case TYPE_NEW_MOBAI_SUIPIAN:
			Map<Integer, MiBaoNew> mbNewMap = MiBaoV2Mgr.inst.confMap;
			if(mbNewMap == null || mbNewMap.size() == 0) {
				break;
			}
			Collection<MiBaoNew> mbNewList = mbNewMap.values();
			for(MiBaoNew mbn : mbNewList) {
				if(mbn.suipianId == itemId) {
					iconId = mbn.icon;
					break;
				}
			}
			break;
		default:
			log.error("A未知奖励类型 {}, awardId{}", itemType, itemId);
			break;
		}
		if (iconId == 0) {
			log.error("获取物品icon错误，iconId=0，itemType={},itemId={}", itemType, itemId);
		}
		return iconId;
	}

	public boolean giveReward(IoSession session, AwardTemp a, JunZhu jz) {
		return giveReward(session, a, jz, true);
	}
	public boolean giveReward(IoSession session,String jiangLi, JunZhu jz) {
		log.info("给予{}发放奖励--{}开始", jz.id,jiangLi);
		if(jiangLi==null||"".equals(jiangLi)){
			return true;
		}
		String[] jiangliArray = jiangLi.split("#");
		try {
			for(String jiangli : jiangliArray) {
				String[] infos = jiangli.split(":");
				int type = Integer.parseInt(infos[0]);
				int itemId = Integer.parseInt(infos[1]);
				int count = Integer.parseInt(infos[2]);
				AwardTemp a = new AwardTemp();
				a.itemType = type;
				a.itemId = itemId;
				a.itemNum = count;
				AwardMgr.inst.giveReward(session, a, jz);
				log.info("给予{}奖励 type {} id {} cnt{}", jz.id,type,itemId,count);
			}
		} catch (Exception e) {
			log.info("给予{}发放奖励--异常{}",jz.id,e);
			return false;
		}
		log.info("给予{}发放奖励--{}结束", jz.id,jiangLi);
		return true;
	}
	
	public boolean giveReward(IoSession session, AwardTemp a, JunZhu jz,boolean sendMainInfo) {
		return giveReward(session, a, jz, sendMainInfo, true);
	}
	public boolean giveReward(IoSession session, AwardTemp a, JunZhu jz,boolean sendMainInfo, boolean sendBagInfo) {
		if (a == null || jz == null) {
			return false;
		}
		long junZhuId = jz.id;
		Bag<BagGrid> bag;
		switch (a.itemType) {
		case 21://玉玦
		case 11://小屋换卡材料
		case 12://小屋换卡材料
		case 13://小屋换卡材料
		case 14://小屋换卡材料
		case 15://小屋换卡材料
		case TYPE_ITEM:// 物品
			if (a.itemId == 900009) {// 卡包积分
				jz.cardJiFen += a.itemNum;
				HibernateUtil.save(jz);
				log.info("{} 获得卡包积分{},达到{}", jz.id, a.itemNum,
						jz.cardJiFen);
			} else if (a.itemId == 900002) {
				YuanBaoMgr.inst.diff(jz, a.itemNum, 0, 0,
						YBType.YB_GET_REWARD, "获得奖励");
				HibernateUtil.save(jz);
				log.info("{} 获得元宝{},达到{}", jz.id, a.itemNum, jz.yuanBao);
			} else if (a.itemId == 900001) {
				jz.tongBi += a.itemNum;
				HibernateUtil.save(jz);
				log.info("{} 获得铜币{},达到{}", jz.id, a.itemNum, jz.tongBi);
			} else if (a.itemId == 900003) {// 体力
				JunZhuMgr.inst.updateTiLi(jz, a.itemNum, "奖励");
				HibernateUtil.save(jz);
				log.info("{} 获得体力{},达到{}", jz.id, a.itemNum, jz.tiLi);
			} else if (a.itemId == 900006) { // 经验
				JunZhuMgr.inst.addExp(jz, a.itemNum);
				log.info("{} 获得经验{},达到{}", jz.id, a.itemNum, jz.exp);
			}else if (a.itemId == 900031) { // 押镖福利次数
				YaBiaoHuoDongMgr.inst.saveFuliTimes(jz, a.itemNum);
				log.info("{} 获得押镖福利次数{}", jz.id, a.itemNum);
			}else if (a.itemId == 900017  ) { // 2016年2月1日 建设值 
				AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(jz.id);
				if(alliance == null) {
					log.error("发放建设值奖励失败，未找到{}所在联盟", jz.id);
				}else{
					AllianceMgr.inst.changeAlianceBuild(alliance, a.itemNum);
					log.info("发放君主--{}建设值奖励--{}，所在联盟{}", jz.id, a.itemNum,alliance.id);
					AllianceMgr.inst.sendAllianceInfo(jz, session, null, alliance);
				}
			} else if (a.itemId == ITEM_LIAN_MENG_GONGXIAN) {// 联盟贡献值
				AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
				if (member != null) {
					member.gongXian += a.itemNum;
					HibernateUtil.save(member);
					log.info("{} 获得联盟贡献值{},达到{}", jz.id, a.itemNum,
							member.gongXian);
				} else {
					member = new AlliancePlayer();
					AllianceMgr.inst.initAlliancePlayerInfo(junZhuId, -1, member, 0);
					member.gongXian = a.itemNum;
					HibernateUtil.insert(member);
					log.error("{} 无AlliancePlayer数据， 新建， 并获得联盟贡献：{} ", jz.id, member.gongXian);
				}
				AllianceMgr.inst.changeGongXianRecord(member, a.itemNum);
				AllianceMgr.inst.sendAllianceInfo(jz, session, member, null);
				break;
			} else if (a.itemId == ITEM_HU_FU_ID){
				AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
				if (member == null || member.lianMengId <= 0) {
					log.error("发放奖励失败，君主:{}没有联盟，不能给予联盟虎符奖励", jz.id);
					break;
				}
				AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean( member.lianMengId);
				if(alliance == null) {
					log.error("发放奖励失败，找不到联盟，id:{},不能给予联盟虎符奖励", member.lianMengId);
					break;
				}
				AllianceMgr.inst.changeAlianceHufu(alliance, a.itemNum);
			} else if (a.itemId == 900018) {// 武艺精气
				int all = TalentMgr.instance.addWuYiJingQi(session,jz,
						a.itemNum);
				log.info("{} 获得武艺精气{},达到{}", jz.id, a.itemNum, all);
//				TalentMgr.instance.sendTalentInfo(session);
//				if(sendMainInfo)JunZhuMgr.inst.sendMainInfo(session,jz,false);
				break;
			} else if (a.itemId == 900019) {// 体魄精气
				int all = TalentMgr.instance.addTiPoJingQi(session,jz,
						a.itemNum);
				log.info("{} 获得体魄精气{},达到{}", jz.id, a.itemNum, all);
//				TalentMgr.instance.sendTalentInfo(session);
//				if(sendMainInfo)JunZhuMgr.inst.sendMainInfo(session,jz,false);
				break;
			} else if(a.itemId == ITEM_WEI_WANG){ // 添加威望奖励
				int all = ShopMgr.inst.addMoney(ShopMgr.Money.weiWang,
						ShopMgr.baizhan_shop_type, jz.id, a.itemNum);
				log.info("君主：{}获取奖励，威望，获取数是：{}, 获取后拥有：{}", jz.id, a.itemNum, all);
				ShopMgr.inst.sendMainIfo(session, jz.id ,ShopMgr.Money.weiWang);
				EventMgr.addEvent(junZhuId,ED.CHANGE_WEIWANG, new Object[] { session, jz });
			} else if(a.itemId == ITEM_GONG_XUN){ // 添加功勋奖励
				int all = ShopMgr.inst.addMoney(ShopMgr.Money.gongXun,
						ShopMgr.lianmeng_battle_shop_type, jz.id, a.itemNum);
				log.info("君主：{}获取奖励，功勋，获取数是：{}, 获取后拥有：{}", jz.id, a.itemNum, all);
				ShopMgr.inst.sendMainIfo(session,jz.id,ShopMgr.Money.gongXun);
				AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(jz.id);
				if(alliance != null) {
					AllianceMgr.inst.sendAllianceInfo(jz, session, null, alliance);
				}
				EventMgr.addEvent(junZhuId,ED.CHANGE_GONGXUN, new Object[] { session, jz });
			} else if(a.itemId == item_gong_jin){ // 添加贡金
				// 20151127
				RankingGongJinMgr.inst.addGongJin(jz.id, a.itemNum);
//				ResourceGongJin re = HibernateUtil.find(ResourceGongJin.class, jz.id);
//				if(re == null){
//					re = GuoJiaMgr.inst.initjzGongJinInfo(jz.id, null, re);
//				}else{
//					GuoJiaMgr.inst.resetResourceGongJin(re);
//				}
//				GuoJiaMgr.inst.changeGongJin(re, a.itemNum);
//				// 判断能不能上缴
//				if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
//					GuoJiaMgr.inst.pushCanShangjiao(jz.id);
//				}
			}else if(a.itemId == item_huang_ye_bi){ // 玩家荒野币
				int all = ShopMgr.inst.addMoney(ShopMgr.Money.huangYeBi, 
						ShopMgr.huangYe_shop_type, jz.id, a.itemNum);
				log.info("君主：{}获取奖励，荒野币，获取数是：{}, 获取后拥有：{}", jz.id, a.itemNum, all);
				ShopMgr.inst.sendMainIfo(session,jz.id,ShopMgr.Money.huangYeBi);
			}else if(a.itemId == vip_exp){
				VipMgr.INSTANCE.addVipExp(jz, a.itemNum);
				log.info("君主：{}通过奖励获取vip经验：{}点，完成", jz.id, a.itemNum);
			}else if(a.itemId == 900032){ //联盟经验
				AllianceBean ac = AllianceMgr.inst.getAllianceByJunZid(jz.id);
				if(ac != null && a.itemNum != 0){
					AllianceMgr.inst.addAllianceExp(a.itemNum, ac);
					log.info("联盟：{}通过奖励获取联盟经验：{}点，完成", ac.id, a.itemNum);
					AllianceMgr.inst.sendAllianceInfo(jz, session, null, ac);
				}
			}else{
				BaseItem bi = TempletService.itemMap.get(a.itemId);
				if (bi != null) {// 如果属于背包物品
					bag = BagMgr.inst.loadBag(junZhuId);
					BagMgr.inst.addItem(session, bag, a.itemId, a.itemNum, -1,jz.level, "发奖:"+a.id);
					/*
					 * 判断是否集齐一套古卷
					 */
					isCollectASuitOfGuJuan(junZhuId, bag, bi.getType(), a.itemId);
					if(sendBagInfo){
						//BagMgr.inst.sendBagInfo(session, bag);
					}
					EventMgr.addEvent(jz.id,ED.GAIN_ITEM, new Object[]{bag.ownerId, a.itemId});
				}
//				log.error("未处理的类型 jzId:{} itemId:{} num:{}", junZhuId,
//						a.itemId, a.itemNum);
			}
			//发奖过程中不发君主信息
//			if(sendMainInfo)JunZhuMgr.inst.sendMainInfo(session, jz, false);
			break;
		case type_fuWen:
			Fuwen fuwen = FuwenMgr.inst.fuwenMap.get(a.itemId);
			if(fuwen == null) {
				log.error("找不到符文id:{}的配置",a.itemId);
				return false;
			}
			bag = BagMgr.inst.loadBag(junZhuId);
			BagMgr.inst.addItem(session, bag, a.itemId, a.itemNum, 0, jz.level, "发奖:"+a.id);
			//if(sendBagInfo)BagMgr.inst.sendBagInfo(0, session, null);
			break;
		case TYPE_ZHUANG_BEI:		// 装备 装进背包
		case TYPE_YU_JUE:			// 玉玦
		case TYPE_JIN_JIE:			// 进阶材料 装进背包
		case TYPE_QIANG_HUA:		// 强化材料
			bag = BagMgr.inst.loadBag(junZhuId);
			BagMgr.inst.addItem(session, bag, a.itemId, a.itemNum, -1, jz.level, "发奖:"+a.id);
			//if(sendBagInfo)BagMgr.inst.sendBagInfo(0, session, null);
			break;
		case type_baoShi:
			bag = BagMgr.inst.loadBag(junZhuId);
			BagMgr.inst.addItem(session, bag, a.itemId, a.itemNum, -1,jz.level, "发奖:"+a.id);
			//if(sendBagInfo)BagMgr.inst.sendBagInfo(0, session, null);
			break;
//		case TYPE_NEW_MI_BAO:
//			MiBaoV2Mgr.inst.addMiBao(jz,a);
//			break;
		case TYPE_NEW_MOBAI_SUIPIAN:
			MiBaoV2Mgr.inst.addMiBaoSuiPian(jz,a);
			MiBaoV2Mgr.inst.sendMiBaoInfo(0, session, null);
			break;
		case TYPE_MI_BAO: 			// 秘宝
		case TYPE_MOBAI_SUIPIAN:	// 秘宝碎片
			MiBao mibao = null;
			MibaoSuiPian suipian = null;
			boolean isCalcJzAttr = false;
			int tempId = 0;
			int initialStar = 0;
			int suiNu = 0;
			int realMiBaoId = 0;
			if (a.itemType == TYPE_MOBAI_SUIPIAN) {// 碎片
				suipian = MibaoMgr.inst.mibaoSuipianMap_2.get(a.itemId);
				if(suipian == null){
					log.error("秘宝MibaoSuiPian配置没有{}数据", a.itemId);
					return false;
				}
				tempId = suipian.tempId;
				initialStar = suipian.initialStar;
				suiNu = a.itemNum;
				realMiBaoId = 0;
				log.info("碎片数量{} 初始星级{}", suiNu, initialStar);
			}
			if (a.itemType == TYPE_MI_BAO) {// 秘宝
				mibao = MibaoMgr.mibaoMap.get(a.itemId);
				if (mibao == null) {
					log.error("秘宝{}数据错误", a.itemId);
					return false;
				}
				suipian = MibaoMgr.inst.mibaoSuipianMap.get(mibao.tempId);
				tempId = mibao.tempId;
				initialStar = mibao.initialStar;
				suiNu = suipian.fenjieNum;
				realMiBaoId = mibao.id;
			}
			boolean isGetMibao = false;
			MiBaoDB mibaoDB = MiBaoDao.inst.get(jz.id,tempId);
			if (mibaoDB == null) {
				mibaoDB = new MiBaoDB();
				// 改自增主键为指定
				// 2015年4月17日16:57:30int改为long
				long dbId = TableIDCreator.getTableID(MiBaoDB.class, 1L);
				mibaoDB.dbId = dbId;

				mibaoDB.ownerId = jz.id;
				MiBaoDao.inst.getMap(jz.id).put(tempId, mibaoDB);
				mibaoDB.tempId = tempId;
				mibaoDB.miBaoId = realMiBaoId;
				mibaoDB.star = initialStar;
				if (a.itemType == TYPE_MOBAI_SUIPIAN) {
					mibaoDB.level = 0;
					mibaoDB.suiPianNum = suiNu;
//					mibaoDB.setClear(false);
				}
				if (a.itemType == TYPE_MI_BAO) {
					mibaoDB.level = 1;
					mibaoDB.suiPianNum = 0;
//					mibaoDB.setClear(mibao, jz);
					log.info("玩家：{}获取了一个完整的秘宝 1 ， mibaodbid：{}", junZhuId, realMiBaoId);
					isGetMibao = true;
					isCalcJzAttr = true;
				}
				log.info("君主 {} 获得 tempId {} id {} 的秘宝", jz.id, tempId,
						realMiBaoId);
				HibernateUtil.save(mibaoDB);
//				 发送秘宝或者碎片更新的消息
//				MibaoMgr.inst.mibaoInfosRequest(PD.C_MIBAO_INFO_REQ, session);
			} else {
				if (mibaoDB.level > 0) { // 有完整秘宝
					mibaoDB.suiPianNum = mibaoDB.suiPianNum + suiNu;
					log.info("君主 :{}拥有秘宝:{}, 因此获得秘宝碎片 {} 个", jz.id,
							mibaoDB.miBaoId, suiNu);
				} else { // 没有完整秘宝
					if (a.itemType == TYPE_MOBAI_SUIPIAN) {
						mibaoDB.suiPianNum = (mibaoDB.suiPianNum + suiNu);
						log.info("君主 :{}获得秘宝:{},碎片 {} 个", jz.id,
								mibaoDB.miBaoId, suiNu);
					}
					if (a.itemType == TYPE_MI_BAO) {
						mibaoDB.miBaoId = realMiBaoId;
						mibaoDB.level = 1;
						mibaoDB.star = initialStar;
						log.info("玩家：{}获取了一个完整的秘宝 2 ， mibaodbid：{}", junZhuId, realMiBaoId);
						isGetMibao = true;
						isCalcJzAttr = true;
					}
				}
				HibernateUtil.save(mibaoDB);
			}
			// 发送秘宝或者碎片更新的消息
			MibaoMgr.inst.mibaoInfosRequest(PD.C_MIBAO_INFO_REQ, session);
			/*
			 *  判断秘宝相关任务是否有完成
			 */
			if (isGetMibao) {
//				String hql1 = "select count(1) from MiBaoDB where ownerId="+jz.id+" and level>=1";
//				int cnt = HibernateUtil.getCount(hql1);
				long cnt = MiBaoDao.inst.getMap(jz.id).values().stream()
						.filter(t->t.level>=1).count();
				EventMgr.addEvent(junZhuId,ED.GAIN_MIBAO, new Object[]{jz,session,cnt});
				// 写在后面，怕有异常导致后面代码无法执行
				doRenWuForMiBao(mibaoDB, jz, session);
				if(isCalcJzAttr){JunZhuMgr.inst.sendMainInfo(session, jz);} 
			}
			break;
			/*
		case TYPE_WU_JIANG:// 武将
		{
			HeroProtoType proto = HeroMgr.tempId2HeroProto.get(a.itemId);
			if (proto == null) {
				log.error("武将模板没有找到{}", a.itemId);
				return false;
			}
			int heroId = proto.getHeroId();
			HeroGrow hg = HeroMgr.inst.getHeroGrowByHeroIdAndStar(heroId, 0);
			if (hg == null) {
				log.error("找不到heroGrow数据，heroId:{},star:{}", heroId, 0);
				return false;
			}
			HeroMgr heroMgr = HeroMgr.getInstance();
			WuJiang wuJiang = heroMgr.getWuJiangByHeroId((int) jz.id, heroId);
			if (wuJiang != null) {
				// TODO a.getItemNum没有用到，是否会根据武将卡的数量来添加精魄数
				HeroGrow dbHeroGrow = HeroMgr.id2HeroGrow.get(wuJiang
						.getHeroGrowId());
				if (dbHeroGrow.getStar() == 5) {
					// HeroMgr.inst.convertGoldenJingPo(pid, session,
					// wuJiang.getHeroId());
				} else {
					int jingPoId = HeroMgr.id2Hero.get(wuJiang.getHeroId())
							.getJingpoId();
					JingPo jingPo = HeroMgr.id2JingPo.get(jingPoId);
					wuJiang.setNum(wuJiang.getNum() + jingPo.fenjieNum);
					HibernateUtil.save(wuJiang);
				}
				log.info("增加武将(id{})个数", heroId);
			} else {
				wuJiang = heroMgr.createWuJiangBean(hg, junZhuId);
				// heroMgr.fillWuJiangAttInfo(wuJiang,
				// (int)jz.id);//2014年10月13日10:43:55 给予的时候先不计算属性
				wuJiang.setCombine(true);
				HeroMgr.inst.addNewWuJiang(wuJiang, junZhuId);
				log.info("新获得武将{}", heroId);
			}
			HeroMgr.inst.sendWuJiangList(session);
			break;
		}
		case TYPE_JING_PO:// 精魄
			JingPo jingPo = HeroMgr.id2JingPo.get(a.itemId);
			int heroId = jingPo.getHeroId();
			WuJiang wuJiang = HeroMgr.inst.getWuJiangByHeroId(junZhuId, heroId);
			if (wuJiang == null) {
				HeroGrow heroGrow = HeroMgr.inst.getHeroGrowByHeroIdAndStar(
						heroId, 0);
				if (heroGrow == null) {
					log.error("找不到heroGrow数据，heroId:{},star:{}", heroId, 0);
					return false;
				}
				wuJiang = HeroMgr.inst.createWuJiangBean(heroGrow, junZhuId);
				HeroMgr.inst.addNewWuJiang(wuJiang, junZhuId);
			} else {
				wuJiang.setNum(wuJiang.getNum() + a.itemNum);
				HibernateUtil.save(wuJiang);
			}
			HeroMgr.inst.sendWuJiangList(session);
			break;
			*/
		default:
			log.error("未知奖励类型 {}, awardId{}", a.itemType, a.id);
			return false;
		}
		if( GameTaskMgr.usedItemID.contains(a.itemId) ){
			EventMgr.addEvent(junZhuId,ED.get_item_finish, new Object[] { junZhuId,
					a.itemId });
		}
		return true;
	}

	public void sendError(IoSession session, String msg) {
		if (session == null) {
			log.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());
		log.debug("sent keji info");
	}

	public StringBuilder reStructAwardStr(String awardStr) {
		if (awardStr == null)
			return null;
		StringBuilder sd = new StringBuilder();
		String[] s = awardStr.split("#");
		if (s != null) {
			int len = s.length;
			for (int i = 0; i < len; i++) {
				String[] award = s[i].split(":");
				if (award != null) {
					int type = Integer.parseInt(award[0]);
					int tempId = Integer.parseInt(award[1]);
					int number = Integer.parseInt(award[2]);
					if (type == 10) {
						AwardTemp a = AwardMgr.inst.calcAwardTemp(tempId);
						if(a != null) {
							type = a.itemType;
							tempId = a.itemId;
							number = a.itemNum;
						}
					}
					sd.append("#").append(type).append(":").append(tempId)
							.append(":").append(number);
				}
			}
			sd = new StringBuilder(sd.substring(1));
		}
		return sd;
	}
	
	public List<AwardTemp> parseAwardConf(String awardConf){
		return parseAwardConf(awardConf, "#", ":");
	}
	public List<AwardTemp> parseAwardConf(String awardConf, String awardsSeparator,
			String awardFormatSeparator) {
		List<AwardTemp> list = new ArrayList<AwardTemp>();
		try{
			if (awardConf != null && !awardConf.equals("")) {
				String[] awardList = awardConf.split(awardsSeparator);
				for (String award : awardList) {
					String[] awardInfo = award.split(awardFormatSeparator);
					int itemType = Integer.parseInt(awardInfo[0]);
					int itemId = Integer.parseInt(awardInfo[1]);
					int itemNum = Integer.parseInt(awardInfo[2]);
					AwardTemp calcV = new AwardTemp();
					calcV.itemType = itemType;
					calcV.itemId = itemId;
					calcV.itemNum = itemNum;
					list.add(calcV);
				}
			}
		} catch(Exception e) {
			log.error("奖励配置解析错误，awardConf:{}, 异常信息e:{}", awardConf, e);
		}
		return list;
	}
	
	/**
	 * 添加秘宝奖励的时候，添加秘宝相关任务事件
	 * @Title: doRenWuForMiBao 
	 * @Description:
	 * @param mibaoDB
	 * @param jz
	 */
	public void doRenWuForMiBao(MiBaoDB mibaoDB, JunZhu jz, IoSession session){
		if(mibaoDB.miBaoId > 0 && mibaoDB.level > 0){
			//  主线任务：星级: 要求解锁
			EventMgr.addEvent(jz.id,ED.mibao_shengStar_x, new Object[]{jz.id, mibaoDB.star});
			//  主线任务：等级 ：要求解锁秘宝
			EventMgr.addEvent(jz.id,ED.mibao_shengji_x, new Object[]{jz.id, mibaoDB.level});
//			 获取秘宝：获得一个秘宝
//			EventMgr.addEvent(ED.get_item_finish, new Object[] { jz.id, mibaoDB.getMiBaoId()});
			// 获取秘宝 ：秘宝合成: 不要求解锁 -朱诚 20150925
			EventMgr.addEvent(jz.id,ED.MIBAO_HECHENG, new Object[]{jz.id, mibaoDB.miBaoId});
			// 主线任务： 获取秘宝的个数： 不要求解锁 -朱诚 20150925
			int number = MibaoMgr.inst.getActivateMiBaoCount(jz.id);
			if (number > 0) {
				EventMgr.addEvent(jz.id,ED.get_x_mibao, new Object[] { jz.id, number});
				// 判断秘宝技能是是否可以开启。先写在这里，就不用eventMgr了
				MibaoMgr.inst.isCanMiBaoJiNengOpen(jz, session, number);
			}
			EventMgr.addEvent(jz.id,ED.miabao_x_star_n, new Object[]{jz.id});
		}
	}

	public void isCollectASuitOfGuJuan(long junZhuId, Bag<BagGrid> bag, int baseType, int itemId){
		if(baseType == BaseItem.TYPE_GU_JUAN_1
				|| baseType == BaseItem.TYPE_GU_JUAN_2
				|| baseType == BaseItem.TYPE_GU_JUAN_3
				|| baseType == BaseItem.TYPE_GU_JUAN_4
				|| baseType == BaseItem.TYPE_GU_JUAN_5){
			int count = BagMgr.inst.getItemTypeCountOfSameSuit(bag, baseType, itemId);
			log.info("君主：{}， 获得type={} 的古卷, 该类型古卷当前种类数是：{}", junZhuId, baseType, count);
			if(count >= BagMgr.a_suit_of_gu_juan){
				// 主线任务: 成功集齐古卷的任何一套即算完成任务  20190916
				EventMgr.addEvent(junZhuId,ED.have_total_guJuan , new Object[] {junZhuId});
				log.info("君主：{}背包中type:{}古卷类型大于等于5，主线任务完成处理被调用", junZhuId, baseType);
			}
		}
	}
}
