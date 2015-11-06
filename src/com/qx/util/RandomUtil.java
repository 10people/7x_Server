package com.qx.util;

import java.util.Random;

public class RandomUtil {
	private static final Random random = new Random();
	private RandomUtil() {
	}
	
	public static int getRandomNum(int maxNum) {
		return random.nextInt(maxNum);
	}
}
