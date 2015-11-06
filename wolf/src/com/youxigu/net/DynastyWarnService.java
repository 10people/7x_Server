package com.youxigu.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynastyWarnService implements IWolfService {
	private static Logger logger = LoggerFactory.getLogger("warnLog");

	public boolean handleMessage(Response response, Object message) {
		if(message instanceof String){
			logger.warn(message.toString());
			System.out.println(message);
			return true;
		}
		return false;
	}
}
