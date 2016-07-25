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
	public void setPreId(String preId) {
		this.preId = preId;
		String[] ids = preId.split(",");
		preIds = new int[ids.length];
		for(int i = 0; i < preIds.length; i ++){
			preIds[i] = Integer.parseInt(ids[i]);
		}
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
	public void setNeedItemNum(String needItemNum) {
		this.needItemNum = needItemNum;
		String[]tmp = needItemNum.split(",");
		nums = new int[tmp.length];
		int n = tmp.length;
		for(int i = 0;i < n; i ++){
			nums[i] = Integer.valueOf(tmp[i]);
		}
	}
}
