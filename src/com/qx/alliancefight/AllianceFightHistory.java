package com.qx.alliancefight;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AllianceFightHistory {
	
	@Id
	public int id;
	
	/** 联盟id，-1表示轮空 */
	public int allianceId1;			
	
	public String alliance1Name;
	
	/** 联盟id，-1表示轮空 */
	public int allianceId2;
	
	public String alliance2Name;
	
	/** 对战胜利的联盟id */
	public int winAllianceId;
	
	/** 战斗赛程 */
	public int battleRound;

}
