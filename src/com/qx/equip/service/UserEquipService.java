package com.qx.equip.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.UserEquipProtos.EquipStrength4AllResp;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthReq;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthResp;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.util.BaseException;
import com.qx.account.FunctionOpenMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.domain.UserEquipDao;
import com.qx.equip.web.UEConstant;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.hero.HeroMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
/**
 * @Description 强化
 *
 */
public class UserEquipService {
	public static Logger log = LoggerFactory.getLogger(UserEquipService.class.getSimpleName());
	public static UserEquipService instance = new UserEquipService();
	public TempletService template = TempletService.getInstance();
	public static UserEquipService getInstance() {
		return instance;
	}
	
	/** 
	 * @Description 强化装备
	 */
	public void doUpgradeEquip(IoSession session, EquipStrengthReq.Builder req) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			log.error("未发现君主");
			return;
		}
		if(!FunctionOpenMgr.inst.isFunctionOpen(UserEquipAction.qiangHuaFunctionId, junZhu.id, junZhu.level)){
			log.error("君主{}尝试装备强化失败，等级{}尚未开启装备强化功能",junZhu.id , junZhu.level);
			return ;
		}
		int junzhulevel=junZhu.level;
		Long junZhuId = junZhu.id;
		long equipId = req.getEquipId();
		List<Long> caiLiaoList = req.getUsedEquipIdsList();
		int equipWhere = req.getType();
		boolean doIt = caiLiaoList.size() > 0;//没有材料表示请求强化数据。
		if(caiLiaoList.contains(equipId)){
			log.error("要强化的装备不能出现在材料中");
			return ;
		}
		long targetInstId = -1;
		int targetItemId = -1;
		Object target = null;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		if(equipWhere == 1){//背包内装备
			BagGrid source = findByDBId(bag, equipId);
			if (source == null) {
				throw new BaseException("强化装备不存在 1");
			}
			target = source;
			targetItemId = source.itemId;
			targetInstId = source.instId;
		}else if(equipWhere == 2){//身上穿戴装备
			EquipGrid source = EquipMgr.inst.findEquip(junZhu.id, equipId);
			if (source == null) {
				log.error("强化失败：君主{}的装备{}不存在",junZhu.id,equipId);
				throw new BaseException("强化装备不存在 2");
			}
			target = source;
			targetItemId = source.itemId;
			targetInstId = source.instId;
		}else{
			throw new BaseException("type error."+equipWhere);
		}
		ZhuangBei zhuangbeiCfg = template.getZhuangBei(targetItemId);
		if(zhuangbeiCfg == null){
			log.error("装备配置找不到 zhuangBeiId:{}", targetItemId);
			return ;
		}
		
		UserEquip dbUe = null;
		if(targetInstId > 0){
			dbUe = UserEquipDao.find(junZhuId, targetInstId);
			if(dbUe == null){
				log.error("找不到装备equipId:{}的强化数据instId:{}", equipId, targetInstId);
				return ;
			}
		}else{
			dbUe = new UserEquip();
			dbUe.userId = junZhuId;
			dbUe.templateId = zhuangbeiCfg.getId();
		}
		int curExp = dbUe.exp;
		int currLv = dbUe.level;
		int curTongli = dbUe.tongli;
		int curMouli = dbUe.mouli;
		int curWuli =  dbUe.wuli;
		if (doIt && currLv >= zhuangbeiCfg.qianghuaMaxLv) {
			log.error("强化等级已经达到满级, equipId:{}", equipId);
			return ;
		}
		if (doIt && currLv >= junzhulevel) {
			log.error("强化等级已经达到君主等级, equipId:{}", equipId);
			return ;
		}
		Map<Long,Integer> caiLiaoIds = new LinkedHashMap<Long,Integer>();
		for(Long dbId : caiLiaoList){
			Integer cnt = caiLiaoIds.get(dbId);
			if(cnt == null){
				cnt = 1;
			}else{
				cnt += 1;
			}
			caiLiaoIds.put(dbId, cnt);
		}
		// 过滤所选强化材料的合法性
		List<BagGrid> materials = new ArrayList<BagGrid>(caiLiaoList.size());
		boolean isWuQi = HeroMgr.inst.isWuQi(zhuangbeiCfg.getBuWei());
		if(doIt)log.info("请求中的材料个数{}", caiLiaoList.size());
		for (Long bagId : caiLiaoIds.keySet()) {
				BagGrid bagGrid = findByDBId(bag, bagId);
				if(bagGrid == null || bagGrid.cnt<=0){
					log.error("背包里找不到材料,bagId:{},材料id:{}", bagId, bagGrid==null?" bagGrid==null":bagGrid.itemId);
					continue;
				}
				if(bagGrid.dbId/BagMgr.spaceFactor != junZhuId){
					log.error("{}请求消耗别人的装备{}进行强化", junZhuId,bagGrid.dbId);
					continue;
				}
				if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao && isWuQi){
					log.error("不能用防具材料强化武器,bagId:{},材料id:{}", bagId, bagGrid.itemId);
					return ;
				}
				if(bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao && !isWuQi){
					log.error("不能用武器材料强化防具,bagId:{},材料id:{}", bagId, bagGrid.itemId);
					return ;
				}
				//功能修改，不再可以使用装备、装备进阶材料等进行强化
				if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao 
						|| bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao){
					BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
					if(bi == null){
						continue;
					}
//					ItemTemp it = (ItemTemp) bi;//进阶材料检测，
//					//进阶材料的(品质)  小于  对应（部位it.id%100） 穿戴的装备品质，可以吃 (itemType==6)表示进阶材料 
//					if (it.itemType==BaseItem.TYPE_JinJie_CaiLiao) {
//						int buwei=-1;
//						buwei=TempletService.itemMap4Buwei.get(it.id);
//						if(buwei<=0){
//							log.error("进阶材料--{} 的部位获取错误",it.id);
//							continue;
//						}
//						//TODO，装备部位增加新位置时，修改此处判断
//						//材料部位判断，1、2、3为武器，10以上为装备
//						if( buwei > 0 && buwei <= 3 && !isWuQi ){//材料对应部位是武器，强化目标不是武器
//							log.error("不能用武器进阶材料强化防具,bagId:{},材料id:{}", bagId, bagGrid.itemId);
//							continue;
//						}
//						if(buwei >= 10 && isWuQi ){//材料对应部位是装备，强化目标不是武器
//							log.error("不能用防具进阶材料强化武器,bagId:{},材料id:{}", bagId, bagGrid.itemId);
//							continue;
//						}
//					}
					
					int cnt = caiLiaoIds.get(bagGrid.dbId);
					if(cnt>bagGrid.cnt){
						log.error("材料个数错误，bagId:{}-itemId:{},实际有{}，请求个数{}",
								bagId, bagGrid.itemId, bagGrid.cnt, cnt);
						continue;
					}
				}else{
					continue;
//					ZhuangBei t = template.getZhuangBei(bagGrid.itemId);//2016-05-10不再可以使用低级装备进行强化
//					if(t == null){
//						continue;
//					}
//					if(isWuQi){
//						if (!HeroMgr.inst.isWuQi(t.getBuWei())) {
//							log.error("不能用防具强化武器");
//							return ;
//						}
//					}else{
//						if (HeroMgr.inst.isWuQi(t.getBuWei())) {
//							log.error("不能用武器强化防具");
//							return ;
//						}
//					}
				}
				materials.add(bagGrid);
		}
		
		int expId = zhuangbeiCfg.getExpId();
		if(doIt)log.info("装备原有经验为 {},使用经验ID {}",curExp, expId);
		List<ExpTemp> expTemps = template.getExpTemps(expId);
		if(expTemps == null){
			sendError(session, "没有经验配置。");
			return ;
		}
		if(expTemps.size()<=1){
			sendError(session, "不可强化。");
			return;
		}
		if(hasBadExp(expTemps)){
			log.error("强化经验配置错误，装备id{}",targetItemId);
			return;
		}
		int toEqJzLevelNeedExp = 0;//升到与君主等级一样所需的最大经验值
		for (int i = currLv; i < junzhulevel; i++) {
			ExpTemp	expTemp = expTemps.get(i);
			toEqJzLevelNeedExp += expTemp.needExp;
		}
		toEqJzLevelNeedExp -= curExp;
		
		if(doIt)log.info("当前君主等级为{}，装备当前强化等级为{}，升级装备到与君主等级相同需要经验为{}",junzhulevel, currLv, toEqJzLevelNeedExp);
		JSONArray logCaiLiao = new JSONArray();
		for (BagGrid bagGrid : materials) {
			if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao 
					|| bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao){//2016-05-12修改不再可以使用装备进阶材料强化
				BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
				if(bi == null || !(bi instanceof ItemTemp)){
					continue;
				}
				ItemTemp it = (ItemTemp) bi;
				int cnt = caiLiaoIds.get(bagGrid.dbId);
				int canUseCount=0;
				for (int i = 0; i < cnt; i++) {
					// TODO 这里是为了控制消耗的材料总经验超过所需最大经验，减少扣除的材料数量。
					// 暂时这个版本先不考虑一并扣除，等策划确定需要这需求再做处理
//					if(gainExp<nowMaxNeedExp){
						curExp += it.effectId;
						canUseCount++;
//					}
				}
				log.info("可消耗物品个数为{}",canUseCount);
				bagGrid.cnt -= canUseCount;
				log.info("强化材料使用成功,[{}]使用强化材料{} x {} 获得经验{}，剩余个数{}",junZhuId,it.getId(),canUseCount,canUseCount*it.effectId,bagGrid.cnt);
				addLogJsonCaiLiao(logCaiLiao, it.getId(), canUseCount);
				HibernateUtil.save(bagGrid);

				continue;
			}
			// 使用的武器强化，可获得的经验数
//			ZhuangBei t = template.getZhuangBei(bagGrid.itemId);//2016-05-12修改，不再可以使用背包中的武器强化
//			if(t == null){
//				log.error("模板没有找到:"+bagGrid.itemId);
//				continue;
//			}
//			log.info("获得模板经验{}",t.getExp());
//			curExp += t.getExp();
//			if(bagGrid.instId > 0) {// FIXME 武器是否强化过，计算数值不对，应把之前等级所耗掉的经验都加上
//				UserEquip mUe = HibernateUtil.find(UserEquip.class, bagGrid.instId);
//				if(mUe != null){
//					curExp += mUe.getExp();
//					// TODO 洗练继承操作 （还要考虑有损继承）
//					curTongli += mUe.getTongli();
//					curMouli += mUe.getMouli();
//					curWuli += mUe.getWuli();
//					
//					log.info("材料之前强化经验{}，洗练统力{}，谋力{}，武力{}",mUe.getExp(),mUe.getTongli(), mUe.getMouli(), mUe.getWuli());
//					HibernateUtil.delete(mUe);
//				}else{
//					log.warn("之前强化信息未找到{}",bagGrid.instId);
//				}
//			}
//			
//			bagGrid.instId = bagGrid.itemId = bagGrid.cnt = 0;
//			HibernateUtil.save(bagGrid);
//			log.info("强化材料使用成功-装备，删除背包物品 {} from pid {}",bagGrid.itemId, junZhuId);
//			addLogJsonCaiLiao(logCaiLiao, bagGrid.itemId, 1);
		}


		if(doIt){
			final int preLv = dbUe.level;
			int newLv = dbUe.level;
			while(curExp > 0){
				int upNeed = expTemps.get(newLv).needExp;
				if(upNeed<=0){
					// FIXME 假如经验，满级后能否进行强化
					upNeed = 10 * newLv+1;
					log.info("使用假的升级经验{}",upNeed);
				}
				//2015年10月8日改成 强化经验超出时，一直用掉
//				if(newLv >= junzhulevel) {
//					//2015年10月8日改成 强化经验超出时，要保留
////					curExp = 0;
//					break;
//				}
				if(curExp < upNeed){
					break;
				}
				log.info("消耗 {} 升级至 {}",upNeed,newLv);
				newLv++;
				curExp -= upNeed;
				if(newLv>(expTemps.size()-1)){//2015年9月17日 1.0expTempsl里面的参数从0开始计算  ExpTemp只有100级
					newLv=expTemps.size()-1;
					log.info("君主--{}装备UserEquip--equipId---{}已达到最大等级",junZhuId,dbUe.equipId);
					break;
				}
			}
			
			dbUe.exp = curExp ;
			dbUe.level = newLv;
			dbUe.tongli = curTongli;
			dbUe.mouli = curMouli;
			dbUe.wuli = curWuli;
			if(targetInstId<=0){
				UserEquipDao.insert(dbUe);
				MC.add(dbUe, dbUe.getIdentifier());
				targetInstId = dbUe.getIdentifier();
				if(equipWhere == 1){ ((BagGrid)target).instId = targetInstId;}
				else if(equipWhere == 2){ ((EquipGrid)target).instId = targetInstId;}
				//方法开头检测过where合法性了。
				HibernateUtil.save(target);
				log.info("bind inst id {}", targetInstId);
			}else{
				HibernateUtil.save(dbUe);
			}
			log.info("装备强化成功,junzhuId:{},强化装备-dbUe.equipId:{},由LV:{}升级至 LV:{} exp {}, tongli {}, mouli {}, wuli {}",
					junZhu.id, dbUe.equipId,currLv, newLv, curExp, curTongli, curMouli, curWuli);
			ActLog.log.EquipStrength(junZhu.id, junZhu.name, (int)dbUe.equipId, zhuangbeiCfg.getName(), preLv, newLv, logCaiLiao);
			// 主线任务：完成一次强化
			EventMgr.addEvent(junZhu.id,ED.QIANG_HUA_FINISH, new Object[]{junZhuId, newLv, junZhu});
			// 每日任务中记录完成强化装备1次
			EventMgr.addEvent(junZhu.id,ED.DAILY_TASK_PROCESS, 
					new DailyTaskCondition(junZhuId, DailyTaskConstants.qianghua_id, 1));
		}
		EquipStrengthResp.Builder resp = EquipStrengthResp.newBuilder();
		resp.setEquipId(equipId);
		resp.setExp(dbUe.exp);
		resp.setLevel(dbUe.level);
		if(dbUe.level >= zhuangbeiCfg.qianghuaMaxLv){
			resp.setExpMax(-1);
		}else{
			resp.setExpMax(expTemps.get(dbUe.level).needExp);
		}
		//计算最大强化数值
		List<QiangHua> qhConfList = TempletService.qiangHuaMaps.get(zhuangbeiCfg.getQianghuaId());
		QiangHua qhConf = qhConfList.get(qhConfList.size()-1);
		resp.setGongJiMax(zhuangbeiCfg.gongji+qhConf.gongji);
		resp.setFangYuMax(zhuangbeiCfg.fangyu+qhConf.fangyu);
		resp.setShengMingMax(zhuangbeiCfg.getShengming()+qhConf.shengming);
		QiangHua qianghua = null;
		if(dbUe.level>0){
			qianghua = template.getQiangHua(zhuangbeiCfg.getQianghuaId(), dbUe.level);
		}
		if(qianghua == null){
			resp.setGongJi(zhuangbeiCfg.gongji);
			resp.setFangYu(zhuangbeiCfg.fangyu);
			resp.setShengMing(zhuangbeiCfg.getShengming());
			log.info("没有强化配置 {} lv {}", zhuangbeiCfg.getId(), dbUe.level);
		}else{
			resp.setGongJi(zhuangbeiCfg.gongji+qianghua.gongji);
			resp.setFangYu(zhuangbeiCfg.fangyu+qianghua.fangyu);
			resp.setShengMing(zhuangbeiCfg.getShengming()+qianghua.shengming);
		}
		if(dbUe.level<zhuangbeiCfg.getQianghuaMaxLv()){
			qianghua = template.getQiangHua(zhuangbeiCfg.getQianghuaId(), dbUe.level+1);
			if(qianghua == null){
				log.error("没有找到 下一级 强化配置 {} lv {}", zhuangbeiCfg.getId(), dbUe.level);
			}
		}
		if(qianghua != null){
			resp.setGongJiAdd(qianghua.gongji);
			resp.setFangYuAdd(qianghua.fangyu);
			resp.setShengMingAdd(qianghua.shengming);
		}
		
		
		//以下1.0版本改变洗练逻辑
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqSH)) {
			resp.setWqSH(zhuangbeiCfg.getWqSH() + dbUe.wqSH);
		}else{
			resp.setWqSH(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqJM)) {
			resp.setWqJM(zhuangbeiCfg.getWqJM() + dbUe.wqJM);
		}else{
			resp.setWqJM(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqBJ)) {
			resp.setWqBJ(zhuangbeiCfg.getWqBJ() + dbUe.wqBJ);
		}else{
			resp.setWqBJ(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqRX)) {
			resp.setWqRX(zhuangbeiCfg.getWqRX() + dbUe.wqRX);
		}else{
			resp.setWqRX(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnSH)) {
			resp.setJnSH(zhuangbeiCfg.getJnSH() + dbUe.jnSH);
		}else{
			resp.setJnSH(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnJM)) {
			resp.setJnJM(zhuangbeiCfg.getJnJM() + dbUe.jnJM);
		}else{
			resp.setJnJM(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnBJ)) {
			resp.setJnBJ(zhuangbeiCfg.getJnBJ() + dbUe.jnBJ);
		}else{
			resp.setJnBJ(0);
		}
		if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnRX)) {
			resp.setJnRX(zhuangbeiCfg.getJnRX() + dbUe.jnRX);
		}else{
			resp.setJnRX(0);
		}
		//TODO 以下 1.1可能不加入
		//FIXME 以下5个属性洗出不变 >0就是有此属性
		//	武器暴击率        
//		if (dbUe.getWqBJL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqBJL)) {
//			resp.setWqBJL(dbUe.getWqBJL());
//		}else{
//			resp.setWqBJL(0);
//		}
//		//技能暴击率           
//		if (dbUe.getJnBJL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnBJL)) {
//			resp.setJnBJL(dbUe.getJnBJL());
//		}else{
//			resp.setJnBJL(0);
//		}
//		//武器免暴率 	
//		if (dbUe.getWqMBL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqMBL)) {
//			resp.setWqMBL(dbUe.getWqMBL());
//		}else{
//			resp.setWqMBL(0);
//		}
//		//	技能免暴率      
//		if (dbUe.getJnMBL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnMBL)) {
//			resp.setJnMBL(dbUe.getJnMBL());
//		}else{
//			resp.setJnMBL(0);
//		}
//		//	属性加成
//		if (dbUe.getSxJiaCheng()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.sxJiaCheng)) {
//			resp.setSxJiaCheng(dbUe.getSxJiaCheng());
//		}else{
//			resp.setSxJiaCheng(0);
//		}
		resp.setZhuangbeiID(zhuangbeiCfg.getId());
		resp.setJinJieExp(dbUe.JinJieExp);
		//以上1.0版本改变洗练逻辑
		// 发送君主信息 2015年9月24日
		if(doIt){
			JunZhuMgr.inst.sendMainInfo(session,junZhu);
		}
		if(doIt){
			BagMgr.inst.sendBagInfo(session, bag);
			BagMgr.inst.sendEquipInfo(0, session, null);
		}
		session.write(resp.build());
	}
	
	
	public BagGrid findByDBId(Bag<BagGrid> bag, long equipId) {
		for(BagGrid bg:bag.grids){
			if(bg == null){
				continue;
			}
			if(bg.dbId == equipId){
				return bg;
			}
		}
		return null;
	}

	/**
	 * @Description 一键强化
	 * @param session
	 * @param req
	 */
	public void doUpAllEquips(IoSession session, EquipStrengthReq.Builder req) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			log.error("未发现君主");
			return;
		}	
		if(!FunctionOpenMgr.inst.isFunctionOpen(UserEquipAction.qiangHuaFunctionId, junZhu.id, junZhu.level)){
			log.error("君主{}尝试一键强化失败，等级{}尚未开启装备强化功能",junZhu.id , junZhu.level);
			return ;
		}
		long junZhuId = junZhu.id;
		log.info("君主{}开始==============《一键强化》",junZhuId);
		//{//let me show you
			Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhuId);
			if(equips == null){
				return;
			}
			Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhuId);
			int upWuQiTimes = doUpAll(junZhu,equips, bag, true);//武器
			int upZhuangBeiTimes = doUpAll(junZhu,equips, bag, false);//防具
			if((upWuQiTimes+upZhuangBeiTimes)<=0){
				log.info("玩家{}一键强化中没有任何装备信息发生变动，怀疑修改协议",junZhu.id);
				return;
			}
		//}
		log.info("君主{}结束==============《一键强化》",junZhuId);
		
		// 发送君主信息 2015年9月24日
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		BagMgr.inst.sendBagInfo(session, bag);
		BagMgr.inst.sendEquipInfo(session, equips);
		//返回一键强化信息
		sendQianhuaInfo4YiJian(junZhuId,session,equips);
		//主线任务：完成一次强化, 参数定为-1， 在GameTaskMgr中获取一个最大的newLevel作为条件判断
		EventMgr.addEvent(junZhu.id,ED.QIANG_HUA_FINISH, new Object[]{junZhuId, -1, junZhu});
		// 每日任务中记录完成强化装备1次
		EventMgr.addEvent(junZhu.id,ED.DAILY_TASK_PROCESS, 
				new DailyTaskCondition(junZhuId, DailyTaskConstants.qianghua_id, 1));
	}
	
	/**
	 * @Description 	//返回一键强化信息
	 * @param junZhuId
	 * @param session
	 */
	public void sendQianhuaInfo4YiJian(long junZhuId, IoSession session,Bag<EquipGrid> equips) {
		List<EquipGrid> list = equips.grids;
		int elistSize=list.size();
		EquipStrength4AllResp.Builder allResp=EquipStrength4AllResp.newBuilder();
		for (int i = 0; i < elistSize; i++) {
			EquipGrid eg=list.get(i);
			if(eg==null){
				log.info("装备在{}位置为空", i);
				continue;
			}
			int targetItemId=eg.itemId;
			long equipId =  eg.instId;//dbUe.getEquipId();
			EquipStrengthResp.Builder resp = EquipStrengthResp.newBuilder();
			ZhuangBei zhuangbeiCfg = template.getZhuangBei(targetItemId);
			if(zhuangbeiCfg == null){
				log.error("装备配置找不到 zhuangBeiId:{}", targetItemId);
				continue;
			}
			UserEquip dbUe = UserEquipDao.find(junZhuId, eg.instId);
			int curExp=-1;
			int curLevel=0;
			if(dbUe==null){
				log.error("找不到装备equipId:{}的强化数据instId:{}",  eg.dbId,  eg.instId);
				continue;
			}else{
				curExp=dbUe.exp;
				curLevel=dbUe.level;
			}
			resp.setEquipId(equipId);
			resp.setExp(curExp);
			resp.setLevel(curLevel);
			if(curLevel>= zhuangbeiCfg.qianghuaMaxLv){
				resp.setExpMax(-1);
			}else{
				int expId = zhuangbeiCfg.getExpId();

				List<ExpTemp> expTemps = template.getExpTemps(expId);
				if(expTemps==null){
					log.error("使用经验ID {}错误，未找到List<ExpTemp> expTemps", expId);
				}else{
					ExpTemp et=expTemps.get(curLevel);
					if(et==null){
						log.error("使用经验ID {}错误，未找到最大经验值ExpMax", expId);
						resp.setExpMax(Integer.MAX_VALUE);
					}else{
						resp.setExpMax(et.needExp);
					}
				}
			}
			//计算最大强化数值
			List<QiangHua> qhConfList = TempletService.qiangHuaMaps.get(zhuangbeiCfg.getQianghuaId());
			QiangHua qhConf = qhConfList.get(qhConfList.size()-1);
			resp.setGongJiMax(zhuangbeiCfg.gongji+qhConf.gongji);
			resp.setFangYuMax(zhuangbeiCfg.fangyu+qhConf.fangyu);
			resp.setShengMingMax(zhuangbeiCfg.getShengming()+qhConf.shengming);
			QiangHua qianghua = null;
			if(curLevel>0){
				qianghua = template.getQiangHua(zhuangbeiCfg.getQianghuaId(),curLevel);
			}
			if(qianghua == null){
				resp.setGongJi(zhuangbeiCfg.gongji);
				resp.setFangYu(zhuangbeiCfg.fangyu);
				resp.setShengMing(zhuangbeiCfg.getShengming());
				log.info("没有强化配置 {} lv {}", zhuangbeiCfg.getId(),curLevel);
			}else{
				resp.setGongJi(zhuangbeiCfg.gongji+qianghua.gongji);
				resp.setFangYu(zhuangbeiCfg.fangyu+qianghua.fangyu);
				resp.setShengMing(zhuangbeiCfg.getShengming()+qianghua.shengming);
			}
			if(curLevel<zhuangbeiCfg.getQianghuaMaxLv()){
				qianghua = template.getQiangHua(zhuangbeiCfg.getQianghuaId(),curLevel+1);
				if(qianghua == null){
					log.error("没有找到 下一级 强化配置 {} lv {}", zhuangbeiCfg.getId(),curLevel);
				}
			}
			if(qianghua != null){
				resp.setGongJiAdd(qianghua.gongji);
				resp.setFangYuAdd(qianghua.fangyu);
				resp.setShengMingAdd(qianghua.shengming);
			}
			
			//以下1.0版本改变洗练逻辑
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqSH)) {
				resp.setWqSH(zhuangbeiCfg.getWqSH() + dbUe.wqSH);
			}else{
				resp.setWqSH(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqJM)) {
				resp.setWqJM(zhuangbeiCfg.getWqJM() + dbUe.wqJM);
			}else{
				resp.setWqJM(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqBJ)) {
				resp.setWqBJ(zhuangbeiCfg.getWqBJ() + dbUe.wqBJ);
			}else{
				resp.setWqBJ(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqRX)) {
				resp.setWqRX(zhuangbeiCfg.getWqRX() + dbUe.wqRX);
			}else{
				resp.setWqRX(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnSH)) {
				resp.setJnSH(zhuangbeiCfg.getJnSH() + dbUe.jnSH);
			}else{
				resp.setJnSH(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnJM)) {
				resp.setJnJM(zhuangbeiCfg.getJnJM() + dbUe.jnJM);
			}else{
				resp.setJnJM(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnBJ)) {
				resp.setJnBJ(zhuangbeiCfg.getJnBJ() + dbUe.jnBJ);
			}else{
				resp.setJnBJ(0);
			}
			if (UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnRX)) {
				resp.setJnRX(zhuangbeiCfg.getJnRX() + dbUe.jnRX);
			}else{
				resp.setJnRX(0);
			}
			//TODO 以下 1.1可能不加入
			//	武器暴击率        
//			if (dbUe.getWqBJL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqBJL)) {
//				resp.setWqBJL(dbUe.getWqBJL());
//			}else{
//				resp.setWqBJL(0);
//			}
//			//技能暴击率           
//			if (dbUe.getJnBJL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnBJL)) {
//				resp.setJnBJL(dbUe.getJnBJL());
//			}else{
//				resp.setJnBJL(0);
//			}
//			//武器免暴率 	
//			if (dbUe.getWqMBL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.wqMBL)) {
//				resp.setWqMBL(dbUe.getWqMBL());
//			}else{
//				resp.setWqMBL(0);
//			}
//			//	技能免暴率      
//			if (dbUe.getJnMBL()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.jnMBL)) {
//				resp.setJnMBL(dbUe.getJnMBL());
//			}else{
//				resp.setJnMBL(0);
//			}
//			//	技能冷却缩减     
//			if (dbUe.getSxJiaCheng()>0){//UserEquipAction.instance.hasEquipTalent(dbUe,zhuangbeiCfg.getId(),UEConstant.sxJiaCheng)) {
//				resp.setSxJiaCheng(dbUe.getSxJiaCheng());
//			}else{
//				resp.setSxJiaCheng(0);
//			}
			resp.setZhuangbeiID(zhuangbeiCfg.getId());
			resp.setJinJieExp(dbUe.JinJieExp);
			allResp.addAllResp(resp);
		}
		log.info("向君主{}发送一键强化结果",junZhuId);
		session.write(allResp.build());
	}
	
	/**
	 * @Description 根据isWuQi 区分 来 一键强化武器或者装备
	 */
	public int doUpAll(JunZhu junZhu, Bag<EquipGrid> equips,
			Bag<BagGrid> bag, boolean isWuQi) {
		long junZhuId = junZhu.id;
		int safeTick = 100;
		int res = 0;
		do{
			//获得背包中的材料可产生的经验 2015年9月19日 材料是否够用进入每件装备时判断
//			List<BagGrid> materials=getMaterialsFromBag(bag,junZhuId,isWuQi);
//			int expCanUse=getExpfromUsedEquipIds(materials, junZhuId);
//			log.info("君主{}获得背包中的材料可产生的经验为{}",junZhuId,expCanUse);
//			if(expCanUse<=0){
//				break;
//			}
			int lowIdx = findLow(equips, junZhu, isWuQi);
			if(lowIdx<0 || lowIdx == Integer.MAX_VALUE){
				log.error("君主{}一键强化出错：无法获取最差装备{}",junZhu.id,equips.grids);
				break;
			}
			EquipGrid eg=equips.grids.get(lowIdx);
			UserEquip dbUe = null;
			if(eg.instId > 0){
				dbUe = UserEquipDao.find(junZhuId, eg.instId);
				if(dbUe == null){
					dbUe = new UserEquip();
					dbUe.userId = junZhuId;
					dbUe.templateId = eg.itemId;
					log.error("找不到装备equipId:{}的强化数据instId:{}",  eg.dbId,  eg.instId);
				}
			}else{
				dbUe = new UserEquip();
				dbUe.userId = junZhuId;
				dbUe.templateId = eg.itemId;
			}
			int needExp = calcNeedExp(equips,dbUe,lowIdx,junZhu.level);
			if(needExp<=0){
				break;
			}
			Map<Integer, Integer> buweiMap=getBuWeiMap(equips);
			Object [] param =new Object[] {dbUe,lowIdx,isWuQi,needExp,buweiMap};
			int curLevel=dbUe.level;
			int curExp=dbUe.exp;
			UserEquip dbUeResult=doUpOneEquip(junZhu,equips,bag,param);
			if(dbUeResult==null){
				log.error("君主{}一键强化出错，Exception", junZhuId);
				break;
			}
			if(curLevel==dbUeResult.level&&curExp==dbUeResult.exp){
				log.info("君主{}一键强化结束，材料耗光，循环次数--{}", junZhuId,100-safeTick);
				break;
			}else{
				res++;
			}
			safeTick --;//防止死循环
		}while(safeTick>0);
		if(safeTick==0){
			log.error("死循环风险{}", junZhuId);
		}
		log.info("君主-{}一键强化---{}结束，循环次数剩余--{},循环次数为---{}", junZhuId,isWuQi?"武器":"防具",safeTick,100-safeTick);
		return res;
	}
	
	
	/**
	 * @Description 得到身上装备部位 --品质对应map	
	 */
	public Map<Integer, Integer> getBuWeiMap(Bag<EquipGrid> equips) {
		Map<Integer, Integer> buweiMap=new HashMap<Integer, Integer>();
		List<EquipGrid> list = equips.grids;
		for (EquipGrid eg : list) {
			if(eg==null) continue;
			ZhuangBei zb= template.getZhuangBei(eg.itemId);
			if(zb==null) continue;
			buweiMap.put(zb.getBuWei(), zb.getPinZhi());
		}
		return buweiMap;
	}

	/**
	 *@Description  计算这个装备升级需要的经验
	 */
	public int calcNeedExp(Bag<EquipGrid> equips, UserEquip dbUe, int lowIdx, int junzhulevel) {
		int ret=Integer.MAX_VALUE;
		int currLv=dbUe.level;
		EquipGrid eg=equips.grids.get(lowIdx);
		int curExp = dbUe.exp;
		log.info("装备原有经验为 {}", curExp);
		ZhuangBei zhuangbeiCfg = template.getZhuangBei(eg.itemId);
		if(zhuangbeiCfg == null){
			return ret;
		}
		int expId = zhuangbeiCfg.getExpId();
		log.info("使用经验ID {}", expId);
		List<ExpTemp> expTemps = template.getExpTemps(expId);
		if(expTemps == null){
			log.error("没有经验配置。");
			return ret;
		}
		if(expTemps.size()<=1){
			log.error("不可强化。");
			return ret;
		}
		if(hasBadExp(expTemps)){
			log.error("强化经验配置错误，装备id{}",eg.itemId);
			return ret;
		}
		int needExp = 0;//所需的最大经验值
		for (int i = currLv; i < currLv+1; i++) {
			ExpTemp	expTemp = expTemps.get(i);
			needExp += expTemp.needExp;
		}
		needExp -= curExp;
		log.info("所需的最大经验值==={}",needExp);
		return needExp;
	}
	
	
	
	/**
	 * @Description 强化一件装备
	 */
	public UserEquip doUpOneEquip(JunZhu junZhu, Bag<EquipGrid> equips,
			Bag<BagGrid> bag, Object[] param) {
		int lowIdx=(Integer) param[1];
		long junZhuId=junZhu.id;
		EquipGrid target=equips.grids.get(lowIdx);//要强化的装备
		if (target == null) {
			log.error("君主{}强化装备不存在 ,强化lowIdx=={}失败",junZhuId,lowIdx);
			return null;
		}
		log.info("君主{}开始强化装备eg.dbId===={}",junZhuId,target.dbId);
//		long equipId =  eg.instId;//dbUe.getEquipId();  dbUe主键
		int targetItemId = target.itemId;// dbUe.getTemplateId();
		ZhuangBei zhuangbeiCfg = template.getZhuangBei(targetItemId);
		if(zhuangbeiCfg == null){
			log.error("装备配置找不到 zhuangBeiId:{} 强化eg.dbId=={}失败", targetItemId,target.dbId);
			return null;
		}
		UserEquip dbUe=(UserEquip) param[0];
		boolean isWuQi=(Boolean) param[2];
		int needExp=(Integer) param[3];
		int junzhulevel=junZhu.level;
		long targetInstId = target.instId; //等于UserEquip的主键equipId
		int curExp = dbUe.exp;
		int currLv = dbUe.level;
		//2015年9月18日 低级装备只能吞噬同级以及以下的装备
		//进阶材料/装备的品质  小于  对应部位 穿戴的装备品质，可以吃
		int curPinzhi=zhuangbeiCfg.pinZhi;
		log.info("装备原有经验为 {}", curExp);
		int expId = zhuangbeiCfg.getExpId();
		log.info("使用经验ID {}", expId);
		List<ExpTemp> expTemps = template.getExpTemps(expId);
		if(expTemps == null){
			log.error("没有经验配置。");
			return null;
		}
		if(expTemps.size()<=1){
			log.error("不可强化。");
			return null;
		}
		if(hasBadExp(expTemps)){
			log.error("强化经验配置错误，装备id{}",targetItemId);
			return null;
		}
		log.info("当前君主等级为{}，装备=={},品质=={},当前强化等级为{}，升级装备到下一级需要经验为{}",junzhulevel,targetItemId,curPinzhi, currLv, needExp);
		HashMap<Integer, Integer> buweiMap=(HashMap<Integer, Integer>) param[4];
		List<BagGrid> materials=getMaterialsFromBagEquips(bag, junZhuId, isWuQi,curPinzhi,buweiMap);
		List<BagGrid> bagList4Use =materials;// bag.grids;	
		int listSize=bagList4Use.size();
		for (int i = listSize-1; i >=0&&needExp>0; i--) {
			BagGrid bagGrid = bagList4Use.get(i);
			if(bagGrid==null){
//				log.info("君主={}背包格子{}是空的",junZhuId,i);
				continue;
			}
			if(!isCanUse(bagGrid, junZhuId, isWuQi)){
//				log.info("君主={}背包格子=={}的物品不能用于当前强化",junZhuId,i);
				continue;
			}
			else{
				log.info("使用君主={}背包格子=={}的材料物品于当前强化",junZhuId,i);
			}
			// 使用的武器强化，可获得的经验数
			ZhuangBei t = template.getZhuangBei(bagGrid.itemId);
			if(t == null){
				log.info("bagGrid.itemId--{},是个材料,使用材料",bagGrid.itemId);
				//使用强化材料强化
				BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
				if(bi == null || !(bi instanceof ItemTemp)){
					log.error("使用材料模板没有找到:"+bagGrid.itemId);
					continue;
				}
				ItemTemp it = (ItemTemp) bi;//武器吃1 装备吃2
				//进阶材料的(品质)  小于  对应（部位it.id%100） 穿戴的装备品质，可以吃 (itemType==6)表示进阶材料 
				if(it.id==960101||it.id==960214){
					System.out.println(it.quality<curPinzhi);
				}
				if (it.itemType==6&&it.quality>=curPinzhi) { 
					log.error("进阶材料的(品质)--{}  >=  对应（部位it.id%100） 穿戴的装备品质--{}，不可用于强化 ,之前判断有漏洞",it.quality,curPinzhi);
					continue;
				}
				int cnt = bagGrid.cnt;
				int canUseCount=0;
				for (int j = 0; j < cnt; j++) {
					// TODO 这里是为了控制消耗的材料总经验超过所需最大经验，减少扣除的材料数量。
					// 暂时这个版本先不考虑一并扣除，等策划确定需要这需求再做处理
					if(needExp>0){
						canUseCount++;
						needExp-=it.effectId;
						curExp+=it.effectId;
					}else{
						break;
					}
				}
				log.info("物品id=={}可消耗物品个数为{}",bagGrid.itemId,canUseCount);
				bagGrid.cnt -= canUseCount;
				log.info("强化材料使用成功,[{}]使用强化材料{} x {} 获得经验{},剩余个数{},还需要的经验为{}",
						junZhuId,it.getId(),canUseCount,canUseCount*it.effectId,bagGrid.cnt,needExp);
				HibernateUtil.save(bagGrid);
			}else{
				// 使用的武器强化，可获得的经验数
				log.info("bagGrid.instId ---{}使用的武器/装备强化,获得模板经验{}",bagGrid.instId,t.getExp());
				if(needExp>0){
					curExp += t.getExp();
					needExp-= t.getExp();
				}else{
					log.info("bagGrid.instId ---{}使用的武器/装备强化结束，needExp=={}",bagGrid.instId,needExp);
					break;
				}
				if(bagGrid.instId > 0) {
					// FIXME 武器是否强化过，计算数值不对，应把之前等级所耗掉的经验都加上 把消耗的武器身上的强化经验加到现有武器上
					UserEquip mUe = UserEquipDao.find(junZhu.id, bagGrid.instId);
					if(mUe != null){
						curExp += mUe.exp;
						//洗练继承操作 （还要考虑有损继承）
						log.info("材料之前强化经验{}，洗练统力{}，谋力{}，武力{}",mUe.exp,mUe.tongli, mUe.mouli, mUe.wuli);
						HibernateUtil.delete(mUe);
					}else{
						log.warn("之前强化信息未找到{}",bagGrid.instId);
					}
				}
				bagGrid.instId = bagGrid.itemId = bagGrid.cnt = 0;
				HibernateUtil.save(bagGrid);
			}
			log.info("强化材料使用成功-装备，删除背包物品 {} from junZhuId =={}",bagGrid.itemId, junZhuId);
		}


		int newLv = dbUe.level;
		while(curExp > 0){
			int upNeed = expTemps.get(newLv).needExp;
			if(upNeed<=0){
				// FIXME 假如经验，满级后能否进行强化
				upNeed = 10 * newLv+1;
				log.info("使用假的升级经验{}",upNeed);
			}
			//1.0装备强化等级可以大于君主等级
//			if(newLv >= junzhulevel) {
////				curExp = 0;
//				break;
//			}
			if(curExp < upNeed){
				break;
			}
			log.info("消耗 {} 升级至 {}",upNeed,newLv);
			newLv++;
			curExp -= upNeed;
			if(newLv>90){
				log.info("君主--{}装备UserEquip--equipId---{}等级--{}  超过90级 进行一键强化中",junZhuId,dbUe.equipId,newLv);	
			}
			if(newLv>(expTemps.size()-1)){//2015年9月17日 1.0expTempsl里面的参数从0开始计算  ExpTemp只有100级
				newLv=expTemps.size()-1;
				log.info("君主--{}装备UserEquip--equipId---{}已达到最大等级",junZhuId,dbUe.equipId);
				break;
			}
		}

		dbUe.exp = curExp;
		dbUe.level = newLv;
		if(dbUe.equipId == 0){
			UserEquipDao.insert(dbUe);
			MC.add(dbUe, dbUe.getIdentifier());
			targetInstId = dbUe.getIdentifier();
			target.instId = targetInstId;
			//方法开头检测过where合法性了。
			HibernateUtil.save(target);
			log.info("bind inst id {}", targetInstId);
		}else{
			HibernateUtil.save(dbUe);
		}
		log.info("装备强化成功,junzhuId:{},强化装备-dbUe.equipId:{},由LV:{}升级至 LV:{} exp {}",junZhu.id, dbUe.equipId,currLv, newLv, curExp);
		//TODO 强化日志
		return dbUe;
	}
	
	/**
	 * @Description 判断 物品是否能用于当前强化
	 */
	public boolean isCanUse(BagGrid bagGrid,long junZhuId,boolean isWuQi) {
		//以下需要判断区分武器和防具的进阶材料
		if(bagGrid == null || bagGrid.cnt<=0){
//			log.error("背包里找不到材料,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
			return false;
		}
		if(bagGrid.dbId/BagMgr.spaceFactor != junZhuId){
//			log.error("{}请求消耗别人的装备{}进行强化", junZhuId,bagGrid.dbId);
			return false;
		}
		if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao && isWuQi){
//			log.error("不能用防具材料强化武器,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
			return false ;
		}
		if(bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao && !isWuQi){
//			log.error("不能用武器材料强化防具,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
			return false ;
		}
		//上面需要判断区分武器和防具的进阶材料
		if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao 
				|| bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao
				|| bagGrid.type == BaseItem.TYPE_JinJie_CaiLiao){
			BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
			if(bi == null){
				return false;
			}
		}else{
			ZhuangBei t = template.getZhuangBei(bagGrid.itemId);
			if(t == null){
				return false;
			}
			if(isWuQi){
				if (!HeroMgr.inst.isWuQi(t.getBuWei())) {
//					log.info("不能用防具强化武器");
					return false ;
				}
			}else{
				if (HeroMgr.inst.isWuQi(t.getBuWei())) {
//					log.info("不能用武器强化防具");
					return false ;
				}
			}
		}
		return true;
	}
	
	/**
	 * @Description 找到身上强化等级最低的装备/武器
	 */
	public int findLow(Bag<EquipGrid> equips, JunZhu junZhu, boolean isWuQi) {
		if(equips == null){
			return -1;
		}
		List<EquipGrid> list = equips.grids;
		if(list == null){
			return -1;
		}
		int cnt = list.size();
		int ret = -1;
		int minBuWei = 16;
		int lowLevel = Integer.MAX_VALUE;
		int lowNeedExp= Integer.MAX_VALUE;
		log.error("强化目标是武器：{}",isWuQi);
		for(int i=0;i<cnt; i++){
			EquipGrid eg = list.get(i);
			if(eg == null){
				continue;
			}
			if(eg.itemId<=0){
				continue;
			}
			ZhuangBei t = template.getZhuangBei(eg.itemId);
			if(t == null){
				continue;
			}
			if(isWuQi){
				if (!HeroMgr.inst.isWuQi(t.getBuWei())) {
					continue ;
				}
			}else{
				if (HeroMgr.inst.isWuQi(t.getBuWei())) {
					continue ;
				}
			}
			if(eg.instId<0){//没强化，等级必然是最低的，直接判断经验
				lowLevel = 0;
				ExpTemp qhExp = TempletService.getInstance().getExpTemp(t.expId, 0);
				if(qhExp == null)continue;
				int needExp = qhExp.needExp;
				if(needExp < lowNeedExp){
					ret = i;
					lowNeedExp = needExp ;
					minBuWei = t.buWei;
				}else if(needExp == lowNeedExp){//需要经验相同，就判断部位
					if(t.buWei < minBuWei ){
						ret = i;
						minBuWei = t.buWei;
					}
				}
				continue ;
			}
			UserEquip ue = UserEquipDao.find(junZhu.id, eg.instId);
			if(ue == null){
				log.error("强化信息丢失 {}", eg.instId);
				continue;
			}
			if(ue.level>=junZhu.level){
				continue;
			}
			//判断等级最低
			ExpTemp qhExp = TempletService.getInstance().getExpTemp(t.expId, ue.level);
			if(qhExp == null)continue ;
			int needExp = qhExp.needExp - ue.exp;
			if(ue.level<lowLevel){
				ret = i;
				lowLevel = ue.level;
				lowNeedExp = needExp ;
				minBuWei = t.buWei;
			}else if(ue.level ==lowLevel){//强化等级相同，判断经验
				if( needExp < lowNeedExp){
					ret = i;
					lowNeedExp = needExp ;
					minBuWei = t.buWei;
				}else if(needExp == lowNeedExp){
					if(t.buWei < minBuWei ){
						ret = i;
						minBuWei = t.buWei;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @Description 获得背包中的可用材料
	 */
	public List<BagGrid> getMaterialsFromBagEquips(Bag<BagGrid> bag,long junZhuId,boolean isWuQi, 
			int curPinzhi,HashMap<Integer,Integer> buweiMap){
		List<BagGrid> list = bag.grids;	
		// 过滤所选强化材料的合法性
		List<BagGrid> materials = new ArrayList<BagGrid>();
		log.info("获取可用材料===是{},装备品质为{}", isWuQi?"武器":"装备",curPinzhi);
		for (BagGrid bagGrid : list) {
			//TODO 背包里的
			if(bagGrid == null || bagGrid.cnt<=0){
//				log.info("背包里找不到材料,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
				continue;
			}
			if(bagGrid.dbId/BagMgr.spaceFactor != junZhuId){
//				log.info("{}请求消耗别人的装备{}进行强化", junZhuId,bagGrid.dbId);
				continue;
			}
			if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao && isWuQi){
//				log.info("不能用防具材料强化武器,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
				continue ;
			}
			if(bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao && !isWuQi){
//				log.info("不能用武器材料强化防具,bagId:{},材料id:{}", bagGrid.dbId, bagGrid.itemId);
				continue ;
			}
			//2016-05-10需求变更，进阶材料和装备不再用于强化
			if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao 
					|| bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao){
				BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
				if(bi == null){
					continue;
				}
				//2016-05-10需求变更，进阶材料不再用于强化
//				ItemTemp it = (ItemTemp) bi;//武器吃1 装备吃2
//				//进阶材料的(品质)  小于  对应（部位it.id%100） 穿戴的装备品质，可以吃 (itemType==6)表示进阶材料 
//				if (it.itemType==BaseItem.TYPE_JinJie_CaiLiao) { //&&it.quality>curPinzhi
//					int buwei=-1;
//					buwei=TempletService.itemMap4Buwei.get(it.id);
//					if(buwei<=0){
//						log.error("进阶材料--{} 的部位获取错误",it.id);
//						continue;
//					}
//					//材料部位判断，1、2、3为武器，10以上为装备
//					if( buwei > 0 && buwei <= 3 && !isWuQi ){//材料对应部位是武器，强化目标不是武器
//						continue;
//					}
//					if(buwei >= 10 && isWuQi ){//材料对应部位是装备，强化目标不是武器
//						continue;
//					}
//					Integer duiyingPinZhi=buweiMap.get(buwei);
//					if(duiyingPinZhi==null){
////						log.error("进阶材料--{} 的获取对应部位品质错误",it.id);
//						duiyingPinZhi=0;
//					}
//					if(it.quality>=duiyingPinZhi){
////						log.info("进阶材料的(品质)--{}  >=  对应（部位） 穿戴的装备品质--{}，不可用于强化 ",it.quality,duiyingPinZhi);
//						continue;
//					}
//				}
			}else{
				continue;
//				ZhuangBei t = template.getZhuangBei(bagGrid.itemId);
//				if(t == null){
//					continue;
//				}
//				if(isWuQi){
//					if (!HeroMgr.inst.isWuQi(t.getBuWei())) {
////						log.info("不能用防具强化武器");
//						continue ;
//					}
//				}else{
//					if (HeroMgr.inst.isWuQi(t.getBuWei())) {
////						log.info("不能用武器强化防具");
//						continue ;
//					}
//				}
////				if (t.pinZhi>curPinzhi) { 
////					log.info("装备=={}品质--{}高于现在装备==》{}的品质=={}，用不了",t.id,t.pinZhi,curPinzhi);
////					continue;
////				}
//				int buwei=t.buWei;
//				Integer duiyingPinZhi=buweiMap.get(buwei);
//				if(duiyingPinZhi==null||t.pinZhi>duiyingPinZhi){
//					log.info("装备=={}品质--{}高于现在身上部位===>{}的装备的品质=={}，用不了",t.id,t.pinZhi,buwei,curPinzhi);
//					continue;
//				}
			}
			materials.add(bagGrid);
		}
		return materials;
	}
	
	/**
	 * @Description 模拟消耗所有材料变成强化经验
	 */
	public int	getExpfromUsedEquipIds(List<BagGrid> materials,long junZhuId){
		int produceExp=0;//消耗所有装备可得到的强化经验
		for (BagGrid bagGrid : materials) {
			// 使用的武器强化，可获得的经验数
			ZhuangBei t = template.getZhuangBei(bagGrid.itemId);
			if(t == null){
//				log.info("模板没有找到:"+bagGrid.itemId+"使用强化材料");
				if(bagGrid.type == BaseItem.TYPE_FangJu_CaiLiao 
						|| bagGrid.type == BaseItem.TYPE_WuQi_CaiLiao
						|| bagGrid.type == BaseItem.TYPE_JinJie_CaiLiao){
					BaseItem bi = TempletService.itemMap.get(bagGrid.itemId);
					if(bi == null || !(bi instanceof ItemTemp)){
						continue;
					}
					ItemTemp it = (ItemTemp) bi;
					int cnt =bagGrid.cnt;
					for (int i = 0; i < cnt; i++) {
						// TODO 这里是为了控制消耗的材料总经验超过所需最大经验，减少扣除的材料数量。
						// 暂时这个版本先不考虑一并扣除，等策划确定需要这需求再做处理
						produceExp += it.effectId;
					}
				}else{
					log.warn("强化材料没有找到:"+bagGrid.itemId+"不是强化材料");
				}	
			}else{
				// 使用的武器强化，可获得的经验数
//				log.info("获得模板经验{}",t.getExp());
				produceExp += t.getExp();
				if(bagGrid.instId > 0) {
					// FIXME 武器是否强化过，计算数值不对，应把之前等级所耗掉的经验都加上
					UserEquip mUe = UserEquipDao.find(junZhuId, bagGrid.instId);
					if(mUe != null){
						produceExp += mUe.exp;
						// TODO 洗练继承操作 （还要考虑有损继承）
//						log.info("材料之前强化经验{}，洗练统力{}，谋力{}，武力{}",mUe.getExp(),mUe.getTongli(), mUe.getMouli(), mUe.getWuli());
					}else{
						log.warn("之前强化信息未找到{}",bagGrid.instId);
					}
				}
			}
		}
		return produceExp;
	}
	
	
	public void addLogJsonCaiLiao(JSONArray logCaiLiao, int id, int canUseCount) {
		JSONObject o = new JSONObject();
		o.put("name", id);
		o.put("num", canUseCount);
		logCaiLiao.add(o);
	}

	public boolean hasBadExp(List<ExpTemp> expTemps) {
		if(expTemps == null){
			return true;
		}
		int size = expTemps.size() - 1;//最后一个不检查
		for(int i=0; i<size; i++){
			ExpTemp e = expTemps.get(i);
			if(e.needExp<=0){
				return true;
			}
		}
		return false;
	}

	public void sendError(IoSession session, String msg) {
		if(session == null){
			log.warn("session is null: {}",msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());		
	}



	/**
	 * @Description 取得装备的攻击/防御/生命/统力/武力/谋力效果信息
	 * 
	 * @param userEquip
	 * @return 一维数组,6个元素,0=攻击,1=防御,2=生命,3=统力,4=武力,5=谋力,
	 */
	public int[] getEquipEffect(UserEquip userEquip) {
		if (userEquip == null) {
			throw new BaseException("装备不存在");
		}
//		TempletService template = TempletService.getInstance();
		int[] effect = new int[6];

		int templateId = userEquip.templateId;
		ZhuangBei zhuangbeiTemp = template.getZhuangBei(templateId);
		// 基础效果
		effect[0] = zhuangbeiTemp.gongji;
		effect[1] = zhuangbeiTemp.fangyu;
		effect[2] = zhuangbeiTemp.getShengming();
		//加洗练效果
		effect[3] = zhuangbeiTemp.getTongli() + userEquip.tongli;
		effect[4] = zhuangbeiTemp.getWuli() + userEquip.wuli;
		effect[5] = zhuangbeiTemp.getMouli() + userEquip.mouli;
		// 强化效果
		int lv = userEquip.level;
		if (lv > 0) {
			int qianghuaId = zhuangbeiTemp.getQianghuaId();
			QiangHua qianghua = template.getQiangHua(qianghuaId, lv);
			effect[0] = effect[0] + qianghua.gongji;
			effect[1] = effect[1] + qianghua.fangyu;
			effect[2] = effect[2] + qianghua.shengming;
		}

		return effect;
	}

}
