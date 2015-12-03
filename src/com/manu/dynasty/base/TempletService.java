package com.manu.dynasty.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.ItemTemp;
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
	public TempletService() {

	}

	public static TempletService getInstance() {
		return templetService;
	}
	public void buildItemMap(){
		Map<Integer, BaseItem> map = new HashMap<Integer, BaseItem>();
		List<BaseItem> list = TempletService.listAll(ItemTemp.class.getSimpleName());
		addList(list,map);
		list = TempletService.listAll(ZhuangBei.class.getSimpleName());
		addList(list,map);
		itemMap = map;
	}

	protected  void addList(List<BaseItem> list, Map<Integer, BaseItem> map) {
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
	private void add(String key, Object data, Map<String, List<?>> dataMap) {
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
				int expId = o.getExpId();
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
				int qianghuaId = o.getQianghuaId();
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
	}
	//加载押镖配置
	public void loadYunbiaoTemp() {
		List<YunbiaoTemp> list = listAll(YunbiaoTemp.class.getSimpleName());
		Map<String, YunbiaoTemp> map = new HashMap<String, YunbiaoTemp>();
		for(YunbiaoTemp yb: list){
			map.put(yb.getKey(), yb);
		}

//		YunbiaoTemp.centerX=Integer.parseInt(map.get("centerX").value);
//		YunbiaoTemp.centerY=Integer.parseInt(map.get("centerY").value);
//		YunbiaoTemp.saveArea1X=Integer.parseInt(map.get("saveArea1X").value);
//		YunbiaoTemp.saveArea1Z=Integer.parseInt(map.get("saveArea1Z").value);
//		YunbiaoTemp.saveArea1r=Integer.parseInt(map.get("saveArea1r").value);
//		YunbiaoTemp.saveArea2X=Integer.parseInt(map.get("saveArea2X").value);
//		YunbiaoTemp.saveArea2Z=Integer.parseInt(map.get("saveArea2Z").value);
//		YunbiaoTemp.saveArea2r=Integer.parseInt(map.get("saveArea2r").value);
//		YunbiaoTemp.saveArea3X=Integer.parseInt(map.get("saveArea3X").value);
//		YunbiaoTemp.saveArea3Z=Integer.parseInt(map.get("saveArea3Z").value);
//		YunbiaoTemp.saveArea3r=Integer.parseInt(map.get("saveArea3r").value);
//		YunbiaoTemp.saveArea4X=Integer.parseInt(map.get("saveArea4X").value);
//		YunbiaoTemp.saveArea4Z=Integer.parseInt(map.get("saveArea4Z").value);
//		YunbiaoTemp.saveArea4r=Integer.parseInt(map.get("saveArea4r").value);
		YunbiaoTemp.saveArea_recoveryPro=Integer.parseInt(map.get("saveArea_recoveryPro").value);
		YunbiaoTemp.saveArea_people_max=Integer.parseInt(map.get("saveArea_people_max").value);
		YunbiaoTemp.bloodVial_recoveryPro=Integer.parseInt(map.get("bloodVial_recoveryPro").value);
		YunbiaoTemp.bloodVialCD=Integer.parseInt(map.get("bloodVialCD").value);
		YunbiaoTemp.bloodVial_freeTimes=Integer.parseInt(map.get("bloodVial_freeTimes").value);
		YunbiaoTemp.resurgenceTimes=Integer.parseInt(map.get("resurgenceTimes").value);
		YunbiaoTemp.incomeAddPro=Integer.parseInt(map.get("incomeAddPro").value);
		YunbiaoTemp.incomeAdd_startTime1=map.get("incomeAdd_startTime1").value;
		YunbiaoTemp.incomeAdd_endTime1=map.get("incomeAdd_endTime1").value;
		YunbiaoTemp.incomeAdd_startTime2=map.get("incomeAdd_startTime2").value;
		YunbiaoTemp.incomeAdd_endTime2=map.get("incomeAdd_endTime2").value;
		YunbiaoTemp.income_lossless_price=Integer.parseInt(map.get("income_lossless_price").value);
		YunbiaoTemp.protectDuration=Integer.parseInt(map.get("protectDuration").value);
		YunbiaoTemp.protectionCD=Integer.parseInt(map.get("protectionCD").value);
		YunbiaoTemp.speedUpDuration=Integer.parseInt(map.get("speedUpDuration").value);
		YunbiaoTemp.speedUpEffect=Integer.parseInt(map.get("speedUpEffect").value);
		YunbiaoTemp.speedUpCD=Integer.parseInt(map.get("speedUpCD").value);
		YunbiaoTemp.speedUpPrice=Integer.parseInt(map.get("speedUpPrice").value);
		YunbiaoTemp.cart_attribute_pro=Integer.parseInt(map.get("cart_attribute_pro").value);
		YunbiaoTemp.foeCart_incomeAdd_pro=Integer.parseInt(map.get("foeCart_incomeAdd_pro").value);
		YunbiaoTemp.killFoeAward=map.get("killFoeAward").value;
		YunbiaoTemp.rewarding_killFoe_max=Integer.parseInt(map.get("rewarding_killFoe_max").value);
		YunbiaoTemp.cartAI_refresh_interval=Integer.parseInt(map.get("cartAI_refresh_interval").value);
		YunbiaoTemp.cartAImax=Integer.parseInt(map.get("cartAImax").value);
		YunbiaoTemp.cartAILvlMin=Integer.parseInt(map.get("cartAILvlMin").value);
		YunbiaoTemp.cartAILvlMax=Integer.parseInt(map.get("cartAILvlMax").value);
	}

	public void loadCanShu() {
		List<CanShu> list = listAll(CanShu.class.getSimpleName());
		Map<String, CanShu> map = new HashMap<String, CanShu>();
		for(CanShu c : list){
			map.put(c.getKey(), c);
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
		
		/*
		 * 百战掠夺建设和威望奖励
		 */
		CanShu.BAIZHAN_LVEDUO_K = Double.parseDouble(map.get("BAIZHAN_LVEDUO_K").value);
		CanShu.BAIZHAN_LVEDUO_JIANSHEZHI = Integer.parseInt(map.get("BAIZHAN_LVEDUO_JIANSHEZHI").value);
		CanShu.BAIZHAN_NPC_WEIWANG = Integer.parseInt(map.get("BAIZHAN_NPC_WEIWANG").value);
		CanShu.BAIZHAN_WEIWANG_ADDLIMIT = Integer.parseInt(map.get("BAIZHAN_WEIWANG_ADDLIMIT").value);

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
		CanShu.CHAT_INTERVAL_TIME = Integer.parseInt(map.get("CHAT_INTERVAL_TIME").value);
		
		CanShu.YUEKA_TIME = Integer.parseInt(map.get("YUEKA_TIME").value);
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
	}

	public ZhuangBei getZhuangBei(int id) {
		return equipMaps.get(id);
	}

	public ExpTemp getExpTemp(int expId, int level) {
		List<ExpTemp> subs = expTempMaps.get(expId);
		if (subs != null) {
			for (ExpTemp o : subs) {
				if (o.getLevel() == level) {
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
				if (o.getLevel() == level) {
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
