package com.qx.alliance.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import groovy.servlet.TemplateServlet;

import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.TypeInfo;
import qxmobile.protobuf.Explore.TypeInfoOrBuilder;
import qxmobile.protobuf.JianZhu.JianZhuInfo;
import qxmobile.protobuf.JianZhu.JianZhuList;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.LianMengKeZhan;
import com.manu.dynasty.template.LianMengShangPu;
import com.manu.dynasty.template.LianMengShuYuan;
import com.manu.dynasty.template.LianMengTuTeng;
import com.manu.dynasty.template.LianMengZongMiao;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.MoBaiBean;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.util.RandomUtil;

/**
 * 联盟建筑管理器
 * @author 康建虎
 *
 */
public class JianZhuMgr {
	public static JianZhuMgr inst;
	public static Logger log = LoggerFactory.getLogger(JianZhuMgr.class.getSimpleName());

	public void getInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		JianZhuLvBean bean = HibernateUtil.find(JianZhuLvBean.class, member.lianMengId);
		if(bean == null){
			bean = new JianZhuLvBean();
			bean.keZhanLv=bean.shuYuanLv=bean.shangPuLv=bean.zongMiaoLv=bean.tuTengLv=1;
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
	public void up(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		JianZhuLvBean bean = HibernateUtil.find(JianZhuLvBean.class, member.lianMengId);
		if(bean == null){
			bean = new JianZhuLvBean();
			bean.lmId = member.lianMengId;
			bean.keZhanLv=bean.shuYuanLv=bean.shangPuLv=bean.zongMiaoLv=bean.tuTengLv=1;
			HibernateUtil.insert(bean);
		}
		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if(lmBean == null){
			sendError(3,session,"没有联盟数据");
			return;
		}
		//1客栈；2书院；3图腾；4商铺；5宗庙
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		int code = req.getErrorCode();
		switch(code){
		case 1:{
			//upKeZhan();
			List<LianMengKeZhan> list = TempletService.listAll(LianMengKeZhan.class.getSimpleName());
			if(list == null){
				sendError(10, session, "配置缺失。");
				return;
			}
			if(bean.keZhanLv>=list.size()){
				sendError(20, session, "已满级");
				return;
			}
			if(bean.keZhanLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengKeZhan conf = list.get(bean.keZhanLv);
			//FIXME 消耗数值
			bean.keZhanLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "升级成功");
			break;
		}
		case 2:{
			List<LianMengShuYuan> list = TempletService.listAll(LianMengShuYuan.class.getSimpleName());
			if(list == null){
				sendError(10, session, "配置缺失。");
				return;
			}
			if(bean.shuYuanLv>=list.size()){
				sendError(20, session, "已满级");
				return;
			}
			if(bean.shuYuanLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengShuYuan conf = list.get(bean.shuYuanLv);
			//FIXME 消耗数值
			bean.shuYuanLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "升级成功");
			break;
		}
		case 4:{ // 商铺
			List<LianMengShangPu> list = TempletService.listAll(LianMengShangPu.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengShangPu配置缺失。");
				return;
			}
			if(bean.shangPuLv>=list.size()){
				sendError(20, session, "bean.shangPuLv已满级");
				return;
			}
			if(bean.shangPuLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengShangPu conf = list.get(bean.shangPuLv);
			//FIXME 消耗数值
			bean.shangPuLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟商铺升级成功");
			break;
			
		}
		case 3:{ 
			List<LianMengTuTeng> list = TempletService.listAll(LianMengTuTeng.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengTuTeng配置缺失。");
				return;
			}
			if(bean.tuTengLv>=list.size()){
				sendError(20, session, "bean.tuTengLv已满级");
				return;
			}
			if(bean.tuTengLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengTuTeng conf = list.get(bean.tuTengLv);
			//FIXME 消耗数值
			bean.tuTengLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟图腾升级成功");
			break;
			
		}
		case 5:{
			List<LianMengZongMiao> list = TempletService.listAll(LianMengZongMiao.class.getSimpleName());
			if(list == null){
				sendError(10, session, "LianMengZongMiao配置缺失。");
				return;
			}
			if(bean.zongMiaoLv>=list.size()){
				sendError(20, session, "bean.zongMiaoLv已满级");
				return;
			}
			if(bean.zongMiaoLv>=lmBean.level){
				sendError(30, session, "联盟等级不够");
			}
			LianMengZongMiao conf = list.get(bean.zongMiaoLv);
			//FIXME 消耗数值
			bean.zongMiaoLv+=1;
			HibernateUtil.update(bean);
			sendError(0, session, "联盟宗庙升级成功");
			break;
			
		}
		}
	}
	/**
	 * 获取联盟科技的配置，用于押镖
	 * @param lmId
	 */
	public LianMengKeJi getKeJiConfForYaBiao(int lmId, int type){
		return getKeJiConfByType(lmId, type);
	}

	public LianMengKeJi getKeJiConfByType(int lmId, int type) {
		LMKJBean bean = HibernateUtil.find(LMKJBean.class, lmId);
		int curLevel = 1;
		if(bean != null){
			curLevel = getKeJiLv(bean, type);
		}
		if(curLevel == 404){
			curLevel = 1;
		}
		LianMengKeJi conf = getKeJiConf(type, curLevel);
		return conf;
	}
	public LianMengKeJi getKeJiConfForFangWu(int lmId){
		int type = 301;
		return getKeJiConfByType(lmId, type);
	}
	public void upLMKJ(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		ErrorMessage.Builder req = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
		if(req == null){
			return;
		}
		LMKJBean bean = HibernateUtil.find(LMKJBean.class, member.lianMengId);
		int curLevel = 1;
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
			log.error("没有找到这个科技 type {} level {}",type, curLevel);
			return;
		}
		//FIXME cost
		//
		if(bean == null){
			bean = new LMKJBean();
			bean.lmId = member.lianMengId;
			fillDefaultLevel(bean);
			HibernateUtil.insert(bean);
		}
		int newLv = curLevel+1;
		setLevel(bean, type , newLv);
		//检查是否正确
		if(newLv != getKeJiLv(bean, type)){
			log.error("保存升级的数据失败， lmId {}，type {}， lv {}",
					member.lianMengId,type,newLv);
		}
		HibernateUtil.update(bean);
		log.info("{} 升级联盟科技 type {} to lv {}",jz.id,type,newLv);
		sendError(0, session, "升级成功", PD.S_LMKJ_UP); 
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
		case 202:bean.type_202=lv;break;
		//case 203:bean.type_203=lv;break;
		case 301:bean.type_301=lv;break;
		case 204:bean.type_204=lv;break;
		}
	}

	public void fillDefaultLevel(LMKJBean bean) {
		bean.type_101=1;
		bean.type_102=1;
		bean.type_103=1;
		bean.type_104=1;
		bean.type_105=1;
		bean.type_106=1;
		bean.type_107=1;
		bean.type_108=1;
		bean.type_109=1;
		bean.type_110=1;
		bean.type_111=1;
		bean.type_202=1;
	//	bean.type_203=1;
		bean.type_301=1;
		bean.type_204=1;
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
		case 202:ret=bean.type_202;break;
		//case 203:ret=bean.type_203;break;
		case 301:ret=bean.type_301;break;
		case 204:ret=bean.type_204;break;
		}
		return ret;
	}

	public void sendLMKJInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。");
			return;
		}
		LMKJBean bean = HibernateUtil.find(LMKJBean.class, member.lianMengId);
		if(bean == null){
			bean = new LMKJBean();
			fillDefaultLevel(bean);
		}
		JianZhuList.Builder ret = JianZhuList.newBuilder();
		List<LianMengKeJi> list = TempletService.listAll(LianMengKeJi.class.getSimpleName());
		int preT = 0;
		for(LianMengKeJi k : list){
			if(preT != k.type){
				preT = k.type;
				JianZhuInfo.Builder b = JianZhuInfo.newBuilder();
				b.setLv(getKeJiLv(bean, k.type));
				ret.addList(b);
			}
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_LMKJ_INFO;
		msg.builder = ret;
		session.write(msg);
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
		HibernateUtil.update(bean);
		log.info("{}抽中{},内容{}",junZhuId,hit,hitO.toString());
	}

	public void chouJiang_N(int id, IoSession session, Builder builder) {
		String idx = "1#2#3";
		sendError(0, session, idx, PD.S_LM_CHOU_JIANG);		
	}
	public int LIANMENG_JIBAI_PRICE = 20;
	public void sendChouJiangInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (member == null) {
			sendError(id, session, "您不在联盟中。",PD.S_ERROR);
			return;
		}
		int zongMiaoLv = 1;
		JianZhuLvBean jianZhuBean = HibernateUtil.find(JianZhuLvBean.class, member.lianMengId);
		if(jianZhuBean != null && jianZhuBean.zongMiaoLv>0){
			zongMiaoLv = jianZhuBean.zongMiaoLv;
		}
		List<LianMengZongMiao> confList = TempletService.listAll(LianMengZongMiao.class.getSimpleName());
		LianMengZongMiao conf = confList.get(zongMiaoLv-1);
		int maxJiBaiTimes = conf.jiBaiMaxTimes;
		ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, jz.id);
		if(bean == null){
			bean = new ChouJiangBean();
			bean.jzId = jz.id;
			JSONArray json = fillChouJiangBean();
			bean.createTime = new Date();
			bean.str = json.toString();
			HibernateUtil.insert(bean);
		}else if(!DateUtils.isSameDay(bean.createTime)){
			JSONArray json = fillChouJiangBean();
			bean.createTime = new Date();
			bean.str = json.toString();
			bean.todayUsedTimes = 0;
			HibernateUtil.update(bean);
		}
		
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
		tb.setMoney(LIANMENG_JIBAI_PRICE);
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
	
	int[] ids = {201,202,203};
	int[] times = {6,3,   1};
	int[] ws = {50,30,   10};
	public JSONArray fillChouJiangBean(){
		List<JSONObject> list = new ArrayList<JSONObject>(10);
		for(int i=0; i<3; i++){
			for(int t=0;t<times[i];t++){
				AwardTemp a = AwardMgr.inst.calcAwardTemp(201);
				JSONObject o = new JSONObject();
				o.put("t", a.getItemType());
				o.put("id", a.getItemId());
				o.put("n", a.getItemNum());
				o.put("w", ws[i]);
				list.add(o);
			}
		}
		Collections.shuffle(list);
		JSONArray arr = new JSONArray(list);
		return arr;
	}
}
