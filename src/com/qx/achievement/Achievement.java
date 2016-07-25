package com.qx.achievement;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name="Achievement")
public class Achievement implements MCSupport{
	/**
	 * 
	 */
	public static final long serialVersionUID = -8620633312424981760L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	public int chengjiuId;
	public long junZhuId;
	public int type;
	public int jindu;
	public boolean isFinish;
	public boolean isGetReward;
	@Override
	public long getIdentifier() {
		return id;
	}
	
}
