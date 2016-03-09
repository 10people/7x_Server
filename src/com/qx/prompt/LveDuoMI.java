package com.qx.prompt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 掠夺军情
 * @author 
 *
 */
@Entity
@Table(name = "lve_duo_MI")
public class LveDuoMI{
	@Id
	public int zhanDouIdFromLveDuo;
	public int lmId;
	/** 被掠夺者（盟友 friend）*/
	public long beanLveDuoJunId;
	/**掠夺者 enemy*/
	public long lveDuoJunId;
	/**将要损失建设值的时间*/
	public Date willLostBuildTime;
	public int willLostBuild;
	public Date battleHappendTime;
	
	public int remainHp; //lveDuoJunId 被协防剩余血量
//	public int allHp; //lveDuoJunId 被协防总血量
	public LveDuoMI(){}
	public LveDuoMI(int zhanDouId, long beanLveDuoJunId, long lveDuoJunId, Date willLostBuildTime, int willLostBuild,
			Date battleHappendTime, int willLostBuildAllianceId) {
		super();
		this.zhanDouIdFromLveDuo = zhanDouId;
		this.beanLveDuoJunId = beanLveDuoJunId;
		this.lveDuoJunId = lveDuoJunId;
		this.willLostBuildTime = willLostBuildTime;
		this.willLostBuild = willLostBuild;
		this.battleHappendTime = battleHappendTime;
		this.lmId = willLostBuildAllianceId;
	}
	
}