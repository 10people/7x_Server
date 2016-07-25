package com.qx.junzhu;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ChengHaoProto.ChengHaoData;
import qxmobile.protobuf.ChengHaoProto.ChengHaoList;
import qxmobile.protobuf.ChengHaoProto.ChengHaoListReq;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.Chenghao;
import com.manu.dynasty.template.Mail;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.huangye.shop.ShopMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.world.Player;
import com.qx.world.Scene;

public class ChenghaoMgr extends EventProc{
	public static Logger log = LoggerFactory.getLogger(ChenghaoMgr.class.getSimpleName());
	public static ChenghaoMgr inst;
	public Map<Integer, Chenghao> chenghaoMap = null;
	public ChenghaoMgr(){
		inst = this;
		init();
	}
	public void init(){
		chenghaoMap = new HashMap<Integer,Chenghao>();
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		for (Chenghao chenghao : confList) {
			chenghaoMap.put(chenghao.id,chenghao);
		}
	}
	public void sendCur(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		//ChengHaoBean bean = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
		ChengHaoBean bean = ChengHaoDao.inst.getChengHaoBeanByState(junZhuId, 'U');
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
			b.setState(0);
		}else{
			long curMs = System.currentTimeMillis();
			long endMs = bean.expireTime.getTime();
			long leftMs = endMs - curMs;
			if(leftMs<=0){
				HibernateUtil.delete(bean);
				ChengHaoDao.inst.delete(bean);
				b.setLeftSec(0);
				b.setId(-1);
				b.setName("");
				b.setState(0);
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				sendMail(jz,bean.tid);
				broadNewChengHao(session,jz.id,"-1");
			}else{
				leftMs /= 1000;
				b.setLeftSec(leftMs);
				b.setId(bean.tid);
				b.setName("称号:"+bean.tid);
				b.setState(bean.state);
			}
			
		}
		session.setAttribute(SessionAttKey.CHENG_HAO_ID, String.valueOf(b.getId()));
		ProtobufMsg msg = new ProtobufMsg();
		msg.builder = b;
		msg.id = PD.S_GET_CUR_CHENG_HAO;
		session.write(msg);
	}
	public void sendList(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		Long junZhuId = jz.id;
		ChengHaoListReq.Builder req = (ChengHaoListReq.Builder) builder;
		int type = req.getType();
		ChengHaoList.Builder ret = ChengHaoList.newBuilder();
		getNewChengHaoList(session, jz, type , ret);
		ret.setMyPoint(ShopMgr.inst.getMoney(ShopMgr.Money.gongXun,junZhuId,null));
		ret.setWeiWang(ShopMgr.inst.getMoney(ShopMgr.Money.weiWang,junZhuId,null));
		ProtobufMsg msg = new ProtobufMsg();
		msg.builder = ret;
		msg.id = PD.S_LIST_CHENG_HAO;
		session.write(msg);
	}
	public void fillConf(ChengHaoData.Builder b, Chenghao conf) {
		b.setLeftSec(conf.validityPeriod*3600*24);
		b.setGongJi(conf.add_injury_scale);
		b.setFangYu(conf.reduce_injury_scale);
		b.setShengMing(0); //2016年6月3日改为0前段不显示
		b.setPrice(conf.price);
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
			//ChengHaoBean cur = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
			ChengHaoBean cur = ChengHaoDao.inst.getChengHaoBeanByState(junZhuId, 'U');
			if(cur != null){
				cur.state = 'G';
				HibernateUtil.save(cur);
				log.info("{} 卸下 {}",junZhuId, cur.tid);
			}else{
				log.info("{} 卸下 {} , not found ",junZhuId, which);
			}
			//通知其他玩家
			EventMgr.addEvent(junZhuId,ED.PLAYER_CHENGHAO_USE, new Object[]{session,junZhuId,"-1"});
			//更新称号（修复wpe卸下称号君主面板称号不卸下）
			List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
			Chenghao fakeOne = confList.get(0);
			ChengHaoData.Builder b = ChengHaoData.newBuilder();
			fillConf(b,fakeOne);//只是为了匹配协议
			b.setId(-1);
			b.setName("");
			b.setState(0);
			session.setAttribute(SessionAttKey.CHENG_HAO_ID, String.valueOf(b.getId()));
			ProtobufMsg msg = new ProtobufMsg();
			msg.builder = b;
			msg.id = PD.S_GET_CUR_CHENG_HAO;
			session.write(msg);
			return;
		}
		//ChengHaoBean want = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and tid="+which);
		ChengHaoBean want = ChengHaoDao.inst.getChengHaoBeanById(junZhuId,which);
		if(want == null){
			log.warn("{}要使用未获得的称号{}",junZhuId,which);
			return;
		}
		//ChengHaoBean preUse = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
		ChengHaoBean preUse = ChengHaoDao.inst.getChengHaoBeanByState(junZhuId, 'U');
		if(preUse != null){
			preUse.state = 'G';
			HibernateUtil.save(preUse);
		}
		want.state = 'U';
		HibernateUtil.save(want);
		sendCur(session, want);
		if(which > 0){
			Chenghao chenghao = chenghaoMap.get(which);
			EventMgr.addEvent(junZhuId,ED.PLAYER_CHENGHAO_USE, new Object[]{session,junZhuId,chenghao.id});
		}
	}
	@Override
	public void proc(Event event) {
		switch(event.id){
		case ED.JUNZHU_LOGIN:{
			JunZhu jz = (JunZhu) event.param;
			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				return;
			}
			refreshChengHaoRed(event, session, jz);
		}
		break;
		case ED.CHANGE_GONGXUN:
		case ED.CHANGE_WEIWANG:{
			Object[] objects = (Object[])event.param;
			IoSession session = (IoSession)objects[0];
			JunZhu jz = (JunZhu)objects[1];
			refreshChengHaoRed(event,session,jz);
		}
			break;
		case ED.REFRESH_TIME_WORK:{
			IoSession session = (IoSession) event.param;
			if(session == null) return;
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz == null) return;
			sendCur(0, session, null);
			ChengHaoList.Builder ret = ChengHaoList.newBuilder();
			getNewChengHaoList(session,jz,-1,ret);
		}
			break;
		case ED.PLAYER_CHENHAO_DUIHUAN:{
			Object[] objects = (Object[])event.param;
			long jzId = (long)objects[0];
			IoSession session = AccountManager.sessionMap.get(jzId);
			if(session == null){
				return;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			refreshChengHaoRed(event,session,jz);
		}
			break;
		case ED.PLAYER_CHENGHAO_USE:{
			Object[] objects = (Object[]) event.param;
			IoSession session = (IoSession)objects[0];
			if(session == null) return;
			long jzId = (long)objects[1];
			String chenghaoId = String.valueOf(objects[2]);
			broadNewChengHao(session, jzId, chenghaoId);
		}
		break;
//		case ED.PVE_GUANQIA:
//			Object[] data = (Object[]) param.param;
//			checkGet(data);
//			break;
		}		
	}
	public void checkGet(Object[] data) {
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
		ChengHaoDao.inst.save(want);
		log.info("{}获得称号{} {}",pid,conf.id,conf.name);
		EventMgr.addEvent(pid,ED.GAIN_CHENG_HAO, new Object[]{pid, conf});
		IoSession ss = AccountManager.sessionMap.get(pid);
		return ss;
	}
	@Override
	public void doReg() {
//		EventMgr.regist(ED.PVE_GUANQIA, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		EventMgr.regist(ED.CHANGE_GONGXUN, this);
		EventMgr.regist(ED.CHANGE_WEIWANG, this);
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.PLAYER_CHENHAO_DUIHUAN, this);
		EventMgr.regist(ED.PLAYER_CHENGHAO_USE, this);
	}
	public void duiHuan(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		TalentUpLevelReq.Builder req = (TalentUpLevelReq.Builder)builder;
		int which = req.getPointId();
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
//		List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class,"where jzId="+jz.id+" and tid="+which);
		ChengHaoBean chengHaoBean = ChengHaoDao.inst.getChengHaoBeanById(jz.id, which);
		if(chengHaoBean  != null){
			return;
		}
		Optional<Chenghao> op = confList.stream().filter(c->c.id==which).findAny();
		if(op.isPresent()==false){
			return;
		}
		//扣功勋或威望
		int daibi = op.get().daibi;
		int price = op.get().price;
		ShopMgr.Money m = null;
		int type = 0;
		if(daibi == AwardMgr.ITEM_WEI_WANG){ //威望
			m = ShopMgr.Money.weiWang;
			type = 1;
		}else if(daibi == AwardMgr.ITEM_GONG_XUN){ //功勋
			m = ShopMgr.Money.gongXun;
			type = 0;
		}
		if(m != null){
			int hava = ShopMgr.inst.getMoney(m,jz.id, null);
			if(hava < price){
				log.error("货币不足");
				return;
			}
			ShopMgr.inst.diffMoney(session,jz, m,price);
		}else{
			log.error("未知货币类型");
			return;
		}
		add(jz.id, op.get());
		log.info("{}兑换{}OK",jz.id,which);
		ChengHaoListReq.Builder req2 = ChengHaoListReq.newBuilder() ;
		req2.setType(type);
		sendList(0, session, req2);
		//添加广播事件
		EventMgr.addEvent(jz.id,ED.PLAYER_CHENHAO_DUIHUAN, new Object[]{jz.id,which});
	}
	
	/**
	 * 教研称号是否过期
	 * @param chenghaoId
	 * @param jzId
	 * @return true过期
	 */
	public boolean isChenghaoOverTime(String chenghaoId,long jzId){
		if(chenghaoId == null || "".equals(chenghaoId)){
			return false;
		}
		ChengHaoBean bean = HibernateUtil.find(ChengHaoBean.class, "where jzId="+jzId +" and tid=" + chenghaoId);
		if(bean != null){
			long endMs = bean.expireTime.getTime();
			long leftMs = endMs - Calendar.getInstance().getTimeInMillis();
			if(leftMs<0){
				HibernateUtil.delete(bean);
				ChengHaoDao.inst.delete(bean);
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	public String updateChengHao(String chenghaoId,JunZhu jz){
		if(!"-1".equals(chenghaoId) && isChenghaoOverTime(chenghaoId,jz.id)){
			sendMail(jz, Integer.parseInt(chenghaoId));
//			Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(110013);
//			if (mailConfig != null) {
//				String content = mailConfig.content.replace("XXXX", ChenghaoMgr.inst.chenghaoMap.get(Integer.parseInt(chenghaoId)).name);
//				EmailMgr.INSTANCE.sendMail(jz.name, content, "",
//						mailConfig.sender, mailConfig, "");
//			}
			chenghaoId = "-1"; //更新称号
		}
		return chenghaoId;
	}
	
	public void sendMail(JunZhu jz,int chenghaoId){
		Chenghao chenghao = chenghaoMap.get(chenghaoId);
		if(chenghao == null) return;
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(110013);
		if (mailConfig != null) {
			String content = mailConfig.content.replace("XXXX", chenghaoMap.get(chenghaoId).name);
			EmailMgr.INSTANCE.sendMail(jz.name, content, "",
					mailConfig.sender, mailConfig, "");
		}
	}
	
	public Chenghao getCurEquipCfg(long junZhuId) {
		//ChengHaoBean bean = HibernateUtil.find(ChengHaoBean.class, "where jzId="+junZhuId+" and state='U'");
		ChengHaoBean bean = ChengHaoDao.inst.getChengHaoBeanByState(junZhuId, 'U');
		if(bean == null) {
			return null;
		}
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		Optional<Chenghao> op = confList.stream().filter(c->c.id==bean.tid).findAny();
		return op.get();
	}
	
	public void refreshChengHaoRed(Event event,IoSession session,JunZhu jz){
//		List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class, "where jzId="+jz.id);
		Map<Integer,ChengHaoBean> chenghaoBeanMap = ChengHaoDao.inst.getMap(jz.id);
		int chenghaoId1 = 0; //郡城战
		int chenghaoId2 = 0; //百战
		long curMs = System.currentTimeMillis();
		for (Integer key : chenghaoBeanMap.keySet()) {
			ChengHaoBean b = chenghaoBeanMap.get(key);
			long endMs = b.expireTime.getTime();
			long leftMs = endMs - curMs;
			if(leftMs<0){//称号过期
				continue;
			}
			Chenghao chenghaoNew = chenghaoMap.get(b.tid);
			if(chenghaoNew.daibi == AwardMgr.ITEM_GONG_XUN){
				if(chenghaoId1 == 0){
					chenghaoId1 = b.tid;
				}else{
					Chenghao chenghaoTmp = chenghaoMap.get(chenghaoId1);
					if(chenghaoNew.add_injury_scale >chenghaoTmp.add_injury_scale || chenghaoNew.reduce_injury_scale > chenghaoTmp.reduce_injury_scale){
						chenghaoId1 = b.tid;
					}
				}
			}
			if(chenghaoNew.daibi == AwardMgr.ITEM_WEI_WANG){
				if(chenghaoId2 == 0){
					chenghaoId2 = b.tid;
				}else{
					Chenghao chenghaoTmp = chenghaoMap.get(chenghaoId2);
					if(chenghaoNew.add_injury_scale >chenghaoTmp.add_injury_scale || chenghaoNew.reduce_injury_scale > chenghaoTmp.reduce_injury_scale){
						chenghaoId2 = b.tid;
					}
				}
			}
		}
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		if(confList == null){
			confList = Collections.emptyList();
			log.error("配置为空");
			return;
		}
		int haveWeiWang = ShopMgr.inst.getMoney(ShopMgr.Money.weiWang,jz.id, null);
		int haveGongxun = ShopMgr.inst.getMoney(ShopMgr.Money.gongXun,jz.id, null);
		boolean junchenzhanRed = false;
		boolean baizhanRed = false;
		for(Chenghao conf : confList){
			if(junchenzhanRed && baizhanRed) break;
			if(conf.daibi == AwardMgr.ITEM_GONG_XUN && !junchenzhanRed){
				if(haveGongxun >= conf.price){
					if(chenghaoId1 == 0){
						junchenzhanRed = true;
					}else{
						Chenghao chenghao = chenghaoMap.get(chenghaoId1);
						if(conf.add_injury_scale > chenghao.add_injury_scale || conf.reduce_injury_scale > chenghao.reduce_injury_scale){
							junchenzhanRed = true;
						}
					}
				}
			}
			if(conf.daibi == AwardMgr.ITEM_WEI_WANG && !baizhanRed){
				if(haveWeiWang  >= conf.price){
					if(chenghaoId2 == 0){
						baizhanRed = true;
					}else{
						Chenghao chenghao = chenghaoMap.get(chenghaoId2);
						if(conf.add_injury_scale > chenghao.add_injury_scale || conf.reduce_injury_scale > chenghao.reduce_injury_scale){
							baizhanRed = true;
						}
					}
				}
			}
		}
		if(junchenzhanRed){
			FunctionID.pushCanShowRed(jz.id, session,FunctionID.chenghao_junchengzhan);
		}else{
			FunctionID.pushCanShowRed(jz.id, session,-FunctionID.chenghao_junchengzhan);
		}
		if(baizhanRed){
			FunctionID.pushCanShowRed(jz.id, session,FunctionID.chenghao_baizhan);
		}else{
			FunctionID.pushCanShowRed(jz.id, session,-FunctionID.chenghao_baizhan);
		}
	}
	
	public void broadNewChengHao(IoSession session,long jzId,String chenghao){
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(scene == null)return;
		Player player = scene.getPlayerByJunZhuId(jzId);
		if(player == null)return;
		player.chengHaoId = chenghao;
		qxmobile.protobuf.Scene.EnterScene.Builder info = scene.buildEnterInfo(player);
		ProtobufMsg msg = new ProtobufMsg(PD.S_HEAD_INFO, info);
		scene.broadCastEvent(msg, 0/*player.userId*/);
	}
	/**
	 * 获得最新称号列表
	 * @param session
	 * @param jz
	 * @param type 0-功勋称号,1-威望,-1-所有
	 */
	public void getNewChengHaoList(IoSession session,JunZhu jz,int type,ChengHaoList.Builder ret){
		Long junZhuId = jz.id;
		//List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class, "where jzId="+junZhuId);
		Map<Integer, ChengHaoBean> m = ChengHaoDao.inst.getMap(junZhuId);
		List<Chenghao> confList = TempletService.listAll(Chenghao.class.getSimpleName());
		if(confList == null){
			confList = Collections.emptyList();
			log.error("配置为空");
		}
		ChengHaoData.Builder b = ChengHaoData.newBuilder();
		long curMs = System.currentTimeMillis();
		for(Chenghao conf : confList){
			if(type == 0){ //功勋
				if(conf.daibi != AwardMgr.ITEM_GONG_XUN) continue;
			}else if(type == 1){ //威望
				if(conf.daibi != AwardMgr.ITEM_WEI_WANG) continue;
			}else{
			}
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
					if(bean.state == 'U'){
						broadNewChengHao(session,jz.id,"-1");
					}
					HibernateUtil.delete(bean);
					ChengHaoDao.inst.delete(bean);
					ss = 0; //更新状态
					sendMail(jz,conf.id);
				}else{
					leftMs /= 1000;
					b.setLeftSec(leftMs);
				}
			}
			b.setState(ss);
			ret.addList(b.build());
		}
	}
}
