package com.manu.dynasty.template;


/**
 * 武将经验
 * @author 丁唯奇
 *
 */
public class ExpTemp implements Comparable<ExpTemp>{
	private int id;//唯一id
	private int expId;// 经验类型
	private int level;// 等级
	private int needExp;// 需要经验值
	
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getExpId() {
		return expId;
	}
	public void setExpId(int expId) {
		this.expId = expId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getNeedExp() {
		return needExp;
	}
	public void setNeedExp(int needExp) {
		this.needExp = needExp;
	}
	@Override
	public int compareTo(ExpTemp o) {
		if(this.level > o.getLevel()){
			return 1;
		}else if(this.level < o.getLevel()) {
			return -1;
		}
		return 0;
	}
	
	
}
