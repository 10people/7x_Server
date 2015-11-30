package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "PromptMSG0")
/**
 * @author yuquanshui
 * @deprecated 盟友快报
 */
public class PromptMSG {
	//盟友速报
	@Id
	public long id;
	public long jzId;
	public long otherJzId;
	public String content;
	/**
	 * //1:某人押镖是否协助 2：某人镖车被抢 安慰 3：自己被安慰获得安慰奖励
	 */
	public int msgType;
	public String award;
	public Date addTime;
	/**
	 * 触发盟友速报的《事件》（押镖、掠夺）--开始的时间
	 */
	public long startTime;
	/**
	 *推送场景 押镖 主城
	 */
	public int scType;
}
