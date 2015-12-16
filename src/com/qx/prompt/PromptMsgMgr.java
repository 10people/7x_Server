package com.qx.prompt;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Prompt.PromptActionReq;
import qxmobile.protobuf.Prompt.PromptActionResp;
import qxmobile.protobuf.Prompt.PromptMSGResp;
import qxmobile.protobuf.Prompt.SuBaoMSG;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.ReportTemp;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.activity.ActivityMgr;
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
import com.qx.world.Mission;
import com.qx.yabiao.YaBiaoHuoDongMgr;
//盟友快报Mgr
public class PromptMsgMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(PromptMsgMgr.class);
	public static PromptMsgMgr inst;
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0, null, null);
	public static Map<Integer, ReportTemp> reportMap;
	public PromptMsgMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "PromptMsgMgr").start();
	}

	public void initData() {
		List<ReportTemp> reportList = TempletService.listAll(ReportTemp.class.getSimpleName());
		 Map<Integer, ReportTemp> reportMap = new HashMap<Integer, ReportTemp>();
		for (ReportTemp r : reportList) {
			reportMap.put(r.ID, r);
		}
		PromptMsgMgr.reportMap=reportMap;
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
//		case PD.C_MOVE2BIAOCHE_REQ:
//			move2BiaoChe(id, builder, session);
//			break;挪到押镖mgr
		case PD.Prompt_Action_Req:
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if (jz == null) {
				log.error("Prompt_Action_Req:协议请求出错, 君主无法从session获取");
				break;
			}
			PromptActionReq.Builder req = (PromptActionReq.Builder)builder;
			int allActionType = req.getReqType();
			long suBaoId = req.getSuBaoId();
			PromptActionResp.Builder resp = PromptActionResp.newBuilder();
			switch(allActionType){
				case SuBaoConstant.ignore:
					ignoreSuBao(suBaoId, session, resp);
					break;
				case SuBaoConstant.bless:
					blessSomeOne(suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.comfort:
					comfort(suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.getAward:
					gainJiangli2SuBao( suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.go: //前往
					YaBiaoHuoDongMgr.inst.move2BiaoChe4KB(suBaoId, jz, session);
					break;
			}
			// 跳出外层Switch
			break;
		default:
			log.error("PromptMsgMgr-未处理的消息{}", id);
			break;
		}

	}
	
	/**
	 * @Description 根据条件保存盟友速报 
	 * @param jzId
	 * @param otherJzId
	 * @param otherJzName
	 * @param function
	 * @param eventId
	 * @param horseType
	 * @param startTime
	 * @return
	 */
	public PromptMSG saveLMKBByCondition(long jzId,long otherJzId, String[] param,
			int eventId,int condition) {
		ReportTemp rt = getReportTempByCondition(eventId, condition);
		if(rt==null){
			log.error("保存{}的联盟速报失败,未找到ReportTemp配置,eventId=={}, horseType=={}",jzId, eventId, condition);
			return null;
		}
		PromptMSG msg = saveLMSBByConfig(jzId, otherJzId, rt, param, condition);
		return msg;
	}
//	/**
//	 * @Description 根据条件保存盟友速报 
//	 * @param jz
//	 * @param otherJz
//	 * @param function
//	 * @param eventId
//	 * @param conditionType 0：没有条件限制 1：镖马的品质
//	 * @param horseType
//	 * @param startTime
//	 * @param receiveObject 1：自己2：盟友3：非盟友（和自己不在一个联盟的玩家）4：加入协助的盟友5：未加入协助的盟友
//	 * @return
//	 */
//	public PromptMSG saveLMKBByCondition(JunZhu jz,JunZhu otherJz, String[] param, 
//			int function,int eventId,int horseType,long startTime) {
//		long jzId=jz.id;
//		long otherJzId=otherJz.id;
//		String	otherJzName=otherJz.name;
//		ReportTemp rt=getReportTempByCondition(eventId, horseType);
//		if(rt==null){
//			log.error("保存{}的联盟速报失败,未找到ReportTemp配置,function=={},eventId=={}, horseType=={}",jzId,function, eventId, horseType);
//			return null;
//		}
//		
//		PromptMSG msg=saveLMSBByConfig(jzId, otherJzId, rt, param, -1);
//		return msg;
//	}
//	
	public String getContent(int languageId, String jzName, String otherName){
		DescId desc = ActivityMgr.descMap.get(languageId);
		String content = "";
		if(desc == null){
			log.error(" 速报错误,未找到DescId配置,descId=={}", languageId);
			content = "未找到描述配置，不知道说啥了,languageId :"+ languageId;
			return content;
		}
		content = desc.getDescription().replace("***", jzName).replace("###", otherName);
		return content;
	}
	/**
	 * @Description 根据配置保存盟友速报 
	 * @param jzId
	 * @param otherJzId
	 * @param otherJzName
	 * @param eventId
	 * @param startTime
	 * @param award
	 * @param content
	 * @return
	 */
	public PromptMSG saveLMSBByConfig(long jzId, long otherJzId, 
			ReportTemp rt,String[] param, int condition) {
//		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class,
//				"where otherJzId='"+otherJzId+"' and jzId='"
//				+jzId+"'and eventId='"+rt.event+"'");
//			Date now=new Date();
//			if(promptMsgList.size()>0){
//				log.error("联盟成员--{}保存 来自盟友--{}的押镖速报失败，盟友速报已存在",jzId,otherJzId);
//				return null;
//			}else{
				PromptMSG msg = new PromptMSG();
				msg.jzId=jzId;
				msg.otherJzId=otherJzId;
				msg.addTime= new Date();
				msg.jzName="";
				msg.otherJName ="";
				if(param != null && param.length == 1){
					msg.jzName = param[0];
				}else if(param != null && param.length == 2){
					msg.otherJName = param[1];
				}
				msg.eventId=rt.event;
				msg.configId=rt.ID;
				msg.content= getContent(rt.languageID, msg.jzName, msg.otherJName);
//				msg.startTime=startTime;
				msg.realCondition = condition;
				String award=rt.clickAward;
				if(param.length>2){
					if(param[2]!=null&&!"".equals(param[2]) ){
						award=param[2];
					}
				}
				msg.award=award;
				HibernateUtil.save(msg);
				return msg;
//			}
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
	public void ignoreSuBao(long subaoId,  IoSession session, PromptActionResp.Builder resp)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		session.write(resp.build());
	}

	public void blessSomeOne(long subaoId,  IoSession session,  PromptActionResp.Builder resp,
			JunZhu jz)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			log.error("{}祝福速报--{}不存在",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		log.info("{}祝福，速报--{} 开始",jz.id,subaoId);
		//祝福别人领取奖励
		String award=msg.award;
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			AwardMgr.inst.giveReward(session, award, jz);
		}
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setFujian(award);
		session.write(resp.build());
		log.info("{}祝福速报--{} 结束",jz.id,subaoId);
	}
	
	
	
	
	public void comfort(long subaoId, 
			IoSession session, 
			PromptActionResp.Builder resp,
			JunZhu jz)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			log.error("{}安慰，速报--{}不存在",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		//安慰别人 自己领取奖励 安慰别人没有奖
		String award = msg.award;
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			AwardMgr.inst.giveReward(session, award, jz);
		}

		switch(msg.eventId){
	
//			case SuBaoConstant.mccf:
//				saveLMKB4ZhuFu(msg.otherJzId,jz.id,jzName,msg.realCondition);
//				saveLMKBByCondition(msg.otherJzId, jz.id, new String[]{jz.name},
//						 msg.eventId, msg.realCondition);
//				break;
			case SuBaoConstant.ybsb:
				//TODO 修改奖励按照公示计算
				saveLMKBByCondition(msg.otherJzId, jz.id, new String[]{jz.name},
						 msg.eventId, msg.realCondition);
				break;
			case SuBaoConstant.been_lveDuo_event: //被掠夺 产生一个安慰速报
				saveLMKBByCondition(jz.id, msg.jzId, new String[]{jz.name, msg.jzName},
						SuBaoConstant.lveDuo_comfort_event, -1);
				break;
			case SuBaoConstant.qiuaw4yb:
				saveLMKBByCondition(jz.id, msg.jzId, new String[]{jz.name, msg.jzName},
						SuBaoConstant.sbaw4yb, -1);
				
			default:
					
		}

		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setFujian(award);
		session.write(resp.build());
		log.info("{}领取速报的奖励，速报--{} 结束",jz.id,subaoId);
	}
	
	/**
	 * @Description 领取速报的奖励
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void gainJiangli2SuBao(long subaoId, 
			IoSession session, 
			PromptActionResp.Builder resp,
			JunZhu jz)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			log.error("{}领取速报的奖励失败，速报--{}不存在",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		log.info("{}领取速报的奖励，速报--{} 开始",jz.id,subaoId);
		//领取奖励
		String award=msg.award;
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			AwardMgr.inst.giveReward(session, award, jz);
		}
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setFujian(award);
		session.write(resp.build());
		log.info("{}领取速报的奖励，速报--{} 结束",jz.id,subaoId);
	}
//	
//	/**
//	 * @Description 联盟成员-jzId 保存来自盟友-otherJzId的安慰联盟速报
//	 * @param ybjzID 被安慰君主Id
//	 * @param jzId 安慰人的君主Id
//	 * @param jzName 安慰人的君主Name
//	 * @param startTime
//	 */
//	public void saveLMKB4AnWei(long ybjzID,long jzId,String jzName,int horseType,
//			int function, int eventId) {
//		long startTime=System.currentTimeMillis();
//		log.info("联盟成员--{} 保存来自盟友{}的安慰联盟速报",ybjzID,jzId);
//		
//		saveLMKBByCondition(ybjzID, jzId, jzName, function, eventId, horseType, startTime);
//	}

//	public void saveLMKB4AnWei(JunZhu jz, PromptMSG msg) {
//		long startTime = System.currentTimeMillis();
//		saveLMKBByCondition(msg.otherJzId, jz.id,
//				 msg.eventId, msg.realCondition, startTime);
//	}
	
	
//	/**
//	 * @Description 联盟成员--ybjzId 保存来自盟友--jzId 的祝福联盟速报
//	 * @param ybjzId
//	 * @param jzId
//	 * @param jzName
//	 */
//	public void saveLMKB4ZhuFu(long ybjzId,long jzId,String jzName,int horseType) {
//		// TODO startTime eventId
//		log.info("联盟成员--{} 保存来自盟友{}的祝福联盟速报",ybjzId,jzId);
//		int eventId= SuBaoConstant.myzf;
//		saveLMKBByCondition(ybjzId, jzId, new String[]{"", jzName}, eventId, horseType);
//	}
	

	public ReportTemp getReportTempByCondition(int eventId, int sendCondition) {
		ReportTemp rt=null;
		for (Map.Entry<Integer, ReportTemp> entry : reportMap.entrySet()) {
			ReportTemp r = entry.getValue();
			if(r==null){
				continue;
			}
			if (eventId == r.event){
				if(r.sendCondition == -1){
					rt=r;
					break;
				}
				if(r.sendCondition == sendCondition){
					rt = r;
					break;
				}
			}
		}
		return rt;
	}
//	/**
//	 * @Description 根据eventId, horseType得到发送配置
//	 * @param function
//	 * @param eventId
//	 * @param horseType
//	 * @return
//	 */
//	public ReportTemp getReportTemp(int function,int eventId,int sendCondition) {
//		ReportTemp rt=null;
//		for (Map.Entry<Integer, ReportTemp> entry : reportMap.entrySet()) {
//			ReportTemp r = entry.getValue();
//			if(r==null){
//				continue;
//			}
//			if (function==r.function&&eventId==r.event) {
//				if(0==r.conditionType){//&&receiveObject==r.receiveObject){
//					rt=r;
//					break;
//				}
//				if(1==r.conditionType&&
//						(sendCondition==r.sendCondition||0==r.sendCondition)){
//					//&&receiveObject==r.receiveObject){
//					rt=r;
//					break;
//				}
//				
//			}
//		}
//		return rt;
//	}
	
	
	/**
	 * @Description 给出自己外所以盟友发快报
	 * @param chufaJz
	 * @param lmId
	 * @param horseType
	 * @param eventId
	 * @param startTime
	 * @return
	 */
	public boolean pushKB2ALLMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,long startTime) {
		long chufaJzId=chufaJz.id;
		log.info("向联盟---{}的所有成员推送盟友速报开始",lmId);
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的所有成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
		//TODO 算配置 判断要不要发
		int function=310;
		ReportTemp rt=getReportTempByCondition(eventId, horseType);
		if(rt==null){
			log.error("向联盟---{}的所有成员推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId,  horseType,2);
			return false;
		}
		String chufaName=chufaJz.name;
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			if(jzId==chufaJzId){
				
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.error("向联盟---{}的所有成员推送盟友速报失败，未找到押镖君主--{}", lmId,jzId);
				continue;
			}
			PromptMSG msg=saveLMSBByConfig(jzId, chufaJzId, rt, new String[]{chufaName}, horseType);
			if(msg==null){
				log.error("向联盟---{}的所有成员推送盟友速报失败,保存速报失败", lmId,jzId);
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
				// 联盟成员的君主id
				SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
				subao=makeSuBaoMSG(subao, msg);
				su.session.write(subao.build());
			}
		}
		log.info("向联盟---{}的所有成员推送押镖盟友速报结束",lmId);
		return true;
	}

	/**
	 * @Description 向协助联盟的成员推送盟友速报
	 * @param chufaJzId
	 * @param lmBean
	 * @param horseType
	 * @param eventId
	 */
	public boolean pushKB2XieZhuMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,long startTime) {
		log.info("向联盟---{}的协助押镖成员推送盟友速报开始",lmId);
		long chufaJzId=chufaJz.id;
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的所有成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
		//TODO 算配置 判断要不要发
		int function=310;
		ReportTemp rt=getReportTempByCondition( eventId, horseType);
		if(rt==null){
			log.error("向联盟---{}的协助押镖成员 推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId, horseType,4);
			return false;
		}

		HashSet<Long> xzSet = YaBiaoHuoDongMgr.inst.xieZhuCache4YBJZ.get(chufaJzId);
		if (xzSet != null) {
			for (Long xzJzId : xzSet) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
				if(jz==null){
					log.error("向联盟---{}的协助押镖成员推送盟友速报失败，未找到押镖君主--{}", lmId,xzJzId);
					continue;
				}
				PromptMSG msg=saveLMSBByConfig(xzJzId, chufaJzId, rt, null, horseType);
				if(msg==null){
					log.error("向联盟---{}的所有成员推送盟友速报失败,保存速报失败", lmId,xzJzId);
					continue;
				}
				SessionUser su = SessionManager.inst.findByJunZhuId(xzJzId);
				if (su != null){
					// 联盟成员的君主id
					SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
					subao =makeSuBaoMSG(subao, msg);
					su.session.write(subao.build());
				}
			}
		}
		log.info("向联盟---{}的协助押镖成员推送押镖盟友速报结束",lmId);
		return true;
	}
	/**
	 * @Description 向未协助联盟的成员推送盟友速报
	 * @param chufaJzId
	 * @param lmBean
	 * @param horseType
	 * @param eventId
	 */
	public boolean pushKB2WeiXieZhuMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,long startTime) {
		log.info("向联盟---{}的未协助押镖成员推送盟友速报开始",lmId);
		long chufaJzId=chufaJz.id;
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的未协助押镖成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
		//TODO 算配置 判断要不要发
		int function=310;
		ReportTemp rt=getReportTempByCondition( eventId, horseType);
		if(rt==null){
			log.error("向联盟---{}的未协助押镖成员 推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId, horseType,5);
			return false;
		}
		HashSet<Long> xzSet = YaBiaoHuoDongMgr.inst.xieZhuCache4YBJZ.get(chufaJzId);
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			//押镖君主自己不发
			if(jzId==chufaJzId){
				continue;
			}
			//协助的盟友不发
			if(xzSet!=null&&xzSet.contains(jzId)){
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz==null){
				log.error("向联盟---{}的未协助押镖成员推送盟友速报失败，未找到押镖君主--{}", lmId,jzId);
				continue;
			}
			PromptMSG msg=saveLMSBByConfig(jzId, chufaJzId, rt, null, horseType);
			if(msg==null){
				log.error("向联盟---{}的未协助押镖成员推送盟友速报失败,保存速报失败", lmId,jzId);
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
				// 联盟成员的君主id
				SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
				subao = makeSuBaoMSG(subao, msg);
				su.session.write(subao.build());
			}
		}
		log.info("向联盟---{}的未协助押镖成员推送押镖盟友速报结束",lmId);
		return true;
	}

	/**
	 * @Description 推送镖车被打给自己
	 * @param jzId
	 * @param lmBean
	 * @param horseType
	 * @param eventId
	 */
	public void pushLMKB2Self(JunZhu jz, 
			Integer horseType, Integer eventId,long  startTime) {
		Long jzId=jz.id;
		Long otherJzId=jz.id;
		log.info("推送联盟快报给自己--{}开始",jzId);
		//TODO 算配置 判断要不要发
		int function=310;
		ReportTemp rt= getReportTempByCondition( eventId, horseType);
		if(rt==null){
			log.error("推送联盟快报给自己失败，押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={}",
					jzId,function, eventId, horseType);
			return;
		}
		PromptMSG msg=saveLMSBByConfig(jzId, otherJzId, rt, null, horseType);
		if(msg==null){
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (su != null){
			// 联盟成员的君主id
			SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
			subao = makeSuBaoMSG(subao, msg);
			su.session.write(subao.build());
		}
		log.info("推送联盟快报给自己--{}结束",jzId);
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
			subao = makeSuBaoMSG(subao, msg);
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
	}
	
	/**
	 * @Description 推送快报
	 * @param chufaJz
	 * @param horseType
	 * @param eventId
	 * @param startTime
	 */
	public void pushLMKB2All(JunZhu chufaJz,int horseType,int eventId,long startTime) {
		long chufaJzId=chufaJz.id;
		AllianceBean lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
		if (lmBean != null) {
			boolean ret1=false;
			boolean ret2=false;
			boolean ret3=false;
			ret1= pushKB2ALLMengYou(chufaJz, lmBean.id, horseType, eventId, startTime);
			if(!ret1){
				ret2= pushKB2XieZhuMengYou(chufaJz, lmBean.id, horseType, eventId, startTime);
			}
			if(!ret2){
				ret3=pushKB2WeiXieZhuMengYou(chufaJz, lmBean.id, horseType, eventId, startTime);
			}
			log.info("镖车出发事件处理结果ret1=={},ret2=={},ret3=={}",ret1,ret2,ret3);
		}
		pushLMKB2Self(chufaJz, horseType, eventId, startTime);
	}
	@Override
	public void proc(Event e) {
		//镖车被打更新SOS列表
		Object[] oa = null;
		Long chufaJzId = null;
		Integer horseType = null;
		Integer eventId = null;
		Long startTime=null;
		AllianceBean lmBean=null;
		JunZhu chufaJz=null;
		switch (e.id) {
		case ED.BIAOCHE_CHUFA://镖车出发
			//TODO 镖车被打更新SOS列表
			oa = (Object[]) e.param;
			chufaJz = (JunZhu) oa[0];
			horseType = (Integer) oa[1];
			startTime=(Long) oa[2];
			if(horseType==null||startTime==null||chufaJz==null){
				log.error("镖车出发事件处理失败,horseType=={}||startTime=={}||chufaJz=={}",
						horseType,startTime,chufaJz);
				return;
			}
			
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJz.id);
			if (lmBean != null) {
				pushKB2WeiXieZhuMengYou(chufaJz, lmBean.id, horseType, SuBaoConstant.mccf_toOther, startTime);
			}
			
			pushLMKB2Self(chufaJz, horseType, SuBaoConstant.mccf_toSelf, startTime);
			break;
		case ED.BIAOCHE_BEIDA://镖车被攻击
			//TODO 镖车被打更新SOS列表
			oa = (Object[]) e.param;
			chufaJz=(JunZhu)oa[0];
			horseType = (Integer) oa[1];
			startTime=(Long) oa[2];
			if(horseType==null||startTime==null||chufaJz==null){
				log.error("镖车被攻击事件处理失败,horseType=={}||startTime=={}||chufaJz=={}",
						horseType,startTime,chufaJz);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJz.id);
			if (lmBean != null) {
				pushKB2ALLMengYou(chufaJz, lmBean.id, horseType, SuBaoConstant.mcbd_toOther, startTime);
			}
			
			pushLMKB2Self(chufaJz, horseType, SuBaoConstant.mcbd_toSelf, startTime);
			break;
		case ED.BIAOCHE_END://押镖镖车从场景中移除
			oa = (Object[]) e.param;
			chufaJzId = (Long) oa[0];
			horseType = (Integer) oa[1];
			startTime=(Long) oa[2];
			if(chufaJzId==null||horseType==null){
				log.error("chufaJzId=={}||horseType=={}",chufaJzId,horseType);
				return;
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
			if (lmBean != null) {
				deleteLMSB2AllMengYou(lmBean.id, chufaJzId, startTime);
			}else{
				deleteLMSB(chufaJzId, chufaJzId, startTime);
			}
			break;
		case ED.Lve_duo_fail: //掠夺失败
		case ED.been_lve_duo: // 被掠夺
			oa = (Object[]) e.param;
			if(oa[0] == null || oa[1] == null ||oa[2] == null  ){
				log.error("玩家掠夺失败，发送通报消息失败");
				return;
			}
			long selfJunId = (Long) oa[0];
			String selfJunZhuName = (String) oa[1];
			String enemyJunZhuName = (String) oa[2];
			if(e.id == ED.Lve_duo_fail){
				eventId = SuBaoConstant.lveDuo_fail_event;
			}else{
				eventId = SuBaoConstant.been_lveDuo_event;
			
			}
			lmBean = AllianceMgr.inst.getAllianceByJunZid(selfJunId);
			if (lmBean != null) {
				boolean yes = tell(selfJunId, lmBean.id,
						eventId,  new String[]{selfJunZhuName, enemyJunZhuName});
				if(yes){
					log.info("玩家:{}掠夺失败，向自己所在的联盟的所有联盟成员发送报消息成功", selfJunId);
				}
			}
			break;
		case ED.BIAOCHE_CUIHUI://押镖镖车摧毁求安慰
			oa = (Object[]) e.param;
			chufaJz=(JunZhu)oa[0];
			horseType = (Integer) oa[1];
			startTime=(Long) oa[2];
			eventId=SuBaoConstant.qiuaw4yb;
			if(chufaJz==null||horseType==null){
				log.error("chufaJz=={}||horseType=={}",chufaJz,horseType);
				return;
			}
			pushLMKB2All(chufaJz, horseType, eventId, startTime);
			break;
		default:
			break;
		}
	}

	public boolean tell(long juid, int lmId, int reportTemp_event, String[] param ) {
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList == null || aplayersList.size() == 0){
			log.error("向联盟---{}的所有成员 推送速报失败，联盟无成员", lmId);
			return false;
		}
		ReportTemp rt = getReportTempByCondition(reportTemp_event, -1);
		if(rt == null){
			log.error("向联盟---{}的所有成员推送 推送速报速报失败, 无配置functionId:{},ReportTemp_event：{}，",
					reportTemp_event);
			return false;
		}
	
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId = aplayer.junzhuId;
			if(jzId == juid){
				continue;
			}
			JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
			if(jz == null){
				log.error("向联盟---{}的所有成员推送速报失败，未找到联盟中的--{}", lmId,jzId);
				continue;
			}
			PromptMSG msg = saveLMSBByConfig(jzId, juid, rt, param, -1);
			if(msg == null){
				log.error("向联盟---{}的所有成员推送速报失败,保存速报失败", lmId,jzId);
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
				// 联盟成员的君主id
				SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
				subao = makeSuBaoMSG(subao, msg);
				su.session.write(subao.build());
			}
		}
		log.info("向联盟---{}的所有成员推送 速报结束",lmId);
		return true;
	}
	
	public SuBaoMSG.Builder makeSuBaoMSG(SuBaoMSG.Builder subao,PromptMSG msg) {
		subao.setSubaoId(msg.id);
		subao.setConfigId(msg.configId);
		subao.setOtherJzId(msg.otherJzId);
		subao.setSubao(msg.content);
		subao.setEventId(msg.eventId);
		return subao;
	}
	
	public boolean deleteLMSB2AllMengYou(int lmId, Long chufaJzId, Long startTime) {
		log.info("清理联盟---{}的所有成员的联盟速报开始",lmId);
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where otherJzId='"+chufaJzId+"' and eventId in(1,2,3)"
				+ "and startTime="+startTime+"");
		for (PromptMSG msg : promptMsgList) {
			if (msg!=null) {
				log.info("删除联盟---{}的 成员--{}的联盟速报Id--{}",lmId,msg.jzId,msg.id);
				HibernateUtil.delete(msg);
			}
		}
		log.info("清理联盟---{}的所有成员的联盟速报结束",lmId);
		return true;
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.BIAOCHE_CHUFA, this);
		EventMgr.regist(ED.BIAOCHE_BEIDA, this);
		EventMgr.regist(ED.BIAOCHE_CUIHUI, this);
		EventMgr.regist(ED.BIAOCHE_END, this);
		EventMgr.regist(ED.Lve_duo_fail, this); // 掠夺失败
		EventMgr.regist(ED.been_lve_duo, this); // 被别人掠夺的
	}
}