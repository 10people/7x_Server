package com.qx.award;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 掉落保底数据记录
 * @author 康建虎
 *
 */
@Entity
@Table(indexes={@Index(name="jzId",columnList="jzId")})
public class DropRateBean {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long dbId;
	
	public long jzId;
	public int groupId;
	public int win;
	public int lose;
	public int fixScore;
}
