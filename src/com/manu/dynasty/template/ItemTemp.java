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
	public String getFunDesc() {
		return funDesc;
	}
	public void setFunDesc(String funDesc) {
		this.funDesc = funDesc;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	public int getRepeatNum() {
		return repeatNum;
	}
	public void setRepeatNum(int repeatNum) {
		this.repeatNum = repeatNum;
	}
	public int getItemType() {
		return itemType;
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
	public int getSellNum() {
		return sellNum;
	}
	public void setSellNum(int sellNum) {
		this.sellNum = sellNum;
	}
	@Override
	public int getType() {
		return itemType;
	}
	@Override
	public int getPinZhi() {
		return quality;
	}
	public int getEffectId() {
		return effectId;
	}
	public void setEffectId(int effectId) {
		this.effectId = effectId;
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
}
