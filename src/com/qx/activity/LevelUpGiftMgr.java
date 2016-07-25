package com.qx.activity;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.XianshiHuodong;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import qxmobile.protobuf.Activity.ActivitLevelGiftResp;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.ActivityGrowthFundRewardResp;
import qxmobile.protobuf.Activity.GrowLevel;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;

public class LevelUpGiftMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(LevelUpGiftMgr.class);
	public static LevelUpGiftMgr inst;
	public Map<Integer,XianshiHuodong> chongjiMap = null;
	public LevelUpGiftMgr(){
		inst = this;
		init();
	}
	public void init(){
		List<XianshiHuodong> list = TempletService.getInstance().listAll(XianshiHuodong.class.getSimpleName());
		chongjiMap = new HashMap<Integer,XianshiHuodong>();
		for (XianshiHuodong xianshiHuodong : list) {
			if(xianshiHuodong.doneType != 1) continue; //只初始化冲级
			chongjiMap.put(Integer.parseInt(xianshiHuodong.doneCondition),xianshiHuodong);
		}
	}
	/**
	 * 获得冲级送礼详情
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getLevelUpGiftInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			logger.error("未找到君主信息");
			return;
		}
		List<LevelUpGiftBean> chongjiList = HibernateUtil.list(LevelUpGiftBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,LevelUpGiftBean> searchMap = new HashMap<Integer,LevelUpGiftBean>();
		for (LevelUpGiftBean levelUpGiftBean : chongjiList) {
			searchMap.put(levelUpGiftBean.level,levelUpGiftBean);
		}
		List<XianshiHuodong> settings = TempletService.getInstance().listAll(XianshiHuodong.class.getSimpleName());
		ActivitLevelGiftResp.Builder resp = ActivitLevelGiftResp.newBuilder();
		for (XianshiHuodong xianshiHuodong : settings) {
			if(xianshiHuodong.doneType != 1) continue; //过滤掉其他配置
			if(searchMap.containsKey(Integer.parseInt(xianshiHuodong.doneCondition))) continue; //领取不显示
			GrowLevel.Builder oneInfo = GrowLevel.newBuilder();
			oneInfo.setId(Integer.parseInt(xianshiHuodong.doneCondition));
			oneInfo.setDes(xianshiHuodong.desc);
			oneInfo.setProcess(jz.level);
			oneInfo.setMaxProcess(Integer.parseInt(xianshiHuodong.doneCondition));
			oneInfo.setFunctionid(-1); //不跳转
			//奖励
			String[] awardsArr = xianshiHuodong.Award.split("#"); //0:900018:2000#0:900019:2000#0:900002:550
			List<String> awardList = Arrays.asList(awardsArr);
			for (String awardStr : awardList) {
				//0:900018:2000
				String[] awardarr = awardStr.split(":");
				Award.Builder award = Award.newBuilder();
				award.setItemType(Integer.parseInt(awardarr[0]));
				award.setItemId(Integer.parseInt(awardarr[1]));
				award.setItemNumber(Integer.parseInt(awardarr[2]));
				oneInfo.addAwardList(award);
			}
			resp.addLeveList(oneInfo);
		}
		//配置
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.ACTIVITY_LEVEL_INFO_RESP;
		msg.builder = resp;
		session.write(msg);
	}
	
	/**
	 * 领取奖励
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void levelUpGiftReward(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityGrowthFundRewardResp.Builder req = (ActivityGrowthFundRewardResp.Builder)builder;
		int level = req.getLevel();
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.ACTIVITY_LEVEL_GET_RESP;
		//校验君主等级
		if(level > jz.level){
			logger.info("等级不足不能领取");
			resp.setResult(1);
			msg.builder = resp;
			session.write(msg);
			return;
		}
		//校验是否已经领取该等级对应奖励
		LevelUpGiftBean levelUpGiftBean =  HibernateUtil.find(LevelUpGiftBean.class,"where jzId=" + jz.id + " and level=" + level + " and getState=1");
		if(levelUpGiftBean != null){
			logger.info("阶段奖励已经领取");
			resp.setResult(2);
			msg.builder = resp;
			session.write(msg);
			return;
		}
		//记录已经领取奖励
		levelUpGiftBean = new LevelUpGiftBean();
		levelUpGiftBean.jzId = jz.id;
		levelUpGiftBean.level = level;
		levelUpGiftBean.getState = 1; //已经领取
		levelUpGiftBean.getTime = new Date();
		HibernateUtil.insert(levelUpGiftBean);
		//道具加到身上
		String[] awardsArr = chongjiMap.get(level).Award.split("#"); //0:900018:2000#0:900019:2000#0:900002:550
		List<String> awardList = Arrays.asList(awardsArr);
		ExploreResp.Builder awardresp = ExploreResp.newBuilder();//奖励弹窗消息
		awardresp.setSuccess(0);
		for (String awardStr : awardList) {
			//0:900018:2000
			String[] awardarr = awardStr.split(":");
			Award.Builder awardInfo = Award.newBuilder();
			awardInfo.setItemType(Integer.parseInt(awardarr[0]));
			awardInfo.setItemId(Integer.parseInt(awardarr[1]));
			awardInfo.setItemNumber(Integer.parseInt(awardarr[2]));
			awardresp.addAwardsList(awardInfo);
			//加道具
			AwardMgr.inst.giveReward(session,awardStr,jz);
		}
		//成功返回
		resp.setResult(0);
		msg.builder = resp;
		session.write(msg);
		msg.id = PD.S_USE_ITEM;
		msg.builder = awardresp;
		session.write(msg);
		//返回ActivityGetRewardResp 0-领奖成功，1-失败，等级不足，2-失败，已经领取，3-未购买成长基金
	}
	
	/**
	 * 是否在活动列表显示，全部领取完关闭
	 * @param jz
	 * @return true 显示
	 */
	public boolean isShow(JunZhu jz){
		boolean isShow = false;
		List<LevelUpGiftBean> chongjiList = HibernateUtil.list(LevelUpGiftBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,LevelUpGiftBean> searchMap = new HashMap<Integer,LevelUpGiftBean>();
		for (LevelUpGiftBean levelUpGiftBean : chongjiList) {
			searchMap.put(levelUpGiftBean.level,levelUpGiftBean);
		}
		List<XianshiHuodong> settings = TempletService.getInstance().listAll(XianshiHuodong.class.getSimpleName());
		ActivitLevelGiftResp.Builder resp = ActivitLevelGiftResp.newBuilder();
		for (XianshiHuodong xianshiHuodong : settings) {
			if(xianshiHuodong.doneType != 1) continue; //过滤掉其他配置
			if(searchMap.containsKey(Integer.parseInt(xianshiHuodong.doneCondition))) continue; //领取不显示
			isShow = true;
		}
		return isShow;
	}
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:{
			JunZhu jz = (JunZhu) event.param;
			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				return;
			}
			isShowRed(session,jz);
		}
		break;
		case ED.junzhu_level_up:{
			Object[] objs = (Object[])event.param;
			long jzId = (long)objs[0];
			IoSession session = AccountManager.sessionMap.get(jzId);
			if(session == null){
				return;
			}
			JunZhu jz = (JunZhu)objs[2];
			isShowRed(session,jz);
		}
		break;
		default:
			break;
		}
		
	}
	@Override
	public void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		EventMgr.regist(ED.junzhu_level_up, this);
	}
	
	public void isShowRed(IoSession session,JunZhu jz){
		if(jz == null){
			return;
		}
		List<LevelUpGiftBean> chongjiList = HibernateUtil.list(LevelUpGiftBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,LevelUpGiftBean> searchMap = new HashMap<Integer,LevelUpGiftBean>();
		for (LevelUpGiftBean levelUpGiftBean : chongjiList) {
			searchMap.put(levelUpGiftBean.level,levelUpGiftBean);
		}
		List<XianshiHuodong> settings = TempletService.getInstance().listAll(XianshiHuodong.class.getSimpleName());
		for (XianshiHuodong xianshiHuodong : settings) {
			if(xianshiHuodong.doneType != 1) continue; //过滤掉其他配置
			if(searchMap.containsKey(Integer.parseInt(xianshiHuodong.doneCondition))) continue; //领取不显示
			if(jz.level >= Integer.parseInt(xianshiHuodong.doneCondition)){
				FunctionID.pushCanShowRed(jz.id,session,FunctionID.activity_levelAward);
				break;
			}
		}
	}
	
}
