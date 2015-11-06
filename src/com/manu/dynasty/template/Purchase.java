package com.manu.dynasty.template;

public class Purchase implements Comparable<Purchase>{
	private int id;
	private int time;
	private int yuanbao;
	private int itemId;
	private int type;
	private int number;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getYuanbao() {
		return yuanbao;
	}
	public void setYuanbao(int yuanbao) {
		this.yuanbao = yuanbao;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	@Override
	public int compareTo(Purchase p) {
		return this.time - p.getTime();
	}
	
}
