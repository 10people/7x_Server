package com.youxigu.boot;

public class Boot {
	public Boot(String conf) {
		if (conf != null) {
			Config.loadConf(conf);
		}
		
		String mainClass = Config.get(Config.PROPERTY_MAIN_CLASS);
		try {
			Thread.currentThread().setContextClassLoader(GameLoader.getInstance());
			Class<?> clazz = Boot.class.getClassLoader().loadClass(mainClass);
			clazz.newInstance();
			Config.printConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
