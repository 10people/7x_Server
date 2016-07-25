package com.qx.yuanbao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class YuanBaoInfo {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;// 2015年4月17日16:57:30int改为long
	public long ownerid;
	public Date timestamp;
	public String reason;
	public int yuanbaoBefore;
	public int yuanbaoAfter;
	public int yuanbaoChange;
	public int price;// 2015年7月3日13:57 添加消费单价
	public int type;// 2015年7月3日14:12 添加元宝类型，具体类型在YBType.java里
	public int costMoney;// 2015年7月2日18:11 添加人民币金额消费记录，充值元宝和购买使用
}
