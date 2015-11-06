package com.youxigu.concurrent;

import java.util.concurrent.Callable;

/**
 * 所有的业务逻辑就是为了实现handler
 * 
 * 如果要请求处理就是要把请求以request的形式发过来
 * 
 * @author wuliangzhu
 *
 */
@SuppressWarnings("unchecked")
public abstract class Handler implements Callable {
	public Request command;
	@Override
	public Object call() throws Exception {
		Object ret = this.handle(this.command);
		
		this.command.counter.decrementAndGet();
		this.command.result.set(ret);
		
		RequestDispatcher.getInstance().requestFinish(this.command);
		return ret;
	}
	
	protected abstract Object handle(Request request);

}
