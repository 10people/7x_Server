package com.youxigu.app.cache.data;

public class User extends Scope{
	@Override
	protected void initFields() {
		this.addField("userId", "int");
		this.addField("userName", "string");
		this.addField("money", "int");		
	}	
}
