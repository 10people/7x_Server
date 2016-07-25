package com.manu.dynasty.template;

import com.manu.dynasty.hero.service.HeroService;

public class HeroProtoType extends BaseItem{
	public int tempId;
	public int heroId;
	public int icon;
	public int heroType;
	public int heroName;
	public int description;
	public int quality;
	public int gongjiType;
	public int sex;
	public int country;
	public String label;
	public int jingpoId;
	
	public transient int[] labelIds;
	public void setLabel(String label) {
		String[]ids = label.split(",");
		labelIds = new int[ids.length];
		for(int i = 0; i < ids.length; i ++){
			labelIds[i] = Integer.parseInt(ids[i]);
		}
		this.label = label;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return HeroService.getNameById(String.valueOf(heroName));
	}

	@Override
	public int getType() {
		return TYPE_HeroProto;
	}

	@Override
	public int getPinZhi() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getIconId() {
		return icon;
	}
	
}
