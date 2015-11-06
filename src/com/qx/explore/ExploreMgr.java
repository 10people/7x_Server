package com.qx.explore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Explore.ExploreAwardsInfo;
import qxmobile.protobuf.Explore.ExploreInfoResp;
import qxmobile.protobuf.Explore.ExploreMineInfo;
import qxmobile.protobuf.Explore.ExploreReq;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.IfExploreResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.util.MathUtils;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.TaskData;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * 管理所有在线玩家的探宝信息 This class is used for ...
 * 
 * @author wangZhuan
 * @version 9.0, 2014年11月5日 下午6:08:25
 */
public class ExploreMgr {
	public static ExploreMgr inst;
	public Map<Integer, ItemTemp> itemTempMap;
	public Map<Integer, Purchase> purchasesMap;
	public static Logger log = LoggerFactory.getLogger(ExploreMgr.class);
	public static final int space = 100;
	public static int awardNumber = 8;
	public static int types = 5;

	public ExploreMgr() {
		inst = this;
		initData();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		Map<Integer, ItemTemp> itemTempMap = new HashMap<Integer, ItemTemp>();
		Map<Integer, Purchase> purchasesMap = new HashMap<Integer, Purchase>();
		List<ItemTemp> itemTempList = TempletService.listAll(ItemTemp.class
				.getSimpleName());
		for (ItemTemp item : itemTempList) {
			itemTempMap.put(item.getId(), item);
		}
		List<Purchase> purchaseList = TempletService.listAll(Purchase.class
				.getSimpleName());
		for (Purchase p : purchaseList) {
			int id = p.getId();
			if (id == ExploreConstant.SIGLE_BUY_ID
					|| id == ExploreConstant.PAY_BUY_ID
					|| id == ExploreConstant.GUILD_1_BUY_ID
					|| id == ExploreConstant.GUILD_2_BUY_ID) {
				purchasesMap.put(p.getId(), p);
			}
		}
		this.itemTempMap = itemTempMap;
		this.purchasesMap = purchasesMap;
	}

	/**
	 * 主城界面显示探宝是否还有次数
	 * 
	 * @Title: sendIfExploreInfo
	 * @Description:
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void sendIfExploreInfo(int code, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		boolean isHaveChance = false;
		if (jz != null) {
			List<ExploreMine> list = getMineList(jz.id);
			if (list != null) {
				for (ExploreMine em : list) {
					if (em != null && em.haveFreeChance() == 127) {
						isHaveChance = true;
						break;
					}
				}
			} else {
				isHaveChance = true;
			}
		}
		qxmobile.protobuf.Explore.IfExploreResp.Builder resp = IfExploreResp
				.newBuilder();
		resp.setYes(isHaveChance);
		session.write(resp.build());
	}

	/**
	 * 请求探宝主界面
	 * 
	 * @Title: sendExploreMineInfo
	 * @Description:
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void sendExploreMineInfo(int code, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("进入探宝，君主不存在");
			return;
		}
		long junZhuId = jz.id;

		List<ExploreMine> mineList = getMineList(junZhuId);
		if (mineList == null) {
			mineList = initMines(junZhuId);
		}
		// 检查是不是存在没有探宝的类型
		if (mineList.size() < types) {
			setMineList(mineList, junZhuId);
		}
		qxmobile.protobuf.Explore.ExploreInfoResp.Builder resp = ExploreInfoResp.newBuilder();
		// 判断君主是否有联盟
		AlliancePlayer guild = HibernateUtil.find(AlliancePlayer.class,
				junZhuId);
		if (guild == null || guild.lianMengId <= 0) {
			resp.setHasGuild(false);
			resp.setGongXian(guild == null ? 0 : guild.gongXian);
			log.info("君主:{}无联盟探宝功能", junZhuId);
		} else {
			resp.setHasGuild(true);
			resp.setGongXian(guild.gongXian);
			log.info("君主:{}有联盟探宝功能", junZhuId);
		}
		resp.setYuanBao(jz.yuanBao);
		// 获取逆鳞精铁和上古青铜的数量
		BagMgr ins = BagMgr.inst;
		Bag<BagGrid> bag = ins.loadBag(junZhuId);
		int tie = ins.getItemCount(bag, ExploreConstant.IRON_ID);
		int tong = ins.getItemCount(bag, ExploreConstant.COPPER_ID);
		resp.setTie(tie);
		resp.setTong(tong);

		qxmobile.protobuf.Explore.ExploreMineInfo.Builder mineInfo = null;
		for (ExploreMine mine : mineList) {
			mineInfo = ExploreMineInfo.newBuilder();
			mineInfo.setType(mine.getType());
			// 发送截止到下一次领奖还有多少时间
			mineInfo.setRemainingTime(mine.getReminingTime());
			// 发送已经抽取的免费次数
			mineInfo.setGotTimes(mine.getExactTimes(new Date()));
			mineInfo.setTotalTimes(mine.getTotalTimes());
			mineInfo.setDiscount(mine.getDiscount());
			mineInfo.setCost(getCost(mine.getType()));
			// 可领取状态
			mineInfo.setIsCanGet(mine.haveFreeChance() == 127 ? true : false);
			resp.addMineRegionList(mineInfo);
		}
		session.write(resp.build());
	}

	/**
	 * 探宝处理： 发送所得奖励，并记录探宝之后的数据
	 * 
	 * @Title: toExplore
	 * @Description:
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void toExplore(int code, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("junzhu not exit");
			return;
		}
		// 接受请求数据
		ExploreReq.Builder req = (qxmobile.protobuf.Explore.ExploreReq.Builder) builder;
		int type = req.getType();
		boolean isBuy = req.getIsBuy();
		byte yes = 0;
		ExploreMine mine = getMineByType(jz.id, type);
		if (mine == null) {
			mine = intMineForType(jz.id, type);
		}
		int thisTimes = mine.getAllTimes() + 1;
		boolean addA = false;
		byte reason = 0;
		// 一定会获得的物品id
		if (isBuy) {
			yes = this.isBuySuccess(jz, type, session);
			if (yes == 0) {
				addA = true;
			} else {
				sendExploreFailedMessage(session, yes);
				log.error("君主{}，探宝类型是{},因为元宝(或者联盟探宝地的贡献值)不足探宝失败", jz.id, type);
				return;
			}
		} else {
			reason = mine.haveFreeChance();
			if (reason == 127) {
				addA = true;
			}
		}
		if (addA) {
			List<AwardTemp> awards = getAwards(type, thisTimes, mine);
			int tanbaoTims = 1;
			if (awards.size() == 0) {
				sendExploreFailedMessage(session, ExploreConstant.DATA_PROBLEM);
				log.error("数据出错");
				return;
			}
			EventMgr.addEvent(ED.TAN_BAO_JIANG_LI, new Object[]{jz,session,awards});
			Map<Integer, int[]> fenjieM = mibaoAward(awards, jz);
			// 现增添奖励，再筛选是否碎成碎片
			for (AwardTemp awa : awards) {
				AwardMgr.inst.giveReward(session, awa, jz, false);
				log.info("{}探宝奖励{}:{}:{}",jz.id,awa.getItemType(),awa.getItemId(),awa.getItemNum());
			}
			// 做一些抽奖结束的处理
			mine.setTimes(mine.getExactTimes(new Date()) + 1);
			if (!isBuy) {
				mine.setLastGetTime(new Date());
			}
			if (type == ExploreConstant.PAY || type == ExploreConstant.GUILD_2) {
				mine.setAllTimes(thisTimes + 10 - 1);
				tanbaoTims = 10;
			} else {
				mine.setAllTimes(thisTimes);
				tanbaoTims = 1;
			}
			HibernateUtil.save(mine);
			sendExploreAwardInfo(session, awards, type, fenjieM);
			HibernateUtil.save(jz);
			JunZhuMgr.inst.sendMainInfo(session);
			log.info("君主{}探宝，类型{},成功", jz.id, type);
			ActLog.log.FineGem(jz.id, jz.name, ActLog.vopenid, type, 0, "0", 0, "0", 0);
			// 一次探宝任务完成
			for (AwardTemp a : awards) {
				if (a.getItemId() == TaskData.tanbao_itemId_1){
						//|| a.getItemId() == TaskData.tanbao_itemId_2) {
					EventMgr.addEvent(ED.get_item_finish, new Object[] { jz.id,
							a.getItemId() });
				}
//				// 检查是否有秘宝 （放在AwardMgr中）
//				if (a.getItemType() == 4) {
//					// 主线任务： 获取秘宝的个数
//					String where2 = " WHERE ownerId =" + jz.id
//							+ " AND level>=1 ";
//					List<MiBaoDB> dbList = HibernateUtil.list(MiBaoDB.class,
//							where2);
//					if (dbList != null && dbList.size() != 0) {
//						EventMgr.addEvent(ED.get_x_mibao, new Object[] { jz.id,
//								dbList.size() });
//					}
//				}
			}
			// 每日任务中记录探宝成功一次
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					jz.id, DailyTaskConstants.tanbao_5_id, tanbaoTims));
		} else {
			sendExploreFailedMessage(session, reason);
			log.error("君主{}，因为{}探宝失败", jz.id, reason);
		}
	}

	/**
	 * 获取一定会获得物品
	 * 
	 * @Title: getCertainAward
	 * @Description:
	 * @param type
	 * @return
	 */
	protected AwardTemp getCertainAward(int type) {
		int cerItemId = 0;
		AwardTemp certainAward = new AwardTemp();
		if (type == ExploreConstant.FREE) {
			cerItemId = ExploreConstant.IRON_ID;
		} else {
			cerItemId = ExploreConstant.COPPER_ID;
		}
		ItemTemp certainItem = itemTempMap.get(cerItemId);
		certainAward.setAwardId(11111);
		certainAward.setItemId(certainItem.getId());
		certainAward.setItemType(ExploreConstant.QIANG_HUA_TYPE);
		if (type == ExploreConstant.PAY || type == ExploreConstant.GUILD_2) {
			certainAward.setItemNum(10);
		} else
			certainAward.setItemNum(1);
		return certainAward;
	}

	/**
	 * 
	 * @Title: getAwardId
	 * @Description:
	 * @param type
	 * @param number
	 * @return
	 */
	protected AwardTemp getTrulyAwards(int type, int number, ExploreMine mine) {
		AwardTemp choiceAward = new AwardTemp();
		switch (type) {
		case ExploreConstant.FREE:
			if (number == 0) {
				choiceAward = AwardMgr.inst
						.calcAwardTemp(ExploreConstant.FREE_AWARDID_0);
			} else {
				choiceAward = AwardMgr.inst
						.calcAwardTemp(ExploreConstant.FREE_AWARDID);
			}
			break;
		/*
		 * 单抽和联盟单抽 5次一组，必抽的一组好的奖励(除了首抽)
		 */
		// 首抽不一样 别的抽取与下面几种类型相同
		case ExploreConstant.SIGLE:
			if (number == 0) {
				choiceAward = AwardMgr.inst
						.calcAwardTemp(ExploreConstant.SIGLE_AWARDID_0);
				break;
			}
			choiceAward = getChoiceAward(number, mine);
			break;
		case ExploreConstant.GUILD_1:
			choiceAward = getChoiceAward(number, mine);
			break;
		}
		return choiceAward;
	}

	/**
	 * 联盟单抽或者付费单抽获取的奖励
	 * 
	 * @Title: getChoiceAward
	 * @Description:
	 * @param number
	 * @param mine
	 * @return
	 */
	public AwardTemp getChoiceAward(int number, ExploreMine mine) {
		AwardTemp temp = null;
		/*
		 * awardNumber次中的最后一次抽奖
		 */
		if (number != 0 && number % awardNumber == 0) {
			if (mine.hasGoodAwar) {
				temp = AwardMgr.inst
						.calcAwardTemp(ExploreConstant.SIGLE_AWARDID_1);
				// awardNumber设置为1组，对下一组进行重置hasGoodAwar的值
				mine.hasGoodAwar = false;
				HibernateUtil.save(mine);
			} else {
				temp = AwardMgr.inst
						.calcAwardTemp(ExploreConstant.SIGLE_AWARDID_2);
			}
			return temp;
		}
		if (mine.hasGoodAwar) {
			temp = AwardMgr.inst.calcAwardTemp(ExploreConstant.SIGLE_AWARDID_1);
		} else {
			int id = getRandomId();
			if (id == ExploreConstant.SIGLE_AWARDID_2) {
				mine.hasGoodAwar = true;
				HibernateUtil.save(mine);
			}
			temp = AwardMgr.inst.calcAwardTemp(id);
		}
		return temp;
	}

	public int getRandomId() {
		int[][] array = { { ExploreConstant.SIGLE_AWARDID_1, 875 },
				{ ExploreConstant.SIGLE_AWARDID_2, 125 } };
		int result = MathUtils.getRandom(array, 1000);
		return result;
	}

	/*
	 * public List<AwardTemp> getTenAwards(){ List<AwardTemp> list = new
	 * ArrayList<AwardTemp>(); int[] ids1 = getRandomAwardId(); int[] ids2 =
	 * getRandomAwardId(); AwardTemp temp = null; AwardTemp mi1 = null;
	 * AwardTemp mi2 = null; int flag = 0; for(int i=0; i < awardNumber; i++){
	 * temp = AwardMgr.inst.calcAwardTemp(ids1[i]); if(temp != null){
	 * list.add(temp); if(temp.getItemType() == 4){ mi1 = temp; } } } for(int
	 * i=0; i < awardNumber; i++){ temp = AwardMgr.inst.calcAwardTemp(ids2[i]);
	 * if(temp != null){ list.add(temp); if(temp.getItemType() == 4){ mi2 =
	 * temp; flag = i; } } } // 防止出现重复的秘宝 if(mi1 != null && mi2 != null){
	 * list.remove(mi2); int i = 0; // 防止死循环 while(i++ < 100 && mi1.getItemId()
	 * == mi2.getItemId()){ mi2 = AwardMgr.inst.calcAwardTemp(ids2[flag]); }
	 * list.add(mi2); } return list; }
	 */
	/*
	 * public List<AwardTemp> getTenAwards_change8(int number, ExploreMine
	 * mine){ List<AwardTemp> list = new ArrayList<AwardTemp>(); AwardTemp temp
	 * = null; AwardTemp flag = null; int stop = 0; for(int i=0; i < 10 ;i++){
	 * stop = 0; temp = getChoiceAward(number, mine); while(temp == null ||
	 * (flag != null && temp.getItemId() == flag.getItemId() && stop++ < 100)){
	 * temp = getChoiceAward(number, mine); } // 防止出现重复秘宝 if(temp.getItemType()
	 * == 4){ flag = temp; } list.add(temp); number ++; } return list; }
	 */
	public List<AwardTemp> getTenAwards_mustHasGood() {
		List<AwardTemp> list = new ArrayList<AwardTemp>();
		int[] ids1 = getRandomAwardId();
		AwardTemp temp = null;
		AwardTemp flag = null;
		int stop = 0;
		for (int i = 0; i < awardNumber; i++) {
			stop = 0;
			do {
				temp = AwardMgr.inst.calcAwardTemp(ids1[i]);
			} while (temp == null && stop++ < 100);
			list.add(temp);
			if (temp != null && temp.getItemType() == 4) {
				flag = temp;
			}
		}
		for (int i = 0; i++ < 2;) {
			stop = 0;
			do {
				temp = AwardMgr.inst.calcAwardTemp(getRandomId());
				// 防止出现null或者和之前相同的秘宝
			} while (temp == null
					|| (flag != null && temp.getItemType() == 4
							&& temp.getItemId() == flag.getItemId() && stop++ < 100));
			list.add(temp);
			if (temp.getItemType() == 4) {
				flag = temp;
			}
		}
		return list;
	}

	/**
	 * 得到一个数组，awardNumber个数字中有且只有一个不一样的，先逐个随机得到，若得不到，则一定赋予
	 * 
	 * @Title: getRandomAwardId5
	 * @Description:
	 * @return
	 */
	public int[] getRandomAwardId() {
		int[] ids = new int[awardNumber];
		ids[0] = getRandomId();
		int which = awardNumber - 1;
		for (int i = 0; i < awardNumber; i++) {
			if (ids[i] == ExploreConstant.SIGLE_AWARDID_2) {
				which = i;
				break;
			} else {
				if (i < awardNumber - 1) {
					ids[i + 1] = getRandomId();
				}
			}
		}
		if (which == awardNumber - 1) {
			ids[which] = ExploreConstant.SIGLE_AWARDID_2;
		}
		if (which < awardNumber - 1) {
			for (int i = which + 1; i < awardNumber; i++) {
				ids[i] = ExploreConstant.SIGLE_AWARDID_1;
			}
		}
		return ids;
	}

	/**
	 * 花元宝购买探宝次数,对于联盟探宝，花费的是玩家的贡献值
	 * 
	 * @Title: isBuySuccess
	 * @Description:
	 * @param jz
	 * @param type
	 * @param times
	 * @return 购买是否成功
	 */
	protected byte isBuySuccess(JunZhu jz, int type, IoSession session) {
		int yuanbao = getCost(type);
		if (type == ExploreConstant.GUILD_1 || type == ExploreConstant.GUILD_2) {
			AlliancePlayer guild = HibernateUtil.find(AlliancePlayer.class,
					jz.id);
			if (guild == null || guild.lianMengId == 0
					|| guild.gongXian < yuanbao) {
				return ExploreConstant.HAVE_NOT_ENOUGH_GONGXIAN;
			}
			guild.gongXian -= yuanbao;
			HibernateUtil.save(guild);
			log.info("玩家id{},姓名 {}, 购买 {}类型的联盟探宝一次, 花费贡献值{}", jz.id, jz.name,
					type, yuanbao);
			return 0;
		} else {
			if (jz.yuanBao < yuanbao) {
				return ExploreConstant.HAVE_NOT_ENOUGH_MONEY;
			}
			// jz.yuanBao -= yuanbao;
			YuanBaoMgr.inst.diff(jz, -yuanbao, 0, yuanbao,
					YBType.YB_BUY_TANBAO_CISHU, "购买探宝次数");
			HibernateUtil.save(jz);
			log.info("玩家id{},姓名 {}, 购买 {}类型的探宝一次, 花费元宝{}个", jz.id, jz.name,
					type, yuanbao);
			// 同步君主元宝信息
			JunZhuMgr.inst.sendMainInfo(session);
			return 0;
		}
	}

	/**
	 * 获取购买探宝的元宝数
	 * 
	 * @Title: getCost
	 * @Description:
	 * @param type
	 * @return
	 */
	protected int getCost(int type) {
		int purchaseId = 0;
		switch (type) {
		case ExploreConstant.SIGLE:
			purchaseId = ExploreConstant.SIGLE_BUY_ID;
			break;
		case ExploreConstant.PAY:
			purchaseId = ExploreConstant.PAY_BUY_ID;
			break;
		case ExploreConstant.GUILD_1:
			purchaseId = ExploreConstant.GUILD_1_BUY_ID;
			break;
		case ExploreConstant.GUILD_2:
			purchaseId = ExploreConstant.GUILD_2_BUY_ID;
			break;
		}
		Purchase p = purchasesMap.get(purchaseId);
		if (p == null)
			return 0;
		return p.getYuanbao();
	}

	/**
	 * 获取奖励
	 * 
	 * @Title: getAwards
	 * @Description:
	 * @param type
	 * @param number
	 * @return
	 */
	protected List<AwardTemp> getAwards(int type, int number, ExploreMine mine) {
		List<AwardTemp> awards = new ArrayList<AwardTemp>();
		awards.add(getCertainAward(type));
		if (type == ExploreConstant.PAY || type == ExploreConstant.GUILD_2) {
			awards.addAll(getTenAwards_mustHasGood());
		} else {
			awards.add(getTrulyAwards(type, number, mine));
		}
		return awards;
	}

	/**
	 * 添加奖励
	 * 
	 * @Title: addAwards
	 * @Description:
	 * @param session
	 * @param jz
	 * @param awards
	 */
	protected void addAwards(IoSession session, JunZhu jz, List<AwardTemp> awards) {
		
	}

	protected Map<Integer, int[]> mibaoAward(List<AwardTemp> awards, JunZhu jz) {
		Map<Integer, int[]> fenjieM = new HashMap<Integer, int[]>();
		if (awards == null || awards.size() == 0)
			return null;
		for (AwardTemp a : awards) {
			// 4 表示 秘宝的itemType是4,
			if (a.getItemType() == 4) {
				MiBao mibao = MibaoMgr.mibaoMap.get(a.getItemId());
				MibaoSuiPian suipian = MibaoMgr.inst.mibaoSuipianMap_2
						.get(mibao.getSuipianId());
				String hql = "where ownerId = " + jz.id + " and tempId="
						+ mibao.getTempId();
				MiBaoDB mibaoDB = HibernateUtil.find(MiBaoDB.class, hql);
				if (mibaoDB != null && mibaoDB.getMiBaoId() > 0) {
					int number = suipian.getFenjieNum();
					fenjieM.put(a.getItemId(), new int[] { suipian.getId(),
							number, 5 }); // 5 表示 碎片的itemType是5,
				}
			}
		}
		return fenjieM;
	}

	/**
	 * 发送探得的宝藏
	 * 
	 * @Title: sendExploreResultInfo
	 * @Description:
	 * @param session
	 */
	protected void sendExploreAwardInfo(IoSession session,
			List<AwardTemp> awards, int type, Map<Integer, int[]> fenjieM) {
		boolean isHave = false;
		if (fenjieM != null && fenjieM.size() != 0) {
			isHave = true;
		}
		ExploreAwardsInfo.Builder resp = ExploreAwardsInfo.newBuilder();
		qxmobile.protobuf.Explore.Award.Builder award = null;
		resp.setType(type);
		int size = awards.size();
		for (int index = 0; index < size; index++) {
			AwardTemp awa = awards.get(index);
			award = qxmobile.protobuf.Explore.Award.newBuilder();
			award.setItemId(awa.getItemId());
			award.setItemType(awa.getItemType());
			int[] piece = fenjieM.get(award.getItemId());
			if (isHave && piece != null) {
				// 分解的碎片id
				award.setPieceId(piece[0]);
				// 分解的碎片数量
				award.setPieceNumber(piece[1]);
				// 分解的碎片的读表类型
				award.setPieceType(piece[2]);
			}
			// 只是秘宝的星级(读表产生的初始星级)
			if (award.getItemType() == 4) {
				MiBao mibao = MibaoMgr.mibaoMap.get(award.getItemId());
				award.setItemStar(mibao.getInitialStar());
			}
			award.setItemNumber(awa.getItemNum());
			// 0就是卡片的奖励物品
			// 1就是飞出的青铜或精铁
			if (index == 0)
				award.setIsQuality(1);
			else
				award.setIsQuality(0);
			resp.addAwardsList(award);
		}
		session.write(resp.build());
	}

	/**
	 * 向前端发送探宝失败的消息
	 * 
	 * @Title: sendExploreFailedMessage
	 * @Description:
	 * @param session
	 */
	protected void sendExploreFailedMessage(IoSession session, int failedType) {
		ExploreResp.Builder resp = ExploreResp.newBuilder();
		resp.setType(failedType);
		session.write(resp.build());
	}

	/**
	 * 初始玩家的所有矿
	 * 
	 * @Title: initJunZhuMine
	 * @Description:
	 * @param junzhuId
	 * @return
	 */
	protected List<ExploreMine> initMines(long junzhuId) {
		List<ExploreMine> mines = new ArrayList<ExploreMine>();
		mines.add(new ExploreMine(junzhuId * space + ExploreConstant.FREE,
				ExploreConstant.FREE));
		mines.add(new ExploreMine(junzhuId * space + ExploreConstant.SIGLE,
				ExploreConstant.SIGLE));
		mines.add(new ExploreMine(junzhuId * space + ExploreConstant.PAY,
				ExploreConstant.PAY));
		return mines;
	}

	/**
	 * 初始化一个类型为 @type 的矿源
	 * 
	 * @Title: intMineForType
	 * @Description:
	 * @param jzI
	 * @param type
	 * @return
	 */
	protected ExploreMine intMineForType(long jzI, int type) {
		long id = jzI * space + type;
		ExploreMine mine = new ExploreMine(id, type);
		MC.add(mine, id);
		HibernateUtil.insert(mine);
		log.info("君主id:{}, 初始化并且持久化类型是:{}的探宝数据成功", jzI, type);
		return mine;
	}

	public ExploreMine getMineByType(long junzhuId, int type) {
		long id = junzhuId * space + type;
		ExploreMine mine = HibernateUtil.find(ExploreMine.class, id);
		return mine;
	}

	public ExploreMine getMineFromList(List<ExploreMine> ms, int type) {
		if (ms == null)
			return null;
		for (ExploreMine m : ms) {
			if (m.getType() == type) {
				return m;
			}
		}
		return null;
	}

	public List<ExploreMine> getMineList(long jid) {
		long start = jid * space;
		long end = start + space;
		List<ExploreMine> list = HibernateUtil.list(ExploreMine.class,
				"where id>=" + start + " and id<" + end);
		return list;
	}

	public void setMineList(List<ExploreMine> mineList, long junZhuId) {
		ExploreMine m1 = null;
		ExploreMine m2 = null;
		ExploreMine m3 = null;
		ExploreMine m4 = null;
		ExploreMine m5 = null;
		for (ExploreMine m : mineList) {
			switch ((byte) m.getType()) {
			case ExploreConstant.FREE:
				m1 = m;
				break;
			case ExploreConstant.SIGLE:
				m2 = m;
				break;
			case ExploreConstant.PAY:
				m3 = m;
				break;
			case ExploreConstant.GUILD_1:
				m4 = m;
				break;
			case ExploreConstant.GUILD_2:
				m5 = m;
				break;
			}
		}
		if (m1 == null) {
			m1 = new ExploreMine(junZhuId, ExploreConstant.FREE);
			mineList.add(m1);
		}
		if (m2 == null) {
			m2 = new ExploreMine(junZhuId, ExploreConstant.SIGLE);
			mineList.add(m2);
		}
		if (m3 == null) {
			m3 = new ExploreMine(junZhuId, ExploreConstant.PAY);
			mineList.add(m3);
		}
		if (m4 == null) {
			m4 = new ExploreMine(junZhuId, ExploreConstant.GUILD_1);
			mineList.add(m4);
		}
		if (m5 == null) {
			m5 = new ExploreMine(junZhuId, ExploreConstant.GUILD_2);
			mineList.add(m5);
		}
	}
}
