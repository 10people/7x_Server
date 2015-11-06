package com.youxigu.net.codec;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.routing.RoutingHandler;

/**
 * 目前还没有想好怎么处理
 * 
 * @author wuliangzhu
 *
 */
public class ConnectServerHandler extends RoutingHandler {
	private static Logger logger = LoggerFactory.getLogger(ConnectServerHandler.class);
	/**
	 * 连接管理程序连接各个server
	 * 
	 * 这里接收到的数据都是server发送过来的，如果第8位为1，则发送给客户端——根据destination找到客户端，否则
	 * 根据code找到backend，发送过去
	 * 
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		super.messageReceived(session, message);
		// 这里要进行一个clientId和destination的绑定关系
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}

}
