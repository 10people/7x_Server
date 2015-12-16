package com.qx.jinengpeiyang;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 玩家技能培养
 * @author 康建虎
 *
 */
@Entity
public class JNBean {
	@Id
	public long jzId;
	//三个武器，每个武器三个技能
	public int wq1_1;
	public int wq1_2;
	public int wq1_3;
	
	public int wq2_1;
	public int wq2_2;
	public int wq2_3;
	
	public int wq3_1;
	public int wq3_2;
	public int wq3_3;
}
