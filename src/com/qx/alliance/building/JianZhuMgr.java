package com.qx.alliance.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.LianMengKeZhan;
import com.manu.dynasty.template.LianMengShangPu;
import com.manu.dynasty.template.LianMengShuYuan;
import com.manu.dynasty.template.LianMengTuTeng;
import com.manu.dynasty.template.LianMengZongMiao;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.util.RandomUtil;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.TypeInfo;
import qxmobile.protobuf.JianZhu.JiHuoLMKJReq;
import qxmobile.protobuf.JianZhu.JiHuoLMKJResp;
import qxmobile.protobuf.JianZhu.JianZhuInfo;
import qxmobile.protobuf.JianZhu.JianZhuList;
import qxmobile.protobuf.JianZhu.KeJiList;

/**
 * 联盟建筑管理器
 * @author 康建虎
 *
 */
public class JianZhuMgr extends EventProc{
	public static JianZhuMgr inst;
	public static Logger log = LoggerFactory.getLogger(JianZhuMgr.class.getSimpleName());
	
	/**	所有的科技类型，要是有新加的类型，请在这里添加上 **/
	public int[] kejiTypes = new int[]{101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
										111, 204, 205, 301};

	public void getInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		JianZhuLvBean bean = JianZhuLvBeanDao.inst.getJianZhuBean(member.lianMengId);
		if(bean == null){
			bean = insertJianZhuLvBean(member.lianMengId, bean);
		}
		////1客栈；2书院；3图腾；4商铺；5宗庙
		JianZhuList.Builder ret = JianZhuList.newBuilder();
		int[] lvArr = {bean.keZhanLv,bean.shuYuanLv,bean.tuTengLv,bean.shangPuLv,bean.zongMiaoLv};
		for(int lv:lvArr){
			JianZhuInfo.Builder info = JianZhuInfo.newBuilder();
			info.setLv(lv);
			ret.addList(info);
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_JIAN_ZHU_INFO;
		msg.builder = ret;
		session.write(msg);
	}
	
	public void sendError(int cmd, IoSession session, String msg) {
		sendError(cmd, session, msg,PD.S_JIAN_ZHU_UP);
	}
	public void sendError(int cmd, IoSession session, String msg,int pctID) {
		if (session == null) {
			log.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		ProtobufMsg p = new ProtobufMsg();
		p.id = pctID;
		p.builder = test;
		session.write(p);
	}

	@SuppressWarnings("unused")
	public synchronized void up(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		if(member.title != AllianceMgr.TITLE_LEADER) {
			log.error("升级联盟建筑失败，君主:{}没有权限，title:{}", jz.id, member.title);
			sendError(1,session,"没有联盟数据");
			return;
		}
		JianZhuLvBean bean = JianZhuLvBeanDao.inst.getJianZhuBean(member.lianMengId);
		if(bean == null) {
			bean = insertJianZhuLvBean(member.lianMengId, bean);
		}
		AllianceBean lmBean = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
		if(lmBean == null){
			sendError(3,session,"没有联盟数据");
			return;
		}
		//1客栈；2书院；3图腾；4商铺；5宗庙
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int code = req.getErrorCode();
		String buildName = "";
		boolean succeed = false;
		switch(code){
		case 1:{
			//upKeZhan();
			List<LianMengKeZhan> list = TempletService.listAll(LianMengKeZhan.class.getSimpleName());
			if(list == null){
				sendError(10, session, "配置缺失。");
				log.error("升级联盟客栈失败，找不到联盟客栈配置");
				return;
			}
			if(bean.keZhanLv>=list.size()){
				sendError(20, session, "已满级");
				log.error("升级联盟客栈失败，联盟:{}的客栈等级已升级到最高等级:{}", lmBean.id, list.size());
				return;
			}
			if(bean.keZhanLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
				log.error("升级联盟客栈失败，联盟:{}的客栈等级已达到联盟当前的等级:{}", lmBean.id, lmBean.level);
				return;
			}
			LianMengKeZhan conf = list.get(bean.keZhanLv - 1);
			if(conf == null) {
				sendError(40, session, "配置错误");
				log.error("升级联盟客栈失败，找不到联盟客栈等级为:{}的配置", bean.keZhanLv);
				return;
			}
			if(conf.keZhan_lvUp_value > lmBean.build) {
				sendError(50, session, "联盟建设值不足");
				log.error("升级联盟客栈失败，联盟:{}当前建设值:{},不足:{}", lmBean.id, lmBean.build, conf.keZhan_lvUp_value);
				return;
			}
			bean.keZhanLv+=1;
			HibernateUtil.update(bean);
			lmBean.build -= conf.keZhan_lvUp_value;
			HibernateUtil.save(lmBean);
			sendError(0, session, "升级成功");
			buildName = HeroService.getNameById(9010+"");
			log.info("升级联盟客栈成功，联盟:{}客栈等级从{}升级到{}消耗建设值:{}",lmBean.id, bean.keZhanLv, bean.keZhanLv+1, conf.keZhan_lvUp_value);
			succeed = true;
			break;
		}
		case 2:{
			List<LianMengShuYuan> list = TempletService.listAll(LianMengShuYuan.class.getSimpleName());
			if(list == null){
				sendError(10, session, "配置缺失。");
				log.error("升级联盟书院失败，找不到联盟书院配置");
				return;
			}
			if(bean.shuYuanLv>=list.size()){
				sendError(20, session, "已满级");
				log.error("升级联盟书院失败，联盟:{}的书院等级已升级到最高等级:{}", lmBean.id, list.size());
				return;
			}
			if(bean.shuYuanLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
				log.error("升级联盟书院失败，联盟:{}的书院等级已达到联盟当前的等级:{}", lmBean.id, lmBean.level);
				return;
			}
			LianMengShuYuan conf = list.get(bean.shuYuanLv - 1);
			if(conf == null) {
				sendError(40, session, "配置错误");
				log.error("升级联盟书院失败，找不到联盟书院等级为:{}的配置", bean.shuYuanLv);
				return;
			}
			if(conf.shuYuan_lvUp_value > lmBean.build) {
				sendError(50, session, "联盟建设值不足");
				log.error("升级联盟书院失败，联盟:{}当前建设值:{},不足:{}", lmBean.id, lmBean.build, conf.shuYuan_lvUp_value);
				return;
			}
			lmBean.build -= conf.shuYuan_lvUp_value;
			HibernateUtil.save(lmBean);
			bean.shuYuanLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "升级成功");
			buildName = HeroService.getNameById(9020+"");
			log.info("升级联盟书院成功，联盟:{}书院等级从{}升级到{}消耗建设值:{}",lmBean.id, bean.keZhanLv, bean.keZhanLv+1, conf.shuYuan_lvUp_value);
			succeed = true;
			break;
		}
		case 4:{ // 商铺
			List<LianMengShangPu> list = TempletService.listAll(LianMengShangPu.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengShangPu配置缺失。");
				log.error("升级联盟商铺失败，找不到联盟商铺配置");
				return;
			}
			if(bean.shangPuLv>=list.size()){
				sendError(20, session, "bean.shangPuLv已满级");
				log.error("升级联盟商铺失败，联盟:{}的商铺等级已升级到最高等级:{}", lmBean.id, list.size());
				return;
			}
			if(bean.shangPuLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
				log.error("升级联盟商铺失败，联盟:{}的商铺等级已达到联盟当前的等级:{}", lmBean.id, lmBean.level);
				return;
			}
			LianMengShangPu conf = list.get(bean.shangPuLv - 1);
			if(conf == null) {
				sendError(40, session, "配置错误");
				log.error("升级联盟商铺失败，联盟:{}当前建设值:{},不足:{}", lmBean.id, lmBean.build, conf.shangPu_lvUp_value);
				return;
			}
			if(conf.shangPu_lvUp_value > lmBean.build) {
				sendError(50, session, "联盟建设值不足");
				log.error("升级联盟商铺失败，找不到联盟商铺等级为:{}的配置", bean.shangPuLv);
				return;
			}
			lmBean.build -= conf.shangPu_lvUp_value;
			HibernateUtil.save(lmBean);
			bean.shangPuLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟商铺升级成功");
			buildName = HeroService.getNameById(9040+"");
			log.info("升级联盟商铺成功，联盟:{}商铺等级从{}升级到{}消耗建设值:{}",lmBean.id, bean.keZhanLv, bean.keZhanLv+1, conf.shangPu_lvUp_value);
			succeed = true;
			break;
			
		}
		case 3:{ 
			List<LianMengTuTeng> list = TempletService.listAll(LianMengTuTeng.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengTuTeng配置缺失。");
				log.error("升级联盟图腾失败，找不到联盟图腾配置");
				return;
			}
			if(bean.tuTengLv>=list.size()){
				sendError(20, session, "bean.tuTengLv已满级");
				log.error("升级联盟图腾失败，联盟:{}的图腾等级已升级到最高等级:{}", lmBean.id, list.size());
				return;
			}
			if(bean.tuTengLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
				log.error("升级联盟图腾失败，联盟:{}的图腾等级已达到联盟当前的等级:{}", lmBean.id, lmBean.level);
				return;
			}
			LianMengTuTeng conf = list.get(bean.tuTengLv - 1);
			if(conf == null) {
				sendError(40, session, "配置错误");
				log.error("升级联盟图腾失败，联盟:{}当前建设值:{},不足:{}", lmBean.id, lmBean.build, conf.tuTeng_lvUp_value);
				return;
			}
			if(conf.tuTeng_lvUp_value > lmBean.build) {
				sendError(50, session, "联盟建设值不足");
				log.error("升级联盟图腾失败，找不到联盟图腾等级为:{}的配置", bean.tuTengLv);
				return;
			}
			lmBean.build -= conf.tuTeng_lvUp_value;
			HibernateUtil.save(lmBean);
			bean.tuTengLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟图腾升级成功");
			buildName = HeroService.getNameById(9030+"");
			log.info("升级联盟图腾成功，联盟:{}图腾等级从{}升级到{}消耗建设值:{}",lmBean.id, bean.keZhanLv, bean.keZhanLv+1, conf.tuTeng_lvUp_value);
			succeed = true;
			break;
			
		}
		case 5:{
			List<LianMengZongMiao> list = TempletService.listAll(LianMengZongMiao.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengZongMiao配置缺失。");
				log.error("升级联盟宗庙失败，找不到联盟宗庙配置");
				return;
			}
			if(bean.zongMiaoLv>=list.size()){
				sendError(20, session, "bean.zongMiaoLv已满级");
				log.error("升级联盟宗庙失败，联盟:{}的宗庙等级已升级到最高等级:{}", lmBean.id, list.size());
				return;
			}
			LianMengZongMiao conf = getLianMengZongMiaoCfgByLevel(bean.zongMiaoLv);
			if(conf == null) {
				sendError(50, session, "配置错误");
				log.error("升级联盟宗庙失败，找不到联盟宗庙等级为:{}的配置", bean.zongMiaoLv);
				return;
			}
			if(lmBean.level < conf.alliance_lv_needed){
				sendError(60, session, "联盟等级不够");
				log.error("升级联盟宗庙失败，联盟:{}的等级不足以升级宗庙", lmBean.id, lmBean.level);
				return;
			}
			if(conf.zongMiao_lvUp_value > lmBean.build) {
				sendError(40, session, "联盟建设值不足");
				log.error("升级联盟宗庙失败，联盟:{}当前建设值:{},不足:{}", lmBean.id, lmBean.build, conf.zongMiao_lvUp_value);
				return;
			}
			lmBean.build -= conf.zongMiao_lvUp_value;
			HibernateUtil.save(lmBean);
			bean.zongMiaoLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟宗庙升级成功");
			buildName = HeroService.getNameById(9050+"");
			log.info("升级联盟宗庙成功，联盟:{}宗庙等级从{}升级到{}消耗建设值:{}",lmBean.id, bean.keZhanLv, bean.keZhanLv+1, conf.zongMiao_lvUp_value);
			succeed = true;
			break;
			
		}
		default:
			return;
		}
		if(succeed) {
			AllianceMgr.inst.processHaveAlliance(jz, PD.ALLIANCE_INFO_REQ, session, member);
			LianmengEvent lmEvent = AllianceMgr.inst.lianmengEventMap.get(20);
			if(lmEvent != null) {
				String eventStr = lmEvent.str == null ? "" : lmEvent.str;
				eventStr = eventStr.replaceFirst("%d", jz.name)
								   .replaceFirst("%d", buildName);
				AllianceMgr.inst.addAllianceEvent(member.lianMengId, eventStr);
			}
		}

	}

	public JianZhuLvBean insertJianZhuLvBean(int lianMengId, JianZhuLvBean bean) {
		if(bean == null){
			bean = new JianZhuLvBean();
			bean.lmId = lianMengId;
			bean.keZhanLv=bean.shuYuanLv=bean.shangPuLv=bean.zongMiaoLv=bean.tuTengLv=1;
			HibernateUtil.insert(bean);
			JianZhuLvBeanDao.inst.insertJianZhuBean(lianMengId, bean);
		}
		return bean;
	}
	/**
	 * 获取联盟科技的配置，用于押镖
	 * @param lmId
	 */
	public LianMengKeJi getKeJiConfForYaBiao(int lmId, int type){
		return getKeJiConfByType(lmId, type);
	}

	public LianMengKeJi getKeJiConfByType(int lmId, int type) {
		LMKJBean bean = LMKJBeanDao.inst.getBean(lmId);
		int curLevel = 0;
		if(bean != null){
			curLevel = getKeJiLv(bean, type);
		}
		if(curLevel == 404){
			curLevel = 0;
		}
		LianMengKeJi conf = getKeJiConf(type, curLevel);
		return conf;
	}
	public LianMengKeJi getKeJiConfForFangWu(int lmId){
		int type = 301;
		return getKeJiConfByType(lmId, type);
	}
	public synchronized void upLMKJ(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		if(member.title != AllianceMgr.TITLE_LEADER) {
			log.error("升级联盟科技失败，君主:{}没有权限，title:{}", jz.id, member.title);
			return;
		}
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		if(req == null){
			return;
		}
		LMKJBean bean = LMKJBeanDao.inst.getBean(member.lianMengId);
		int curLevel = 0;
		int type = req.getErrorCode();
		if(bean != null){
			curLevel = getKeJiLv(bean, type);
		}
		if(curLevel == 404){
			log.error("错误的科技类型{}", type);
			return;
		}
		LianMengKeJi conf = getKeJiConf(type, curLevel);
		if(conf == null){
			return;
		}
		
		int newLv = curLevel+1;
		LianMengKeJi nextCof = getKeJiConf(type, newLv);
		if(nextCof == null) {
			log.error("升级联盟升级失败， 可能已经研究到最高等级或找不到配置type:{} curLevel:{}",type, curLevel);
			return;
		}
		
//		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		AllianceBean lmBean = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
		if(lmBean == null){
			log.error("未找到对应的联盟{}",member.lianMengId);
			return;
		}
		if(lmBean.build<conf.lvUpValue){
			log.info("建设值不足，需要 {} 实有 {}",conf.lvUpValue,lmBean.build);
			return;
		}
		
		JianZhuLvBean jianZhuBean = JianZhuLvBeanDao.inst.getJianZhuBean(member.lianMengId);
		if(jianZhuBean == null) {
			jianZhuBean = insertJianZhuLvBean(member.lianMengId, jianZhuBean);
		}
		if(jianZhuBean.shuYuanLv < curLevel) {
			log.error("升级联盟科技失败，科技研究等级不能超过书院等级");
			return;
		}
		
		//
		if(bean == null){
			bean = new LMKJBean();
			fillDefaultLevel(member.lianMengId, bean);
		}
		lmBean.build -= conf.lvUpValue;
		HibernateUtil.update(lmBean);
		log.info("扣除联盟建设值 {} of lmId {}",conf.lvUpValue, lmBean.id);
		AllianceMgr.inst.sendAllianceInfo(jz, session, null, lmBean);
		
		
		setLevel(bean, type , newLv);
		//检查是否正确
		if(newLv != getKeJiLv(bean, type)){
			log.error("保存升级的数据失败， lmId {}，type {}， lv {}",
					member.lianMengId,type,newLv);
		}
		HibernateUtil.update(bean);
		log.info("{} 升级联盟科技 type {} to lv {}",jz.id,type,newLv);
		sendError(0, session, "升级成功", PD.S_LMKJ_UP); 
		JunZhuMgr.inst.sendMainInfo(session,jz);
		LianmengEvent lmEvent = AllianceMgr.inst.lianmengEventMap.get(22);
		if(lmEvent != null) {
			String eventStr = lmEvent.str == null ? "" : lmEvent.str;			
			eventStr = eventStr.replaceFirst("%d", jz.name)
					.replaceFirst("%d", conf.name);
			AllianceMgr.inst.addAllianceEvent(member.lianMengId, eventStr);
		}
	}
	
	public void jiHuoLMKJ(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		} 
		JiHuoLMKJReq.Builder request = (qxmobile.protobuf.JianZhu.JiHuoLMKJReq.Builder) builder;
		int type = request.getKeJiType();
		
		LMKJBean bean = LMKJBeanDao.inst.getBean(member.lianMengId);
		int kjLevel = 0;
		if(bean != null){
			kjLevel = getKeJiLv(bean, type);
		}
		
		LMKJJiHuo lmkjJiHuo = HibernateUtil.find(LMKJJiHuo.class, jz.id);
		if(lmkjJiHuo == null) {
			lmkjJiHuo = new LMKJJiHuo();
			lmkjJiHuo.junzhuId = jz.id;
			fillDefaultLMKJJiHuo(lmkjJiHuo);
			Cache.lMKJJiHuoCache.put(jz.id, lmkjJiHuo);
			HibernateUtil.insert(lmkjJiHuo);
		}
		
		if(kjLevel == 0) {
			log.error("联盟科技激活失败，该科技type:{}还未研究", type);
			return;
		}
		int jiHuoLevel = getKeJiJiHuoLv(lmkjJiHuo, type);
		if(jiHuoLevel == 404){
			log.error("联盟科技激活失败，错误的科技类型{}", type);
			return;
		}
		if(jiHuoLevel >= kjLevel) {
			log.error("联盟科技激活失败，科技类型{}激活等级:{}超过或者等于了科技等级:{}", type, jiHuoLevel, kjLevel);
			return;
		}
		int newLevel = jiHuoLevel + 1;
		setKJJHLevel(lmkjJiHuo, type, newLevel);
		HibernateUtil.save(lmkjJiHuo);
		log.info("联盟科技激活成功，君主:{}科技类型{}激活等级从{} 到 {}", jz.id, type, jiHuoLevel, newLevel);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		JiHuoLMKJResp.Builder response = JiHuoLMKJResp.newBuilder();
		response.setResult(0);
		response.setJiHuoLv(newLevel);
		session.write(response.build());
	}
	
	public void setLevel(LMKJBean bean, int type, int lv) {
		switch(type){
		case 101:bean.type_101=lv;break;
		case 102:bean.type_102=lv;break;
		case 103:bean.type_103=lv;break;
		case 104:bean.type_104=lv;break;
		case 105:bean.type_105=lv;break;
		case 106:bean.type_106=lv;break;
		case 107:bean.type_107=lv;break;
		case 108:bean.type_108=lv;break;
		case 109:bean.type_109=lv;break;
		case 110:bean.type_110=lv;break;
		case 111:bean.type_111=lv;break;
//		case 202:bean.type_202=lv;break;
		//case 203:bean.type_203=lv;break;
		case 301:bean.type_301=lv;break;
		case 204:bean.type_204=lv;break;
		case 205:bean.type_205=lv;break;
		}
	}
	
	public void setKJJHLevel(LMKJJiHuo kjJiHuo, int type, int lv) {
		switch(type){
		case 101:kjJiHuo.type_101=lv;break;
		case 102:kjJiHuo.type_102=lv;break;
		case 103:kjJiHuo.type_103=lv;break;
		case 104:kjJiHuo.type_104=lv;break;
		case 105:kjJiHuo.type_105=lv;break;
		case 106:kjJiHuo.type_106=lv;break;
		case 107:kjJiHuo.type_107=lv;break;
		case 108:kjJiHuo.type_108=lv;break;
		case 109:kjJiHuo.type_109=lv;break;
		case 110:kjJiHuo.type_110=lv;break;
		case 111:kjJiHuo.type_111=lv;break;
//		case 202:bean.type_202=lv;break;
		//case 203:bean.type_203=lv;break;
		case 301:kjJiHuo.type_301=lv;break;
		case 204:kjJiHuo.type_204=lv;break;
		case 205:kjJiHuo.type_205=lv;break;
		}
	}

	public void fillDefaultLevel(int lianMengId, LMKJBean bean) {
		bean.lmId = lianMengId;
		bean.type_101=0;
		bean.type_102=0;
		bean.type_103=0;
		bean.type_104=0;
		bean.type_105=0;
		bean.type_106=0;
		bean.type_107=0;
		bean.type_108=0;
		bean.type_109=0;
		bean.type_110=0;
		bean.type_111=0;
//		bean.type_202=0;
	//	bean.type_203=0;
		bean.type_301=0;
		bean.type_204=0;
		bean.type_205=0;
		if(lianMengId>0)
			LMKJBeanDao.inst.insertBean(lianMengId, bean);
	}

	public void fillDefaultLMKJJiHuo(LMKJJiHuo lmkjJiHuo) {
		lmkjJiHuo.type_101=0;
		lmkjJiHuo.type_102=0;
		lmkjJiHuo.type_103=0;
		lmkjJiHuo.type_104=0;
		lmkjJiHuo.type_105=0;
		lmkjJiHuo.type_106=0;
		lmkjJiHuo.type_107=0;
		lmkjJiHuo.type_108=0;
		lmkjJiHuo.type_109=0;
		lmkjJiHuo.type_110=0;
		lmkjJiHuo.type_111=0;
//		bean.type_202=0;
//		bean.type_203=0;
		lmkjJiHuo.type_301=0;
		lmkjJiHuo.type_204=0;
		lmkjJiHuo.type_205=0;
	}

	public LianMengKeJi getKeJiConf(int type, int curLevel) {
		List<LianMengKeJi> list = TempletService.listAll(LianMengKeJi.class.getSimpleName());
		LianMengKeJi ret = null;
		if(list == null){
			return ret;
		}
		for(LianMengKeJi k : list){
			if(k.type == type && k.level==curLevel){
				ret = k;
			}
		}
		return ret;
	}

	public int getKeJiLv(LMKJBean bean, int type){
		int ret = 404;//not found
		switch(type){
		case 101:ret=bean.type_101;break;
		case 102:ret=bean.type_102;break;
		case 103:ret=bean.type_103;break;
		case 104:ret=bean.type_104;break;
		case 105:ret=bean.type_105;break;
		case 106:ret=bean.type_106;break;
		case 107:ret=bean.type_107;break;
		case 108:ret=bean.type_108;break;
		case 109:ret=bean.type_109;break;
		case 110:ret=bean.type_110;break;
		case 111:ret=bean.type_111;break;
//		case 202:ret=bean.type_202;break;
		//case 203:ret=bean.type_203;break;
		case 301:ret=bean.type_301;break;
		case 204:ret=bean.type_204;break;
		case 205:ret=bean.type_205;break;
		}
		return ret;
	}
	
	public int getKeJiJiHuoLv(LMKJJiHuo kjJiHuo, int type){
		int ret = 404;//not found
		switch(type){
		case 101:ret=kjJiHuo.type_101;break;
		case 102:ret=kjJiHuo.type_102;break;
		case 103:ret=kjJiHuo.type_103;break;
		case 104:ret=kjJiHuo.type_104;break;
		case 105:ret=kjJiHuo.type_105;break;
		case 106:ret=kjJiHuo.type_106;break;
		case 107:ret=kjJiHuo.type_107;break;
		case 108:ret=kjJiHuo.type_108;break;
		case 109:ret=kjJiHuo.type_109;break;
		case 110:ret=kjJiHuo.type_110;break;
		case 111:ret=kjJiHuo.type_111;break;
//		case 202:ret=bean.type_202;break;
		//case 203:ret=bean.type_203;break;
		case 301:ret=kjJiHuo.type_301;break;
		case 204:ret=kjJiHuo.type_204;break;
		case 205:ret=kjJiHuo.type_205;break;
		}
		return ret;
	}

	public void sendLMKJInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		LMKJBean bean = LMKJBeanDao.inst.getBean(member.lianMengId);
		if(bean == null){
			bean = new LMKJBean();
			fillDefaultLevel(member.lianMengId, bean);
		}
		LMKJJiHuo lmkjJiHuo = HibernateUtil.find(LMKJJiHuo.class, jz.id);
		if(lmkjJiHuo == null){
			lmkjJiHuo = new LMKJJiHuo();
			fillDefaultLMKJJiHuo(lmkjJiHuo);
		}
		KeJiList.Builder ret = KeJiList.newBuilder();
		List<LianMengKeJi> list = TempletService.listAll(LianMengKeJi.class.getSimpleName());
		int preT = 0;
		for(LianMengKeJi k : list){
			if(preT != k.type){
				preT = k.type;
				qxmobile.protobuf.JianZhu.KeJiInfo.Builder b = qxmobile.protobuf.JianZhu.KeJiInfo.newBuilder();
				b.setLv(getKeJiLv(bean, k.type));
				b.setJiHuoLv(getKeJiJiHuoLv(lmkjJiHuo, k.type));
				ret.addList(b);
			}
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_LMKJ_INFO;
		msg.builder = ret;
		session.write(msg);
	}

	public void jiBai(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(id, session, "您不在联盟中。",PD.S_ERROR);
			return;
		}
		int costGongXian = CanShu.LIANMENG_JIBAI_PRICE;
		if(member.gongXian < costGongXian) {
			sendError(id, session, "联盟贡献不足。",PD.S_ERROR);
			return;
		}
		chouJiang_1(id, session, builder);
		member.gongXian -= CanShu.LIANMENG_JIBAI_PRICE;
		HibernateUtil.save(member);
		AllianceMgr.inst.processHaveAlliance(jz, PD.ALLIANCE_INFO_REQ, session, member);
	}
	
	public void chouJiang_1(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, junZhuId);
		if(bean == null){
			//must be created when get info
			return;
		}
		JSONArray arr = new JSONArray(bean.str);
		int sumW = 0;
		for(int i=0; i<10; i++){
			sumW += arr.getJSONObject(i).optInt("w",0);
		}
		if(sumW == 0){
			sendError(10, session, "今日奖励已抽完", PD.S_ERROR);
			return;
		}
		int lucky = RandomUtil.getRandomNum(sumW);
		sumW = 0;
		int hit = -1;
		for(int i=0; i<10; i++){
			sumW += arr.getJSONObject(i).optInt("w",0);
			if(sumW>lucky){
				sumW = i;
				hit = i;
				break;
			}
		}
		if(hit<0 || hit>=10){
			log.error("计算随机抽奖错误 {}",hit);
			return;
		}
		String idx = String.valueOf(hit);
		sendError(0, session, idx, PD.S_LM_CHOU_JIANG);
		JSONObject hitO = arr.getJSONObject(hit);
		hitO.put("w", 0);
		bean.str = arr.toString();
		bean.todayUsedTimes+=1;
		bean.todayLeftTimes -= 1;
		bean.historyAll += 1;
		AwardTemp a = new AwardTemp();
		a.itemType = hitO.optInt("t");
		a.itemId = hitO.optInt("id");
		a.itemNum = hitO.optInt("n");
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
		if(jz == null)return;
		AwardMgr.inst.giveReward(session, a, jz,false,false);
		HibernateUtil.update(bean);
		log.info("{}抽中{},内容{}",junZhuId,hit,hitO.toString());
		if(session.containsAttribute("don'tSync")==false){
			JunZhuMgr.inst.sendMainInfo(session,jz,false);
			//BagMgr.inst.sendBagInfo(0, session, null);
		}
		EventMgr.addEvent(jz.id, ED.jibai , new Object[] {junZhuId, hitO.optInt("id"), hitO.optInt("n",1)});
	}

	public void chouJiang_N(int id, IoSession session, Builder builder) {
//		String idx = "1#2#3";
//		sendError(0, session, idx, PD.S_LM_CHOU_JIANG);	
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(id, session, "您不在联盟中。",PD.S_ERROR);
			return;
		}
		int maxJiBaiTimes = getMaxJiBaiTimes(member);
		ChouJiangBean bean = getChouJiangBean(jz, maxJiBaiTimes);
		int leftTimes = Math.max(0,maxJiBaiTimes - bean.todayUsedTimes);
		if(leftTimes<=0){
			sendError(id, session, "没有剩余次数。",PD.S_ERROR);
			return;
		}
		int costGongXian = CanShu.LIANMENG_JIBAI_PRICE * leftTimes;
		if(member.gongXian < costGongXian) {
			sendError(id, session, "联盟贡献不足。",PD.S_ERROR);
			return;
		}
		
		final StringBuffer sb = new StringBuffer();
		DummySession fakeS = new DummySession(){
			@Override
			public WriteFuture write(final Object message) {
				if(message instanceof ProtobufMsg){
					ProtobufMsg msg = (ProtobufMsg) message;
					switch(msg.id){
					case PD.S_LM_CHOU_JIANG:
						ErrorMessage.Builder e = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) msg.builder;
						String idx = e.getErrorDesc();
						sb.append(idx);
						sb.append("#");
						break;
					default:
						break;
					}
				}
				return null;
			}
		};
		fakeS.setAttribute(SessionAttKey.junZhuId, jz.id);
		log.info("{}准备连续祭拜{}次", jz.id, leftTimes);
		fakeS.setAttribute("don'tSync");
		for(int i=0; i<leftTimes; i++){
			chouJiang_1(0, fakeS, null);
			Object fail = fakeS.getAttribute("FAIL");
			if(fail != null){
				log.info("FAIL {}", fail);
				break;
			}
		}
		fakeS.removeAttribute("don'tSync");
		//remove last #
		if(sb.length()==0){
			log.error("nothing gain");
			return;
		}
		sb.setLength(sb.length()-1);
		String gain = sb.toString();
		sendError(0, session, gain, PD.S_LM_CHOU_JIANG);
		member.gongXian -= costGongXian;
		HibernateUtil.save(member);
		AllianceMgr.inst.processHaveAlliance(jz, PD.ALLIANCE_INFO_REQ, session, member);
		log.info("{}连续祭拜结束，获得{}", jz.id, gain);
		JunZhuMgr.inst.sendMainInfo(session,jz,false);
		//BagMgr.inst.sendBagInfo(0, session, null);
	}

	public void sendChouJiangInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。",PD.S_ERROR);
			return;
		}
		int maxJiBaiTimes = getMaxJiBaiTimes(member);
		ChouJiangBean bean = getChouJiangBean(jz, maxJiBaiTimes);
		
		JSONArray arr = new JSONArray(bean.str);
		int cnt = arr.length();
		if(cnt != 10){
			log.error("奖励个数不是10个，jzId{}, cnt{}",jz.id,cnt);
			sendError(id, session, "暂无奖励，请稍后再来。",PD.S_ERROR);
			return;
		}
		ExploreResp.Builder ret = ExploreResp.newBuilder();
		ret.setSuccess(0);//
		TypeInfo.Builder tb = TypeInfo.newBuilder();
		tb.setMoney(CanShu.LIANMENG_JIBAI_PRICE);
		tb.setCd(maxJiBaiTimes);
		int leftTimes = Math.max(0,maxJiBaiTimes - bean.todayUsedTimes);
		tb.setRemainFreeCount(leftTimes);
		ret.setInfo(tb);
		for(int i=0; i<cnt; i++){
			Award.Builder a = Award.newBuilder();
			JSONObject o = arr.getJSONObject(i);
			a.setItemType(o.optInt("t", 0));
			a.setItemId(o.optInt("id", 0));
			a.setItemNumber(o.optInt("n", 0));
			a.setMiBaoStar(o.optInt("w",0)==0?1:0);//w==0，已抽到
			ret.addAwardsList(a);
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_LM_CHOU_JIANG_INFO;
		msg.builder = ret;
		session.write(msg);
	}

	public int getMaxJiBaiTimes(AlliancePlayer member) {
		int zongMiaoLv = 1;
		JianZhuLvBean jianZhuBean = JianZhuLvBeanDao.inst.getJianZhuBean(member.lianMengId);
		if(jianZhuBean != null && jianZhuBean.zongMiaoLv>0){
			zongMiaoLv = jianZhuBean.zongMiaoLv;
		}
		int maxJiBaiTimes = 0;
		LianMengZongMiao conf = getLianMengZongMiaoCfgByLevel(zongMiaoLv);
		if(conf != null) {
			maxJiBaiTimes = conf.jiBaiMaxTimes;
		}
		return maxJiBaiTimes;
	}
	
	public LianMengZongMiao getLianMengZongMiaoCfgByLevel(int zongMiaoLevel) {
		List<LianMengZongMiao> confList = TempletService.listAll(LianMengZongMiao.class.getSimpleName());
		LianMengZongMiao lianMengZongMiao = null;
		for(LianMengZongMiao lmzm : confList) {
			if(lmzm.zongMiaoLevel == zongMiaoLevel) {
				lianMengZongMiao = lmzm;
				break;
			}
		}
		if(lianMengZongMiao == null) {
			log.error("找不到等级为:{}的联盟宗庙配置", zongMiaoLevel);
		}
		return lianMengZongMiao;
	}

	public ChouJiangBean getChouJiangBean(JunZhu jz, int maxJiBaiTimes) {
		ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, jz.id);
		if(bean == null){
			bean = new ChouJiangBean();
			bean.jzId = jz.id;
			JSONArray json = fillChouJiangBean();
			bean.createTime = new Date();
			bean.todayLeftTimes = maxJiBaiTimes;
			bean.str = json.toString();
			HibernateUtil.insert(bean);
		}else if(!DateUtils.isSameSideOfFour(bean.createTime, new Date())){
			JSONArray json = fillChouJiangBean();
			bean.createTime = new Date();
			bean.str = json.toString();
			bean.todayUsedTimes = 0;
			bean.todayLeftTimes = maxJiBaiTimes;
			HibernateUtil.update(bean);
		}
		return bean;
	}
	
	int[] ids = {201,202,203};
	int[] times = {1,9,   0};
	int[] ws = {10,50,   0};
	public JSONArray fillChouJiangBean(){
		List<JSONObject> list = new ArrayList<JSONObject>(10);
		for(int i=0; i<3; i++){
			for(int t=0;t<times[i];t++){
				AwardTemp a = AwardMgr.inst.calcAwardTemp(ids[i]);
				if(a != null) {
					JSONObject o = new JSONObject();
					o.put("t", a.itemType);
					o.put("id", a.itemId);
					o.put("n", a.itemNum);
					o.put("w", ws[i]);
					list.add(o);
				}
			}
		}
		Collections.shuffle(list);
		JSONArray arr = new JSONArray(list);
		return arr;
	}

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
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(
					FunctionID.LianMeng, jz.id, jz.level);
			if(!isOpen){
				break;
			}
			AlliancePlayer member = AllianceMgr.inst.getAlliancePlayer(jz.id);
			if (member == null || member.lianMengId <= 0) {
				break;
			}
//			AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
			AllianceBean lmBean = AllianceBeanDao.inst.getAllianceBean(member.lianMengId);
			if(lmBean == null){
				break;
			}
			ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, jz.id);
			boolean chou = false;
			if(bean == null){
				chou =  true;
			}else if(bean.todayLeftTimes > 0){
				chou = true;
			}else if(!DateUtils.isSameDay(bean.createTime)){
				chou = true;
			}
			if(chou){
				// 祭拜
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.LianMengJiBai);
				// 一键祭拜
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.YiJianJiBai);
			}

			LMKJBean bean2 = LMKJBeanDao.inst.getBean(member.lianMengId);
			if(bean2 == null){
				bean2 = new LMKJBean();
				fillDefaultLevel(member.lianMengId, bean2);
			}
			/*
			 * 成员激活
			 */
			if(member.title == AllianceMgr.TITLE_MEMBER){
				LMKJJiHuo lmkjJiHuo = HibernateUtil.find(LMKJJiHuo.class, jz.id);
				if(lmkjJiHuo == null) {
					lmkjJiHuo = new LMKJJiHuo();
					lmkjJiHuo.junzhuId = jz.id;
					fillDefaultLMKJJiHuo(lmkjJiHuo);
				}
				for(int type= 101; type <=301; type++ ){
					int lvkeji = getKeJiLv(bean2, type);
					if (lvkeji == 404 || lvkeji ==0){
						continue;
					}
					int lvjihuo = getKeJiJiHuoLv(lmkjJiHuo, type);
					if (lvjihuo == 404){
						continue;
					}
					if(lvjihuo < lvkeji){
						FunctionID.pushCanShowRed(jz.id, session, FunctionID.LianMengShuYuanKiJi);
						break;
					}
				}
				// 盟主副盟主 研究
			}else if(member.title == AllianceMgr.TITLE_LEADER || member.title == AllianceMgr.TITLE_DEPUTY_LEADER){
				JianZhuLvBean jianZhuBean = JianZhuLvBeanDao.inst.getJianZhuBean(member.lianMengId);
				if(jianZhuBean == null) {
					return;
				}
				for(int type : kejiTypes){
					int lv = getKeJiLv(bean2, type);
					if(lv != 404){
						LianMengKeJi conf = getKeJiConf(type, lv);
						if(conf != null && lmBean.build >= conf.lvUpValue && jianZhuBean.shuYuanLv > conf.shuYuanlvNeeded){
							FunctionID.pushCanShowRed(jz.id, session, FunctionID.LianMengShuYuanKiJi);
							break;
						}
					}
				}
			}
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
}
