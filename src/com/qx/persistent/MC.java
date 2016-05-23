package com.qx.persistent;

import java.util.HashSet;
import java.util.Set;

import com.manu.dynasty.store.MemcachedCRUD;
import com.qx.achievement.Achievement;
import com.qx.activity.XianShiBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.DailyAwardBean;
import com.qx.bag.BagGrid;
import com.qx.bag.EquipGrid;
import com.qx.equip.domain.UserEquip;
import com.qx.hero.WjKeJi;
import com.qx.hero.WuJiang;
import com.qx.huangye.HYTreasure;
import com.qx.jinengpeiyang.JNBean;
import com.qx.jingmai.JmBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.TalentAttr;
import com.qx.purchase.TiLi;
import com.qx.purchase.TongBi;
import com.qx.purchase.XiLian;
import com.qx.pve.BuZhenBean;
import com.qx.task.DailyTaskBean;
import com.qx.timeworker.TimeWorker;
import com.qx.yabiao.YunBiaoHistory;

/**
 * @author 康建虎
 *
 */
public class MC {
	/**
	 * 控制哪些类进行memcached缓存。
	 * 被控制的类在进行创建时，需要注意调用MC的add和hibernate的insert。
	 */
	public static Set<Class<? extends MCSupport>> cachedClass = new HashSet<Class<? extends MCSupport>>();
	static{
		cachedClass.add(JNBean.class);
		cachedClass.add(JunZhu.class);
		cachedClass.add(WjKeJi.class);

		cachedClass.add(BagGrid.class);
		cachedClass.add(EquipGrid.class);
		cachedClass.add(WuJiang.class);

		// 经脉
		cachedClass.add(JmBean.class);
		// 日奖励
		cachedClass.add(DailyAwardBean.class);
		// 每日任务
		cachedClass.add(DailyTaskBean.class);
		// 布阵
		cachedClass.add(BuZhenBean.class);
		
		cachedClass.add(TimeWorker.class);
		cachedClass.add(TiLi.class);
		cachedClass.add(TongBi.class);
		cachedClass.add(XiLian.class);		
		cachedClass.add(UserEquip.class);		
		cachedClass.add(Achievement.class);		


//		cachedClass.add(AllianceBean.class);
		cachedClass.add(AlliancePlayer.class);
		// 天赋
		cachedClass.add(TalentAttr.class);
		//押镖
//		cachedClass.add(YaBiaoBean.class);
		//限时活动
		cachedClass.add(XianShiBean.class);
	}
	public static <T> T get(Class<T> t, long id){
		if(!cachedClass.contains(t)){
			return null;
		}
//		t.geta
		//
		String c = t.getSimpleName();
		String key = c+"#"+id;
		Object o = MemcachedCRUD.getInstance().getObject(key);
		return (T)o;
	}

	public static Object getValue(String key){
		Object o = MemcachedCRUD.getInstance().getObject(key);
		return o;
	} 
	
	public static boolean add(Object t, long id){
		if(!cachedClass.contains(t.getClass())){
			return false;
		}
		String c = t.getClass().getSimpleName();
		String key = c+"#"+id;
		return MemcachedCRUD.getInstance().add(key, t);
	}

	public static boolean addKeyValue(String key, Object value){
		return MemcachedCRUD.getInstance().add(key, value);
	}
	
	public static void update(Object t, long id){
		if(!cachedClass.contains(t.getClass())){
			return;
		}
		String c = t.getClass().getSimpleName();
		String key = c+"#"+id;
		MemcachedCRUD.getInstance().update(key, t);
	}
	
	/**
	 * 根据主键删除缓存
	 * @param obj	删除对象
	 * @param id	主键id
	 */
	public static void delete(Class clazz, long id) {
		if(!cachedClass.contains(clazz)) {
			return;
		}
		String prefix = clazz.getSimpleName();
		String key = prefix + "#" + id;
		MemcachedCRUD.getInstance().deleteObject(key);
	}
}
