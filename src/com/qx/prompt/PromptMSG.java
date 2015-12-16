package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "PromptMSG15")
/**
 * @author yuquanshui
 * @deprecated 盟友快报
 */
public class PromptMSG {
	//盟友速报
	@Id
	public long id;
	/*
	 * 速报的当事者，当事人和另一方
	 */
	public long jzId;
	public String jzName; 
	public long otherJzId;
	public String otherJName;
	public String content;
	/**
	 * //1:某人押镖是否协助 2：某人镖车被抢 安慰 3：自己被安慰获得安慰奖励
	 */
	public int eventId;
	public int configId; // ReportTemp.xml id
	public String award; //  奖励（配置中不一定正确）
	public Date addTime; // 快报生成时间
//		/**
//		 * 触发盟友速报的《事件》（押镖、掠夺）--开始的时间
//		 */
//		public long startTime; 
//	/**
//	 *推送场景  1：运镖场景	2：主城
//
//	 */
////	public String recveiveScence;
//	
////	1：自己
////	2：盟友
////	3：非盟友（和自己不在一个联盟的玩家）
////	4：加入协助的盟友
////	5：未加入协助的盟友
//	
//	/**
//	 * 接收对象 
//	 */
////	public int receiveObject;
	/**
	 * 马车类型 
	 */
	public int realCondition;
}
