package com.youxigu.app.cache.data;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractBucket implements IBucket {
	protected Map<String, IScope> scopes = new HashMap<String, IScope>();

	public AbstractBucket(){
		init();
	}
	
	protected abstract void init();
	
	@Override
	public IScope getScope(String name) {
		return scopes.get(name);
	}	

	@Override
	public void addScope(String name, IScope scope) {
		scopes.put(name, scope);
	}
}
