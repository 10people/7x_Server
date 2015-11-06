package com.youxigu.net;

/**
 * 用来处理接收到的消息
 * 
 * @author wuliangzhu
 *
 */
public interface IWolfService {
	/**
	 * 如果这个service处理了这个消息就返回true，否则返回false
	 * @param response
	 * @param message
	 * @return
	 */
	boolean handleMessage(Response response, Object message);
}
