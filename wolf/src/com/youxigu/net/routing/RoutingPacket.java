package com.youxigu.net.routing;

import org.apache.mina.common.ByteBuffer;

import com.youxigu.net.codec.Serializable;

/**
 * 所有的数据包应当继承这个类，解包和打包建议用simpleObject
 * 
 * @author wuliangzhu
 *
 */
public abstract class RoutingPacket extends Serializable{
	private int serverId; // 用来确定数据包发送给哪个backend server 8
	private int fromId; // 从哪里发送过来的 // 代表一个socket，可以是用户，也可以是server 12
	private int toId; // 要发送得地址, 处理完结果，一般都需要设置这个toId 16	
	private int destinationId; // 目标，一般是发送给用户，主动推送给用户需要设置这个 20
	
	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getFromId() {
		return fromId;
	}

	public void setFromId(int fromId) {
		this.fromId = fromId;
	}

	public int getToId() {
		return toId;
	}

	public void setToId(int toId) {
		this.toId = toId;
	}

	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}
	
	public void encode(ByteBuffer buf) {
		buf.putInt(this.serverId);
		buf.putInt(this.fromId);
		buf.putInt(this.toId);
		buf.putInt(this.destinationId);
	}
	
	public void decode(ByteBuffer buf){
		this.serverId = buf.getInt();
		this.fromId = buf.getInt();
		this.toId = buf.getInt();
		this.destinationId = buf.getInt();
	}
}
