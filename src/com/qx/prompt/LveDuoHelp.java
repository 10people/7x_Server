package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.manu.dynasty.template.CanShu;

@Entity
@Table(name="lve_duo_help")
public class LveDuoHelp{

	@Id
	public long junzhuId;
	/**当日总协助次数*/
	public int todayAllHelp;
	/**今日剩余协助次数*/
	public int todayRemainHelp;
	public Date lastHelpTime;

	public LveDuoHelp(){}
	public LveDuoHelp(long jzid){
		junzhuId = jzid;
		todayAllHelp = CanShu.EXPEL_DAYTIMES;
		todayRemainHelp = todayAllHelp;
	} 
}
