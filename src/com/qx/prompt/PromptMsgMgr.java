package com.qx.prompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Table;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.template.AnnounceTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.GuYongBing;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.template.ReportTemp;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.activity.ActivityMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.EquipMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.friends.GreetMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pve.PveMgr;
import com.qx.pvp.LveDuoBean;
import com.qx.pvp.LveDuoMgr;
import com.qx.pvp.PvpMgr;
import com.qx.task.DailyTaskMgr;
import com.qx.timeworker.FunctionID;
import com.qx.world.BroadcastMgr;
import com.qx.world.Mission;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;

import qxmobile.protobuf.Prompt.HistoryBattleInfo;
import qxmobile.protobuf.Prompt.JunQingReq;
import qxmobile.protobuf.Prompt.JunQingResp;
import qxmobile.protobuf.Prompt.PromptActionReq;
import qxmobile.protobuf.Prompt.PromptActionResp;
import qxmobile.protobuf.Prompt.PromptMSGResp;
import qxmobile.protobuf.Prompt.QuZhuReq;
import qxmobile.protobuf.Prompt.SuBaoMSG;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.QuZhuBattleEndReq;
import qxmobile.protobuf.ZhanDou.QuZhuBattleEndResp;
import qxmobile.protobuf.ZhanDou.ZhanDouInitError;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;
/**
 * @Description 通知管理
 *
 */
public class PromptMsgMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(PromptMsgMgr.class);
	public static PromptMsgMgr inst;
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static Mission exit = new Mission(0, null, null);
	public static Map<Integer, ReportTemp> reportMap;
	public static int tongbiCODE = AwardMgr.ITEM_TONGBI_ID;
	public static int gongxianCODE = 900015;
	public static Map<Integer, Long> fightingLock = new HashMap<Integer, Long>();
//	public static Map<Long, Long[]> prepareLock = new HashMap<Long, Long[]>();
//	public static Map<Integer, GuYongBing> bingMap = new HashMap<Integer, GuYongBing>();
	public AtomicInteger zhandouIdMgr = new AtomicInteger(1);
	public static String beanLveContent="";
	
	/**
	 * 三分钟删除过期通知的sql
	 */
	public 	static String sql2ThreeMinClear=null;
	public PromptMsgMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "PromptMsgMgr").start();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		List<ReportTemp> reportList = TempletService.listAll(ReportTemp.class.getSimpleName());
		 Map<Integer, ReportTemp> reportMap = new HashMap<Integer, ReportTemp>();
		for (ReportTemp r : reportList) {
			reportMap.put(r.ID, r);
		}
		PromptMsgMgr.reportMap=reportMap;
		
		DescId desc = ActivityMgr.descMap.get(6000001);
		if(desc != null){
			beanLveContent = desc.getDescription();
		}
		//TODO 带策划加入配置
		SuBaoConstant.clearShortDistance=8;
		SuBaoConstant.clearLongDistance = 48;
		String tableName="PromptMSG";
		Table annotation = (Table)PromptMSG.class.getAnnotation(Table.class);
		if(annotation != null){
			tableName= annotation.name();
		}
		sql2ThreeMinClear="delete from  "+tableName+" where eventId in ("
		+SuBaoConstant.mccf_toOther+","//系统自动发的邀请加入协助
		+SuBaoConstant.zdqz+","//主动求助
		+SuBaoConstant.askgh4lm2other+","//联盟开启对其他人求恭贺
		+SuBaoConstant.askgh4lm2leader+","//联盟开启对盟主之类的求恭贺
		+SuBaoConstant.askgh4baizhan+")  and  addTime<'";
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
		case PD.go_qu_zhu_req:
			goQuZhu(id, session, builder);
			break;
		case PD.qu_zhu_battle_end_req: //联盟军情之 掠夺驱逐玩法
			dealQuZhuBattleResult(id, session, builder);
			break;
		case PD.qu_zhu_req:
			quZhuInitData(id, session, builder);
			break;
		case PD.alliance_junQing_req: //联盟军情
			allianceMIReq(id, session, builder);
			break;
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
				case SuBaoConstant.lveDuo_go:
 					ignoreSuBao(suBaoId, session, resp, allActionType);
					break;
				case SuBaoConstant.bless:
					blessSomeOne(suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.comfort:
					comfort(suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.gonghe:
					GreetMgr.inst.refreshGongheTimes2JunZhu(suBaoId,jz);
				case 0:
				case SuBaoConstant.getAward:
					gainJiangli2SuBao( suBaoId, session, resp, jz);
					break;
				case SuBaoConstant.go: //前往
					YaBiaoHuoDongMgr.inst.move2BiaoChe4KB(suBaoId, jz, session);
					break;
				case SuBaoConstant.hello:
				case SuBaoConstant.gun:
					GreetMgr.inst.AnswerGreetAndAddFriend(suBaoId,allActionType, session);
					break;
				case SuBaoConstant.joinLM:
				case SuBaoConstant.refuseLM:
					GreetMgr.inst.Answer2InviteLM(suBaoId,allActionType, session);
					break;
				case SuBaoConstant.invite2Lm:
					GreetMgr.inst.Invite2LM4GongHe(jz,suBaoId,allActionType, session);
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
	 * 	     参数1： 君主名字1；速报的所属君主
	 *    参数2： 君主名字2； 
	 *    参数3：铜币收入 ； 2016年3月12日 策划加入 联盟贡献收入
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
	
	/**
	 * @Description 拼出通知的描述内容
	 */
	public String getContent(int languageId, String name1,String name2,String jiangli){
		DescId desc = ActivityMgr.descMap.get(languageId);
		String content = "";
		if(desc == null){
			log.error(" 速报错误,未找到DescId配置,descId=={}", languageId);
			content = "未找到描述配置，不知道说啥了,languageId :"+ languageId;
			return content;
		}
		content = desc.getDescription();
		if(!"".equals(name1)){
			content = content.replace("XX", name1);
		}
		if(!"".equals(name2)){
			content = content.replace("YY", name2);
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
	 * 	     参数1： 君主名字1； 速报的所属君主
	 *    参数2： 君主名字2； 
	 *    参数3：本条快报本身带来的收益（铜币收入） ； 2016年3月12日 策划加入 联盟贡献收入
	 *    参数4： 本条快报引起的下条快报会有的收益,作为存储 （运镖者马车的价值存储） 
	 *    
	 *    ------下面参数为个例-----
	 *    参数5：拼接安慰奖励的原马车价值；
	 *    参数6：拼接杀死仇人的奖励的仇人等级；
	 * @param condition  当保存邀请加入联盟通知时 保存着lmId
	 * @return
	 */
	public PromptMSG saveLMSBByConfig(long jzId, long otherJzId, 
			ReportTemp rt,String[] param, int condition) {
		PromptMSG msg =null;
		List<PromptMSG> msgList=HibernateUtil.list(PromptMSG.class, "where otherJzId='"+otherJzId+"' and jzId='"
				+jzId+"' and eventId = "+rt.event+"");
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
					String[] ybAward=shouru.split("#");
					int gongxian=Integer.valueOf(ybAward[1]); 
					award="0:"+tongbiCODE+":"+ybAward[0];
					if(gongxian>0){
						award+="#0:"+gongxianCODE +":"+ybAward[1];
					}
					jiangli=shouru;//2016年3月12日 现在没用 谁知道啥时候会加回来
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
						moneyXishu=ss.xishu;
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
	
	/**
	 * @Description 保存场景互动---邀请加入联盟的通知
	 */
	public PromptMSG savePromptMSG4ChangjingHudong(long jzId, long otherJzId, 
			int eventId,int lmId,	String[] param) {
		ReportTemp rt = getReportTempByCondition(eventId, -1);
		if(rt==null){
			log.error("保存{}接收 --{}场景互动的通知失败,未找到ReportTemp配置,eventId=={}, lmId=={}",jzId,otherJzId, eventId, lmId);
			return null;
		}
		PromptMSG msg =null;
		List<PromptMSG> msgList=HibernateUtil.list(PromptMSG.class, "where otherJzId='"+otherJzId+"' and jzId='"
				+jzId+"' and eventId = "+rt.event+"");
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
		msg.realCondition = lmId;
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
		}
		msg.award=award;
		msg.content= getContent(rt.DescID, msg.jzName1,msg.jzName2,jiangli);
		HibernateUtil.save(msg);
		return msg;
	}

	
	/**
	 * @Description 	删除盟友jzId对ybJzID的速报 
	 */
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
	 * @Description 清理君主 对应类型的速报
	 */
	public void deleteLMSBByEventId(long jzId,int eventId) {
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where  jzId='"
				+jzId+"' and eventId ="+eventId+"");
		for (PromptMSG msg : promptMsgList) {
			if (msg!=null) {
				log.info("删除君主{}的速报Id--{},类型--{},内容为---《{}》",msg.jzId,msg.id,eventId,msg.content);
				HibernateUtil.delete(msg);
			}
		}
	}
	/**
	 * @Description 清理君主 根据 两个君主的Id 和 eventId
	 */
	public void deleteMsgByEventIdAndOtherJzId(long jzId,long otherJzId,int eventId) {
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where  jzId='"
				+jzId+"' and eventId ="+eventId+"  and otherJzId ="+otherJzId);
		for (PromptMSG msg : promptMsgList) {
			if (msg!=null) {
				log.info("删除君主{}的速报Id--{},类型--{},内容为---《{}》",msg.jzId,msg.id,eventId,msg.content);
				HibernateUtil.delete(msg);
			}
		}
	}
	/**
	 * @Description 拒绝邀请时清理当前lmId对jzId的所有邀请通知
	 */
	public void deleteMsgByEventIdAndLmId(long jzId,int lmId,int eventId) {
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where  jzId='"
				+jzId+"' and eventId ="+eventId+"  and realCondition ="+lmId);
		for (PromptMSG msg : promptMsgList) {
			if (msg!=null) {
				log.info("删除君主{}的速报Id--{},类型--{},内容为---《{}》",msg.jzId,msg.id,eventId,msg.content);
				HibernateUtil.delete(msg);
			}
		}
	}
	/**
	 * @Description 忽略速报
	 */
	public void ignoreSuBao(long subaoId,  IoSession session, PromptActionResp.Builder resp, int type)  {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			resp.setSubaoType(type);
			session.write(resp.build());
			return;
		}
		HibernateUtil.delete(msg);
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setSubaoType(type);
		session.write(resp.build());
	}
	
	
	/**
	 * @Description //祝福
	 */
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
	
	

	
	/**
	 * @Description 	//安慰
	 */
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
				 msg2 = saveLMKBByCondition(msg.otherJzId, jz.id, 
						 new String[]{msg.jzName1, jz.name, msg.cartWorth},
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
	
	
	
	/**
	 * @Description 根据条件eventId和sendCondition得到速报配置
	 */
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
	 * @Description 向未协助联盟的成员推送盟友速报 2016年4月7日 此方法只对 	
	 * mccf_toOther=104  zdqz=105对未协助盟友发送 并且 只对在线的人 发送
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
			//未找到配置说明策划没有配置 此eventId 和 此horseType类型的事件 (2016年4月5日  目前马车触发 只有橙马和紫马有配置（horseType 为4 、5）)
			log.warn("向联盟---{}的未协助押镖成员 推送盟友速报失败,押镖君主--{},未找到ReportTemp配置,function=={},eventId=={}, horseType=={},receiveObject=={}",
					lmId,chufaJzId,function, eventId, horseType,5);
			return false;
		}
		HashSet<Long> xzSet = YaBiaoHuoDongMgr.xieZhuCache4YBJZ.get(chufaJzId);
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId=aplayer.junzhuId;
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
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
	 *@Description   获取盟友快报
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getMengyoukuaibao(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("获取盟友快报失败，君主不存在");
			return;
		}
		Long jzId = jz.id;
		PromptMSGResp.Builder resp = PromptMSGResp.newBuilder();
		// 查询出8小时内的盟友速报记录  
//		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "order by addTime desc" ,50); query.setFirstResult(0);   //从第0条开始	query.setMaxResults(size);//一共取size条
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
//		case ED.Lve_duo_fail: //掠夺失败
////			handleLveDuo(e);
//			break;
		case ED.been_lve_duo: // 被掠夺
			handleBeanLveDuo(e);
			break;
		case ED.Join_LM: // //清理邀请入盟速报
			handleCleanInvite(e);
			break;
		case ED.NEW_LIANMENG_YB_JUQING:
			YaBiaoJunQing yq = (YaBiaoJunQing) e.param;
			Long jzId = yq.ybjzId;
			Integer lmId = yq.lmId;
			tellAllMembers(jzId, lmId, true, FunctionID.lianmengJunQingYabiao, null);
			break;
		case ED.ACC_LOGIN:
			if (e.param != null && e.param instanceof Long) {
				long jzid = (Long) e.param;
				JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzid);
				if (junZhu == null) {
					break;
				}
				boolean isOpen2 = FunctionOpenMgr.inst.isFunctionOpen(FunctionID.yabiao, jzid, junZhu.level);
				if(!isOpen2){
					break;
				}
				AlliancePlayer allianceMember = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
				if(allianceMember != null && allianceMember.lianMengId > 0){
					List<YaBiaoJunQing> list = HibernateUtil.list(YaBiaoJunQing.class,
							" where lmId="+allianceMember.lianMengId);
					if(list != null && list.size() > 0) {
						SessionUser su = SessionManager.inst.findByJunZhuId(junZhu.id);
						if(su != null){
							FunctionID.pushCanShowRed(junZhu.id, su.session, FunctionID.lianmengJunQingYabiao);
						}
					}
				}
			}
			break;
//		case ED.REFRESH_TIME_WORK:
//			IoSession session = (IoSession) e.param;
//			if(session == null){
//				break;
//			}
//			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//			if(jz == null){
//				break;
//			}
//			boolean allianceMember = false;
//			AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jz.id);
//			if(player != null && player.lianMengId > 0) {
//				allianceMember = true;
//			}
//			// 掠夺开启，联盟军情开启
//			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.lveDuo, jz.id, jz.level);
//			if(isOpen && allianceMember){
//				List<LveDuoMI> miList = HibernateUtil.list(LveDuoMI.class, " where lmId="+player.lianMengId);
//				if(miList != null && miList.size() > 0) {
//					FunctionID.pushCanShowRed(jz.id, session, FunctionID.lianmengJunQingLveDuo);
//				}
//			}
//			// 押镖开启，军情开启
//			boolean isOpen2=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.yabiao, jz.id, jz.level);
//			if(isOpen2 && allianceMember){
//				List<YaBiaoJunQing> list = HibernateUtil.list(YaBiaoJunQing.class, " where lmId="+player.lianMengId);
//				if(list != null && list.size() > 0) {
//					FunctionID.pushCanShowRed(jz.id, session, FunctionID.lianmengJunQingYabiao);
//				}
//			}
//			break;
		default:
			break;
		}
	}
	
	/**
	 * @Description 清理邀请入盟速报
	 */
	public void handleCleanInvite(Event e) {
		Object[] oa = (Object[]) e.param;
		Long jzId = (Long) oa[0];
		log.info("清理---{}的邀请入盟速报,原因：已加入联盟",jzId);
		deleteLMSBByEventId(jzId, SuBaoConstant.invite);
	}

	public void handleBeanLveDuo(Event e){
		// 参数列表：{jId, junZhu.name, enemy.name, lostbuild}
		if(e == null || e.param == null){
			return;
		}
		if(! (e.param instanceof Object[])){
			return;
		}
		Object[] oa = (Object[]) e.param;
		if(oa.length < 5){
			return;
		}
		if(oa[0] == null || oa[1] == null ||oa[2] == null
				|| oa[3]==null  ||oa[4]==null ){
			log.error("发送掠夺失败，参数有null，请程序查看");
			return;
		}
//		int eventId = SuBaoConstant.lveDuo_fail_event;
		long  lveduoJzId = (Long) oa[0]; // 掠夺者
		long beanLveDjId =  (Long) oa[1];
		int willLostbuild = (Integer)oa[2];
		int zhandouId = (Integer)oa[3];
		int willLostBuildAllianceId = (Integer)oa[4];
		LveDuoMI mi = HibernateUtil.find(LveDuoMI.class, zhandouId);
		if(mi != null){
			log.error("存在战斗id是 ：{}的联盟军情之掠夺军情", zhandouId);
			return;
		}
		Date now = new Date();
		Date willLostBuildTime = 
				new Date(System.currentTimeMillis() + CanShu.EXPEL_TIMELIMIT * 60 * 1000);
		mi = new LveDuoMI(zhandouId, beanLveDjId, lveduoJzId,
				willLostBuildTime, willLostbuild, now, willLostBuildAllianceId);
		mi.remainHp = -1; // 初始值，表示没有被打过
		HibernateUtil.insert(mi);
		JunZhu jz1 = HibernateUtil.find(JunZhu.class, beanLveDjId);
		JunZhu jz2 = HibernateUtil.find(JunZhu.class, lveduoJzId);
		if(jz1 == null || jz2 == null){
			return;
		}
		AllianceBean lmBean = AllianceMgr.inst.getAllianceByJunZid(beanLveDjId);
		if(lmBean != null){
			// 红点
			try{
				// 广播
				String c = getBroadCastContent(jz1.name, jz2.name, willLostbuild);
				ProtobufMsg msg = BroadcastMgr.inst.buildMsg(c);
				tellAllMembers(beanLveDjId, lmBean.id, true, FunctionID.lianmengJunQingLveDuo, msg);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			// 聊天
			try{
				SessionUser user = SessionManager.inst.findByJunZhuId(jz1.id);
				if(user == null) return;
				String  chatContent = beanLveContent.replace("XXX", jz1.name).
						replace("*玩家名字七个字*", jz2.name).
						replace("00:00", "30:00").replace("M", willLostbuild+"");
				ChatPct.Builder b = ChatPct.newBuilder();
				Channel value = Channel.valueOf(1);// 联盟频道1
				b.setChannel(value);
				b.setContent(chatContent);
				b.setSenderId(jz1.id);
				b.setSenderName(jz1.name);
				ChatMgr.inst.addMission(PD.C_Send_Chat, user.session, b);
			}catch(Exception e2){
				e2.printStackTrace();
				log.error("被掠夺 发送聊天报错",e2);
			}
			
		}
	}

	public String getBroadCastContent(String mengyouName, String name2, int m){
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList != null){
			//"您的盟友[dbba8f]XXX[-]遭到[d80202]*玩家名字七个字*[-]的掠夺！
			//联盟将在[f5aa29]00:00[-]后损失M建设值！请速去驱逐敌人！
			Optional<AnnounceTemp> conf = confList.stream().filter(t->t.type==28).findFirst();
			if(conf.isPresent()){
				AnnounceTemp t = conf.get();
				String c = t.announcement.replace("XXX", mengyouName)
						.replace("*玩家名字七个字*", name2)
						.replace("00:00", "30:00")
						.replace("M", m+"");
				return c;
			}
		}
		return "";
	}
	public void handleBeanLveDuoAttack(Event e) {
		if(e == null || e.param == null){
			return;
		}
		if(! (e.param instanceof Object[])){
			return;
		}
		Object[] oa = (Object[]) e.param;
		if(oa.length < 5){
			return;
		}
		if(oa[0] == null || oa[1] == null ||oa[2] == null
				|| oa[3] == null  ||oa[4] == null ){
			log.error("发送被掠夺通报失败，参数有null，请程序查看");
			return;
		}
		long firstJzId = (Long) oa[0]; // 掠夺者
		String firstName = (String) oa[1];
		long secondJzId = (Long)oa[2]; // 被掠夺者
		String secondName = (String) oa[3];
		int jiangli = (Integer) oa[4];
		int eventId = SuBaoConstant.been_lveDuo_event;
		AllianceBean lmBean = AllianceMgr.inst.getAllianceByJunZid(secondJzId);
		if (lmBean != null) {
			boolean yes = tell(secondJzId, lmBean.id,
					eventId,  new String[]{firstName, secondName, "" , jiangli+""});
			if(yes){
				log.info("玩家:{}掠夺玩家：{}成功，"
						+ "向玩家：{}所在的联盟的所有联盟成员发送报消息成功", firstJzId, secondJzId, secondJzId);
			}
		}
	}
	
	/** 
	 * @Description 镖车被攻击事件处理
	 */
	public  void handleCarAttack(Event e) {
		Object[]	oa = (Object[]) e.param;
		Long	jbJzId = (Long) oa[0];
		Long	ybjzId=(Long)oa[1];
		Integer	jbJzUid = (Integer) oa[2];
		Integer	ybjzUid=(Integer)oa[3];
		if(ybjzId==null||jbJzId==null||jbJzUid==null||ybjzUid==null){
			log.error("镖车被攻击事件处理失败,ybjz=={}||jbJz=={},jbJzUid=={}||ybjzUid=={}",ybjzId,jbJzId,jbJzUid,ybjzUid);
			return;
		}
		AllianceBean	lmBean = AllianceMgr.inst.getAllianceByJunZid(ybjzId);
		if (lmBean != null) {
			YaBiaoJunQing msg=saveYaBiaoJunQing(ybjzId, jbJzId,ybjzUid,jbJzUid,lmBean.id);
			//添加新押镖联盟军情事件
			EventMgr.addEvent(ED.NEW_LIANMENG_YB_JUQING,msg);
		}
	}
	/**
	 * @Description 保存联盟军情
	 * @return
	 */
	public YaBiaoJunQing saveYaBiaoJunQing(long ybJzId, Long jbJzId, Integer ybjzUid, Integer jbjzUid, int lmId) {
		YaBiaoJunQing msg=new YaBiaoJunQing();
		msg.ybjzId=ybJzId;
		msg.jbjzId=jbJzId;
		msg.ybjzUid=ybjzUid;
		msg.jbjzUid=jbjzUid;
		msg.lmId=lmId;
		msg.happenTime=new Date();
		HibernateUtil.save(msg);
		log.info("保存联盟---{}联盟军情,押镖君主--{}，劫镖君主---{}", lmId,ybJzId,jbJzId);
		return msg;
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
			//清理联盟速报
			deleteLMSB2AllMengYou(lmBean.id, chufaJzId);
			//清理联盟押镖军情
			deleteLMJQ2AllMengYou(lmBean.id, chufaJzId);
		}else{
			deleteLMSB(chufaJzId, chufaJzId);
		}
	}
	
	/**
	 * @Description 返回押镖军情的两个Player对象
	 */
	public List<Object> getLianMengJunQing4YB(YaBiaoJunQing msg ) {
		List<Object> result4Empty=Collections.EMPTY_LIST;
		if(msg==null){
			log.error("联盟军情生成失败，YaBiaoJuQing--{}",msg);
			return result4Empty;
		}
		long ybJzId=msg.ybjzId;
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("联盟军情生成失败，未找到押镖马车--{}",ybJzId);
			return result4Empty;
		}
		//马车所在场景对象
		Scene sc = (Scene) ybr.session.getAttribute(SessionAttKey.Scene);
		if (sc == null) {
			log.error("联盟军情生成失败 ，镖车--{}场景未找到",ybJzId);
			return result4Empty;
		}
		Player biaoChe=sc.players.get(msg.ybjzUid);
		Player jbjz=sc.getPlayerByJunZhuId(msg.jbjzId);
		if(biaoChe==null||jbjz==null){
			log.error("联盟军情生成失败 ，镖车--{}未找到",msg.ybjzUid);
			return result4Empty;
		}
		List<Object> result=new ArrayList<Object>();
		result.add(biaoChe);
		result.add(jbjz);
		result.add(ybr.horseType);
		result.add(biaoChe.posX);
		result.add(biaoChe.posZ);
		return result;
	}
	
	/**
	 * @Description 马车出发处理
	 */
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

	
	/**
	 * @Description 	//马车被摧毁处理
	 */
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

	public void tellAllMembers(long juid, int lmId, boolean isIncludeSelf, int redId, ProtobufMsg msg){
		List<AlliancePlayer> aplayersList = AllianceMgr.inst.getAllianceMembers(lmId);
		if(aplayersList == null || aplayersList.size() == 0){
			return;
		}
		for (AlliancePlayer aplayer : aplayersList) {
			long jzId = aplayer.junzhuId;
			if(jzId == juid && !isIncludeSelf){
				continue;
			}
			SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
			if (su != null){
				FunctionID.pushCanShowRed(jzId, su.session, redId);

				// 在线玩家发送广播
				if(msg != null){
					su.session.write(msg);
				}
			}
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
	/**
	 * @Description 推送通知/速报
	 * @param session
	 * @param msg
	 */
	public void pushSubao(IoSession session,PromptMSG msg) {
		if(msg==null||session==null) return;
		SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
		subao=makeSuBaoMSG(subao, msg);
		session.write(subao.build());
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
	
	/**
	 * @Description 清理联盟---lmId的关于--chufaJzId的联盟军情
	 * @param lmId
	 * @param chufaJzId
	 * @return
	 */
	public boolean deleteLMJQ2AllMengYou(int lmId, Long chufaJzId) {
		log.info("清理联盟---{}的关于--{}的联盟军情开始",lmId,chufaJzId);
		List<YaBiaoJunQing> msgList = HibernateUtil.list(YaBiaoJunQing.class, "where ybjzId='"+chufaJzId+"'");
		for (YaBiaoJunQing msg : msgList) {
			if (msg!=null) {
				log.info("删除联盟---{}的 成员--{}的联盟军情Id--{}",lmId,chufaJzId,msg.id);
				HibernateUtil.delete(msg);
			}
		}
		log.info("清理联盟---{}的关于--{}的联盟军情结束",lmId,chufaJzId);
		return true;
	}

	public void allianceMIReq(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("掠夺求助出错，君主不存在");
			return;
		}
		long jId = jz.id;
		JunQingReq.Builder req = (JunQingReq.Builder)builder;
		int type = req.getType();
		AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jId);
		if(player == null || player.lianMengId <=0 ){
			return;
		}
		JunQingResp.Builder resp = JunQingResp.newBuilder();
		resp.setType(type);
		switch(type){
		case 1:
			dealLveDuoInfo(resp, jz, player.lianMengId);
			break;
		case 2:
			dealYaBiaoInfo(resp, jz, player.lianMengId);
			break;
		}
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.alliance_junQing_resq;
		pm.builder = resp;
		session.write(pm);
	}
	
	public void dealYaBiaoInfo(JunQingResp.Builder resp, JunZhu jzzzz, int lmid){
		/*
		 * 军情数据
		 */
//		lmId
		String where = " where lmId = " +lmid;
		List<YaBiaoJunQing> list = HibernateUtil.list(YaBiaoJunQing.class, where);
		HistoryBattleInfo.Builder info = null;
		Player enemy = null;
		Player friend = null;
		Integer horseType = null;
		for(YaBiaoJunQing mi: list){
			List<Object> pli = getLianMengJunQing4YB(mi);
			if(pli.size() == 0){
				continue;
			}
			enemy = (Player)pli.get(1);
			friend =  (Player)pli.get(0);
			horseType = (Integer)pli.get(2);
			
			
			info = HistoryBattleInfo.newBuilder();
			info.setEnemyJid(enemy.jzId);
			info.setEnemyName(enemy.name);
			info.setEnemyLevel(enemy.jzlevel);
			info.setEnemyCountryId(enemy.guojia);
			info.setEnemyAllHP(enemy.totalLife);
			info.setEnemyRemainHP(enemy.currentLife);
			info.setEnemyAllianceName(enemy.lmName);
			info.setEnemyZhanLi(enemy.zhanli);
			info.setEnemyRoleId(enemy.roleId);
	
			info.setFriendAllHP(friend.totalLife);
			info.setFriendRemainHP(friend.currentLife);
			info.setFriendJid(friend.jzId);
			info.setFriendName(friend.name);
			info.setFriendRoleId(horseType);
			info.setPosX((Float) pli.get(3));
			info.setPosZ((Float) pli.get(4));
			info.setHappendTime(mi.happenTime.getTime());
			info.setFriendHorseRoleId((int)pli.get(2));
			info.setItemId((int)mi.id);
			resp.addInfos(info);
		}
	}
	public void dealLveDuoInfo(JunQingResp.Builder resp, JunZhu jz, int lmid){
		/*
		 * 掠夺军情数据
		 */
		String where = " where lmId = " + lmid;
		List<LveDuoMI> list = HibernateUtil.list(LveDuoMI.class, where);
		HistoryBattleInfo.Builder info = null;
		JunZhu enemy = null;
		JunZhu friend = null;
		for(LveDuoMI mi: list){
			info = HistoryBattleInfo.newBuilder();
			info.setEnemyJid(mi.lveDuoJunId);
			enemy = HibernateUtil.find(JunZhu.class, mi.lveDuoJunId);
			if(enemy == null){
				continue;
			}
			friend = HibernateUtil.find(JunZhu.class, mi.beanLveDuoJunId);
			if(friend == null){
				continue;
			}
			long time = mi.willLostBuildTime.getTime() - System.currentTimeMillis();
			if(time <= 0){
				// 扣除建设值的倒计时已经结束，扣除建设值在其他方法中处理，此处只continue
				continue;
			}
			info.setEnemyName(enemy.name);
			info.setEnemyLevel(enemy.level);
			info.setEnemyCountryId(enemy.guoJiaId);
			info.setEnemyAllHP(enemy.shengMingMax);
			info.setEnemyRemainHP(mi.remainHp == -1? info.getEnemyAllHP(): mi.remainHp);
			AllianceBean b = AllianceMgr.inst.getAllianceByJunZid(enemy.id);
			info.setEnemyAllianceName(b==null?"":b.name);
			info.setEnemyZhanLi(JunZhuMgr.inst.getZhanli(enemy));
			info.setEnemyRoleId(enemy.roleId);
			int state = 0; // 0 没有     1 正在被驱逐
			Long yes  = fightingLock.get(mi.zhanDouIdFromLveDuo);
			if(yes != null){
				if(System.currentTimeMillis() > (yes + 3 * 60 * 1000)){
					fightingLock.remove(mi.zhanDouIdFromLveDuo);
				}else{
					state = 1;
				}
			}
			info.setState(state);
			info.setFriendJid(friend.id);
			info.setFriendName(friend.name);
			info.setFriendRoleId(friend.roleId);
			info.setHappendTime(mi.battleHappendTime.getTime());
			info.setRemainTime((int)time/1000);
			info.setWillLostBuild(mi.willLostBuild);
			info.setItemId(mi.zhanDouIdFromLveDuo);
			resp.addInfos(info);
		}
		/*
		 * 掠夺协助次数
		 */
		LveDuoHelp help = HibernateUtil.find(LveDuoHelp.class, jz.id);
		if(help == null){
			help = new LveDuoHelp(jz.id);
		}else{
			resetHelpData(help);
		}
		resp.setTodayAllHelp(help.todayAllHelp);
		resp.setTodayRemainHelp(help.todayRemainHelp);
		resp.setCd(getCD(help.lastHelpTime));
	}
	public int getCD(Date lastDate){
		if(lastDate == null) return 0;
		Date now = new Date();
		int cd = CanShu.EXPEL_CD;
		int leftTime = (int) (lastDate.getTime() / 1000 - now.getTime()
				/ 1000 +  cd);
		return leftTime<=0?0:leftTime;
	}
	
	public void quZhuInitData(int d, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		long jid = junZhu.id;
		PvpZhanDouInitReq.Builder req = (PvpZhanDouInitReq.Builder) builder;
		int zhanDouIdFromLveDuo = (int)req.getUserId();
		LveDuoMI lveMi = HibernateUtil.find(LveDuoMI.class, zhanDouIdFromLveDuo);
		if(lveMi == null){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("驱逐时间已过");
			session.write(errresp.build());
			log.error("君主：{}驱逐失败，驱逐对象已无效", jid);
			return;
		}
		long enemyId = lveMi.lveDuoJunId;
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jid);
		if(p == null || p.lianMengId <= 0){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("您已退出联盟，无法驱逐");
			session.write(errresp.build());
			log.error("君主：{}驱逐敌人：{}失败，君主退出联盟", jid, enemyId);
			return;
		}
		/*对联盟不做要求*/
//		AlliancePlayer p2 = HibernateUtil.find(AlliancePlayer.class, enemyId);
//		if(p2 == null || p2.lianMengId <= 0){
//			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
//			errresp.setResult("对手已退出联盟，无法驱逐");
//			session.write(errresp.build());
//			log.error("君主：{}驱逐敌人：{}失败，敌人退出联盟", jid, enemyId);
//			return;
//		}
		Long yes = fightingLock.get(zhanDouIdFromLveDuo);
		if(yes != null){
			if(System.currentTimeMillis() > (yes + 3 * 60 * 1000)){
				fightingLock.remove(zhanDouIdFromLveDuo);
			}else{
				ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
				errresp.setResult("对手正在被驱逐");
				session.write(errresp.build());
				log.error("君主：{}驱逐敌人：{}失败，另有攻击方已经在驱逐敌人", jid, enemyId);
				return;
			}
		}
		// 协防次数
		LveDuoHelp helpInfo = HibernateUtil.find(LveDuoHelp.class, jid);
		if(helpInfo == null){
			helpInfo = new LveDuoHelp(jid);
		}else{
			resetHelpData(helpInfo);
		}
		if(helpInfo.todayRemainHelp <= 0){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("协防剩余次数不够");
			session.write(errresp.build());
			log.error("君主：{}驱逐敌人：{}失败，协防剩余次数不够", jid, enemyId);
			return;
		}
		// 协防cd
		if(getCD(helpInfo.lastHelpTime) > 0){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("协防CD");
			session.write(errresp.build());
			log.error("君主：{}驱逐敌人：{}失败，协防CD", jid, enemyId);
			return;
		}
		int newZhandouId = zhandouIdMgr.incrementAndGet(); // 战斗id 后台使用
		
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		// 对手
		int enemyIndex = 101;
		JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
		JunZhuMgr.inst.calcJunZhuTotalAtt(enemy);
		Node.Builder enemyNode = Node.newBuilder();
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(enemy);
		PveMgr.inst.fillZhuangbei4Player(enemyNode, zbIdList, enemy.id);
		enemyNode.addFlagIds(101);
		enemyNode.setNodeType(NodeType.PLAYER);
		enemyNode.setNodeProfession(NodeProfession.NULL);
		enemyNode.setModleId(enemy.roleId);
		enemyNode.setNodeName(enemy.name);
		PveMgr.inst.fillDataByGongjiType(enemyNode, null);
		PveMgr.inst.fillGongFangInfo(enemyNode, enemy);
		LveDuoBean ebean = HibernateUtil.find(LveDuoBean.class, enemyId);
		int fangshouId = ebean == null ? -1 : ebean.gongJiZuHeId;
		PveMgr.inst.fillJZMiBaoDataInfo(enemyNode, fangshouId, enemy.id);
		LveDuoMI duoMI = HibernateUtil.find(LveDuoMI.class, zhanDouIdFromLveDuo);
		int enemyRem = duoMI.remainHp  == -1? enemy.getShengming(): duoMI.remainHp;
		enemyNode.setHp(enemyRem);
		enemyNode.setHpNum(1);
		enemyNode.setAppearanceId(1);
		enemyNode.setNuQiZhi(MibaoMgr.inst.getChuShiNuQi(enemy.id));
		enemyNode.setMibaoCount(0);
		enemyNode.setMibaoPower(JunZhuMgr.inst.getAllMibaoProvideZhanli(enemy));
		enemys.add(enemyNode.build());
		// 敌人雇佣兵
		enemyIndex += 1;
		setBingData(enemys, enemyIndex, enemy.level);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(enemyId));
		resp.setEnemyTroop(enemyTroop);

		/*
		 *  君主自己
		 */
		
		long jId = junZhu.id;
		int jlevel = junZhu.level;
		int mapId = 0;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jId);
		int gongjiId = bean == null ? -1 : bean.gongJiZuHeId;
		int selfFlagIndex = 1;
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu,
				selfFlagIndex, gongjiId, selfTroop);
		selfFlagIndex += 1;
		setBingData(selfs, selfFlagIndex,jlevel);
		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(jId));
		resp.setSelfTroop(selfTroop);
		resp.setZhandouId(newZhandouId);
		resp.setMapId(mapId);
		resp.setLimitTime(CanShu.MAXTIME_LUEDUO);
		session.write(resp.build());
		log.info("君主：{}驱逐敌人：{}， 符合条件，进入战斗界面完成", junZhu.id, enemyId);
		dealBattleRecord(helpInfo, zhanDouIdFromLveDuo, enemyId);
	}

	public void setBingData(List<Node> selfs, int flagIndex, int mylevel){
		int[] bings = LveDuoMgr.inst.resetFangShouGuongYongBing(mylevel);
		ArrayList<GuYongBing> bingList = new ArrayList<GuYongBing>();
		for (int i = 0; i < bings.length; i++) {
			GuYongBing bing =  LveDuoMgr.bingMap.get(bings[i]);
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++) {
				bingList.add(bing);
			}
		}
		PvpMgr.inst.fillGuYongBingDataInfo(selfs, flagIndex, bingList);
	}
	public void dealBattleRecord(LveDuoHelp bean,Integer zhanDouIdFromLveDuo,
			long enemyId){
		bean.lastHelpTime = new Date();
		bean.todayRemainHelp  -= 1;
		HibernateUtil.save(bean);
		fightingLock.put(zhanDouIdFromLveDuo, System.currentTimeMillis());
		log.info("防守方：{}，战斗锁定", zhanDouIdFromLveDuo);
//		EventMgr.addEvent(ED.lve_duo , new Object[] { jz.id});
	}
	
	public void resetHelpData(LveDuoHelp bean){
		if(bean == null ||bean.lastHelpTime == null){
			return;
		}
		if(DateUtils.isTimeToReset(bean.lastHelpTime, CanShu.REFRESHTIME_PURCHASE)){
			bean.todayAllHelp = CanShu.EXPEL_DAYTIMES;
			bean.todayRemainHelp = bean.todayAllHelp;
			HibernateUtil.save(bean);
			log.info("君主：{}过第二天零点重置了驱逐LveDuoHelp：", bean.junzhuId);
		}
	}
	
	public void dealQuZhuBattleResult(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		long jId = junZhu.id;
		QuZhuBattleEndReq.Builder req = (QuZhuBattleEndReq.Builder)builder;
		int oldZhandouId = req.getItemId();
		LveDuoMI lvMI = HibernateUtil.find(LveDuoMI.class, oldZhandouId);
		int lostbuild = 0;
		if(lvMI == null){
			log.info("没有数据");
			lostbuild = -1;
		}else{
			long winId = req.getWinId();
			long enemyId = lvMI.lveDuoJunId;
			if(winId == oldZhandouId){
				// 驱逐失败 不挽回建设值 do nothing
				lvMI.remainHp = req.getRemainHp();
				HibernateUtil.save(lvMI);
			}else if(winId == jId){
				// 驱逐成功
				HibernateUtil.delete(lvMI);
				JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
				LianmengEvent e = AllianceMgr.inst.lianmengEventMap.get(25);
				String eventStr = e == null? "": e.str;
				eventStr = eventStr
						.replaceFirst("%d", junZhu.name)
						.replaceFirst("%d", enemy==null? "":enemy.name);
				AllianceMgr.inst.addAllianceEvent(lvMI.lmId, eventStr);
				lostbuild = lvMI.willLostBuild;
			}else{
				log.error("战斗结果winId 有问题：{},需要问大王！！！", winId );
			}
			
			fightingLock.remove(oldZhandouId);
		}
		QuZhuBattleEndResp.Builder resp = QuZhuBattleEndResp.newBuilder();
		resp.setOk(1);
		resp.setBuild(lostbuild);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.qu_zhu_battle_end_resp;
		pm.builder = resp;
		session.write(pm);
	}
	
	public void goQuZhu(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		long jid = junZhu.id;
		QuZhuReq.Builder req = (QuZhuReq.Builder) builder;
		int zhanDouIdFromLveDuo = (int)req.getItemId();
		LveDuoMI lveMi = HibernateUtil.find(LveDuoMI.class, zhanDouIdFromLveDuo);
		// Code == 0: 可以协防
		// 1 对手联盟变更 2  我的联盟变更
		//3 ：驱逐时间已过。4 对手正在被驱逐 5 协防次数不够  6 协防cd大于0
		if(lveMi == null){
			DailyTaskMgr.INSTANCE.sendError(session, 3, PD.go_qu_zhu_resp, 3);
			return;
		}
		long enemyId = lveMi.lveDuoJunId;
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jid);
		if(p == null || p.lianMengId <= 0){
			DailyTaskMgr.INSTANCE.sendError(session, 2, PD.go_qu_zhu_resp, 2);
			log.error("君主：{}驱逐敌人：{}失败，君主退出联盟", jid, enemyId);
			return;
		}
		/*不做判定*/
//		AlliancePlayer p2 = HibernateUtil.find(AlliancePlayer.class, enemyId);
//		if(p2 == null || p2.lianMengId <= 0){
//			DailyTaskMgr.INSTANCE.sendError(session, 1, PD.go_qu_zhu_resp,1);
//			log.error("君主：{}驱逐敌人：{}失败，敌人退出联盟", jid, enemyId);
//			return;
//		}
		Long yes = fightingLock.get(enemyId);
		if(yes != null){
			if(System.currentTimeMillis() > (yes + 3 * 60 * 1000)){
				fightingLock.remove(enemyId);
			}else{
				DailyTaskMgr.INSTANCE.sendError(session, 4, PD.go_qu_zhu_resp, 4);
				log.error("君主：{}驱逐敌人：{}失败，另有攻击方已经在驱逐敌人", jid, enemyId);
				return;
			}
		}
		// 协防次数
		LveDuoHelp helpInfo = HibernateUtil.find(LveDuoHelp.class, jid);
		if(helpInfo == null){
			helpInfo = new LveDuoHelp(jid);
		}else{
			resetHelpData(helpInfo);
		}
		if(helpInfo.todayRemainHelp <= 0){
			DailyTaskMgr.INSTANCE.sendError(session, 5, PD.go_qu_zhu_resp, 5);
			log.error("君主：{}驱逐敌人：{}失败，协防剩余次数不够", jid, enemyId);
			return;
		}
		// 协防cd
		if(getCD(helpInfo.lastHelpTime) > 0){
			DailyTaskMgr.INSTANCE.sendError(session, 6, PD.go_qu_zhu_resp, 6);
			log.error("君主：{}驱逐敌人：{}失败，协防CD", jid, enemyId);
			return;
		}
		DailyTaskMgr.INSTANCE.sendError(session, 0, PD.go_qu_zhu_resp, 0);
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.BIAOCHE_CHUFA, this);
		EventMgr.regist(ED.BIAOCHE_BEIDA, this);
		EventMgr.regist(ED.BIAOCHE_CUIHUI, this);
		EventMgr.regist(ED.BIAOCHE_END, this);
		EventMgr.regist(ED.Lve_duo_fail, this); // 掠夺失败
		EventMgr.regist(ED.been_lve_duo, this); // 被别人掠夺的
		EventMgr.regist(ED.Join_LM, this);//清理邀请入盟速报
//		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.NEW_LIANMENG_YB_JUQING, this);
//		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.ACC_LOGIN, this);
	}
	
	
	/**
	 * @Description //保存恭贺通知（速报）
	 * @return
	 */
	public PromptMSG savePromptMSG4GongHe(long jzId, long otherJzId,
			int eventId, String[] param) {
		ReportTemp rt = getReportTempByCondition(eventId, -1);
		if(rt==null){
			log.error("保存恭贺 {}--{}交互通知失败,未找到ReportTemp配置,eventId=={}, lmId=={}",jzId,otherJzId, eventId);
			return null;
		}
		PromptMSG msg =null;
		List<PromptMSG> msgList=HibernateUtil.list(PromptMSG.class, "where otherJzId='"+otherJzId+"' and jzId='"
				+jzId+"' and eventId = "+rt.event+"");
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
			if(param.length >= 3){
				if(param[2]!=null&&!"".equals(param[2])){
					award= param[2];
				}
			}
		}
		msg.award=award;
		msg.content= getContent(rt.DescID, msg.jzName1,msg.jzName2,jiangli);
		HibernateUtil.save(msg);
		return msg;
	}
}