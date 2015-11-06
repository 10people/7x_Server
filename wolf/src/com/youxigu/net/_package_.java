package com.youxigu.net;

public class _package_ {
	/**
	 * server 职责描述
	 * 
	 * 1 接受请求：所有的请求要做的逻辑都在请求参数中实现，server只负责提供CPU资源，不负责实现；
	 * 2 提供序列化协议实现；
	 * 3 可以支持多线程
	 * 
	 * client 职责
	 * 
	 * 1 提供分配任务的接口；
	 * 2 提供处理结束的回调，默认回调为空实现
	 * 
	 * 需要实现的有：
	 * 1 WolfServer
	 * 2 WolfClient
	 * 3 WolfResponse 不需要了 用wolfTask代替了
	 * 4 WolfTask
	 * 5 Transcoder：默认是序列化的
	 * 6 IoHandler：模式调用task进行执行
	 * 
	 */
}
