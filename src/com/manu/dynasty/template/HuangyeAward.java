package com.manu.dynasty.template;

import java.util.HashSet;
import java.util.Set;

public class HuangyeAward implements SelfBuilder{
	public int site;
	public int itemType;
	public int itemId;
	public String guanqiaId;
	public Set<Integer> containGuanqia;
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
