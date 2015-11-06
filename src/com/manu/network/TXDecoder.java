package com.manu.network;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 康建虎
 *
 */
public class TXDecoder extends ProtoBuffDecoder{
	public static Logger log = LoggerFactory.getLogger(TXDecoder.class);
	public static final byte[] headBytes = "tgw_l7_forward\r\nHost:app12345.qzoneapp.com:80\r\n\r\n".getBytes();
	protected boolean doDecode(IoSession ses, IoBuffer in,
			ProtocolDecoderOutput arg2) throws Exception {
		//检查是否已经发送过socket头。
		//tgw_l7_forward\r\nHost:app12345.qzoneapp.com:80\r\n\r\n
		if(ses.containsAttribute(SessionAttKey.socketHeadReceived)==false){
			int len = in.remaining();
			if(len>2000){
				in.skip(len);
				log.error("地址头长度超标 {}", len);
				return false;
			}
			in.mark();
			byte[] arr = new byte[len];
			in.get(arr);
			in.rewind();
			int charNcnt = 0;
			boolean ok = false;
			for(int i=0; i<len; i++){
				if(arr[i] == '\n') charNcnt ++;
				if(charNcnt == 3){
					in.skip(i+1);
					ses.setAttribute(SessionAttKey.socketHeadReceived, Boolean.TRUE);
					ok = true;
					break;
				}
			}
			if(ok){
				log.debug("地址头检查通过");
			}else{
				log.debug("地址头检查-未-通过, len {}",len);
				return false;
			}
		}
		return super.doDecode(ses, in, arg2);
	}

}
