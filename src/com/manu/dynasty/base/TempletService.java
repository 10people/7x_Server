package com.manu.dynasty.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.Fengcefuli;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.JCZTemp;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.template.ZhuangBei;
import com.qx.award.AwardMgr;

/**
 * 添加一个模板表需要做以下几步 1.创建实体类 2.在conf/templet/中添加模板数据文件
 * 3.在conf/templet/dataConfig.xml加入刚才的模板数据文件
 * 
 * @author ranbo
 * 
 */
public class TempletService {
	public static Logger log = LoggerFactory.getLogger(TempletService.class);
	public static TempletService templetService = new TempletService();
	/**
	 * key:实体名 value:该实体下的所有模板数据
	 */
	public static Map<String, List<?>> templetMap = new HashMap<String, List<?>>();

	public static Map<Integer, ZhuangBei> equipMaps = new HashMap<Integer, ZhuangBei>();
	public static Map<Integer, List<ExpTemp>> expTempMaps = new HashMap<Integer, List<ExpTemp>>();
	public static Map<Integer, List<QiangHua>> qiangHuaMaps = new HashMap<Integer, List<QiangHua>>();
	public static Map<Integer, BaseItem> itemMap = new HashMap<Integer, BaseItem>();
	//2015年9月19日item id 部位对应map
	public static Map<Integer, Integer> itemMap4Buwei = new HashMap<Integer, Integer>();
	public static Map<Integer, ItemTemp> itemTempMap = new HashMap<Integer, ItemTemp>();
	public static Set<String> effectshowItemIds = Collections.EMPTY_SET;
	public TempletService() {

	}

	public static TempletService getInstance() {
		return templetService;
	}
	public void buildItemMap(){
		Map<Integer, BaseItem> map = new HashMap<Integer, BaseItem>();
		List<BaseItem> list = TempletService.listAll(ItemTemp.class.getSimpleName());
		addList(list,map);
		
		HashSet<String> showSetIds = new HashSet<>();
		list.stream()
			.filter(it->((ItemTemp)it).effectshow == 1)
			.mapToInt(it->it.getId())
			.forEach(e->showSetIds.add(String.valueOf(e)));
		effectshowItemIds = showSetIds;
		
		list = TempletService.listAll(ZhuangBei.class.getSimpleName());
		addList(list,map);
		itemMap = map;
	}

	public  void addList(List<BaseItem> list, Map<Integer, BaseItem> map) {
		for(BaseItem o : list){
			int id = o.getId();
			if(map.containsKey(id)){
				log.error("重复的ID {} {}",o.getId(),o.getName());
			}else{
				map.put(id, o);
			}
		}
	}
	/**
	 * 获取该实体类下所有模板数据
	 * 
	 * @param beanName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List listAll(String beanName) {
		return templetMap.get(beanName);
	}

	public void registerObject(Object o, Map<String, List<?>> dataMap) {
		add(o.getClass().getSimpleName(), o, dataMap);
	}



	@SuppressWarnings("unchecked")
	public void add(String key, Object data, Map<String, List<?>> dataMap) {
		List list = dataMap.get(key);
		if (list == null) {
			list = new ArrayList();
			dataMap.put(key, list);
		}
		list.add(data);
	}
	
	public void afterLoad() {
		// 装备配置数据

		List<ZhuangBei> zhuangbeis = TempletService.listAll("ZhuangBei");
		if (zhuangbeis != null) {
			for (ZhuangBei o : zhuangbeis) {

				equipMaps.put(o.getId(), o);
				itemMap4Buwei.put(o.getJinjieItem(), o.getBuWei());
			}
		}

		// 经验ExpTemp
		List<ExpTemp> expTemps = TempletService.listAll("ExpTemp");
		expTempMaps.clear();
		if (expTemps != null) {
			for (ExpTemp o : expTemps) {
				int expId = o.expId;
				List<ExpTemp> subs = expTempMaps.get(expId);
				if (subs == null) {
					subs = new ArrayList<ExpTemp>();
					expTempMaps.put(expId, subs);
				}
				subs.add(o);
			}
		}

		// 强化 qiangHuaMaps
		List<QiangHua> qiangHuas = TempletService.listAll("QiangHua");
		qiangHuaMaps.clear();
		if (qiangHuas != null) {
			for (QiangHua o : qiangHuas) {
				int qianghuaId = o.qianghuaId;
				List<QiangHua> subs = qiangHuaMaps.get(qianghuaId);
				if (subs == null) {
					subs = new ArrayList<QiangHua>();
					qiangHuaMaps.put(qianghuaId, subs);
				}
				subs.add(o);
			}
		}
		
		List<ItemTemp> itemTempList = TempletService.listAll(ItemTemp.class.getSimpleName());
		for(ItemTemp it : itemTempList) {
			itemTempMap.put(it.getId(), it);
		}
		
		buildItemMap();
		HeroService.initNameMap();
		loadCanShu();
		loadYunbiaoTemp();
		 loadFengcefuli();
		 loadJCZTemp();
	}
	//加载押镖配置
	public void loadYunbiaoTemp() {
		List<YunbiaoTemp> list = listAll(YunbiaoTemp.class.getSimpleName());
		Map<String, YunbiaoTemp> map = new HashMap<String, YunbiaoTemp>();
		for(YunbiaoTemp yb: list){
			map.put(yb.key, yb);
		}

		YunbiaoTemp.saveArea_recoveryPro=Float.parseFloat(map.get("saveArea_recoveryPro").value);
		YunbiaoTemp.saveArea_recovery_interval=Integer.parseInt(map.get("saveArea_recovery_interval").value);
		YunbiaoTemp.saveArea_people_max=Integer.parseInt(map.get("saveArea_people_max").value);
		YunbiaoTemp.bloodVial_recoveryPro=Integer.parseInt(map.get("bloodVial_recoveryPro").value);
		YunbiaoTemp.bloodVialCD=Integer.parseInt(map.get("bloodVialCD").value);
		YunbiaoTemp.bloodVial_freeTimes=Integer.parseInt(map.get("bloodVial_freeTimes").value);
		YunbiaoTemp.resurgenceTimes=Integer.parseInt(map.get("resurgenceTimes").value);
		YunbiaoTemp.incomeAddPro=Integer.parseInt(map.get("incomeAddPro").value);
		YunbiaoTemp.incomeAdd_startTime1=map.get("incomeAdd_startTime1").value;
		YunbiaoTemp.incomeAdd_endTime1=map.get("incomeAdd_endTime1").value;
		YunbiaoTemp.time1_switch=Integer.parseInt(map.get("time1_switch").value);
		YunbiaoTemp.incomeAdd_startTime2=map.get("incomeAdd_startTime2").value;
		YunbiaoTemp.incomeAdd_endTime2=map.get("incomeAdd_endTime2").value;
		YunbiaoTemp.time2_switch=Integer.parseInt(map.get("time2_switch").value);
		YunbiaoTemp.incomeAdd_startTime3=map.get("incomeAdd_startTime3").value;
		YunbiaoTemp.incomeAdd_endTime3=map.get("incomeAdd_endTime3").value;
		YunbiaoTemp.time3_switch=Integer.parseInt(map.get("time3_switch").value);
		YunbiaoTemp.income_lossless_price=Integer.parseInt(map.get("income_lossless_price").value);
		YunbiaoTemp.protectDuration=Integer.parseInt(map.get("protectDuration").value);
		YunbiaoTemp.protectionCD=Integer.parseInt(map.get("protectionCD").value);
		YunbiaoTemp.speedUpDuration=Integer.parseInt(map.get("speedUpDuration").value);
		YunbiaoTemp.speedUpEffect=Integer.parseInt(map.get("speedUpEffect").value);
		YunbiaoTemp.speedUpCD=Integer.parseInt(map.get("speedUpCD").value);
		YunbiaoTemp.speedUpPrice=Integer.parseInt(map.get("speedUpPrice").value);
		YunbiaoTemp.cart_attribute_pro=Integer.parseInt(map.get("cart_attribute_pro").value);
		YunbiaoTemp.foeCart_incomeAdd_pro=Integer.parseInt(map.get("foeCart_incomeAdd_pro").value);
		YunbiaoTemp.rewarding_killFoe_max=Integer.parseInt(map.get("rewarding_killFoe_max").value);
		YunbiaoTemp.cartAI_refresh_interval=map.get("cartAI_refresh_interval").value;
		YunbiaoTemp.cartAImax=Integer.parseInt(map.get("cartAImax").value);
		YunbiaoTemp.cartAILvlMin=Integer.parseInt(map.get("cartAILvlMin").value);
		YunbiaoTemp.cartAILvlMax=Integer.parseInt(map.get("cartAILvlMax").value);
		YunbiaoTemp.autoResurgenceTime=Integer.parseInt(map.get("autoResurgenceTime").value);
		YunbiaoTemp.yunbiao_start_broadcast=map.get("yunbiao_start_broadcast").value;
		YunbiaoTemp.yunbiao_end_broadcast=map.get("yunbiao_end_broadcast").value;
		YunbiaoTemp.yunbiao_comforted_award_k=Double.parseDouble(map.get("yunbiao_comforted_award_k").value);
		YunbiaoTemp.yunbiao_comforted_award_b=Integer.parseInt(map.get("yunbiao_comforted_award_b").value);
		YunbiaoTemp.yunbiao_comforted_award_Num=Integer.parseInt(map.get("yunbiao_comforted_award_Num").value);
		YunbiaoTemp.cartAI_appear_interval=Integer.parseInt(map.get("cartAI_appear_interval").value);
		YunbiaoTemp.killFoeAward_k=Double.parseDouble(map.get("killFoeAward_k").value);
		YunbiaoTemp.killFoeAward_b=Double.parseDouble(map.get("killFoeAward_b").value);
		YunbiaoTemp.cartTime=Integer.parseInt(map.get("cartTime").value);
		YunbiaoTemp.yunbiaoScene_modelNum_max=Integer.parseInt(map.get("yunbiaoScene_modelNum_max").value);
		YunbiaoTemp.yunbiao_start_broadcast_CD=Integer.parseInt(map.get("yunbiao_start_broadcast_CD").value);
		YunbiaoTemp.robincome_LvMax=Integer.parseInt(map.get("robincome_LvMax").value);
		YunbiaoTemp.cartLifebarNum=Float.parseFloat(map.get("cartLifebarNum").value);
		YunbiaoTemp.damage_amend_Y=Float.parseFloat(map.get("damage_amend_Y").value);
		YunbiaoTemp.damage_amend_X=Float.parseFloat(map.get("damage_amend_X").value);
		//2016年1月25日
//		YunbiaoTemp.cartTimesAdd1=Integer.parseInt(map.get("cartTimesAdd1").value);
		YunbiaoTemp.cartTimesAdd2=Integer.parseInt(map.get("cartTimesAdd2").value);
		YunbiaoTemp.assistant_application_CD = Integer.parseInt(map.get("assistant_application_CD").value);
	}
	//加载封测活动配置
	public void loadFengcefuli() {
		List<Fengcefuli> list = listAll(Fengcefuli.class.getSimpleName());
		Map<String, Fengcefuli> map = new HashMap<String, Fengcefuli>();
		for(Fengcefuli fl: list){
			map.put(fl.key, fl);
		}
		Fengcefuli.YBTitle=map.get("YBTitle").value;
		Fengcefuli.YBStartTime1=map.get("YBStartTime1").value;
		Fengcefuli.YBaward1=map.get("YBaward1").value;
		Fengcefuli.YBStartTime2=map.get("YBStartTime2").value;
		Fengcefuli.YBaward2=map.get("YBaward2").value;
		Fengcefuli.TiliTitle=map.get("TiliTitle").value;
		Fengcefuli.TiliStartTime1=map.get("TiliStartTime1").value;
		Fengcefuli.TiliEndTime1=map.get("TiliEndTime1").value;
		Fengcefuli.Tiliaward1=map.get("Tiliaward1").value;
		Fengcefuli.TiliStartTime2=map.get("TiliStartTime2").value;
		Fengcefuli.TiliEndTime2=map.get("TiliEndTime2").value;
		Fengcefuli.Tiliaward2=map.get("Tiliaward2").value;
		Fengcefuli.TiliStartTime3=map.get("TiliStartTime3").value;
		Fengcefuli.TiliEndTime3=map.get("TiliEndTime3").value;
		Fengcefuli.Tiliaward3=map.get("Tiliaward3").value;
		Fengcefuli.Tilimessage=map.get("Tilimessage").value;
		Fengcefuli.YuekaTitle=map.get("YuekaTitle").value;
		Fengcefuli.YuekaRefreshTime=map.get("YuekaRefreshTime").value;
		Fengcefuli.Yuekaaward=map.get("Yuekaaward").value;
	}
	public void loadCanShu() {
		List<CanShu> list = listAll(CanShu.class.getSimpleName());
		Map<String, CanShu> map = new HashMap<String, CanShu>();
		for(CanShu c : list){
			map.put(c.key, c);
		}
		CanShu.JUNZHU_PUGONG_QUANZHONG = Double.parseDouble(map.get("JUNZHU_PUGONG_QUANZHONG").value);
		CanShu.JUNZHU_PUGONG_BEISHU = Double.parseDouble(map.get("JUNZHU_PUGONG_BEISHU").value);
		CanShu.JUNZHU_JINENG_QUANZHONG = Double.parseDouble(map.get("JUNZHU_JINENG_QUANZHONG").value);
		CanShu.JUNZHU_JINENG_BEISHU = Double.parseDouble(map.get("JUNZHU_JINENG_BEISHU").value);
		
		CanShu.WUJIANG_PUGONG_QUANZHONG = Double.parseDouble(map.get("WUJIANG_PUGONG_QUANZHONG").value);
		CanShu.WUJIANG_PUGONG_BEISHU = Double.parseDouble(map.get("WUJIANG_PUGONG_BEISHU").value);
		CanShu.WUJIANG_JINENG_QUANZHONG = Double.parseDouble(map.get("WUJIANG_JINENG_QUANZHONG").value);
		CanShu.WUJIANG_JINENG_BEISHU = Double.parseDouble(map.get("WUJIANG_JINENG_BEISHU").value);
		
		CanShu.SHIBING_PUGONG_QUANZHONG = Double.parseDouble(map.get("SHIBING_PUGONG_QUANZHONG").value);
		CanShu.SHIBING_PUGONG_BEISHU = Double.parseDouble(map.get("SHIBING_PUGONG_BEISHU").value);
		CanShu.SHIBING_JINENG_QUANZHONG = Double.parseDouble(map.get("SHIBING_JINENG_QUANZHONG").value);
		CanShu.SHIBING_JINENG_BEISHU = Double.parseDouble(map.get("SHIBING_JINENG_BEISHU").value);
		
		CanShu.MAXTIME_PVE = Integer.parseInt(map.get("MAXTIME_PVE").value);
		CanShu.MAXTIME_HUANGYE_PVE = Integer.parseInt(map.get("MAXTIME_HUANGYE_PVE").value);
		CanShu.MAXTIME_HUANGYE_PVP = Integer.parseInt(map.get("MAXTIME_HUANGYE_PVP").value);
		CanShu.DAYTIMES_LEGENDPVE = Integer.parseInt(map.get("DAYTIMES_LEGENDPVE").value);
		CanShu.ADD_TILI_INTERVAL_TIME = Integer.parseInt(map.get("ADD_TILI_INTERVAL_TIME").value);
		CanShu.ADD_TILI_INTERVAL_VALUE = Integer.parseInt(map.get("ADD_TILI_INTERVAL_VALUE").value);
		CanShu.ADD_XILIAN_VALUE = Integer.parseInt(map.get("ADD_XILIAN_VALUE").value);
		CanShu.ADD_XILIAN_INTERVAL_TIME = Integer.parseInt(map.get("ADD_XILIAN_INTERVAL_TIME").value);
		CanShu.ADD_MIBAODIANSHU_INTERVAL_TIME = Integer.parseInt(map.get("ADD_MIBAODIANSHU_INTERVAL_TIME").value);
		CanShu.FREE_XILIAN_TIMES_MAX = Integer.parseInt(map.get("FREE_XILIAN_TIMES_MAX").value);
		
		CanShu.ZHANLI_M = Double.parseDouble(map.get("ZHANLI_M").value);
		CanShu.ZHANLI_C = Double.parseDouble(map.get("ZHANLI_C").value);
		CanShu.ZHANLI_R = Double.parseDouble(map.get("ZHANLI_R").value);
		CanShu.ZHANLI_L = Double.parseDouble(map.get("ZHANLI_L").value);
		CanShu.ZHANLI_K1 = Double.parseDouble(map.get("ZHANLI_k1").value);
		CanShu.ZHANLI_K2 = Double.parseDouble(map.get("ZHANLI_k2").value);
		CanShu.ZHANLI_M1 = Double.parseDouble(map.get("ZHANLI_m1").value);
		CanShu.ZHANLI_M2 = Double.parseDouble(map.get("ZHANLI_m2").value);
		
		/*
		 * 百战掠夺建设和威望奖励
		 */
		CanShu.BAIZHAN_LVEDUO_K = Double.parseDouble(map.get("BAIZHAN_LVEDUO_K").value);
		CanShu.BAIZHAN_LVEDUO_JIANSHEZHI = Integer.parseInt(map.get("BAIZHAN_LVEDUO_JIANSHEZHI").value);
		CanShu.BAIZHAN_NPC_WEIWANG = Integer.parseInt(map.get("BAIZHAN_NPC_WEIWANG").value);
		CanShu.BAIZHAN_WEIWANG_ADDLIMIT = Integer.parseInt(map.get("BAIZHAN_WEIWANG_ADDLIMIT").value);
		CanShu.BAIZHAN_FIRSTWIN_YUANBAO = Integer.parseInt(map.get("BAIZHAN_FIRSTWIN_YUANBAO").value);
		CanShu.BAIZHAN_FREE_TIMES = Integer.parseInt(map.get("BAIZHAN_FREE_TIMES").value);

		CanShu.MAXTIME_BAIZHAN = Integer.parseInt(map.get("MAXTIME_BAIZHAN").value);
		CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_1 = Double.parseDouble(map.get("CHUSHIHUA_CHUANDAIZHUANGBEI_1").value);
		CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_2 = Double.parseDouble(map.get("CHUSHIHUA_CHUANDAIZHUANGBEI_2").value);
		CanShu.CHUSHIHUA_CHUANDAIZHUANGBEI_3 = Double.parseDouble(map.get("CHUSHIHUA_CHUANDAIZHUANGBEI_3").value);
		CanShu.HUANGYEPVE_AWARD_X = Double.parseDouble(map.get("HUANGYEPVE_AWARD_X").value);
		CanShu.HUANGYEPVP_PRODUCE_P = Double.parseDouble(map.get("HUANGYEPVP_PRODUCE_P").value);
		CanShu.HUANGYEPVP_KILL_K = Double.parseDouble(map.get("HUANGYEPVP_KILL_K").value);
		CanShu.HUANGYEPVP_AWARDTIME = map.get("HUANGYEPVP_AWARDTIME").value;
		CanShu.HUANGYEPVE_FASTCLEAR_TIME = Integer.parseInt(map.get("HUANGYEPVE_FASTCLEAR_TIME").value);
		
		CanShu.REFRESHTIME_DANGPU = map.get("REFRESHTIME_DANGPU").value;
		CanShu.LIANMENG_CREATE_COST = Integer.parseInt(map.get("LIANMENG_CREATE_COST").value);
		
		CanShu.CHAT_WORLD_INTERVAL_TIME = Integer.parseInt(map.get("CHAT_WORLD_INTERVAL_TIME").value);
		CanShu.CHAT_ALLIANCE_INTERVAL_TIME = Integer.parseInt(map.get("CHAT_ALLIANCE_INTERVAL_TIME").value);
		CanShu.CHAT_BROADCAST_INTERVAL_TIME = Integer.parseInt(map.get("CHAT_BROADCAST_INTERVAL_TIME").value);
		CanShu.CHAT_SECRET_INTERVAL_TIME = Integer.parseInt(map.get("CHAT_SECRET_INTERVAL_TIME").value);
		CanShu.CHAT_MAX_WORDS = Integer.parseInt(map.get("CHAT_MAX_WORDS").value);
		
		
		CanShu.YUEKA_TIME = Integer.parseInt(map.get("YUEKA_TIME").value);
		CanShu.ZHOUKA_TIME = Integer.parseInt(map.get("ZHOUKA_TIME").value);
		CanShu.VIPLV_ININT = Integer.parseInt(map.get("VIPLV_ININT").value);
		CanShu.IS_YUEKA_INIT = Integer.parseInt(map.get("IS_YUEKA_INIT").value);
		//高级房屋参数
		CanShu.FANGWUJINGPAI_1 = Integer.parseInt(map.get("FANGWUJINGPAI_1").value);
		CanShu.FANGWUJINGPAI_2 = Integer.parseInt(map.get("FANGWUJINGPAI_2").value);
		CanShu.FANGWUJINGPAI_3 = Integer.parseInt(map.get("FANGWUJINGPAI_3").value);
		CanShu.FANGWUJINGPAI_4 = Integer.parseInt(map.get("FANGWUJINGPAI_4").value);
		CanShu.REFRESHTIME_GAOJIFANGWU = Integer.parseInt(map.get("REFRESHTIME_GAOJIFANGWU").value);
		// 天赋
		CanShu.TIANFULV_DIANSHUADD1 = Integer.parseInt(map.get("TIANFULV_DIANSHUADD1").value);
		CanShu.TIANFULV_DIANSHUADD2 = Integer.parseInt(map.get("TIANFULV_DIANSHUADD2").value);
		CanShu.TIANFULV_DIANSHUADD3 = Integer.parseInt(map.get("TIANFULV_DIANSHUADD3").value);
		CanShu.TIANFULV_DIANSHUADD4 = Integer.parseInt(map.get("TIANFULV_DIANSHUADD4").value);
		CanShu.TIANFULV_DIANSHUADD5 = Integer.parseInt(map.get("TIANFULV_DIANSHUADD5").value);
		
		/*
		 * 探宝冷却时间
		 */
		CanShu.TONGBI_TANBAO_REFRESHTIME = Integer.parseInt(map.get("TONGBI_TANBAO_REFRESHTIME").value);
		CanShu.YUANBAO_TANBAO_REFRESHTIME  = Integer.parseInt(map.get("YUANBAO_TANBAO_REFRESHTIME").value);
		CanShu.TONGBI_TANBAO_FREETIMES  = Integer.parseInt(map.get("TONGBI_TANBAO_FREETIMES").value);
		CanShu.TONGBI_TANBAO_BAODI=  Integer.parseInt(map.get("TONGBI_TANBAO_BAODI").value);
		CanShu.YUANBAO_TANBAO_BAODI=  Integer.parseInt(map.get("YUANBAO_TANBAO_BAODI").value);

		CanShu.XILIANADD_MIN = Integer.parseInt(map.get("XILIANADD_MIN").value);
		CanShu.XILIANADD_MAX = Integer.parseInt(map.get("XILIANADD_MAX").value);
		
		CanShu.REFRESHTIME = Integer.parseInt(map.get("REFRESHTIME").value);
		/*
		 * 掠夺
		 */
		CanShu.OPENTIME_LUEDUO = map.get("OPENTIME_LUEDUO").value;
		CanShu.CLOSETIME_LUEDUO = map.get("CLOSETIME_LUEDUO").value;
		CanShu.LUEDUO_MAXNUM = Integer.parseInt(map.get("LUEDUO_MAXNUM").value);
		CanShu.LUEDUO_CD = Integer.parseInt(map.get("LUEDUO_CD").value);
		CanShu.LUEDUO_PROTECTTIME = Integer.parseInt(map.get("LUEDUO_PROTECTTIME").value);
		CanShu.LUEDUO_RECOVER_INTERVAL_TIME = Integer.parseInt(map.get("LUEDUO_RECOVER_INTERVAL_TIME").value);
		CanShu.LUEDUO_RECOVER_PERCENT =  Double.parseDouble(map.get("LUEDUO_RECOVER_PERCENT").value);
		CanShu.LUEDUO_RESOURCE_PERCENT = Double.parseDouble(map.get("LUEDUO_RESOURCE_PERCENT").value);
		CanShu.LUEDUO_JIANSHE_REDUCE = Integer.parseInt(map.get("LUEDUO_JIANSHE_REDUCE").value);
		CanShu.LUEDUO_RESOURCE_MINNUM = Integer.parseInt(map.get("LUEDUO_RESOURCE_MINNUM").value);
		CanShu.LUEDUO_HAND_DAYMINNUM = Integer.parseInt(map.get("LUEDUO_HAND_DAYMINNUM").value);
		CanShu.LUEDUO_HAND_WEEKMINNUM = Integer.parseInt(map.get("LUEDUO_HAND_WEEKMINNUM").value);
		CanShu.LUEDUO_DAYAWARD_GIVETIME = map.get("LUEDUO_DAYAWARD_GIVETIME").value;
		CanShu.LUEDUO_WEEKWARD_GIVETIME = map.get("LUEDUO_WEEKWARD_GIVETIME").value;
		CanShu.MAXTIME_LUEDUO = Integer.parseInt(map.get("MAXTIME_LUEDUO").value);
		CanShu.LIANMENG_JIBAI_PRICE = Integer.parseInt(map.get("LIANMENG_JIBAI_PRICE").value);
		CanShu.LUEDUO_PERSONRANKAWARD_GIVETIME = map.get("LUEDUO_PERSONRANKAWARD_GIVETIME").value;
		CanShu.LUEDUO_LIANMENGRANKAWARD_GIVETIME = map.get("LUEDUO_LIANMENGRANKAWARD_GIVETIME").value;
		CanShu.LUEDUO_CANSHU_L =  Float.parseFloat(map.get("LUEDUO_CANSHU_L").value);
		CanShu.LUEDUO_CANSHU_N =  Float.parseFloat(map.get("LUEDUO_CANSHU_N").value);
		CanShu.LUEDUO_CANSHU_M =  Float.parseFloat(map.get("LUEDUO_CANSHU_M").value);
		CanShu.LUEDUO_CANSHU_X =  Float.parseFloat(map.get("LUEDUO_CANSHU_X").value);
		CanShu.LUEDUO_CANSHU_Y =  Float.parseFloat(map.get("LUEDUO_CANSHU_Y").value);
		CanShu.LUEDUO_CANSHU_Z =  Float.parseFloat(map.get("LUEDUO_CANSHU_Z").value);
		CanShu.LUEDUO_CANSHU_A =  Float.parseFloat(map.get("LUEDUO_CANSHU_A").value);
		CanShu.LUEDUO_CANSHU_B =  Float.parseFloat(map.get("LUEDUO_CANSHU_B").value);
		CanShu.LUEDUO_CANSHU_C =  Float.parseFloat(map.get("LUEDUO_CANSHU_C").value);
		CanShu.LUEDUO_GONGJIN_INIT = Integer.parseInt(map.get("LUEDUO_GONGJIN_INIT").value);
		

		/*
		 * 驱逐
		 */
		CanShu.EXPEL_TIMELIMIT = Integer.parseInt(map.get("EXPEL_TIMELIMIT").value);
		CanShu.EXPEL_DAYTIMES = Integer.parseInt(map.get("EXPEL_DAYTIMES").value);
		CanShu.EXPEL_CD = Integer.parseInt(map.get("EXPEL_CD").value);
		/*
		 * 押镖
		 */
		CanShu.OPENTIME_YUNBIAO = map.get("OPENTIME_YUNBIAO").value;
		CanShu.CLOSETIME_YUNBIAO  = map.get("CLOSETIME_YUNBIAO").value;
		CanShu.REFRESHTIME_YUNBIAO = map.get("REFRESHTIME_YUNBIAO").value;
		CanShu.YUNBIAO_MAXNUM = Integer.parseInt(map.get("YUNBIAO_MAXNUM").value);
		CanShu.JIEBIAO_MAXNUM = Integer.parseInt(map.get("JIEBIAO_MAXNUM").value);
		CanShu.JIEBIAO_CD = Integer.parseInt(map.get("JIEBIAO_CD").value);
		CanShu.JIEBIAO_RESULTBACK_MAXTIME = Integer.parseInt(map.get("JIEBIAO_RESULTBACK_MAXTIME").value);
		CanShu.MAXTIME_JIEBIAO = Integer.parseInt(map.get("MAXTIME_JIEBIAO").value);
		CanShu.YUNBIAOASSISTANCE_INVITEDMAXNUM = Integer.parseInt(map.get("YUNBIAOASSISTANCE_INVITEDMAXNUM").value);
		CanShu.YUNBIAOASSISTANCE_MAXNUM = Integer.parseInt(map.get("YUNBIAOASSISTANCE_MAXNUM").value);
		CanShu.YUNBIAOASSISTANCE_GAIN_SUCCEED = Integer.parseInt(map.get("YUNBIAOASSISTANCE_GAIN_SUCCEED").value);
		CanShu.YUNBIAOASSISTANCE_GAIN_FAIL = Integer.parseInt(map.get("YUNBIAOASSISTANCE_GAIN_FAIL").value);
		CanShu.YUNBIAOASSISTANCE_HPBONUS = Double.parseDouble(map.get("YUNBIAOASSISTANCE_HPBONUS").value);
		/*排行榜*/
		CanShu.RANK_MAXNUM = Integer.parseInt(map.get("RANK_MAXNUM").value);
		CanShu.RANK_MINLEVEL = Integer.parseInt(map.get("RANK_MINLEVEL").value);
		
		CanShu.REFRESHTIME_PURCHASE =  Integer.parseInt(map.get("REFRESHTIME_PURCHASE").value);
		/*洗练*/
		CanShu.XILIANZHI_Free =  Integer.parseInt(map.get("XILIANZHI_Free").value);
		CanShu.XILIANZHI_YUANBAO =  Integer.parseInt(map.get("XILIANZHI_YUANBAO").value);
		CanShu.XILIANSHI_MAXTIMES=  Integer.parseInt(map.get("XILIANSHI_MAXTIMES").value);
		CanShu.XILIANZHI_MAX=  Integer.parseInt(map.get("XILIANZHI_MAX").value);
		/** 玩家未领取的威望的初始值*/
		CanShu.WEIWANG_INIT=  Integer.parseInt(map.get("WEIWANG_INIT").value);
		CanShu.WUQI_BAOJILV = Double.parseDouble(map.get("WUQI_BAOJILV").value);
		CanShu.JINENG_BAOJILV = Double.parseDouble(map.get("JINENG_BAOJILV").value);
		CanShu.JION_ALLIANCE_LV_MINI = Integer.parseInt(map.get("JION_ALLIANCE_LV_MINI").value);
		
		CanShu.WORLDCHAT_FREETIMES = Integer.parseInt(map.get("WORLDCHAT_FREETIMES").value);
		CanShu.WORLDCHAT_PRICE = Integer.parseInt(map.get("WORLDCHAT_PRICE").value);
		CanShu.BROADCAST_PRICE = Integer.parseInt(map.get("BROADCAST_PRICE").value);
		
		CanShu.LUEDUO_AWARDEDCOMFORT_MAXTIMES =  Integer.parseInt(map.get("LUEDUO_AWARDEDCOMFORT_MAXTIMES").value);
		CanShu.YUNBIAO_AWARDEDCOMFORT_MAXTIMES =  Integer.parseInt(map.get("YUNBIAO_AWARDEDCOMFORT_MAXTIMES").value);
		CanShu.TILI_JILEI_SHANGXIAN =  Integer.parseInt(map.get("TILI_JILEI_SHANGXIAN").value);
		CanShu.FANGWU_INITIAL_EXP =  Integer.parseInt(map.get("FANGWU_INITIAL_EXP").value);
		CanShu.CONGRATULATE_AWARD_K =  Double.parseDouble(map.get("CONGRATULATE_AWARD_K").value);
		CanShu.CONGRATULATE_AWARD_B =  Double.parseDouble(map.get("CONGRATULATE_AWARD_B").value);
		CanShu.BIAOQING_INTERVAL =  Integer.parseInt(map.get("BIAOQING_INTERVAL").value);
		CanShu.GREETED_CD =  Integer.parseInt(map.get("GREETED_CD").value);
		CanShu.LIEFU_FREETIMES_REFRESH = map.get("LIEFU_FREETIMES_REFRESH").value;
		CanShu.CHANGE_COUNTRY_COST = Integer.parseInt(map.get("CHANGE_COUNTRY_COST").value);
		CanShu.LIANMENG_LVLUP_REDUCE = Integer.parseInt(map.get("LIANMENG_LVLUP_REDUCE").value);
		CanShu.EQUIPMENT_FREETIMES = Integer.parseInt(map.get("EQUIPMENT_FREETIMES").value);
		CanShu.BAOSHI_FREETIMES = Integer.parseInt(map.get("BAOSHI_FREETIMES").value);
		CanShu.QIANGHUA_FREETIMES = Integer.parseInt(map.get("QIANGHUA_FREETIMES").value);
		CanShu.JINGQI_FREETIMES = Integer.parseInt(map.get("JINGQI_FREETIMES").value);
		CanShu.YUEKA_YUANBAO = Integer.parseInt(map.get("YUEKA_YUANBAO").value);
		CanShu.ZHONGSHENKA_YUANBAO = Integer.parseInt(map.get("ZHONGSHENKA_YUANBAO").value);
		CanShu.ZHOUKA_YUANBAO = Integer.parseInt(map.get("ZHOUKA_YUANBAO").value);
		CanShu.MEIRI_DINGSHIZENGSONG_TILI = Integer.parseInt(map.get("MEIRI_DINGSHIZENGSONG_TILI").value);
		CanShu.CHENGZHANGJIJIN_VIP = Integer.parseInt(map.get("CHENGZHANGJIJIN_VIP").value);
		CanShu.CHENGZHANGJIJIN_COST = Integer.parseInt(map.get("CHENGZHANGJIJIN_COST").value);
		CanShu.CHENGZHANGJIJIN_REBATE = Integer.parseInt(map.get("CHENGZHANGJIJIN_REBATE").value);
		CanShu.LIANMXIAOWU_EXP = Double.parseDouble(map.get("LIANMXIAOWU_EXP").value);
		CanShu.LIEFU_BLUE_FIRSTAWARD = map.get("LIEFU_BLUE_FIRSTAWARD").value;
		CanShu.LIEFU_GREEN_FIRSTAWARD = map.get("LIEFU_GREEN_FIRSTAWARD").value;
		CanShu.ZHUANGBEI_FIRSTAWARD = map.get("ZHUANGBEI_FIRSTAWARD").value;
		CanShu.CHANGE_COUNTRY_CD = Integer.parseInt(map.get("CHANGE_COUNTRY_CD").value);
		CanShu.CHANGE_NAME_CD = Integer.parseInt(map.get("CHANGE_NAME_CD").value);
		CanShu.SHILIAN_CLEARCD_COST = Integer.parseInt(map.get("SHILIAN_CLEARCD_COST").value);
		CanShu.GUOGUAN_RANK_MINLEVEL = Integer.parseInt(map.get("GUOGUAN_RANK_MINLEVEL").value);
		CanShu.BAIZHAN_RANK_MINLEVEL = Integer.parseInt(map.get("BAIZHAN_RANK_MINLEVEL").value);
		CanShu.CHONGLOU_RANK_MINLEVEL = Integer.parseInt(map.get("CHONGLOU_RANK_MINLEVEL").value);
	}
	
	public void loadJCZTemp(){
		List<JCZTemp> list = listAll(JCZTemp.class.getSimpleName());
		Map<String, JCZTemp> map = new HashMap<String, JCZTemp>();
		for(JCZTemp jTemp : list){
			map.put(jTemp.key, jTemp);
		}
		JCZTemp.declaration_startTime  = map.get("declaration_startTime").value;
		JCZTemp.preparation_startTime = map.get("preparation_startTime").value;
		JCZTemp.fighting_startTime = map.get("fighting_startTime").value;
		JCZTemp.fighting_endTime = map.get("fighting_endTime").value;
		JCZTemp.buyfenshen_price = map.get("buyfenshen_price").value;
	}

	public ZhuangBei getZhuangBei(int id) {
		return equipMaps.get(id);
	}

	public ExpTemp getExpTemp(int expId, int level) {
		List<ExpTemp> subs = expTempMaps.get(expId);
		if (subs != null) {
			for (ExpTemp o : subs) {
				if (o.level == level) {
					return o;
				}
			}
		}
		return null;
	}

	public List<ExpTemp> getExpTemps(int expId) {
		return expTempMaps.get(expId);

	}

	public QiangHua getQiangHua(int qianghuaId, int level) {
		List<QiangHua> subs = qiangHuaMaps.get(qianghuaId);
		if (subs != null) {
			for (QiangHua o : subs) {
				if (o.level == level) {
					return o;
				}
			}
		}
		return null;
	}

	public List<QiangHua> getQiangHuas(int qianghuaId) {
		return qiangHuaMaps.get(qianghuaId);

	}
	
	public ItemTemp getItemTemp(int id) {
		return itemTempMap.get(id);
	}
	
	public static int[] parseAwardString(String awardStr) {
		int[] awardArray = new int[]{};
		if(awardStr == null){
			return awardArray;
		}
		if(awardStr.isEmpty()){
			return awardArray;
		}
		String[]tmp = awardStr.split(",");
		int[] arr = new int[tmp.length*2];
		int idx = -2;
		for(String pair : tmp){
			idx+=2;
			String []tmpPair = pair.split("=");
			int aId = 0;//Integer.valueOf(tmpPair[0]);
			int roll = 0;//Integer.valueOf(tmpPair[1]);
			try{
				aId = Integer.valueOf(tmpPair[0]);
				roll = Integer.valueOf(tmpPair[1]);
			}catch(Exception e){
				AwardMgr.log.error("加载配置文件，有奖励字段配置错误awardStr:{}",awardStr);
				continue;
			}
			arr[idx] = aId;
			arr[idx+1]=roll;
		}
		awardArray = arr;
		return awardArray;
	}
}
