package com.youxigu.app.cache.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 修改scope种类：
 * 1 一般的对象，比如玩家： 属性都是基础类型：等级，名字等；
 * 2 不可堆叠的集合类：比如宠物，对宠物的属性修改User:Pets:Pet.field;
 * 3 可堆叠的集合类：这个要特殊处理，分为背包和道具
 *    用户 -> 武将列表 -> 武将 -> 武将属性
 *    这种可以分解，比如武将列表，可以分为 PetList=[];Pet_101=obj;Pet_102=obj;
 * 
 * @author wuliangzhu
 *
 */
public abstract class Scope implements IScope {
	protected Map<String, IFieldValue> fields = new HashMap<String, IFieldValue>();
	
	
	public Scope() {
		this.initFields();
	}
	
	protected abstract void initFields();
	
	@Override
	public void addField(String key, String fieldType) {
		IFieldValue value = FieldValue.create(fieldType);
		if (value != null) {
			fields.put(key, value);
		}
	}
	
	public IFieldValue get(String key){
		return fields.get(key);
	}
	
	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

	@Override
	public void save() {
		// TODO Auto-generated method stub

	}

}
