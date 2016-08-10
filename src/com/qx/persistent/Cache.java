package com.qx.persistent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.LRUMap;

import com.qx.activity.FuliInfo;
import com.qx.activity.QiandaoInfo;
import com.qx.activity.ShouchongInfo;
import com.qx.activity.XianShiBean;
import com.qx.alliance.AllianceApply;
import com.qx.alliance.MoBaiBean;
import com.qx.alliance.building.LMKJJiHuo;
import com.qx.friends.GongHeBean;
import com.qx.guojia.ResourceGongJin;
import com.qx.huangye.BuZhenHYPve;
import com.qx.huangye.BuZhenHYPvp;
import com.qx.huangye.HYTreasureTimes;
import com.qx.huangye.shop.WuBeiFangBean;
import com.qx.jinengpeiyang.JNBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.PlayerTime;
import com.qx.junzhu.TalentAttr;
import com.qx.mibao.MibaoLevelPoint;
import com.qx.purchase.TiLi;
import com.qx.pve.BuZhenMibaoBean;
import com.qx.pve.JunzhuPveInfo;
import com.qx.pve.SaoDangBean;
import com.qx.pvp.LveDuoBean;
import com.qx.pvp.PvpBean;
import com.qx.task.DailyTaskActivity;
import com.qx.vip.PlayerVipInfo;
import com.qx.world.PosInfo;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoBean;
import com.qx.yabiao.YunBiaoHistory;
import com.qx.youxia.BuZhenYouXia;

public class Cache {
	public static ConcurrentHashMap<String, Object> lockMap = new ConcurrentHashMap<>();
	public static Map<Class, Map> caCheMap = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, JunZhu> jzCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, QiandaoInfo> qdCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, YaBiaoBean> ybCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, JNBean> jnBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, JunzhuPveInfo> jzPveInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, GongHeBean> gongHeBeanCacahe = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, MoBaiBean> moBaiBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, PvpBean> pvpBeanCaChe = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, PlayerVipInfo> playerVipInfoCaChe = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, YBBattleBean> ybBattleCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, MibaoLevelPoint> mbLevelPointCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, SaoDangBean> saoDangCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, PlayerTime> playerTimeCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, FuliInfo> fuliInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, ResourceGongJin> resGongJinCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, BuZhenMibaoBean> buZhenMiBaoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, BuZhenYouXia> buZhenYouXiaCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, BuZhenHYPve> buZhenHYPve = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, BuZhenHYPvp> buZhenHYPvp = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, TalentAttr> talentAttrCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, HYTreasureTimes> treasureTimesCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, AllianceApply> allianceApplyCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, ShouchongInfo> shouChongInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, TiLi> tiliCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, XianShiBean> xianshiBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, LveDuoBean> lvoDuoBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long,DailyTaskActivity> dailyTaskActivityCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, WuBeiFangBean> wuBeiFangBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, LMKJJiHuo> lMKJJiHuoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, YunBiaoHistory> yunBiaoHistoryCache = Collections.synchronizedMap(new LRUMap(5000));
	public static Map<Long, PosInfo> posInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	public static void init() {
		caCheMap.put(JunZhu.class, jzCache);
		caCheMap.put(QiandaoInfo.class, qdCache);
		caCheMap.put(YaBiaoBean.class, ybCache);
		caCheMap.put(JNBean.class, jnBeanCache);
		caCheMap.put(JunzhuPveInfo.class, jzPveInfoCache);
		caCheMap.put(PvpBean.class, pvpBeanCaChe);
		caCheMap.put(PlayerVipInfo.class, playerVipInfoCaChe);
		caCheMap.put(MoBaiBean.class, moBaiBeanCache);
		caCheMap.put(GongHeBean.class, gongHeBeanCacahe);
		caCheMap.put(YBBattleBean.class, ybBattleCache);
		caCheMap.put(MibaoLevelPoint.class, mbLevelPointCache);
		caCheMap.put(SaoDangBean.class, saoDangCache);
		caCheMap.put(PlayerTime.class, playerTimeCache);
		caCheMap.put(FuliInfo.class, fuliInfoCache);
		caCheMap.put(ResourceGongJin.class, resGongJinCache);
		caCheMap.put(BuZhenMibaoBean.class, buZhenMiBaoCache);
		caCheMap.put(TalentAttr.class, talentAttrCache);
		caCheMap.put(HYTreasureTimes.class, treasureTimesCache);
		caCheMap.put(AllianceApply.class, allianceApplyCache);
		caCheMap.put(ShouchongInfo.class, shouChongInfoCache);
		caCheMap.put(TiLi.class, tiliCache);
		caCheMap.put(BuZhenYouXia.class, buZhenYouXiaCache);
		caCheMap.put(BuZhenHYPve.class, buZhenHYPve);
		caCheMap.put(BuZhenHYPvp.class, buZhenHYPvp);
		caCheMap.put(XianShiBean.class, xianshiBeanCache);
		caCheMap.put(LveDuoBean.class, lvoDuoBeanCache);
		caCheMap.put(DailyTaskActivity.class, dailyTaskActivityCache);
		caCheMap.put(WuBeiFangBean.class, wuBeiFangBeanCache);
		caCheMap.put(LMKJJiHuo.class, lMKJJiHuoCache);
		caCheMap.put(YunBiaoHistory.class, yunBiaoHistoryCache);
		caCheMap.put(PosInfo.class, posInfoCache);
	}
	
	@SuppressWarnings("rawtypes")
	public static Object getLock(Class clazz, long cacheKey) {
		String key = clazz.getSimpleName() + "_" + cacheKey;
		Object lock = lockMap.get(key);
		if(lock != null) {
			return lock;
		}
		lockMap.putIfAbsent(key, new Object());
		return lockMap.get(key);
	}
}
