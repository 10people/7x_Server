package com.youxigu.app.cache.data;


public class UserBucket extends AbstractBucket {

	@Override
	protected void init() {
		this.addScope("user", new User());

	}


}
