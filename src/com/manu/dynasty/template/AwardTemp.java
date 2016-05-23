package com.manu.dynasty.template;

import com.qx.junzhu.JunZhu;

public class AwardTemp implements Cloneable {
	private int id;
	private int awardId;
	private int itemId;
	private int itemType;
	private int itemNum;
	private int weight;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAwardId() {
		return awardId;
	}
	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getItemType() {
		return itemType;
	}
	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	public int getItemNum() {
		return itemNum;
	}
	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
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
