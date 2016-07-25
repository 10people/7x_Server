package com.qx.alliance;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceProtos.GiveUpVoteResp;
import qxmobile.protobuf.AllianceProtos.MengZhuApplyResp;
import qxmobile.protobuf.AllianceProtos.MengZhuVote;
import qxmobile.protobuf.AllianceProtos.MengZhuVoteResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.Mail;
import com.qx.email.EmailMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.LveDuoMgr;

public class AllianceVoteMgr {
	public Logger logger = LoggerFactory.getLogger(AllianceMgr.class);
	public static AllianceVoteMgr inst;

	public AllianceVoteMgr() {
		inst = this;
	}

	public void mengzhuApply(int cmd, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		MengZhuApplyResp.Builder response = MengZhuApplyResp.newBuilder();

		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			sendError(cmd, session, "该联盟不存在");
			logger.error("未发现联盟，id:{}", mgrMember.lianMengId);
			return;
		}

		if (alncBean.status != AllianceConstants.STATUS_APPLY) {
			response.setCode(1);
			session.write(response.build());
			sendError(cmd, session, "该联盟未开启盟主申请或者申请时间结束");
			return;
		}

		if (mgrMember.isBaoming == AllianceConstants.BAOMING_TRUE) {
			sendError(cmd, session, "已经报名了");
			return;
		}

		Date date = new Date();
		mgrMember.isBaoming = AllianceConstants.BAOMING_TRUE;
		mgrMember.baomingTime = date;
		HibernateUtil.save(mgrMember);
		logger.info("君主:[{}:{}],在时间:{}报名了盟主选举", junZhu.name, junZhu.id, date);

		response.setCode(0);
		session.write(response.build());
	}

	public void mengzhuVote(int cmd, IoSession session, Builder builder) {
		MengZhuVote.Builder request = (qxmobile.protobuf.AllianceProtos.MengZhuVote.Builder) builder;
		long id = request.getJunzhuId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 进行投票操作的成员
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			return;
		}
		
		JunZhu votedJunZhu = null;
		AlliancePlayer votedMember = null;
		// 判断是否是给自己投票
		if(junZhu.id == id) {
			votedJunZhu = junZhu;
			votedMember = mgrMember;
		} else {
			votedJunZhu = HibernateUtil.find(JunZhu.class, id);
			votedMember = AllianceMgr.inst.getAlliancePlayer(id);
		}
		if (votedJunZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 被投票的成员
		if (votedMember == null) {
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			sendError(cmd, session, "该联盟不存在");
			logger.error("未发现联盟，id:{}", mgrMember.lianMengId);
			return;
		}
		if (alncBean.status != AllianceConstants.STATUS_VOTING) {
			sendError(cmd, session, "还不能进行投票");
			return;
		}
		if (mgrMember.isVoted == AllianceConstants.VOTED_GIVE_UP) {
			sendError(cmd, session, "已经放弃投票，不能再投票");
			return;
		}

		if (mgrMember.isVoted == AllianceConstants.VOTED_TURE) {
			sendError(cmd, session, "已经投过票了");
			return;
		}
		
		Date date = new Date();
		mgrMember.isVoted = AllianceConstants.VOTED_TURE;
		mgrMember.voteJunzhuId = id;
		HibernateUtil.save(mgrMember);
		logger.info("[{}:{}]在时间:{}盟主选举投票数加1给:{}", id, junZhu.name, date,votedJunZhu.name);

		votedMember.voteNum = votedMember.voteNum + 1;
		votedMember.lastVoteTime = date;
		HibernateUtil.save(votedMember);
		logger.info("[{}:{}]在时间:{}盟主选举投票数加1", id, votedJunZhu.name, date);

		MengZhuVoteResp.Builder response = MengZhuVoteResp.newBuilder();
		response.setJunzhuId(id);
		response.setVoteNum(votedMember.voteNum);
		session.write(response.build());
	}

	public void giveUpVote(int cmd, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer mgrMember = AllianceMgr.inst.getAlliancePlayer(junZhu.id);
		if (mgrMember == null) {
			return;
		}
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(mgrMember.lianMengId);
		if (alncBean == null) {
			sendError(cmd, session, "该联盟不存在");
			logger.error("未发现联盟，id:{}", mgrMember.lianMengId);
			return;
		}
		GiveUpVoteResp.Builder response = GiveUpVoteResp.newBuilder();
		if (mgrMember.isVoted == AllianceConstants.VOTED_GIVE_UP) {
			response.setCode(1);
			session.write(response.build());
			return;
		}

		mgrMember.isVoted = AllianceConstants.VOTED_GIVE_UP;
		HibernateUtil.save(mgrMember);
		logger.info("君主:{} 放弃了投票", junZhu.name);
		response.setCode(0);
		session.write(response.build());
	}

	public void voteOver(int lianMengId) {
		AllianceBean alncBean = AllianceBeanDao.inst.getAllianceBean(lianMengId);
		if (alncBean == null) {
			logger.error("联盟选举结束，未发现联盟，id:{}", lianMengId);
			return;
		}
		if (alncBean.status != AllianceConstants.STATUS_VOTING) {
			logger.error("联盟选举结束，投票未结束");
			return;
		}
		JunZhu mengzhuJZ = HibernateUtil.find(JunZhu.class, alncBean.creatorId);

		List<AlliancePlayer> playerList = HibernateUtil.list(
				AlliancePlayer.class, " where lianMengId=" + lianMengId
						+ " and isBaoming=" + AllianceConstants.BAOMING_TRUE
						+ " order by voteNum desc");
		if(playerList.size() <= 0) {
			return;
		}

		int voteNumMax = playerList.get(0).voteNum;// 得票最高的票数
		Date when = playerList.get(0).lastVoteTime;// 得票最高的其中一个的最后一票时间
		List<AlliancePlayer> readyList = new ArrayList<AlliancePlayer>();// 票数相同的预备名单
		for (AlliancePlayer ap : playerList) {
			if (ap.voteNum == voteNumMax) {
				readyList.add(ap);
			}
		}

		AlliancePlayer result = null;
		if (readyList.size() == 1) {
			result = readyList.get(0);
		} else {
			for (AlliancePlayer ap : readyList) {
				if (ap.lastVoteTime.before(when)) {
					result = ap;
					when = ap.lastVoteTime;
				}
			}
		}
		JunZhu retJunzhu = null;
		if(result.voteNum == 0) {//参加投票最高投票数为0，则还是原来的盟主
			result = HibernateUtil.find(AlliancePlayer.class, alncBean.creatorId);
			retJunzhu = HibernateUtil.find(JunZhu.class, alncBean.creatorId);
		} else {
			retJunzhu = HibernateUtil.find(JunZhu.class, result.junzhuId);
		}
		if(retJunzhu == null){ 
			logger.error("联盟选举结束，找不到君主信息, junzhuId:{}", result.junzhuId);
			return;
		}
		
		Date date = new Date();
		boolean isChange = false;
		if(result.title == AllianceMgr.TITLE_LEADER){
			result.getTitleTime = date;
			HibernateUtil.save(result);
			logger.info("联盟选举结束，联盟:{}选举,原盟主:{}继续任职，时间:{}",lianMengId, mengzhuJZ.name, date);
		} else {
			isChange = true;
			AlliancePlayer mengZhu = HibernateUtil.find(AlliancePlayer.class,
					" where junzhuId=" + alncBean.creatorId
					+ " and lianMengId=" + alncBean.id);
			mengZhu.title = AllianceMgr.TITLE_MEMBER;
			mengZhu.getTitleTime = date;
			HibernateUtil.save(mengZhu);
			
			result.title = AllianceMgr.TITLE_LEADER;
			result.getTitleTime = date;
			HibernateUtil.save(result);
			
			alncBean.creatorId = result.junzhuId;
			logger.info("联盟选举结束，联盟:{}选举,新盟主:{}任职，时间:{}",lianMengId, retJunzhu.name, date);
		}
		alncBean.status = AllianceConstants.STATUS_NORMAL;
		HibernateUtil.save(alncBean);
		List<AlliancePlayer> members = AllianceMgr.inst.getAllianceMembers(alncBean.id);
		for(AlliancePlayer member : members) {
			member.isVoteDialog = AllianceConstants.VOTE_DIALOG;
			member.isBaoming = AllianceConstants.BAOMING_FALSE;
			member.isVoted = AllianceConstants.VOTED_FALSE;
			member.voteNum = 0;
			HibernateUtil.save(member);
		}
		// 发送联盟选举结束邮件通知
		List<Object[]> aList = LveDuoMgr.inst.getAllAllianceMberName(alncBean.id);
		if(isChange) {
			Mail mailConfig = null;				
			/*for(AlliancePlayer member : members) { 
				mailConfig = EmailMgr.INSTANCE.getMailConfig(30010);				
				String content = mailConfig.content.replace("***", retJunzhu.name).replace("XXX", mengzhuJZ.name);
				JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
				if(member.junzhuId != mengzhuJZ.id && member.junzhuId != retJunzhu.id) {
					EmailMgr.INSTANCE.sendMail(memberJunzhu.name, content, "", mailConfig.sender, mailConfig,"");
				}*/
			for(Object[] a:aList){
				long jzId = ((BigInteger)a[1]).longValue();
				String mName = (String) a[0];
				mailConfig = EmailMgr.INSTANCE.getMailConfig(30010);				
				String content = mailConfig.content.replace("***", retJunzhu.name).replace("XXX", mengzhuJZ.name);
				if(jzId != mengzhuJZ.id && jzId != retJunzhu.id) {
					EmailMgr.INSTANCE.sendMail(mName, content, "", mailConfig.sender, mailConfig,"");
			}
			}
			// 发给原盟主
			mailConfig = EmailMgr.INSTANCE.getMailConfig(30008);
			String content = mailConfig.content.replace("***", retJunzhu.name);
			EmailMgr.INSTANCE.sendMail(mengzhuJZ.name, content, "", mailConfig.sender, mailConfig,"");
			// 发给现任盟主
			mailConfig = EmailMgr.INSTANCE.getMailConfig(30009);
			content = mailConfig.content.replace("***", mengzhuJZ.name);
			EmailMgr.INSTANCE.sendMail(retJunzhu.name, content, "", mailConfig.sender, mailConfig,"");
		} else {
			/*for(AlliancePlayer member : members) { 
				Mail mailConfig = null;				
				JunZhu memberJunzhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
				String content = "";
				if(member.title == AllianceMgr.TITLE_LEADER) {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(30011);				
					content = mailConfig.content;
				} else {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(30012);				
					content = mailConfig.content.replace("***", mengzhuJZ.name);
				}
				EmailMgr.INSTANCE.sendMail(memberJunzhu.name, content, "", mailConfig.sender, mailConfig,"");
			}*/
			for(Object[] a:aList){
				long jzId = ((BigInteger)a[1]).longValue();
				String mName = (String) a[0];
				int title = (int) a[2];
				Mail mailConfig = null;				
				String content = "";
				if(title == AllianceMgr.TITLE_LEADER) {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(30011);				
					content = mailConfig.content;
				} else {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(30012);				
					content = mailConfig.content.replace("***", mengzhuJZ.name);
				}
				EmailMgr.INSTANCE.sendMail(mName, content, "", mailConfig.sender, mailConfig,"");
			}
		}
	}

	public void sendError(int cmd, IoSession session, String msg) {
		if (session == null) {
			logger.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
}
