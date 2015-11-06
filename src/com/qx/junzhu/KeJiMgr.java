package com.qx.junzhu;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.KeJiProto.KeJiConf;
import qxmobile.protobuf.KeJiProto.KeJiInfoRet;
import qxmobile.protobuf.KeJiProto.KeJiShengJiReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.JunzhuKeji;
import com.manu.network.SessionAttKey;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.persistent.HibernateUtil;

/**
 * @author 康建虎
 *
 */
public class KeJiMgr {
	public static int spaceFactor = 100;
	public static int keJiCnt = 10;
	public static Logger log = LoggerFactory.getLogger(KeJiMgr.class);
	
	public void sendKeJiInfo(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhuId id");
			return ;
		}
//		KeJiInfoReq.Builder b = (qxmobile.protobuf.KeJiProto.KeJiInfoReq.Builder) builder;
		long start = junZhuId * spaceFactor;
		long end = start + keJiCnt;
		List<JzKeji> list = HibernateUtil.list(JzKeji.class, "where dbId>="+start+" and dbId<"+end);
		sendListInfo(session, list);
	}

	protected void sendListInfo(IoSession session, List<JzKeji> list) {
		KeJiInfoRet.Builder ret = KeJiInfoRet.newBuilder();
//		List<Keji> list = TempletService.listAll(Keji.class.getSimpleName());
		long time = System.currentTimeMillis();
		for(JzKeji k : list){
			KeJiConf.Builder cf = KeJiConf.newBuilder();
			cf.setId(k.kejiId);
//			cf.setName(k.name);
//			cf.setDesc(k.getDescription());
			cf.setCiShu(k.ciShu);
			if(k.lengQueTime>time){
				cf.setLengQue(k.lengQueTime-time);
			}else{
				cf.setLengQue(0);
			}
			ret.addData(cf);
		}
		session.write(ret.build());
	}
	
	public void keJiShengJi(int id, IoSession session, Builder builder){
		KeJiShengJiReq.Builder req = (qxmobile.protobuf.KeJiProto.KeJiShengJiReq.Builder) builder;
		int kejiId = req.getId();
		List<JunzhuKeji> list = TempletService.listAll(JunzhuKeji.class.getSimpleName());
		if(list == null){
			log.error("没有配置");
			return;
		}
		JunzhuKeji conf = null;
		for(JunzhuKeji k : list){
			if(k.getId() == kejiId){
				conf = k;
				break;
			}
		}
		if(conf == null){
			sendError(session,"没有这个科技"+ kejiId);
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			sendError(session, "君主没有初始化");
			return;
		}
		if(jz.tongBi<conf.getMoney()){
			sendError(session, "金币不足，需要"+conf.getMoney()+"，拥有"+jz.tongBi);
			return;
		}
		if(jz.level<conf.getLimitLevel()){
			sendError(session, "等级不足，需要"+conf.getLimitLevel()+"级，当前"+jz.level+"级");
			return;
		}
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			log.error("null junZhuId id");
			return ;
		}
		long start = junZhuId * spaceFactor;
		long end = start + keJiCnt;
		List<JzKeji> dblist = HibernateUtil.list(JzKeji.class, "where dbId>="+start+" and dbId<"+end);
		JzKeji preDb = null;
		for(JzKeji k : dblist){
			if(k.kejiId == kejiId){
				preDb = k;
				break;
			}
		}
		if(preDb == null){
			preDb = new JzKeji();
			preDb.dbId = junZhuId * spaceFactor + dblist.size();
			preDb.kejiId = kejiId;
		}
		//一个科技只研究一次。
		if(preDb.ciShu>0){//conf.yanjiucishu){
			sendError(session, "已研究了此科技。");
			return;
		}
		jz.tongBi -= conf.money;
		preDb.ciShu += 1;
		preDb.lengQueTime = System.currentTimeMillis() + conf.costTime * 1000;
		log.info("{} 花费 {} 研究科技 {}，达到{}",
				jz.name,conf.money,conf.name,preDb.ciShu);
		HibernateUtil.save(preDb);
		log.info("保存成功 {}",preDb.dbId);
		dblist.add(preDb);
		sendListInfo(session, dblist);
		// 君主科技升级完成
		EventMgr.addEvent(ED.JUNZHU_KEJI_PROMOTE, new Object[]{junZhuId, kejiId});
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
}
