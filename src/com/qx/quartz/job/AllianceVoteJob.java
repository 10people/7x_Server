package com.qx.quartz.job;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.util.DateUtils;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceConstants;
import com.qx.alliance.AllianceVoteMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class AllianceVoteJob implements Job {
	public Logger logger = LoggerFactory.getLogger(AllianceVoteJob.class);
	
	@Override
	public void execute(JobExecutionContext context) {
		
		List<AllianceBean> alncList = HibernateUtil.list(AllianceBean.class, "");
		for (AllianceBean ab : alncList) {
			long junzhuId = ab.creatorId;
			Date curDate = new Date();
			int differDays = 0;
			if (ab.status == AllianceConstants.STATUS_NORMAL) {
				PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junzhuId);
				if (null == playerTime) {
					playerTime = new PlayerTime(junzhuId);
					Cache.playerTimeCache.put(junzhuId, playerTime);
					HibernateUtil.insert(playerTime);
				}
				Date lastLogoutTime = playerTime.logoutTime;
				differDays = DateUtils.daysBetween(lastLogoutTime, curDate);
				if (differDays >= 7) {
					ab.status = AllianceConstants.STATUS_APPLY;
					ab.applyStartTime = curDate;
					logger.info("联盟:{} 由于盟主:{}7天不上线，联盟状态被设置为选举报名状态,时间:{}", ab.id, junzhuId, curDate);
					HibernateUtil.save(ab);
				}
			} else if (ab.status == AllianceConstants.STATUS_APPLY) {
				differDays = DateUtils.daysBetween(ab.applyStartTime, curDate);
				if (differDays >= 1) {
					ab.status = AllianceConstants.STATUS_VOTING;
					ab.voteStartTime = curDate;
					logger.info("联盟:{} 由选举报名状态置为投票状态, 时间:{}", ab.id, curDate);
					HibernateUtil.save(ab);
				}
			} else if (ab.status == AllianceConstants.STATUS_VOTING) {
				differDays = DateUtils.daysBetween(ab.applyStartTime, curDate);
				if (differDays >= 2) {
					AllianceVoteMgr.inst.voteOver(ab.id);
					logger.info("联盟:{} 结束联盟选举，时间:{}", ab.id, curDate);
				}
			}

		}
	}

}
