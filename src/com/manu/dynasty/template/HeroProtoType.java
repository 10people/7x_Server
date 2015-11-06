package com.manu.dynasty.template;

import com.manu.dynasty.hero.service.HeroService;

public class HeroProtoType extends BaseItem{
	private int tempId;
	private int heroId;
	private int icon;
	private int heroType;
	private int heroName;
	private int description;
	private int quality;
	private int gongjiType;
	private int sex;
	private int country;
	private String label;
	private int jingpoId;
	
	private transient int[] labelIds;
	
	public int getJingpoId() {
		return jingpoId;
	}

	public void setJingpoId(int jingpoId) {
		this.jingpoId = jingpoId;
	}

	public int[] getLabelIds(){
		return labelIds;
	}
	
	public int getTempId() {
		return tempId;
	}
	public void setTempId(int tempId) {
		this.tempId = tempId;
	}
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public int getHeroType() {
		return heroType;
	}
	public void setHeroType(int heroType) {
		this.heroType = heroType;
	}
	public int getHeroName() {
		return heroName;
	}
	public void setHeroName(int heroName) {
		this.heroName = heroName;
	}
	public int getDescription() {
		return description;
	}
	public void setDescription(int description) {
		this.description = description;
	}
	public int getQuality() {
		return quality;
	}
	public void setQuality(int quality) {
		this.quality = quality;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public int getCountry() {
		return country;
	}
	public void setCountry(int country) {
		this.country = country;
	}
	public String getLabel() {
		return label;
	}
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

	public int getGongjiType() {
		return gongjiType;
	}

	public void setGongjiType(int gongjiType) {
		this.gongjiType = gongjiType;
	}

	@Override
	public int getIconId() {
		return icon;
	}
	
}
