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

import qxmobile.protobuf.Explore.ExploreInfoResp;
import qxmobile.protobuf.Explore.ExploreReq;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.TypeInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.qx.account.FunctionOpenMgr;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.TaskData;
import com.qx.timeworker.FunctionID;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * 管理所有在线玩家的探宝信息 This class is used for ...
 * 
 * @author wangZhuan
 * @version 9.0, 2014年11月5日 下午6:08:25
 */
public class ExploreMgr extends EventProc{
	public static ExploreMgr inst;
	public Map<Integer, ItemTemp> itemTempMap;
	public Map<Integer, Purchase> purchasesMap;
	public static Logger log = LoggerFactory.getLogger(ExploreMgr.class.getSimpleName());
	public static final int space = 100;
	public static int awardNumber = 8;
	public static int types = 5;
	
	
	/**保底奖励概率*/ 
	public static int tongBi_goodAwardProbability = CanShu.TONGBI_TANBAO_BAODI;
	public static int yuanBao_goodAwardProbability = CanShu.YUANBAO_TANBAO_BAODI;
	public static int allProbability = 1000;

	public static byte money_ok = 0;
	/** 铜币不足**/
	public static byte tongBi_little = 1;
	/** 元宝不足**/
	public static byte yuanBao_little = 2;
	public static byte wrong = 3;

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
			if (id == TanBaoData.tongBi_pay_sigle
					|| id == TanBaoData.tongBi_pay_ten
					|| id == TanBaoData.yuanBao_pay_sigle
					|| id == TanBaoData.yuanBao_pay_ten) {
				purchasesMap.put(p.getId(), p);
			}
		}
		this.itemTempMap = itemTempMap;
		this.purchasesMap = purchasesMap;
	}
//
//	/**
//	 * 主城界面显示探宝是否还有次数
//	 * 
//	 * @Title: sendIfExploreInfo
//	 * @Description:
//	 * @param code
//	 * @param session
//	 * @param builder
//	 */
//	public void sendIfExploreInfo(int code, IoSession session, Builder builder) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		boolean isHaveChance = false;
//		if (jz != null) {
//			List<ExploreMine> list = getMineList(jz.id);
//			if (list != null) {
//				for (ExploreMine em : list) {
//					if (em != null && em.haveFreeChance() == 127) {
//						isHaveChance = true;
//						break;
//					}
//				}
//			} else {
//				isHaveChance = true;
//			}
//		}
//		qxmobile.protobuf.Explore.IfExploreResp.Builder resp = IfExploreResp
//				.newBuilder();
//		resp.setYes(isHaveChance);
//		session.write(resp.build());
//	}

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
		Map<Byte, ExploreMine> map = new HashMap<Byte, ExploreMine>();
		for(ExploreMine e: mineList){
			map.put((byte)(e.id % space), e);
		}

		qxmobile.protobuf.Explore.ExploreInfoResp.Builder resp = ExploreInfoResp.newBuilder();
		/*
		 *  铜币探宝
		 */
		int all = TanBaoData.tongBi_all_free_times;
		ExploreMine e = map.get(TanBaoData.tongBi_type);
		if(e != null){
			resetExploreMine(e);
		}
		resp.setAllFreeTongBiCount(all);
		resp.setRemainFreeTongBiCount( e == null? all: all - e.usedFreeNumber);
		resp.setTongBiCd(e == null? 0:getCD(e.lastFreeGetTime, TanBaoData.tongBi_CD));
		resp.setTongBi(jz.tongBi);

		/*
		 * 元宝探宝
		 */
		e = map.get(TanBaoData.yuanBao_type);
		resp.setYuanBaoCd(e == null? 0:getCD(e.lastFreeGetTime, TanBaoData.yuanBao_CD));
		resp.setYuanBao(jz.yuanBao);

		session.write(resp.build());
	}

	public int getCD(Date lastGetTime, int timeInterval){
		if(lastGetTime == null){
			return 0;
		}
		long lastTime = lastGetTime.getTime() / 1000;
		long time = lastTime + timeInterval - System.currentTimeMillis() / 1000;
		return time < 0? 0: (int)time;
	}

	/**
	 * 探宝处理： 发送所得奖励，并记录探宝之后的数据
	 * 
	 * @Title: toExplore
	 * @Description:
	 * @param code
	 * @param session
	 * @param builder
	 * @return 仅仅是为了gm中探宝返回的提示
	 */
	public void toExplore(int code, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("junzhu not exit");
			return;
		}
		// 接受请求数据
		ExploreReq.Builder req = (qxmobile.protobuf.Explore.ExploreReq.Builder) builder;
		int reqType = req.getType(); // 1-铜币单抽, 2-铜币十连抽；3-元宝单抽，4-元宝十连抽
		
		int sqlType = 0;
		int timeInterval = 0;
		if(reqType == 1 || reqType == 2){
			sqlType = TanBaoData.tongBi_type;
			timeInterval = TanBaoData.tongBi_CD;
		}else if(reqType == 3 || reqType == 4){ // 元宝抽奖类型
			sqlType = TanBaoData.yuanBao_type;
			timeInterval = TanBaoData.yuanBao_CD;
		}else{
			log.error("请求探宝类型不存在");
			return;
		}
		ExploreMine mine = getMineByType(jz.id, sqlType);
		if (mine == null) {
			mine = intMineForType(jz.id, sqlType);
		}else{
			// 铜币探宝需要reset，元宝探宝没有次数限制，所以不需要
			if(sqlType == TanBaoData.tongBi_type) resetExploreMine(mine);
		}
	
		int money = getCost(reqType);
		int chouCishu = 0;
		
		// 11 铜币免费单抽， 12铜币付费单抽， 13- 铜币十连抽  
		// 21 元宝免费单抽，22元宝付费单抽， 23- 元宝十连抽 
		int chouType = 0; 
		switch(reqType){ // 1-铜币单抽, 2-铜币十连抽；3-元宝单抽，4-元宝十连抽
			case 1:
				int cd = getCD(mine.lastFreeGetTime, TanBaoData.tongBi_CD);
				int remianTimes = TanBaoData.tongBi_all_free_times - mine.usedFreeNumber;
				if(cd <= 0 && remianTimes > 0){
					chouType = 11;
				}else{
					boolean ok = isTongBiOk(jz, money, session);
					if(!ok){
						sendExploreFailedMessage(session, tongBi_little);
						log.error("君主:{}铜币单抽失败，铜币不足", jz.id);
						return ;
					}
					chouType = 12;
				}
				chouCishu = 1;
				break;
			case 2:
				boolean ok = isTongBiOk(jz, money, session);
				if(ok){
					chouType = 13;
				}else{
					sendExploreFailedMessage(session, tongBi_little);
					log.error("君主:{}铜币10抽失败，铜币不足", jz.id);
					return ;
				}
				chouCishu = 10;
				break;
			case 3:
				cd = getCD(mine.lastFreeGetTime, TanBaoData.yuanBao_CD);
				if(cd <= 0){
					chouType = 21;
				}else{
					ok = isYuanBaoOk(jz, money, session);
					if(ok){
						chouType = 22;
					}else{
						sendExploreFailedMessage(session, yuanBao_little);
						log.error("君主:{}元宝单抽失败，元宝不足", jz.id);
						return ;
					}
				}
				chouCishu = 1;
				break;
			case 4://元宝十连抽
				// 首次十连抽免费
				if(mine.tenChouClickNumber == 0){
					ok = true;
					//免费十连不触发十连副本
					session.setAttribute("FreeShiLian", Boolean.TRUE);
				}else{
					ok = isYuanBaoOk(jz, money, session);
				}
				if(ok){
					chouType = 23;
				}else{
					sendExploreFailedMessage(session, yuanBao_little);
					log.error("君主:{}元宝十连抽失败，元宝不足", jz.id);
					return ;
				}
				chouCishu = 10;
				break;
			default:
				log.error("君主:{}探宝失败，无效探宝类型{}", jz.id, reqType);
				return ;
		}
		/*
		 * 获取奖励，并且保存抽奖记录
		 */
		List<Integer> awarL = getAwardIds(mine, chouType);
		HibernateUtil.save(mine);
		
		List<AwardTemp> awards = new ArrayList<AwardTemp>();
		for(Integer awardId: awarL){
			AwardTemp award = AwardMgr.inst.calcAwardTemp(awardId);
			if(award != null) awards.add(award);
		}

		Map<Integer, int[]> fenjieM = mibaoAward(awards, jz);
		// 先增添奖励，再筛选是否碎成碎片
		session.setAttribute("inTanBaoGiveReward");
		for (AwardTemp awa : awards) {
			AwardMgr.inst.giveReward(session, awa, jz, false);
			log.info("{}探宝奖励{}:{}:{}",jz.id,awa.getItemType(),awa.getItemId(),awa.getItemNum());
		}
		session.removeAttribute("inTanBaoGiveReward");
		ExploreResp.Builder resp = ExploreResp.newBuilder();
		resp.setSuccess(0);
		sendExploreAwardInfo(resp, awards, fenjieM);
		TypeInfo.Builder inf =TypeInfo.newBuilder();
		if(reqType == 1 || reqType == 2){
			inf.setMoney(jz.tongBi);
		}else{
			inf.setMoney(jz.yuanBao);
		}
		if(reqType == 1){
			// 铜币免费单抽发送剩余次数
			inf.setRemainFreeCount(TanBaoData.tongBi_all_free_times - mine.usedFreeNumber);
		}
		inf.setCd(getCD(mine.lastFreeGetTime, timeInterval));
		resp.setInfo(inf);
		session.write(resp.build());
		log.info("君主{}探宝，类型{},成功", jz.id, reqType);
		ActLog.log.FineGem(jz.id, jz.name, ActLog.vopenid, reqType, 0, "0", 0, "0", 0);

		// 一次探宝任务完成
		for (AwardTemp a : awards) {
			if (a.getItemId() == TaskData.tanbao_itemId_1){
				EventMgr.addEvent(ED.get_item_finish, new Object[] { jz.id,
						a.getItemId() });
			}
		}
		// 每日任务中记录探宝成功chouCishu次
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				jz.id, DailyTaskConstants.tanbao_5_id, chouCishu));
		EventMgr.addEvent(ED.TAN_BAO_JIANG_LI, new Object[]{jz,session,awards});
		/*
		 * 限时活动（成就）触发事件
		 */
		if(chouType == 21 || chouType == 22){
			EventMgr.addEvent(ED.tanbao_oneTimes, new Object[]{jz.id, mine.danChouClickNumber});
		}else if(chouType == 23){
			EventMgr.addEvent(ED.tanbao_tenTimes, new Object[]{jz.id, mine.tenChouClickNumber});
		}else if(chouType == 11 || chouType == 12){
			EventMgr.addEvent(ED.tongbi_oneTimes, new Object[]{jz.id, mine.danChouClickNumber});
		}else if(chouType == 13){
			EventMgr.addEvent(ED.tongbi_tenTimes, new Object[]{jz.id, mine.tenChouClickNumber});
		}
	}


	
	/**
	 * 元宝抽：
		判断是否是首次免费，是的话，9000；不是的话，正常逻辑
		判断是否是首次付费，是的话，9001，不是的话，正常逻辑
		正常逻辑：
		{
		    次数+1
		    若此次不保底，9002
		    若此次保底，是否是首次保底，是的话9004，不是的话9003
		}
		
		铜币抽：
		判断是否是首次免费，是的话，9100；不是的话，正常逻辑
		正常逻辑：
		{
		    次数+1
		    若此次不保底，9101
		    若此次保底，9102
		}
	 * @param m
	 * @param reqType
	 * @return
	 */
	public List<Integer> getAwardIds(ExploreMine m, int reqType){
		ArrayList<Integer> list = new ArrayList<Integer>();
		// 11 铜币免费单抽， 12铜币付费单抽， 13- 铜币十连抽  
		// 21 元宝免费单抽， 22元宝付费单抽，23- 元宝十连抽 
		switch(reqType){
		case 11:
			if(m.historyFree == 0){
				list.add(getTongFirstFreeAwardId(m));
				log.info("铜币免费单抽：第一次historyFree == 0 执行");
			}
			else{
				list.add(getTongNormalAwardId(m)); 
				m.historyFree += 1;
			}
			m.lastFreeGetTime = new Date();
			m.usedFreeNumber += 1;
			m.danChouClickNumber += 1;
			break;
		case 12:
			list.add(getTongNormalAwardId(m));
			m.historyFree += 1;
			m.danChouClickNumber += 1;
			break;
		case 13: // 铜币十连抽
			/*
			 * 每次铜币10连抽行为都是单独的抽奖tenToEx.totalProbability == 0，不和单抽关联
			 * 20160118 from 策划 
			 */
			ExploreMine tenToEx = new ExploreMine();
			tenToEx.totalProbability = 0;
			for(int i=0; i<10; i++){
				list.add(getTongNormalAwardId(tenToEx));
			}
			m.tenChouClickNumber += 1;
			break;
		case 21:
			if(m.historyFree == 0){
				list.add(getYuanFirstFreeAwardId(m));
				log.info("元宝免费单抽：第一次historyFree == 0 执行");
			}else {
				list.add(getYuanNormalAwardId(m));
			}
			m.lastFreeGetTime = new Date();
			// 为了限时活动记录
			m.danChouClickNumber += 1;
			break;
		case 22:
			if(m.historyPay == 0){
				list.add(getYuanFirstPayAwardId(m));
				log.info("元宝付费单抽：第一次historyPay == 0 执行");
			}else{
				list.add(getYuanNormalAwardId(m));
			}
			// 为了限时活动记录
			m.danChouClickNumber += 1;
			break;
		case 23: //元宝十连抽
			/*
			 * 元宝 十连抽 不走 同意概率
			 * 每一次 10连抽行为都重新从 totalProbability==0 开始算起。
			 */
			if(m.historyPay == 0){
				list.add(getYuanFirstPayAwardId(m));
				log.info("元宝十连抽：第一次historyPay == 0 执行");
	
				ExploreMine tenEx = new ExploreMine();
				tenEx.totalProbability = 0;
				tenEx.historyBaoDi = m.historyBaoDi;
				for(int i=0; i<9; i++){
					list.add(getYuanNormalAwardId(tenEx));
				}
				m.historyBaoDi = tenEx.historyBaoDi;
			}else{
				ExploreMine tenEx = new ExploreMine();
				tenEx.totalProbability = 0;
				tenEx.historyBaoDi = m.historyBaoDi;
				for(int i=0; i<10; i++){
					list.add(getYuanNormalAwardId(tenEx));
				}
				m.historyBaoDi = tenEx.historyBaoDi;
			}
			// 为了限时活动记录
			m.tenChouClickNumber += 1;
			break;
		}
		return list;
	}
	
	public int getYuanFirstFreeAwardId(ExploreMine m){
		m.historyFree += 1;
		return TanBaoData.yuanBao_first_free_awardId;
	}

	public int getYuanFirstPayAwardId(ExploreMine m){
		m.historyPay += 1;
		return TanBaoData.yuanBao_first_pay_awardId;
	}

	public int getYuanNormalAwardId(ExploreMine mine){
		// 对于该次抽奖，累计概率+125
		mine.totalProbability += yuanBao_goodAwardProbability;
	
		int normalId = TanBaoData.yuanBao_normal_awardId;
		int goodId = TanBaoData.yuanBao_good_awardId;

		int awardId = getAwardId(mine.totalProbability, goodId, normalId);
		/*
		 *  如果获取了好的奖励，可理解为提前预支了1000 - mine.totalProbability的可能性，
		 *  所以累计概率为减去总概率1000，因此累计概率有可能是负的
		 */
		if(goodId == awardId){
			mine.totalProbability = mine.totalProbability - allProbability;
		}

		// 处理首次保底奖励
		if(mine.historyBaoDi == 0 && goodId == awardId){
			awardId =  TanBaoData.yuanBao_first_good_awardId;
			mine.historyBaoDi += 1;
			log.info("元宝抽奖：第一次保底historyBaoDi == 0 执行");
		}
		return awardId;
	}

	
	public int getTongFirstFreeAwardId(ExploreMine mine){
		mine.historyFree += 1;
		return TanBaoData.tongBi_first_free_awardId;
	}
	public int getTongNormalAwardId(ExploreMine mine){
		// 对于该次抽奖，累计概率+125
		mine.totalProbability += tongBi_goodAwardProbability;
		int goodId = TanBaoData.tongBi_good_awardId;
		int normalId = TanBaoData.tongBi_normal_awardId;
		int awardId = getAwardId(mine.totalProbability, goodId, normalId);
		if(goodId == awardId){
			mine.totalProbability = mine.totalProbability - allProbability;
		}
		return awardId;
	}

	public int getAwardId(int totalProbability, int goodId, int nomalId){
		// [1, 1000]
		int num = MathUtils.getRandomInMax(1, allProbability);
		if(num <= totalProbability){
			return goodId;
		}
		return nomalId;
	}

	public void resetExploreMine(ExploreMine m){
		if(m == null){
			return;
		}
		if(m.id%space ==TanBaoData.tongBi_type){
			if(DateUtils.isTimeToReset(m.lastFreeGetTime, CanShu.REFRESHTIME_PURCHASE)){
				int cd = TanBaoData.tongBi_CD * 1000;
				m.usedFreeNumber = 0;
				m.lastFreeGetTime =
						new Date(System.currentTimeMillis() - cd);
				HibernateUtil.save(m);
				log.info("君主：{}第二天重置铜币探宝：", m.id/space);
			}
		}
	}
	
//	/**
//	 * 获取一定会获得物品
//	 * 
//	 * @Title: getCertainAward
//	 * @Description:
//	 * @param type
//	 * @return
//	 */
//	protected AwardTemp getCertainAward(int type) {
//		int cerItemId = 0;
//		AwardTemp certainAward = new AwardTemp();
//		if (type == ExploreConstant.FREE) {
//			cerItemId = ExploreConstant.IRON_ID;
//		} else {
//			cerItemId = ExploreConstant.COPPER_ID;
//		}
//		ItemTemp certainItem = itemTempMap.get(cerItemId);
//		certainAward.setAwardId(11111);
//		certainAward.setItemId(certainItem.getId());
//		certainAward.setItemType(ExploreConstant.QIANG_HUA_TYPE);
//		if (type == ExploreConstant.PAY || type == ExploreConstant.GUILD_2) {
//			certainAward.setItemNum(10);
//		} else
//			certainAward.setItemNum(1);
//		return certainAward;
//	}
	

//	/**
//	 * 
//	 * @Title: getAwardId
//	 * @Description:
//	 * @param type
//	 * @param number
//	 * @return
//	 */
//	protected AwardTemp getTrulyAwards(int type, int number, ExploreMine mine) {
//		AwardTemp choiceAward = new AwardTemp();
//		switch (type) {
//		case TanBaoData.FREE:
//			if (number == 0) {
//				choiceAward = AwardMgr.inst
//						.calcAwardTemp(TanBaoData.FREE_AWARDID_0);
//			} else {
//				choiceAward = AwardMgr.inst
//						.calcAwardTemp(TanBaoData.FREE_AWARDID);
//			}
//			break;
//		/*
//		 * 单抽和联盟单抽 5次一组，必抽的一组好的奖励(除了首抽)
//		 */
//		// 首抽不一样 别的抽取与下面几种类型相同
//		case TanBaoData.SIGLE:
//			if (number == 0) {
//				choiceAward = AwardMgr.inst
//						.calcAwardTemp(TanBaoData.SIGLE_AWARDID_0);
//				break;
//			}
//			choiceAward = getChoiceAward(number, mine);
//			break;
//		case TanBaoData.GUILD_1:
//			choiceAward = getChoiceAward(number, mine);
//			break;
//		}
//		return choiceAward;
//	}

//	/**
//	 * 联盟单抽或者付费单抽获取的奖励
//	 * 
//	 * @Title: getChoiceAward
//	 * @Description:
//	 * @param number
//	 * @param mine
//	 * @return
//	 */
//	public AwardTemp getChoiceAward(int number, ExploreMine mine) {
//		AwardTemp temp = null;
//		/*
//		 * awardNumber次中的最后一次抽奖
//		 */
//		if (number != 0 && number % awardNumber == 0) {
//			if (mine.hasGoodAwar) {
//				temp = AwardMgr.inst
//						.calcAwardTemp(TanBaoData.SIGLE_AWARDID_1);
//				// awardNumber设置为1组，对下一组进行重置hasGoodAwar的值
//				mine.hasGoodAwar = false;
//				HibernateUtil.save(mine);
//			} else {
//				temp = AwardMgr.inst
//						.calcAwardTemp(TanBaoData.SIGLE_AWARDID_2);
//			}
//			return temp;
//		}
//		if (mine.hasGoodAwar) {
//			temp = AwardMgr.inst.calcAwardTemp(TanBaoData.SIGLE_AWARDID_1);
//		} else {
//			int id = getRandomId();
//			if (id == TanBaoData.SIGLE_AWARDID_2) {
//				mine.hasGoodAwar = true;
//				HibernateUtil.save(mine);
//			}
//			temp = AwardMgr.inst.calcAwardTemp(id);
//		}
//		return temp;
//	}

//	public int getRandomId() {
//		int[][] array = { { TanBaoData.SIGLE_AWARDID_1, 875 },
//				{ TanBaoData.SIGLE_AWARDID_2, 125 } };
//		int result = MathUtils.getRandom(array, 1000);
//		return result;
//	}

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
//	public List<AwardTemp> getTenAwards_mustHasGood() {
//		List<AwardTemp> list = new ArrayList<AwardTemp>();
//		int[] ids1 = getRandomAwardId();
//		AwardTemp temp = null;
//		AwardTemp flag = null;
//		int stop = 0;
//		for (int i = 0; i < awardNumber; i++) {
//			stop = 0;
//			do {
//				temp = AwardMgr.inst.calcAwardTemp(ids1[i]);
//			} while (temp == null && stop++ < 100);
//			list.add(temp);
//			if (temp != null && temp.getItemType() == 4) {
//				flag = temp;
//			}
//		}
//		for (int i = 0; i++ < 2;) {
//			stop = 0;
//			do {
//				temp = AwardMgr.inst.calcAwardTemp(getRandomId());
//				// 防止出现null或者和之前相同的秘宝
//			} while (temp == null
//					|| (flag != null && temp.getItemType() == 4
//							&& temp.getItemId() == flag.getItemId() && stop++ < 100));
//			list.add(temp);
//			if (temp.getItemType() == 4) {
//				flag = temp;
//			}
//		}
//		return list;
//	}
//
//	/**
//	 * 得到一个数组，awardNumber个数字中有且只有一个不一样的，先逐个随机得到，若得不到，则一定赋予
//	 * 
//	 * @Title: getRandomAwardId5
//	 * @Description:
//	 * @return
//	 */
//	public int[] getRandomAwardId() {
//		int[] ids = new int[awardNumber];
//		ids[0] = getRandomId();
//		int which = awardNumber - 1;
//		for (int i = 0; i < awardNumber; i++) {
//			if (ids[i] == TanBaoData.SIGLE_AWARDID_2) {
//				which = i;
//				break;
//			} else {
//				if (i < awardNumber - 1) {
//					ids[i + 1] = getRandomId();
//				}
//			}
//		}
//		if (which == awardNumber - 1) {
//			ids[which] = TanBaoData.SIGLE_AWARDID_2;
//		}
//		if (which < awardNumber - 1) {
//			for (int i = which + 1; i < awardNumber; i++) {
//				ids[i] = TanBaoData.SIGLE_AWARDID_1;
//			}
//		}
//		return ids;
//	}


	public boolean isTongBiOk(JunZhu jz, int money, IoSession session){
		if(jz.tongBi < money){
			return false; // 铜币不足
		}
		jz.tongBi -= money;
		HibernateUtil.save(jz);
		log.info("玩家id{},姓名 {}, 购买 探宝, 花费铜币{}个", jz.id, jz.name,
				 money);
		// 同步君主元宝信息
		JunZhuMgr.inst.sendMainInfo(session);
		return true;
	}
	
	public boolean isYuanBaoOk(JunZhu jz, int money, IoSession session){
		if (jz.yuanBao < money) {
			return false;
		}
		YuanBaoMgr.inst.diff(jz, -money, 0, money,
				YBType.YB_BUY_TANBAO_CISHU, "购买探宝次数");
		HibernateUtil.save(jz);
		log.info("玩家id{},姓名 {}, 购买 探宝, 花费元宝{}个", jz.id, jz.name,
				 money);
		// 同步君主元宝信息
		JunZhuMgr.inst.sendMainInfo(session);
		return true;
	}
//	public byte isBuySuccess(JunZhu jz, int type, IoSession session) {
//		int money = getCost(type);
//		if (type == 1 || type == 2) {
//			if(jz.tongBi < money){
//				return tongBi_little; // 铜币不足
//			}
//			jz.tongBi -= money;
//			// 同步君主元宝信息
//			JunZhuMgr.inst.sendMainInfo(session);
//		} else if(type == 3 || type == 4){
//			if (jz.yuanBao < money) {
//				return yuanBao_little; // 元宝不足
//			}
//			YuanBaoMgr.inst.diff(jz, -money, 0, money,
//					YBType.YB_BUY_TANBAO_CISHU, "购买探宝次数");
//			HibernateUtil.save(jz);
//			log.info("玩家id{},姓名 {}, 购买 {}类型的探宝一次, 花费元宝{}个", jz.id, jz.name,
//					type, money);
//			// 同步君主元宝信息
//			JunZhuMgr.inst.sendMainInfo(session);
//		}else{
//			log.info("玩家id{}探宝类型：{}不存在", jz.id, type);
//			return wrong;
//		}
//		return money_ok;
//	}


	public int getCost(int type) {
		int purchaseId = 0;
		switch (type) {
		case 1:
			purchaseId = TanBaoData.tongBi_pay_sigle;
			break;
		case 2:
			purchaseId = TanBaoData.tongBi_pay_ten;
			break;
		case 3:
			purchaseId = TanBaoData.yuanBao_pay_sigle;
			break;
		case 4:
			purchaseId = TanBaoData.yuanBao_pay_ten;
			break;
		}
		Purchase p = purchasesMap.get(purchaseId);
		if (p == null)
			return 0;
		return p.getYuanbao();
	}

//	/**
//	 * 获取奖励
//	 * 
//	 * @Title: getAwards
//	 * @Description:
//	 * @param type
//	 * @param number
//	 * @return
//	 */
//	protected List<AwardTemp> getAwards(int type, int number, ExploreMine mine) {
//		List<AwardTemp> awards = new ArrayList<AwardTemp>();
////	awards.add(getCertainAward(type));
//		if (type == TanBaoData.PAY || type == TanBaoData.GUILD_2) {
//			awards.addAll(getTenAwards_mustHasGood());
//		} else {
//			awards.add(getTrulyAwards(type, number, mine));
//		}
//		return awards;
//	}



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
	public void sendExploreAwardInfo(ExploreResp.Builder resp,
			List<AwardTemp> awards, Map<Integer, int[]> fenjieM) {
		boolean isHave = false;
		if (fenjieM != null && fenjieM.size() != 0) {
			isHave = true;
		}
		qxmobile.protobuf.Explore.Award.Builder award = null;
		int size = awards.size();
		for (int index = 0; index < size; index++) {
			AwardTemp awa = awards.get(index);
			award = qxmobile.protobuf.Explore.Award.newBuilder();
			award.setItemId(awa.getItemId());
			award.setItemType(awa.getItemType());
			int[] piece = fenjieM.get(award.getItemId());
			if (isHave && piece != null) {
//				// 分解的碎片id
//				award.setPieceId(piece[0]);
				// 分解的碎片数量
				award.setPieceNumber(piece[1]);
//				// 分解的碎片的读表类型
//				award.setPieceType(piece[2]);
			}
			// 只是秘宝的星级(读表产生的初始星级)
			if (award.getItemType() == 4) {
				MiBao mibao = MibaoMgr.mibaoMap.get(award.getItemId());
				award.setMiBaoStar(mibao.getInitialStar());
			}
			award.setItemNumber(awa.getItemNum());
			resp.addAwardsList(award);
		}
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
		resp.setSuccess(failedType);
		session.write(resp.build());
	}
//
//	/**
//	 * 初始玩家的所有矿
//	 * 
//	 * @Title: initJunZhuMine
//	 * @Description:
//	 * @param junzhuId
//	 * @return
//	 */
//	protected List<ExploreMine> initMines(long junzhuId) {
//		List<ExploreMine> mines = new ArrayList<ExploreMine>();
//		mines.add(new ExploreMine(junzhuId * space + TanBaoData.FREE,
//				TanBaoData.FREE));
//		mines.add(new ExploreMine(junzhuId * space + TanBaoData.SIGLE,
//				TanBaoData.SIGLE));
//		mines.add(new ExploreMine(junzhuId * space + TanBaoData.PAY,
//				TanBaoData.PAY));
//		return mines;
//	}

	/**
	 * 初始化一个类型为 @type 的矿源
	 * 
	 * @Title: intMineForType
	 * @Description:
	 * @param jzI
	 * @param type
	 * @return
	 */
	public ExploreMine intMineForType(long jzI, int sqlType) {
		long id = jzI * space + sqlType;
		ExploreMine mine = new ExploreMine();
		mine.id = id;
		mine.lastFreeGetTime = null;
		mine.totalProbability = 0;
		mine.usedFreeNumber = 0;
		mine.tenChouClickNumber = 0;
		HibernateUtil.insert(mine);
		log.info("君主id:{}, 初始化并且持久化类型是:{}的探宝数据成功", jzI, sqlType);
		return mine;
	}

	public ExploreMine getMineByType(long junzhuId, int type) {
		long id = junzhuId * space + type;
		ExploreMine mine = HibernateUtil.find(ExploreMine.class, id);
		return mine;
	}

//	public ExploreMine getMineFromList(List<ExploreMine> ms, int type) {
//		if (ms == null)
//			return null;
//		for (ExploreMine m : ms) {
//			if (m.getType() == type) {
//				return m;
//			}
//		}
//		return null;
//	}

	public List<ExploreMine> getMineList(long jid) {
		long start = jid * space;
		long end = start + space;
		List<ExploreMine> list = HibernateUtil.list(ExploreMine.class,
				"where id>=" + start + " and id<" + end);
		return list;
	}

//	public void setMineList(List<ExploreMine> mineList, long junZhuId) {
//		ExploreMine m1 = null;
//		ExploreMine m2 = null;
//		ExploreMine m3 = null;
//		ExploreMine m4 = null;
//		ExploreMine m5 = null;
//		for (ExploreMine m : mineList) {
//			switch ((byte) m.getType()) {
//			case TanBaoData.FREE:
//				m1 = m;
//				break;
//			case TanBaoData.SIGLE:
//				m2 = m;
//				break;
//			case TanBaoData.PAY:
//				m3 = m;
//				break;
//			case TanBaoData.GUILD_1:
//				m4 = m;
//				break;
//			case TanBaoData.GUILD_2:
//				m5 = m;
//				break;
//			}
//		}
//		if (m1 == null) {
//			m1 = new ExploreMine(junZhuId, TanBaoData.FREE);
//			mineList.add(m1);
//		}
//		if (m2 == null) {
//			m2 = new ExploreMine(junZhuId, TanBaoData.SIGLE);
//			mineList.add(m2);
//		}
//		if (m3 == null) {
//			m3 = new ExploreMine(junZhuId, TanBaoData.PAY);
//			mineList.add(m3);
//		}
//		if (m4 == null) {
//			m4 = new ExploreMine(junZhuId, TanBaoData.GUILD_1);
//			mineList.add(m4);
//		}
//		if (m5 == null) {
//			m5 = new ExploreMine(junZhuId, TanBaoData.GUILD_2);
//			mineList.add(m5);
//		}
//	}

	@Override
	public void proc(Event e) {
		switch (e.id) {
		case ED.REFRESH_TIME_WORK:
			IoSession session = (IoSession) e.param;
			if(session == null){
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz == null){
				break;
			}
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.tanBao, jz.id, jz.level);
			if(!isOpen){
				break;
			}
			/*
			 *  -1 没有免费单抽记录； 1-可以免费单抽， 0-不能抽奖
			 */
			byte freeRecord_yuanbao = -1; 
			byte freeRecord_tongbi = -1;
			List<ExploreMine> list = getMineList(jz.id);
			if (list != null && list.size() != 0) {
				for (ExploreMine mine : list) {
					int type = (int)(mine.id % space);
					if( type == TanBaoData.tongBi_type){
						resetExploreMine(mine);
						int cd = getCD(mine.lastFreeGetTime,  TanBaoData.tongBi_CD);
						int remianTimes = TanBaoData.tongBi_all_free_times - mine.usedFreeNumber;
						if(cd <= 0 && remianTimes > 0){
							freeRecord_tongbi = 1;	//可以抽奖
						}else{
							freeRecord_tongbi = 0;	//不可以抽奖
						}
					}else if(type == TanBaoData.yuanBao_type){
						int cd = getCD(mine.lastFreeGetTime,  TanBaoData.yuanBao_CD);
						if(cd <= 0){
							freeRecord_yuanbao = 1;	//可以抽奖
						}else{
							freeRecord_yuanbao = 0;	//不可以抽奖
						}
					}
				}
			}
			// 从没有进行过免费单抽，那么可以单抽
			if(freeRecord_yuanbao == -1){
				freeRecord_yuanbao = 1;
			}
			if(freeRecord_tongbi == -1){
				freeRecord_tongbi = 1;
			}
			// 免费单抽有免费机会，可以抽奖
			if(freeRecord_tongbi == 1){
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.tanBao_free_tongbi);
			}
			if(freeRecord_yuanbao == 1){
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.tanBao_free_yuanbao);
			}
			break;
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
}
