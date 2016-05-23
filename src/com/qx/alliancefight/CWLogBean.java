package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class CWLogBean {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	public String lmName1;	//占领方联盟名字
	public int country1;
	public String lmName2;	//被占领方联盟名字(可能是NPC)
	public int country2; //NPC 为-1
	public int cityId;	//被占领城市id
	public Date ot ;	//占领时间
}
