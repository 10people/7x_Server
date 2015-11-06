package com.youxigu.app.cache.data;


public class StringValue extends FieldValue {
	private String value = "";
	
	@Override
	public void add(String val) {
		if (this.value != null) {
			this.value = this.value + val;
		}else {
			this.value = val;
		}
	}
	
	@Override
	public void set(String value) {
		this.value = value;		
	}

	@Override
	public String toString() {
		return value;
	}	
}
