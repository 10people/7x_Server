package com.manu.dynasty.chat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.boot.GameServer;
//import com.manu.dynasty.pvp.domain.NationalWarConstants;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.friends.FriendMgr;
import com.qx.gm.role.GMRoleMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;
import com.qx.vip.VipData;
import com.qx.world.Mission;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.Chat.BlackJunzhuInfo;
import qxmobile.protobuf.Chat.BlacklistResp;
import qxmobile.protobuf.Chat.CGetChat;
import qxmobile.protobuf.Chat.CGetYuYing;
import qxmobile.protobuf.Chat.CancelBlack;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.Chat.ChatSettings;
import qxmobile.protobuf.Chat.ContactsJunzhuInfo;
import qxmobile.protobuf.Chat.GetBlacklistResp;
import qxmobile.protobuf.Chat.JoinToBlacklist;
import qxmobile.protobuf.Chat.RecentContacts;
import qxmobile.protobuf.Chat.SChatLogList;
import qxmobile.protobuf.Chat.SGetYuYing;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

//FIXME 需要使用单独的线程来处理聊天。
/**
 * 
 * @author 康建虎
 * 
 */
public class ChatMgr implements Runnable {
	public static Logger log = LoggerFactory.getLogger(ChatMgr.class);
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0, null, null);
	public static ChatMgr inst;
	public static int MAX_BlACK_NUM = 100;// 屏蔽玩家数量上限
	public static final int SUCCESS = 0;// 屏蔽玩家成功
	public static final int ERROR_EXIST = 101;// 屏蔽的好友已存在
	public static final int ERROR_SELF = 102;// 不能屏蔽自己
	public static final int ERROR_MAXNUM = 103;// 屏蔽玩家数达到上限
	public static final int ERROR_JUNZHU_NULL = 104;// 君主不存在
	public static final int ERROR_NOT_BLACK = 105;// 未屏蔽的君主
	/** 世界聊天对应的purchase表的唯一id **/
	public static int CHAT_WORLD_COST_TYPE = 102;
	public static SensitiveFilter sFilter;

	public static String CACHE_BLACKLIST_OF_JUNZHU = "ChatBlackList:id:";
	public static String CACHE_RECENT_CONTACTS = "ChatRecentContacts:id:";
	public int saveRecentContactCount = 6;
	// ConcurrentHashMap迭代时不会出并发修改异常，它是弱一致的。
	public ConcurrentHashMap<Long, SessionUser> allUser;
	public ChatChWorld chWorld;
	public ChatChSiLiao chSiLiao;
	public ChatChBroadcast chBroadcast;
	public ChatChLianMeng chLianMeng;
	public ChatChSys chSys;
	// public ChatChGuoJia[] chGuoJiaArr;
	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 */
	public SimpleDateFormat dateFormat;

	public static ChatMgr getInst() {
		if (inst == null) {
			new ChatMgr();
		}

		return inst;
	}

	public ChatMgr() {
		allUser = new ConcurrentHashMap<Long, SessionUser>();
		setInst();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//
		chWorld = new ChatChWorld("ChatChWorld");
		chSys = new ChatChSys("ChatChSys");
		chSiLiao = new ChatChSiLiao("ChatChSiLiao");
		chLianMeng = new ChatChLianMeng("ChatChLianMeng");
		chBroadcast = new ChatChBroadcast("ChatChBroadcast");
		// chGuoJiaArr = new ChatChGuoJia[7];
		// chGuoJiaArr[NationalWarConstants.NATION_ID_QIN-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_QIN);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_YAN-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_YAN);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_ZHAO-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_ZHAO);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_WEI-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_WEI);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_HAN-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_HAN);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_QI-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_QI);
		// chGuoJiaArr[NationalWarConstants.NATION_ID_CHU-1] = new
		// ChatChGuoJia(NationalWarConstants.NATION_ID_CHU);
		new Thread(this, "ChatMgr").start();
		log.info("启动ChatMgr");
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
		log.info("退出ChatMgr");
	}

	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void shutdown() {
		missions.add(exit);
	}

	protected void setInst() {
		inst = this;
	}

	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.C_Send_Chat:
			clientSendChat(id, builder, session);
			break;
		case PD.C_GET_CHAT_CONF:
			sendChatConf(id,session,builder);
			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
	}
	
	public static int worldFreeTimesOfDay = CanShu.WORLDCHAT_FREETIMES;
	public static int worldPrice = CanShu.WORLDCHAT_PRICE;
	
	public void sendChatConf(int id, IoSession session, Builder builder) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(jzId == null){
			return;
		}
		int useTimes = 0;
		ChatInfo info = HibernateUtil.find(ChatInfo.class, jzId);
		if(info != null && DateUtils.isSameDay(info.lastTime)){
			//不是null且是今日的记录，则使用次数有效。
			useTimes = info.useTimes;
		}
		ErrorMessage.Builder ret = ErrorMessage.newBuilder();
		ret.setCmd(worldPrice);
		ret.setErrorCode(Math.max(0,worldFreeTimesOfDay - useTimes));
		ret.setErrorDesc("ErrorCode是剩余免费世界聊天次数。");
		ProtobufMsg msg = new ProtobufMsg();
		msg.builder = ret;
		msg.id = PD.S_GET_CHAT_CONF;
		session.write(msg);
	}

	public void clientSendChat(int id, Builder builder, IoSession session) {
		if ((builder instanceof ChatPct.Builder) == false) {
			log.error("不是聊天消息类 {}", builder.getClass().getSimpleName());
			return;
		}
		ChatPct.Builder cm = (ChatPct.Builder) builder;
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}

		// 2015年7月3日 16:52 检查账号是否处于禁言，禁言则禁止发表言论
		if (GMRoleMgr.checkGMJinyan(jz.id)) {
			log.warn("发言人{}已经被禁言", jz.id);
			return;
		}

		cm.setSenderName(jz.name);
		cm.setRoleId(jz.roleId);
		fixSendTime(cm);
		String msg = cm.getContent();
		if (log.isDebugEnabled()) {
			log.debug(
					"发起聊天 senderId {} senderName {} channel {} content {}",
					new Object[] { cm.getSenderId(), cm.getSenderName(),
							cm.getChannel(), msg });
		}
		//2015年9月8日增加发言者联盟 国家 vip等级
		cm.setGuoJia(jz.guoJiaId);
		cm.setVipLevel(jz.vipLevel);
		AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		String lianmengName=ybabean == null ? "" : ybabean.name;
		cm.setLianmengName(lianmengName);
		cm.setLianmengId(ybabean == null ? 0 : ybabean.id);
		log.info("君主{} vip等级---{} 联盟---{} 国家 --{} 发表聊天", jz.name,cm.getVipLevel(),"".endsWith(cm.getLianmengName())?"无联盟":cm.getLianmengName(),cm.getGuoJia());
		// 处理聊天内容
		String content = replaceIllegal(msg);
		if (content != null) {
			cm.setContent(content);
		} else {
			log.error("重置君主--{}的聊天内容--{}失败", jz.id, msg);
		}
		if(isCooltime(cm, session)) {
			log.error("发送聊天失败，频道:{}的cd时间还未到", cm.getChannel());
			return;
		}
		switch (cm.getChannel()) {
		case SILIAO:
			siLiao(cm, session);
			break;
		case LIANMENG:
			lianMeng(session, cm, allUser);
			break;
		case GUOJIA:
			sendGuoJia(cm);
			break;
		case SHIJIE:
			sendWorldChat(id, session, cm, jz);
			break;
		case XiaoWu:
			xiaoWu(session, cm);
			break;
		case Broadcast:
			userBroadcast(session,cm, jz);
			break;
		default:
			log.error("未处理的频道类型 {}", cm.getChannel());
			break;
		}
	}

	public void sendWorldChat(int cmd, IoSession session, ChatPct.Builder cm, JunZhu jz) {
		boolean open = BigSwitch.inst.vipMgr.isVipPermit(VipData.world_chat, jz.vipLevel);
		if (!open) {
			log.info("{}未满足世界聊天VIP要求", jz.name);
			return;
		}
		if(cm.getType() == 2) {// 表示联盟招募
			AlliancePlayer mgrMember = HibernateUtil.find(AlliancePlayer.class, jz.id);
			if (mgrMember == null || mgrMember.lianMengId <= 0) {
				log.error("发送联盟招募聊天信息失败-君主:{}还未加入联盟", jz.id);
				return;
			}
			AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mgrMember.lianMengId);
			if (alncBean == null) {
				log.error("发送联盟招募聊天信息失败-联盟:{}未找到", mgrMember.lianMengId);
				return;
			}
		}
		
		boolean sendFree = false;
		ChatInfo info = HibernateUtil.find(ChatInfo.class, jz.id);
		if(info == null){ //没发过
			info = new ChatInfo();
			info.jzId = jz.id;
			info.lastTime = new Date();
			info.useTimes = 1;
			HibernateUtil.insert(info);
		}else if(!DateUtils.isSameDay(info.lastTime)){ //今天没发过，重置次数
			info.lastTime = new Date();
			info.useTimes = 1;
			HibernateUtil.update(info);
		}else if(info.useTimes < worldFreeTimesOfDay){ //有免费次数
			sendFree = true;
			info.useTimes += 1;
			HibernateUtil.update(info);
		}else if(jz.yuanBao < worldPrice){
			log.info("{}元宝不足，不能发广播", jz.yuanBao);
			return;
		}
		
		if(!sendFree) {
			YuanBaoMgr.inst.diff(jz, -worldPrice, 0, worldPrice, YBType.YB_CHAT_WORLD, "世界聊天");
			HibernateUtil.update(jz);
			if(worldPrice > 0){
				JunZhuMgr.inst.sendMainInfo(session,jz);// 推送元宝信息
			}
			log.info("junzhu:{}世界聊天,花费元宝:{}", jz.name, worldPrice);
		}
		session.setAttribute(SessionAttKey.LAST_WORLD_CHAT_KEY, System.currentTimeMillis());
		// guojia :周国 = 0; QIN = 1; YAN = 2; ZHAO = 3; WEI = 4; HAN = 5; QI = 6; CHU = 7; 100-系统
		chWorld.saveChatRecord(cm);
		cm.clearSoundData();// 去除声音信息。
		broadcast(cm, allUser);
	}

	/**
	 * 玩家发起广播，客户端需要横向滚动。2015年12月28日11:17:29
	 * @param session
	 * @param cm
	 */
	public void userBroadcast(IoSession session,
			qxmobile.protobuf.Chat.ChatPct.Builder cm, JunZhu jz) {
		if(jz.yuanBao < CanShu.BROADCAST_PRICE){
			log.info("{}元宝不足，不能发广播", jz.yuanBao);
			return;
		}
		session.setAttribute(SessionAttKey.LAST_BROADCAST_CHAT_KEY, System.currentTimeMillis());
		YuanBaoMgr.inst.diff(jz, -CanShu.BROADCAST_PRICE, CanShu.BROADCAST_PRICE, 0, YBType.WORLD_CHAT, "广播频道聊天");
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		broadcast(cm, allUser);
		chBroadcast.saveChatRecord(cm);
	}

	public void xiaoWu(IoSession session,
			qxmobile.protobuf.Chat.ChatPct.Builder cm) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (jzId == null) {
			return;
		}
		Long hId = BigSwitch.inst.houseMgr.inWhichHouse.get(jzId);
		if (hId == null) {
			log.warn("所在房屋没有找到{}", jzId);
			return;
		}
		Map<Long, IoSession> map = BigSwitch.inst.houseMgr.playerInHouse
				.get(hId);
		if (map == null) {
			log.warn("小屋频道没有找到{}", jzId);
			return;
		}
		Iterator<IoSession> it = map.values().iterator();
		ChatPct b = cm.build();
		while (it.hasNext()) {
			IoSession ioSession = (IoSession) it.next();
			ioSession.write(b);
		}
	}

	// FIXME 这个方法的效率有待考核
	protected void lianMeng(IoSession session,
			qxmobile.protobuf.Chat.ChatPct.Builder cm,
			ConcurrentHashMap<Long, SessionUser> allUser2) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (jzId == null) {
			return;
		}
		
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (ap == null) {
			log.error("联盟成员信息没有找到：{}", jzId);
			return;
		}
		long lmId = ap.lianMengId;
		if (lmId <= 0) {
			log.warn("{}已不在联盟中", jzId);
			return;
		}
		session.setAttribute(SessionAttKey.LAST_LIANMENG_CHAT_KEY, System.currentTimeMillis());
		// 联盟成员的君主id
		Set<String> jzIds = Redis.getInstance().sget(
				AllianceMgr.inst.CACHE_MEMBERS_OF_ALLIANCE + lmId);
		Iterator<SessionUser> it = allUser.values().iterator();
		ChatPct pct = cm.build();
		while (it.hasNext()) {
			SessionUser u = it.next();
			Long junzhuId = (Long) u.session
					.getAttribute(SessionAttKey.junZhuId);
			if (junzhuId == null) {
				continue;
			}
			String v = junzhuId.toString();
			if (!jzIds.contains(v)) {
				continue;
			}
			u.session.write(pct);
		}
		chLianMeng.saveChatRecord(cm);
	}

	public void sendGuoJia(qxmobile.protobuf.Chat.ChatPct.Builder cm) {

	}

	protected void fixSendTime(ChatPct.Builder cm) {
		cm.setDateTime(dateFormat.format(Calendar.getInstance().getTime()));
		if(cm.getContent().length() > CanShu.CHAT_MAX_WORDS) {
			cm.setContent(cm.getContent().substring(0, CanShu.CHAT_MAX_WORDS));
		}
	}

	public void siLiao(qxmobile.protobuf.Chat.ChatPct.Builder cm,
			IoSession session) {
		if (cm.hasReceiverId() == false) {
			log.warn("私聊时为发来接受者id,senderName {}", cm.getSenderName());
			return;
		}
		if (isSenderBlack(cm.getReceiverId(), cm.getSenderName())) {// 如果发送人在屏蔽列表内
			return;
		}
		long count = Redis.getInstance().llen(CACHE_RECENT_CONTACTS + cm.getSenderId());
		if(count > saveRecentContactCount) {
			Redis.getInstance().rpop(CACHE_RECENT_CONTACTS + cm.getSenderId());
		} 
		Redis.getInstance().lpush_(CACHE_RECENT_CONTACTS + cm.getSenderId(), cm.getReceiverId()+"");
		long recvId = cm.getReceiverId();
		IoSession recvSession = SessionManager.getInst().getIoSession(recvId);
		if(recvSession != null) {
			session.setAttribute(SessionAttKey.LAST_SILIAO_CHAT_KEY, System.currentTimeMillis());
			recvSession.write(cm.build());
			session.write(cm.build());
		}
		chSiLiao.saveChatRecord(cm);
	}

	public boolean isCooltime(qxmobile.protobuf.Chat.ChatPct.Builder cm, IoSession session) {
		long currentMillis = System.currentTimeMillis();
		Long lastTime = currentMillis;
		int interval = 0;
		switch (cm.getChannel()) {
		case SILIAO:
			lastTime = (Long) session.getAttribute(SessionAttKey.LAST_SILIAO_CHAT_KEY);
			interval = CanShu.CHAT_SECRET_INTERVAL_TIME * 1000;
			break;
		case LIANMENG:
			lastTime = (Long) session.getAttribute(SessionAttKey.LAST_LIANMENG_CHAT_KEY);
			interval = CanShu.CHAT_ALLIANCE_INTERVAL_TIME * 1000;
			break;
		case SHIJIE:
			lastTime = (Long) session.getAttribute(SessionAttKey.LAST_WORLD_CHAT_KEY);
			interval = CanShu.CHAT_WORLD_INTERVAL_TIME * 1000;
			break;
		case Broadcast:
			lastTime = (Long) session.getAttribute(SessionAttKey.LAST_BROADCAST_CHAT_KEY);
			interval = CanShu.CHAT_BROADCAST_INTERVAL_TIME * 1000;
			break;
		default:
			log.error("未处理的频道类型 {}", cm.getChannel());
			break;
		}
			
		if (lastTime != null && currentMillis - lastTime < interval) {
			log.warn("发送速度过快{}", cm.getSenderId());
			return true;
		}
		return false;
	}

	public synchronized void addUser(SessionUser u) {
		allUser.put(u.session.getId(), u);
	}

	public synchronized void sendSysChat(String msg) {
		ChatPct.Builder cm = ChatPct.newBuilder();
		fixSendTime(cm);
		cm.setChannel(Channel.SYSTEM);
		cm.setContent(msg);
		cm.setSenderId(-1);
		cm.setSenderName("系统");
		cm.setGuoJia(100);//系统的国家
		chSys.saveChatRecord(cm);
		broadcast(cm, allUser);
		log.info("系统消息 {}", msg);
	}

	public synchronized void broadcast(ChatPct.Builder cm,
			ConcurrentHashMap<Long, SessionUser> map) {
		Iterator<SessionUser> it = map.values().iterator();
		ChatPct pct = cm.build();
		while (it.hasNext()) {
			SessionUser u = it.next();
			Long junzhuId = (Long) u.session
					.getAttribute(SessionAttKey.junZhuId);
			// if (junzhuId != null
			// && !Redis.getInstance().sexist(
			// CACHE_BLACKLIST_OF_JUNZHU + junzhuId,
			// "" + cm.getSenderName())) {
			if (junzhuId != null) {
				if ("系统".equals(cm.getSenderName())) {
					log.info("给{}发送系统信息", junzhuId);
					u.session.write(pct);
				} else if (!isSenderBlack(junzhuId, cm.getSenderName())) {
					log.info("给{}发送聊天信息", junzhuId);
					u.session.write(pct);
				}
			}
		}
	}

	public synchronized void removeUser(SessionUser u) {
		allUser.remove(u.session.getId());
	}

	public List<?> getChatLog(Builder builder) {
		if ((builder instanceof CGetChat.Builder) == false) {
			log.error("类型错误 {}", builder.getClass().getSimpleName());
			return Collections.EMPTY_LIST;
		}
		String key = chWorld.key;
		CGetChat.Builder cb = (qxmobile.protobuf.Chat.CGetChat.Builder) builder;
		int base = cb.getStart();
		int cnt = cb.getEnd() - 1;// 当做需要获取条数。需要时可以由服务器修正。redis接口是两端闭区间，所以-1.
		Redis rds = Redis.getInstance();
		if (base == -100) {
			List<?> list = Redis.getInstance().lrange(key,
					ChatPct.getDefaultInstance(), -cnt - 1, -1);
			log.debug("获取最近 {} {}条", -1, -cnt);
			return list;
		}
		ChatPct.Builder head = (ChatPct.Builder) rds.lindex(key,
				ChatPct.getDefaultInstance(), 0);
		int headIdx = 0;
		if (head != null) {
			headIdx = head.getSeq();
		}
		if (base <= headIdx) {
			// 客户端已经有全部的记录
			log.debug("客户端基数<=服务器基数");
			return Collections.EMPTY_LIST;
		}// base > headIdx
		int endIndex = base - headIdx;
		int startIndex = endIndex - cnt;
		if (startIndex < 0) {
			log.debug("计算出的起始下标 {} 小于0, cnt {} ", startIndex, cnt);
			startIndex = 0;
		}
		List<?> list = Redis.getInstance().lrange(key,
				ChatPct.getDefaultInstance(), startIndex, endIndex);
		return list;
		/*
		 * 假定聊天记录类似于QQ的查看更多（即往前翻），客户端传来自己的基数（base），
		 * 服务器取出库里head的记录，用它的ID和客户端的基数运算，得出base位于列表中的下标M， 再往前N个，作为开始下标，则redis获取
		 * M-N 至 M 之间的记录，发给客户端。 M = base - head.id
		 * 为什么不从末尾计算？计算过程中产生的聊天会是list的末尾下标不断变化，会导致计算不准。
		 */
	}

	public void sendChatLog(int id, Builder builder, IoSession session) {
		log.debug("准备发送聊天记录");
		List<?> list = ChatMgr.getInst().getChatLog(builder);
		log.debug("记录条数：{}", list.size());
		SChatLogList.Builder b = SChatLogList.newBuilder();
		for (Object o : list) {
			ChatPct.Builder cm = (qxmobile.protobuf.Chat.ChatPct.Builder) o;
			b.addLogs(cm);
		}
		session.write(b.build());
		log.debug("发送完毕。");
	}

	public void getSound(int id, IoSession session, Builder builder) {
		CGetYuYing.Builder req = (qxmobile.protobuf.Chat.CGetYuYing.Builder) builder;
		int seq = req.getSeq();
		Channel channel = req.getChannel();
		ChatChLog chatChLog = null;
		
		switch (channel) {
			case SILIAO:
				chatChLog = ChatMgr.inst.chSiLiao;
				break;
			case SHIJIE:
				chatChLog = ChatMgr.inst.chWorld;
				break;
			case Broadcast:
				chatChLog = ChatMgr.inst.chBroadcast;
				break;
			case LIANMENG:
				chatChLog = ChatMgr.inst.chLianMeng;
				break;
			default:
				break;
		}

		String key = chatChLog.key;
		ChatPct.Builder head = (ChatPct.Builder) Redis.getInstance().lindex(key,
				ChatPct.getDefaultInstance(), 0);
		int headIdx = 0;
		if (head != null) {
			headIdx = head.getSeq();
		}
		if (seq < headIdx) {
			// 客户端已经有全部的记录
			log.info("此条记录已删除。");
			return;
		}
		int startIndex = seq - headIdx;
		ChatPct.Builder chatPct = (qxmobile.protobuf.Chat.ChatPct.Builder) Redis.getInstance()
				.lindex(key, ChatPct.getDefaultInstance(), startIndex);
		if(chatPct == null) {
			log.error("请求的语音信息丢失，key:{},seq:{}", key, seq);
			return;
		}
		SGetYuYing.Builder response = SGetYuYing.newBuilder();
		response.setChannel(channel);
		response.setSeq(seq);
		response.setSoundData(chatPct.getSoundData());
		
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_get_sound;
		msg.builder = response;
		session.write(msg);
		log.info("发送语音数据，长度{}", chatPct.getSoundLen());
	}

	/**
	 * 添加进黑名单
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 * @param isSend		是否发送屏蔽消息，当是邮件屏蔽调用该方法就为false
	 * @return
	 */
	public boolean joinBlacklist(int id, IoSession session, Builder builder, boolean isSend) {
		JoinToBlacklist.Builder request = (qxmobile.protobuf.Chat.JoinToBlacklist.Builder) builder;
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return false;
		}
		long blackJunzhuId = request.getJunzhuId();
		BlacklistResp.Builder response = BlacklistResp.newBuilder();
		JunZhu blackJunzhu = HibernateUtil.find(JunZhu.class, blackJunzhuId);
		if (blackJunzhu == null) {
			log.info("要关注的君主id {} 不存在", blackJunzhuId);
			response.setJunzhuId(blackJunzhuId);
			response.setResult(ERROR_JUNZHU_NULL);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_JOIN_BLACKLIST_RESP, response);
			return false;
		}
		long blackNum = Redis.getInstance().scard_(
				CACHE_BLACKLIST_OF_JUNZHU + junzhu.id);
		if (blackNum >= MAX_BlACK_NUM) {// 屏蔽数达到上限
			log.info("屏蔽玩家数达到上限{}人", MAX_BlACK_NUM);
			response.setJunzhuId(blackJunzhuId);
			response.setResult(ERROR_MAXNUM);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_JOIN_BLACKLIST_RESP, response);
			return false;
		}
		if (blackJunzhuId == junzhu.id) {
			log.info("不能屏蔽自己");
			response.setJunzhuId(blackJunzhuId);
			response.setResult(ERROR_SELF);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_JOIN_BLACKLIST_RESP, response);
			return false;
		}
		Boolean exits = Redis.getInstance().sexist(
				CACHE_BLACKLIST_OF_JUNZHU + junzhu.id, "" + blackJunzhuId);
		if (exits) {// 玩家已屏蔽
			log.info("玩家已屏蔽");
			response.setJunzhuId(blackJunzhuId);
			response.setResult(ERROR_EXIST);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_JOIN_BLACKLIST_RESP, response);
			return false;
		}
		Redis.getInstance().sadd(CACHE_BLACKLIST_OF_JUNZHU + junzhu.id,
				"" + blackJunzhuId);
		response.setJunzhuId(blackJunzhuId);
		response.setResult(SUCCESS);
		BlackJunzhuInfo.Builder blackInfo = BlackJunzhuInfo.newBuilder();
		blackInfo.setJunzhuId(blackJunzhuId);
		blackInfo.setName(blackJunzhu.name);
		blackInfo.setLevel(blackJunzhu.level);
		blackInfo.setIconId(blackJunzhu.roleId);
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				blackJunzhu.id);
		if (member == null || member.lianMengId <= 0) {
			blackInfo.setLianMengName("");
		} else {
			AllianceBean alnc = HibernateUtil.find(AllianceBean.class,
					member.lianMengId);
			blackInfo.setLianMengName(alnc == null ? "" : alnc.name);
		}
		blackInfo.setJunXian("1");
		{
			PvpBean bean = HibernateUtil.find(PvpBean.class, blackJunzhu.id);
			if (bean != null) {
//				BaiZhan bz = PvpMgr.inst.baiZhanMap.get(bean.junXianLevel);
//				String jxStr = bz == null ? "???" : HeroService
//						.getNameById(bz.name);
				blackInfo.setJunXian(String.valueOf(bean.junXianLevel));
			}
		}
		blackInfo.setVipLv(blackJunzhu.vipLevel);
		blackInfo.setZhanLi(PvpMgr.inst.getZhanli(blackJunzhu));
		// FIXME 之前跟陈雷庆约定国家为0-6，需要前台统一为1-7
		blackInfo.setGuojia(blackJunzhu.guoJiaId);
		response.setJunzhuInfo(blackInfo.build());
		log.info("{}将{}添加进黑名单", junzhu.name, blackJunzhu.name);
		// 如果是好友，取消好友关注
		double tmpScore = Redis.getInstance().zscore(
				FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + junzhu.id,
				"" + blackJunzhuId);
		Boolean friendExits = tmpScore == -1 ? false : true;
		if (friendExits) {// 如果是关注的好友
			log.info("{}存在{}的好友列表里", blackJunzhu.name, junzhu.name);
			Redis.getInstance().zrem(
					FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + junzhu.id,
					"" + blackJunzhuId);
			log.info("{}取消{}的好友关注", junzhu.name, blackJunzhu.name);
		}
		if(isSend) {
			writeByProtoMsg(session, PD.S_JOIN_BLACKLIST_RESP, response);
		}
		return true;
	}

	/**
	 * 获取黑名单
	 * 
	 * @param cmd
	 * @param session
	 */
	public void getBlackList(int cmd, IoSession session) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		GetBlacklistResp.Builder response = GetBlacklistResp.newBuilder();
		Set<String> ids = Redis.getInstance().sget(
				CACHE_BLACKLIST_OF_JUNZHU + junzhu.id);
		if (ids != null && ids.size() > 0) {
			for (String id : ids) {
				JunZhu blacker = HibernateUtil.find(JunZhu.class,
						Long.parseLong(id));
				BlackJunzhuInfo.Builder bjz = BlackJunzhuInfo.newBuilder();
				bjz.setJunzhuId(blacker.id);
				bjz.setName(blacker.name);
				bjz.setLevel(blacker.level);
				bjz.setIconId(blacker.roleId);
				AlliancePlayer member = HibernateUtil.find(
						AlliancePlayer.class, blacker.id);
				if (member == null || member.lianMengId <= 0) {
					bjz.setLianMengName("");
				} else {
					AllianceBean alnc = HibernateUtil.find(AllianceBean.class,
							member.lianMengId);
					bjz.setLianMengName(alnc == null ? "" : alnc.name);
				}
				bjz.setJunXian("1");
				{
					PvpBean bean = HibernateUtil
							.find(PvpBean.class, blacker.id);
					if (bean != null) {
//						BaiZhan bz = PvpMgr.inst.baiZhanMap
//								.get(bean.junXianLevel);
//						String jxStr = bz == null ? "???" : HeroService
//								.getNameById(bz.name);
						bjz.setJunXian(String.valueOf(bean.junXianLevel));
					}
				}
				bjz.setVipLv(blacker.vipLevel);
				bjz.setZhanLi(PvpMgr.inst.getZhanli(blacker));
				// FIXME 之前跟陈雷庆约定国家为0-6，需要前台统一为1-7
				bjz.setGuojia(blacker.guoJiaId);
				response.addJunzhuInfo(bjz.build());
			}
		}
		response.setBlackMax(MAX_BlACK_NUM);
		// session.write(response.build());
		writeByProtoMsg(session, PD.S_GET_BALCKLIST, response);
	}

	/**
	 * 取消屏蔽
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void cancelBlack(int id, IoSession session, Builder builder) {
		CancelBlack.Builder request = (qxmobile.protobuf.Chat.CancelBlack.Builder) builder;
		long cancelJunzhuId = request.getJunzhuId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		BlacklistResp.Builder response = BlacklistResp.newBuilder();
		JunZhu cancelJunzhu = HibernateUtil.find(JunZhu.class, cancelJunzhuId);
		if (cancelJunzhu == null) {
			log.error("找不到君主,id:{}", cancelJunzhu);
			response.setJunzhuId(cancelJunzhuId);
			response.setResult(ERROR_JUNZHU_NULL);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_CANCEL_BALCK, response);
			return;
		}

		Boolean exits = Redis.getInstance().sexist(
				CACHE_BLACKLIST_OF_JUNZHU + junzhu.id, "" + cancelJunzhuId);
		if (!exits) {
			log.error("君主:{}不在君主:{}的黑名单里", cancelJunzhu.name, junzhu.name);
			response.setJunzhuId(cancelJunzhuId);
			response.setResult(ERROR_NOT_BLACK);
			// session.write(response.build());
			writeByProtoMsg(session, PD.S_CANCEL_BALCK, response);
			return;
		}

		Redis.getInstance().sremove(CACHE_BLACKLIST_OF_JUNZHU + junzhu.id,
				"" + cancelJunzhuId);
		response.setResult(SUCCESS);
		response.setJunzhuId(cancelJunzhuId);
		log.info("{}将{}添加从黑名单中移除", junzhu.name, cancelJunzhu.name);
		writeByProtoMsg(session, PD.S_CANCEL_BALCK, response);
		// session.write(response.build());
	}

	/**
	 * 判断邮件发送方是否在黑名单中
	 * 
	 * @param senderName
	 * @return
	 */
	private boolean isSenderBlack(long receiverId, String senderName) {
		log.info("判断邮件发送方是否在黑名单中,发送人为{}", senderName);
		JunZhu sender = HibernateUtil.findByName(JunZhu.class, senderName,
				" where name='" + senderName + "'");
		return Redis.getInstance().sexist(
				ChatMgr.CACHE_BLACKLIST_OF_JUNZHU + receiverId,
				String.valueOf(sender.id));
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	private void writeByProtoMsg(IoSession session, int prototype,
			Builder response) {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = prototype;
		msg.builder = response;
		log.info("发送协议号为：{}", prototype);
		session.write(msg);
	}

	public void getRecentContacts(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		RecentContacts.Builder response = RecentContacts.newBuilder();
		List<String> recentContactList = Redis.getInstance().lgetList(CACHE_RECENT_CONTACTS + junzhu.id);
		for(String jzIdStr : recentContactList) {
			long jzId = Long.parseLong(jzIdStr);
			JunZhu contact = HibernateUtil.find(JunZhu.class, jzId);
			if(contact == null) {
				log.error("获取最近联系人错误，找不到君主id为:{}的君主", jzId);
				continue;
			}
			ContactsJunzhuInfo.Builder contactInfo = ContactsJunzhuInfo.newBuilder();
			contactInfo.setJunzhuId(contact.id);
			contactInfo.setName(contact.name);
			contactInfo.setLevel(contact.level);
			contactInfo.setIconId(contact.roleId);
			response.addJunzhuInfo(contactInfo);
		}
		session.write(response.build());
	}
	
	/**
	 * @Description: 屏蔽
	 * @param list
	 * @param chatString
	 * @return
	 */
	public String replaceIllegal2(String chatString) {
		log.info("处理前发送聊天内容为--{}", chatString);
		List<String> senNameList = BigSwitch.inst.accMgr.getSensitiveWord();
		for (String s : senNameList) {
			if (chatString != null && chatString.contains(s)) {
				chatString = chatString.replace(s, "***");
			}
		}
		return chatString;
	}

	/**
	 * @Description:当字数比较少的时候（指一两百字）较快； 几十个字两种方法无太大区别，上千字是第一种方法效率高
	 * @param txt
	 * @return
	 */
	public String replaceIllegal(String chatString) {
		sFilter = SensitiveFilter.getInstance();
		log.info("处理前发送聊天内容为--{}", chatString);
		chatString = SensitiveFilter.instance.replaceSensitiveWord(chatString,
				2, "*");
		return chatString;
	}

	public void setChatSettings(int id, Builder builder, IoSession session) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("设置聊天设置出错，找不到君主");
			return;
		}
		ChatSetting chatSetting = getChatSetting(junzhu.id);
		ChatSettings.Builder request = (qxmobile.protobuf.Chat.ChatSettings.Builder) builder;
		chatSetting.wolrd = request.getWorld();
		chatSetting.lianMeng = request.getLianMeng();
		chatSetting.siLiao = request.getSiLiao();
		chatSetting.wifiAutoPlayer = request.getWifiAutoPlay();
		HibernateUtil.save(chatSetting);
		session.write(request.build());
	}

	private ChatSetting getChatSetting(long junzhuId) {
		ChatSetting chatSetting = HibernateUtil.find(ChatSetting.class, junzhuId);
		if(chatSetting == null) {
			chatSetting = new ChatSetting();
			chatSetting.junZhuId = junzhuId;
			chatSetting.wolrd = true;
			chatSetting.lianMeng = true;
			chatSetting.siLiao = true;
			chatSetting.wifiAutoPlayer = true;
			HibernateUtil.insert(chatSetting);
		}
		return chatSetting;
	}

	public void getChatSettings(int id, Builder builder, IoSession session) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("设置聊天设置出错，找不到君主");
			return;
		}
		ChatSetting chatSetting = getChatSetting(junzhu.id);
		ChatSettings.Builder response = ChatSettings.newBuilder();
		response.setWorld(chatSetting.wolrd);
		response.setSiLiao(chatSetting.siLiao);
		response.setLianMeng(chatSetting.lianMeng);
		response.setWifiAutoPlay(chatSetting.wifiAutoPlayer);
		session.write(response.build());
	}
}
