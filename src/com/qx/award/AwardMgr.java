package com.qx.award;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattlePveResult.AwardItem;
import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.PveTemp;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.battle.PveMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.guojia.GuoJiaMgr;
import com.qx.guojia.ResourceGongJin;
import com.qx.hero.HeroMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.TalentMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.util.TableIDCreator;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class AwardMgr {
	public static AwardMgr inst;
	public static boolean cheatHit = false;
	public static Logger log = LoggerFactory.getLogger(AwardMgr.class);
	public static Random rnd = new Random();
	public Map<Integer, List<AwardTemp>> awardId2Award;
	public static int ITEM_TONGBI_ID = 900001; 		//铜币
	public static int ITEM_EXP_ID = 900006;			//经验
	public static int ITEM_HU_FU_ID = 910000;		//虎符itemId
	public static int ITEM_WEI_WANG = 900011;	// 威望
	public static int item_gong_jin = 900028; //贡金
	public static int item_huang_ye_bi = 900026; //荒野币

	
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
	
	
	public AwardMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<AwardTemp> list = TempletService.listAll("AwardTemp");
		Map<Integer, List<AwardTemp>> awardId2Award = new HashMap<Integer, List<AwardTemp>>();
		for (Object o : list) {
			AwardTemp award = (AwardTemp) o;
			List<AwardTemp> tmp = awardId2Award.get(award.getAwardId());
			if (tmp == null) {
				tmp = new ArrayList<AwardTemp>();
				awardId2Award.put(award.getAwardId(), tmp);
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
			sum += conf.getWeight();
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
	public void getAward(int guanQiaId, Boolean chuanQiMark, boolean pass,
			IoSession session) {
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		BattleResult.Builder ret = BattleResult.newBuilder();
		if (!pass) {
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
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("没有找到君主");
			return;
		}

		jz.tongBi += conf.getMoney();
		JunZhuMgr.inst.addExp(jz, conf.getExp());
		log.info("{}打完关卡:{} 传奇:{} 得到金钱{} 经验{}", jz.name, guanQiaId,
				chuanQiMark, conf.getMoney(), conf.getExp());
		ret.setMoney(conf.getMoney());
		ret.setExp(conf.getExp());

		if (session.getAttribute(SessionAttKey.firstQuanQiaId) == null) {
			log.info("不是首次挑战该关卡，关卡id:{} 传奇:{}，不能获得首次奖励", conf.getId(),
					chuanQiMark);
		} else if (conf.getId() != (Integer) session
				.getAttribute(SessionAttKey.firstQuanQiaId)) {
			log.info("打的id是{}，首打id是{} 传奇:{}，无首次奖励", conf.getId(),
					session.getAttribute(SessionAttKey.firstQuanQiaId),
					chuanQiMark);
		} else {
			if (conf.firstAwardId.equals("")) {
				log.info("该关卡没有配置首次奖励，关卡id:{} 传奇:{}", conf.getId(), chuanQiMark);
			} else {
				List<Integer> fistHitAwardIdList = getHitAwardId(conf.firstAwardConf, 0);
				for (Integer awardId : fistHitAwardIdList) {
					AwardTemp calcV = calcAwardTemp(awardId);
					if(calcV != null) {
						fillBattleAwardInfo(ret, calcV);
						getAwardList.add(calcV);
						log.info("给予 {}关卡首次奖励type {} id {}, 关卡id:{} 传奇:{} ",
								jz.name, calcV.getItemType(), calcV.getItemId(),
								conf.getId(), chuanQiMark);
					}
				}
			}
		}
		session.setAttribute(SessionAttKey.firstQuanQiaId, null);
		//log.info("计算掉落");
		List<Integer> hitAwardIdList = getHitAwardId(conf.awardConf, jz.id);
		for (Integer awardId : hitAwardIdList) {
			AwardTemp calcV = calcAwardTemp(awardId);
			if(calcV != null) {
				fillBattleAwardInfo(ret, calcV);
				getAwardList.add(calcV);
			}
		}
		session.write(ret.build());
		for(AwardTemp award : getAwardList) {
			giveReward(session, award, jz, false,false);
		}
		BagMgr.inst.sendBagInfo(0, session, null);
		JunZhuMgr.inst.sendMainInfo(session);
	}

	/**
	 * 获得命中奖励id列表
	 * 
	 * @param awardArray
	 *            奖励int数组，index=偶数表示awardId，index=之前偶数+1表示获得这个奖励的几率
	 * @return
	 */
	public List<Integer> getHitAwardId(int[] awardArray, long jzId) {
		List<Integer> awardIdList = new ArrayList<Integer>();
		int len = awardArray.length;
		for (int i = 0; i < len; i += 2) {
			int awardId = awardArray[i];
			final int hitNum = awardArray[i + 1];
			int fixRate = hitNum;
			DropRateBean bean = null;
			char dbOp = 'N';//无DB操作
			if(jzId>0){//继续掉率保底
				bean = HibernateUtil.find(DropRateBean.class, "where jzId="+jzId+" and groupId="+awardId);
				if(bean == null){
					bean = new DropRateBean();
					bean.jzId = jzId;
					bean.groupId = awardId;
					dbOp = 'I';//插入
				}else{
					dbOp = 'U';//更新
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
					log.info("fix {}->{} v {} to {}",jzId,awardId, -100, bean.fixScore);
				}
			}else if(bean != null){
				bean.lose += 1;//不掉，概率累加
				bean.fixScore += hitNum;
				log.info("fix {}->{} v {} to {}",jzId,awardId, hitNum, bean.fixScore);
			}
			if(dbOp == 'I'){
				HibernateUtil.insert(bean);
			}else if(dbOp == 'U'){
				HibernateUtil.update(bean);
			}
		}
		return awardIdList;
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
						log.info("命中奖励 awardId:{}, 添加", awardId);
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
				if (item.getAwardItemType() == calcV.getItemType()
						&& item.getAwardId() == calcV.getItemId()) {
					item.setAwardNum(item.getAwardNum() + calcV.getItemNum());
					item.build();
					return;
				}
			}
			AwardItem.Builder award = AwardItem.newBuilder();
			log.info("hit award id {}", calcV.getId());
			award.setAwardId(calcV.getItemId());
			award.setAwardNum(calcV.getItemNum());
			award.setAwardItemType(calcV.getItemType());
			int iconId = getItemIconid(calcV.getItemType(), calcV.getItemId());
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
				if (item.getAwardItemType() == calcV.getItemType()
						&& item.getAwardId() == calcV.getItemId()) {
					item.setAwardNum(item.getAwardNum() + calcV.getItemNum());
					item.build();
					return;
				}
			}
			AwardItem.Builder award = AwardItem.newBuilder();
			log.info("hit award id {}", calcV.getId());
			award.setAwardId(calcV.getItemId());
			award.setAwardNum(calcV.getItemNum());
			award.setAwardItemType(calcV.getItemType());
			int iconId = getItemIconid(calcV.getItemType(), calcV.getItemId());
			award.setAwardIconId(iconId);
			ret.addAwardItems(award.build());
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
			iconId = proto.getIcon();
			break;
		case TYPE_ITEM:
		case TYPE_ZHUANG_BEI:// 装备 装进背包
		case TYPE_YU_JUE:// 玉玦
		case TYPE_JIN_JIE:// 进阶材料 装进背包
		case TYPE_QIANG_HUA:// 强化材料
		case type_fuWen:  // 符文
		case type_baoShi: // 宝石
			iconId = TempletService.itemMap.get(itemId).getIconId();
			break;
		case TYPE_MI_BAO: // 秘宝
			iconId = MibaoMgr.mibaoMap.get(itemId).getIcon();
			break;
		case TYPE_MOBAI_SUIPIAN: // 碎片
			iconId = MibaoMgr.inst.mibaoSuipianMap_2.get(itemId).getIcon();
			break;
		default:
			log.error("未知奖励类型 {}, awardId{}", itemType, itemId);
			break;
		}
		if (iconId == 0) {
			log.error("iconId=0，itemType={},itemId={}", itemType, itemId);
		}
		return iconId;
	}

	public boolean giveReward(IoSession session, AwardTemp a, JunZhu jz) {
		return giveReward(session, a, jz, true);
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
		switch (a.getItemType()) {
		case TYPE_ITEM:// 物品
			if (a.getItemId() == 900009) {// 卡包积分
				jz.cardJiFen += a.getItemNum();
				HibernateUtil.save(jz);
				log.info("{} 获得卡包积分{},达到{}", jz.id, a.getItemNum(),
						jz.cardJiFen);
			} else if (a.getItemId() == 900002) {
				// jz.yuanBao += a.getItemNum();
				YuanBaoMgr.inst.diff(jz, a.getItemNum(), 0, 0,
						YBType.YB_GET_REWARD, "获得奖励");
				HibernateUtil.save(jz);
				log.info("{} 获得元宝{},达到{}", jz.id, a.getItemNum(), jz.yuanBao);
			} else if (a.getItemId() == 900001) {
				jz.tongBi += a.getItemNum();
				HibernateUtil.save(jz);
				log.info("{} 获得铜币{},达到{}", jz.id, a.getItemNum(), jz.tongBi);
			} else if (a.getItemId() == 900003) {// 体力
				JunZhuMgr.inst.updateTiLi(jz, a.getItemNum(), "奖励");
				HibernateUtil.save(jz);
				log.info("{} 获得体力{},达到{}", jz.id, a.getItemNum(), jz.tiLi);
			} else if (a.getItemId() == 900006) { // 经验
				JunZhuMgr.inst.addExp(jz, a.getItemNum());
				log.info("{} 获得经验{},达到{}", jz.id, a.getItemNum(), jz.exp);
			} else if (a.getItemId() == 900015) {// 联盟贡献值
				AlliancePlayer member = HibernateUtil.find(
						AlliancePlayer.class, jz.id);
				if (member != null) {
					member.gongXian += a.getItemNum();
					HibernateUtil.save(member);
					log.info("{} 获得联盟贡献值{},达到{}", jz.id, a.getItemNum(),
							member.gongXian);
				} else {
					/*
					 * 新需求： 没有联盟依旧可以获得联盟声望奖励 2150831
					 */
					member = new AlliancePlayer();
					AllianceMgr.inst.initAlliancePlayerInfo(junZhuId, -1, member, 0);
					member.gongXian = a.getItemNum();
					HibernateUtil.insert(member);
					log.error("{} 无AlliancePlayer数据， 新建， 并获得联盟贡献：{} ", jz.id, member.gongXian);
				}
				AllianceMgr.inst.changeGongXianRecord(jz.id, a.getItemNum());
				break;
			} else if (a.getItemId() == 900018) {// 武艺精气
				int all = TalentMgr.instance.addWuYiJingQi(jz.id,
						a.getItemNum());
				log.info("{} 获得武艺精气{},达到{}", jz.id, a.getItemNum(), all);
				TalentMgr.instance.sendTalentInfo(session);
				break;
			} else if (a.getItemId() == 900019) {// 体魄精气
				int all = TalentMgr.instance.addTiPoJingQi(jz.id,
						a.getItemNum());
				log.info("{} 获得体魄精气{},达到{}", jz.id, a.getItemNum(), all);
				TalentMgr.instance.sendTalentInfo(session);
				break;
			} else if(a.getItemId() == ITEM_WEI_WANG){ // 添加威望奖励
				int all = ShopMgr.inst.addMoney(ShopMgr.Money.weiWang,
						ShopMgr.baizhan_shop_type, jz.id, a.getItemNum());
				log.info("君主：{}获取奖励，威望，获取数是：{}, 获取后拥有：{}", jz.id, a.getItemNum(), all);
			}else if(a.getItemId() == item_gong_jin){ // 添加贡金
				ResourceGongJin re = HibernateUtil.find(ResourceGongJin.class, jz.id);
				if(re == null){
					re = GuoJiaMgr.inst.initjzGongJinInfo(jz.id, null, re);
				}else{
					GuoJiaMgr.inst.resetResourceGongJin(re);
				}
				GuoJiaMgr.inst.changeGongJin(re, a.getItemNum());
				// 判断能不能上缴
				if(GuoJiaMgr.inst.isCanShangjiao(jz.id)){
					GuoJiaMgr.inst.pushCanShangjiao(jz.id);
				}
			}else if(a.getItemId() == item_huang_ye_bi){ // 玩家荒野币
				int all = ShopMgr.inst.addMoney(ShopMgr.Money.huangYeBi, 
						ShopMgr.huangYe_shop_type, jz.id, a.getItemNum());
				log.info("君主：{}获取奖励，荒野币，获取数是：{}, 获取后拥有：{}", jz.id, a.getItemNum(), all);
			}else {
				BaseItem bi = TempletService.itemMap.get(a.getItemId());
				if (bi != null) {// 如果属于背包物品
					bag = BagMgr.inst.loadBag(junZhuId);
					BagMgr.inst.addItem(bag, a.getItemId(), a.getItemNum(), -1,jz.level, "发奖:"+a.getId());
					/*
					 * 判断是否集齐一套古卷
					 */
					isCollectASuitOfGuJuan(junZhuId, bag, bi.getType(), a.getItemId());
					if(sendBagInfo){
						BagMgr.inst.sendBagInfo(0, session, null);
					}
					EventMgr.addEvent(ED.GAIN_ITEM, new Object[]{bag.ownerId, a.getItemId()});
				}
//				log.error("未处理的类型 jzId:{} itemId:{} num:{}", junZhuId,
//						a.getItemId(), a.getItemNum());
			}
			if(sendMainInfo)JunZhuMgr.inst.sendMainInfo(session);
			break;
		case TYPE_ZHUANG_BEI:		// 装备 装进背包
		case TYPE_YU_JUE:			// 玉玦
		case TYPE_JIN_JIE:			// 进阶材料 装进背包
		case TYPE_QIANG_HUA:		// 强化材料
		case type_fuWen:
		case type_baoShi:
			bag = BagMgr.inst.loadBag(junZhuId);
			BagMgr.inst.addItem(bag, a.getItemId(), a.getItemNum(), -1,jz.level, "发奖:"+a.getId());
			if(sendBagInfo)BagMgr.inst.sendBagInfo(0, session, null);
			break;
		case TYPE_MI_BAO: 			// 秘宝
		case TYPE_MOBAI_SUIPIAN:	// 秘宝碎片
			MiBao mibao = null;
			MibaoSuiPian suipian = null;
			int tempId = 0;
			int initialStar = 0;
			int suiNu = 0;
			int realMiBaoId = 0;
			if (a.getItemType() == TYPE_MOBAI_SUIPIAN) {// 碎片
				suipian = MibaoMgr.inst.mibaoSuipianMap_2.get(a.getItemId());
				tempId = suipian.getTempId();
				initialStar = suipian.getInitialStar();
				suiNu = a.getItemNum();
				realMiBaoId = 0;
				log.info("碎片数量{} 初始星级{}", suiNu, initialStar);
			}
			if (a.getItemType() == TYPE_MI_BAO) {// 秘宝
				mibao = MibaoMgr.mibaoMap.get(a.getItemId());
				if (mibao == null) {
					log.error("秘宝{}数据错误", a.getItemId());
					return false;
				}
				suipian = MibaoMgr.inst.mibaoSuipianMap.get(mibao.getTempId());
				tempId = mibao.getTempId();
				initialStar = mibao.getInitialStar();
				suiNu = suipian.getFenjieNum();
				realMiBaoId = mibao.getId();
			}
			boolean isGetMibao = false;
			String hql = "where ownerId = " + jz.id + " and tempId=" + tempId;
			MiBaoDB mibaoDB = HibernateUtil.find(MiBaoDB.class, hql);
			if (mibaoDB == null) {
				mibaoDB = new MiBaoDB();
				// 改自增主键为指定
				// 2015年4月17日16:57:30int改为long
				long dbId = TableIDCreator.getTableID(MiBaoDB.class, 1L);
				mibaoDB.setDbId(dbId);

				mibaoDB.setOwnerId(jz.id);
				mibaoDB.setTempId(tempId);
				mibaoDB.setMiBaoId(realMiBaoId);
				mibaoDB.setStar(initialStar);
				if (a.getItemType() == TYPE_MOBAI_SUIPIAN) {
					mibaoDB.setLevel(0);
					mibaoDB.setSuiPianNum(suiNu);
					mibaoDB.setClear(false);
				}
				if (a.getItemType() == TYPE_MI_BAO) {
					mibaoDB.setLevel(1);
					mibaoDB.setSuiPianNum(0);
					mibaoDB.setClear(mibao, jz);
					log.info("玩家：{}获取了一个完整的秘宝 1 ， mibaodbid：{}", junZhuId, realMiBaoId);
					isGetMibao = true;
				}
				log.info("君主 {} 获得 tempId {} id {} 的秘宝", jz.id, tempId,
						realMiBaoId);
				HibernateUtil.save(mibaoDB);
//				 发送秘宝或者碎片更新的消息
//				MibaoMgr.inst.mibaoInfosRequest(PD.C_MIBAO_INFO_REQ, session);
			} else {
				if (mibaoDB.getLevel() > 0) { // 有完整秘宝
					mibaoDB.setSuiPianNum(mibaoDB.getSuiPianNum() + suiNu);
					log.info("君主 :{}拥有秘宝:{}, 因此获得秘宝碎片 {} 个", jz.id,
							mibaoDB.getMiBaoId(), suiNu);
				} else { // 没有完整秘宝
					if (a.getItemType() == TYPE_MOBAI_SUIPIAN) {
						mibaoDB.setSuiPianNum(mibaoDB.getSuiPianNum() + suiNu);
						log.info("君主 :{}获得秘宝:{},碎片 {} 个", jz.id,
								mibaoDB.getMiBaoId(), suiNu);
					}
					if (a.getItemType() == TYPE_MI_BAO) {
						mibaoDB.setMiBaoId(realMiBaoId);
						mibaoDB.setLevel(1);
						mibaoDB.setStar(initialStar);
						log.info("玩家：{}获取了一个完整的秘宝 2 ， mibaodbid：{}", junZhuId, realMiBaoId);
						isGetMibao = true;
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
				doRenWuForMiBao(mibaoDB, jz);
				EventMgr.addEvent(ED.GAIN_MIBAO, new Object[]{jz,session});
			}
			break;
			/*
		case TYPE_WU_JIANG:// 武将
		{
			HeroProtoType proto = HeroMgr.tempId2HeroProto.get(a.getItemId());
			if (proto == null) {
				log.error("武将模板没有找到{}", a.getItemId());
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
					wuJiang.setNum(wuJiang.getNum() + jingPo.getFenjieNum());
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
			JingPo jingPo = HeroMgr.id2JingPo.get(a.getItemId());
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
				wuJiang.setNum(wuJiang.getNum() + a.getItemNum());
				HibernateUtil.save(wuJiang);
			}
			HeroMgr.inst.sendWuJiangList(session);
			break;
			*/
		default:
			log.error("未知奖励类型 {}, awardId{}", a.getItemType(), a.getId());
			return false;
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
						type = a.getItemType();
						tempId = a.getItemId();
						number = a.getItemNum();
					}
					sd.append("#").append(type).append(":").append(tempId)
							.append(":").append(number);
				}
			}
			sd = new StringBuilder(sd.substring(1));
		}
		return sd;
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
					calcV.setItemType(itemType);
					calcV.setItemId(itemId);
					calcV.setItemNum(itemNum);
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
	public void doRenWuForMiBao(MiBaoDB mibaoDB, JunZhu jz){
		if(mibaoDB.getMiBaoId() > 0 && mibaoDB.getLevel() > 0){
			if(mibaoDB.isClear()){
				//  主线任务：星级: 要求解锁
				EventMgr.addEvent(ED.mibao_shengStar_x, new Object[]{jz.id, mibaoDB.getStar()});
				//  主线任务：等级 ：要求解锁秘宝
				EventMgr.addEvent(ED.mibao_shengji_x, new Object[]{jz.id, mibaoDB.getLevel()});
			}
			// 获取秘宝：获得一个秘宝： 不要求解锁 
			EventMgr.addEvent(ED.get_item_finish, new Object[] { jz.id, mibaoDB.getMiBaoId()});
			// 获取秘宝 ：秘宝合成: 不要求解锁 -朱诚 20150925
			EventMgr.addEvent(ED.MIBAO_HECHENG, new Object[]{jz.id, mibaoDB.getMiBaoId()});
			// 主线任务： 获取秘宝的个数： 不要求解锁 -朱诚 20150925
			String where2 = " WHERE ownerId =" + jz.id + " AND level>=1 AND miBaoId > 0";
			List<MiBaoDB> dbList = HibernateUtil.list(MiBaoDB.class, where2);
			int number = 0;
			for(MiBaoDB d: dbList){
				if(d.getMiBaoId() > 0 && d.getLevel() > 0){
//					if(d.isClear()){
						number ++;
//					}else if(MibaoMgr.inst.isClear(d.getMiBaoId(), jz)){
//						number ++;
//						HibernateUtil.save(d);
//					}
				}
			}
			if (number > 0) {
				EventMgr.addEvent(ED.get_x_mibao, new Object[] { jz.id,
						dbList.size() });
			}
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
				EventMgr.addEvent(ED.have_total_guJuan , new Object[] {junZhuId});
				log.info("君主：{}背包中type:{}古卷类型大于等于5，主线任务完成处理被调用", junZhuId, baseType);
			}
		}
	}
}
