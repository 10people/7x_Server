package com.qx.junzhu;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ChengHaoProto.ChengHaoData;
import qxmobile.protobuf.ChengHaoProto.ChengHaoList;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Chenghao;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.bag.Bag;
import com.qx.bag.EquipGrid;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.persistent.HibernateUtil;

public class ChenghaoMgr extends EventProc{
	public static Logger log = LoggerFactory.getLogger(ChenghaoMgr.class.getSimpleName());
	public static ChenghaoMgr inst;
	public ChenghaoMgr(){
		inst = this;
	}
	public void sendCur(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		ChengHaoBean bean = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
		sendCur(session, bean);
	}
	public void sendCur(IoSession session, ChengHaoBean bean) {
		ChengHaoData.Builder b = ChengHaoData.newBuilder();
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		Chenghao fakeOne = confList.get(0);
		fillConf(b,fakeOne);//只是为了匹配协议
		if(bean == null){
			b.setId(-1);
			b.setName("");
		}else{
			b.setId(bean.tid);
			b.setName("称号:"+bean.tid);
		}
		session.setAttribute(SessionAttKey.CHENG_HAO_ID, String.valueOf(b.getId()));
		b.setState('U');
		ProtobufMsg msg = new ProtobufMsg();
		msg.builder = b;
		msg.id = PD.S_GET_CUR_CHENG_HAO;
		session.write(msg);
	}
	public void sendList(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class, "where jzId="+junZhuId);
		Map<Integer, ChengHaoBean> m = new HashMap<Integer, ChengHaoBean>(list.size());
		for(ChengHaoBean b : list){
			m.put(b.tid, b);
		}
		//
		ChengHaoList.Builder ret = ChengHaoList.newBuilder();
		//
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		if(confList == null){
			confList = Collections.emptyList();
			log.error("配置为空");
		}
		ChengHaoData.Builder b = ChengHaoData.newBuilder();
		long curMs = System.currentTimeMillis();
		for(Chenghao conf : confList){
			b.setId(conf.id);
			b.setName(conf.name);
			int ss = 0;
			ChengHaoBean bean = m.get(conf.id);
			fillConf(b, conf);
			if(bean != null){
				ss = bean.state;
				long endMs = bean.expireTime.getTime();
				long leftMs = endMs - curMs;
				if(leftMs<0){
					log.info("{} 称号过期 {}",junZhuId, bean.tid);
					HibernateUtil.delete(bean);
				}else{
					leftMs /= 1000;
					b.setLeftSec(leftMs);
				}
			}
			b.setState(ss);
			ret.addList(b.build());
		}
		ret.setMyPoint(999);
		ProtobufMsg msg = new ProtobufMsg();
		msg.builder = ret;
		msg.id = PD.S_LIST_CHENG_HAO;
		session.write(msg);
	}
	public void fillConf(ChengHaoData.Builder b, Chenghao conf) {
		b.setLeftSec(conf.validityPeriod*3600*24);
		b.setGongJi(conf.id+1);
		b.setFangYu(conf.id+2);
		b.setShengMing(conf.id+3);
		b.setPrice(conf.id%1000);
	}
	public void use(int id, IoSession session, Builder builder) {
		TalentUpLevelReq.Builder req = (TalentUpLevelReq.Builder) builder;
		if(req == null){
			return;
		}
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		int which = req.getPointId();
		if(which <= 0){//卸下
			ChengHaoBean cur = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
			if(cur != null){
				cur.state = 'G';
				HibernateUtil.save(cur);
				log.info("{} 卸下 {}",junZhuId, cur.tid);
			}else{
				log.info("{} 卸下 {} , not found ",junZhuId, which);
			}
			return;
		}
		ChengHaoBean want = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and tid="+which);
		if(want == null){
			log.warn("{}要使用未获得的称号{}",junZhuId,which);
			return;
		}
		ChengHaoBean preUse = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
		if(preUse != null){
			preUse.state = 'G';
			HibernateUtil.save(preUse);
		}
		want.state = 'U';
		HibernateUtil.save(want);
		sendCur(session, want);
	}
	@Override
	public void proc(Event param) {
//		switch(param.id){
//		case ED.PVE_GUANQIA:
//			Object[] data = (Object[]) param.param;
//			checkGet(data);
//			break;
//		}		
	}
	protected void checkGet(Object[] data) {
		/* 老称号不要了
		Integer guanQiaId = (Integer) data[1];
		Long pid = (Long) data[0];
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		if(confList == null){
			log.error("conf list is null");
			return;
		}
		Chenghao conf = null;
		String guanQiaIdStr = String.valueOf(guanQiaId);
		for(Chenghao ch : confList){
			if( guanQiaIdStr.equals(ch.condition)){
				conf = ch;
				break;
			}
		}
		if(conf == null){
			return;
		}
		ChengHaoBean want = HibernateUtil.find(ChengHaoBean.class, "where jzId="+pid+" and tid="+conf.id);
		if(want != null){
			return;
		}
		IoSession ss = add(pid, conf);
		if(ss != null && ss.isConnected()){
			ChengHaoData.Builder note = ChengHaoData.newBuilder();
			fillConf(note, conf);
			note.setId(conf.id);
			note.setName("");
			note.setState('N');
			ProtobufMsg msg = new ProtobufMsg();
			msg.id = PD.S_NEW_CHENGHAO;
			msg.builder = note;
			ss.write(msg);//通知获得新称号
		}
		*/
	}
	public IoSession add(Long pid, Chenghao conf) {
		ChengHaoBean want;
		want = new ChengHaoBean();
		want.jzId = pid;
		want.tid = conf.id;
		want.state = 'G';
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, conf.validityPeriod);
		want.expireTime = c.getTime();
		HibernateUtil.insert(want);
		log.info("{}获得称号{} {}",pid,conf.id,conf.name);
		EventMgr.addEvent(ED.GAIN_CHENG_HAO, new Object[]{pid, conf});
		IoSession ss = AccountManager.sessionMap.get(pid);
		return ss;
	}
	@Override
	protected void doReg() {
//		EventMgr.regist(ED.PVE_GUANQIA, this);
	}
	public void duiHuan(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		TalentUpLevelReq.Builder req = (TalentUpLevelReq.Builder)builder;
		int which = req.getPointId();
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class,
				"where jzId="+junZhuId+" and tid="+which);
		if(list.size()>0){
			return;
		}
		Optional<Chenghao> op = confList.stream().filter(c->c.id==which).findAny();
		if(op.isPresent()==false){
			return;
		}
		add(junZhuId, op.get());
		log.info("{}兑换{}OK",junZhuId,which);
		sendList(0, session, null);
	}
}
