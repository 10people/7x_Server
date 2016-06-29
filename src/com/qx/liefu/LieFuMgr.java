package com.qx.liefu;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LieFuTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.SessionAttKey;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.GameTaskMgr;
import com.qx.task.WorkTaskBean;
import com.qx.util.RandomUtil;

import qxmobile.protobuf.LieFuProto.LieFuActionInfo;
import qxmobile.protobuf.LieFuProto.LieFuActionInfoResp;
import qxmobile.protobuf.LieFuProto.LieFuActionReq;
import qxmobile.protobuf.LieFuProto.LieFuActionResp;
import qxmobile.protobuf.LieFuProto.LieFuAward;

public class LieFuMgr {
	public static LieFuMgr inst;
	public Logger logger = LoggerFactory.getLogger(LieFuMgr.class);
	
	public Map<Integer, LieFuTemp> lieFuTempMap = new HashMap<>();
	
	/** 按钮可使用状态 **/
	public static final int STATE_USEABLE = 1;
	/** 按钮不可使用状态 **/
	public static final int STATE_NOT_USE = 0;
	
	public LieFuMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<LieFuTemp> lieFuTempList = TempletService.listAll(LieFuTemp.class.getSimpleName());
		Map<Integer, LieFuTemp> lieFuTempMap = new HashMap<>();
		for(LieFuTemp liefu : lieFuTempList) {
			lieFuTempMap.put(liefu.id, liefu);
		}
		this.lieFuTempMap = lieFuTempMap;
	}
	
	public void actionInfoRequest(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("猎符操作失败，找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		
		LieFuBean lieFuBean = HibernateUtil.find(LieFuBean.class, junZhu.id);
		if(lieFuBean == null) {
			lieFuBean = new LieFuBean();
			lieFuBean.junzhuId = junZhu.id;
			HibernateUtil.insert(lieFuBean);
		} else {
			boolean refreshTime = DateUtils.isTimeToReset(lieFuBean.lastActionTime, CanShu.REFRESHTIME);
			if(refreshTime) {
				lieFuBean.lastActionTime = new Date();
				lieFuBean.type1UseTimes = 0;
				lieFuBean.type2UseTimes = 0;
				lieFuBean.type3UseTimes = 0;
				lieFuBean.type4UseTimes = 0;
				lieFuBean.dayUseTimes = 0;
				HibernateUtil.save(lieFuBean);
			}
		}
		
		int freeTimes = getFreeTimes(1, lieFuBean);
		LieFuActionInfoResp.Builder response = LieFuActionInfoResp.newBuilder();
		for(Map.Entry<Integer, LieFuTemp> entry : lieFuTempMap.entrySet()) {
			LieFuTemp lieFuTemp = entry.getValue();
			LieFuActionInfo.Builder actionBuilder = LieFuActionInfo.newBuilder();
			actionBuilder.setType(lieFuTemp.id);
			int cost = freeTimes > 0 ? 0 : lieFuTemp.cost;
			actionBuilder.setCost(cost);
			actionBuilder.setState(getActionState(lieFuTemp.id, lieFuBean));
			actionBuilder.setFreeTimes(freeTimes);
			response.addLieFuActionInfo(actionBuilder);
		}
		session.write(response.build());
	}
	
	public int getFreeTimes(int type, LieFuBean lieFuBean) {
		int freeTimes = 0;
		switch(type) {
		case 1:								
			freeTimes = lieFuBean.dayUseTimes > 0 ? 0 : 1;
			break;
//		default:
//			logger.error("获取猎符免费次数失败，猎符类型:{}错误", type);
//			break;
		}
		return freeTimes;
	}
	
	public int getTotalTimes(int type, LieFuBean lieFuBean) {
		int totalTimes = 0;
		switch(type) {
		case 1:								
			totalTimes = lieFuBean.type1TotalTimes;
			break;
		case 2:
			totalTimes = lieFuBean.type2TotalTimes;
			break;
		}
		return totalTimes;
	}
	
	public void addTotalTimes(int type, LieFuBean lieFuBean,int times) {
		switch(type) {
		case 1:								
			lieFuBean.type1TotalTimes += times;
			break;
		case 2:
			lieFuBean.type2TotalTimes += times;
			break;
		}
	}

	public int getActionState(int type, LieFuBean lieFuBean) {
		int state = STATE_NOT_USE;
		switch(type) {
		case 1:								// 一直都能用
			state = STATE_USEABLE;
			break;
		case 2:
			state = lieFuBean.type2State;
			break;
		case 3:
			state = lieFuBean.type3State;
			break;
		case 4:
			state = lieFuBean.type4State;
			break;
		default:
			logger.error("获取猎符状态失败，猎符类型:{}错误", type);
			break;
		}
		return state;
	}

	public void doAction(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("猎符操作失败，找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		
		LieFuActionReq.Builder request = (qxmobile.protobuf.LieFuProto.LieFuActionReq.Builder) builder; 
		int type = request.getType();
		
		LieFuActionResp.Builder response = LieFuActionResp.newBuilder();
		
		LieFuTemp lieFuTemp = lieFuTempMap.get(type);
		if(lieFuTemp == null) {
			logger.error("猎符操作失败，找不到类型id为:{}的配置", type);
			response.setResult(2);
			session.write(response.build());
			return;
		}
		
		LieFuBean lieFuBean = HibernateUtil.find(LieFuBean.class, junZhu.id);
		if(lieFuBean == null) {
			lieFuBean = new LieFuBean();
			lieFuBean.junzhuId = junZhu.id;
			HibernateUtil.insert(lieFuBean);
		}
		
		int state = getActionState(type, lieFuBean);
		if(state == STATE_NOT_USE) {
			logger.error("猎符操作失败，君主:{},猎符类型:{}还不能使用", junZhu.id, type);
			return;
		}
				
		boolean free = false;
		if(lieFuBean.dayUseTimes <= 0) {
			free = true;
		}
		
		if(!free && junZhu.tongBi < lieFuTemp.cost) {
			logger.error("猎符操作失败，猎符类型:{},君主:{}的铜币:{}不足:{}", type, junZhu.id, junZhu.tongBi, lieFuTemp.cost);
			response.setResult(1);
			session.write(response.build());
			return;
		}
		if(!free) {
			junZhu.tongBi -= lieFuTemp.cost;
			HibernateUtil.save(junZhu);
			logger.info("猎符操作成功，君主:{}花费了铜币:{}进行的type:{}的猎符操作", junZhu.id, lieFuTemp.cost, type);
		}
		
		List<AwardTemp> getAwardList = new ArrayList<>(0);
		getAwardList = AwardMgr.inst.getHitAwardList(lieFuTemp.awardID, ",", "=");
		if(type == 2 /*&& getTotalTimes(2, lieFuBean) <= 0*/) {// 蓝色猎符类型第一次，获得特定的符文
			WorkTaskBean taskBean = GameTaskMgr.inst.getTask(junZhu.id, 200065); 
			if(taskBean != null) {
				getAwardList = AwardMgr.inst.getHitAwardList(CanShu.LIEFU_BLUE_FIRSTAWARD, ",", "=");
				logger.info("君主:{}第一次猎符蓝色操作，获得奖励:{}", junZhu.id, getAwardList);
			}
		} else if(type == 1 /*&& getTotalTimes(1, lieFuBean) <= 0*/) {// 绿色猎符类型第一次，获得特定的符文
			WorkTaskBean taskBean = GameTaskMgr.inst.getTask(junZhu.id, 200060); 
			if(taskBean != null) {
				getAwardList = AwardMgr.inst.getHitAwardList(CanShu.LIEFU_GREEN_FIRSTAWARD, ",", "=");
				logger.info("君主:{}第一次猎符绿色 操作，获得奖励:{}", junZhu.id, getAwardList);
			}
		} 
		// 是否会触发下一个
		int nextType = -1;
		int type1First = getTotalTimes(1, lieFuBean);
		// 要是第一次进行猎符操作，必然会触发蓝色猎符类型
		if(type1First <= 0) {
			nextType = 2;
			changeTypeState(lieFuBean, nextType, STATE_USEABLE);
		} else {
			int hitValue = RandomUtil.getRandomNum(100) + 1;
			if(hitValue <= lieFuTemp.promoRate) {
				nextType = getNextActionType(type);
				changeTypeState(lieFuBean, nextType, STATE_USEABLE);
			}
		}
		
		changeTypeState(lieFuBean, type, STATE_NOT_USE);
		changeTypeUseTimes(lieFuBean, type, 1);
		addTotalTimes(type, lieFuBean, 1);
		lieFuBean.totalTimes += 1;
		lieFuBean.dayUseTimes += 1;
		HibernateUtil.save(lieFuBean);
		List<Integer> fuwenIdList = new ArrayList<>();		// 用于检测是否发送广播
		for(AwardTemp getAward : getAwardList) {
			fuwenIdList.add(getAward.getItemId());
			AwardMgr.inst.giveReward(session, getAward, junZhu, false, false);
		}
		JunZhuMgr.inst.sendMainInfo(session);
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		BagMgr.inst.sendBagInfo(session, bag);
		
		
		response.setResult(0);
		response.setType(type);
		response.setTypeState(getActionState(type, lieFuBean));
		response.setNextType(nextType);
		response.setNextTypeState(getActionState(nextType, lieFuBean));
		for(AwardTemp getAward : getAwardList) {
			LieFuAward.Builder awardBuilder = LieFuAward.newBuilder();
			awardBuilder.setItemType(getAward.getItemType());
			awardBuilder.setItemId(getAward.getItemId());
			awardBuilder.setItemNum(getAward.getItemNum());
			response.addLieFuAwardList(awardBuilder);
		}
		session.write(response.build());		
		EventMgr.addEvent(ED.done_lieFu_x, new Object[]{junZhu.id , lieFuBean.totalTimes});
		EventMgr.addEvent(ED.LIEFU_GET_FUWEN, new Object[]{junZhu.name , fuwenIdList});
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(junZhu.id , DailyTaskConstants.lieFu, 1));
	}

	public void changeTypeUseTimes(LieFuBean lieFuBean, int type, int times) {
		switch(type) {
		case 1:
			lieFuBean.type1UseTimes += times;
			break;
		case 2:
			lieFuBean.type2UseTimes += times;
			break;
		case 3:
			lieFuBean.type3UseTimes += times;
			break;
		case 4:
			lieFuBean.type4UseTimes += times;
			break;
		default:
			break;
		}
	}

	public int getDayUseTimes(int type, LieFuBean lieFuBean) {
		int dayUseTimes = 0;
		switch(type) {
		case 1:
			dayUseTimes = lieFuBean.type1UseTimes;
			break;
		case 2:
			dayUseTimes = lieFuBean.type2UseTimes;
			break;
		case 3:
			dayUseTimes = lieFuBean.type3UseTimes;
			break;
		case 4:
			dayUseTimes = lieFuBean.type4UseTimes;
			break;
		default:
			break;
		}
		return dayUseTimes;
	}

	public void changeTypeState(LieFuBean lieFuBean, int type, int state) {
		switch(type) {
			case 2:
				lieFuBean.type2State = state;
				break;
			case 3:
				lieFuBean.type3State = state;
				break;
			case 4:
				lieFuBean.type4State = state;
				break;
			default:
				break;
		}
	}

	public int getNextActionType(int type) {
		int nextType = -1;
		switch(type) {
			case 1:
				nextType = 2;
				break;
			case 2:
				nextType = 3;
				break;
			case 3:
				nextType = 4;
				break;
			case 4:
				break;
			default:
				break;
		}
		return nextType;
	}

}
