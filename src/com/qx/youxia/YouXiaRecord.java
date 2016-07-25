package com.qx.youxia;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 游侠关卡战斗记录（用来记录是否挑战过，历史成绩。 并与扫荡相关）
 * 只有在完成过一次某个游侠关卡后才有记录
 * @author lizhaowen
 *
 */
@Entity
public class YouXiaRecord {
	@Id
	public long id;
	
	public long junzhuId;
	
	public int guanQiaId;

	public int type;//游侠类型
	
	/** 是否通过关，需要根据不同的游侠类型的通关条件来确定 ，0-未通关，1-通关*/
	@Column(columnDefinition = "INT default " + YouXiaMgr.GUANQIA_NOT_PASS)
	public int pass;

	/** 历史杀怪最好成绩 */
	public int score;
}
