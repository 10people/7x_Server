package com.youxigu.net;

import java.util.Map;

public class DefaultWolfTask implements WolfTask {
	private static final long serialVersionUID = -6373667428978881579L;
	private Map<?, ?> attributes;
	
	public WolfTask execute(Response response){return null;};

	public Map<?, ?> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<?, ?> attributes) {
		this.attributes = attributes;
	}

}
