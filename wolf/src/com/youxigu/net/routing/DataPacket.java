package com.youxigu.net.routing;

import org.apache.mina.common.ByteBuffer;



/**
 * 游戏数据包
 *  包括操作码 和 实际的数据
 *  opcode | destination | other
 *  
 *  opcode 决定了数据包的流向 destination 有时候用来分配服务器
 *  
 *  
 *  1 接收数据；
 *  2 获取 opcode destination；
 *  3 根据 opcode 与 server 的 对应关系，转发 opcode.
 *  4 由连接管理器去重连 各个server，
 *  
 *  接收到的数据只是转发，中间没有业务逻辑处理，所以发送的数据格式一定是 len | flag |  opcode | destination
 *  flag 是用来标示这个数据包肯定是这个业务逻辑的，如果flag不正确，这个包是不会处理的
 * @author wuliangzhu
 *
 */
public final class DataPacket {
	public static final int PACKET_FLAG = 0x3ae5b2f7;
	private static final int FROM_ADDRESS = 8;
	private static final int TO_ADDRESS = 12;
	
	private int size; // 数据包大小，是不包括自身的
	private int flag; // 用来标示消息的有效性 4
	private int serverId; // 用来确定数据包发送给哪个backend server 8
	private int fromId; // 从哪里发送过来的 // 代表一个socket，可以是用户，也可以是server 12
	private int toId; // 要发送得地址, 处理完结果，一般都需要设置这个toId 16
	
	private int destinationId; // 目标，一般是发送给用户，主动推送给用户需要设置这个 20
	private ByteBuffer data;
	
	public void modifyToId(int id) {
		data.putInt(TO_ADDRESS, id);
	}
	
	public void modifyFromId(int id) {
		if (data != null) {
			data.putInt(FROM_ADDRESS, id);
		}
	}
	
	public ByteBuffer getData() {
		return data;
	}
	public void setData(ByteBuffer data) {
		this.data = data;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getDestinationId() {
		return destinationId;
	}
	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getFromId() {
		return fromId;
	}
	public void setFromId(int fromId) {
		this.fromId = fromId;
		this.modifyFromId(fromId);
	}
	public int getToId() {
		return toId;
	}
	public void setToId(int toId) {
		this.toId = toId;
	}
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
}
