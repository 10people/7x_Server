package com.yy;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.KaiFuHuoDong;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.Explore.TypeInfo;

public class YYMgr extends EventProc{
	public Logger log = LoggerFactory.getLogger(YYMgr.class.getSimpleName());
	public static YYMgr inst;
	public static String keFuInfo = "QQ信息找客服";
	public static String keFuQQ = "1234567";
	public static String faq = "常见问题";
	public static YYMgr getInst(){
		 synchronized(YYMgr.class){
			if(inst == null){
				inst = new YYMgr();
				inst.load();
				inst.doReg();
			}
		 }
		return inst;
	}
	public void load(){
		String v = Redis.getInstance().get("YYMgr.keFuInfo");
		if(v != null)keFuInfo = v;
				
		v = Redis.getInstance().get("YYMgr.keFuQQ");
		if(v != null)keFuQQ=v;
		
		v = Redis.getInstance().get("YYMgr.faq");
		if(v != null)faq = v;
	}
	public void getQQ(int id, IoSession session, Builder builder){
		ErrorMessage.Builder em = ErrorMessage.newBuilder();
		em.setCmd(0);
		em.setErrorCode(0);
		em.setErrorDesc(keFuInfo+"#=#=#"+keFuQQ);
		session.write(new ProtobufMsg((short)id,em));
	}
	public void getLv(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		YYLvRewardInfo bean = HibernateUtil.find(YYLvRewardInfo.class, jz.id);
		String info = "";
		if(bean != null){
			info = bean.lvs;
		}
		ExploreResp.Builder ret = ExploreResp.newBuilder();
		List<KaiFuHuoDong> list = TempletService.listAll(KaiFuHuoDong.class.getSimpleName());
		if(list == null){
			list = Collections.EMPTY_LIST;
		}
		ret.setSuccess(0);
		for(KaiFuHuoDong conf : list){
			Award.Builder t = Award.newBuilder();
			String str = conf.award;
			String arr[]=str.split(":");
			t.setItemType(Integer.parseInt(arr[0]));
			t.setItemId(Integer.parseInt(arr[1]));
			t.setItemNumber(Integer.parseInt(arr[2]));
			t.setMiBaoStar(conf.lv);//领取等级
			int st = 1;//1：等级不够；
			if(jz.level>=conf.lv){
				st = 2;
				if(info.contains("#"+conf.lv+"#")){
					st = 3;
				}
			}
			t.setPieceNumber(st);//领取状态，1：等级不够；2：可领取；3：已领取
			ret.addAwardsList(t);
		}
		session.write(new ProtobufMsg((short)id,ret));
	}
	public void getFAQ(int id, IoSession session, Builder builder){
		ErrorMessage.Builder em = ErrorMessage.newBuilder();
		em.setCmd(0);
		em.setErrorCode(0);
		em.setErrorDesc(faq);
		session.write(new ProtobufMsg((short)id,em));
	}
	public void lingQu(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			return;
		}
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		int lv = req.getErrorCode();
		YYLvRewardInfo bean = HibernateUtil.find(YYLvRewardInfo.class, jz.id);
		if(bean == null){
			bean = new YYLvRewardInfo();
			bean.jzId = jz.id;
			bean.lvs = "#";
			HibernateUtil.insert(bean);
		}
		List<KaiFuHuoDong> list = TempletService.listAll(KaiFuHuoDong.class.getSimpleName());
		if(list == null){
			list = Collections.EMPTY_LIST;
		}
		KaiFuHuoDong conf = null;
		for(KaiFuHuoDong t : list){
			if(t.lv == lv){
				conf = t;
				break;
			}
		}
		ExploreResp.Builder ret = ExploreResp.newBuilder();
		ret.setSuccess(0);//
		if(conf == null){
			ret.setSuccess(3);
			//req.setErrorDesc("缺少配置");
			log.error("缺少等级配置 {} for jzId {}",lv,jz.id);
		}else if(bean.lvs.contains("#"+lv+"#")){
			//已经领取过了
			ret.setSuccess(3);
			//req.setErrorDesc("已领取过了");
		}else if(jz.level<0){
			//ret.setSuccess(1);
			//req.setErrorDesc("等级不够");
		}else{
			//req.setErrorDesc("领取成功");
			bean.lvs+= lv+"#";
			HibernateUtil.update(bean);
			AwardMgr.inst.giveReward(session, conf.award, jz);
			log.info("{} get lv {}, reward {}", jz.id, lv, conf.award);
			//
			List<AwardTemp> alist = AwardMgr.inst.parseAwardConf(conf.award, "#", ":");
			alist.forEach(t->BagMgr.inst.award2msg(ret, t));
		}
		session.write(new ProtobufMsg(PD.S_USE_ITEM, ret));
	}
	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.junzhu_level_up:{
			//new Object[] { jz.id,			jz.level, jz }
			Object[] arr = (Object[]) param.param;
			checkRedNotice((Long)arr[0],(Integer)arr[1]);
			break;
		}
		case ED.JUNZHU_LOGIN:
			JunZhu jz = (JunZhu) param.param;
			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				return;
			}
			checkRedNotice(jz.id, jz.level);
			break;
		}
	}
	public void checkRedNotice(Long jzId, int lv) {
		List<KaiFuHuoDong> list = TempletService.listAll(KaiFuHuoDong.class.getSimpleName());
		if(list == null){
			list = Collections.EMPTY_LIST;
		}
		YYLvRewardInfo bean = HibernateUtil.find(YYLvRewardInfo.class, jzId);
		String gotLvs = "";
		if(bean != null){
			gotLvs = bean.lvs;
		}
		boolean hit = false;
		for(KaiFuHuoDong t : list){
			if(t.lv > lv){
				break;
			}
			if(gotLvs.contains("#"+t.lv+"#")){
				continue;
			}
			hit = true;
			break;
		}
		if(!hit){
			return;
		}
		IoSession session = AccountManager.sessionMap.get(jzId);
		if(session == null){
			return;
		}
		FunctionID.pushCanShowRed(jzId, session, FunctionID.level_reward);
	}
	@Override
	public void doReg() {
		EventMgr.regist(ED.junzhu_level_up, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
	}
	
}
