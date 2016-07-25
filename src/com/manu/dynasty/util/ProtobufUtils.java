package com.manu.dynasty.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD2Proto;

public class ProtobufUtils {
	public static Logger eLogger = LoggerFactory.getLogger(ProtobufUtils.class.getSimpleName());
	public static Map<Integer, MessageLite> prototypeMap = new HashMap<Integer, MessageLite>();

	@SuppressWarnings("unchecked")
	public static Map<Class, Integer> protoClassToIdMap = new HashMap<Class, Integer>();

	public static void register(MessageLite liteClass, int uid) {
		prototypeMap.put(uid, liteClass);
		protoClassToIdMap.put(liteClass.getClass(), uid);
	}

	public static MessageLite.Builder parseFrom(int len, IoBuffer buf) {
		int uid = buf.getInt();
		byte[] data = new byte[len - 4];
		buf.get(data);

		Builder ret = parseProto(uid, data);

		return ret;
	}

	public static Builder parseProto(int protoId, byte[] data) {
		return parseProto(protoId, data,0,data.length);
	}

	public static Builder parseProto(int protoId, byte[] data, int offset,
			int len) {
		MessageLite tmpLite = prototypeMap.get(protoId);
		Builder ret = null;
		if(tmpLite == null){
			tmpLite = PD2Proto.inst.map(protoId);
		}
		if (tmpLite != null) {
			try {
				MessageLite.Builder builder = tmpLite.newBuilderForType();
				ret = builder.mergeFrom(data, offset, len);
			} catch (Exception e) {
				eLogger.error("解析协议出错", e);
				e.printStackTrace();
			}
		} else {
			eLogger.error("B没有找到协议类型 {}", protoId);
		}
		return ret;
	}

	public static MessageLite.Builder parseFrom(int len, byte[] data) {
		IoBuffer buf = IoBuffer.allocate(1024).setAutoExpand(true);
		buf.put(data);
		buf.flip();

		return parseFrom(data.length, buf);
	}

	public static byte[] toByteArray(MessageLite msg) {
		Integer into = protoClassToIdMap.get(msg.getClass());
		int uid = 1;
		if (into != null) {
			uid = into;
		} else {
			eLogger.error("the uid is null:" + msg.getClass());
		}

		byte[] data = msg.toByteArray();

		int dl = data.length;
		byte[] ret = new byte[dl + 4];
		ret[3] = (byte) (uid & 0xff);
		ret[2] = (byte) ((uid >> 8) & 0xff);
		ret[1] = (byte) ((uid >> 16) & 0xff);
		ret[0] = (byte) ((uid >> 24) & 0xff);

		System.arraycopy(data, 0, ret, 4, dl);

		return ret;
	}
}
