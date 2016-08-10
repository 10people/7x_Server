package com.qx.alliancefight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.JCZCity;
import com.manu.dynasty.template.JCZNpcTemp;
import com.manu.dynasty.template.JCZTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.TXSocketMgr;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.world.FightScene;

import qxmobile.protobuf.AllianceFightProtos.BidMsgInfo;
import qxmobile.protobuf.AllianceFightProtos.BidRecord;
import qxmobile.protobuf.AllianceFightProtos.CityFightInfoReq;
import qxmobile.protobuf.AllianceFightProtos.CityFightInfoResp;
import qxmobile.protobuf.AllianceFightProtos.CityInfo;
import qxmobile.protobuf.AllianceFightProtos.CityOperateType;
import qxmobile.protobuf.AllianceFightProtos.CityWarBidReq;
import qxmobile.protobuf.AllianceFightProtos.CityWarBidResp;
import qxmobile.protobuf.AllianceFightProtos.CityWarGrandInfo;
import qxmobile.protobuf.AllianceFightProtos.CityWarGrandResp;
import qxmobile.protobuf.AllianceFightProtos.CityWarOperateReq;
import qxmobile.protobuf.AllianceFightProtos.CityWarOperateResp;
import qxmobile.protobuf.AllianceFightProtos.CityWarRewardInfo;
import qxmobile.protobuf.AllianceFightProtos.CityWarRewardReq;
import qxmobile.protobuf.AllianceFightProtos.CityWarRewardResp;
import qxmobile.protobuf.AllianceFightProtos.CityWarScoreResultReq;
import qxmobile.protobuf.AllianceFightProtos.CityWarScoreResultResp;
import qxmobile.protobuf.AllianceFightProtos.PlayerScore;
import qxmobile.protobuf.AllianceFightProtos.ScoreInfo;
import qxmobile.protobuf.AllianceFightProtos.ScoreList;

public class BidMgr extends EventProc{
	public Logger log = LoggerFactory.getLogger(BidMgr.class.getSimpleName());
	public static BidMgr inst;
	public Map<Integer, JCZCity> jczmap = null; //城池配置
	public Map<Integer, JCZNpcTemp> jczNpcmap = null; //城池NPC配置
	
	/**城池状态-NPC占领*/
	public static final int city_state_npc_occupation = 0;
	/**城池状态-乙方占领*/
	public static final int city_state_own_occupation = 1;
	/**城池状态-被敌人占领*/
	public static final int city_state_enemy_occupation = 2;
	
	/**野战城池状态-已战胜*/
	public static final int city_state_conquer = 1;
	/**野战城池状态-未战胜*/
	public static final int city_state_not_conquer = 0;
	
	/**联盟战时段-0宣战时段*/
	public static final int lm_fight_interval_declare = 0;
	/**联盟战时段-1揭晓时段*/
	public static final int lm_fight_interval_announce = 1;
	/**联盟战时段-2战斗时段*/
	public static final int lm_fight_interval_fighting = 2;
	/**联盟战时段-3其他时段*/
	public static final int lm_fight_interval_other= 3;
	
	/**成功 */
	public static final int city_war_operate_result0 = 0;
	/**BID 不是盟主或副盟主   或   GET_REWARD 领取失败  或 	ENTER_FIGHT 非攻方盟员或非守方盟员*/
	public static final int city_war_operate_result1 = 1;
	/**BID 非宣战时段  或 	ENTER_FIGHT 非战斗时段*/
	public static final int city_war_operate_result2 = 2;
	/**BID 联盟等级不足   	或 	ENTER_FIGHT 不能频繁进出 */
	public static final int city_war_operate_result3 = 3;
	/**BID 不能对自己领土宣战  	或 	ENTER_FIGHT 今日战斗结束*/
	public static final int city_war_operate_result4 = 4;
	/**BID 虎符不足 	或 	ENTER_FIGHT 没有敌人宣战*/
	public static final int city_war_operate_result5 = 5;
	/**BID 野城只能宣战一次 */
	public static final int city_war_operate_result6 = 6;
	
	/**宣战开始时间*/
	public static String city_war_declaration_startTime = "8:00"; 
	/**揭晓开始*/
	public static String city_war_preparation_startTime = "16:00";
	/**战斗开始*/
	public static String city_war_fighting_startTime = "20:00";
	/**战斗结束*/
	public static String city_war_fighting_endTime = "20:30";
	/**宣战消息清空*/
	public static String city_war_bid_msg_endTime = "4:00";
	/**进入战场CD*/
	public static int city_war_enter_cd = 10;
	/**战报条数*/
	public static int city_war_fight_log_num = 20;
	/**野城宣战锁,只能有一个人对野城宣战*/
	public static final Object yechengBid = new Object();
	
	public static final String REDIS_KEY_SCORE_RESULT = "cityWarScore";
	
	public static final String HASHKEY_YECHENG = "yecheng";
	
	public BidMgr() {
		inst = this;
		inst.init();
	}
	
	public void init(){
		List<JCZCity> list = TempletService.getInstance().listAll(JCZCity.class.getSimpleName());
		jczmap = new HashMap<Integer,JCZCity>();
		for (JCZCity jczCity : list) {
			jczmap.put(jczCity.id,jczCity);
		}
		List<JCZNpcTemp> list2 = TempletService.getInstance().listAll(JCZNpcTemp.class.getSimpleName());
		jczNpcmap = new HashMap<Integer,JCZNpcTemp>();
		for (JCZNpcTemp jczNpcTemp : list2) {
			jczNpcmap.put(jczNpcTemp.id, jczNpcTemp);
		}
		//初始化时间段
		if(!"".equals(JCZTemp.declaration_startTime)){
			city_war_declaration_startTime = JCZTemp.declaration_startTime;
		}
		if(!"".equals(JCZTemp.preparation_startTime)){
			city_war_preparation_startTime = JCZTemp.preparation_startTime;
		}
		if(!"".equals(JCZTemp.fighting_startTime)){
			city_war_fighting_startTime = JCZTemp.fighting_startTime;
		}
		if(!"".equals(JCZTemp.fighting_endTime)){
			city_war_fighting_endTime = JCZTemp.fighting_endTime;
		}
	}

	/**
	 * 竞拍宣战（野城宣战要考虑同步）
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void bid(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		CityWarOperateReq.Builder opreq = (CityWarOperateReq.Builder) builder;
		int city = opreq.getCityId();
		int price = opreq.getPrice();
		int type = jczmap.get(city).type;
		if(type == 1){
			bid(id,session,jz,city,price);
		}else if (type == 2) { //野城需要同步
			synchronized(yechengBid){
				bid(id,session,jz,city,price);
			}
		}
	}
	
	/**
	 * 竞拍宣战
	 * @param id 协议ID
	 * @param session session
	 * @param city 竞拍城池Id
	 * @param price 竞拍价格
	 */
	public void bid(int id,IoSession session,JunZhu jz,int city,int hufu) {
		//校验是否有联盟
		AlliancePlayer  alliancePlayer = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if(alliancePlayer == null || alliancePlayer.lianMengId <= 0){
			log.error("没有联盟");
			return;
		}
		CityWarOperateResp.Builder cwop = CityWarOperateResp.newBuilder();
		//是盟主或副盟主
		if(alliancePlayer.title <= 0){
			log.error("只有盟主或副盟主才能宣战");
			cwop.setResult(city_war_operate_result1);
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
			return;
		}
		//是否是宣战时段
		if(!DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_preparation_startTime) && jczmap.get(city).type == 1){ //不在宣战时间段内(普通)
			log.error("不在宣战时间段内");
			cwop.setResult(city_war_operate_result2);
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
			return;
		}
//		else if(!DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_fighting_startTime) && jczmap.get(city).type == 2){//不在宣战时间段内(野城)
//			log.error("不在宣战时间段内");
//			cwop.setResult(city_war_operate_result2);
//			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
//			return;
//		}
		AllianceBean allianceBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(jczmap.get(city).type == 1){ //普通城池需要校验
			//校验是否是自己占领的城池,不能对自己领土宣战
			/*CityBean cityBean = HibernateUtil.find(CityBean.class,"where cityId=" + city + " and lmId=" + alliancePlayer.lianMengId);*/
			CityBean cityBean = CityBeanDao.inst.getCityBeanById(city);
			if(cityBean != null && cityBean.lmId == alliancePlayer.lianMengId){
				log.error("不能对自己的领土宣战");
				cwop.setResult(city_war_operate_result4);
				msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
				return;
			}
		}else if(jczmap.get(city).type == 2){ //野城只能对一个野城宣战
			List<BidBean> listWildBean = HibernateUtil.list(BidBean.class,"where type=2 and lmId=" + allianceBean.id + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() + "'");
			if(listWildBean != null){
				for (BidBean wildbidBean : listWildBean) {
					if(wildbidBean.cityId == city){
						log.error("野城只能宣战一次");
						cwop.setResult(city_war_operate_result6);
						msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
						return;
					}else {
						log.error("只能对一个野城宣战");
						cwop.setResult(city_war_operate_result4);
						msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
						return;
					}
				}
			}
		}
		
		//校验联盟等级
		int needLmLv = jczmap.get(city).allianceLv;
		if(allianceBean.level < needLmLv){
			log.error("联盟等级不足不能宣战");
			cwop.setResult(city_war_operate_result3);
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
			return;
		}
		
		//校验虎符
		if(jczmap.get(city).type == 2){ //野城虎符读配置
			hufu = jczmap.get(city).cost;
		}
		int haveCount = allianceBean.hufuNum;
		if (haveCount < hufu) {
			log.error("竞拍失败，君主:{}背包内的虎符有:{}不足:{}个", jz.id, haveCount,hufu);
			cwop.setResult(city_war_operate_result5);
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
			return;
		}
		//扣虎符
		AllianceMgr.inst.changeAlianceHufu(allianceBean,-hufu);
		/**
		 * 宣战部分
		 */
		BidBean bean = HibernateUtil.find(BidBean.class, "where lmId=" + alliancePlayer.lianMengId + " and cityId=" + city + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() + "'");
		Date bidDate = new Date();
		if (bean == null) {
			bean = new BidBean();
			bean.lmId = alliancePlayer.lianMengId;
			bean.cityId = city;
			bean.type = jczmap.get(city).type;
			HibernateUtil.insert(bean);
			//联盟首次宣战推送消息
			sendBidMsg(allianceBean.name,city,bidDate.getTime() / 1000);
		}
		bean.priceReal += hufu;
		bean.bidTime = bidDate;
		HibernateUtil.update(bean);
		
		//宣战成功消息返回
		cwop.setResult(city_war_operate_result0);
		BidRecord.Builder bidRecord = BidRecord.newBuilder();
		bidRecord.setAllianceName(allianceBean.name); //联盟名字 
		bidRecord.setHuFuNum(bean.priceReal); //竞拍价（自己的要显示真实的价格）
		bidRecord.setTime(bean.bidTime.getTime() / 1000); //竞拍时间，秒级时间戳
		cwop.setBidRecord(bidRecord);
		msgSend(PD.S_CITYWAR_OPERATE_RESP,session,cwop);
		//添加竞拍事件
		EventMgr.addEvent(allianceBean.id,ED.CITY_WAR_BID,new Object[]{allianceBean,city});
		log.info("{} bid {} price {} total {}", jz.id, city, hufu, bean.priceReal);
	}

	/**
	 * 联盟战首页信息，根据请求type决定返回普通城池还是野战城池信息
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void cityFightInfo(int id, IoSession session, Builder builder) {
		switch (((CityFightInfoReq.Builder)builder).getType()) {
		case 1:
			normalCityFightInfo(id, session, builder);
			break;
		case 2:
			wildCityFightInfo(id, session, builder);
			break;
		default:
			log.error("获取联盟首页城池 CityFightInfoReq 请求 type不合法");
			break;
		}
	}
	
	/**
	 * 获得普通城池列表
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void normalCityFightInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean myAlliance = AllianceMgr.inst.getAllianceByJunZid(jz.getId());
		if(myAlliance == null){
			log.error("没有加入联盟");
			return;
		}
		//初始化城池配置
		List<JCZCity> cityListSetting = TempletService.listAll(JCZCity.class.getSimpleName());
		//查询所有城池状态信息
		/*List<CityBean> citiesState = HibernateUtil.list(CityBean.class,"");
		Map<Integer,CityBean> citiesStateMap = new HashMap<Integer,CityBean>();
		//转换一下格式方便处理
		if(citiesState != null){
			for(CityBean cityBean:citiesState){ 
				citiesStateMap.put(cityBean.cityId,cityBean);
			}
		}*/
		//创建返回协议
		CityFightInfoResp.Builder info = CityFightInfoResp.newBuilder();
		//获取联盟ID
		List<BidBean> AllBidList = HibernateUtil.list(BidBean.class, "where priceReal>0 and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() +"'");//真实数据非缓存
		Map<Integer, List<BidBean>> bidbeanListMapReal = new HashMap<Integer,List<BidBean>>();
		Map<Integer, List<BidBean>> bidbeanListMapCache = new HashMap<Integer,List<BidBean>>();
		for (BidBean bidBeanTmp : AllBidList) {
			//真实
			List<BidBean> listTemp = bidbeanListMapReal.get(bidBeanTmp.cityId);
			if(listTemp == null){
				listTemp = new ArrayList<BidBean>();
			}
			listTemp.add(bidBeanTmp);
			bidbeanListMapReal.put(bidBeanTmp.cityId,listTemp);
			//缓存
			if(bidBeanTmp.priceCache > 0){
				List<BidBean> listTempCache = bidbeanListMapCache.get(bidBeanTmp.cityId);
				if(listTempCache == null){
					listTempCache = new ArrayList<BidBean>();
				}
				listTempCache.add(bidBeanTmp);
				bidbeanListMapCache.put(bidBeanTmp.cityId,listTempCache);
			}
		}
		int myCityCount = 0; //记录乙方占领城池数
		for(JCZCity jczCity:cityListSetting){
			if (jczCity.type == 2) //跳过野战城池
				continue;
			CityInfo.Builder cityInfo = CityInfo.newBuilder();
			cityInfo.setCityId(jczCity.id); //设置cityId
			int state2 = 0; //0-无、1-宣战、2-防守,3- 进攻状态
			if(CityBeanDao.inst.getMap().containsKey(jczCity.id) && CityBeanDao.inst.getMap().get(jczCity.id).lmId > 0){ //玩家占领
				if(CityBeanDao.inst.getMap().get(jczCity.id).lmId == myAlliance.id){ //乙方占领
					cityInfo.setCityState(city_state_own_occupation); //设置城池状态
					cityInfo.setLmIconId(myAlliance.icon); //设置占领城池联盟icon
					cityInfo.setGuojiaId(myAlliance.country);
					myCityCount++;
					if(bidbeanListMapCache.get(jczCity.id) != null && bidbeanListMapCache.get(jczCity.id).size() > 0){
						state2 = 2; //2-防守,根据缓存判断,我看到别人宣战信息是缓存
					}
					cityInfo.setOcLmName(myAlliance.name);
				}else{
					AllianceBean allianceBean = AllianceBeanDao.inst.getAllianceBean(CityBeanDao.inst.getMap().get(jczCity.id).lmId);
					if(allianceBean == null){ //联盟被解散
						CityBean cityBean = CityBeanDao.inst.getMap().get(jczCity.id);
						cityBean.lmId = 0; //默认为NPC占领
						HibernateUtil.update(cityBean); //更新
						cityInfo.setCityState(city_state_npc_occupation); //设置城池状态
						cityInfo.setLmIconId(jczCity.icon); //设置占领城池联盟icon
						
					}else{
						cityInfo.setCityState(city_state_enemy_occupation); //设置城池状态
						cityInfo.setLmIconId(allianceBean.icon); //设置占领城池联盟icon
						cityInfo.setGuojiaId(allianceBean.country);
						cityInfo.setOcLmName(allianceBean.name);
					}
					
				}
			}else{ //npc占领
				cityInfo.setCityState(city_state_npc_occupation); //设置城池状态
				cityInfo.setLmIconId(jczCity.icon); //设置占领城池联盟icon
			}
			CityBean cityBean = CityBeanDao.inst.getMap().get(jczCity.id);
			if(cityBean == null){
				cityBean = new CityBean();
				cityBean.cityId = jczCity.id;
				cityBean.lmId = 0;
				cityBean.atckLmId = 0;
				CityBeanDao.inst.getMap().put(jczCity.id,cityBean);
				HibernateUtil.insert(cityBean);
			}
			//联盟宣战的城池，蓝色箭头（宣战）一直指向被宣战的城池图标上方，
			//揭晓阶段显示宣战成功后，蓝色箭头变为红色箭头（进攻字样）。
			List<BidBean> BidList = bidbeanListMapReal.get(jczCity.id);
			if(BidList != null && BidList.size() > 0){
				for (BidBean bidBean : BidList) {
					if(bidBean.lmId == myAlliance.id){ //我对该城池宣战
						state2 = 1;
						break;
					}
				}
			}
			if(cityBean.atckLmId == myAlliance.id){ //我对该城池宣战
				state2 = 1;
			}
			if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime) && state2 == 1){ //战斗 ,//揭晓竞拍失败显示“竞拍失败”
				if(cityBean.atckLmId == myAlliance.id){
					state2 = 3;
				}else{
					if(cityBean.atckLmId != myAlliance.id){
						state2 = 4; //显示竞拍失败四个字
					}
				}
			}
			//处理揭晓阶段联盟解散情况
			if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_startTime)){ 
				if(cityBean.atckLmId > 0 && cityBean.atckLmId != myAlliance.id){
					AllianceBean atckalliance = AllianceBeanDao.inst.getAllianceBean(cityBean.atckLmId);
					if(atckalliance == null){
						cityBean.atckLmId = 0;
						HibernateUtil.update(cityBean);
					}
				}
			}
			Calendar enddate = Calendar.getInstance();
			enddate.setTimeInMillis(AllianceFightMgr.getFightEndTime());
			if(!DateUtils.isInDeadline4Start(city_war_declaration_startTime,enddate.get(Calendar.HOUR_OF_DAY) + ":" + enddate.get(Calendar.MINUTE))){ //其他阶段
				state2 = 0;
			}
			long fightEndTime = AllianceFightMgr.getFightEndTime();
			if(cityBean.atckLmId == -100 || Calendar.getInstance().getTimeInMillis() > fightEndTime){ //战斗结束
				state2 = 0;
			}
			cityInfo.setCityState2(state2);
			info.addCityList(cityInfo);
		}
		
		info.setMyCityCount(myCityCount); //设置乙方占领的城池个数
		
		//设置时段
		if(DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_preparation_startTime)){
			info.setInterval(lm_fight_interval_declare);
		}else if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_startTime)){
			info.setInterval(lm_fight_interval_announce);
		}else if(DateUtils.isInDeadline4Start(city_war_fighting_startTime,city_war_fighting_endTime)){
			info.setInterval(lm_fight_interval_fighting);
		}else{
			info.setInterval(lm_fight_interval_other);
		}
		
		// 设置操作类型
		info.setType(((CityFightInfoReq.Builder)builder).getType()); 
		
		//推荐城池
		List<BidBean> wildBidList = HibernateUtil.list(BidBean.class,"where lmId=" + myAlliance.id + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() + "' and type=2"); //野城
		List<BidBean> myBidList = HibernateUtil.list(BidBean.class, "where lmId=" + myAlliance.id + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() +"' and type=1"); //普通
		if(wildBidList.size() > 0){ //对野城宣战
			info.setRecCityId(0); //野城宣战
		}else if(myBidList == null || myBidList.size() <= 0){//没有对任何城市宣战推荐野城
			info.setRecCityId(1); //推荐打野城
		}else{
			List<CityBean> aCityBeans = HibernateUtil.list(CityBean.class,"where atckLmId=" + myAlliance.id);
			if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_startTime) && (aCityBeans == null || aCityBeans.size() <= 0)){ //揭晓阶段宣战失败宣战失败推荐打野城(宣战了野城不再显示)
					info.setRecCityId(1); //推荐打野城
			}else{
				info.setRecCityId(-1); //已经宣战成功不推荐
			}
		}
		//宣战消息
		List<BidBean> bidList = HibernateUtil.list(BidBean.class, "where bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() +"' order by bidTime desc",0,3); //取最新的三条
		BidMsgInfo.Builder bidMsg = BidMsgInfo.newBuilder();
		if(bidList != null){
			for (BidBean bidBean : bidList) {
				bidMsg.setAllianceName(AllianceMgr.inst.getAllianceName(bidBean.lmId));
				bidMsg.setCityId(bidBean.cityId);
				bidMsg.setBidTime(bidBean.bidTime.getTime() / 1000);
				info.addBidList(bidMsg);
			}
		}
		//倒计时
		info.setCountDown(getCountDown());
		//返回联盟虎符
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if(alliancePlayer.title > 0){ //盟主和副盟主可以看到
			info.setHaveHufu(myAlliance.hufuNum);
		}
		msgSend(PD.S_ALLIANCE_CITYFIGHTINFO_RESP,session,info);
	}
	
	/**
	 * 获得野战城池信息
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void wildCityFightInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null || jz.guoJiaId < 0){
			log.error("未找到君主信息");
			return;
		}
		AllianceBean myAlliance = AllianceMgr.inst.getAllianceByJunZid(jz.getId());
		if(myAlliance == null){
			log.error("没有加入联盟");
			return;
		}
		List<JCZCity> cityListSetting = TempletService.getInstance().listAll(JCZCity.class.getSimpleName());
		CityFightInfoResp.Builder info = CityFightInfoResp.newBuilder();
		//查出野城信息
		/*List<WildCityBean> wildCityBeanList = HibernateUtil.list(WildCityBean.class,"where lmId=" + myAlliance.id);
		Map<Integer,WildCityBean> wildCityBeanMap = new HashMap<Integer,WildCityBean>();
		for (WildCityBean wildCityBeanTmp : wildCityBeanList) {
			wildCityBeanMap.put(wildCityBeanTmp.cityId, wildCityBeanTmp);//每个联盟cityId不会重复
		}*/
		Map<Integer,WildCityBean> wildCityBeanMap = WildCityBeanDao.inst.getMap(myAlliance.id);
		List<BidBean> bidBeanList = HibernateUtil.list(BidBean.class,"where lmId=" + myAlliance.id + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() + "'");
		Map<Integer, BidBean> bidBeanMap = new HashMap<Integer,BidBean>();
		for (BidBean bidBeanTmp : bidBeanList) {
			bidBeanMap.put(bidBeanTmp.cityId, bidBeanTmp);
		}
		List<EnterWarTimeBean> EnterWarlist = HibernateUtil.list(EnterWarTimeBean.class,"where lmId=" + myAlliance.id + " and isIn=1 and enterTime>'" + dayStart() + "' and enterTime<'" + dayEnd() + "'");
		Map<Integer,Integer> enterNumMap = new HashMap<Integer,Integer>();
		for (EnterWarTimeBean enterWarTimeBean : EnterWarlist) {
			if(enterNumMap.containsKey(enterWarTimeBean.cityId)){
				enterNumMap.put(enterWarTimeBean.cityId,enterNumMap.get(enterWarTimeBean.cityId) + 1);
			}else{
				enterNumMap.put(enterWarTimeBean.cityId,1);
			}
		}
		for(JCZCity jczCity:cityListSetting){
			if(jczCity.type == 1) //排除普通城池
				continue;
			CityInfo.Builder cityInfo = CityInfo.newBuilder();
			cityInfo.setCityId(jczCity.id); //设置cityId
			//设置城池状态
			WildCityBean wildCityBean = wildCityBeanMap.get(jczCity.id);
			if(wildCityBean != null &&  wildCityBean.isWin == 1){
				cityInfo.setCityState(city_state_conquer); 
			}else{
				cityInfo.setCityState(city_state_not_conquer);
			}
			if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){
//				List list = HibernateUtil.list(EnterWarTimeBean.class,"where cityId=" + jczCity.id + " and lmId=" + myAlliance.id + " and isIn=1 and enterTime>'" + dayStart() + "' and enterTime<'" + dayEnd() + "'");
				if(enterNumMap.get(jczCity.id) == null){
					cityInfo.setLmNum(0);
				}else{
					cityInfo.setLmNum(enterNumMap.get(jczCity.id));
				}
			}
			BidBean bidBean = bidBeanMap.get(jczCity.id);
			if(bidBean != null){
				cityInfo.setCityState2(1); //已经宣战
			}else{
				cityInfo.setCityState2(0); //未宣战
			}
			info.addCityList(cityInfo);
		}
		//推荐城池
		info.setRecCityId(1);
		//倒计时
		info.setCountDown(getCountDown());
		info.setType(((CityFightInfoReq.Builder)builder).getType()); //设置操作类型
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if(alliancePlayer.title > 0){ //盟主和副盟主可以看到
			info.setHaveHufu(myAlliance.hufuNum);
		}
		msgSend(PD.S_ALLIANCE_CITYFIGHTINFO_RESP,session,info);
	}
	
	/***
	 * 奖励信息
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void cityWarRewardInfo(int id,IoSession session,Builder builder){
		switch (((CityWarRewardReq.Builder)builder).getRewardType()) {
		case 0:
			lmCityWarReward(id, session, builder);
			break;
		case 1:
			personalCityWarReward(id, session, builder);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 获得战报信息
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void cityWarGrandInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		CityWarRewardReq.Builder req = (CityWarRewardReq.Builder) builder;
		int pageNum = req.getRewardType();
		int start = (pageNum - 1) * city_war_fight_log_num;
		List<CWLogBean> logList = HibernateUtil.list(CWLogBean.class,"order by ot desc",start,city_war_fight_log_num); 
		CityWarGrandResp.Builder warGrandList = CityWarGrandResp.newBuilder();
		CityWarGrandInfo.Builder wargand = CityWarGrandInfo.newBuilder();
		if(logList != null){
			for (CWLogBean cwLogBean : logList) {
				wargand.setAllianceName1(cwLogBean.lmName1);
				wargand.setNationId1(cwLogBean.country1);
				wargand.setAllianceName2(cwLogBean.lmName2);
				wargand.setNationId2(cwLogBean.country2);
				wargand.setCityId(cwLogBean.cityId);
				wargand.setTime(cwLogBean.ot.getTime() / 1000);
				warGrandList.addGrandList(wargand);
			}
		}
		msgSend(PD.S_CITYWAR_GRAND_RESP,session,warGrandList);
	}
	
	/**
	 * 竞拍页面返回
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void cityWarBidPageInfo(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;	
		}
		CityWarBidReq.Builder bidreq = (CityWarBidReq.Builder) builder;
		int cityId = bidreq.getCityId();
		//校验是否有联盟
		AlliancePlayer alliancePlayer = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if(alliancePlayer == null || alliancePlayer.lianMengId <= 0){
			log.error("没有联盟");
			return;
		}
		
		//返回消息
		CityWarBidResp.Builder resp = CityWarBidResp.newBuilder();
		Calendar calendar = Calendar.getInstance();
		List<BidBean> bidBeans = HibernateUtil.list(BidBean.class, "where cityId=" + cityId + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() +"' order by priceReal desc,bidTime asc");
		int maxPrice = 0;
		int winLmId = 0;
		boolean isBid = false;
		for (BidBean bidBeanTmp : bidBeans) {
			BidRecord.Builder bidrecord = BidRecord.newBuilder();
			bidrecord.setAllianceName(AllianceMgr.inst.getAllianceName(bidBeanTmp.lmId));
			if(bidBeanTmp.lmId == alliancePlayer.lianMengId || DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){ //自己的竞拍看到的是真是数据
				bidrecord.setHuFuNum(bidBeanTmp.priceReal);
			}else{ //其他人的竞拍数据只能看到缓存
				if(bidBeanTmp.priceCache > 0){
					bidrecord.setHuFuNum(bidBeanTmp.priceCache);
				}else{
					continue; //缓存竞拍护符为0的跳过
				}
			}
			bidrecord.setTime(bidBeanTmp.bidTime.getTime() / 1000);
			if(DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_preparation_startTime) ==false 
					|| alliancePlayer.title > 0){ //不是竞拍阶段，或者是盟主副盟主，则可以查看列表，
				resp.addRecordList(bidrecord); //列表
			}
			if(bidBeanTmp.priceReal > maxPrice){
				maxPrice = bidBeanTmp.priceReal;
				winLmId = bidBeanTmp.lmId;
			}
			if(bidBeanTmp.lmId == alliancePlayer.lianMengId){
				isBid = true;
			}
		}
		//计算bidState
		int bidState = 0;
		CityBean cityBean = CityBeanDao.inst.getCityBeanById(cityId);
		if(DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_preparation_startTime)){ //在宣战时间段内
			if(isBid){
				bidState = 1;
			}
		}else if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){ //揭晓时间段
			if(winLmId == alliancePlayer.lianMengId){
				bidState = 2;
			}else if(isBid){
				bidState = 3;
			}
			//返回进攻方名字
			if(cityBean.atckLmId > 0){
				AllianceBean alliance =AllianceBeanDao.inst.getAllianceBean(cityBean.atckLmId);
				if(alliance != null){
					resp.setGeneral(alliance.name);
				}
			}
		}
		
		resp.setBidState(bidState); //设置状态
		
		if(cityBean != null){
			if(cityBean.lmId > 0){
				AllianceBean alliance = AllianceBeanDao.inst.getAllianceBean(cityBean.lmId);
//				String name = "";
				String aName = "";
				if(alliance == null) {
				}else{
					aName = alliance.name;
					JunZhu junzhu = HibernateUtil.find(JunZhu.class, alliance.creatorId);
//					if(junzhu != null){
//						name = junzhu.name;
//					}
				}
//				resp.setGeneral(name); 2016年5月25日10:59:26 修改为进攻方联盟名字
				resp.setAllianceName(aName);
			}
		}
		//进入战场联盟数
		if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){
			List<EnterWarTimeBean> list = HibernateUtil.list(EnterWarTimeBean.class,"where cityId=" + cityId  + " and lmId=" + alliancePlayer.lianMengId + " and isIn=1 and enterTime>'" + dayStart() + "' and enterTime<'" + dayEnd() + "'");
			if(list == null){
				resp.setLmNum(0);
			}else{
				resp.setLmNum(list.size());
			}
		}
		//设置整点倒计时
		long now =  calendar.getTimeInMillis();
		calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY) + 1);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND,0);
		long diff = (calendar.getTimeInMillis() - now) / 1000;
		resp.setRefreshTime((int)diff);
		AllianceBean myAlliance = AllianceMgr.inst.getAllianceByJunZid(jz.getId());
		if(alliancePlayer.title > 0){ //盟主和副盟主可以看到
			resp.setHaveHufu(myAlliance.hufuNum);
		}
		msgSend(PD.S_CITYWAR_BID_RESP,session,resp);
	}
	
	/**
	 *攻城战操作 
	 * @param id 协议ID
	 * @param session session
	 * @param builder 消息体
	 */
	public void cityWarOperate(int id,IoSession session,Builder builder){
		switch (((CityWarOperateReq.Builder)builder).getOperateType().getNumber()) {
		case CityOperateType.BID_VALUE:
			bid(id,session,builder);
			break;
		case CityOperateType.ENTER_FIGHT_VALUE:
			enterCityWar(id,session,builder);
			break;
		case CityOperateType.GET_REWARD_VALUE:
			getCityWarReward(id,session,builder);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 个人奖励
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void personalCityWarReward(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(aBean == null){
			log.error("没有联盟");
			return;
		}
		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(dayStart());
		calendar.add(Calendar.DATE,-2); //前天
		calendar.set(Calendar.HOUR_OF_DAY,CanShu.REFRESHTIME); //2016年6月17日10:53:35 朱老师确认，今天只能看到前天刷新时间（4点）以后的奖励
		String dt = DateUtils.datetime2Text(calendar.getTime());
		List<LMZAwardBean> list = HibernateUtil.list(LMZAwardBean.class,
				"where jzId=" + jz.id + " and dt>'" +dt+ "' and fromType=2 order by dt desc");
		CityWarRewardResp.Builder rewardList = CityWarRewardResp.newBuilder();
		CityWarRewardInfo.Builder rewardInfo = CityWarRewardInfo.newBuilder(); 
		if(list != null){
			for (LMZAwardBean lmzAwardBean : list) {
				rewardInfo.setId(lmzAwardBean.dbId); //奖励ID
				rewardInfo.setCityId(lmzAwardBean.cityId);
				rewardInfo.setWarType(lmzAwardBean.warType); //战斗类型
				rewardInfo.setResult(lmzAwardBean.result); //个人杀敌数
				rewardInfo.setRewardNum(lmzAwardBean.rewardNum); //道具数量
				int time = awardsTimeCompute(lmzAwardBean.dt);
				if(time < 0) continue;
				rewardInfo.setTime(awardsTimeCompute(lmzAwardBean.dt)); //时间
				rewardInfo.setGetState(lmzAwardBean.getState); //领取状态
				rewardList.addRewardList(rewardInfo);
			}
		}
		rewardList.setRewardType(((CityWarRewardReq.Builder)builder).getRewardType());
		msgSend(PD.S_CITYWAR_REWARD_RESP,session,rewardList);
	}
	
	/**
	 * 联盟奖励
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void lmCityWarReward(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(aBean == null){
			log.error("没有联盟");
			return;
		}
		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(dayStart());
		calendar.add(Calendar.DATE,-2); //前天
		calendar.set(Calendar.HOUR_OF_DAY,CanShu.REFRESHTIME); //2016年6月17日10:53:35 朱老师确认，今天只能看到前天刷新时间（4点）以后的奖励
		String dt = DateUtils.datetime2Text(calendar.getTime());
		List<LMZAwardBean> list = HibernateUtil.list(LMZAwardBean.class,
				"where jzId=" + jz.id + " and dt>'" +dt+ "' and fromType=1 order by dt desc");
		CityWarRewardResp.Builder rewardList = CityWarRewardResp.newBuilder();
		CityWarRewardInfo.Builder rewardInfo = CityWarRewardInfo.newBuilder(); 
		if(list != null){
			for (LMZAwardBean lmzAwardBean : list) {
				rewardInfo.setId(lmzAwardBean.dbId); //奖励ID
				rewardInfo.setCityId(lmzAwardBean.cityId);
				rewardInfo.setWarType(lmzAwardBean.warType); //战斗类型
				rewardInfo.setResult(lmzAwardBean.result); //个人杀敌数
				rewardInfo.setRewardNum(lmzAwardBean.rewardNum); //道具数量
				int time = awardsTimeCompute(lmzAwardBean.dt);
				if(time < 0) continue;
				rewardInfo.setTime(awardsTimeCompute(lmzAwardBean.dt)); //时间
				rewardInfo.setGetState(lmzAwardBean.getState); //领取状态
				rewardList.addRewardList(rewardInfo);
			}
		}
		rewardList.setRewardType(((CityWarRewardReq.Builder)builder).getRewardType());
		msgSend(PD.S_CITYWAR_REWARD_RESP,session,rewardList);
	}	
	
	/**
	 * 进入战场
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void enterCityWar(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(aBean == null){
			log.error("没有联盟");
			return;
		}
		
		CityWarOperateReq.Builder req = (CityWarOperateReq.Builder)builder;
		int cityId = req.getCityId();
		CityBean cityBean = CityBeanDao.inst.getCityBeanById(cityId);
		int lmId = aBean.id;
		int result = 0; //成功
		CityWarOperateResp.Builder resp = CityWarOperateResp.newBuilder();
		//校验时间段
		if(DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_preparation_startTime)){ //宣战时段禁止进入战场,2016-5-21 16:03:28 策划改为揭晓时段就可以进入站场
			result = 2; //2-宣战时段禁止进入战场
			resp.setResult(result);
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
			return;
		}
		if(!DateUtils.isInDeadline4Start(city_war_declaration_startTime,city_war_fighting_endTime)){
			result = 4;//其他时间段，上场结束，下一场未开始
			resp.setResult(result); 
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
			return;
		}else{
			if(jczmap.get(cityId).type == 1){ //普通城池需要校验
				//没有敌人宣战
				if(cityBean != null && cityBean.lmId > 0){
					if(cityBean.lmId == aBean.id && cityBean.atckLmId<=0 && cityBean.atckLmId != -100){
//						List<BidBean> bidList = HibernateUtil.list(BidBean.class, "where cityId=" + cityId + " and lmId=" + aBean.id + " and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() +"'");
//						if(bidList == null || bidList.size() <= 0){
							result = 5; //没有敌人宣战
							resp.setResult(result);
							msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
							return;
//						}
					}
				}
			}
		}
		
		//校验是否是攻守方
		if(jczmap.get(cityId).type == 1){ //普通城池需要校验
			if(cityBean != null && cityBean.atckLmId == -100){
				result = 4;//战斗结束,上场结束，下一场未开始
				resp.setResult(result); 
				msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
				return;
			}
			if(cityBean == null || (cityBean.lmId != lmId && cityBean.atckLmId != lmId)){
				result = 1; // 1-非攻方盟员或非守方盟员
				resp.setResult(result); 
				msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
				return;
			}
		}else if(jczmap.get(cityId).type == 2){ //野城战斗结束后不让进
			//WildCityBean wildCityBean = HibernateUtil.find(WildCityBean.class,"where lmId=" + aBean.id + " and winTime>'" + dayStart() + "' and winTime<'" + dayEnd() +"'"); 
			Map<Integer,WildCityBean> wildCityBeanMap = WildCityBeanDao.inst.getMap(aBean.id);
			boolean isEnd = false;
			for(Integer dbId:wildCityBeanMap.keySet()){
				WildCityBean tmp = wildCityBeanMap.get(dbId);
				if(tmp.winTime.getTime() > dayStart().getTime() && tmp.winTime.getTime() < dayEnd().getTime()){
					isEnd = true;
					break;
				}
			}
			if(isEnd){//今日战斗过结算了
				result = 4;//战斗结束,上场结束，下一场未开始
				resp.setResult(result); 
				msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
				return;
			}
		}
		//校验战斗是否结束
		Calendar calendar = Calendar.getInstance();
		long nowTime = calendar.getTimeInMillis();
		long fightEndTime = AllianceFightMgr.getFightEndTime();
		if(nowTime + 11000  > fightEndTime){//11秒内结束的战斗，就不要再进了。
			BigSwitch.inst.scMgr.fightScenes.clear();//清空战场。
			result = 4; //4-今日战斗结束
			resp.setResult(result); 
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
			return;
		}
		//不能频繁进出
		EnterWarTimeBean eBean = HibernateUtil.find(EnterWarTimeBean.class,"where jzId=" + jz.id + " and cityId=" + cityId);
		int cd = 0;
		if(eBean != null){
			long time1 = Calendar.getInstance().getTimeInMillis();
			long time2 =  eBean.enterTime.getTime();
			if(((time1 - time2) / 1000) < city_war_enter_cd){ //校验cd
				cd = (int)(eBean.enterTime.getTime() / 1000 + city_war_enter_cd - Calendar.getInstance().getTimeInMillis() / 1000);
				result = 3; //3-不能频繁进出
				resp.setCdTime(cd); //冷却时间
				resp.setResult(result); 
				msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
				return;
			}
		}
		//更新进入战场时间
		Date date = new Date();
		if(eBean == null){
			eBean = new EnterWarTimeBean();
			eBean.jzId = jz.id;
			eBean.cityId = cityId;
			eBean.lmId = aBean.id;
			eBean.isIn = 1; //进入战场
			eBean.enterTime = date;
			HibernateUtil.insert(eBean);
		}else{
			eBean.lmId = aBean.id;
			eBean.isIn = 1; //进入战场
			eBean.enterTime = date;
			HibernateUtil.update(eBean);
		}
		//operateType : ENTER_FIGHT  1-非攻方盟员或非守方盟员 2-非战斗时段 3-不能频繁进出 4-今日战斗结束 5-没有敌人宣战
		resp.setResult(result);
		msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
	}
	
	//出战场
	public void exitScene(long jzId,int cityId){
		if(jzId > 0 && cityId > 0){
			String sql = "update " + EnterWarTimeBean.class.getSimpleName() + " set isIn=0 where jzId=" + jzId + " and cityId=" + cityId;
			HibernateUtil.executeSql(sql);
		}
	}
	
	/**
	 * 领奖
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getCityWarReward(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(aBean == null){
			log.error("没有联盟");
			return;
		}
		
		CityWarOperateReq.Builder req = (CityWarOperateReq.Builder)builder;
		int reward = req.getRewardId();
		LMZAwardBean lmzAwardBean = HibernateUtil.find(LMZAwardBean.class,reward);
		if(lmzAwardBean == null){
			log.error("奖励不存在");
			return;
		}
		
		//校验是否领取的自己的奖励
		if(lmzAwardBean.jzId != jz.id){
			log.error("只能领取自己的奖励");
			return;
		}
		CityWarOperateResp.Builder resp = CityWarOperateResp.newBuilder();
		if(lmzAwardBean.getState == 1){
			resp.setResult(1); //领奖失败已经领取
			msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
			return;
		}
		
		String awardStr = "0:" + AwardMgr.ITEM_GONG_XUN + ":" + lmzAwardBean.rewardNum;
		AwardMgr.inst.giveReward(session,awardStr,jz);
		resp.setResult(0);
		//修改状态
		lmzAwardBean.getState = 1;
		HibernateUtil.update(lmzAwardBean);
		//required int32 result = 1;//0-成功    operateType ：GET_REWARD  1-领取失败，奖励已领取
		msgSend(PD.S_CITYWAR_OPERATE_RESP,session,resp);
	}
	
	/**
	 * 获取积分战报
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getScoreResult(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("未找到君主信息");
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(aBean == null){
			log.error("没有联盟");
			return;
		}
		CityWarScoreResultReq.Builder req = (CityWarScoreResultReq.Builder)builder;
		int cityId = req.getCityId();
		CityWarScoreResultResp.Builder resp = CityWarScoreResultResp.newBuilder();
		byte[] bytes = null;
		if(cityId <= 0){//请求野城
			bytes = Redis.getInstance().hget(REDIS_KEY_SCORE_RESULT.getBytes(),(HASHKEY_YECHENG + "_" + aBean.id).getBytes());
		}else{
			JCZCity jczCity = jczmap.get(cityId);
			if(jczCity == null){
				log.error("请求的城池不存在");
				return;
			}
			bytes = Redis.getInstance().hget(REDIS_KEY_SCORE_RESULT.getBytes(), String.valueOf(cityId).getBytes());
		}
		if(bytes == null){
			msgSend(PD.S_CITYWAR_SCORE_RESULT_RESP,session,resp);
			return;
		}
		ScoreList.Builder scoreList = ScoreList.newBuilder();
		try {
			scoreList.mergeFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		} 
		if(scoreList != null){
			for (PlayerScore playerScore : scoreList.getListList()) {
				ScoreInfo.Builder info = ScoreInfo.newBuilder();
				info.setRank(playerScore.getRank());
				info.setJzId(playerScore.getJzId());
				info.setScore(playerScore.getJiFen());
				info.setKillNum(playerScore.getKillCnt());
				info.setLianShaNum(playerScore.getLianSha());
				info.setGongXun(playerScore.getGx());
				info.setLmName(playerScore.getLmName());
				JunZhu jzTemp = HibernateUtil.find(JunZhu.class, playerScore.getJzId());
				info.setJzName(jzTemp.getName());
				//1守，2攻 Side
				if(cityId <= 0){ //野城
					info.setSide(2); //野城2
				}else{
					info.setSide(playerScore.getSide());
				}
				if(aBean.name.equals(playerScore.getLmName())){ //自己盟友
					info.setIsEnemy(1);
				}else{ //敌人
					info.setIsEnemy(0);
				}
				resp.addScoreList(info);
			}	
			resp.setDate(scoreList.getDateTime());
			resp.setCityName(HeroService.getNameById(""+jczmap.get(scoreList.getCityId()).name));
			resp.setIsNpc(scoreList.getIsNpc());
		}
		msgSend(PD.S_CITYWAR_SCORE_RESULT_RESP,session,resp);
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
	 * 获取阶段倒计时
	 * @return int 秒数
	 */
	public int getCountDown(){
		Calendar calendar = Calendar.getInstance();
		long nowTime = calendar.getTimeInMillis();
		long nextTime = calendar.getTimeInMillis();
		if (DateUtils.isInDeadline4Start(city_war_declaration_startTime, city_war_preparation_startTime)) { // 在宣战时间段内
			String[] nextTimeArr = city_war_preparation_startTime.split(":");
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(nextTimeArr[0]));
			nextTime = calendar.getTimeInMillis();
		} else if (DateUtils.isInDeadline4Start(city_war_preparation_startTime, city_war_fighting_startTime)) { // 揭晓时间段
			String[] nextTimeArr = city_war_fighting_startTime.split(":");
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(nextTimeArr[0]));
			nextTime = calendar.getTimeInMillis();
		} else if (DateUtils.isInDeadline4Start(city_war_fighting_startTime, city_war_fighting_endTime)) {// 战斗阶段
			String[] nextTimeArr = city_war_declaration_startTime.split(":");
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(nextTimeArr[0]));
			nextTime = calendar.getTimeInMillis();
		}
		return (int)(nextTime - nowTime);
	}
	
	/**
	 * 更新竞拍记录缓存
	 */
	public void refreshBidPrice(){
		String sql = "update "+ BidBean.class.getSimpleName() +" set priceCache=priceReal";
		HibernateUtil.executeSql(sql);
	}
	
	/**
	 * 推送宣战信息
	 * @param allianceName 联盟名字
	 * @param cityId 宣战城池ID
	 * @param bidTime 宣战时间 
	 */
	public void sendBidMsg(String allianceName,int cityId,long bidTime){
		if(allianceName == null || "".equals(allianceName) || cityId < 0 || bidTime < 0){
			return;
		}
		//构造消息
		ProtobufMsg pMsg = new ProtobufMsg();
		pMsg.id = PD.S_CITYWAR_BID_MSG_RESP;
		BidMsgInfo.Builder bidMsg = BidMsgInfo.newBuilder();
		bidMsg.setAllianceName(allianceName);
		bidMsg.setCityId(cityId);
		bidMsg.setBidTime(bidTime);
		pMsg.builder = bidMsg;
		//联盟推动宣战情报信息
		Iterator<IoSession> it = TXSocketMgr.inst.acceptor.getManagedSessions().values().iterator();
		while(it.hasNext()){
			IoSession session;
			session = it.next();
			Object lmName=session.getAttribute(SessionAttKey.LM_NAME, "***");
			if(!"***".equals(lmName)){
				session.write(pMsg);
			}
		}
	}
	
	/**
	 * 竞拍结算
	 */
	public void bidJieSuan(){
		BigSwitch.inst.scMgr.fightScenes.clear();//清空战场。
		refreshBidPrice(); //刷新竞拍缓存
		List<JCZCity> citylist = TempletService.getInstance().listAll(JCZCity.class.getSimpleName());
		Map<Integer,Integer>  hufuMap = new HashMap<Integer,Integer>(); //缓存返还护符数
		/*List<CityBean> cityBeanList = HibernateUtil.list(CityBean.class,"");
		Map<Integer,CityBean> cityBeanMap = new HashMap<Integer,CityBean>();
		//转换一下格式方便处理
		if(cityBeanList != null){
			for(CityBean cityBeanTmp:cityBeanList){ 
				cityBeanMap.put(cityBeanTmp.cityId,cityBeanTmp);
			}
		}*/
		Map<Integer, List<BidBean>> bidbeanListMapReal = new HashMap<Integer,List<BidBean>>();
		List<BidBean> AllBidList = HibernateUtil.list(BidBean.class, "where priceReal>0 and bidTime>='" + dayStart() + "' and bidTime<='" + dayEnd() + "' order by priceReal desc,bidTime asc");
		Map<Integer, Integer> lmIdMap = new HashMap<>(); //用于红点推送
		for (BidBean bidBeanTmp : AllBidList) {
			if(bidBeanTmp.type == 2){ //野城
				lmIdMap.put(bidBeanTmp.lmId,null);
				continue;
			}
			List<BidBean> listTemp = bidbeanListMapReal.get(bidBeanTmp.cityId);
			if(listTemp == null){
				listTemp = new ArrayList<BidBean>();
			}
			listTemp.add(bidBeanTmp);
			bidbeanListMapReal.put(bidBeanTmp.cityId,listTemp);
		}
		for (JCZCity jczCity : citylist) {
			if(jczCity.type == 2) continue;//野城
//			List<BidBean> beans = HibernateUtil.list(BidBean.class, "where cityId=" + jczCity.id + " and bidTime>='" + dayStart() + "' and bidTime<='" + dayEnd() + "' order by priceReal desc,bidTime asc");
			List<BidBean> beans = bidbeanListMapReal.get(jczCity.id);
			CityBean cityBean = CityBeanDao.inst.getCityBeanById(jczCity.id);
			if(beans != null && beans.size() > 0){
				//竞拍失败返还虎符
				int atckLmid = 0;
				for (BidBean bidBean : beans) {
					AllianceBean allianceBean = AllianceBeanDao.inst.getAllianceBean(bidBean.lmId);
					if(allianceBean == null) continue;//特殊处理联盟不存在情况
					if(atckLmid <= 0){
						atckLmid = bidBean.lmId; //只存一次，查询结果已经排好序，第一个一定是竞拍成功的联盟
					}
					if(bidBean.dbId == beans.get(0).dbId) continue; //竞拍成功不返回
					//虎符
					if(hufuMap.containsKey(bidBean.lmId)){
						hufuMap.put(bidBean.lmId,hufuMap.get(bidBean.lmId) + bidBean.priceReal);
					}else{
						hufuMap.put(bidBean.lmId,bidBean.priceReal);
					}
				}
				//结算保存
				if(atckLmid > 0){
					if(cityBean == null){
						cityBean = new CityBean();
						cityBean.cityId = jczCity.id;
						cityBean.lmId = 0; //没人占领
						cityBean.atckLmId = atckLmid;
						CityBeanDao.inst.getMap().put(jczCity.id, cityBean);
						HibernateUtil.insert(cityBean);
					}else{
						cityBean.atckLmId = atckLmid;
						HibernateUtil.update(cityBean);
					}
					//联盟ID用于红点推送
					if(cityBean.lmId > 0 && !lmIdMap.containsKey(cityBean.lmId)){
						lmIdMap.put(cityBean.lmId,null);
					}
					if(!lmIdMap.containsKey(atckLmid)){
						lmIdMap.put(atckLmid,null);		
					}
				} 
			}else{ //没有人宣战给守城联盟发奖励
				if(cityBean != null && cityBean.lmId > 0){ //联盟镇守
					String awardStr = jczCity.award;
					List<AwardTemp> alist = AwardMgr.inst.parseAwardConf(awardStr);
					int cityGX = alist.get(0).itemNum;
					Set<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(cityBean.lmId);
					Date t = new Date();
					for (AlliancePlayer member : memberList) {
						LMZAwardBean b = new LMZAwardBean();
						b.jzId = member.junzhuId;
						b.cityId = jczCity.id;
						b.warType = 2; //未发生战斗
						b.result = 1;
						b.rewardNum = cityGX/memberList.size();
						b.getState = 0;
						b.dt = t;
						b.fromType = 1;
						HibernateUtil.insert(b);
					}
				}
			}
		}
		//保存
		for (Map.Entry<Integer, Integer> entry : hufuMap.entrySet()) {  
			AllianceBean allianceBean =AllianceBeanDao.inst.getAllianceBean(entry.getKey());
			if(allianceBean != null){
				AllianceMgr.inst.changeAlianceHufu(allianceBean,entry.getValue());
			}
		} 
		FightScene.freeFuHuoUsedTimes.clear();
		FightScene.buyXPTimes.clear();
		//推送红点
		for (Integer key:lmIdMap.keySet()){
			Set<AlliancePlayer> aList = AllianceMgr.inst.getAllianceMembers(key);
			for (AlliancePlayer alliancePlayer : aList) {
				IoSession sessionTmp = AccountManager.getIoSession(alliancePlayer.junzhuId);
				if(sessionTmp != null){
					FunctionID.pushCanShowRed(alliancePlayer.junzhuId,sessionTmp,FunctionID.city_war_can_enter);
				}
			}
		}
	}
	
	/**
	 * day起始时间
	 * @return Date
	 */ 
	public Date dayStart(){
		return DateUtils.getDayStart(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
	}
	
	/**
	 * day 结束时间
	 * @return Date
	 */
	public Date dayEnd(){
		return DateUtils.getDayEnd(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
	}
	
	/**
	 * 计算奖励时间（今天、昨天、前天）
	 * @param date
	 * @return int 
	 */
	public int awardsTimeCompute(Date date){
		if(DateUtils.isSameDay(date)){
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE,-1); //昨天
		if(DateUtils.isSameDay(date,calendar.getTime())){
			return 1;
		}
		calendar.add(Calendar.DATE,-1); //前天
		if(DateUtils.isSameDay(date,calendar.getTime())){
			return 2;
		}
		return -1;
	}
	
	/**
	 * 保存战报日志方法(成功占领调用此方法保存日志)
	 * @param lmId1 占领方联盟Id
	 * @param lmId2 被占领方联盟Id 0代表NPC
	 * @param cityId 占领城池ID
	 * @param ot 占领时间
	 */
	public void saveLog(int lmId1,int lmId2,int cityId,Date ot){
		if(lmId1 <= 0 || cityId <= 0 || lmId1 == lmId2){ //城池易主才发送
			return;
		}
		if(ot == null){
			ot = new Date();
		}
		CWLogBean cwLogBean = new CWLogBean();
		AllianceBean allianceBean1 = AllianceBeanDao.inst.getAllianceBean(lmId1);
		if(allianceBean1 == null){
			return;
		}
		cwLogBean.lmName1 = allianceBean1.name;
		cwLogBean.country1 = allianceBean1.country;
		String name2 = "";
		if(lmId2 > 0){
			AllianceBean allianceBean2 = AllianceBeanDao.inst.getAllianceBean(lmId2);
			if(allianceBean2 == null){
				return;
			}
			cwLogBean.lmName2 = allianceBean2.name;
			cwLogBean.country2 = allianceBean2.country;
			name2 = allianceBean2.name;
		}else{ //npc
			name2 = jczmap.get(cityId).NPCname;
			cwLogBean.lmName2 = name2;
			cwLogBean.country2 = -1; //前端会处理
		}
		cwLogBean.cityId = cityId;
		cwLogBean.ot = ot;
		//发送广播
		String cityName = HeroService.getNameById(jczmap.get(cityId).name + "");
		EventMgr.addEvent(allianceBean1.id,ED.CITY_WAR_ZHAN_LING, new Object[] { cwLogBean.lmName1,name2,lmId2>0?false:true,cityName});
		HibernateUtil.insert(cwLogBean);
	}
	
	/**
	 * 每天定时清除竞拍记录（定时器四点清除数据）
	 */
	public void regularClearData(){
		//清理竞拍记录
		log.info("定时清理竞拍记录开始...");
		String sql1 = "delete from " + BidBean.class.getSimpleName() + " where bidTime<'"+ new Date()+"'";
		HibernateUtil.executeSql(sql1);
		log.info("定时清理竞拍记录结束...");
		//重置结算数据
		log.info("定时重置结算数据开始...");
		String sql2 = "update " + CityBean.class.getSimpleName() + " set atckLmId=0";
		HibernateUtil.executeSql(sql2);
		for(Integer cityId : jczmap.keySet()){
			if(jczmap.get(cityId).type == 2) continue;
			CityBean cityBean = CityBeanDao.inst.getMap().get(cityId);
			if(cityBean == null) continue;
			cityBean.atckLmId = 0;
		}
		log.info("定时重置结算数据结束...");
		/*//重置进入战场数据
		log.info("定时重置进入战场数据开始...");
		String sql3 = "delete from " + EnterWarTimeBean.class.getSimpleName() + " where enterTime<'"+ new Date()+"'";
		HibernateUtil.executeSql(sql3);
		log.info("定时重置进入战场数据结束...");*/
		//清理过期奖励
		log.info("定时清理过期奖励开始...");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7); //奖励保存七天，前端展示的是三天内的奖励
		String sql4 = "delete from " + LMZAwardBean.class.getSimpleName() + " where dt<'"+ calendar.getTime() +"'";
		HibernateUtil.executeSql(sql4);
		log.info("定时清理过期奖励结束...");
	}
	
	/**
	 * 郡城战期间不能解散联盟
	 * @return true 可以解散
	 */
	public boolean isLmCanDismiss(){
		long fightEndTime = AllianceFightMgr.getFightEndTime();
		Date startTime = DateUtils.parseHourMinute(BidMgr.city_war_fighting_startTime);
		long nowTime = Calendar.getInstance().getTimeInMillis();
		if(nowTime >= startTime.getTime() && nowTime <= fightEndTime){
			return false;
		}else {
			return true;
		}
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
			canEnterWarRed(session,jz);
			isAwardRed(session, jz);
		}
		break;
		case ED.CITY_WAR_FIGHT_JIESUAN:{
			Object[] obj = (Object[])event.param;
			List<Long> jzIdList = (List<Long>)obj[0];
			if(jzIdList == null) return;
			int type = (int) obj[1];
			sendAwardRed(jzIdList,type);
		}
		break;
		case ED.CITY_WAR_BID:{
			Object[] obj = (Object[])event.param;
			AllianceBean allianceBean = (AllianceBean) obj[0];
			int cityId = (int) obj[1];
			JCZCity jczCity = jczmap.get(cityId);
			if(jczCity != null && jczCity.type == 2){
				sendCanEnterWarRedToAllMembers(allianceBean);
			}
		}
		break;
		default:
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		EventMgr.regist(ED.CITY_WAR_FIGHT_JIESUAN,this);
		EventMgr.regist(ED.CITY_WAR_BID, this);
	}
	
	/**
	 * 有战场可进入红点
	 * @param session
	 * @param jz
	 */
	public void canEnterWarRed(IoSession session,JunZhu jz){
		AllianceBean allianceBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(allianceBean == null) return;
		List<EnterWarTimeBean> eBeanList = HibernateUtil.list(EnterWarTimeBean.class,"where jzId=" + jz.id + " and enterTime>'" + dayStart() + "' and enterTime<'" + dayEnd() +"'");
		if(eBeanList == null || eBeanList.size() <=0){ //今天没进入过战场
			/*List<CityBean> AllBidList = HibernateUtil.list(CityBean.class, "");*/
			Map<Integer, CityBean> m = CityBeanDao.inst.getMap();
			for (Integer cityId : m.keySet()) {
				CityBean cityBean = m.get(cityId);
				if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){
					if(cityBean.atckLmId >0 && (cityBean.atckLmId == allianceBean.id || cityBean.lmId == allianceBean.id)){ //攻方或守方可以进入战场
						FunctionID.pushCanShowRed(jz.id,session, FunctionID.city_war_can_enter);
						return;
					}
				}
			}
			//是否对野城宣战
			if(DateUtils.isInDeadline4Start(city_war_preparation_startTime,city_war_fighting_endTime)){
				List<BidBean> bidBeanList = HibernateUtil.list(BidBean.class,"where lmId=" + allianceBean.id + " and type=2 and bidTime>'" + dayStart() + "' and bidTime<'" + dayEnd() + "'");
				if(bidBeanList != null && bidBeanList.size() > 0){
					FunctionID.pushCanShowRed(jz.id,session, FunctionID.city_war_can_enter);
				}
			}
		}
	}
	
	/**
	 * 有奖励领红点
	 * @param session
	 * @param jz
	 */
	public void isAwardRed(IoSession session,JunZhu jz){
		AllianceBean allianceBean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		if(allianceBean == null) return;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE,-2); //前天
		String dt = DateUtils.datetime2Text(calendar.getTime());
		List<LMZAwardBean> lmAward = HibernateUtil.list(LMZAwardBean.class,
				"where jzId=" + jz.id + " and dt>'" +dt+ "' and fromType=1 and getState=0 order by dt desc");
		if(lmAward != null && lmAward.size() > 0){
			FunctionID.pushCanShowRed(jz.id,session,FunctionID.city_war_lianmeng_award);
		}
		List<LMZAwardBean> grAward = HibernateUtil.list(LMZAwardBean.class,
				"where jzId=" + jz.id + " and dt>'" +dt+ "' and fromType=2 and getState=0 order by dt desc");
		if(grAward != null && grAward.size() > 0){
			FunctionID.pushCanShowRed(jz.id,session,FunctionID.city_war_personal_award);
		}
	}
	
	/**
	 * 推送奖励红点
	 * @param session
	 * @param alliancePlayers
	 * @param type
	 */
	public void sendAwardRed(List<Long> jzIdList,int type){
		if(jzIdList == null) return;
		int functionId = 0;
		if(type == 1){ //联盟奖励
			functionId = FunctionID.city_war_lianmeng_award;
		}else if(type == 2){ //个人奖励
			functionId = FunctionID.city_war_personal_award;
		}else{
			return;
		}
		for (Long jzId : jzIdList) {
			IoSession sessionTmp = AccountManager.getIoSession(jzId);
			if(sessionTmp != null){
				FunctionID.pushCanShowRed(jzId,sessionTmp,functionId);
			}
		}
	}
	
	public void sendCanEnterWarRedToAllMembers(AllianceBean allianceBean){
		if(allianceBean == null) return;
		Set<AlliancePlayer> aList = AllianceMgr.inst.getAllianceMembers(allianceBean.id);
		List<EnterWarTimeBean> enterWarTimeBeans = HibernateUtil.list(EnterWarTimeBean.class,"where lmId=" + allianceBean.id + " and enterTime>'" + dayStart() + "' and enterTime<'" + dayEnd() +"'");
		Map<Long, EnterWarTimeBean> enterMap = new HashMap<Long,EnterWarTimeBean>();
		for (EnterWarTimeBean enterWarTimeBean : enterWarTimeBeans) {
			enterMap.put(enterWarTimeBean.jzId, enterWarTimeBean);
		}
		if(aList != null){
			for (AlliancePlayer alliancePlayer : aList) {
				IoSession sessionTmp = AccountManager.getIoSession(alliancePlayer.junzhuId);
				EnterWarTimeBean enterWarTimeBean = enterMap.get(alliancePlayer.junzhuId); 
				if((enterWarTimeBean == null || !DateUtils.isSameDay(enterWarTimeBean.enterTime)) && sessionTmp != null){
					FunctionID.pushCanShowRed(alliancePlayer.junzhuId,sessionTmp,FunctionID.city_war_can_enter);
				}
			}
		}
	}
}
