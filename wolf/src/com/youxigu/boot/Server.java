package com.youxigu.boot;

import com.youxigu.net.WolfServer;

public class Server {
	public Server() {
		WolfServer server = WolfServer.create();
		server.start();
	}
}
