package com.youxigu.net.routing;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;


public class DataPacketCoder extends CumulativeProtocolDecoder implements ProtocolEncoder {
	private static final int MAX_PACKET_SIZE = 1024;
	public void dispose(IoSession session) throws Exception {
	}
	
	/**
	 * 因为链接管理器只负责转发，所以数据发送过来的都是byte数组，直接转发就好了
	 * 
	 */
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
	        
	        DataPacket packet = (DataPacket)message;
	        
	        out.write(packet.getData());
	}

	/**
	 * datasize[4] | data
	 *          |opcode [4]|  			
	 */
	@Override
	protected boolean doDecode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		if (!in.prefixedDataAvailable(4, MAX_PACKET_SIZE)) {
            return false;
        }
		
		int packetSize = in.getInt(0); // 略过长度字段
//		int flag = in.getInt(4);
//		// 判断数据包的有效性
//		if (flag != DataPacket.PACKET_FLAG) {
//			System.out.println("PACKET FORMAT　ERROR flag:" + flag);
//			return true;
//		}
		
		DataPacket packet = new DataPacket();
		packet.setSize(packetSize);
		packet.setFlag(DataPacket.PACKET_FLAG);
		packet.setServerId(in.getInt(4));
		packet.setFromId(in.getInt(8));
		packet.setToId(in.getInt(12));
		packet.setDestinationId(in.getInt(16));
		
		byte[] data = new byte[packetSize + 4];
		in.get(data);
		
		ByteBuffer buf = ByteBuffer.wrap(data);

		//buf.flip(); 发送的数据，一般要把数据的pos留在最大数据上面
		
		packet.setData(buf); // 读取所有数据包数据，准备转发
        
        out.write(packet);
        
        return true;
	}

}
