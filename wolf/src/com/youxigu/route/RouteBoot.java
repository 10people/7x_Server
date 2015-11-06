package com.youxigu.route;

import com.youxigu.net.WolfServer;

/**
 * 路由服务器启动程序
 * 
 * @author wuliangzhu
 *
 */
public class RouteBoot {
	public RouteBoot() {
		WolfServer server = WolfServer.create();
		server.start();
	}
}
