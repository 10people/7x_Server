package com.qx.test.main;

import java.util.Random;


public class RC4 {

	public static String decry_RC4(byte[] data, String key) {
		if (data == null || key == null) {
			return null;
		}
		return asString(RC4Base(data, key));
	}

	public static String decry_RC4(String data, String key) {
		if (data == null || key == null) {
			return null;
		}
		return new String(RC4Base(HexString2Bytes(data), key));
	}

	public static byte[] encry_RC4_byte(String data, String key) {
		if (data == null || key == null) {
			return null;
		}
		byte b_data[] = data.getBytes();
		return RC4Base(b_data, key);
	}

	public static String encry_RC4_string(String data, String key) {
		if (data == null || key == null) {
			return null;
		}
		return toHexString(asString(encry_RC4_byte(data, key)));
	}

	private static String asString(byte[] buf) {
		StringBuffer strbuf = new StringBuffer(buf.length);
		for (int i = 0; i < buf.length; i++) {
			strbuf.append((char) buf[i]);
		}
		return strbuf.toString();
	}

	private static byte[] initKey(String aKey) {
		byte[] b_key = aKey.getBytes();
		byte state[] = new byte[256];

		for (int i = 0; i < 256; i++) {
			state[i] = (byte) i;
		}
		int index1 = 0;
		int index2 = 0;
		if (b_key == null || b_key.length == 0) {
			return null;
		}
		for (int i = 0; i < 256; i++) {
			index2 = ((b_key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;
			byte tmp = state[i];
			state[i] = state[index2];
			state[index2] = tmp;
			index1 = (index1 + 1) % b_key.length;
		}
		return state;
	}

	private static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch & 0xFF);
			if (s4.length() == 1) {
				s4 = '0' + s4;
			}
			str = str + s4;
		}
		return str;// 0x表示十六进制
	}

	private static byte[] HexString2Bytes(String src) {
		int size = src.length();
		byte[] ret = new byte[size / 2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < size / 2; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	private static byte uniteBytes(byte src0, byte src1) {
		char _b0 = (char) Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (char) (_b0 << 4);
		char _b1 = (char) Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	private static byte[] RC4Base(byte[] input, String mKkey) {
		int x = 0;
		int y = 0;
		byte key[] = initKey(mKkey);
		int xorIndex;
		byte[] result = new byte[input.length];

		for (int i = 0; i < input.length; i++) {
			x = (x + 1) & 0xff;
			y = ((key[x] & 0xff) + y) & 0xff;
			byte tmp = key[x];
			key[x] = key[y];
			key[y] = tmp;
			xorIndex = ((key[x] & 0xff) + (key[y] & 0xff)) & 0xff;
			result[i] = (byte) (input[i] ^ key[xorIndex]);
		}
		return result;
	}
	  //随机生成字段
	public static String getRandomString(int length) { 
//  	    String base = "abcdefghijklmnopqrstuvwxyz0123456789俞伯牙席潮海丁克曾管正学管虎管谟业管仲陈伟霆王世充李渊杨坚郭树清李鸿忠王穗明刘铁男李登辉彭长健邓鸿王中军景百孚赵永亮陆兆禧严介和郁亮茅于轼王小波冯唐";   
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();   
  	    StringBuffer sb = new StringBuffer();   
  	    for (int i = 0; i < length; i++) {   
  	        int number = random.nextInt(base.length());   
  	        sb.append(base.charAt(number));   
  	    }   
  	    return sb.toString();   
  	 }  
	public static void main(String[] args) {
		
		long   startTime0 = System.currentTimeMillis();
  		for (int i = 0; i < 10; i++) {
  			String test = getRandomString(100);
  			int seed=new Random().nextInt(5)+1;//请求seed
  			String key = getRandomString(7);//请求key
  			String serverkey=ProtoEncrypt.EncodeData(seed,key,key.length());//客户端生成serverkey
  			
  			String clientkey = encry_RC4_string(key, serverkey);//客户端得到key 和serverkey生成clientkey
  			System.out.println("当前字符:" + test);
  			System.out.println("加密后秘钥为字前:" + key+"\n" +"加密后秘钥为字符:" + serverkey);
  			long   startTime = System.currentTimeMillis();
  			String str = encry_RC4_string(test, clientkey);//客户端根据clientkey 加密test
  			long cur1 = System.currentTimeMillis();
  			System.out.println("加密后字符:" + str);
  			System.out.println("加密时间:"+(cur1 - startTime));
  			long cur2 = System.currentTimeMillis();
  			System.out.println("解密后字符:" + decry_RC4(str, clientkey));//服务器解析
  			System.out.println("解密时间:"+(cur2 - startTime));
//  			Thread.sleep(1000*1);
  		}
  		long cur3 = System.currentTimeMillis();
  		System.out.println("共用时间:"+(cur3 - startTime0));
	}
}