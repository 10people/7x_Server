package com.manu.dynasty.template;

public class Purchase implements Comparable<Purchase>{
	public int id;
	public int time;
	public int yuanbao;
	public int itemId;
	public int type;
	public int number;
	@Override
	public int compareTo(Purchase p) {
		return this.time - p.time;
	}
	
}
