package com.qx.card;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Cards.BuyCardBagResp;
import qxmobile.protobuf.Cards.CardItem;
import qxmobile.protobuf.Cards.OpenCardBagResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.hero.HeroMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.world.Mission;

public class CardMgr implements Runnable{
	public static CardMgr inst;
	public static int cardId = 900008;//	卡包;
	public static int[] packageIds = new int[]{901,901,901,902,903};
	public static Logger log = LoggerFactory.getLogger(CardMgr.class);
	
	private static Logger eLogger = LoggerFactory.getLogger(CardMgr.class);
	private LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0,null,null);
	
	public CardMgr(){
		inst = this;
	}

	public void startMissionThread() {
//		new Thread(this, "CardMgr No.1").start();
	}

	public void enqueue(int code, IoSession session, Builder builder) {
//		Mission mission = new Mission(code,session,builder);
//		missions.add(mission);
	}
	public void shutdown(){
		missions.add(exit);
	}
	@Override
	public void run() {
		while(true){
			Mission mission = null;
			try {
				mission = missions.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}
			if(mission == exit){
				break;
			}
				try {
					completeMission(mission);
				} catch (Throwable e) {
					eLogger.error("completeMission error {}", mission.code);
					eLogger.error("run方法异常", e);
				}
		}
		log.info("退出CardMgr");
	}

	private void completeMission(Mission mission) {
		if (mission == null) {
			eLogger.error("mission is null...");
			return;
		}
		int code = mission.code;
		IoSession session = mission.session;
		Builder builder = mission.builer;
		switch(code){
			case PD.BUY_CARDBAG_REQ:
				buyCardBag(code, session, builder);
				break;
			case PD.OPEN_CARDBAG_REQ:
				openCardBag(code, session, builder);
				break;
		}
	}

	public void openCardBag(int code, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhu id");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("没有找到君主");
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		BagGrid useBg = null;
		for(BagGrid bg : bag.grids){
			if(bg.itemId == cardId && bg.cnt>0){
				useBg = bg;
				break;
			}
		}
		if(useBg == null){
			sendError(session, "您没有卡包，请先购买。");
			return;
		}
		useBg.cnt -= 1;
		log.info("扣除一个卡包 from {}", junZhuId);
		if(useBg.cnt<=0){
			useBg.cnt = useBg.itemId = 0;
		}
		HibernateUtil.save(useBg);
		OpenCardBagResp.Builder ret = OpenCardBagResp.newBuilder();
		AwardMgr mgr = AwardMgr.inst;
		for(int aId : packageIds){
			AwardTemp calcV = mgr.calcAwardTemp(aId);
			if(calcV == null){
				log.error("没有结果，{}", aId);
				continue;
			}
			CardItem.Builder item = CardItem.newBuilder();
			item.setType(calcV.getItemType());
			item.setNum(calcV.getItemNum());
			item.setItemId(calcV.getItemId());
			mgr.giveReward(session, calcV, jz);
			if(calcV.getItemType() == 7){//武将
				log.info("武将protoId {}",calcV.getItemId());
				HeroProtoType proto = HeroMgr.tempId2HeroProto.get(calcV.getItemId());
				if(proto == null){
					log.error("武将没有找到{}",calcV.getItemId());
				}else{
					item.setName(HeroService.getNameById(proto.getHeroName()+""));
				}
			}else{
				BaseItem it = TempletService.itemMap.get(calcV.getItemId());
				if(it != null){
					item.setName(HeroService.getNameById(it.getName()));
				}
			}
			ret.addItems(item);
		}
		BagMgr.inst.sendBagInfo(session, bag);
		session.write(ret.build());
	}
	public void sendError(IoSession session, String msg) {
		if(session == null){
			log.warn("session is null: {}",msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());		
	}
	public void buyCardBag(int code, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhu id");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null)return;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
		int cnt = 1;
		BagMgr.inst.addItem(bag, cardId, cnt, -1,jz.level, "购买卡包");
		BuyCardBagResp.Builder ret = BuyCardBagResp.newBuilder();
		ret.setRet(1);
		ret.setMsg("购买成功");
		BagMgr.inst.sendBagInfo(session, bag);
		session.write(ret.build());
	}
}
