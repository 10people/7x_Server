package com.manu.dynasty.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * 验证码工具 默认值（图片160*40、验证码长度5、干扰线数量150）
 * 
 * @author FengRui
 * @version 1.3.2
 * @date 2010-12-27
 */
public class ValidateCode {

	public static String[] fontNames = null;
	public static int[] fontType = { Font.PLAIN, Font.ITALIC, Font.BOLD };
	public static Random random = new Random();

	static {
		GraphicsEnvironment gEnv = GraphicsEnvironment
				.getLocalGraphicsEnvironment();// 系统字体
		fontNames = gEnv.getAvailableFontFamilyNames();// 所有系统字体名称
	}

	public int width = 160;// 图片宽度
	public int height = 40;// 图片长度
	public int codeCount = 4;// 验证码长度
	public int lineCount = 150;// 干扰线数量
	public char[] codeSequence = { 'A', 'C', 'D', 'E', 'F', 'H', 'J', 'K',
			'M', 'N', 'P', 'R', 'S', 'T', 'U', 'X', 'Y', 'Z', 'a', 'c', 'd',
			'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 's', 't', 'u', 'x',
			'y', 'z', '2', '3', '4', '5', '6', '7' };

	public String code = null;
	public byte[] datas = null;

	public static ValidateCode getValidateCode() {
		ValidateCode vc = new ValidateCode();
		vc.createCode();
		return vc;
	}

	public static ValidateCode getValidateCode(int width, int height) {
		ValidateCode vc = new ValidateCode(width, height);
		vc.createCode();
		return vc;
	}

	public static ValidateCode getValidateCode(int width, int height,
			int codeCount, int lineCount) {
		ValidateCode vc = new ValidateCode(width, height, codeCount, lineCount);
		vc.createCode();
		return vc;
	}

	ValidateCode() {
	}

	ValidateCode(int width, int height) {
		this.width = width;
		this.height = height;
		createCode();
	}

	ValidateCode(int width, int height, int codeCount, int lineCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
		this.lineCount = lineCount;
		createCode();
	}

	public static Font getFont(int fontHeight) {
		return Font.decode(fontNames[random.nextInt(fontNames.length)])
				.deriveFont(fontType[random.nextInt(fontType.length)],
						fontHeight);
	}

	public void createCode() {
		int x = 0;
		int fontHeight = 0;
		int codeY = 0;
		int red = 0;
		int green = 0;
		int blue = 0;

		x = this.width / (this.codeCount + 2);
		fontHeight = this.height - 4;
		codeY = this.height - 2;

		BufferedImage buffImg = new BufferedImage(this.width, this.height, 1);
		Graphics2D g = buffImg.createGraphics();

		Random random = new Random();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.width, this.height);
		for (int i = 0; i < this.lineCount; ++i) {// 干扰线
			int xs = random.nextInt(this.width);
			int ys = random.nextInt(this.height);
			int xe = xs + random.nextInt(this.width / 8);
			int ye = ys + random.nextInt(this.height / 8);
			red = random.nextInt(120);
			green = random.nextInt(120);
			blue = random.nextInt(120);
			g.setColor(new Color(red, green, blue));
			g.drawLine(xs, ys, xe, ye);
		}

		StringBuffer randomCode = new StringBuffer();
		double sumR = 0.0d;// 旋转值
		Font font = getFont(fontHeight);// 随机字体
		for (int i = 0; i < this.codeCount; ++i) {// 验证码
			char c = codeSequence[random.nextInt(this.codeSequence.length)];
			while (!font.canDisplay(c)) {// 此Font没有指定字符的字形
				font = getFont(fontHeight);// 再重新随机一个字体
			}
			g.setFont(font);// 字体

			sumR = -sumR + (random.nextDouble() - 0.5d) * Math.PI * 0.04;
			g.rotate(sumR);// 旋转

			red = random.nextInt(120);
			green = random.nextInt(120);
			blue = random.nextInt(120);
			g.setColor(new Color(red, green, blue));// 色值
			g.drawString(String.valueOf(c), (i + 1) * x, codeY);

			randomCode.append(String.valueOf(c));
		}
		g.dispose();

		ByteArrayOutputStream os = null;
		try {
			os = new ByteArrayOutputStream();
			ImageIO.write(buffImg, "JPEG", os);
			os.flush();
			this.datas = os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception e) {

			}
		}

		this.code = randomCode.toString();
	}

	public String getCode() {
		return this.code;
	}

	public byte[] getDatas() {
		return datas;
	}

}
