package com.qx.world;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="problem_player")
public class ProblemPlayer {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	public long junZhuId;
	
	public String junZhuName;
	/**记录时间*/
	public Date time;
	/**设备mac*/
	public String mac;
	public String descInfo;
 
	public ProblemPlayer(){}

	public ProblemPlayer(long junZhuId, String junZhuName, Date time,
			String mac, String desc) {
		super();
		this.junZhuId = junZhuId;
		this.junZhuName = junZhuName;
		this.time = time;
		this.mac = mac;
		this.descInfo = desc;
	}
}

