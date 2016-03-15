package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "PromptMSG21")
/**
 * @author yuquanshui
 * @deprecated 盟友快报
 */
public class PromptMSG {
	//盟友速报
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	/**
	 * 速报的当事者，当事人和另一方
	 */
	public long jzId;
	public long otherJzId;
	/**
	 * 存储名字1 ，和jzId无对应关系
	 */
	public String jzName1;
	/**
	 * 存储名字2，和otherJzId无对应关系
	 */
	public String jzName2;
	public String content;

	public int eventId;
	public int configId; // ReportTemp.xml id
	public String award; //  奖励（配置中不一定正确）
	public Date addTime; // 快报生成时间
	/**
	 * 马车类型  当保存邀请加入联盟通知时 保存着lmId
	 */
	public int realCondition; 
	/**
	 * 马车价值   用于拼出被安慰的君主所获取的安慰奖
	 * 也用于 被掠夺拼出的奖励
	 */
	public String  cartWorth;
}
