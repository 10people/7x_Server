package com.qx.pawnshop;

public class GoodsInfo {
	private int id;			// dangpu配置文件的id
//	public int  boolean isSell;	//是否已售出，true-以售完
	public int num; //剩余签到次数
	
	/**
	 * 
	 * 转换时Bean所要求的：
		被转换的Bean必需是public的。
		Bean被转换的属性一定要有对应的get方法，且一定要是public的。
		Bean中不能用引用自身的this的属性，否则运行时出现et.sf.json.JSONException: There is a cycle in the hierarchy!异常
		json-lib包转换时，不能以null为键名，
		否则运行报net.sf.json.JSONException:java.lang.NullPointerException:JSON keys must not be null nor the 'null' string.
		json-lib包转换时， 转换成XML元素key的名字要符合XML元素标签的命名规则，
		否则会报nu.xom.IllegalNameException: NCNames cannot start with the character 25异常
	 * @return
	 */
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
//	public boolean isSell() {
//		return isSell;
//	}
//	public void setSell(boolean isSell) {
//		this.isSell = isSell;
//	}
	
}
