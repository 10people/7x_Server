package com.qx.alliance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "JuanXianBean")
public class JuanXianBean {
	@Id
	public long jzId;//主键，与君主ID绑定
	
	@Column(columnDefinition = "INT default 0")
	public int jianSheTimes;//基础建设已用次数
	
	@Column(columnDefinition = "INT default 0")
	public int huFuTimes;//军政建设已用次数
	
	public Date lastResetTime;//上次重置的时间
}
