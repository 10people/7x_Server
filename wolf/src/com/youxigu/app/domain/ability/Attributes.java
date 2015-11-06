package com.youxigu.app.domain.ability;

import java.util.HashMap;
import java.util.Map;

public abstract class Attributes {
	protected Map<String, String> attributes = new HashMap<String, String>();
	
	public Attributes(){
		this.addAttribute();
	}
	
	protected abstract void addAttribute();
	
	public int get(String attr){
		String tmp = this.attributes.get(attr);
		
		return tmp == null ? 0 : Integer.parseInt(tmp);
	}
	
	protected void add(String attr, String defValue, String valueType){
		this.attributes.put(attr, defValue);
	}
	
	protected void add(String attr, int defValue, String valueType){
		this.attributes.put(attr, "" + defValue);
	}
	
	protected void add(String attr, String defValue){
		this.add(attr, defValue, "string");
	}
	
	protected void add(String attr, int defValue){
		this.add(attr, defValue, "string");
	}
	
	protected void set(String attr, String val){
		this.attributes.put(attr, val);
	}
	
	protected void set(String attr, int val){
		this.attributes.put(attr, "" + val);
	}
	
	protected boolean has(String attr){
		return this.attributes.get(attr) != null;
	}
}
