package com.qx.award;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.DailyAwardProto.DailyAward;
import qxmobile.protobuf.DailyAwardProto.DailyAwardArr;
import qxmobile.protobuf.DailyAwardProto.DailyAwardInfo;
import qxmobile.protobuf.DailyAwardProto.GetDailyAward;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.HeroProtoType;
import com.manu.dynasty.template.Jiangli;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.hero.HeroMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;

/**
 * 每日奖励
 * @author 康建虎
 *
 */
public class DailyAwardMgr {
	public static Logger log = LoggerFactory.getLogger(DailyAwardMgr.class);
	public static DailyAwardMgr inst;
	public DailyAwardMgr(){
		setInst();
	}
	protected void setInst() {
		inst = this;
	}
	public void sendInfo(int id, IoSession session, Builder builder) {
		List<Jiangli> list = TempletService.listAll(Jiangli.class.getSimpleName());
		if(list == null || list.size() == 0	){
			log.error("配置错误");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("没有君主");
			return;
		}
		DailyAwardBean bean = HibernateUtil.find(DailyAwardBean.class, jz.id);
		if(bean == null){
			bean = new DailyAwardBean();
			bean.junZhuId = jz.id;
			// TODO 是不是要添加具体属性数值设置？
			// 添加到缓存中 
			MC.add(bean, jz.id);
			HibernateUtil.insert(bean);
		}
		Date today = new Date();
		//每日奖励
		DailyAwardInfo.Builder ret = DailyAwardInfo.newBuilder();
		DailyAwardArr.Builder everyDayArr = DailyAwardArr.newBuilder();
		fillContent(everyDayArr, list.get(0));
		boolean sameDate = isSameDate(bean.preDaily, today);
		everyDayArr.setYiLing(sameDate? 1 : 0);
		ret.setDailyAward(everyDayArr);
		//累积登录奖励
		int cnt = list.size();
		for(int i=1; i<cnt; i++){
			DailyAwardArr.Builder loginArr = DailyAwardArr.newBuilder();
			Jiangli conf = list.get(i);
			if(conf.getAwardtype() != 1){
				continue;
			}
			fillContent(loginArr, conf);
			loginArr.setYiLing(sameDate ? 1 : (bean.leiJiLogin<i ? 0 : 1) );
			ret.addLoginAward(loginArr);
		}
		//
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_daily_award_info;
		msg.builder = ret;
		session.write(msg);
	}
	protected void fillContent(
			qxmobile.protobuf.DailyAwardProto.DailyAwardArr.Builder everyDayArr,
			Jiangli jiangli) {
		//TODO 注意后期将这个解析缓存起来。
		String txt = jiangli.getItem();
		String[] parts = txt.split("#");
		everyDayArr.setName(HeroService.getNameById(jiangli.getName()));
		for(String v : parts){
			String[] nums = v.split(":");
			if(nums.length !=3 ){
				log.error("配置错误:"+v);
				continue;
			}
			DailyAward.Builder da = DailyAward.newBuilder();
			int t = Integer.parseInt(nums[0]);
			int id = Integer.parseInt(nums[1]);
			int cnt = Integer.parseInt(nums[2]);
			switch(t){
			case 0:
			case 2:
				BaseItem it = TempletService.itemMap.get(id);
				if(it == null){
					log.error("没有找到物品{}",v);
					continue;
				}
				da.setAwardName(HeroService.getNameById(it.getName()));
				da.setAwardIconId(it.getIconId());
				break;
			case 7:
				HeroProtoType proto = HeroMgr.tempId2HeroProto.get(id);
				if(proto == null){
					log.error("没有找到武将 {}", v);
					continue;
				}
				da.setAwardName(HeroService.getNameById(proto.getHeroName()+""));
				da.setAwardIconId(proto.getIcon());
				break;
			default:
				log.error("未知类型:{}",v);
				continue;
			}
			da.setAwardType(t);
			da.setAwardId(jiangli.id);
			da.setCnt(cnt);
			everyDayArr.addItems(da);
		}
	}
	public void sendAward(int id, IoSession session, Builder builder) {
		GetDailyAward.Builder req = (qxmobile.protobuf.DailyAwardProto.GetDailyAward.Builder) builder;		
		int reqId = req.getAwardId();
		List<Jiangli> list = TempletService.listAll(Jiangli.class.getSimpleName());
		if(list == null || list.size() == 0	){
			log.error("配置错误");
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("没有君主");
			return;
		}
		DailyAwardBean bean = HibernateUtil.find(DailyAwardBean.class, jz.id);
		if(bean == null){
			bean = new DailyAwardBean();
			bean.junZhuId = jz.id;
			// 添加到缓存中
			MC.add(bean, bean.junZhuId);
			HibernateUtil.insert(bean);
		}
		Date today = Calendar.getInstance().getTime();
		switch(reqId){
		case 1000:{
			Date preDate = bean.preDaily;
			if(isSameDate(preDate, today)){
				sendError(session, "已领过当天奖励");
				return;
			}
			log.info("领取每日奖励{} {}",jz.id,jz.name);
			bean.preDaily = today;
			HibernateUtil.save(bean);
			log.info("更新数据库成功");
			giveAward(session,list.get(0), jz);
			log.info("领取完毕 {}",jz.id);
		}
			break;
		case 2000:{
			Date preDate = bean.preLogin;
			if(isSameDate(preDate, today)){
				sendError(session, "今日已领过~，请明日再来！");
				return;
			}
			log.info("领取累登奖励{}",jz.id);
			bean.preLogin = today;
			bean.leiJiLogin += 1;
			if(list.size()<bean.leiJiLogin || bean.leiJiLogin>7){
				sendError(session, "您已领取过了全部奖励");
				log.warn("累登次数超限",bean.leiJiLogin,list.size());
				return;
			}
			HibernateUtil.save(bean);
			log.info("保存DB ok");
			giveAward(session,list.get(bean.leiJiLogin), jz);
			log.info("{}领取累登{}奖励完毕",jz.id, bean.leiJiLogin);
		}
			break;
		default:
			log.error("错误的奖励类型{}",reqId);
			return;
		}
		sendInfo(0, session, null);
	}
	public boolean isSameDate(Date a, Date b){
		if(a == null || b == null){
			return false;
		}
		return a.getYear() == b.getYear() &&
				a.getMonth() == b.getMonth()
				&& a.getDate() == b.getDate();
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
		log.debug("sent keji info");
	}
	public List<AwardTemp> giveAward(IoSession session, Jiangli jiangli, JunZhu jz) {
		ArrayList<AwardTemp> ret = new ArrayList<AwardTemp>();
		if(jiangli == null){
			log.error("奖励是null");
			return ret;
		}
		String txt = jiangli.getItem();
		String[] parts = txt.split("#");
		for(String v : parts){
			String[] nums = v.split(":");
			if(nums.length !=3 ){
				log.error("配置错误:"+v);
				continue;
			}
			int t = Integer.parseInt(nums[0]);
			int id = Integer.parseInt(nums[1]);
			int cnt = Integer.parseInt(nums[2]);
			AwardTemp a = new AwardTemp();
			a.setItemType(t);
			a.setItemId(id);
			a.setItemNum(cnt);
			AwardMgr.inst.giveReward(session, a, jz);
			log.info("给予{}奖励 type {} id {} cnt{}",
					jz.id,t,id,cnt);
			ret.add(a);
		}
		return ret;
	}
}
