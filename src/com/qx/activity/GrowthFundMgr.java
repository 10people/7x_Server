package com.qx.activity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChengZhangJiJin;
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
import com.qx.yuanbao.YuanBaoMgr;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.ActivityGrowthFundResp;
import qxmobile.protobuf.Activity.ActivityGrowthFundRewardResp;
import qxmobile.protobuf.Activity.GrowLevel;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;

public class GrowthFundMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(GrowthFundMgr.class);
	public static GrowthFundMgr inst;
	public Map<Integer,ChengZhangJiJin> czMap = null;
	/**成就购买状态*/
	public GrowthFundMgr(){
		inst = this;
		init();
	}
	public void init(){
		List<ChengZhangJiJin> list = TempletService.getInstance().listAll(ChengZhangJiJin.class.getSimpleName());
		czMap = new HashMap<Integer,ChengZhangJiJin>();
		if(list != null){
			for (ChengZhangJiJin chengZhangJiJin : list) {
				czMap.put(chengZhangJiJin.getLevel(),chengZhangJiJin);
			}
		}
	}
	/**
	 * 购买成基金
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void buyGrowthFund(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		
		GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jz.id);
		if(gBuyBean != null){
			logger.error("已经购买过成长基金");
			return;
		}
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		//校验VIP等级
		if(CanShu.CHENGZHANGJIJIN_VIP > jz.vipLevel){ //等级不足
			logger.error("购买成长基金失败VIP等级不足");
			resp.setResult(1);//返回 1-失败，等级不足，
			msgSend(PD.ACTIVITY_GROWTHFUND_BUY_RESP,session,resp);
			return;
		}
		//校验元宝
		int costYuanbao = CanShu.CHENGZHANGJIJIN_COST;
		if (jz.yuanBao < costYuanbao) {
			logger.error("购买基金失败，君主:{}背包内的元宝有:{}不足:{}个", jz.id, jz.yuanBao,costYuanbao);
			resp.setResult(2);//返回 2-失败，元宝不足
			msgSend(PD.ACTIVITY_GROWTHFUND_BUY_RESP,session,resp);
			return;
		}
		//扣元宝
		YuanBaoMgr.inst.diff(jz,-costYuanbao,0,0,0,"购买成长基金");
		JunZhuMgr.inst.sendMainInfo(session,jz,false); //通知前端
		//更新数据库
		gBuyBean = new GrowthFundBuyBean();
		gBuyBean.buyTime = new Date();
		gBuyBean.jzId = jz.id;
		HibernateUtil.insert(gBuyBean);
		//刷新红点
		EventMgr.addEvent(ED.activity_chengzhangjijin, new Object[]{session,jz});
		//成功返回
		resp.setResult(0);//返回  0-成功
		msgSend(PD.ACTIVITY_GROWTHFUND_BUY_RESP,session,resp);
	}
	/**
	 * 获得成基金详情
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getGrowthFundInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityGrowthFundResp.Builder resp = ActivityGrowthFundResp.newBuilder();
		List<GrowthFundBean> growthFundList = HibernateUtil.list(GrowthFundBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,GrowthFundBean> searchMap = new HashMap<Integer,GrowthFundBean>();
		for (GrowthFundBean growthFundBean : growthFundList) {
			searchMap.put(growthFundBean.level,growthFundBean);
		}
		List<ChengZhangJiJin> list = TempletService.getInstance().listAll(ChengZhangJiJin.class.getSimpleName());
		for (ChengZhangJiJin chengZhangJiJin : list) {
			if(searchMap.containsKey(chengZhangJiJin.getLevel())) continue; //领取不显示
			GrowLevel.Builder oneInfo = GrowLevel.newBuilder();
			oneInfo.setId(chengZhangJiJin.getLevel());
			oneInfo.setDes(chengZhangJiJin.getDesc());
			oneInfo.setProcess(jz.level);
			oneInfo.setMaxProcess(chengZhangJiJin.getLevel());
			oneInfo.setFunctionid(-1); //不跳转
			//奖励
			Award.Builder award = Award.newBuilder();
			String[] itemArr = chengZhangJiJin.getAward().split(":");
			award.setItemType(Integer.parseInt(itemArr[0]));
			award.setItemId(Integer.parseInt(itemArr[1]));
			award.setItemNumber(Integer.parseInt(itemArr[2]));
			oneInfo.addAwardList(award);
			resp.addLeveList(oneInfo);
		}
		//购买状态
		GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jz.id);
		if(gBuyBean == null){ //没有购买基金
			if(CanShu.CHENGZHANGJIJIN_VIP > jz.vipLevel){ //等级不足
				resp.setResult(0); //0-VIP等级不足未购买
			}else{
				resp.setResult(1); //1-VIP等级达到未购买
			}
		}else{
			resp.setResult(2); //2-已购买
		}
		///VIP等级
		resp.setVipLvNeed(jz.vipLevel);
		//配置
		resp.setCostNum(CanShu.CHENGZHANGJIJIN_COST);
		resp.setGetNum(CanShu.CHENGZHANGJIJIN_REBATE);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.ACTIVITY_GROWTHFUND_INFO_RESP;
		msg.builder = resp;
		session.write(msg);
	}
	/**
	 * 领取成长基金
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getGrowthFundReward(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityGrowthFundRewardResp.Builder req = (ActivityGrowthFundRewardResp.Builder)builder;
		int level = req.getLevel();
		GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jz.id);
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		ProtobufMsg msg = new ProtobufMsg();
		//校验是否购买基金
		msg.id = PD.ACTIVITY_GROWTHFUND_GETREWARD_RESP;
		if(gBuyBean == null){ //没有购买基金
			logger.info("没有购买成长基金");
			resp.setResult(3);
			msg.builder = resp;
			session.write(msg);
			return;
		}
		//校验君主等级
		if(level > jz.level){
			logger.info("等级不足不能领取");
			resp.setResult(1);
			msg.builder = resp;
			session.write(msg);
			return;
		}
		//校验是否已经领取该等级对应奖励
		GrowthFundBean gFundBean =  HibernateUtil.find(GrowthFundBean.class,"where jzId=" + jz.id + " and level=" + level + " and getState=1");
		if(gFundBean != null){
			logger.info("阶段奖励已经领取");
			resp.setResult(2);
			msg.builder = resp;
			session.write(msg);
			return;
		}
		//记录已经领取奖励
		gFundBean = new GrowthFundBean();
		gFundBean.jzId = jz.id;
		gFundBean.level = level;
		gFundBean.getState = 1;
		gFundBean.getTime = new Date();
		HibernateUtil.insert(gFundBean);
		//成功返回
		resp.setResult(0);
		msg.builder = resp;
		session.write(msg);
		//道具加到身上
		String[] itemArr = czMap.get(level).award.split(":");
		AwardMgr.inst.giveReward(session,czMap.get(level).award,jz);
		//刷新红点
		EventMgr.addEvent(ED.activity_chengzhangjijin, new Object[]{session,jz});
		//奖励弹窗消息
		ExploreResp.Builder awardresp = ExploreResp.newBuilder();
		awardresp.setSuccess(0);
		Award.Builder awardInfo = Award.newBuilder();
 		awardInfo.setItemType(Integer.parseInt(itemArr[0]));
		awardInfo.setItemId(Integer.parseInt(itemArr[1]));
		awardInfo.setItemNumber(Integer.parseInt(itemArr[2]));
		awardresp.addAwardsList(awardInfo);
		msg.id = PD.S_USE_ITEM;
		msg.builder = awardresp;
		session.write(msg);
		//返回ActivityGetRewardResp 0-领奖成功，1-失败，等级不足，2-失败，已经领取，3-未购买成长基金
	}
	
	/**
	 * 消息发送
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void msgSend(int id,IoSession session,Builder builder){
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = id;
		msg.builder = builder;
		session.write(msg);
	}
	
	/**
	 * 活动是否显示
	 * @param jz
	 * @return true 显示活动
	 */
	public boolean isShow(JunZhu jz){
		List<ChengZhangJiJin> list = TempletService.getInstance().listAll(ChengZhangJiJin.class.getSimpleName());
		List<GrowthFundBean> growthFundList = HibernateUtil.list(GrowthFundBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,GrowthFundBean> searchMap = new HashMap<Integer,GrowthFundBean>();
		for (GrowthFundBean growthFundBean : growthFundList) {
			searchMap.put(growthFundBean.level,growthFundBean);
		}
		boolean isShow = false;
		for (ChengZhangJiJin chengZhangJiJin : list) {
			if(searchMap.containsKey(chengZhangJiJin.getLevel())) continue; //领取不显示
			isShow =true;
		}
		return isShow;
	}
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:{
			long jzId = (long)event.param;
			IoSession session = AccountManager.sessionMap.get(jzId);
			if(session == null){
				return;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
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
		case ED.activity_chengzhangjijin:{
			Object[] objects = (Object[]) event.param;
			IoSession session = (IoSession)objects[0];
			if(session == null){
				return;
			}
			JunZhu jz = (JunZhu) objects[1];
			isShowRed(session,jz);
		}
		break;
		default:
			break;
		}
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN,this);
		EventMgr.regist(ED.junzhu_level_up,this);
		EventMgr.regist(ED.activity_chengzhangjijin, this);
	}
	
	public void isShowRed(IoSession session,JunZhu jz){
		if(session == null){
			return;
		}
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		//购买状态
		GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jz.id);
		if(gBuyBean == null){ //没有购买基金
			return;
		}
		List<GrowthFundBean> growthFundList = HibernateUtil.list(GrowthFundBean.class,"where jzId=" + jz.id + " and getState=1");
		Map<Integer,GrowthFundBean> searchMap = new HashMap<Integer,GrowthFundBean>();
		for (GrowthFundBean growthFundBean : growthFundList) {
			searchMap.put(growthFundBean.level,growthFundBean);
		}
		List<ChengZhangJiJin> list = TempletService.getInstance().listAll(ChengZhangJiJin.class.getSimpleName());
		for (ChengZhangJiJin chengZhangJiJin : list) {
			if(searchMap.containsKey(chengZhangJiJin.getLevel())) continue; //领取不显示
			if(jz.level >= chengZhangJiJin.getLevel()){
				FunctionID.pushCanShowRed(jz.id,session,FunctionID.activity_chengzhangjijin);
				break;
			}
		}
	}
}
