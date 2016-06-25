package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class WildCityBean {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	public int cityId;
	public int lmId;
	public Date winTime; //失败或战胜结算时时间都会更新
	@Column(columnDefinition = "INT default 0")
	public int isWin; //0,未战胜，1战胜
}
