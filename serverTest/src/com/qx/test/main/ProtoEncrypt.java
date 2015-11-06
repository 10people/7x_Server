package com.qx.test.main;

import java.util.Random;

public class ProtoEncrypt {
	private  static  int kHeadLen  = 0;//加密起始位
	public   static String EncodeData( int seed, String  paramString, int paramStringSize)
	{
		if (paramStringSize <= kHeadLen) return paramString;
		int i = 0;
		for (i = 0; i < kHeadLen; ++i)
		{
			seed = (seed << 3) - seed + (paramString.charAt(i));
		}
		StringBuffer sbuffer = new StringBuffer();
		for (i = kHeadLen; i < paramStringSize; ++i)
		{
			char old = paramString.charAt(i);
			char newb = ( char)(seed + old);
			sbuffer.append(newb);
			seed = (seed << 3) - seed + old;
		}
		return paramString.substring(0,kHeadLen)+sbuffer.toString();
	}

	public   static String DecodeData( int seed, String paramString, int paramStringSize)
	{
		if (paramStringSize <= kHeadLen) return paramString;
 
		int i = 0;
		for (i = 0; i < kHeadLen; ++i)
		{
			seed = (seed << 3) - seed + (paramString.charAt(i));
		}
		StringBuffer sbuffer = new StringBuffer();
		for (i = kHeadLen; i < paramStringSize; ++i)
		{
			char b =paramString.charAt(i);
			char newb = ( char)(b - seed);
			sbuffer.append(newb);
			seed = (seed << 3) - seed + newb;
		}
		return paramString.substring(0,kHeadLen)+sbuffer.toString();
	}
	  //随机生成字段
  	public static String getRandomString(int length) { 
  		String base = "abcdefghijklmnopqrstuvwxyz0123456789俞伯牙席潮海丁克曾管正学管虎管谟业管仲陈伟霆王世充李渊杨坚郭树清李鸿忠王穗明刘铁男李登辉彭长健邓鸿王中军景百孚赵永亮陆兆禧严介和郁亮茅于轼王小波冯唐";   
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
  			String test = getRandomString(5);
  			int seed=new Random().nextInt(5)+1;
  			System.out.println("当前字符:" + test);
  			long   startTime = System.currentTimeMillis();
  			String entest=EncodeData(seed,test,test.length());
  			System.out.println("加密后字符:" + 	entest);
  			long cur1 = System.currentTimeMillis();
  			System.out.println("加密时间:"+(cur1 - startTime));
  			System.out.println("解密后字符:" + DecodeData(seed,entest,entest.length()));
  			long cur2 = System.currentTimeMillis();
  			System.out.println("解密时间:"+(cur2 - startTime));
//  			Thread.sleep(1000*1);
  		}
  		long cur3 = System.currentTimeMillis();
  		System.out.println("共用时间:"+(cur3 - startTime0));
	}
}
