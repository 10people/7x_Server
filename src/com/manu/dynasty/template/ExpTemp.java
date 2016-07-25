package com.manu.dynasty.template;


/**
 * 武将经验
 * @author 丁唯奇
 *
 */
public class ExpTemp implements Comparable<ExpTemp>{
	public int id;//唯一id
	public int expId;// 经验类型
	public int level;// 等级
	public int needExp;// 需要经验值
	@Override
	public int compareTo(ExpTemp o) {
		if(this.level > o.level){
			return 1;
		}else if(this.level < o.level) {
			return -1;
		}
		return 0;
	}
	
	
}
