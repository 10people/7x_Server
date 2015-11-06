package com.youxigu.boot;

import java.io.File;
import java.net.URL;

public class ChatRoot {
	public static void main(String[] args) {
		try {
			String conf = null;
			if (args.length > 0) {
				conf = args[0];
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
