package com.qx.junzhu;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class AcitvitedTaoZhuang {
	@Id
	public long jId;
	public int maxActiId; // 当前已经激活的最大套装id
	public int maxActiQiangHuaId; //当前已经激活的最大强化套装id
	
}