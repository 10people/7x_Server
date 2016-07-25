package com.qx.gm.email;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.core.servlet.GMServlet;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CommonItem;
import com.manu.dynasty.template.Mail;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.DoSendBareMailReq;
import com.qx.gm.message.DoSendMailReq;
import com.qx.gm.message.MailProp;
import com.qx.gm.role.GMRoleMgr;
import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.util.TableIDCreator;

/**
 * @ClassName: GMEmailMgr
 * @Description: GM邮件
 * @author 何金成
 * @date 2015年7月4日 下午6:12:04
 * 
 */
public class GMEmailMgr extends EventProc {
	public static GMEmailMgr inst;
	public Logger logger = LoggerFactory.getLogger(GMEmailMgr.class);
	public static final int GM_MAIL_TYPE = 60002;// GM带附件邮件类型
	public static final int GM_BARE_MAIL_TYPE = 60001;// GM普通邮件类型
	public static Map<Integer, AwardTemp> awardMap = new HashMap<Integer, AwardTemp>();
	public static Map<Integer, CommonItem> commonItemMap = new HashMap<Integer, CommonItem>();

	public GMEmailMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		// 加载首冲奖励配置文件
		List<AwardTemp> awardList = TempletService.listAll(AwardTemp.class
				.getSimpleName());
		for (AwardTemp award : awardList) {
			awardMap.put(award.itemId, award);
		}
		List<CommonItem> commonItems = TempletService.listAll(CommonItem.class
				.getSimpleName());
		for (CommonItem commonItem : commonItems) {
			commonItemMap.put(commonItem.id, commonItem);
		}
	}

	/**
	 * @param prop4Md5
	 * @Title: doSendMail
	 * @Description: 发送带附件邮件请求（邮件发道具）
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doSendMail(DoSendMailReq request, String prop4Md5,
			PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5(prop4Md5)) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		final List<String> revNames = new ArrayList<String>();
		String roleNames = request.getRolename();
		if (roleNames.length() != 0) {// 按角色名发送
			if (roleNames.contains("，")) {// 分隔符用错，必须用英文逗号
				response.setCode(CodeUtil.SPLIT_ERROR);
				GMServlet.write(response, writer);
				return;
			}
			if (roleNames.contains(",")) {// 多个角色名
				for (String roleName : roleNames.split(",")) {
					JunZhu receiver = HibernateUtil.findByName(JunZhu.class,
							roleName, " where name='" + roleName + "'");
					if (null == receiver) {
						response.setCode(CodeUtil.NONE_JUNZHU);
						GMServlet.write(response, writer);
						return;
					}
					if (!String.valueOf(receiver.id).endsWith(
							String.valueOf(GameServer.serverId))) {// 非本服玩家
						response.setCode(CodeUtil.NONE_JUNZHU);
						GMServlet.write(response, writer);
						return;
					}
					revNames.add(roleName);
				}
			} else {// 只有一个角色名
				JunZhu receiver = HibernateUtil.findByName(JunZhu.class,
						roleNames, " where name='" + roleNames + "'");
				if (null == receiver) {
					response.setCode(CodeUtil.NONE_JUNZHU);
					GMServlet.write(response, writer);
					return;
				}
				if (!String.valueOf(receiver.id).endsWith(
						String.valueOf(GameServer.serverId))) {// 非本服玩家
					response.setCode(CodeUtil.NONE_JUNZHU);
					GMServlet.write(response, writer);
					return;
				}
				revNames.add(roleNames);
			}
		}

		final String content = request.getText();
		int zone = request.getZone();
		int levelMin = request.getLevlemin();
		int levelMax = request.getLevlemax();

		StringBuilder sBuilder = new StringBuilder();

		for (MailProp prop : request.getProp()) {
			if (prop.getItemcount() == 0) {
				response.setCode(CodeUtil.FUJIN_COUNT_ERROR);
				GMServlet.write(response, writer);
				return;
			}
			AwardTemp tmp = awardMap.get(prop.getItemId());
			if (tmp == null) {
				// BaseItem o = TempletService.itemMap.get(prop.getItemId());
				CommonItem o = commonItemMap.get(prop.getItemId());
				sBuilder.append(o.itemType);
			} else {
				sBuilder.append(awardMap.get(prop.getItemId()).itemType);
			}
			sBuilder.append(":" + prop.getItemId());
			sBuilder.append(":" + prop.getItemcount() + "#");
		}
		final String fujian = sBuilder.substring(0, sBuilder.length() - 1);// 去掉最后一个“#”

		final Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(GM_MAIL_TYPE);
		mailConfig.title = request.getSubject();
		final String param = "";

		// 邮件模板
		final GMEMailInfo tmp = new GMEMailInfo();
		tmp.id = TableIDCreator.getTableID(GMEMailInfo.class, 1);
		tmp.name = request.getRolename();
		tmp.minLevel = request.getLevlemin();
		tmp.maxLevel = request.getLevlemax();
		tmp.title = request.getSubject();
		tmp.content = content;
		tmp.fujian = fujian;
		tmp.sendDate = new Date();
		tmp.mailType = GM_MAIL_TYPE;
		HibernateUtil.insert(tmp);
		final GMEMailInfo mailInfo = tmp;

		if (roleNames.length() != 0) {// 按角色名发送
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (String revName : revNames) {
						JunZhu junZhu = HibernateUtil.findByName(JunZhu.class,
								revName, "where name='" + revName + "'");
						if (GMRoleMgr.inst.isJunzhuOnline(junZhu) == 0) {// 不在线不发送
							continue;
						}
						EmailMgr.INSTANCE.sendMail(revName, content, fujian,
								mailConfig.sender, mailConfig, param);
						GMEmailSendRecord sendRecord = new GMEmailSendRecord();
						sendRecord.mailId = mailInfo.id;
						sendRecord.jzId = junZhu.id;
						HibernateUtil.insert(sendRecord);
					}
					logger.info("gm doSendEmail:所有邮件全部发送完成");
				}
			}).start();
		} else {// 按等级发送
			final List<JunZhu> jzList = HibernateUtil
					.list(JunZhu.class, "where level between " + levelMin
							+ " and " + levelMax + "");
			if (jzList == null || jzList.size() == 0) {
				response.setCode(CodeUtil.NONE_JUNZHU);
				GMServlet.write(response, writer);
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (JunZhu junZhu : jzList) {
						if (GMRoleMgr.inst.isJunzhuOnline(junZhu) == 0) {// 不在线不发送
							continue;
						}
						EmailMgr.INSTANCE.sendMail(junZhu.name, content,
								fujian, mailConfig.sender, mailConfig, param);
						GMEmailSendRecord sendRecord = new GMEmailSendRecord();
						sendRecord.mailId = mailInfo.id;
						sendRecord.jzId = junZhu.id;
						HibernateUtil.insert(sendRecord);
					}
					logger.info("gm doSendEmail:所有邮件全部发送完成");
				}
			}).start();
		}
		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
	}

	/**
	 * @Title: doSendBareMail
	 * @Description: 发送普通邮件
	 * @param request
	 * @param writer
	 * @return void
	 * @throws
	 */
	public void doSendBareMail(DoSendBareMailReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		// MD5验证
		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		final List<String> revNames = new ArrayList<String>();
		String roleNames = request.getRolename();
		if (roleNames.length() != 0) {// 按角色名发送
			if (roleNames.contains("，")) {// 分隔符用错，必须用英文逗号
				response.setCode(CodeUtil.SPLIT_ERROR);
				GMServlet.write(response, writer);
				return;
			}
			if (roleNames.contains(",")) {// 多个角色名
				for (String roleName : roleNames.split(",")) {
					JunZhu receiver = HibernateUtil.findByName(JunZhu.class,
							roleName, " where name='" + roleName + "'");
					if (null == receiver) {
						response.setCode(CodeUtil.NONE_JUNZHU);
						GMServlet.write(response, writer);
						return;
					}
					if (!String.valueOf(receiver.id).endsWith(
							String.valueOf(GameServer.serverId))) {// 非本服玩家
						response.setCode(CodeUtil.NONE_JUNZHU);
						GMServlet.write(response, writer);
						return;
					}
					revNames.add(roleName);
				}
			} else {// 只有一个角色名
				JunZhu receiver = HibernateUtil.findByName(JunZhu.class,
						roleNames, " where name='" + roleNames + "'");
				if (null == receiver) {
					response.setCode(CodeUtil.NONE_JUNZHU);
					GMServlet.write(response, writer);
					return;
				}
				if (!String.valueOf(receiver.id).endsWith(
						String.valueOf(GameServer.serverId))) {// 非本服玩家
					response.setCode(CodeUtil.NONE_JUNZHU);
					GMServlet.write(response, writer);
					return;
				}
				revNames.add(roleNames);
			}
		}

		final String content = request.getText();
		int zone = request.getZone();
		int levelMin = request.getLevlemin();
		int levelMax = request.getLevlemax();
		String subject = request.getSubject();
		final Mail mailConfig = EmailMgr.INSTANCE
				.getMailConfig(GM_BARE_MAIL_TYPE);
		mailConfig.title = subject;
		final String param = "";

		// 邮件模板
		final GMEMailInfo tmp = new GMEMailInfo();
		tmp.id = TableIDCreator.getTableID(GMEMailInfo.class, 1);
		tmp.name = request.getRolename();
		tmp.minLevel = request.getLevlemin();
		tmp.maxLevel = request.getLevlemax();
		tmp.title = request.getSubject();
		tmp.content = content;
		tmp.fujian = "";
		tmp.sendDate = new Date();
		tmp.mailType = GM_BARE_MAIL_TYPE;
		HibernateUtil.insert(tmp);
		final GMEMailInfo mailInfo = tmp;

		if (roleNames.length() != 0) {// 按角色名发送
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (String revName : revNames) {
						JunZhu junZhu = HibernateUtil.findByName(JunZhu.class,
								revName, "where name='" + revName + "'");
						if (GMRoleMgr.inst.isJunzhuOnline(junZhu) == 0) {// 不在线不发送
							continue;
						}
						EmailMgr.INSTANCE.sendMail(revName, content, "",
								mailConfig.sender, mailConfig, param);
						GMEmailSendRecord sendRecord = new GMEmailSendRecord();
						sendRecord.mailId = mailInfo.id;
						sendRecord.jzId = junZhu.id;
						HibernateUtil.insert(sendRecord);
					}
					logger.info("gm doSendBareMail:所有邮件全部发送完成");
				}
			}).start();
		} else {// 按等级发送
			final List<JunZhu> jzList = HibernateUtil
					.list(JunZhu.class, "where level between " + levelMin
							+ " and " + levelMax + "");
			if (jzList == null || jzList.size() == 0) {
				response.setCode(CodeUtil.NONE_JUNZHU);
				GMServlet.write(response, writer);
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (JunZhu junZhu : jzList) {
						if (GMRoleMgr.inst.isJunzhuOnline(junZhu) == 0) {// 不在线不发送
							continue;
						}
						EmailMgr.INSTANCE.sendMail(junZhu.name, content, "",
								mailConfig.sender, mailConfig, param);
						GMEmailSendRecord sendRecord = new GMEmailSendRecord();
						sendRecord.mailId = mailInfo.id;
						sendRecord.jzId = junZhu.id;
						HibernateUtil.insert(sendRecord);
					}
					logger.info("gm doSendBareMail:所有邮件全部发送完成");
				}
			}).start();
		}
		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
	}

	public void sendGMEMail(final long jzId) {
		final JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (null == jz) {
			logger.info("发送离线邮件事件参数错误，君主不存在");
			return;
		}
		final List<GMEMailInfo> mailList = HibernateUtil.list(
				GMEMailInfo.class, "");
		if (mailList == null) {
			return;
		}
		for (GMEMailInfo mail : mailList) {
			GMEmailSendRecord sendRecord = HibernateUtil.find(
					GMEmailSendRecord.class, "where mailId=" + mail.id
							+ " and jzId=" + jzId + "");
			if (sendRecord == null) {
				PlayerTime playerTime = HibernateUtil.find(PlayerTime.class,
						jzId);
				if (null == playerTime) {
					playerTime = new PlayerTime(jzId);
					Cache.playerTimeCache.put(jzId, playerTime);
					return;
				}
				if (compareDate(mail.sendDate,
						playerTime.createRoleTime) >= 0) {
					//if (Arrays.asList(mail.name.split(",")).contains(jz.name)
					if (ArrayUtils.indexOf(mail.name.split(","),jz.name)>=0
							|| (mail.minLevel <= jz.level && mail.maxLevel >= jz.level)) {
						String content = mail.content;
						Mail mailConfig = EmailMgr.INSTANCE
								.getMailConfig(mail.mailType);
						mailConfig.title = mail.title;
						String param = "";
						String fujian = mail.fujian;
						EmailMgr.INSTANCE.sendMail(jz.name, content, fujian,
								mailConfig.sender, mailConfig, param);
					}
					sendRecord = new GMEmailSendRecord();
					sendRecord.mailId = mail.id;
					sendRecord.jzId = jz.id;
					HibernateUtil.insert(sendRecord);
				}
			}
		}
		logger.info("君主 {} 的离线邮件已全部发送", jz.name);
	}

	/**
	 * @Title: compareDate
	 * @Description: 比较两个日期
	 * @param d1
	 * @param d2
	 * @return
	 * @return int d1<d2 -1;d1==d2 0;d1>d2 1
	 * @throws
	 */
	public int compareDate(Date d1, Date d2) {
		if (d1.getTime() < d2.getTime()) {
			return -1;
		} else if (d1.getTime() == d2.getTime()) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public void proc(Event event) {
		Long jzId = null;
		if (event.param != null && event.param instanceof Long) {
			jzId = (Long) event.param;
		}
		switch (event.id) {
		case ED.CHECK_EMAIL:
			if (jzId != null) {
				sendGMEMail(jzId);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.CHECK_EMAIL, this);
	}

}
