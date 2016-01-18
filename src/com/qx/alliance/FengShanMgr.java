package com.qx.alliance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceProtos.FengShanInfo;
import qxmobile.protobuf.AllianceProtos.FengShanInfoResp;
import qxmobile.protobuf.AllianceProtos.FengShanReq;
import qxmobile.protobuf.AllianceProtos.FengShanResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LianmengFengshan;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskMgr;
import com.qx.timeworker.FunctionID;

public class FengShanMgr extends EventProc{
	public Logger log = LoggerFactory.getLogger(FengShanMgr.class);

	public static FengShanMgr inst;
	public Map<Integer, LianmengFengshan> LianmengFengshanMap;


	public FengShanMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<LianmengFengshan> LianmengFengshanList = TempletService.getInstance().listAll(LianmengFengshan.class.getSimpleName());
		Map<Integer, LianmengFengshan> LianmengFengshanMap = new HashMap<Integer, LianmengFengshan>();
		for (LianmengFengshan fs : LianmengFengshanList) {
			LianmengFengshanMap.put(fs.id, fs);
		}
		this.LianmengFengshanMap = LianmengFengshanMap;
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.HUOYUE_CHANGE, this);		
	}
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.HUOYUE_CHANGE:
			refreshFengShanInfo(event);
			break;
		case ED.REFRESH_TIME_WORK:
			break;
		}
	}

	public FengshanBean getFengShanBean(long jzId){
		FengshanBean fsBean = HibernateUtil.find(FengshanBean.class, jzId);
		if(fsBean == null){
			fsBean = initFengShanBean(jzId);
		}
		resetFengShanInfo(fsBean);
		return fsBean;
	}
	public FengshanBean initFengShanBean(long jzId){
		log.info("初始化君主--{}封禅信息",jzId);
		FengshanBean fsBean = new FengshanBean();
		fsBean.jzId = jzId;
		fsBean.lastResetTime=new Date();
		HibernateUtil.save(fsBean);
		return fsBean;
	}
	public void resetFengShanInfo(FengshanBean fsBean){
		if(fsBean == null){
			return;
		}
		Date now = new Date();
		Date  lastResetTime=fsBean.lastResetTime;
		if(lastResetTime != null && DateUtils.isTimeToReset(lastResetTime,CanShu.REFRESHTIME_PURCHASE)) {
			log.info("reset君主--{}封禅信息",fsBean.jzId);
			fsBean.lastResetTime = now;
			fsBean.isGetFengShan1 = false;
			fsBean.isGetFengShan2 = false;
			HibernateUtil.save(fsBean);
		}
	}
	//请求封禅信息
	public void  getFengShanInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求封禅信息出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		log.info("{}请求封禅信息开始",jzId);
		int huoyue=DailyTaskMgr.INSTANCE.getTodayHuoYueDu(jzId);
		FengshanBean fsBean =getFengShanBean(jzId);
		FengShanInfoResp.Builder resp=FengShanInfoResp.newBuilder();
		resp.setHuoyuedu(huoyue);
		
		FengShanInfo.Builder fsInfo1=FengShanInfo.newBuilder();
		fsInfo1.setConfId(1);
		// State状态 1未达到条件 2可封禅 3 已封禅
		int state1=getFengShanState(fsBean, huoyue, 1);
		fsInfo1.setState(state1);
		resp.addFsInfo(fsInfo1);
		
		FengShanInfo.Builder fsInfo2=FengShanInfo.newBuilder();
		fsInfo2.setConfId(2);
		int state2=getFengShanState(fsBean, huoyue, 2);
		fsInfo2.setState(state2);
		resp.addFsInfo(fsInfo2);
		
		log.info("{}请求封禅信息结束，活跃度--{}，封禅1状态---{}，封禅2状态---{}",jzId,huoyue,state1,state2);
		session.write(resp.build());
//		FengShanReq.Builder rep2Builder=FengShanReq.newBuilder();
//		rep2Builder.setConfId(1);
//		doFengShan(id, rep2Builder,session);
	}
	public int getFengShanState(FengshanBean fsBean,int huoyue,int confId) {
		if(confId==1&&fsBean.isGetFengShan1){
			return 3;
		}
		if(confId==2&&fsBean.isGetFengShan2){
			return 3;
		}
		LianmengFengshan fsConf=LianmengFengshanMap.get(confId);
		if(huoyue>=fsConf.huoyuedu){
			return 2;
		}
		return 1;
	}
	//请求进行封禅
	public void  doFengShan(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求封禅信息出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		FengShanReq.Builder rep=(FengShanReq.Builder)builder;
		int confId=rep.getConfId();
		log.info("{}请求封禅--{}开始",jzId,confId);
		int result=20;
		switch (confId) {
		case 1:
			result=doFengShan(confId,jz, session);
			break;
		case 2:
			result=doFengShan(confId,jz, session);
			break;
		default:
			log.error("{}请求封禅出错，错误的配置ID--{}",jzId,confId);
			break;
		}
		FengShanResp.Builder resp=FengShanResp.newBuilder();
		resp.setConfId(confId);
		resp.setResult(result);
		log.info("{}请求封禅结束，结果--{}",jzId,result);
		session.write(resp.build());
	}
	//进行封禅
	public int doFengShan(int confId,JunZhu jz, IoSession session){
		long jzId=jz.id;
		LianmengFengshan fsConf=LianmengFengshanMap.get(confId);
		int huoyue=DailyTaskMgr.INSTANCE.getTodayHuoYueDu(jzId);
		if(huoyue<fsConf.huoyuedu){
			log.error("请求封禅信息出错：君主-{}活跃度--{}不够--{}",jzId,huoyue,fsConf.huoyuedu);
			return 20;
		}
		FengshanBean fsBean =getFengShanBean(jzId);
		if(confId==1&&fsBean.isGetFengShan1){
			log.error("请求封禅信息出错：君主-{}已经封禅--{}",jzId,confId);
			return 30;
		}
		if(confId==2&&fsBean.isGetFengShan2){
			log.error("请求封禅信息出错：君主-{}已经封禅--{}",jzId,confId);
			return 30;
		}
		String award =fsConf.award;
		if(award!=null&&!"".equals(award)){
			AwardMgr.inst.giveReward(session, award, jz);
		}
		switch (confId) {
		case 1:
			fsBean.isGetFengShan1=true;
			break;
		case 2:
			fsBean.isGetFengShan2=true;
			break;
		default:
			log.error("请求封禅信息出错：君主-{}更新封禅--{}状态出错",jzId,confId);
			break;
		}
		fsBean.lastResetTime=new Date();
		HibernateUtil.save(fsBean);
		return 10;
	}


	//刷新封禅是否出现红点
	public void refreshFengShanInfo(Event event) {
		Object[]	obs = (Object[])event.param;
		long jzId = (Long)obs[0];
		int huoyue=(Integer)obs[1];
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		//封禅活动1
		LianmengFengshan fsConf1=LianmengFengshanMap.get(1);
		if(huoyue>=fsConf1.huoyuedu){
			if(su.session!=null){
				FunctionID.pushCanShangjiao(jzId,su.session, FunctionID.fengShan);
			}
			return;
		}
		//刷新封禅活动2
		LianmengFengshan fsConf2=LianmengFengshanMap.get(2);
		if(huoyue>=fsConf2.huoyuedu){
			if(su.session!=null){
				FunctionID.pushCanShangjiao(jzId,su.session, FunctionID.fengShan);
			}
			return;
		}
	}

}