package com.youxigu.boot;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 为了方便game目录管理，建立2个额外目录：
 * lib 游戏依赖的目录库
 * ext 游戏扩展库，这个目录会在游戏类加载之前加载用于覆盖游戏的默认实现
 * conf 游戏的配置文件
 * script 扩展脚本
 * 
 * @author wuliangzhu
 *
 */
public class GameLoader extends URLClassLoader{
	static GameLoader loader = null;
	
	private static final String LIB_PATH = "../lib";
	private static final String CONF_PATH = "../conf";
	private static final String EXT_PATH = "../ext";
	private static final String CLASS_PATH = "../bin";
	
	private static final String LIB_PATH_2 = "lib";
	private static final String CONF_PATH_2 = "conf";
	private static final String EXT_PATH_2 = "ext";
	private static final String CLASS_PATH_2 = "bin";
	
	public static GameLoader getInstance() {
		if (loader == null)
			try {
				loader = new GameLoader(new URL[]{new URL("file://.")});
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		return loader;
	}
	
	 GameLoader(URL[] urls) {
		super(urls);
	}
	
	 GameLoader(URL[] urls, ClassLoader loader) {
		super(urls, loader);
	}
	
	@SuppressWarnings("unchecked")
	public void run (String conf) {
		this.loadLib();
		try {
			this.addURL(new File("../bin").toURI().toURL());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		try {
			Class clazz = this.loadClass("com.youxigu.boot.Boot");
			Constructor constructor = clazz.getConstructor(String.class);
			constructor.newInstance(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 把包下的所有的class都加载进来,并调用无参数的构造方法
	 * @param packName
	 * @throws ClassNotFoundException 
	 */
	public void loadPackage(String packName) {
		String packPath = packName.startsWith("/") ? packName.substring(1) : packName;
		String classPrefix = packPath.replace('/', '.');
		
		URL url = this.getResource(packName);
		if (url == null) {
			url = this.getParent().getResource(packName);
		}
		if (url == null) {
			url = ClassLoader.class.getResource(packName);
		}
		
		try {
			String fileName = url.getFile();
			File file = new File(fileName);
			String fullName = null;
			for (String tmp : file.list()) {
				fullName = classPrefix + "." + tmp;
				Class.forName(fullName.substring(0, fullName.lastIndexOf('.')), true, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断目录下的所有文件如果是jar，则要单独加入url
	 * 1 如果是文件夹，则遍历所有文件调用loadDIr;
	 * 2 如果否则 加入
	 * @param dir
	 */
	public void loadDir(String dir) {
		File file = new File(dir);
		if (file != null && file.isDirectory()) {
			try {
				if (file.isHidden()) {
						return;
				}
				this.addURL(file.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			File[] files = file.listFiles();
			for (File tmp : files) {
				this.loadDir(tmp.getAbsolutePath());
			}
			return;
		}else if (dir.endsWith(".class")) {
			// return;
		}
		
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		this.addURL(url);
	}
	
	@SuppressWarnings("unchecked")
	private void loadLib() {
		this.loadDir(GameLoader.LIB_PATH);
		this.loadDir(GameLoader.EXT_PATH);
		this.loadDir(GameLoader.CONF_PATH);
		this.loadDir(GameLoader.CLASS_PATH);
		
		this.loadDir(GameLoader.LIB_PATH_2);
		this.loadDir(GameLoader.EXT_PATH_2);
		this.loadDir(GameLoader.CONF_PATH_2);
		this.loadDir(GameLoader.CLASS_PATH_2);
	}
}
