package com.youxigu.app.cache.data;

import java.util.LinkedList;
import java.util.List;

public class ListValue extends FieldValue {
	public List<String> value = new LinkedList<String>();

	/**
	 * 如果是正值，就是添加，如果是负值，则删除绝对值
	 */
	@Override
	public void add(String val) {
		boolean add = true;
		if (val.startsWith("-")) {
			val = val.substring(1);
			add = false;
		}
		
		if (add) {
			if (!this.value.contains(val)) {
				this.value.add(val);
			}
		}else {
			this.value.remove(val);
		}
	}

	@Override
	public void set(String value) {
		if (value == null || value.trim().length() == 0) {
			return;
		}
		
		this.value.clear();
		String[] args = value.split(",");
		for (String arg : args) {
			if (arg.trim().length() == 0) {
				continue;
			}
			
			this.add(arg);
		}
	}

	@Override
	public boolean validate(String value) {
		// TODO Auto-generated method stub
		return super.validate(value);
	}
	
}
