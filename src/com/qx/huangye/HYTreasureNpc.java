package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * @author lizhaowen
 *
 */
@Entity
public class HYTreasureNpc {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	
	public int position;//在地图中的位置
	
	public long treasureId;//2015年4月17日16:56:58改为long
	
	//HuangyeNpc.id 字段 
	public int npcId;
	
	public int remainHp;
	
	public int boCi; //npcId所在波次
	public HYTreasureNpc() {
		super();
	}
	//2015年4月17日16:57:30int改为long treasureId
	public HYTreasureNpc(int position, long treasureId, int npcId, int remainHp,
			int boCi) {
		super();
		//改自增主键为指定 
	    this.id=TableIDCreator.getTableID(HYTreasureNpc.class, 1L);
		this.position = position;
		this.treasureId = treasureId;
		this.npcId = npcId;
		this.remainHp = remainHp;
		this.boCi = boCi;
	}
}
