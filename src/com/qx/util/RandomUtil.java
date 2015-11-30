package com.qx.util;

import java.math.BigDecimal;
import java.util.Random;

public class RandomUtil {
	private static final Random random = new Random();
	private RandomUtil() {
	}
	
	public static int getRandomNum(int maxNum) {
		return random.nextInt(maxNum);
	}
	
	/**
	 * 随机一个指定区间的小数值
	 * @param startNumInclude			1位小数值
	 * @param endNumInclude				1位小数值
	 * @return
	 */
	public static double getRandomNum(double startNumInclude, double endNumInclude) {
		int differ = (int) (endNumInclude * 10 - startNumInclude * 10 + 1);
		int temp = random.nextInt(differ);
		temp += 9;
		double result = temp * 0.1;
		return result;
	}
	
	public static double getScaleValue(double value, int scale) {
        BigDecimal bg = new BigDecimal(value);
        double result = bg.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        return result;
    }
}
