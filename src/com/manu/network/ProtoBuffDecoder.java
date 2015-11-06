package com.manu.network;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.msg.ProtobufMsg;

/**
 * @author 康建虎
 *
 */
public class ProtoBuffDecoder extends CumulativeProtocolDecoder{
	public static Logger log = LoggerFactory.getLogger(ProtoBuffDecoder.class);
	private static final int MAX_PACKET_SIZE = Integer.MAX_VALUE;//350K
	/** 
	 * 接口规定：返回true，表示buff里还有数据，需要再次解析。
	 * 底层就会再次调用此方法。
	 */
	protected boolean doDecode(IoSession ses, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		//
		int cnt = 1;
		while(in.prefixedDataAvailable(4, MAX_PACKET_SIZE)){
			int lenAll = in.getInt();
			int protoId = in.getShort();
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = protoId;
			Builder msg = null;
			if(lenAll>2){
				byte[] data = new byte[lenAll - 2];
				in.get(data);
				msg = ProtobufUtils.parseProto(protoId, data);
				pm.builder = msg;
			}
			out.write(pm);
			log.debug("循环第{}个",cnt);
			if(protoId != 22002)
				log.debug("解析到消息 {} {}",protoId, msg == null ? "null" : msg.getClass().getName());
			cnt++;
			//
			if(in.remaining()>0){
				//log.info(" 读取后还有剩余 {}",in.remaining());
				//dump(in);
			}
//			return true;
		}
		return false;
	}
	protected void dump(IoBuffer in) {
		int pos = in.position();
		int len = in.remaining();
		byte[] arr = new byte[len];
		in.get(arr);
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<len; i++	){
			if(i < 20){
				sb.append(String.format(" %02X", arr[i]));
			}else if(i == 20){
				sb.append(String.format(" ++ ", arr[i]));
			}else if(i>len - 20){
				sb.append(String.format(" %02X", arr[i]));
			}
		}
		log.info("dump:{}",sb.toString());
		in.position(pos);
	}

}
