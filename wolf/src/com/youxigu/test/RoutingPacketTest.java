package com.youxigu.test;

import org.apache.mina.common.ByteBuffer;

import com.youxigu.net.codec.PacketUtil;

public class RoutingPacketTest {

	/**
	 * 1int 2string 2float x1000000 5300ms
	 *  1int 2string 1float x1000000 4600ms
	 *   1int 1string 1float x1000000 3200ms
	 *   2int 1string 1float x1000000 3700ms
	 *   
	 *   put object:
	 *   1int 2string 2float x1000000 6000ms
	 *  1int 2string 1float x1000000 5300ms
	 *   1int 1string 1float x1000000 3900ms
	 *   2int 1string 1float x1000000 4400ms
	 * @param args
	 */
	public static void main(String[] args) {
		SayHello sh = new SayHello();
		
	long s = System.currentTimeMillis();
	  ByteBuffer bb = ByteBuffer.allocate(1024).setAutoExpand(true);
	   for (int i = 0; i < 1000000; i++) {
		PacketUtil.putObject(bb, sh);
		
		
//		sh.encode(bb);

		bb.flip();
		
		sh.hp = 1001;
		sh.name = "com.youxigu.test.SyaHello";
		sh.money = 700.2f;
//		try {
//			SayHello.class.newInstance();
//		} catch (InstantiationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IllegalAccessException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		sh.decode(bb);
		sh = (SayHello)PacketUtil.getObject(bb);
	   }
	   
	   long e = System.currentTimeMillis();
		System.out.println(sh.hp + " " + sh.name + " " + bb.position() + " used" + (e - s));
	}

}
