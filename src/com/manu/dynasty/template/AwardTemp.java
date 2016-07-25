package com.manu.dynasty.template;

import com.qx.junzhu.JunZhu;

public class AwardTemp implements Cloneable {
	public int id;
	public int awardId;
	public int itemId;
	public int itemType;
	public int itemNum;
	public int weight;
	public int getId() {
		return id;
	}
	@Override
	public String toString() {
		return "AwardTemp [id=" + id + ", awardId=" + awardId + ", itemId="
				+ itemId + ", itemType=" + itemType + ", itemNum=" + itemNum
				+ ", weight=" + weight + "]";
	}
	@Override
	public AwardTemp clone() {
		AwardTemp o = null;
		try {
			o = (AwardTemp) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	
	
}
