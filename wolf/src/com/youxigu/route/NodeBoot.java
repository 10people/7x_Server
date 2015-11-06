package com.youxigu.route;

import com.youxigu.net.WolfServer;

/**
 * 作为独立独立服务器的节点启动程序，如果不需要监听，则不需要调用这个
 * 
 * @author wuliangzhu
 *
 */
public class NodeBoot {
	public NodeBoot() {
		// 进行route服务器连接
		Node node = Node.create();
		node.start();

		// 启动监听程序
		WolfServer server = WolfServer.create();
		server.start();
	}
}
