package com.youxigu.app.cache.data;


public class FieldValue implements IFieldValue{

	public static IFieldValue create(String fieldType){
		if ("int".equals(fieldType)) {
			return new IntValue();
		}else if ("string".equals(fieldType)) {
			return new StringValue();
		}
		
		return null;
	}

	@Override
	public void add(String value) {
		
	}

	@Override
	public void set(String value) {
		
	}
	
	@Override
	public boolean validate(String value) {
		return false;
	}

}
