package com.youxigu.boot;

import java.io.File;
import java.net.URL;


/**
 * 游戏的入口
 * 
 * @author wuliangzhu
 *
 */
public class Root {
	public static void main(String[] args) {
		try {
			String conf = null;
			if (args.length > 1) {
				conf = args[1];
			}
			
			URL url = new File(".").toURI().toURL();
			GameLoader loader = new GameLoader(new URL[]{url});
			GameLoader.loader = loader;
			
			loader.run(conf);
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
}
