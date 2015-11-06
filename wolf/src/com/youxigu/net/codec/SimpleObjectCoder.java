package com.youxigu.net.codec;

import java.io.NotSerializableException;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.youxigu.net.routing.RoutingPacket;


public class SimpleObjectCoder extends CumulativeProtocolDecoder implements ProtocolEncoder {
	private static final int MAX_PACKET_SIZE = 1024;
	
	public void dispose(IoSession session) throws Exception {
	}
	
	/**
	 * 因为链接管理器只负责转发，所以数据发送过来的都是byte数组，直接转发就好了
	 * 
	 */
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {	        
		 if (!(message instanceof Serializable)) {
	            throw new NotSerializableException();
	        }

	        ByteBuffer buf = ByteBuffer.allocate(64);
	        buf.setAutoExpand(true);
	        buf.skip(4); // 用于放置objectsize
	        
	        if (message instanceof RoutingPacket) { // 加routing 封包
	        	RoutingPacket rp = (RoutingPacket)message;
	        	rp.encode(buf);
	        }else {
	        	buf.skip(16);
	        }
	        
	        PacketUtil.putObject(buf, (Serializable)message);
	        
	        int objectSize = buf.position() - 4;
	        if (objectSize > MAX_PACKET_SIZE) {
	            buf.release();
	            throw new IllegalArgumentException(
	                    "The encoded object is too big: " + objectSize + " (> "
	                            + MAX_PACKET_SIZE + ')');
	        }
	        
	        buf.putInt(0, objectSize); // 放objectSize
	        
	        buf.flip();
	        out.write(buf);
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
		
		in.getInt(); // 略过长度字段
		
		int skipPos = in.position();
		
		in.skip(16); // 略过包头
		
		Object o = PacketUtil.getObject(in);
		if (o instanceof RoutingPacket) {
			RoutingPacket rp = (RoutingPacket)o;
			int oldPos = in.position();
			in.position(skipPos);
			rp.decode(in);
			in.position(oldPos);
		}
        
        out.write(o);
        
        return true;
	}

}
