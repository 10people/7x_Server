package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;
import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * 
 * @author lizhaowen
 *
 */
@Entity
public class HYTreasureNpc implements DBHash {
	@Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	public int position;// 在地图中的位置

	public long treasureId;

	// HuangyeNpc.id 字段
	public int npcId;

	public int remainHp;

	public int boCi; // npcId所在波次

	public HYTreasureNpc() {
		super();
	}

	public HYTreasureNpc(int position, long treasureId, int npcId, int remainHp, int boCi) {
		super();
		// 改自增主键为指定
		this.id = TableIDCreator.getTableID(HYTreasureNpc.class, 1L);
		this.position = position;
		this.treasureId = treasureId;
		this.npcId = npcId;
		this.remainHp = remainHp;
		this.boCi = boCi;
	}

	@Override
	public long hash() {
		return treasureId * 1000;
	}
}
