package com.youxigu.app.cache.data;


public class IntValue extends FieldValue {
	private int value;
	
	@Override
	public void add(String value) {
		int val = Integer.parseInt(value);
		this.value += val;
	}

	@Override
	public void set(String value) {
		int val = Integer.parseInt(value);
		this.value = val;		
	}

	@Override
	public String toString() {
		return new Integer(value).toString();
	}
	
	
}
