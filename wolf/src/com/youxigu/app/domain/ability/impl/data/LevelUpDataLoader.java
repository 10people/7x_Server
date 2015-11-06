package com.youxigu.app.domain.ability.impl.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.youxigu.app.domain.ability.IDataLoader;

/**
 * 提供升级数据
 * 
 * @author wuliangzhu
 *
 */
public class LevelUpDataLoader implements IDataLoader{
	private Map<Integer, List<Integer>> levelUpData = new HashMap<Integer, List<Integer>>();
	
	private static LevelUpDataLoader instance;
	
	public static LevelUpDataLoader get(){
		if (instance == null) {
			instance = new LevelUpDataLoader();
		}
		
		return instance;
	}
	
	/**
	 * 要提供升级类型：比如玩家 ，武将等
	 * 标示当前等级需要多少经验升级
	 * @param type
	 * @param level
	 * @return -1 标示 type类型不存在; -2 标示等级越界，或者已经达到最高等级
	 */
	public Object get(int... args){
		int type = args[0];
		int level = args[1];
		List<Integer> exps = this.levelUpData.get(type);
		if (exps == null) {
			return -1;
		}
		
		int index = level - 1;
		if (exps.size() <= index) {
			return  -2;
		}
		
		return exps.get(index);
	}

}
