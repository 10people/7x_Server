package com.qx.mibao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="MibaoLevelPoint")
public class MibaoLevelPoint {
	@Id
	public long junzhuId;
	public int point;
	public Date lastAddTime;	// 上次点数增加的时间
	
	public int dayTimes;
	public Date lastBuyTime;
	
//	public int needAllStar; // 能够获取宝箱需要的星星数，分母

	@Override
	public String toString() {
		return "MibaoLevelPoint [junzhuId=" + junzhuId + ", point=" + point
				+ ", lastAddTime=" + lastAddTime + ", dayTimes=" + dayTimes
				+ ", lastBuyTime=" + lastBuyTime + "]";
	}
	
}
