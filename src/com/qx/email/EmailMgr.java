package com.qx.email;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import log.ActLog;
import log.OurLog;
import net.sf.json.JSONArray;

import org.apache.commons.lang.time.DateUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Chat.JoinToBlacklist;
import qxmobile.protobuf.EmailProtos.DeleteEmailResp;
import qxmobile.protobuf.EmailProtos.EmailGoods;
import qxmobile.protobuf.EmailProtos.EmailInfo;
import qxmobile.protobuf.EmailProtos.EmailListResponse;
import qxmobile.protobuf.EmailProtos.EmailResponse;
import qxmobile.protobuf.EmailProtos.EmailResponseResult;
import qxmobile.protobuf.EmailProtos.GetRewardRequest;
import qxmobile.protobuf.EmailProtos.GetRewardResponse;
import qxmobile.protobuf.EmailProtos.NewMailResponse;
import qxmobile.protobuf.EmailProtos.ReadEmail;
import qxmobile.protobuf.EmailProtos.ReadEmailResp;
import qxmobile.protobuf.EmailProtos.SendEmail;
import qxmobile.protobuf.EmailProtos.SendEmailResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.Mail;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.gm.role.GMRoleMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.world.Mission;

public class EmailMgr extends EventProc implements Runnable {
	public static EmailMgr INSTANCE = null;
	public ThreadLocal<Long> receiverJzId = new ThreadLocal<Long>();
	public static Logger log = LoggerFactory.getLogger(EmailMgr.class);
	public static int EMAIL_PLAYER = 80000;
	public static final int READED_TRUE = 1;
	public static final int READED_FALSE = 0;

	public static final int DELETE_FALSE = 1;
	public static final int DELETE_TRUE = 2;

	public static final int GET_REWARD_FALSE = 0;
	public static final int GET_REWARD_TRUE = 1;

	/** 阅后即删 **/
	public static int OPER_READ_DELETE = 1;
	/** 领取即删 **/
	public static int OPER_GET_DELETE = 2;
	/** 操作即删 **/
	public static int OPER_HANDLE_DELETE = 3;

	/** 发送邮件冷却时间，单位-毫秒 **/
	public static int EMAIL_COLD_TIME = 1 * 60 * 1000;

	public static ThreadLocal<Email> sentMail = new ThreadLocal<Email>();
	protected SimpleDateFormat simpleDateFormat;

	public ConcurrentLinkedQueue<Mission> missions = new ConcurrentLinkedQueue<Mission>();
	public Map<Integer, Mail> mailConfigMap;

	public EmailMgr() {
		simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd");
		INSTANCE = this;
		initData();
	}

	public void initData() {
		List<Mail> mailConfigList = TempletService.listAll(Mail.class
				.getSimpleName());
		Map<Integer, Mail> mailConfigMap = new HashMap<Integer, Mail>();
		for (Mail m : mailConfigList) {
			mailConfigMap.put(m.type, m);
		}
		this.mailConfigMap = mailConfigMap;
	}

	public Mail getMailConfig(int type) {
		Mail mailCfg = mailConfigMap.get(type);
		if (mailCfg == null) {
			log.error("mail.xml配置文件找不到type={}的数据", type);
		}
		return mailCfg;
	}

	public void exec(int id, IoSession session, Builder builder) {
		Mission mission = new Mission(id, session, builder);
		missions.add(mission);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			Mission mission = missions.poll();
			if (mission == null) {
				try {
					Thread.sleep(100);
				} catch (Throwable e) {
					log.error("线程休眠异常", e);
				}
			} else {
				try {
					completeMission(mission);
				} catch (Throwable e) {
					log.error("completeMission error {}", mission.code);
					log.error("run方法异常", e);
				}
			}
		}
	}

	protected void completeMission(Mission mission) {
		if (mission == null) {
			log.error("mission is null...");
			return;
		}

		int code = mission.code;
		Builder builder = mission.builer;
		IoSession session = mission.session;
		switch (code) {
		// case PD.Mail:
		// Mail.Builder mail = (Mail.Builder)builder;
		// this.writeMail(session, mail);
		// break;
		//
		// default:
		// break;
		}
	}

	public synchronized void sendEmail(int cmd, IoSession session, Builder builder) {
		SendEmail.Builder request = (qxmobile.protobuf.EmailProtos.SendEmail.Builder) builder;
		String receiverName = request.getReceiverName();
		String content = request.getContent();
		// 0-发送成功，1-失败，收件人名空，2-失败，内容为空，3-失败，找不到玩家 、
		// 4-失败，有非法字符，5-你被对方屏蔽，6-不能给自己发,7-间隔时间不到1分钟
		JunZhu sender = JunZhuMgr.inst.getJunZhu(session);
		if (sender == null) {
			log.error("未发现君主");
			return;
		}
		// 2015年7月3日 16:59 检查账号是否处于禁言状态，封停则禁止发邮件
		if (GMRoleMgr.checkGMJinyan(sender.id)) {
			log.error("发件人{}已经被禁言", sender.id);
			return;
		}
		if (receiverName == null || receiverName.equals("")) {
			sendSendEmailResp(session, 1);
			return;
		}
		JunZhu receiver = HibernateUtil.findByName(JunZhu.class, receiverName,
				" where name='" + receiverName + "'");
		if (receiver == null) {
			sendSendEmailResp(session, 3);
			return;
		}
		boolean isExitsBlack = Redis.getInstance()
				.sexist(ChatMgr.CACHE_BLACKLIST_OF_JUNZHU + receiver.id,
						"" + sender.id);
		if (isExitsBlack) { // 我在对方的黑名单中
			sendSendEmailResp(session, 5);
			return;
		}
		isExitsBlack = Redis.getInstance()
				.sexist(ChatMgr.CACHE_BLACKLIST_OF_JUNZHU + sender.id,
						"" + receiver.id);
		if (isExitsBlack) { // 对方在我的黑名单中
			sendSendEmailResp(session, 8);
			return;
		}
		if (sender.id == receiver.id) {
			sendSendEmailResp(session, 6);
			return;
		}
		if (content == null || content.equals("")) {
			sendSendEmailResp(session, 2);
			return;
		}
		Map<Long, Long> coldTimeMap = (Map<Long, Long>) session
				.getAttribute(SessionAttKey.LAST_SEND_EMAIL_KEY);
		if (coldTimeMap == null) {
			coldTimeMap = new HashMap<Long, Long>();
			session.setAttribute(SessionAttKey.LAST_SEND_EMAIL_KEY, coldTimeMap);
		}
		Long cdTime = coldTimeMap.get(receiver.id);
		long curTime = System.currentTimeMillis();
		if (cdTime == null || cdTime <= curTime) {
			coldTimeMap.put(receiver.id, curTime + EMAIL_COLD_TIME);
		} else {
			sendSendEmailResp(session, 7);
			return;
		}
		// TODO 非法字符
		Mail mailConfig = getMailConfig(EMAIL_PLAYER);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type=" + EMAIL_PLAYER + "的数据, cmd:{}",
					cmd);
			return;
		}
		boolean isSuccess = sendMail(receiverName, content, "", sender.id,sender.name,
				mailConfig, "");
		if (isSuccess) {
			sendSendEmailResp(session, 0);
			String jzId = String.valueOf(sender.id);
			OurLog.log.SnsFlow(jzId, 1, 4, 0, jzId);
			ActLog.log.EmailLog(sender.id, sender.name, ActLog.vopenid, 2, 
					ActLog.vopenid, receiverJzId.get().longValue(), receiverName, "Title", content, new JSONArray());
		} else {
			log.error("玩家邮件发送失败！！");
		}

	}

	protected void sendSendEmailResp(IoSession session, int result) {
		SendEmailResp.Builder response = SendEmailResp.newBuilder();
		response.setResult(result);
		session.write(response.build());
	}
	public boolean sendMail(String revName, String content, String fujian,
			String senderName, Mail mailConfig, String param) {
		return sendMail(revName, content, fujian, -1, senderName, mailConfig, param);
	}
	/**
	 * 发送邮件
	 * 
	 * @param revName
	 * @param content
	 * @param fujian
	 * @param senderName
	 * @param mailConfig
	 * @param param
	 *            如果该类型邮件不需要设置参数，则传""或null
	 * @return
	 */
	public boolean sendMail(String revName, String content, String fujian,
			long senderId,String senderName, Mail mailConfig, String param) {
		receiverJzId.set(0L);
		sentMail.remove();
		if (revName == null || revName.equals("")) {
			log.error("revName收件人姓名不能为空");
			return false;
		}
		if (content == null || content.equals("")) {
			log.error("content内容不能为空");
			return false;
		}
		if (senderName == null || senderName.equals("")) {
			log.error("senderName发件人姓名不能为空");
			return false;
		}
		JunZhu receiver = HibernateUtil.findByName(JunZhu.class, revName,
				" where name='" + revName + "'");
		if (receiver == null) {
			log.error("not found receiver junZhu by name : " + revName);
			return false;
		}
		receiverJzId.set(receiver.id);
		Date sendTime = new Date();
		Email email = new Email();
		// 改自增主键为指定
		// 2015年4月17日16:57:30int改为long
		long eId = TableIDCreator.getTableID(Email.class, 1L);
		email.setId(eId);
		email.senderJzId = senderId;
		email.setReceiverId(receiver.id);
		email.setContent(content);
		email.setTitle(mailConfig.title);
		email.setGoods(fujian);
		email.setIsDelete(DELETE_FALSE);
		email.setSenderName(senderName);
		email.sendTime = sendTime;
		email.type = mailConfig.type;
		email.isReaded = READED_FALSE;
		email.isGetReward = GET_REWARD_FALSE;
		email.taitou = mailConfig.taitou == null ? "" : mailConfig.taitou;
		email.param = param == null ? "" : param;
		if (mailConfig.remainTime > 0) {
			email.expireTime = DateUtils.addDays(sendTime,
					mailConfig.remainTime);
		}
		HibernateUtil.save(email);
		sentMail.set(email);
		log.info("{} 发送给 {} 一封邮件，邮件id：{}, 时间{}", senderName, revName,
				email.getId(), sendTime);

		IoSession revSession = AccountManager.getIoSession(receiver.id);
		if (revSession != null) {
			NewMailResponse.Builder newResp = NewMailResponse.newBuilder();
			EmailInfo.Builder emailInfo = EmailInfo.newBuilder();
			emailInfo.setId(email.getId());
			emailInfo.setTitle(mailConfig.title);
			emailInfo.setContent(content);
			emailInfo.setSenderName(senderName);
			emailInfo.setType(mailConfig.type);
			emailInfo.setTime(email.sendTime.getTime());
			emailInfo.setIsRead(READED_FALSE);
			emailInfo.setTaiTou(email.taitou == null ? "" : email.taitou);
			if (email.type == EMAIL_PLAYER) {
				String sql = " where name='" + email.getSenderName() + "'";
				JunZhu junzhu = HibernateUtil.findByName(JunZhu.class,
						email.getSenderName(), sql);
				if (junzhu == null) {
					log.error("没有找到君主 {}", email.getSenderName());
				} else {
					//	2015年8月28日 optional int64 jzId = 11;//私信用到junzhuID
					emailInfo.setJzId(senderId);
					emailInfo.setRoleId(junzhu.roleId);
				}
			}
			
			try {
				if (fujian != null && !fujian.equals("")) {
					String[] goodsList = fujian.split("#");
					for (String goodsInfo : goodsList) {
						String[] goods = goodsInfo.split(":");
						EmailGoods.Builder goodsBuilder = EmailGoods
								.newBuilder();
						goodsBuilder.setType(Integer.parseInt(goods[0]));
						goodsBuilder.setId(Integer.parseInt(goods[1]));
						goodsBuilder.setCount(Integer.parseInt(goods[2]));
						emailInfo.addGoodsList(goodsBuilder.build());
					}
				}
			} catch (Exception e) {
				log.error("邮件附件的格式不正确：附件:{}", fujian);
			}
			newResp.setEmail(emailInfo.build());
			revSession.write(newResp.build());
		} else {
			log.error("找不到相对应的 IoSession，junzhuId:{}, 可能是从jsp页面上访问该方法",
					receiver.id);
		}
		return true;
	}

	protected void sendError(IoSession session, int code, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(code);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	/**
	 * 请求邮件列表
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void requestMailList(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("not found sender junZhu");
			return;
		}
		EmailListResponse.Builder response = EmailListResponse.newBuilder();
		List<Email> emailList = HibernateUtil.list(Email.class,
				" where receiverId = " + junZhu.id + " and isDelete = "
						+ DELETE_FALSE);
		Date curTime = new Date();
		for (Email mail : emailList) {
			if (mail.expireTime != null && mail.sendTime != mail.expireTime
					&& curTime.after(mail.expireTime)) {
				mail.setIsDelete(DELETE_TRUE);
				HibernateUtil.save(mail);
				continue;
			}
			EmailInfo.Builder emailInfo = EmailInfo.newBuilder();
			emailInfo.setId(mail.getId());
			emailInfo.setType(mail.type);
			if (mail.type == EMAIL_PLAYER) {
				String sql = " where name='" + mail.getSenderName() + "'";
				JunZhu junzhu = HibernateUtil.findByName(JunZhu.class,
						mail.getSenderName(), sql);
				if (junzhu == null) {
					log.error("没有找到君主 {}", mail.getSenderName());
					continue;
				}
				//	2015年8月28日 optional int64 jzId = 11;//私信用到junzhuID
				emailInfo.setJzId(junzhu.id);
				emailInfo.setRoleId(junzhu.roleId);
			}
			if (isSenderBlack(junZhu.id, mail.senderJzId)) {// 如果发送人在君主屏蔽列表内
				continue;// 不添加此封邮件到列表
			}
			emailInfo.setSenderName(mail.getSenderName());
			emailInfo.setTitle(mail.getTitle());
			emailInfo.setContent(mail.getContent());
			emailInfo.setTime(mail.sendTime.getTime());
			emailInfo.setIsRead(mail.isReaded);
			emailInfo.setTaiTou(mail.taitou == null ? "" : mail.taitou);
			String goodsList = mail.getGoods();
			try {
				if (goodsList != null && !goodsList.equals("")) {
					String[] goods = goodsList.split("#");
					for (String g : goods) {
						String[] ginfo = g.split(":");
						EmailGoods.Builder emailGoods = EmailGoods.newBuilder();
						emailGoods.setType(Integer.parseInt(ginfo[0]));
						emailGoods.setId(Integer.parseInt(ginfo[1]));
						emailGoods.setCount(Integer.parseInt(ginfo[2]));
						emailInfo.addGoodsList(emailGoods.build());
					}
				}
				response.addEmailList(emailInfo.build());

			} catch (Exception e) {
				log.error("邮件附件的格式不正确：附件:{}", goodsList);
			}
		}

		log.info("{} 请求了邮件列表", junZhu.name);
		session.write(response.build());
	}

	/**
	 * 判断邮件发送方是否在黑名单中
	 * 
	 * @param senderName
	 * @return
	 */
	protected boolean isSenderBlack(long receiverId, long senderid) {
			return Redis.getInstance().sexist(
					ChatMgr.CACHE_BLACKLIST_OF_JUNZHU + receiverId,
					String.valueOf(senderid));
	}

	/**
	 * 领取邮件附件
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void getRewardRequest(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("not found sender junZhu");
			return;
		}
		GetRewardRequest.Builder request = (qxmobile.protobuf.EmailProtos.GetRewardRequest.Builder) builder;
		long emailId = request.getId();
		Email email = HibernateUtil.find(Email.class, emailId);
		if (email == null || email.getIsDelete() == DELETE_TRUE) {
			log.error("找不到对应的邮件，邮件id:{},junzhuId:{}", emailId, junZhu.id);
			sendGetRewardResponse(session, emailId, 1);
			return;
		}
		String goods = email.getGoods();
		if (goods == null || goods.equals("")) {
			log.error("该邮件没有附件可以领取，emailId:{}", emailId);
			sendGetRewardResponse(session, emailId, 2);
			return;
		}
		if (email.isGetReward == GET_REWARD_TRUE) {
			log.error("该邮件已经领取奖励，emailId:{}", emailId);
			sendGetRewardResponse(session, emailId, 2);
			return;
		}
		String[] goodsArray = goods.split("#");
		for (String g : goodsArray) {
			String[] ginfo = g.split(":");
			AwardTemp award = new AwardTemp();
			award.setItemType(Integer.parseInt(ginfo[0]));
			award.setItemId(Integer.parseInt(ginfo[1]));
			award.setItemNum(Integer.parseInt(ginfo[2]));
			AwardMgr.inst.giveReward(session, award, junZhu);
		}
		// 删除邮件
		email.setIsDelete(DELETE_TRUE);
		email.isGetReward = GET_REWARD_TRUE;
		log.info("邮件id:{}领取奖励成功，并删除", emailId);
		HibernateUtil.save(email);
		sendGetRewardResponse(session, emailId, 0);
		sendDeleteEmailNotify(session, emailId);
	}

	protected void sendDeleteEmailNotify(IoSession session, long emailId) {
		DeleteEmailResp.Builder response = DeleteEmailResp.newBuilder();
		response.setId(emailId);
		session.write(response.build());
	}

	protected void sendGetRewardResponse(IoSession session, long emailId,
			int result) {
		GetRewardResponse.Builder response = GetRewardResponse.newBuilder();
		response.setIsSuccess(result);
		response.setId(emailId);
		session.write(response.build());
	}

	public void markReadedEmail(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("not found sender junZhu");
			return;
		}
		ReadEmail.Builder request = (qxmobile.protobuf.EmailProtos.ReadEmail.Builder) builder;
		long emailId = request.getId();

		ReadEmailResp.Builder response = ReadEmailResp.newBuilder();
		Email email = HibernateUtil.find(Email.class, emailId);
		if (email == null) {
			log.error("要标记为已读的邮件不存在，emailId:{}", emailId);
			response.setResult(1);
			session.write(response.build());
			return;
		}
		Mail mailConfig = getMailConfig(email.type);
		if (mailConfig == null) {
			return;
		}
		response.setResult(0);
		response.setEmailId(emailId);
		session.write(response.build());
		email.isReaded = READED_TRUE;
		if (mailConfig.mailType == OPER_READ_DELETE) {
			email.setIsDelete(DELETE_TRUE);
			sendDeleteEmailNotify(session, emailId);
			log.info("邮件id:{}为阅后即焚类型，在此删除", emailId);
		}
		if (email.type == EMAIL_PLAYER) {
			email.expireTime = DateUtils.addDays(email.sendTime, 7);
		}
		HibernateUtil.save(email);
	}

	public void emailResponse(int cmd, IoSession session, Builder builder) {
		EmailResponse.Builder request = (qxmobile.protobuf.EmailProtos.EmailResponse.Builder) builder;
		int operCode = request.getOperCode();
		long emailId = request.getEmailId();

		EmailResponseResult.Builder response = EmailResponseResult.newBuilder();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("not found sender junZhu");
			return;
		}
		Email email = HibernateUtil.find(Email.class, emailId);
		if (email == null) {
			log.error("要删除的邮件不存在，emailId:{}", emailId);
			response.setIsSuccess(1);
			session.write(response.build());
			return;
		}
		switch (operCode) {
		case 1:// 同意
		case 2:// 拒绝
			dealMailOper(session, junZhu, email, operCode);
			break;
		case 5:// 屏蔽玩家
			JoinToBlacklist.Builder blackReq = JoinToBlacklist.newBuilder();
			JunZhu blackJunzhu = HibernateUtil.findByName(JunZhu.class,
					email.getSenderName(),
					" where name='" + email.getSenderName() + "'");
			blackReq.setJunzhuId(blackJunzhu.id);
			boolean isSuccess = ChatMgr.inst.joinBlacklist(cmd, session, blackReq, false);
			if (!isSuccess) {
				response.setIsSuccess(1);
			} else {
				response.setIsSuccess(0);
			}
			response.setEmailId(emailId);
			session.write(response.build());
			break;
		default:
			log.error("错误的邮件操作类型");
			break;
		}
	}

	public void dealMailOper(IoSession session, JunZhu junZhu, Email email,
			int operCode) {
		switch (email.type) {
		case 10015:// 待售房屋交易
			BigSwitch.inst.houseMgr.answerApply(session, junZhu, email,
					operCode);
			break;
		case 10004:// 强售房屋交易
			BigSwitch.inst.houseMgr.leaderAnswerApply(session, junZhu, email,
					operCode);
			break;
		}
	}

	@Override
	public void proc(Event e) {
		switch (e.id) {
			case ED.REFRESH_TIME_WORK:
				IoSession session = (IoSession) e.param;
				if(session == null){
					break;
				}
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				if(jz == null){
					break;
				}
				List<Email> emailList = HibernateUtil.list(Email.class,
						" where receiverId = " + jz.id + " and isDelete = "
								+ DELETE_FALSE + " and isReaded = 0"); 
				boolean hasSystemEmail = false;
				boolean hasPersonEmail = false;
				for (Email mail : emailList) {
					// 判断是系统邮件 或者 非黑名单的私信（需显示）
					if(mail.senderJzId == -1){
						hasSystemEmail = true;
					}else if(!isSenderBlack(jz.id, mail.senderJzId)){
						hasPersonEmail = true;
					}
					if(hasSystemEmail && hasPersonEmail){
						break;
					}
				}
				if(hasSystemEmail){
					// 发送系统邮件未读提示
					FunctionID.pushCanShangjiao(jz.id, session, FunctionID.youxiang_system);
				}
				if(hasPersonEmail){
					// 发送私信邮件未读提示
					FunctionID.pushCanShangjiao(jz.id, session, FunctionID.youxiang_person);
				}
				break;
			}
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

}
