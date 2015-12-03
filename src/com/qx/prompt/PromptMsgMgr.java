package com.qx.prompt;

import java.util.Date;
import java.util.HashSet;
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
import com.manu.network.BigSwitch;
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
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;
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
		case PD.C_MOVE2BIAOCHE_REQ:
			move2BiaoChe(id, builder, session);
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
			log.error("联盟成员--{}保存 来自盟友--{}的押镖速报失败，盟友速报已存在",jzId,otherJzId);
			return null;
		}else{
			PromptMSG msg = new PromptMSG();
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
	//删除盟友速报 
	public boolean deleteLMSB(long jzId,long ybJzID,long startTime) {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class, "where otherJzId='"+ybJzID+"' and jzId='"
				+jzId+"'and startTime='"+startTime+"'");
		if (msg!=null) {
			HibernateUtil.save(msg);
			return true;
		} else {
			return false;
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
	 * @Description 请求前往镖车坐标，返回镖车坐标
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void move2BiaoChe(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求前往镖车失败，未找到请求君主");
			return;
		}
		log.info("{}请求前往镖车",jz.id);
		UpDateSuBaoReq.Builder req=(UpDateSuBaoReq.Builder)builder;
		long subaoId =req.getSubaoId();
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		UpDateSuBaoResp.Builder resp=UpDateSuBaoResp.newBuilder();
		if(msg==null){
			log.error("{}请求前往镖车失败，未找到快报--{}信息",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		long ybJzId=msg.otherJzId;
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("{}请求前往镖车失败，未找到--{}的镖车信息",jz.id,ybJzId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		log.info("{}请求前往{}镖车成功，镖车坐标x--{},z---{}",jz.id,ybJzId,ybr.posX,ybr.posZ);
		HibernateUtil.delete(msg);
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
			log.error("领取速报的奖励失败，君主不存在");
			return;
		}
		UpDateSuBaoReq.Builder req=(UpDateSuBaoReq.Builder)builder;
		long subaoId =req.getSubaoId();
		long jzId=jz.id;
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		UpDateSuBaoResp.Builder resp=UpDateSuBaoResp.newBuilder();
		if(msg==null){
			log.error("{}领取速报的奖励失败，速报--{}不存在",jzId,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		log.info("{}领取速报的奖励，速报--{} 开始",jzId,subaoId);
		//领取奖励
		String award=msg.award;
		AwardMgr.inst.giveReward(session, award, jz);
		//TODO 给盟友发快报 读取配置得到
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
		log.info("{}领取速报的奖励，速报--{} 结束",jzId,subaoId);
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
	 * @Description 保存给自己联盟快报
	 * @param shoushangJzId
	 * @param jzId
	 * @param jzName
	 */
	public void saveLMKB2Self(long jzId,long startTime) {
		int msgType=SuBaoConstant.zijibeida;
		String award="";
		String content="你的镖车正在被攻击，是否前往";
		log.info("保存给---{}自己的联盟速报",jzId);
		saveLianMengKuaiBao(jzId, jzId, "", msgType, startTime,award,content);
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
	 * @Description 向联盟的成员推送押镖盟友被打速报
	 * @param chufaJzId
	 * @param lmBean
	 * @param horseType
	 * @param msgType
	 */
	public void pushBiaoCheBeiDa2MengYou(long chufaJzId, AllianceBean lmBean,int horseType,int msgType,long startTime) {
		log.info("向联盟---{}的成员推送押镖盟友被打速报开始",lmBean.id);
		JunZhu chufaJz=HibernateUtil.find(JunZhu.class, chufaJzId);
		if(chufaJz==null){
			log.error("向联盟---{}的成员推送押镖盟友被打速报失败，未找到押镖君主--{}", lmBean.id,chufaJzId);
			return;
		}
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmBean.id);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的成员推送押镖盟友被打速报失败，联盟无成员", lmBean.id);
			return;
		}
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			if(jzId==chufaJzId){
				//TODO 触发事件的君主单独处理
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.error("向联盟---{}的成员推送押镖盟友被打速报失败，未找到押镖君主--{}", lmBean.id,jzId);
				continue;
			}
			String award="";
			String content="";
			PromptMSG msg=saveLianMengKuaiBao(jzId, chufaJzId, jz.name, msgType, startTime,award,content);
			if(msg==null){
				log.error("向联盟---{}的成员推送押镖盟友被打速报失败,保存速报失败", lmBean.id,jzId);
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

	/**
	 * @Description 推送镖车被打给自己
	 * @param jzId
	 * @param lmBean
	 * @param horseType
	 * @param msgType
	 */
	public void pushBiaoCheBeiDa2Self(Long jzId, 
			Integer horseType, Integer msgType,long  startTime) {
		log.info("推送镖车被打给自己--{}开始",jzId);
		JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
		if(jz==null){
			log.error("推送镖车被打给自己--{}失败，未找到押镖君主",jzId);
			return;
		}
		String award="";
		String content="你的镖车被打了";
		PromptMSG msg=saveLianMengKuaiBao(jzId, jzId, jz.name, msgType, startTime,award,content);
		if(msg==null){
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (su != null){
			Scene scene = (Scene) su.session.getAttribute(SessionAttKey.Scene);
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
					log.info("不在押镖场景");
				}	
			}
		}
		log.info("推送镖车被打给自己--{}结束",jzId);
	}
	/**
	 * @Description 向盟友推送镖车被摧毁速报
	 */
	public void pushBiaoCheBeiSha2MengYou(Long ybjzId,
			AllianceBean lmBean, Integer horseType, Integer msgType,Long  startTime) {
		log.info("向联盟---{}的成员推送押镖盟友被摧毁速报开始",lmBean.id);
		JunZhu ybJz=HibernateUtil.find(JunZhu.class, ybjzId);
		if(ybJz==null){
			log.error("向联盟---{}的成员推送押镖盟友被摧毁速报失败，未找到押镖君主--{}", lmBean.id,ybjzId);
			return;
		}
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmBean.id);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的成员推送押镖盟友被摧毁速报失败，联盟无成员", lmBean.id);
			return;
		}
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			if(jzId==ybjzId){
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.error("向联盟---{}的成员推送押镖盟友被摧毁速报失败，未找到押镖君主--{}", lmBean.id,jzId);
				continue;
			}
			// TODO 删除之前的盟友快报
			boolean isSuccess=deleteLMSB(jzId, ybjzId, startTime);
			if (!isSuccess) {
				log.error("向联盟---{}的成员推送押镖盟友被摧毁速报出错，删除君主--{}开始押镖时间为--{}速报失败 ",jzId,ybjzId, startTime);
			}
			//TODO 通知主人被杀  通知盟友发安慰快报 
			String award="";
			String content="盟友马车被劫安慰一下吧";
			PromptMSG msg=saveLianMengKuaiBao(jzId, ybjzId, jz.name, msgType, startTime,award,content);
			if(msg==null){
				log.error("向联盟---{}的成员推送押镖盟友被摧毁速报失败,保存速报失败", lmBean.id,jzId);
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

	
	/**
	 * @Description 推送镖车被摧毁给自己
	 * @param chufaJzId
	 * @param lmBean
	 * @param horseType
	 * @param msgType
	 */
	public void pushBiaoCheBeiSha2Self(Long jzId, AllianceBean lmBean,
			Integer horseType, Integer msgType,Long startTime) {
		log.info("推送镖车被摧毁给自己--{}开始",jzId);
		JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
		if(jz==null){
			log.error("推送镖车被摧毁给自己--{}失败，未找到押镖君主",jzId);
			return;
		}
		// TODO 删除之前的盟友快报
		boolean isSuccess=deleteLMSB(jzId, jzId, startTime);
		if (!isSuccess) {
			log.error("推送镖车被摧毁给自己错误，删除君主--{}开始押镖时间为--{}速报失败 ",jzId,startTime);
		}
		String award="";
		String content="你的镖车被摧毁了";
		PromptMSG msg=saveLianMengKuaiBao(jzId, jzId, jz.name, msgType, startTime,award,content);
		if(msg==null){
			log.error("推送镖车被摧毁给自己失败，保存摧毁速报失败，jzId--{} ",jzId);
			return;
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
					log.info("不在押镖场景");
				}	
			}
		}
		log.info("推送镖车被摧毁给自己--{}结束",jzId);		
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
	
	/**
	 * @Description jzId是不是 押镖君主ybJzId的协助者
	 * @param ybJzId
	 * @param jzId
	 * @return
	 */
	public boolean isXieZhu(long ybJzId,long jzId) {
		if(ybJzId==jzId){
			return false;
		}
		HashSet<Long> xzSet = YaBiaoHuoDongMgr.xieZhuCache4YBJZ.get(ybJzId);
		if (xzSet != null) {
			return	xzSet.contains(jzId);
		}
		return false;
		
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
		case ED.BIAOCHE_CHUFA://镖车出发
			//TODO 镖车被打更新SOS列表
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			startTime=(Long) oa[2];
			msgType = SuBaoConstant.mengyouchufa;
			if(chufaJzId==null||horseType==null||msgType==null||startTime==null){
				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType,startTime);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean != null) {
				pushBiaoCheBeiDa2MengYou(chufaJzId, lmBean, horseType, msgType, startTime);
			}
			pushBiaoCheBeiDa2Self(chufaJzId, horseType, msgType, startTime);
			break;
		case ED.BIAOCHE_BEIDA://镖车被攻击
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
			if (lmBean != null) {
				pushBiaoCheBeiDa2MengYou(chufaJzId, lmBean, horseType, msgType, startTime);
			}
			pushBiaoCheBeiDa2Self(chufaJzId, horseType, msgType, startTime);
			break;
		case ED.BIAOCHE_BEISHA://镖车被杀
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			msgType = (Integer) oa[2];
			startTime=(Long) oa[3];
			if(chufaJzId==null||horseType==null||msgType==null){
				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean != null) {
				pushBiaoCheBeiSha2MengYou(chufaJzId, lmBean, horseType, msgType, startTime);
			}
			pushBiaoCheBeiSha2Self(chufaJzId, lmBean, horseType, msgType, startTime);
			break;
//		case ED.BIAOCHE_END:
//			oa = (Object[]) e.param;
//			chufaJzId = (Long) oa[0];
//			horseType = (Integer) oa[1];
//			msgType = (Integer) oa[2];
//			if(chufaJzId==null||horseType==null||msgType==null){
//				log.error("chufaJzId=={}||horseType=={}||msgType=={}||startTime=={}",chufaJzId,horseType,msgType);
//				return;
//			}
//			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
//			if (lmBean != null) {
//				pushBiaoCheEnd2XieZhu(chufaJzId, lmBean, horseType, msgType);
//			}
//			break;
		default:
			break;
		}
	}


	
	@Override
	protected void doReg() {
		EventMgr.regist(ED.BIAOCHE_CHUFA, this);
		EventMgr.regist(ED.BIAOCHE_BEIDA, this);
		EventMgr.regist(ED.BIAOCHE_BEISHA, this);
		EventMgr.regist(ED.BIAOCHE_END, this);
	}



}