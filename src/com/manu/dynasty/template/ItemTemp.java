package com.manu.dynasty.template;

import com.manu.dynasty.base.TempletService;

public class ItemTemp extends BaseItem{
	public int id;
	public String name;
	public String funDesc;
	public String icon;
	public int quality;
	public int repeatNum;
	public int itemType;
	public int sellNum;
	public int effectId;//强化材料给的强化经验
	public String awardID;
	public int effectshow;//是否在背包中展示首次获得。
	@Override
	public int getId() {
		return id;
	}
	@Override
	public String getName() {
		return name;
	}
	public void setItemType(int itemType) {
		switch(itemType){
		case 1:
		case 2:
			this.itemType = itemType;
			break;
		default:
			this.itemType = TYPE_ITEM;
		}
	}
	@Override
	public int getType() {
		return itemType;
	}
	@Override
	public int getPinZhi() {
		return quality;
	}
	@Override
	public int getIconId() {
		try{
			return Integer.parseInt(icon);
		}catch(NumberFormatException e){
			TempletService.log.error("物品icon设置错误，id:"+id, e);
			return 0;
		}
	}
	public int getRepeatNum() {
		return repeatNum;
	}
}
