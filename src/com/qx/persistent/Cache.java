package com.qx.persistent;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;

import com.qx.activity.FuliInfo;
import com.qx.activity.QiandaoInfo;
import com.qx.alliance.MoBaiBean;
import com.qx.friends.GongHeBean;
import com.qx.guojia.ResourceGongJin;
import com.qx.jinengpeiyang.JNBean;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.PlayerTime;
import com.qx.mibao.MibaoLevelPoint;
import com.qx.pve.BuZhenBean;
import com.qx.pve.BuZhenMibaoBean;
import com.qx.pve.JunzhuPveInfo;
import com.qx.pve.SaoDangBean;
import com.qx.pvp.PvpBean;
import com.qx.vip.PlayerVipInfo;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoBean;

public class Cache {
	public static Map<Class , Map> caCheMap = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, JunZhu> jzCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, QiandaoInfo> qdCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, YaBiaoBean> ybCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, JNBean> jnBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, JunzhuPveInfo> jzPveInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, GongHeBean> gongHeBeanCacahe = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, MoBaiBean> moBaiBeanCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long,PvpBean> pvpBeanCaChe = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long,PlayerVipInfo> playerVipInfoCaChe = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, YBBattleBean> ybBattleCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, MibaoLevelPoint> mbLevelPointCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, SaoDangBean> saoDangCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, PlayerTime> playerTimeCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, FuliInfo> fuliInfoCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, ResourceGongJin> resGongJinCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static Map<Long, BuZhenMibaoBean> buZhenMiBaoCache = Collections.synchronizedMap(new LRUMap(5000));
	 public static void init(){
		 caCheMap.put(JunZhu.class , jzCache);
		 caCheMap.put(QiandaoInfo.class,qdCache);
		 caCheMap.put(YaBiaoBean.class,ybCache);
		 caCheMap.put(JNBean.class,jnBeanCache);
		 caCheMap.put(JunzhuPveInfo.class,jzPveInfoCache);
		 caCheMap.put(PvpBean.class,pvpBeanCaChe);
		 caCheMap.put(PlayerVipInfo.class,playerVipInfoCaChe);
		 caCheMap.put(MoBaiBean.class,moBaiBeanCache);
		 caCheMap.put(GongHeBean.class,gongHeBeanCacahe);
		 caCheMap.put(YBBattleBean.class, ybBattleCache);
		 caCheMap.put(MibaoLevelPoint.class,mbLevelPointCache);
		 caCheMap.put(SaoDangBean.class, saoDangCache);
		 caCheMap.put(PlayerTime.class,playerTimeCache);
		 caCheMap.put(FuliInfo.class,fuliInfoCache);
		 caCheMap.put(ResourceGongJin.class,resGongJinCache);
		 caCheMap.put(BuZhenMibaoBean.class,buZhenMiBaoCache);
	 }
}
