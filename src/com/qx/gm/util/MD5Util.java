package com.qx.gm.util;

import java.security.MessageDigest;

/**
 * @ClassName: MD5Util
 * @Description: MD5加密工具类
 * @author 何金成
 * @date 2015年7月4日 下午6:14:25
 * 
 */
public class MD5Util {
	public final static String MD5(String pwd) {
		// 用于加密的字符
		char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			byte[] btInput = pwd.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) { // i = 0
				byte byte0 = md[i]; // 95
				str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
				str[k++] = md5String[byte0 & 0xf]; // F
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @Title: checkMD5
	 * @Description: TODO 等给了秘钥后用秘钥进行验证
	 * @param pwd
	 *            验证的字符串
	 * @param md5
	 *            验证码
	 * @return
	 * @return boolean
	 * @throws
	 */
	public static boolean checkMD5(String pwd, String md5) {
		String md5key = MD5(pwd);
		if (md5key.equals(md5.toUpperCase())) {
			return true;
		}
		return false;
		// return true;
	}

}
