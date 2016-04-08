package com.qx.friends;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Greet.GreetReq;
import qxmobile.protobuf.Greet.GreetResp;
import qxmobile.protobuf.Greet.InviteReq;
import qxmobile.protobuf.Greet.InviteResp;
import qxmobile.protobuf.Prompt.PromptActionResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AnnounceTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMSG;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.timeworker.FunctionID;
import com.qx.world.BroadcastMgr;
import com.qx.world.Player;
import com.qx.world.Scene;

public class GreetMgr extends EventProc{
	
	/**
	 * 被打招呼冷却时间
	 */
	public static  int GREETED_COOL_TIME = 60000;
	/**表情展示时间*/
	public static  int FACE_COOL_TIME = 3000;
	/**恭贺结算的时间*/
	public static  int SETTLE_GONGHE_TIME = 3*60*1000;
	public static GreetMgr inst;
	public static Map<Integer, AnnounceTemp> annnounceMap;
	public Logger log = LoggerFactory.getLogger(GreetMgr.class);
	public GreetMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		GREETED_COOL_TIME =  CanShu.GREETED_CD * 1000;
		FACE_COOL_TIME=CanShu.BIAOQING_INTERVAL*1000;
		SETTLE_GONGHE_TIME = 3*60*1000;
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		Map<Integer, AnnounceTemp> annnounceMap = new HashMap<Integer, AnnounceTemp>();
		for (AnnounceTemp a : confList) {
			annnounceMap.put(a.id, a);
		}
		GreetMgr.annnounceMap=annnounceMap;
	}
	
	/**
	 * @Description 打招呼并添加好友
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void GreetAndAddFriend(int cmd, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			sendError(session, cmd, "未发现君主");
			log.error("cmd:{},未发现君主", cmd); 
			return;
		}
		long jzId=jz.id;
		GreetReq.Builder req=(GreetReq.Builder)builder;
		long targetjzId=req.getJzId();
		log.info("君主--{}对---{}打招呼并添加好友开始",jzId,targetjzId);
		GreetResp.Builder resp=GreetResp.newBuilder();
		if(targetjzId==jzId){
			log.error("君主--{}对---{}打招呼并添加好友失败，不能对自己打招呼",jzId,targetjzId);
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(targetjzId);
		if(su==null){
			log.info("君主--{}对---{}打招呼并添加好友失败，目标已下线",jzId,targetjzId);
			return;
		}
		IoSession targetSession=su.session;
		Long greetedCdTime = (Long) targetSession.getAttribute(SessionAttKey.LAST_GREETED_KEY);
		long currentMillis = System.currentTimeMillis();
		if (greetedCdTime == null || greetedCdTime <= currentMillis) {
			targetSession.setAttribute(SessionAttKey.LAST_GREETED_KEY, currentMillis
					+ GREETED_COOL_TIME);
		} else {
			log.info("君主--{}对---{}打招呼并添加好友失败，该玩家正在忙，1分钟内无法打招呼",jzId,targetjzId);
			resp.setResCode(2);
			session.write(resp.build());
			return;
		}
	
		//添加好友
	
		int resCode=addFriend(jz, targetjzId);
		resp.setResCode(resCode);
		session.write(resp.build());
		if(resCode==1){
			log.info("君主--{}对---{}打招呼并添加好友成功",jzId,targetjzId);
		}
		//广播表情
		broadcastFace(jzId, session);
		//发送系统广播
//		<AnnounceTemp id="231" type="17" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]发现您器宇不凡，一时起了亲近之心，向您抱拳致意。[-]" announceObject="4" />
		String msg=getBroadString(GreetConstant.greet);
		if(msg!=null){
			msg=msg.replace("*玩家名字七个字*", jz.name);
			log.info("向--{} 发送系统广播--{}", targetjzId,msg);
			BroadcastMgr.inst.send2JunZhu(msg, targetjzId);
		}
		//生成并发送通知
		sendGreetPrompt(targetSession, jzId, jz.name, targetjzId);
	}

	/**
	 * @Description 添加好友
	 * @param jz
	 * @param targetjzId
	 * @return -2 不能关注自己 -1 目标不存在 （负值 理论不该出现） 
			   10 对方在自己黑名单 20 自己好友达到上限 30 判断目标玩家已经在自己的好友列表中。
			   1 添加成功
	 */
	public int addFriend(JunZhu jz, long targetjzId ) {
		long jzId=jz.id;
		JunZhu friendJunZhu = HibernateUtil.find(JunZhu.class, targetjzId);
		if (friendJunZhu == null) {
			log.info("要关注的君主id {} 不存在", targetjzId);
			return -1; 
		}
		if (jz.id == targetjzId) {
			log.info("不能关注自己");
			return -2;
		}
		//1黑名单判断
		Boolean isBlack = Redis.getInstance().sexist(ChatMgr.inst.CACHE_BLACKLIST_OF_JUNZHU + jzId, "" + targetjzId);
		if(isBlack){
			return 10; 
		}
	
		//2 判断自己的好友数是否已经达到上限值
		long friendNum = Redis.getInstance().zcard_(FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id);
		if (friendNum >= FriendMgr.MAX_FRIEND_NUM) {// 好友达到人数上限
			log.info("好友数达到上限{}人", FriendMgr.MAX_FRIEND_NUM);
			return 20;
		}
		//3 判断目标玩家是否已经在自己的好友列表中。
		double tmpScore = Redis.getInstance().zscore(
				FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id, "" + targetjzId);
		Boolean exits = tmpScore == -1 ? false : true;
		if (exits) {// 好友已存在
			log.info("好友已关注，请不要重复关注");
			return 30;
		}
		//以下加好友处理
		double addScore = 0;
		if (Redis.getInstance().zcard_(FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id) > 0) {
			addScore = Redis.getInstance().zscore(
					FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id,
					Redis.getInstance().ztop(FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id, 1).iterator().next());
			// 获取目前最高的score
		} else {// 好友数为0
			addScore = 1;
		}
		Redis.getInstance().zadd(FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + jz.id,(int) (addScore + 1), "" + targetjzId);
		log.info("{}将{}添加进好友", jz.name, friendJunZhu.name);
		return 1;
	}

	/**
	 * @Description 广播表情
	 * @param jzId
	 * @param session
	 */
	public void broadcastFace(long jzId, IoSession session) {
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(scene != null) {
			Player player = scene.getPlayerByJunZhuId(jzId);
			if(player != null) {
				ProtobufMsg msg = makeHeadPct(player);
				log.info("广播君主--{}的表情",player.jzId);
				scene.broadCastEvent(msg, player.userId);
			}
		}
	}
	
	/**
	 * @Description 拼出脑门上次称号和表情
	 * @param player
	 * @return
	 */
	public ProtobufMsg makeHeadPct(final Player player) {
		ErrorMessage.Builder head = ErrorMessage.newBuilder();
		head.setCmd(0);
		head.setErrorCode(player.userId);
		head.setErrorDesc("chengHao:"+player.chengHaoId
				+"#$#LM:"+player.lmName
				+"#$#VIP:"+(player.vip)
				+"#$#ZhiWu:"+(player.zhiWu)
				//表情1 显示3秒
				+"#$#Face:"+("1|")+FACE_COOL_TIME
				//+"#$#targetUid:"+.. 如果要有表情目标 在这加
 				);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_HEAD_STRING;
		msg.builder = head;
		log.info("拼出--{}脑门上次称号和表情--{}",player.jzId,head.getErrorDesc());
		return msg;
	}


	/**
	 * @Description 生成并发送通知
	 * @param targetSession
	 * @param jzId
	 * @param jzName
	 * @param targetJzId
	 */
	public void sendGreetPrompt(IoSession targetSession,long jzId,String jzName,long  targetJzId) {
		log.info("生成并发送君主--{}对--{}打招呼通知", jzId, targetJzId);
		int eventId=SuBaoConstant.greet;
		PromptMSG msg=PromptMsgMgr.inst.saveLMKBByCondition(targetJzId,jzId, new String[]{jzName,""}, eventId, 1);
		if (msg!=null){
			PromptMsgMgr.inst.pushSubao(targetSession, msg);
		}
	}
	
	/**
	 * @Description 响应招呼并添加好友
	 */
	public void AnswerGreetAndAddFriend(long subaoId, int type, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error(" 响应招呼并添加好友:{},未发现君主", type); 
			return;
		}
		long jzId=jz.id;
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		resp.setSubaoType(type);
		if(msg==null){
			log.error("{} 响应招呼并添加好友:失败，未找到快报--{}信息",jzId,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		long greetJzId=msg.otherJzId;
		log.info("君主--{}响应---{}的招呼并添加好友开始",jzId,greetJzId);
		//添加好友
		int resCode=addFriend(jz, greetJzId);
		GreetResp.Builder resp2=GreetResp.newBuilder();
		resp2.setResCode(resCode);
		session.write(resp2.build());

		if(resCode==1){
			log.info("君主--{}响应---{}的招呼并添加好友成功",jzId,greetJzId);
		}
		String respContent="*玩家名字七个字*回应了你的善意。";
		//处理响应
		switch (type) {
		case SuBaoConstant.hello:
			//[dbba8f]*玩家名字七个字*[-][ffffff]回应了你的善意。[-] 19
//			<AnnounceTemp id="232" type="18" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]回应了你的善意。[-]" announceObject="3" />
			respContent=getBroadString(GreetConstant.hello);
			break;
		case SuBaoConstant.gun:
//			<AnnounceTemp id="233" type="19" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]希望你不要打扰他。[-]" announceObject="3" />
			//	[dbba8f]*玩家名字七个字*[-][ffffff]希望你不要打扰他。[-] 18
			respContent=getBroadString(GreetConstant.gun);
			break;
		default:
			respContent=null;
			log.error("君主--{}响应---{}的招呼类型错误--{}",jzId,greetJzId,type);
			break;
		}
		//打招呼的玩家会收到屏幕上方的系统提示
		if(respContent!=null){
			respContent=respContent.replace("*玩家名字七个字*", jz.name);
			log.info("告诉---{} ，君主--{}响应他的招呼",greetJzId,jzId);
			BroadcastMgr.inst.send2JunZhu(respContent, greetJzId);
		}
		//删除速报
		log.info("君主--{}响应---{}的招呼结束，删除速报-{}---<{}>",jzId,greetJzId,msg.id,msg.content);
		HibernateUtil.delete(msg);
	}
	//获取广播内容
	public String getBroadString(int typeId) {
		List<AnnounceTemp> broadcastList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		Optional<AnnounceTemp> p = broadcastList.stream().filter(t->t.type==typeId).findFirst();
		AnnounceTemp a=null;
		if(p.isPresent()){
			a=p.get();
		}
		if(a==null||a.announcement==null){
			log.error("获取广播内容失败，内容不存在announceTempId---{}",typeId);
			return null;
		}
		return a.announcement;
	}
	/**
	 * 发送错误消息
	 * 
	 * @param session
	 * @param cmd
	 * @param msg
	 */
	public void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
	
	/**
	 * @Description  邀请入盟
	 * @param id
	 * @param session
	 * @param builder
	 */
	 
	//返回 结果 1成功 2 联盟已满 3对方未开启联盟功能 4对方已加入别的联盟 5 你没有权限邀请别人加入联盟 （具体描述根据策划文档来） 
	// -1 目标不存在  负数逻辑异常 一般不可能出现
	public void Invite2LM(long jzId,String jzName,	long targetjzId, IoSession session) {
		InviteResp.Builder resp=InviteResp.newBuilder();
		// 判断你没有权限邀请别人加入联盟 
		AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (player == null || player.lianMengId <= 0||player.title<1) {
			log.error("{}邀请别人入盟失败,玩家没有权限邀请别人入盟", jzId);
			resp.setResCode(5);
			session.write(resp.build());
			return ;
		}
		AllianceBean  playerAlliance= HibernateUtil.find(AllianceBean.class, player.lianMengId);
		if (playerAlliance == null) {
			log.error("{}邀请别人入盟失败,联盟{}不存在", jzId,player.lianMengId);
			resp.setResCode(5);
			session.write(resp.build());
			return ;
		}
		int lmId= player.lianMengId ;
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, targetjzId);
		if (targetJz == null) {
			log.error("邀请别人入盟失败,目标君主id {} 不存在", targetjzId);
			resp.setResCode(-1);
			session.write(resp.build());
			return ; 
		}
		//1)第一步，联盟人数是否已满。
		int memberMax =AllianceMgr.inst.getAllianceMemberMax(playerAlliance.level);
		if (playerAlliance.members >= memberMax) {
			log.error("邀请别人入盟失败，联盟:{}人数已满,数量:{}等级:{}", playerAlliance.id, playerAlliance.members, playerAlliance.level);
			resp.setResCode(2);
			session.write(resp.build());
			return;
		}
		//2)第二步，判断玩家是否开启了联盟功能。
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.LianMeng, targetjzId, targetJz.level);
		if(!isOpen){
			log.info("君主--{}的功能---{}未开启,不推送",targetjzId,FunctionID.LianMeng);
			resp.setResCode(3);
			session.write(resp.build());
			return;
		}
		//3)第三步，判断该玩家是否已经加入联盟
		AllianceBean targetBean = AllianceMgr.inst.getAllianceByJunZid(targetjzId);
		if (targetBean != null) {
			log.info("{}有联盟，邀请别人入盟失败", targetjzId);
			resp.setResCode(4);
			session.write(resp.build());
			return;
		}
		resp.setResCode(1);
		session.write(resp.build());
		
		//生成邀请通知
		sendInvitePrompt(targetjzId,jzId,jzName,lmId,playerAlliance.name);
		//触发邀请事件
		EventMgr.addEvent(ED.Invite_LM, new Object[] { targetjzId, jzId});
	}
	/**
	 * @Description 生成邀请通知
	 * @param targetjzId
	 * @param invitejzId
	 * @param  invitejzName 
	 * @param lmId
	 * @param lmName 
	 */
	public void sendInvitePrompt(long targetjzId, long invitejzId, String  invitejzName, int lmId, String lmName) {
		log.info("生成并发送君主--{}对--{}邀请加入联盟--{}通知", invitejzId, targetjzId,lmId);
		int eventId=SuBaoConstant.invite;
		//说明 ：new String[]{lmName,invitejzName} 只是按照属性向savePromptMSG4ChangjingHudong 传递2个要替换的字符串
		PromptMSG msg=PromptMsgMgr.inst.savePromptMSG4ChangjingHudong(targetjzId,invitejzId, eventId, lmId, new String[]{lmName,invitejzName});
		if (msg!=null){
			SessionUser su = SessionManager.inst.findByJunZhuId(targetjzId);
			if(su==null){
				log.info("取消发送君主--{}对--{}邀请加入联盟--{}通知，目标下线", invitejzId, targetjzId,lmId);
				return;
			}
			IoSession targetSession=su.session;
			log.info("发送君主--{}对--{}邀请加入联盟--{}通知", invitejzId, targetjzId,lmId);
			PromptMsgMgr.inst.pushSubao(targetSession, msg);
		}
	}
	
	
	/**
	 * @Description /响应邀请加入联盟通知
	 * @param subaoId
	 * @param type
	 * @param session
	 */
	public void Answer2InviteLM(long subaoId, int type,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("响应邀请加入联盟通知失败  类型:{},未发现君主", type); 
			return;
		}
		long jzId=jz.id;
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		resp.setSubaoType(type);
		if(msg==null){
			log.error("{} 响应邀请加入联盟通知失败，未找到快报--{}信息",jzId,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		long greetJzId=msg.otherJzId;
		int lmId=msg.realCondition;
		if(lmId<0){
			log.error("{} 响应邀请加入联盟通知失败，未找到快报--{}信息",jzId,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		log.info("君主--{}响应---{}的邀请加入联盟--{}通知开始",jzId,greetJzId,lmId);

		switch (type) {
		case SuBaoConstant.joinLM:
			AnswerYes2InviteLM(msg, jz, session);
			break;
		case SuBaoConstant.refuseLM:;
			AnswerNo2InviteLM(msg, jz, session);
			break;
		default:
			break;
		}
		log.info("君主--{}响应---{}的邀请加入联盟--{}通知完成 ，删除通知--{}内容---《{}》",jzId,greetJzId,lmId,msg.id,msg.content);
		HibernateUtil.delete(msg);
	}
	
	
	/**
	 * @Description //拒绝邀请
	 */
	public void AnswerNo2InviteLM(PromptMSG msg, JunZhu jz, IoSession session) {
		long jzId=jz.id;
		long greetJzId=msg.otherJzId;
		int lmId=msg.realCondition;
		log.info("君主--{}拒绝---{}的邀请加入联盟--{}",jzId,greetJzId,lmId);
	}

	
	/**
	 * @Description 	//同意邀请
	 */
	public void AnswerYes2InviteLM(PromptMSG msg, JunZhu jz, IoSession session) {
		long jzId=jz.id;
		long greetJzId=msg.otherJzId;
		int lmId=msg.realCondition;
		log.info("君主--{}同意---{}的邀请加入联盟--{},尝试加入联盟",jzId,greetJzId,lmId);
		//加入处理
		AllianceMgr.inst.agreeInvite(PD.S_ALLIANCE_INVITE_RESP, session, jz, lmId);
	}


	
	@Override
	protected void doReg() {
		//第一次百战
		EventMgr.regist(ED.first_baiZhan_success, this);
		//联盟开启
		EventMgr.regist(ED.LM_FUNCTION_OPEN, this);
	}
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.first_baiZhan_success:
			handleGongHe4BaiZhan(event);
			break;
		case ED.LM_FUNCTION_OPEN:
			handleGongHe4LMOPEN(event);
			break;
		}
	}
	
	/**
	 * @Description 百战触发恭贺
	 * @param event
	 */
	public void handleGongHe4BaiZhan(Event event) {
		Object[]obs = (Object[]) event.param;
		long jzId = (Long)obs[0];
		JunZhu	jz = HibernateUtil.find(JunZhu.class, jzId);
		if(jz==null){
			log.error("玩家--{}打完第一次百战千军,触发恭贺广播处理失败，未找到JunZhu",jzId); 
			return;
		}
		Integer times = (Integer)obs[1];
		if(times==1){
			//玩家打完第一次百战千军,邀请恭贺新信息保存
			saveGongHeBean(jz);
//			<AnnounceTemp id="237" type="23" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]通过不断努力，终于击败了[-][e5e205]竞技[-][ffffff]中的对手！快去恭贺他吧！[-]" announceObject="1,2" />
			log.info("玩家--{}打完第一次百战千军,触发恭贺广播==次数----{}",jzId,times); 
			String msg=getBroadString(GreetConstant.baizhan);
			if(msg!=null){
				msg=msg.replace("*玩家名字七个字*", jz.name);
				log.info("向所有人发送系统广播--{}",msg);
				BroadcastMgr.inst.send(msg);
			}
			int eventId =SuBaoConstant.askgh4baizhan;
			sendYaoQingGongHe2All4BaiZhan(jzId,jz.name,eventId);
		}
	}
	
	
	/**
	 * @Description //打完第一次百战千军,邀请恭贺新信息保存
	 */
	public void saveGongHeBean(JunZhu jz) {
		long jzId =jz.id;
		log.info("玩家--{}打完第一次百战千军,邀请恭贺新信息保存",jzId); 
		GongHeBean gongheInfo=HibernateUtil.find(GongHeBean.class, jzId);
		if(gongheInfo!=null){
			log.warn("玩家--{}打完第一次百战千军,邀请恭贺新信息保存异常，恭贺信息已经初始化过",jzId); 
		}else{
			log.info("玩家--{}打完第一次百战千军,邀请恭贺信息初始化",jzId); 
			gongheInfo=new GongHeBean();
		}
		gongheInfo.jzId=jzId;
		if(gongheInfo.start4firstBZ==0){
			gongheInfo.start4firstBZ=new Date().getTime();
		}else{
			log.error("玩家--{}打完第一次百战千军,邀请恭贺新信息保存异常 ，start4firstBZ---{}",jzId,gongheInfo.start4firstBZ); 
		}
		HibernateUtil.save(gongheInfo);
	}
	
	
	/**
	 * @Description jzId开启联盟,邀请恭贺新信息保存
	 */
	public void saveGongHeBean4LM(long jzId) {
		log.info("玩家--{}开启联盟,邀请恭贺新信息保存",jzId); 
		GongHeBean gongheInfo=HibernateUtil.find(GongHeBean.class, jzId);
		if(gongheInfo!=null){
			log.warn("玩家--{}开启联盟,邀请恭贺新信息保存异常，恭贺信息已经初始化过",jzId); 
		}else{
			gongheInfo=new GongHeBean();
			log.info("玩家--{}开启联盟,邀请恭贺新信息初始化",jzId); 
		}
		gongheInfo.jzId=jzId;
		if(gongheInfo.start4LM==0){
			gongheInfo.start4LM=new Date().getTime();
		}else{
			log.error("玩家--{}开启联盟,邀请恭贺新信息保存异常 ，start4LM---{}",jzId,gongheInfo.start4LM); 
		}
		HibernateUtil.save(gongheInfo);
	}
	
	/**
	 * @Description 刷新恭贺计数
	 */
	public void refreshGongheTimes2JunZhu(long subaoId, JunZhu jz) {
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		if(msg==null){
			log.error("{}刷新恭贺计数失败，速报--{}不存在",jz.id,subaoId);
			return;
		}
		//速报会在别的地方删掉
		long targetJzId=msg.otherJzId;
		int eventType=msg.eventId;
		if(eventType==SuBaoConstant.askgh4baizhan){
			//刷新 玩家打完第一次百战千军 被恭贺信息
			refreshGongHeInfo4FirstBZ(jz, targetJzId);
		}else{
			//刷新玩家  --联盟功能开启 恭贺计数
			refreshGongHeInfo4LMOpen(jz, targetJzId);
			
		}
	}
	
	/**
	 * @Description //刷新玩家  --联盟功能开启 恭贺计数
	 */
	public void refreshGongHeInfo4LMOpen(JunZhu jz, long targetJzId) {
		GongHeBean gongheInfo=HibernateUtil.find(GongHeBean.class, targetJzId);
		Date date = new Date();
		if(gongheInfo==null){
			log.error("刷新恭贺计数玩家--{}联盟功能开启异常，恭贺信息未初始化过",targetJzId); 
			gongheInfo=new GongHeBean();
			gongheInfo.jzId=targetJzId;
			gongheInfo.start4LM = date.getTime();
		}
		if(gongheInfo.start4LM<0){
			log.info("{}不刷新--{}联盟功能恭贺计数 ,超过三分钟",jz.id,targetJzId);
			return;
		}
		if(gongheInfo.start4LM == 0){
			gongheInfo.start4LM = date.getTime();
			HibernateUtil.save(gongheInfo);
		}
		Date now =new Date();
		long distance2t=(int) (now.getTime()-gongheInfo.start4LM);
		if(distance2t<SETTLE_GONGHE_TIME){
			gongheInfo.times4LM++;
			HibernateUtil.save(gongheInfo);
			if (gongheInfo.times4LM==1) {
				//发送首次恭贺广播给targetJzId
//				<AnnounceTemp id="238" type="24" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]恭贺你开启了[-][00e1c4]联盟[-][ffffff]功能！他是首个恭贺你的玩家！[-]" announceObject="3" />
				String content=getBroadString(GreetConstant.firstLMGongHe);
				if(content!=null){
					content=content.replace("*玩家名字七个字*", jz.name);
					log.info("向--{} 发送系统广播--{}", targetJzId,content);
					BroadcastMgr.inst.send2JunZhu(content, targetJzId);
				}
			}
			log.info("{}刷新--{}联盟功能恭贺计数 ,变为--{}",jz.id,targetJzId,gongheInfo.times4LM);
		}else{
			log.info("{}不刷新--{}联盟功能恭贺计数 ,超过三分钟--{}",jz.id,targetJzId,distance2t);
		}
	}

	//刷新 玩家打完第一次百战千军 被恭贺信息
	public void refreshGongHeInfo4FirstBZ(JunZhu jz ,long targetJzId) {
		GongHeBean gongheInfo=HibernateUtil.find(GongHeBean.class, targetJzId);
		Date date = new Date();
		if(gongheInfo==null){
			log.error("刷新恭贺计数玩家--{}打完第一次百战千军异常，恭贺信息未初始化过",targetJzId); 
			gongheInfo=new GongHeBean();
			gongheInfo.jzId=targetJzId;
			gongheInfo.start4firstBZ=date.getTime();
		}
		if(gongheInfo.start4firstBZ<0){
			log.info("{}不刷新--{}打完第一次百战千军恭贺计数 ,超过三分钟",jz.id,targetJzId);
			return;
		}
		if(gongheInfo.start4firstBZ == 0){
			gongheInfo.start4firstBZ = date.getTime();
			HibernateUtil.save(gongheInfo);
		}
		Date now =new Date();
		long distance2t=(int) (now.getTime()-gongheInfo.start4firstBZ);
		double k=CanShu.CONGRATULATE_AWARD_K;
		double b=CanShu.CONGRATULATE_AWARD_B;
		if(distance2t<SETTLE_GONGHE_TIME){
			gongheInfo.times4firstBZ++;
			int addYuanBao= (int) (k*gongheInfo.times4firstBZ+b);
			if(addYuanBao>0){
				gongheInfo.award4firstBZ+=k*gongheInfo.times4firstBZ+b;
				HibernateUtil.save(gongheInfo);
			}
			if (gongheInfo.times4firstBZ==1) {
				//发送首次恭贺通知给targetJzId
				sendFirstGongheMsg(jz,targetJzId);
			}
			log.info("{}刷新--{}打完第一次百战千军恭贺计数 ,变为--{},奖励增加--{}，变为---{}",jz.id,targetJzId,gongheInfo.times4firstBZ,addYuanBao,gongheInfo.award4firstBZ);
		}else{
			log.info("{}不刷新--{}打完第一次百战千军恭贺计数 ,超过三分钟--{}",jz.id,targetJzId,distance2t);
		}
	}
	
	/**
	 * @Description 生成并发送首次百战恭贺通知给
	 * @param jz
	 * @param targetJzId
	 */
	public void sendFirstGongheMsg(JunZhu jz, long targetJzId) {
		long jzId=jz.id;
		log.info("生成并发送--{}首次百战恭贺通知给--{}", jzId, targetJzId);
		int eventId=SuBaoConstant.firstgh2baizhan;
		PromptMSG msg=PromptMsgMgr.inst.saveLMKBByCondition(targetJzId,jzId, new String[]{jz.name,""}, eventId, 1);
		if (msg!=null){
			SessionUser su = SessionManager.inst.findByJunZhuId(targetJzId);
			if(su==null){
				log.info("发送--{}首次恭贺通知给--{}失败，目标已下线",jzId,targetJzId);
				return;
			}
//			<AnnounceTemp id="239" type="25" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]恭贺你击败了[-][e5e205]竞技[-][ffffff]中的对手！他是首个恭贺你的玩家！快去领取奖励吧！[-]" announceObject="3" />
			String content=getBroadString(GreetConstant.firstBZGongHe);
			if(content!=null){
				content=content.replace("*玩家名字七个字*", jz.name);
				log.info("向--{} 发送系统广播--{}", targetJzId,msg);
				BroadcastMgr.inst.send2JunZhu(content, targetJzId);
			}
			log.info("发送--{}首次恭贺通知给--{}",jzId,targetJzId);
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
	}

	/**
	 * @Description 联盟开启触发恭贺
	 * @param event
	 */
	public void handleGongHe4LMOPEN(Event event) {
		Object[]obs = (Object[]) event.param;
		Long jzId = (Long)obs[0];
		String jzName=(String)obs[1];
		if(jzId==null||jzName==null){
			log.error("玩家--{}联盟开启触发恭贺广播处理失败，未找到JunZhu",jzId); 
			return;
		}
		//初始化 start4LM
		saveGongHeBean4LM(jzId);
//<AnnounceTemp id="236" type="22" condition="-1" announcement="[dbba8f]*玩家名字七个字*[-][ffffff]通过不断努力，终于开启了[-][00e1c4]联盟[-][ffffff]功能！快去恭贺他吧！[-]" announceObject="1,2" />
		String msg=getBroadString(GreetConstant.lianmeng);
		if(msg!=null){
			msg=msg.replace("*玩家名字七个字*", jzName);
			log.info("君主:{}联盟功能开启，向所有人发送系统广播--{}", msg, jzId);
			BroadcastMgr.inst.send(msg);
		}
		sendYaoQingGongHe2All(jzId,jzName);
	}
	
	/**
	 * @Description 给所有在线玩家推送邀请联盟开启恭贺通知
	 * @param targertId
	 * @param targetjzName
	 */
	public void sendYaoQingGongHe2All(long targertId, String targetjzName) {
		log.info("玩家:{}开启了联盟功能,触发恭贺广播开始",targertId); 
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		if(list == null){
			return;
		}
		for (SessionUser su: list){
			if(su==null){
				continue;
			}
			IoSession session = su.session;
			JunZhu jz =  JunZhuMgr.inst.getJunZhu(session);
			if(jz==null||jz.id==targertId){
				continue;
			}
			long jzId=jz.id;
			int eventId=SuBaoConstant.askgh4lm2other;
			AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jzId);
			if (player != null && player.lianMengId >0 &&
					(player.title == AllianceMgr.TITLE_LEADER || player.title == AllianceMgr.TITLE_DEPUTY_LEADER)) {
				log.info("邀功联盟玩家:{}-职位:{}来恭贺玩家:{}开启的联盟功能",jzId, targertId);
				eventId=SuBaoConstant.askgh4lm2leader;
				sendGongHePrompt(jz.id, targertId, targetjzName, eventId,session);
			}else{
				log.info("邀功联盟玩家:{} 来恭贺玩家:{}开启的联盟功能",jzId, targertId);
				sendGongHePrompt(jz.id, targertId, targetjzName, eventId,session);
			}
		}
		log.info("玩家:{}开启了联盟功能,触发恭贺广播结束",targertId); 
	}
	/**
	 * @Description 给所有在线玩家推送邀请百战恭贺通知
	 * @param targertId
	 * @param targetjzName
	 * @param eventId
	 */
	public void sendYaoQingGongHe2All4BaiZhan(long targertId, String targetjzName,int eventId) {
		log.info("玩家--{}打完第一次百战千军,触发恭贺广播开始",targertId); 
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		if(list == null){
			return;
		}
		for (SessionUser su: list){
			if(su==null){
				continue;
			}
			IoSession session = su.session;
			JunZhu jz =  JunZhuMgr.inst.getJunZhu(session);
			if(jz==null||jz.id==targertId){
				continue;
			}
			sendGongHePrompt(jz.id, targertId, targetjzName, eventId,session);
		}
		log.info("玩家--{}打完第一次百战千军,触发恭贺广播结束",targertId); 
	}
	/**
	 * @Description 生成邀请恭贺通知
	 * @param jzId
	 * @param session 
	 * @param invitejzId
	 * @param  invitejzName 
	 * @param lmId
	 */
	public void sendGongHePrompt(long jzId, long targertId, String  targetjzName, int eventId, IoSession session) { 
		 log.info("生成并发送邀请---{}恭贺---{}通知  ，eventId=={}",jzId, targertId,eventId);
		 PromptMSG msg=PromptMsgMgr.inst.savePromptMSG4GongHe(jzId,targertId, eventId, new String[]{targetjzName});
		if (msg!=null){
			log.info("向--{}推送邀请恭贺--{}通知",  jzId, targertId);
			PromptMsgMgr.inst.pushSubao(session, msg);
		}
		 log.info("生成并发送邀请---{}恭贺---{}通知  ，eventId=={}完成",jzId, targertId,eventId);
	}
	
	/**
	 * @Description //处理从速报过来的邀请入盟
	 */
	public void Invite2LM4GongHe(JunZhu jz,long subaoId, int type,IoSession session) {
		long jzId=jz.id;
		String jzName=jz.name;
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		resp.setSubaoType(type);
		if(msg==null){
			log.error("君主{}从通知邀请别人加入联盟失败 ，未找到快报--{}信息",jzId,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		long targetJzId=msg.otherJzId;
		log.info("君主{}从通知邀请别人加入联盟 ，邀请目标--{}",jzId,targetJzId);
		Invite2LM(jzId, jzName, targetJzId, session);
		//删除速报
		HibernateUtil.delete(msg);
		log.info("君主{}从通知邀请别人加入联盟,不发奖,删除速报--{} 结束",jzId,subaoId);
	}

	
}
