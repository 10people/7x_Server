package com.manu.dynasty.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * ByteBuffer数据操作工具类
 * 
 * @author Administrator
 * 
 */
public class ByteBufferUtil {

	private static final String NULL_STR = "";

	/**
	 * 将String对象放入ByteBuffer中
	 * 
	 * @param buffer
	 * @param str
	 */
	public static void putString(ByteBuffer buffer, String str) {
		if (str == null)
			str = NULL_STR;
		byte[] data = str.getBytes(Charset.forName("utf-8"));
		buffer.putInt(data.length);
		buffer.put(data);
	}

	/**
	 * 从ByteBuffer中取出String对象
	 * 
	 * @param buffer
	 * @return
	 */
	public static String getString(ByteBuffer buffer) {
		int len = buffer.getInt();
		if(len > 5000 || len < 0) len = 1;
		byte[] data = new byte[len];
		buffer.get(data);
		String str = new String(data, Charset.forName("utf-8"));
		if (NULL_STR.equals(str)) {
			return null;
		}
		return str;
	}
	
	/**
	 * 将string数组放入buffer
	 * @param buffer
	 * @param strs
	 */
	public static void putStringArray(ByteBuffer buffer,String[] strs){
		if(strs!=null&&strs.length>0){
			for (String str : strs) {
				byte[] data = str.getBytes(Charset.forName("utf-8"));
				buffer.putInt(data.length);
				buffer.put(data);
			}
		}
		
	}

	/**
	 * 将byte数据放入ByteBuffer中
	 * 
	 * @param buf
	 * @param b
	 */
	public static void putByteArray(ByteBuffer buf, byte[] b) {
		if (b == null || b.length < 1) {
			buf.putInt(0);
			return;
		}
		buf.putInt(b.length);
		buf.put(b);
	}

	/**
	 * 从ByteBuffer中取出byte数组
	 * 
	 * @param buf
	 * @return
	 */
	public static byte[] getByteArray(ByteBuffer buf) {
		int len = buf.getInt();
		if (len == 0) {
			return new byte[0];
		}
		byte[] data = new byte[len];
		buf.get(data);
		return data;
	}

	/**
	 * 将int数组放入ByteBuffer中
	 * 
	 * @param buf
	 * @param array
	 */
	public static void putIntArray(ByteBuffer buf, int[] array) {
		if (array == null || array.length < 1) {
			buf.putInt(0);
			return;
		}
		buf.putInt(array.length * 4);
		for (int value : array) {
			buf.putInt(value);
		}
	}

	/**
	 * 从ByteBuffer中取出int数组
	 * 
	 * @param buf
	 * @return
	 */
	public static int[] getIntArray(ByteBuffer buf) {
		int len = buf.getInt() / 4;
		if (len == 0) {
			return new int[0];
		}
		int[] data = new int[len];
		for (int k = 0; k < len; k++) {
			data[k] = buf.getInt();
		}
		return data;
	}

	/**
	 * 将long数组放入ByteBuffer中
	 * 
	 * @param buf
	 * @param array
	 */
	public static void putLongArray(ByteBuffer buf, long[] array) {
		if (array == null || array.length < 1) {
			buf.putInt(0);
			return;
		}
		buf.putInt(array.length * 8);
		for (long value : array) {
			buf.putLong(value);
		}
	}

	/**
	 * 从ByteBuffer中取出long数组
	 * 
	 * @param buf
	 * @return
	 */
	public static long[] getLongArray(ByteBuffer buf) {
		int len = buf.getInt() / 8;
		if (len == 0) {
			return new long[0];
		}
		long[] data = new long[len];
		for (int k = 0; k < len; k++) {
			data[k] = buf.getLong();
		}
		return data;
	}

	/**
	 * 将short数组放入bytebuffer中
	 * 
	 */
	public static void putShortArray(ByteBuffer buf, short[] array) {
		if (array == null || array.length < 1) {
			buf.putInt(0);
			return;
		}
		buf.putInt(array.length * 2);
		for (short value : array) {
			buf.putShort(value);
		}
	}

	/**
	 * 从ByteBuffer中取出short数组
	 * 
	 */
	public static short[] getShortArray(ByteBuffer buf) {
		int len = buf.getInt() / 2;
		if (len == 0) {
			return new short[0];
		}
		short[] data = new short[len];
		for (int k = 0; k < len; k++) {
			data[k] = buf.getShort();
		}
		return data;
	}
	
	/**
	 * 把char数组放入buffer
	 * @param buf
	 * @param array
	 */
	public static void putCharArray(ByteBuffer buf, char[] array){
		if (array!=null&&array.length>0) {
			buf.putInt(array.length*2);
			for (char s : array) {
				buf.putChar(s);
			}
		}
	}
	
	/**
	 * 取出char[]
	 * @param buffer
	 * @return
	 */
	public static char[] getCharArray(ByteBuffer buffer){
		int len = buffer.getInt()/2;
		if (len==0) {
			return new char[0];
		}
		char[] data = new char[len];
		for (int i = 0; i < len; i++) {
			data[i]=buffer.getChar();
		}
		return data;
	}
	
	/**
	 * 把Float数组存入buffer
	 * @param buf
	 * @param array
	 */
	public static void putFloatArray(ByteBuffer buf, float[] array){
		if (array!=null&&array.length>0) {
			buf.putInt(array.length*4);
			for (float c : array) {
				buf.putFloat(c);
			}
		}
	}
	
	/**
	 * 取出float[]
	 * @param buffer
	 * @return
	 */
	public static float[] getFloatArray(ByteBuffer buffer){
		int len = buffer.getInt()/4;
		if (len==0) {
			return new float[0];
		}
		float[] data = new float[len];
		for (int i = 0; i < len; i++) {
			data[i]=buffer.getFloat();
		}
		return data;
	}
	
	/**
	 * 把Double数组存入buffer
	 * @param buf
	 * @param array
	 */
	public static void putDoubleArray(ByteBuffer buf, double[] array){
		if (array!=null&&array.length>0) {
			buf.putInt(array.length*8);
			for (double d : array) {
				buf.putDouble(d);
			}
		}
	}
	
	/**
	 * 取出double[]
	 * @param buffer
	 * @return
	 */
	public static double[] getDoubleArray(ByteBuffer buffer){
		int len = buffer.getInt()/8;
		if (len==0) {
			return new double[0];
		}
		double[] data = new double[len];
		for (int i = 0; i < len; i++) {
			data[i]=buffer.getDouble();
		}
		return data;
	}
	
	
	/**
	 * 对各种类型数组存入buffer·
	 * @param buffer
	 * @param obs
	 */
	public static void putAllArrayToBuffer(ByteBuffer buffer,Object[] obs) throws Exception{
			
		Object object = obs[0];
		if (object instanceof Byte) {
			byte[] bts = new byte[obs.length];
			for (int i = 0; i < obs.length; i++) {
				bts[i]=(Byte)obs[i];
			}
			putByteArray(buffer, bts);
		}else if (object instanceof Character) {
			char[] chs = new char[obs.length];
			for (int i = 0; i < obs.length; i++) {
				chs[i]=(Character)obs[i];
			}
			putCharArray(buffer, chs);
		}else if (object instanceof Integer) {
			int[] ints = new int[obs.length];
			for (int i = 0; i < obs.length; i++) {
				ints[i]=(Integer)obs[i];
			}
			putIntArray(buffer, ints);
		}else if (object instanceof Short) {
			short[] sts = new short[obs.length];
			for (int i = 0; i < obs.length; i++) {
				sts[i]=(Short)obs[i];
			}
			putShortArray(buffer, sts);
		}else if (object instanceof Long) {
			long[] lgs = new long[obs.length];
			for (int i = 0; i < obs.length; i++) {
				lgs[i]=(Long)obs[i];
			}
			putLongArray(buffer, lgs);
		}else if (object instanceof Float) {
			float[] flts = new float[obs.length];
			for (int i = 0; i < obs.length; i++) {
				flts[i]=(Float)obs[i];
			}
			putFloatArray(buffer, flts);
		}else if (object instanceof Double) {
			double[] dbs = new double[obs.length];
			for (int i = 0; i < obs.length; i++) {
				dbs[i]=(Double)obs[i];
			}
			putDoubleArray(buffer, dbs);
		}else if (object instanceof String) {
			String[] strs = new String[obs.length];
			for (int i = 0; i < obs.length; i++) {
				strs[i]=(String)obs[i];
			}
			putStringArray(buffer, strs);
		}
	}
	
	/**
	 * 取出buffer中对象
	 */
	public static Object getArrayByBuffer(ByteBuffer buffer,Object obj){
		if (obj instanceof byte[]) {
			byte[] bys=getByteArray(buffer);
			return bys;
		}else if (obj instanceof int[]) {
			int[] ins=getIntArray(buffer);
			return ins;
		}else if (obj instanceof short[]) {
			short[] sot=getShortArray(buffer);
			return sot;
		}else if (obj instanceof long[]) {
			long[] lgs=getLongArray(buffer);
			return lgs;
		}else if (obj instanceof char[]) {
			char[] chs=getCharArray(buffer);
			return chs;
		}else if (obj instanceof float[]) {
			float[] fls=getFloatArray(buffer);
			return fls;
		}else if (obj instanceof double[]) {
			double[] dbs=getDoubleArray(buffer);
			return dbs;
		}else if (obj instanceof String[]) {
			String str=getString(buffer);
			return str;
		}
		return null;
	}

}

