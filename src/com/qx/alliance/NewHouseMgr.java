package com.qx.alliance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.FangWu;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.network.PD;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.alliance.building.LMKJJiHuo;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.world.Mission;

import qxmobile.protobuf.House.HouseExpInfo;

public class NewHouseMgr  extends EventProc implements Runnable {
	public Logger log = LoggerFactory.getLogger(NewHouseMgr.class.getSimpleName());
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static Mission exit = new Mission(0, null, null);
	public static int fangwuSum = 280;
	public Map<Integer, FangWu> houseTemp = null ;
	
	public NewHouseMgr(){
		new Thread(this, "HouseMgr").start();
	}
	public void init(){
		List<FangWu> houseList = TempletService.listAll(FangWu.class.getSimpleName());
		
		Map<Integer, FangWu> houseMap = new HashMap<Integer, FangWu>();
		
		for(FangWu fw : houseList){
			houseMap.put(fw.lv, fw);
		}
		
		houseTemp = houseMap ;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (GameServer.shutdown == false) {
			Mission m = null;
			try {
				m = missions.take();
			} catch (InterruptedException e) {
				log.error("interrupt", e);
				continue;
			}
			if (m == exit) {
				break;
			}
			try {
				handle(m);
			} catch (Throwable e) {
				log.info("异常协议{}", m.code);
				log.error("处理出现异常", e);
			}
		}
		log.info("退出HouseMgr");
	}
	
	public void shutdown() {
		missions.add(exit);
	}

	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.C_LM_HOUSE_INFO:
			
			break;
		case PD.C_Set_House_state:
			
			break;
		case PD.C_HOUSE_EXCHANGE_RQUEST:
			
			break;
		case PD.C_EHOUSE_EXCHANGE_RQUEST:
			
			break;
		case PD.C_HOUSE_APPLY_LIST:
			
			break;
		case PD.C_AnswerExchange:
			
			break;
		case PD.C_CANCEL_EXCHANGE:
			
			break;
		case PD.C_EnterOrExitHouse:
			
			break;
		case PD.C_ShotOffVisitor:
			
			break;
		case PD.C_GetHouseVInfo:
			
			break;
		case PD.C_get_house_exp:
			
			break;
		case PD.C_get_house_info:
			
			break;
		case PD.C_GET_BIGHOUSE_EXP:
			
			break;
		case PD.C_huan_wu_info:
			
			break;
		case PD.C_huan_wu_Oper:
			
			break;
		case PD.C_huan_wu_list:
			
			break;
		case PD.C_huan_wu_exchange: {
			
		}
			break;
		case PD.C_ExCanJuanJiangLi: {
			
		}
			break;
		case PD.C_up_house: {
			
		}
			break;
		case PD.C_Pai_big_house:
			
			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
	}



	@Override
	public void proc(Event e) {
		switch (e.id) {
		case ED.Join_LM:
			joinLM(e);
			break;
		case ED.Leave_LM:
			leaveLM(e);
			break;
		case ED.REFRESH_TIME_WORK:
			checkHouseRedPoint(e);
			break;
		default:
			log.error("错误事件参数",e.id);
			break;
		}
		
	}
	
	/**
	 * @Description：处理玩家加入联盟事件*/
	public void joinLM(Event e){
		
	}
	/**
	 * @Description：处理玩家离开联盟事件*/
	public void leaveLM(Event e){
		
	}
	
	/**
	 * @Description：房屋经验满红点推送处理*/
	public void checkHouseRedPoint(Event e){
		log.info("定时刷新房屋经验");
		IoSession session=(IoSession) e.param;
		if(session==null){
			log.error("定时刷新房屋经验错误，session为null");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz==null){
			log.error("定时刷新房屋经验错误，JunZhu为null");
			return;
		}
		long jzId=jz.id;
		int level=jz.level;
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.LianMeng, jzId, level);
		if(!isOpen){
			log.info("君主--{}的功能---{}未开启,不推送",jzId,FunctionID.LianMeng);
			return;
		}
		//TODO 合法性判断结束，处理红点推送判断
		log.info("定时刷新房屋经验完成");
	}
	
	/** 
	 * @Description：刷新房屋经验信息，并返回房屋经验是否最大
	  */
	
	public boolean refreshHouseExp(JunZhu junzhu , HouseExpInfo.Builder expInfo){
		long jzId=junzhu.id;
		AlliancePlayer ap = AllianceMgr.inst.getAlliancePlayer(jzId);
		if (ap == null) {
			log.info("君主{}无联盟，不刷新房屋经验，ap == null", jzId);
			return false;
		}
		if (ap.lianMengId <= 0) {
			log.info("君主{}无联盟，不刷新房屋经验，ap.lianMengId <= 0", jzId);
			return false;
		}
		HouseBean hb = HibernateUtil.find(HouseBean.class, "where jzId=" + jzId);
		if(hb==null){ 
			log.info("君主{}没有房屋，无经验可以领取", jzId);
			return false;
		}
		if(hb.lmId<=0){
			log.info("君主{}离开联盟-没有房屋，无经验可以领取", jzId);
			return false;
		}
		
		//获取房屋配置
		FangWu fwConf = houseTemp.get(hb.level);
		//获取房屋相关的联盟科技
		LianMengKeJi lmkj =getKejiConf2FangWu(hb.jzId, hb.lmId);
		
		
		
		double addSpeedRatio=lmkj.value1;	//value1科技增加经验获取速度
		int addLimit4keji=lmkj.value2;				//value2代表科技增加经验存储上限
		int exMax=fwConf.produceLimit +addLimit4keji;  //房屋总获取经验上限：房屋上限+科技加成
		double produceSpeed= fwConf.produceSpeed*(100+addSpeedRatio)/100; //实际生产经验速度：基础速度*（1+科技增加速度）
		int getexp = 0 ;
		
		Date preGetExpT = hb.preGainExpTime; //获取上次收取经验时间
		if (hb.preGainExpTime == null) {
			preGetExpT = hb.firstHoldTime; //未收取过则默认房屋创建时间
		}
		if (preGetExpT == null) {
			log.error("数据错误，获取{}玩家房屋创建时间失败" , hb.jzId);
			expInfo.setCur(0);
		} else {
			Date nowGainExpTime = new Date();
			long t = nowGainExpTime.getTime() - preGetExpT.getTime();//毫秒
			t = t / 1000;// 换算至秒
			t = t / 60;// 换算至分钟
			t = (long) (t * produceSpeed / 60);// 时速
			getexp = (int)Math.min(t, exMax);
			hb.preGainExpTime = nowGainExpTime;
		}	
		expInfo.setLevel(hb.level);//房屋等级
		expInfo.setCur(getexp);//获取经验
		expInfo.setMax(exMax);//房屋最大经验
		if(getexp == exMax ){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * @Description： 获取对房屋生效的联盟科技
	 * */
	public LianMengKeJi getKejiConf2FangWu(long jzId,int lmId) {
		LianMengKeJi kjConf=JianZhuMgr.inst.getKeJiConfForFangWu(lmId);
		LianMengKeJi retConf=kjConf;
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(jzId);
		if(player==null){
			log.error("{}得到房屋的有效房屋科技配置异常，没有找到AlliancePlayer,联盟房屋科技等级--{}，君主激活的房屋科技等级--{}",jzId, kjConf.level);
			return retConf;
		}
		if(player.title==AllianceMgr.TITLE_LEADER||player.title==AllianceMgr.TITLE_DEPUTY_LEADER){
			log.info("{}这货是盟主，联盟房屋科技等级--{},这货不用激活科技等级",jzId, kjConf.level);
			return retConf;
		}
		LMKJJiHuo lmkjJiHuo = HibernateUtil.find(LMKJJiHuo.class, jzId);
		int type = 301;
		int jiHuoLevel = lmkjJiHuo==null?0:lmkjJiHuo.type_301;
		LianMengKeJi jiHuoconf = JianZhuMgr.inst.getKeJiConf(type, jiHuoLevel);
		if(kjConf!=null&&jiHuoconf!=null){
			log.info("联盟房屋科技等级--{}，君主激活的房屋科技等级--{}", kjConf.level,jiHuoconf.level);
			if(jiHuoconf.level<kjConf.level){
				log.info("逗逼设定出现了,联盟房屋科技等级--{}，大于君主激活的房屋科技等级--{}，返回君主激活房屋科技等级", kjConf.level,jiHuoconf.level);
				retConf=jiHuoconf;
			}
		}
		return retConf;
	}
	
	
	@Override
	public void doReg() {
		EventMgr.regist(ED.Join_LM, this); //监听加入联盟
		EventMgr.regist(ED.Leave_LM, this); //监听离开联盟
		EventMgr.regist(ED.REFRESH_TIME_WORK, this); //监听定时刷新
	}
	
}
