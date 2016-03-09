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
	private long id;
	
	private long junzhuId;
	
	private int guanQiaId;

	private int type;//游侠类型
	
	/** 是否通过关，需要根据不同的游侠类型的通关条件来确定 ，0-未通关，1-通关*/
	@Column(columnDefinition = "INT default " + YouXiaMgr.GUANQIA_NOT_PASS)
	private int pass;

	/** 历史杀怪最好成绩 */
	private int score;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public int getGuanQiaId() {
		return guanQiaId;
	}

	public void setGuanQiaId(int guanQiaId) {
		this.guanQiaId = guanQiaId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPass() {
		return pass;
	}

	public void setPass(int pass) {
		this.pass = pass;
	}

}
