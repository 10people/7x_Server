package com.qx.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.vip.PlayerVipInfo;
import com.qx.vip.VipMgr;

import qxmobile.protobuf.Activity.ActivityCardGetRewardReq;
import qxmobile.protobuf.Activity.ActivityCardResp;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.MonthCardInfo;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;

public class MonthCardMgr {
	public Logger logger = LoggerFactory.getLogger(ShouchongMgr.class);
	public static MonthCardMgr inst;
	public static int minth_card1_status = 0; // 0-需要充值，1-已经充值未领取，2-已经领取 测试用
	public static int minth_card2_status = 0; // 0-需要充值，1-已经充值未领取，2-已经领取 测试用
	/**月卡详情返回：需要充值 <br> 领奖返回：领奖成功*/
	public static final int RESP_0 = 0;
	/**月卡详情返回：可以领取  <br> 领奖返回：失败，需要充值*/
	public static final int RESP_1 = 1;
	/**月卡详情返回：已经领取  <br> 领奖返回：失败，已经领取*/
	public static final int RESP_2 = 2;
	
	public Map<String, CanShu> canshuMap = null;
	public MonthCardMgr() {
		inst = this;
		init();
	}
	public void init() {
	}
	
	/**
	 * 获取月卡详情
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void monthCardInfo(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityCardResp.Builder resp = ActivityCardResp.newBuilder();
		MonthCardInfo.Builder info = MonthCardInfo.newBuilder();
		PlayerVipInfo playerVipInfo = VipMgr.INSTANCE.getPlayerVipInfo(jz.id);
		//校验是否已经领取过
		FuliInfo fuliInfo = HibernateUtil.find(FuliInfo.class,jz.id);
		//月卡
		int rmDays = playerVipInfo.yueKaRemianDay;
		if(rmDays <= 0){ //没有购买月卡
			info.setResult(RESP_0);
		}else{
			//校验时间
			if(!isMonthCardReward(fuliInfo,0)){ 
				info.setResult(RESP_2);//已经领取
				info.setCd(getRewardCd());
			}else{
				info.setResult(RESP_1);//可以领取
			}
		}
		info.setGiveNum(CanShu.YUEKA_YUANBAO); //领取元宝数
		info.setRmDays(rmDays); //剩余天数
		resp.addMonthCard(info);
		//终身卡
		MonthCardInfo.Builder info2 = MonthCardInfo.newBuilder();
		if(playerVipInfo.haveZhongShenKa <= 0){ //没有购买终身卡
			info2.setResult(RESP_0);
		}else{
			//校验时间
			if(!isMonthCardReward(fuliInfo,1)){ 
				info2.setResult(RESP_2);//已经领取
				info2.setCd(getRewardCd());
			}else{
				info2.setResult(RESP_1);//可以领取
			}
		}
		info2.setGiveNum(CanShu.ZHONGSHENKA_YUANBAO);
		info2.setRmDays(-1);
		resp.addMonthCard(info2);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.ACTIVITY_MONTH_CARD_INFO_RESP;
		msg.builder = resp;
		session.write(msg);
	}
	
	/**
	 * 领取月卡奖励
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void monthCardGetReward(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityCardGetRewardReq.Builder req = (ActivityCardGetRewardReq.Builder)builder;
		int type = req.getType();
		PlayerVipInfo playerVipInfo = VipMgr.INSTANCE.getPlayerVipInfo(jz.id);
		FuliInfo fuliInfo = HibernateUtil.find(FuliInfo.class,jz.id);
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		int give = 0; //元宝数
		boolean success = false;
		if(type == 0){ //月卡
			if(playerVipInfo.yueKaRemianDay <= 0){ //没有购买月卡
				resp.setResult(RESP_1); //需要充值
			}else{
				//校验是否已经领取过
				if(!isMonthCardReward(fuliInfo,type)){ 
					resp.setResult(RESP_2); //已经领取
				}else{
					success = true;
					resp.setResult(RESP_0); //成功
					//更新领取状态
					if(fuliInfo == null){
						fuliInfo = new FuliInfo();
						fuliInfo.jzId = jz.id;
						fuliInfo.getYuKaFuLiTime = new Date();
						HibernateUtil.insert(fuliInfo);
					}else{
						fuliInfo.getYuKaFuLiTime = new Date();
						HibernateUtil.update(fuliInfo);
					}
				}
			}
			give = CanShu.YUEKA_YUANBAO;
		}else if(type == 1){ //终身卡
			if(playerVipInfo.haveZhongShenKa <= 0){ //没有购买终身卡
				resp.setResult(RESP_1); //需要充值
			}else{
				//校验时间
				if(!isMonthCardReward(fuliInfo,type)){ 
					resp.setResult(RESP_2); //已经领取
				}else{
					success = true;
					resp.setResult(RESP_0); //成功
					//更新领取状态
					if(fuliInfo == null){
						fuliInfo = new FuliInfo();
						fuliInfo.jzId = jz.id;
						fuliInfo.getZhongShenKaTime = new Date();
						HibernateUtil.insert(fuliInfo);
					}else{
						fuliInfo.getZhongShenKaTime = new Date();
						HibernateUtil.update(fuliInfo);
					}
				}
			}
			give = CanShu.ZHONGSHENKA_YUANBAO;
		}else{
			logger.error("未知的月卡类型type:" + type);
			return;
		}
		ProtobufMsg msg = new ProtobufMsg();
		if(success){
			String jiangLi= "0:" + AwardMgr.item_yuan_bao + ":" + give;
			//加元宝
 			AwardMgr.inst.giveReward(session,jiangLi,jz);
			ExploreResp.Builder awardresp = ExploreResp.newBuilder();
			awardresp.setSuccess(0);
			Award.Builder awardInfo = Award.newBuilder();
			awardInfo.setItemType(0);
			awardInfo.setItemId(AwardMgr.item_yuan_bao);
			awardInfo.setItemNumber(give);
			awardresp.addAwardsList(awardInfo);
			msg.id = PD.S_USE_ITEM;
			msg.builder = awardresp;
			session.write(msg);
		}
		msg.id = PD.ACTIVITY_MONTH_CARD_REWARD_RESP;
		msg.builder = resp;
		session.write(msg);
		//required int32 result = 1; // 0-领奖成功，1-失败，需要充值，2-失败，已经领取
	}
	
	public void pmSend(int id,IoSession session,Builder builder){
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = id;
		msg.builder = builder;
		session.write(msg);
	}
	
	/**
	 * @Description 校验月卡、终身卡是否领取
	 * @param type 0 月卡，1终身卡
	 */
	public boolean isMonthCardReward(FuliInfo info,int type) {
		boolean result = true;
		if(type == 0){
			if(info != null&&info.getYuKaFuLiTime != null){
				result = DateUtils.isTimeToReset(info.getYuKaFuLiTime,CanShu.REFRESHTIME_PURCHASE);
			}
		}else if(type == 1){
			if(info != null&&info.getZhongShenKaTime != null){
				result = DateUtils.isTimeToReset(info.getZhongShenKaTime,CanShu.REFRESHTIME_PURCHASE);
			}
		}
		return result;
	}
	
	/**
	 * 获取领奖倒计时
	 * @return int 秒数
	 */
	public int getRewardCd(){
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR_OF_DAY,CanShu.REFRESHTIME_PURCHASE);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if(now > calendar.getTimeInMillis()){
			calendar.add(Calendar.DATE,1);
			return (int)(calendar.getTimeInMillis() - now) / 1000;
		}else{
			return (int)(now - calendar.getTimeInMillis() / 1000);
		}
	}
}
