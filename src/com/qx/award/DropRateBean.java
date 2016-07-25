package com.qx.award;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.qx.persistent.DBHash;

/**
 * 掉落保底数据记录
 * @author 康建虎
 *
 */
@Entity
@Table(indexes={@Index(name="jzId",columnList="jzId")})
public class DropRateBean implements DBHash {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long dbId;
	
	public long jzId;
	public int groupId;
	public int win;
	public int lose;
	public int fixScore;
	
	@Transient
	public char dbOp = 'N';// 进行计算时的状态，N:无DB操作,I插入操作，U更新操作

	@Override
	public long hash() {
		return jzId;
	}
}
