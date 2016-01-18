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
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.ReportTemp;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;
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
	public static int tongbiCODE = AwardMgr.ITEM_TONGBI_ID;
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
				case SuBaoConstant.iKnow:
 					ignoreSuBao(suBaoId, session, resp);
					break;
				case SuBaoConstant.bless:
					blessSomeOne(suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.comfort:
					comfort(suBaoId, session, resp, jz);
					break;
				case 0:
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
	 * @Description  根据条件保存盟友速报 
	 * @param jzId
	 * @param otherJzId
	 * @param param 
	 * 参数次序  
	 * 	     参数1： 君主名字1；
	 *    参数2： 君主名字2； 
	 *    参数3：铜币收入 ；
	 *    参数4： 运镖者马车的价值存储 
	 *    参数5：拼接安慰奖励的原马车价值；
	 *    参数6：拼接杀死仇人的奖励的仇人等级；
	 * @param eventId
	 * @param condition
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
	public String getContent(int languageId, String jzName1,String jzName2,String jiangli){
		System.out.println(11);
		DescId desc = ActivityMgr.descMap.get(languageId);
		String content = "";
		if(desc == null){
			log.error(" 速报错误,未找到DescId配置,descId=={}", languageId);
			content = "未找到描述配置，不知道说啥了,languageId :"+ languageId;
			return content;
		}
		content = desc.getDescription();
		if(!"".equals(jzName1)){
			content = content.replace("XX", jzName1);
		}
		if(!"".equals(jzName2)){
			content = content.replace("YY", jzName2);
		}
		if(jiangli!=null&&!"".equals(jiangli)){
			content = content.replace("$$", jiangli);
		}
		return content;
	}
	/**
	 * @Description  根据配置保存盟友速报 
	 * @param jzId
	 * @param otherJzId
	 * @param rt
	 * @param param
	 * 	   参数次序  
	 * 	     参数1： 君主名字1；
	 *    参数2： 君主名字2； 
	 *    参数3：本条快报本身带来的收益（铜币收入） ；
	 *    参数4： 本条快报引起的下条快报会有的收益,作为存储 （运镖者马车的价值存储） 
	 *    
	 *    ------下面参数为个例-----
	 *    参数5：拼接安慰奖励的原马车价值；
	 *    参数6：拼接杀死仇人的奖励的仇人等级；
	 * @param condition
	 * @return
	 */
	public PromptMSG saveLMSBByConfig(long jzId, long otherJzId, 
			ReportTemp rt,String[] param, int condition) {
		PromptMSG msg =null;
		List<PromptMSG> msgList=HibernateUtil.list(PromptMSG.class, "where otherJzId='"+otherJzId+"' and jzId='"
				+jzId+"' and eventId = "+rt.event+")");
		int lsize=msgList.size();
		if(lsize>0){
			log.info("jzId={} ,otherJzId={} , eventId ={}的快报已存在了{}条，不保存了",jzId,otherJzId,rt.event,lsize);
			return msgList.get(0);
		}
		msg = new PromptMSG();
		msg.jzId=jzId;
		msg.otherJzId=otherJzId;
		msg.addTime= new Date();
		msg.eventId=rt.event;
		msg.configId=rt.ID;
		msg.realCondition = condition;
		msg.jzName1="";
		msg.jzName2 ="";
		msg.cartWorth="";
		String jiangli="";
		String award=rt.clickAward;
		if(param != null){
			if(param.length >= 1){
				if(param[0]!=null&&!"".equals(param[0])){
					msg.jzName1 = param[0];
				}
			}
			if(param.length >= 2){
				if(param[1]!=null&&!"".equals(param[1])){
					msg.jzName2 = param[1];
				}
			}
			//第三个参数  表示劫镖或者押镖收入 运镖君主马车价值
			if(param.length>=3){
				if(param[2]!=null&&!"".equals(param[2]) ){
					String shouru =param[2];
					int ybWorth=Integer.valueOf(shouru);
					int myAward=(int) (ybWorth);
					// 掠夺安慰奖励
					if(msg.eventId ==  SuBaoConstant.lveDuo_comfort_event){
						award = "0:"+AwardMgr.item_gong_jin+":"+myAward;
					}else{
						award="0:"+tongbiCODE+":"+myAward;
					}
					jiangli=shouru;
				}
			}
			//第四个参数表示运镖君主马车价值 参数用来拼出 安慰奖励
			//保存运镖君主马车100%的价值 
			if(param.length==4){
				if(param[3]!=null&&!"".equals(param[3]) ){
					msg.cartWorth=param[3];
				}
			}
			//第五个参数表示 帮贡奖励 暂时没用到
			//2015年12月16日 根据策划需求 根据运镖君主的马车价值算出他被安慰一次得到的奖励
			if(param.length==5){
				if(param[4]!=null&&!"".equals(param[4]) ){
					String cartWorth =param[4];
					int ybWorth=Integer.valueOf(cartWorth);
					int anweiAward=(int) (ybWorth*YunbiaoTemp.yunbiao_comforted_award_k+YunbiaoTemp.yunbiao_comforted_award_b);
					award="0:"+tongbiCODE+":"+anweiAward;
					jiangli=anweiAward+"";
				}
			}
			if(param.length==6){
				if(param[5]!=null&&!"".equals(param[5]) ){
					String enemyLevel =param[5];
					int level =Integer.valueOf(enemyLevel);
					int moneyXishu=1000;
					JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(level);
					if(ss!=null){
						moneyXishu=ss.moneyXishu;
					}else{
						log.error("保存杀死仇人快报出错,未找到JunzhuShengji配置，jzlevel-{}",level);
					}
					int killEnemyAward=(int) (moneyXishu*YunbiaoTemp.killFoeAward_k+YunbiaoTemp.killFoeAward_b);
					award="0:"+tongbiCODE+":"+killEnemyAward;
					jiangli=killEnemyAward+"";
				}
			}
		}
	
		msg.award=award;
		msg.content= getContent(rt.DescID, msg.jzName1,msg.jzName2,jiangli);
		HibernateUtil.save(msg);
		return msg;
	}
	//删除盟友速报 
	public void deleteLMSB(long jzId,long ybJzID) {
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where otherJzId='"+ybJzID+"' and jzId='"
				+jzId+"' and eventId in ("+SuBaoConstant.mccf_toSelf+","+SuBaoConstant.mcbd_toSelf+")");
		for (PromptMSG msg : promptMsgList) {
			if (msg!=null) {
				log.info("删除君主{}的联盟速报Id--{}",msg.jzId,msg.id);
				HibernateUtil.delete(msg);
			}
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
			resp.setSubaoType(SuBaoConstant.ignore);
			session.write(resp.build());
			return;
		}
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setSubaoType(SuBaoConstant.ignore);
		session.write(resp.build());
	}

	public void blessSomeOne(long subaoId,  IoSession session,  PromptActionResp.Builder resp,
			JunZhu jz)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			log.error("{}祝福速报--{}不存在",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			resp.setSubaoType(SuBaoConstant.bless);
			session.write(resp.build());
			return;
		}
		log.info("{}祝福，速报--{} 开始",jz.id,subaoId);
		//祝福别人领取奖励
		String award=msg.award;
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			AwardMgr.inst.giveReward(session, award, jz);
		}
		//生成祝福快报
		PromptMSG msg2= saveLMKBByCondition(msg.otherJzId,jz.id, new String[]{jz.name },SuBaoConstant.myzf, msg.realCondition);
		SessionUser su = SessionManager.inst.findByJunZhuId(msg.otherJzId);
		if (su != null){
			SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
			subao = makeSuBaoMSG(subao, msg2);
			su.session.write(subao.build());
		}
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setSubaoType(SuBaoConstant.bless);
		award=award==null?"":award;
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
			resp.setSubaoType(SuBaoConstant.comfort);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		
		/*
		 * 安慰别人 自己领取奖励 
		 */
		
		PromptInfo pin = HibernateUtil.find(PromptInfo.class, jz.id);
		if(pin == null){
			pin = new PromptInfo();
			pin.jId = jz.id;
		}
		boolean mustGet = false;
		switch(msg.eventId){
			case SuBaoConstant.been_lveDuo_event: //被掠夺 产生一个安慰速报
				if(pin.ldComfortCount >= CanShu.LUEDUO_AWARDEDCOMFORT_MAXTIMES){
					mustGet = true;
				}
				pin.ldComfortCount ++;
				HibernateUtil.save(pin);
				break;
			case SuBaoConstant.qiuaw4yb:
				if(pin.ybComfortCount >= CanShu.YUNBIAO_AWARDEDCOMFORT_MAXTIMES){
					mustGet = true;
				}
				pin.ybComfortCount++;
				HibernateUtil.save(pin);
				break;
			default: log.error("未知的安慰类型：{}" , msg.eventId); break;
		}
		if(mustGet){
			String award = msg.award;
			if(award!=null&&!"".equals(award)&&award.contains(":")){
				AwardMgr.inst.giveReward(session, award, jz);
				log.info("玩家：{}因为事件：{}，安慰了玩家：{}，掠夺安慰次数：{}， 押镖安慰次数：{}"
						+ "获得了奖励:{}" ,
						jz.id, msg.eventId, msg.jzId, pin.ldComfortCount, pin.ybComfortCount, award);
			}
		}

		/*
		 * 产生新的速报
		 */
		PromptMSG msg2 = null;
		switch(msg.eventId){
	
			case SuBaoConstant.been_lveDuo_event: //被掠夺 产生一个安慰速报
				 msg2 = saveLMKBByCondition(msg.otherJzId, msg.jzId, new String[]{msg.jzName2, jz.name, msg.cartWorth},
						SuBaoConstant.lveDuo_comfort_event, -1);
				
				break;
			case SuBaoConstant.qiuaw4yb:
				 msg2 =saveLMKBByCondition(msg.otherJzId, msg.jzId, new String[]{jz.name ,"",null,null,msg.cartWorth},
						SuBaoConstant.sbaw4yb, msg.realCondition);
			default:
				break;
					
		}

		SessionUser su = SessionManager.inst.findByJunZhuId(msg.otherJzId);
		if (su != null && msg2 != null){
			SuBaoMSG.Builder subao2=SuBaoMSG.newBuilder();
			makeSuBaoMSG(subao2, msg2);;
			su.session.write(subao2.build());
		}
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setSubaoType(SuBaoConstant.comfort);
		resp.setFujian(msg.award==null?"":msg.award);
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
			resp.setSubaoType(SuBaoConstant.getAward);
			session.write(resp.build());
			return;
		}
		//领取奖励
		String award=msg.award;
		log.info("{}领取速报的奖励，速报--{} 奖励--{}",jz.id,subaoId,award);
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			AwardMgr.inst.giveReward(session, award, jz);
		}
		//删除速报
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setSubaoType(SuBaoConstant.getAward);
		award=award==null?"":award;
		resp.setFujian(award);
		session.write(resp.build());
		log.info("{}领取速报的奖励，速报--{} 结束",jz.id,subaoId);
	}
	

	public ReportTemp getReportTempByCondition(int eventId, int sendCondition) {
		ReportTemp rt=null;
		for (Map.Entry<Integer, ReportTemp> entry : reportMap.entrySet()) {
			ReportTemp r = entry.getValue();
			if(r==null){
				continue;
			}
			if (eventId == r.event){
				if(r.sendCondition== -1){
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
	
	
	/**
	 * @Description 给出自己外所以盟友发快报
	 * @return
	 */
	public boolean pushKB2ALLMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,String cartWorth) {
		long chufaJzId=chufaJz.id;
		log.info("向联盟---{}的所有成员推送盟友速报开始",lmId);
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的所有成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
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
			PromptMSG msg=saveLMSBByConfig(jzId, chufaJzId, rt, new String[]{chufaName,"","",cartWorth}, horseType);
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
	 */
	public boolean pushKB2XieZhuMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,String cartWorth) {
		log.info("向联盟---{}的协助押镖成员推送盟友速报开始",lmId);
		long chufaJzId=chufaJz.id;
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的所有成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
		int function=310;
		ReportTemp rt=getReportTempByCondition( eventId, horseType);
		if(rt==null){
			log.error("向联盟---{}的协助押镖成员 推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId, horseType,4);
			return false;
		}

		HashSet<Long> xzSet = YaBiaoHuoDongMgr.xieZhuCache4YBJZ.get(chufaJzId);
		if (xzSet != null) {
			for (Long xzJzId : xzSet) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
				if(jz==null){
					log.error("向联盟---{}的协助押镖成员推送盟友速报失败，未找到押镖君主--{}", lmId,xzJzId);
					continue;
				}
				PromptMSG msg=saveLMSBByConfig(xzJzId, chufaJzId, rt, new String[]{chufaJz.name}, horseType);
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
	public boolean pushKB2WeiXieZhuMengYou(JunZhu chufaJz,int lmId,int horseType,int eventId,String cartWorth) {
		log.info("向联盟---{}的未协助押镖成员推送盟友速报开始",lmId);
		long chufaJzId=chufaJz.id;
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList==null||aplayersList.size()==0){
			log.error("向联盟---{}的未协助押镖成员推送盟友速报失败，联盟无成员", lmId);
			return false;
		}
		int function=310;
		ReportTemp rt=getReportTempByCondition( eventId, horseType);
		if(rt==null){
			log.error("向联盟---{}的未协助押镖成员 推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId, horseType,5);
			return false;
		}
		HashSet<Long> xzSet = YaBiaoHuoDongMgr.xieZhuCache4YBJZ.get(chufaJzId);
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
			PromptMSG msg=saveLMSBByConfig(jzId, chufaJzId, rt, new String[]{chufaJz.name}, horseType);
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
			Integer horseType, Integer eventId) {
		Long jzId=jz.id;
		Long otherJzId=jz.id;
		log.info("推送联盟快报给自己--{}开始",jzId);
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
		String Time_8_age=DateUtils.getNHourAgo(8);
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where  jzId="+jzId+"and (award<>'' or  addTime>='"+Time_8_age+"')");
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
	public void pushLMKB2All(JunZhu chufaJz,int horseType,int eventId,String cartWorth) {
		long chufaJzId=chufaJz.id;
		AllianceBean lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
		if (lmBean != null) {
			boolean ret1=false;
			boolean ret2=false;
			boolean ret3=false;
			ret1= pushKB2ALLMengYou(chufaJz, lmBean.id, horseType, eventId, cartWorth);
			if(!ret1){
				ret2= pushKB2XieZhuMengYou(chufaJz, lmBean.id, horseType, eventId, cartWorth);
			}
			if(!ret2){
				ret3=pushKB2WeiXieZhuMengYou(chufaJz, lmBean.id, horseType, eventId, cartWorth);
			}
			log.info("事件处理结果ret1=={},ret2=={},ret3=={}",ret1,ret2,ret3);
		}
		pushLMKB2Self(chufaJz, horseType, eventId);
	}
	@Override
	public void proc(Event e) {
		switch (e.id) {
		case ED.BIAOCHE_CHUFA://镖车出发
			handleCarStart(e);
			break;
		case ED.BIAOCHE_BEIDA://镖车被攻击
			//镖车被打更新SOS列表
			handleCarAttack(e);
			break;
		case ED.BIAOCHE_END://押镖镖车从场景中移除
			handleCarRemove(e);
			break;
		case ED.BIAOCHE_CUIHUI://押镖镖车摧毁求安慰 和通知自己
			handleCarDestroy(e);
			break;
		case ED.Lve_duo_fail: //掠夺失败
		case ED.been_lve_duo: // 被掠夺
			handleLveDuoAttack(e);
			break;
		default:
			break;
		}
	}
	public  void handleLveDuoAttack(Event e) {
		if(e == null || e.param == null){
			return;
		}
		if(! (e.param instanceof Object[])){
			return;
		}
		Object[] oa = (Object[]) e.param;
		if(oa.length <= 2){
			return;
		}
		if(oa[0] == null || oa[1] == null ||oa[2] == null){
			log.error("发送掠夺通报失败，参数有null，请程序查看");
			return;
		}
		long selfJunId = (Long) oa[0];
		String selfJunZhuName = (String) oa[1];
		String enemyJunZhuName = (String) oa[2];
		int jiangli = 0;
		int eventId=0;
		if(e.id == ED.Lve_duo_fail){
			eventId = SuBaoConstant.lveDuo_fail_event;
		}else{
			eventId = SuBaoConstant.been_lveDuo_event;
			if(oa.length == 4 && oa[3] != null){
				jiangli =(Integer) oa[3];
			}else{
				log.error("被掠夺快报有错，没有有效参数oa[3]，请程序查看");
			}
		}
		AllianceBean	lmBean = AllianceMgr.inst.getAllianceByJunZid(selfJunId);
		if (lmBean != null) {
			boolean yes = tell(selfJunId, lmBean.id,
					eventId,  new String[]{selfJunZhuName, enemyJunZhuName, "" , jiangli+""});
			if(yes){
				log.info("玩家:{}掠夺失败，向自己所在的联盟的所有联盟成员发送报消息成功", selfJunId);
			}
		}
	}
	public  void handleCarAttack(Event e) {
		Object[]	oa = (Object[]) e.param;
		JunZhu	ybjz=(JunZhu)oa[0];
		Integer	horseType = (Integer) oa[1];
		if(horseType==null||ybjz==null){
			log.error("镖车被攻击事件处理失败,horseType=={}||chufaJz=={}",
					horseType,ybjz);
			return;
		}
		AllianceBean	lmBean = AllianceMgr.inst.getAllianceByJunZid(ybjz.id);
		if (lmBean != null) {
			pushKB2ALLMengYou(ybjz, lmBean.id, horseType, SuBaoConstant.mcbd_toOther, "");
		}
		
		pushLMKB2Self(ybjz, horseType, SuBaoConstant.mcbd_toSelf);
	}
	public  void handleCarRemove(Event e) {
		Object[] oa = (Object[]) e.param;
		Long	chufaJzId = (Long) oa[0];
		Integer horseType = (Integer) oa[1];
		if(chufaJzId==null||horseType==null){
			log.error("chufaJzId=={}||horseType=={}",chufaJzId,horseType);
			return;
		}
		AllianceBean	lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
		if (lmBean != null) {
			deleteLMSB2AllMengYou(lmBean.id, chufaJzId);
		}else{
			deleteLMSB(chufaJzId, chufaJzId);
		}
	}
	public  void handleCarStart(Event e) {
		Object[]oa = (Object[]) e.param;
		JunZhu ybjz = (JunZhu) oa[0];
		Integer	horseType = (Integer) oa[1];
		AllianceBean	lmBean = AllianceMgr.inst.getAllianceByJunZid(ybjz.id);
		if (lmBean != null) {
			pushKB2WeiXieZhuMengYou(ybjz, lmBean.id, horseType, SuBaoConstant.mccf_toOther,"");
		}
		//2015年12月18日 去掉出发给自己的快报
		pushLMKB2Self(ybjz, horseType, SuBaoConstant.mccf_toSelf);
	}
	public  void handleCarDestroy(Event e) {
		Object[]	oa = (Object[]) e.param;
		JunZhu	ybjz=(JunZhu)oa[0];
		JunZhu	jbjz=(JunZhu)oa[1];
		Integer	horseType = (Integer) oa[2];
		String 	cartWorth=(String) oa[3];
		int	eventId=SuBaoConstant.qiuaw4yb;
		if(ybjz==null||jbjz==null||horseType==null||cartWorth==null){
			log.error("ybjz=={}||ybjz=={}||horseType=={}||cartWorth=={}",ybjz,jbjz,horseType,cartWorth);
			return;
		}
		long chufaJzId=ybjz.id;
		AllianceBean lmBean = AllianceMgr.inst.getAllianceByJunZid(chufaJzId);
		if (lmBean != null) {
			boolean ret1=false;
			ret1= pushKB2ALLMengYou(ybjz, lmBean.id, horseType, eventId, cartWorth);
			log.info("事件处理结果ret1=={}",ret1);
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
		if(msg.award!=null){
			subao.setAward(msg.award);
		}
		//TODO 删掉删掉！！！
		if(true){
			log.info("君主--{}的速报=={}，内容为---《{}》,配置id=={}，otherJzId=={},奖励=={}",msg.jzId,msg.id,msg.content,msg.configId,msg.otherJzId,msg.award);
		}
		return subao;
	}
	
	public boolean deleteLMSB2AllMengYou(int lmId, Long chufaJzId) {
		log.info("清理联盟---{}的所有成员的联盟速报开始",lmId);
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where otherJzId='"+chufaJzId+"' and eventId in("
				+SuBaoConstant.mccf_toOther+","	+SuBaoConstant.jionxz_toSelf+","	+SuBaoConstant.mcbd_toOther+","+SuBaoConstant.zdqz+")");
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