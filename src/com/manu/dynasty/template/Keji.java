package com.manu.dynasty.template;

public class Keji {
	public int id;
	public String name;
	public String description;
	public int level;
	public int kejiType;
	public int dataType;
	public int value;
	public int limitLevel;
	public String preId;
	public int posId;
	public String needItemId;
	public String needItemNum;
	public int costTime;
	
	// 当前科技的等级升到下一等级，需要其他科技等级大于等于preIds里的配置的科技等级
	public int[] preIds;
	// items下标与nums下标相同的分别表示升级需要的同一种物品和数量，需要配置表必须正确
	public int[] items;
	public int[] nums;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getKejiType() {
		return kejiType;
	}
	public void setKejiType(int kejiType) {
		this.kejiType = kejiType;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getLimitLevel() {
		return limitLevel;
	}
	public void setLimitLevel(int limitLevel) {
		this.limitLevel = limitLevel;
	}
	public String getPreId() {
		return preId;
	}
	public void setPreId(String preId) {
		this.preId = preId;
		String[] ids = preId.split(",");
		preIds = new int[ids.length];
		for(int i = 0; i < preIds.length; i ++){
			preIds[i] = Integer.parseInt(ids[i]);
		}
	}
	public int getPosId() {
		return posId;
	}
	public void setPosId(int posId) {
		this.posId = posId;
	}
	public String getNeedItemId() {
		return needItemId;
	}
	public void setNeedItemId(String needItemId) {
		this.needItemId = needItemId;
		String[]itemStr = needItemId.split(",");
		int n = itemStr.length;
		items = new int[n];
		for(int i = 0;i < n; i ++){
			items[i] = Integer.valueOf(itemStr[i]);
		}
	}
	public String getNeedItemNum() {
		return needItemNum;
	}
	public void setNeedItemNum(String needItemNum) {
		this.needItemNum = needItemNum;
		String[]tmp = needItemNum.split(",");
		nums = new int[tmp.length];
		int n = tmp.length;
		for(int i = 0;i < n; i ++){
			nums[i] = Integer.valueOf(tmp[i]);
		}
	}
	public int getCostTime() {
		return costTime;
	}
	public void setCostTime(int costTime) {
		this.costTime = costTime;
	}
}
