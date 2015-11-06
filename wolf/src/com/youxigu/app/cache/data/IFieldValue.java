package com.youxigu.app.cache.data;

public interface IFieldValue {
	public void set(String value);
	public void add(String value);
	public boolean validate(String value);
}
