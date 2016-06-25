package com.qx.vip;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.util.TableIDCreator;


/**
 * 玩家VIP充值记录
 * @author lizhaowen
 *
 */
@Entity
@Table
public class VipRechargeRecord {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	// 就是君主id
	public long accId;
	// 本次充值金额
	public int curAmount;		
	// 本次充值时间
	public Date time;
	// 累计充值金额
	public int sumAmount;
	// VIP级别
	public int level;
	/* 充值类型: 对应ChongZhi.xml的id值 */
	public int type;
	// 本次所加元宝
	public int addYB;
	// 月卡有效期,购买月卡后的初始值是：CanShu.YUEKA_TIME
	public int yueKaValid;
	/**
	 * 没有默认的构造函数，hibernate报错：No default constructor for entity
	 * <p> Title: </p>
	 * <p> Description:</p>
	 */
	public VipRechargeRecord(){}
	public VipRechargeRecord(long jid, int cur, Date time, 
			int sum, int level, int type, int addYB, int yueKaValid){
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
		this.id=(TableIDCreator.getTableID(VipRechargeRecord.class, 1L));
		
		this.accId = jid;
		this.curAmount = cur;
		this.sumAmount = sum;
		this.time = time;
		this.level = level;
		this.type = type;
		this.addYB = addYB;
		this.yueKaValid = yueKaValid;
	}
}
