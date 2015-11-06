package com.manu.dynasty.template;

import java.util.HashSet;
import java.util.Set;

public class HuangyeAward implements SelfBuilder{
	public int site;
	public int itemType;
	public int itemId;
	public String guanqiaId;
	private Set<Integer> containGuanqia;
	public int getSite() {
		return site;
	}
	public void setSite(int site) {
		this.site = site;
	}
	public int getItemType() {
		return itemType;
	}
	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public String getGuanqiaId() {
		return guanqiaId;
	}
	public void setGuanqiaId(String guanqiaId) {
		containGuanqia = new HashSet<Integer>();
		this.guanqiaId = guanqiaId;
		String[] guanqias = guanqiaId.split(",");
		for(String guanqia : guanqias) {
			containGuanqia.add(Integer.parseInt(guanqia));
		}
	}
	@Override
	public void build() {
		setGuanqiaId(guanqiaId);
	}
	
}
