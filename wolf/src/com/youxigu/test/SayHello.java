package com.youxigu.test;

import com.youxigu.net.routing.RoutingPacket;

public class SayHello extends RoutingPacket {
	public int hp = 100;
//	public int hp2 = 100;
	public String name = "hello";
	public String name2 = "hellopublic String name = hello;";
	public float money = 10.6f;
	public float money2 = 10.6f;
	
	public SayHello(){
		this.initFields(SayHello.class);
	}
}
