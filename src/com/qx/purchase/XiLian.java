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
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "db_id", unique = true, nullable = false)
	private long dbId;
	private Date date;
	private int num;		//当日元宝洗练次数
	private int xlsCount;	//当日洗练石洗练次数 大于策划配置的最大值之后，会用元宝洗练但是次数也会增加
	public int getXlsCount() {
		return xlsCount;
	}
	public void setXlsCount(int xlsCount) {
		this.xlsCount = xlsCount;
	}
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	@Override
	public long getIdentifier() {
		return dbId;
	}
}
