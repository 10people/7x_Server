package com.qx.friends;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "GongHeBean2")
/**
 * @deprecated 恭贺
 */
public class GongHeBean {
	@Id
	public long jzId;
	/**
	/*第一次百战打完邀请恭贺开始时间 负数表示已经结算过
	 */
	public long start4firstBZ; 
	/**
	/*第一次百战打完被恭贺次数
	 */
	public int times4firstBZ;
	/**
	/*第一次百战打完被恭贺奖励
	 */
	public int award4firstBZ;
	/**
	/*联盟功能开启邀请恭贺开始时间 负数表示已经结算过
	 */
	public long start4LM; 
	/**
	/*联盟功能开启被恭贺次数
	 */
	public int times4LM;
	/**
	/*联盟功能开启被恭贺奖励
	 */
	public int award4LM;
	
}
