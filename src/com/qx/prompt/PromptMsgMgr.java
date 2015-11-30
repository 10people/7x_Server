package com.qx.prompt;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Prompt.PromptMSGResp;
import qxmobile.protobuf.Prompt.SuBaoMSG;
import qxmobile.protobuf.Prompt.UpDateSuBaoReq;
import qxmobile.protobuf.Prompt.UpDateSuBaoResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.alliance.AllianceBean;
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
import com.qx.world.FightScene;
import com.qx.world.Mission;
import com.qx.world.Scene;
//盟友快报Mgr
public class PromptMsgMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(PromptMsgMgr.class);
	public static PromptMsgMgr inst;
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0, null, null);
	public ConcurrentHashMap<Integer, Scene> yabiaoScenes;
	public PromptMsgMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "PromptMsgMgr").start();
	}

	public void initData() {
		
	}

	@Override
	public void run() {
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
		log.info("退出PromptMsgMgr");
	}


	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.C_MengYouKuaiBao_Req:
			getMengyoukuaibao(id, builder, session);
			break;
		default:
			log.error("PromptMsgMgr-未处理的消息{}", id);
			break;
		}

	}

	//保存盟友速报 
	public PromptMSG saveLianMengKuaiBao(long jzId,long otherJzId,String otherJzName,
			int msgType,long startTime,String award,String content) {
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where otherJzId='"+otherJzId+"' and jzId='"
			+jzId+"'and startTime='"+startTime+"'");
		Date now=new Date();
		if(promptMsgList.size()>0){
			log.info("联盟中成员--{}保存--{}的押镖开始时间为---{}的盟友速报已存在",jzId,otherJzId,startTime);
			return null;
		}else{
			PromptMSG msg = new PromptMSG();
//			String content="哇啦啦哇凉哇凉你被杀了";
			msg.jzId = jzId;
			msg.otherJzId = otherJzId;
			msg.content=content;
			msg.addTime=now;
			msg.msgType=msgType;
			//TODO 读取配置加载
			msg.startTime=now.getHours()*100+now.getMinutes();
			msg.award=award;
			HibernateUtil.save(msg);
			return msg;
		}
	}
	
	
	/**
	 * @Description 忽略速报
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void IgnoreSuBao(int id, Builder builder, IoSession session)  {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("答复协助君主不存在");
			return;
		}
		UpDateSuBaoReq.Builder req=(UpDateSuBaoReq.Builder)builder;
		long subaoId =req.getSubaoId();
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		UpDateSuBaoResp.Builder resp=UpDateSuBaoResp.newBuilder();
		if(msg==null){
			resp.setSubaoId(id);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		HibernateUtil.delete(msg);
		resp.setSubaoId(id);
		resp.setResult(10);
		session.write(resp.build());
	}

	
	/**
	 * @Description 领取速报的奖励
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void gainJiangli2SuBao(int id, Builder builder, IoSession session)  {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("答复协助君主不存在");
			return;
		}
		UpDateSuBaoReq.Builder req=(UpDateSuBaoReq.Builder)builder;
		long subaoId =req.getSubaoId();
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		UpDateSuBaoResp.Builder resp=UpDateSuBaoResp.newBuilder();
		if(msg==null){
			resp.setSubaoId(id);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		//领取奖励
		String award=msg.award;
		AwardMgr.inst.giveReward(session, award, jz);
		//TODO 给盟友发快报 读取配置得到
		long jzId=jz.id;
		String jzName=jz.name;
		if(msg.msgType==SuBaoConstant.mengyouchufa){
			saveLMKB4ZhuFu(msg.otherJzId,jzId, jzName);
		}
		if(msg.msgType==SuBaoConstant.mengyoubeisha){
			saveLMKB4AnWei(msg.otherJzId,jzId, jzName);
		}
		
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(id);
		resp.setResult(10);
		resp.setFujian(award);
		session.write(resp.build());
		
	}
	
	/**
	 * @Description 根据msgType保存联盟快报
	 */
	public void saveLMKB4AllType(long shoushangJzId,long jzId,String jzName,int msgType) {
		long startTime=System.currentTimeMillis();
		String award="";
		String content="被杀啦被杀啦被杀啦被杀啦被杀啦被杀啦";
		log.info("联盟成员--{} 保存来自盟友{}的安慰联盟速报",shoushangJzId,jzId);
		saveLianMengKuaiBao(shoushangJzId, jzId, jzName, msgType, startTime,award,content);
	}
	/**
	 * @Description 联盟成员-jzId 保存来自盟友-otherJzId的安慰联盟速报
	 * @param shoushangJzId 被安慰君主Id
	 * @param jzId 安慰人的君主Id
	 * @param jzName 安慰人的君主Name
	 * @param msgType 
	 * @param startTime
	 */
	public void saveLMKB4AnWei(long shoushangJzId,long jzId,String jzName) {
		// TODO startTime msgType
		int msgType=SuBaoConstant.mengyouanwei;
		long startTime=System.currentTimeMillis();
		String award="";
		String content="被杀啦被杀啦被杀啦被杀啦被杀啦被杀啦";
		log.info("联盟成员--{} 保存来自盟友{}的安慰联盟速报",shoushangJzId,jzId);
		saveLianMengKuaiBao(shoushangJzId, jzId, jzName, msgType, startTime,award,content);
	}
	
	/**
	 * @Description 联盟成员--yunbiaoJzId保存来自盟友--jzId 的祝福联盟速报
	 * @param yunbiaoJzId
	 * @param jzId
	 * @param jzName
	 */
	public void saveLMKB4ZhuFu(long yunbiaoJzId,long jzId,String jzName) {
		// TODO startTime msgType
		int msgType= SuBaoConstant.mengyouzhufu;
		long startTime=System.currentTimeMillis();
		String award="";
		String content="";
		log.info("联盟成员--{} 保存来自盟友{}的祝福联盟速报",yunbiaoJzId,jzId);
		saveLianMengKuaiBao(yunbiaoJzId, jzId, jzName, msgType, startTime,award,content);
	}

	/**
	 * @Description 向联盟的成员推送押镖盟友速报
	 * @param chufaJzId
	 * @param lmBean
	 * @param horseType
	 * @param msgType
	 */
	public void pushPromptMSG4BiaoCheBeiDa(long chufaJzId, AllianceBean lmBean,int horseType,int msgType,long startTime) {
		log.info("向联盟---{}的成员推送押镖盟友速报开始",lmBean.id);
		JunZhu chufaJz=HibernateUtil.find(JunZhu.class, chufaJzId);
		if(chufaJz==null){
			log.error("向联盟---{}的成员推送押镖盟友速报失败，未找到君主--{}", lmBean.id,chufaJzId);
			return;
		}
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmBean.id);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的成员推送押镖盟友速报失败，联盟无成员", lmBean.id);
			return;
		}
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			if(jzId==chufaJzId){
				//TODO 触发事件的君主单独处理
				
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			String award="";
			String content="";
			PromptMSG msg=saveLianMengKuaiBao(jzId, chufaJzId, jz.name, msgType, startTime,award,content);
			if(msg==null){
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
				Scene scene = (FightScene) su.session.getAttribute(SessionAttKey.Scene);
				if (scene != null) {
					//TODO 场景不同发不发送
					 if(scene.name.contains("YB")){
						 // 联盟成员的君主id
						 SuBaoMSG.Builder msgResp = SuBaoMSG.newBuilder();
						 msgResp.setOtherJzId(msg.otherJzId);
						 msgResp.setMsgType(msg.msgType);
						 su.session.write(msgResp.build());
					 }else{
						 //TODO
						log.info("主城");
					}	
				}
			}
		}
		log.info("向联盟---{}的成员推送押镖盟友速报结束",lmBean.id);
	}
	public void pushKuaiBao4BiaoCheCuiHui(Long chufaJzId,
			AllianceBean lmBean, Integer horseType, Integer msgType,
			Long startTime) {
		// TODO 删除之前的盟友快报
		//TODO 通知主人被杀  通知盟友发安慰快报 
		
	}
	public void pushKuaiBao4BiaoCheDaoDa(Long chufaJzId, AllianceBean lmBean,
			Integer horseType, Integer msgType, Long startTime) {
		// TODO Auto-generated method stub
	}

	/**
	 * @Description: 推送掠夺盟友速报
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void pushPromptMSG4LvDuo(long jzId) {
		//TODO 推送掠夺盟友速报
	}
	/**
	 * @Description: 获取盟友快报
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getMengyoukuaibao(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("答复协助君主不存在");
			return;
		}
		Long jzId = jz.id;
		PromptMSGResp.Builder resp = PromptMSGResp.newBuilder();
		// 查询出8小时内的盟友速报记录
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where jzId='"+jzId+"' and  addTime>date_add(now(), interval -8 hour)");
		for (PromptMSG msg: promptMsgList) {
			SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
			subao.setSubaoId(msg.id);
			subao.setOtherJzId(msg.otherJzId);
			subao.setSubao(msg.content);
			subao.setMsgType(msg.msgType);
			resp.addMsgList(subao.build());
		}
		session.write(resp.build());
	}
	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void shutdown() {
		missions.add(exit);
		Iterator<Scene> it = yabiaoScenes.values().iterator();
		while(it.hasNext()){
			it.next().shutdown();
		}
	}

	@Override
	public void proc(Event e) {
		//镖车被打更新SOS列表
		Object[] oa = null;
		Long chufaJzId = null;
		Integer horseType = null;
		Integer msgType = null;
		Long startTime=null;
		AllianceBean lmBean=null;
		switch (e.id) {
		case ED.BIAOCHE_BEIDA:
			//TODO 镖车被打更新SOS列表
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			msgType = (Integer) oa[2];
			startTime=(Long) oa[3];
			if(chufaJzId==null||horseType==null||msgType==null||startTime==null){
				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType,startTime);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean == null) {
				log.info("{}没有联盟，不用推送重要提示列表", chufaJzId);
				return;
			}
			pushPromptMSG4BiaoCheBeiDa(chufaJzId, lmBean, horseType, msgType, startTime);
			break;
		case ED.BIAOCHE_BEISHA:
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			msgType = (Integer) oa[2];
			startTime=(Long) oa[3];
			if(chufaJzId==null||horseType==null||msgType==null||startTime==null){
				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType,startTime);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean == null) {
				log.info("{}没有联盟，不用推送重要提示列表", chufaJzId);
				return;
			}
			pushKuaiBao4BiaoCheCuiHui(chufaJzId, lmBean, horseType, msgType, startTime);
			break;
		case ED.BIAOCHE_END:
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			msgType = (Integer) oa[2];
			startTime=(Long) oa[3];
			if(chufaJzId==null||horseType==null||msgType==null||startTime==null){
				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType,startTime);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean == null) {
				log.info("{}没有联盟，不用推送重要提示列表", chufaJzId);
				return;
			}
			pushKuaiBao4BiaoCheDaoDa(chufaJzId, lmBean, horseType, msgType, startTime);
			break;
		default:
			break;
		}
	}


	
	@Override
	protected void doReg() {
		EventMgr.regist(ED.BIAOCHE_BEIDA, this);
		EventMgr.regist(ED.BIAOCHE_BEISHA, this);
		EventMgr.regist(ED.BIAOCHE_END, this);
	}



}