package com.qx.purchase;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "pur_xilian")
public class XiLian implements MCSupport {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 1L;
	@Id
	@Column(name = "db_id", unique = true, nullable = false)
	public long dbId;
	public Date date;
	public int num;		//当日元宝洗练次数
	public int xlsCount;	//当日洗练石洗练次数 大于策划配置的最大值之后，会用元宝洗练但是次数也会增加
	@Override
	public long getIdentifier() {
		return dbId;
	}
}
