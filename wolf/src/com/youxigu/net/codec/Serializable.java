package com.youxigu.net.codec;

import java.lang.reflect.Field;

import org.apache.mina.common.ByteBuffer;


public abstract class Serializable {
	protected static Field[] allFields = null;
	
	protected void initFields(Class<?> clazz) {
		if (allFields == null) {
			allFields = clazz.getFields();
		}
	}
	
	public abstract void encode(ByteBuffer buffer);
	
	public abstract void decode(ByteBuffer buffer);
	
	protected void encodeFields(ByteBuffer buf) {
		if (allFields != null && allFields.length > 0) {
			for (Field f : allFields) {
				try {
					Object o = f.get(this);
					if (o instanceof Integer) {
						buf.putInt((Integer)o);
					}else if (o instanceof String) {
						PacketUtil.putUTFString(buf, (String)o);
					}else if (o instanceof Float) {
						buf.putFloat((Float)o);
					}else {
						System.out.println("not implements:" + f.getName());
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
		}
	}
	
	protected void decodeFields(ByteBuffer buf) {
		if (allFields != null && allFields.length > 0) {
			for (Field f : allFields) {
				try {
					Object o = f.get(this);
					if (o instanceof Integer) {
						int i = buf.getInt();
						f.set(this, i);
					}else if (o instanceof String) {
						String s = PacketUtil.getUTFString(buf);
						f.set(this, s);
					}else if (o instanceof Float) {
						float i = buf.getFloat();
						f.set(this, i);
					}else {
						System.out.println("not implements:" + f.getName());
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
		}
	}
}
