package com.qx.alliancefight;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AllianceFightMatch {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;
	
	/** 联盟id，-1表示轮空 */
	public int allianceId1;			
	
	/** 联盟id，-1表示轮空 */
	public int allianceId2;
	
	/** 战斗赛程 */
	public int battleRound;

	public AllianceFightMatch() {
		super();
	}

	public AllianceFightMatch(int allianceId1, int allianceId2, int battleRound) {
		super();
		this.allianceId1 = allianceId1;
		this.allianceId2 = allianceId2;
		this.battleRound = battleRound;
	}
	
	
}
