package com.qx.equip.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import log.ActLog;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.UserEquipProtos.EquipJinJie;
import qxmobile.protobuf.UserEquipProtos.EquipJinJieResp;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthReq;
import qxmobile.protobuf.UserEquipProtos.XiLianReq;
import qxmobile.protobuf.UserEquipProtos.XiLianRes;
import qxmobile.protobuf.UserEquipProtos.XilianError;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.template.BuweiRank;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.QiRiLiBao;
import com.manu.dynasty.template.QiangHua;
import com.manu.dynasty.template.Xilian;
import com.manu.dynasty.template.XilianQujian;
import com.manu.dynasty.template.XilianShuxing;
import com.manu.dynasty.template.XilianzhiQujian;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.template.ZhuangbeiPinzhi;
import com.manu.dynasty.util.BaseException;
import com.manu.dynasty.util.MathUtils;
import com.manu.dynastyBackup.base.domain.User;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.AccountManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.achievement.AchievementCondition;
import com.qx.achievement.AchievementConstants;
import com.qx.alliance.HouseMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.equip.domain.EquipXiLian;
import com.qx.equip.domain.UserEquip;
import com.qx.equip.domain.UserEquipDao;
import com.qx.equip.domain.XilianFirstRecord;
import com.qx.equip.service.UserEquipService;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.purchase.XiLian;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.WorkTaskBean;
import com.qx.timeworker.FunctionID;
import com.qx.timeworker.TimeWorkerMgr;
import com.qx.util.RandomUtil;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * @Description 洗练
 *
 */
public class UserEquipAction  extends EventProc {
	public static Logger log = LoggerFactory.getLogger(UserEquipAction.class.getSimpleName());
	public static UserEquipAction instance;
	/** 洗练增加值表  洗练类型typeID:元宝|免费, 洗练列表  **/
	public Map<Integer, List<Xilian>> xilianMap;
	/** type:list **/
	public Map<Integer, List<XilianQujian>> xilianQujianMap;
	public Map<Integer,XilianShuxing> xilianShuxingMap;
	public Map<Integer,BuweiRank> buWeiMap = new HashMap<Integer,BuweiRank>();
//	public Map<Integer,Xilianzhi> xilianzhiMap;需求变更 废弃
	public List<XilianzhiQujian> xlziqujianList;
	
	/** 装备、或装备属性锁定标识 **/
	public final int LOCK_FALG = 1114;//2015年9月8日 1.0不用洗练锁 此数值无具体意义 原来LOCK_FALG=1
	public UserEquipService userEquipService = UserEquipService.getInstance();
	public static int xilianshi=910002;//洗练石
	
	public static int xiLianFunctionId = 1210 ;
	public static int jinJieFunctionId = 1211 ;
	public static int qiangHuaFunctionId = 1212;
	
	public static boolean useEquipXiLianCache = true ;
	
	
	public UserEquipAction() {
		instance = this;
		initData();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		List<BuweiRank> buWeiList = TempletService.listAll(BuweiRank.class.getSimpleName());
		buWeiList.stream().forEach( b -> buWeiMap.put (b.id,b) );
		List<Xilian> xilianList = TempletService.listAll(Xilian.class.getSimpleName());
		Map<Integer, List<Xilian>> xilianMap = new HashMap<Integer, List<Xilian>>();
		
		for (Xilian xilian : xilianList) {
			List<Xilian> list = xilianMap.get(xilian.typeID);
			if (list == null) {
				list = new ArrayList<Xilian>();
				xilianMap.put(xilian.typeID, list);
			}
			list.add(xilian);
		}
		this.xilianMap = xilianMap;

		List<XilianQujian> qujianList = TempletService.listAll(XilianQujian.class.getSimpleName());
		Map<Integer, List<XilianQujian>> qujianMap = new HashMap<Integer, List<XilianQujian>>();
		for (XilianQujian qujian : qujianList) {
			List<XilianQujian> list = qujianMap.get(qujian.type);
			if (list == null) {
				list = new ArrayList<XilianQujian>();
				qujianMap.put(qujian.type, list);
			}
			list.add(qujian);
		}
		this.xilianQujianMap = qujianMap;
		//加载洗练属性配置
		List<XilianShuxing> shuxingList = TempletService.listAll(XilianShuxing.class.getSimpleName());
		Map<Integer, XilianShuxing> shuxingMap = new HashMap<Integer,XilianShuxing>();
		for (XilianShuxing shuxing : shuxingList) {
				shuxingMap.put(shuxing.ZhuangbeiID, shuxing);
		}
		this.xilianShuxingMap = shuxingMap;
		//加载洗练值配置 需求变更 废弃
//		List<Xilianzhi> xilianzhiList = TempletService.listAll(Xilianzhi.class.getSimpleName());
//		 Map<Integer,Xilianzhi> xilianzhiMap=new HashMap<Integer, Xilianzhi>();
//		for (Xilianzhi xlz : xilianzhiList) {
//			xilianzhiMap.put(xlz.Xilianzhi, xlz);
//		}
//		this.xilianzhiMap = xilianzhiMap;
		//加载洗练值区间配置
		List<XilianzhiQujian> xlziqujianList = TempletService.listAll(XilianzhiQujian.class.getSimpleName());
//		Map<Integer,XilianzhiQujian> xlziqujianMap=new HashMap<Integer, XilianzhiQujian>();
//		for (XilianzhiQujian xlzqj : xlziqujianList) {
//			xlziqujianMap.put(xlzqj.ID, xlzqj);
//		}
		this.xlziqujianList = xlziqujianList;
	}

	public static UserEquipAction getInstance() {
		return instance;
	}

	/**
	 * @Description 强化装备 public static final short C_EQUIP_UPGRADE = 24003; public static
	 * final short S_EQUIP_UPGRADE = 24004;
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void doUpgradeEquip(int cmd, IoSession session, Builder builder) {
		EquipStrengthReq.Builder req = (EquipStrengthReq.Builder) builder;
		userEquipService.doUpgradeEquip(session, req);
	}
	
	
	/**
	 * @Description 一键强化
	 */
	public void doUpAllgradeEquips(int cmd, IoSession session, Builder builder) {
		EquipStrengthReq.Builder req = (EquipStrengthReq.Builder) builder;
		userEquipService.doUpAllEquips(session, req);
	}

	public void exec(int id, IoSession session, Builder builder) {
		XiLianReq.Builder req = (qxmobile.protobuf.UserEquipProtos.XiLianReq.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("找不到君主，id:{}",
					session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		if(!FunctionOpenMgr.inst.isFunctionOpen(xiLianFunctionId, junZhu.id, junZhu.level)){
			log.error("君主{}的洗练功能尚未开启，君主等级{}", junZhu.id, junZhu.level);
			return;
		}
		
		log.info("君主-- {}进行洗练相关操作req.getAction()---{}",junZhu.id, req.getAction());
		switch (req.getAction()) {
		case 0: // 请求洗练装备信息
			sendXiLianInfo(id, session, req, junZhu);
			break;
		case 1: // 免费洗练
//			if (TimeWorkerMgr.instance.getXilianTimes(junZhu) <= 0) {
//				log.error("免费洗练次数已用完");
//				sendXilianErrorResp(session, 1);
//				return;
//			}
			xiLian(id, session, req, junZhu);
			break;
		case 2: // 元宝洗练
//			boolean isPermit = VipMgr.INSTANCE.isVipPermit(
//					VipData.yuanBao_xiLian, junZhu.vipLevel);
//			if (!isPermit) {
//				log.error("VIP等级不够，不能进行元宝洗练操作");
//				sendXilianErrorResp(session, 4);
//				return;
//			}
			xiLian(id, session, req, junZhu);
			break;
		case 3: // 确认洗练结果
			confirmXiLian(id, session, req, junZhu);
			break;
		case 4: // 取消洗练结果
			cancelXiLian(id, session, req, junZhu);
			break;
		default:
			log.error("未知动作 {}", req.getAction());
			break;
		}
	}

	public void sendXilianErrorResp(IoSession session, int result) {
		XilianError.Builder errorResp = XilianError.newBuilder();
		errorResp.setResult(1);
		session.write(errorResp.build());
	}

	/**
	 *  @Description 取消洗练
	 */
	public void cancelXiLian(int id, IoSession session,
			qxmobile.protobuf.UserEquipProtos.XiLianReq.Builder req,
			JunZhu junZhu) {
		long equipId = req.getEquipId();
		if(useEquipXiLianCache){
			EquipXiLian equipXiLian = EquipXiLianDao.inst.get(junZhu.id ,equipId);
			if (equipXiLian != null) {
				EquipXiLianDao.inst.delete(equipXiLian);
			} else {
				log.error("cmd:{},没有找到洗练数据：equipId:{},junzhuId:{}", id, equipId,junZhu.id);
			}
		}else{
			EquipXiLian equipXiLian = HibernateUtil.find(EquipXiLian.class, "where equipId = " + equipId +" and junZhuId = "+junZhu.id );
			if (equipXiLian != null) {
				HibernateUtil.delete(equipXiLian);
			} else {
				log.error("cmd:{},没有找到洗练数据：equipId:{},junzhuId:{}", id, equipId,junZhu.id);
			}
		}
		
		
		sendXiLianInfo(id, session, req, junZhu);
	}

	/**
	 * @Description 确认洗练
	 */
	public void confirmXiLian(int id, IoSession session,
			qxmobile.protobuf.UserEquipProtos.XiLianReq.Builder req,
			JunZhu junZhu) {
		long equipId = req.getEquipId();
		// 查找上次洗练结果
		EquipXiLian equipXiLian = null ;
		if(useEquipXiLianCache){
			equipXiLian = EquipXiLianDao.inst.get(junZhu.id , equipId );
		}else{
			equipXiLian = HibernateUtil.find(EquipXiLian.class, "where equipId = " + equipId +" and junZhuId = "+junZhu.id );
		}
		
		if (equipXiLian == null) {
			log.error("洗练结果确认失败，没有找到洗练数据，equipId:{}", equipId);
			return;
		}

//		int equipWhere = req.getType();
//		Object target = null;
		long targetInstId = -1;
		int targetItemId = -1;
//		if (equipWhere == 1) {
//			BagGrid source = findEquip(equipId, junZhu.id);
//			if (source == null) {
//				throw new BaseException("洗练结果确认失败，洗练装备不存在");
//			}
//			target = source;
//			targetInstId = source.instId;
//			targetItemId = source.itemId;
//		} else if (equipWhere == 2) {
		EquipGrid target = EquipMgr.inst.findEquip(junZhu.id, equipId);
		if (target == null) {
			log.error("洗练结果确认失败，没有找到装备{}", equipId);
		}
		targetItemId = target.itemId;
		targetInstId = target.instId;
//		}
//		if (target == null) {
//			throw new BaseException("洗练结果确认失败，洗练装备不存在");
//		}

		TempletService template = TempletService.getInstance();
		ZhuangBei zhuangBei = template.getZhuangBei(targetItemId);
		if (zhuangBei == null) {
			log.error("洗练结果确认失败，没有找到对应的装备,zhuangBeiId:", targetItemId);
			sendError(id, session, "洗练结果确认失败，没有找到对应的装备,zhuangBeiId:" + targetItemId);
			return;
		}
		UserEquip dbUe = null;
		if (targetInstId > 0) {
			dbUe = UserEquipDao.find(junZhu.id, targetInstId);
			if (dbUe == null) {
				log.error("洗练结果确认失败，没有找到已经洗练的装备数据，equipId:{}", targetInstId);
				sendError(id, session, "洗练结果确认失败，没有找到已经洗练的装备数据，equipId:{}"
						+ targetInstId);
				return;
			}
		} else {
			dbUe = new UserEquip();
			dbUe.userId = junZhu.id;
			dbUe.templateId = zhuangBei.getId();
		}
		//以下属性废弃
//		int curTongli = dbUe.tongli;
//		int curWuyi = dbUe.wuli;
//		int curZhimou = dbUe.mouli;
//		int tongliMax = getXiLianMaxValue(zhuangBei.getTongli(), zhuangBei,
//				dbUe.level);
//		int wuyiMax = getXiLianMaxValue(zhuangBei.getWuli(), zhuangBei,
//				dbUe.level);
//		int zhimouMax = getXiLianMaxValue(zhuangBei.getMouli(), zhuangBei,
//				dbUe.level);
//		curTongli += equipXiLian.tongShuiAdd;
//		curTongli = getXilianFinalValue(zhuangBei.getTongli(), curTongli,
//				tongliMax);
//		curWuyi += equipXiLian.wuYiAdd;
//		curWuyi = getXilianFinalValue(zhuangBei.getWuli(), curWuyi, wuyiMax);
//		curZhimou += equipXiLian.zhiMouAdd;
//		curZhimou = getXilianFinalValue(zhuangBei.getMouli(), curZhimou,
//				zhimouMax);
//		dbUe.tongli = curTongli;
//		dbUe.wuli = curWuyi;
//		dbUe.mouli = curZhimou;
		//以上属性废弃
		
		// TODO 以下1.0版本改变洗练逻辑 改完待确认   原判断逻辑  	if (zhuangBei.getWqSH() > 0) {
		int maxSxValue = getXiLianMaxValue(zhuangBei.getWqSH(), zhuangBei,
				dbUe.level);
		if (equipXiLian.wqSHAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqSH)) {
			int curWqSH = dbUe.wqSH; // （武器伤害加深）属性洗练值
			curWqSH += equipXiLian.wqSHAdd;
			curWqSH = getXilianFinalValue(zhuangBei.getWqSH(), curWqSH, maxSxValue);
			dbUe.wqSH = curWqSH;
		}
		if (equipXiLian.wqJMAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqJM)) {
			int curWqJM = dbUe.wqJM; // （武器伤害减免）属性洗练值
			curWqJM += equipXiLian.wqJMAdd;
			curWqJM = getXilianFinalValue(zhuangBei.getWqJM(), curWqJM, maxSxValue);
			dbUe.wqJM = curWqJM;
		}
		if (equipXiLian.wqBJAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqBJ)) {
			int curWqBJ = dbUe.wqBJ; // （武器暴击加深）属性洗练值
			curWqBJ += equipXiLian.wqBJAdd;
			curWqBJ = getXilianFinalValue(zhuangBei.getWqBJ(), curWqBJ, maxSxValue);
			dbUe.wqBJ = curWqBJ;
		}
		if (equipXiLian.wqRXAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqRX)) {
			int curWqRX = dbUe.wqRX; // （武器暴击加深）属性洗练值
			curWqRX += equipXiLian.wqRXAdd;
			curWqRX = getXilianFinalValue(zhuangBei.getWqRX(), curWqRX, maxSxValue);
			dbUe.wqRX = curWqRX;
		}
		if ( equipXiLian.jnSHAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnSH)) {
			int curJnSH = dbUe.jnSH; // （技能伤害加深）属性洗练值
			curJnSH += equipXiLian.jnSHAdd;
			curJnSH = getXilianFinalValue(zhuangBei.getJnSH(), curJnSH, maxSxValue);
			dbUe.jnSH = curJnSH;
		}
		if ( equipXiLian.jnJMAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnJM)) {
			int curJnJM = dbUe.jnJM; // （技能伤害减免）属性洗练值
			curJnJM += equipXiLian.jnJMAdd;
			curJnJM = getXilianFinalValue(zhuangBei.getJnJM(), curJnJM, maxSxValue);
			dbUe.jnJM = curJnJM;
		}
		if ( equipXiLian.jnBJAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnBJ)) {
			int curJnBJ = dbUe.jnBJ; // （技能暴击加深）属性洗练值
			curJnBJ += equipXiLian.jnBJAdd;
			curJnBJ = getXilianFinalValue(zhuangBei.getJnBJ(), curJnBJ, maxSxValue);
			dbUe.jnBJ = curJnBJ;
		}
		if (equipXiLian.jnRXAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnRX)) {
			int curJnRX = dbUe.jnRX; // （技能暴击减免）属性洗练值
			curJnRX += equipXiLian.jnRXAdd;
			curJnRX = getXilianFinalValue(zhuangBei.getJnRX(), curJnRX, maxSxValue);
			dbUe.jnRX = curJnRX;
		}
		//以下属性洗出后不变
		//TODO 以下5个属性不需要确认 系统自动确认
//		//	武器暴击率       
//		if ( equipXiLian.getWqBJLAdd()>0) {//&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqBJL)) {
//			dbUe.setWqBJL(equipXiLian.getWqBJLAdd());
//		}
//		//技能暴击率	   
//		if(equipXiLian.getJnBJLAdd()>0) {//&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnBJL)) {
//			dbUe.setJnBJL(equipXiLian.getJnBJLAdd());
//		}
//		//武器免暴率	
//		if (equipXiLian.getWqMBLAdd()>0) {//&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqMBL)) {
//			dbUe.setWqMBL(equipXiLian.getWqMBLAdd());
//		}
//		//	技能免暴率    
//		if (equipXiLian.getJnMBLAdd()>0) {//&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnMBL)) {
//			dbUe.setJnMBL(equipXiLian.getJnMBLAdd());
//		}
//		//属性加成     => 1.1改成属性加成
//		if (equipXiLian.getSxJiaChengAdd()>0) {//&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.sxJiaCheng)) {
//			dbUe.setSxJiaCheng(equipXiLian.getSxJiaChengAdd());
//		}  
		//以上1.0版本改变洗练逻辑
		
		if (dbUe.equipId == 0) {
			UserEquipDao.insert(dbUe);
			MC.add(dbUe, dbUe.getIdentifier());
		} else {
			HibernateUtil.save(dbUe);
		}
		if( useEquipXiLianCache){
			EquipXiLianDao.inst.delete(equipXiLian);
		}else{
			HibernateUtil.delete(equipXiLian);
		}
		
		XiLianRes.Builder resp = sendXiLianInfo(junZhu,  zhuangBei, dbUe, equipId, 0, 0, null);
		resp.setCloseRedPoint(isXiMan4All(junZhu.id));
		resp.setCloseRedPoint(true);
		session.write(resp.build());
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
	}

	public void xiLian(int id, IoSession session,
			qxmobile.protobuf.UserEquipProtos.XiLianReq.Builder req,
			JunZhu junZhu) {
		long equipId = req.getEquipId();
		EquipXiLian equipXiLian = null ;
		if( useEquipXiLianCache){
			equipXiLian = EquipXiLianDao.inst.get(junZhu.id , equipId);
		}else{
			equipXiLian = HibernateUtil.find(EquipXiLian.class, "where equipId = " + equipId +" and junZhuId = "+junZhu.id );
		}
		
		if (equipXiLian != null) {
			log.error("上次已经洗练过，进行确认操作");
			sendXilianErrorResp(session, 5);
			return;
		}
		
		EquipGrid target = EquipMgr.inst.findEquip(junZhu.id, equipId);
		
		if (target == null) {
			throw new BaseException("洗练装备不存在");
		}
		TempletService template = TempletService.getInstance();
		ZhuangBei zhuangBeiTmp = template.getZhuangBei(target.itemId);
		if (zhuangBeiTmp == null) {
			log.error("没有找到对应的装备,zhuangBeiId:", target.itemId);
			sendError(id, session, "没有找到对应的装备,zhuangBeiId:" + target.itemId );
			return;
		}
		if(zhuangBeiTmp.pinZhi <= 1) {
			log.warn("白色装备不能进行洗练操作zhuangbeiId:{}", target.itemId );
			return;
		}
		UserEquip dbUe = null;
		if (target.instId > 0) {
			dbUe = UserEquipDao.find(junZhu.id, target.instId);
			if (dbUe == null) {
				log.error("没有找到已经洗练的装备数据，equipId:{}", target.instId);
				sendError(id, session, "没有找到已经洗练的装备数据，equipId:{}"
						+ target.instId);
				return;
			}
		} else {
			dbUe = new UserEquip();
			dbUe.userId = junZhu.id;
			dbUe.templateId = zhuangBeiTmp.getId();
			UserEquipDao.insert(dbUe);
		}
		log.info("属性锁定状态码 :tongli {}, mouli {}, wuli {}", req.getLockTongli(),
				req.getLockMouli(), req.getLockWuli());
		int action = req.getAction();
		
		// 判断各属性是否锁定，锁定则不改变值，（还要考虑到以后锁定收费的情况如何处理）,
		// 根据锁定情况来判断可以进行的操作
		equipXiLian = new EquipXiLian();
		
		equipXiLian.junZhuId = junZhu.id;
		equipXiLian.equipId = req.getEquipId();
		
		// 需要判断是否有该装备属性，再进行判断是否锁定
		//刷新属性条数
		log.info("君主{}的装备{}原本开启的洗练属性为---{}",junZhu.id, dbUe.equipId,dbUe.hasXilian);
		Object[]	returnObj=refreshEquipByXiLianValue(dbUe,target.itemId);
		dbUe=(UserEquip) returnObj[0];
//		HibernateUtil.save(dbUe);
		boolean isNewTalent=(Boolean) returnObj[1];//是否洗出新属性标记
		boolean isXiLian = false ;
		if(isNewTalent){
			log.info("君主{}的装备{}，开启新属性，现有开启的洗练属性为---{}",junZhu.id, dbUe.equipId,dbUe.hasXilian);
		}else{
			//没洗出新属性
			int maxSxValue=getXiLianMaxValue(0, zhuangBeiTmp, dbUe.level);
			int totalvalue = 0 ;
			if ( dbUe.hasXilian.contains(UEConstant.wqSH) ) {
				totalvalue = zhuangBeiTmp.getWqSH()+dbUe.wqSH ;
				if(totalvalue<maxSxValue){
					equipXiLian.wqSHAdd = getAddValue(action, totalvalue);
					isXiLian = true ;
				}else{
					equipXiLian.wqSHAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.wqJM)) {
				totalvalue = zhuangBeiTmp.getWqJM()+dbUe.wqJM ;
				if(totalvalue < maxSxValue){
					equipXiLian.wqJMAdd = getAddValue(action, totalvalue);
					isXiLian = true ;
				}else{
					equipXiLian.wqJMAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.wqBJ)) {
				totalvalue = zhuangBeiTmp.getWqBJ()+dbUe.wqBJ ;
				if( totalvalue<maxSxValue){
					equipXiLian.wqBJAdd = getAddValue(action, totalvalue );
					isXiLian = true ;
				}else{
					equipXiLian.wqBJAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.wqRX)) {
				totalvalue = zhuangBeiTmp.getWqRX()+dbUe.wqRX ;
				if(totalvalue < maxSxValue){
					equipXiLian.wqRXAdd =(getAddValue(action, totalvalue ));
					isXiLian = true ;
				}else{
					equipXiLian.wqRXAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.jnSH)) {
				totalvalue = zhuangBeiTmp.getJnSH()+dbUe.jnSH;
				if(totalvalue < maxSxValue){
					equipXiLian.jnSHAdd = getAddValue(action, totalvalue);
					isXiLian = true ;
				}else{
					equipXiLian.jnSHAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.jnJM)) {
				totalvalue = zhuangBeiTmp.getJnJM()+dbUe.jnJM ;
				if(totalvalue<maxSxValue){
					equipXiLian.jnJMAdd = getAddValue(action, totalvalue );
					isXiLian = true ;
				}else{
					equipXiLian.jnJMAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.jnBJ)) {
				totalvalue = zhuangBeiTmp.getJnBJ()+dbUe.jnBJ ;
				if(totalvalue < maxSxValue){
					equipXiLian.jnBJAdd =getAddValue(action, totalvalue);
					isXiLian = true ;
				}else{
					equipXiLian.jnBJAdd = 0;
				}
			}
			if (dbUe.hasXilian.contains(UEConstant.jnRX)) {
				totalvalue = zhuangBeiTmp.getJnRX()+dbUe.jnRX;
				if(totalvalue < maxSxValue){
					equipXiLian.jnRXAdd = getAddValue(action, totalvalue);
					isXiLian = true ;
				}else{
					equipXiLian.jnRXAdd = 0;
				}
			}
//FIXME 洗出以下5个属性的时候会自动确认不会进行属性增减
//			//	武器暴击率       
//			if ( (dbUe.getWqBJL()==0)&&hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqBJL)) {
//				equipXiLian.setWqBJLAdd(zhuangBeiTmp.getWqBJL());
//			}
//			//技能暴击率	   
//			if( (dbUe.getJnBJL()==0)&&hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnBJL)) {
//				equipXiLian.setJnBJLAdd(zhuangBeiTmp.getJnBJL());
//			}
//			//武器免暴率	
//			if ((dbUe.getWqMBL()==0)&&hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqMBL)) {
//				equipXiLian.setWqMBLAdd(zhuangBeiTmp.getWqMBL());
//			}
//			//	技能免暴率    
//			if ((dbUe.getJnMBL()==0)&&hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnMBL)) {
//				equipXiLian.setJnMBLAdd(zhuangBeiTmp.getJnMBL());
//			}
//			//	属性加成  
//			if ((dbUe.getSxJiaCheng()==0)&&hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.sxJiaCheng)) {
//				equipXiLian.setSxJiaChengAdd(zhuangBeiTmp.getSxJiaCheng());
//			}  
			//1.0去掉洗练锁
//			//以上1.0版本改变洗练逻辑
//			if (req.getAction() == 1 && lockNum > 0) {
//				//			sendXilianErrorResp(session, 7);
//				log.error("有属性锁定时不能进行免费洗练，。。。。。。。。。。。。。。。。。逻辑错误");
//				//			return;
//			}
			if( isXiLian ){
				if(useEquipXiLianCache){
					EquipXiLianDao.inst.save(equipXiLian);
				}else{
					HibernateUtil.save(equipXiLian);
				}
			}else{
				log.info("君主{}洗练结果都是0",junZhu.id);
			}
		}
		
		
		

//		int times = TimeWorkerMgr.instance.getXilianTimes(junZhu);
		int times = 0 ;
//		int payYuanBao = 0;
		if (req.getAction() == 1) {
			// 如果是免费洗练。扣除一次
			times = TimeWorkerMgr.instance.subFreeXilianTimes(junZhu.id, 1);
		} else if (req.getAction() == 2) {
			log.info("君主:{},进行高级洗练", junZhu.id);
			// 高级洗练，扣除洗练石或者扣除元宝 ,元宝需要根据洗练次数扣除
			Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
			int cnt = BagMgr.inst.getItemCount(bag, xilianshi);
			XiLian xiLian = PurchaseMgr.inst.getXiLian(junZhu.id);
			//当有洗练石且本日洗练石使用次数小于最大使用次数
			if (cnt >0&&xiLian.xlsCount<CanShu.XILIANSHI_MAXTIMES) {
				BagMgr.inst.removeItem(session, bag, xilianshi, 1, "高级洗练", junZhu.level);
				//洗练石每日使用个数限制  CanShu.XILIANSHI_MAXTIMES
				xiLian.xlsCount = xiLian.xlsCount+1;
				log.info("君主:{},进行高级洗练---洗练石洗练次数:{},花费洗练石一个", junZhu.id,xiLian.xlsCount);
			}else{
				int needYuanBao = PurchaseMgr.inst.getNeedYuanBao(
						PurchaseConstants.XILIAN, xiLian.num + 1);
//				payYuanBao = needYuanBao;
				// 计算洗练
				YuanBaoMgr.inst.diff(junZhu, -needYuanBao, 0, needYuanBao,
						YBType.YB_XILIAN_ZHUANGBEI, "洗练装备");
				HibernateUtil.save(junZhu);
				xiLian.num = xiLian.num + 1;
				log.info("君主:{},进行高级洗练---元宝洗练次数:{}，洗练石洗练次数---{},花费元宝:{}", junZhu.id,
						xiLian.num,xiLian.xlsCount, needYuanBao);				
				}
		    xiLian.date = new Date();
			HibernateUtil.save(xiLian);
		}
		if(isNewTalent){
			log.info("君主{}的装备{}，开启新属性，自动进行确认",junZhu.id, dbUe.equipId,dbUe.hasXilian);
			saveXilian4NewShuxing(dbUe,zhuangBeiTmp,equipXiLian);
		}else{
			HibernateUtil.save(dbUe);
			if (target.instId <= 0) {
				target.instId = dbUe.equipId;
				HibernateUtil.save(target);
				log.info("targetInstId <= 0  bind inst id {}", target.instId);
			}
		}
		XiLianRes.Builder resp = sendXiLianInfo(junZhu, zhuangBeiTmp, dbUe, equipId, 0,0,equipXiLian);
		session.write(resp.build());
		// 发送战力数据
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		// 主线任务：洗练一次弓
		EventMgr.addEvent(junZhu.id,ED.XILIAN_ONE_GONG, new Object[] { junZhu.id });
		EventMgr.addEvent(junZhu.id,ED.ACHIEVEMENT_PROCESS, new AchievementCondition(
				junZhu.id, AchievementConstants.type_xilian_times, 1));
		// 每日任务
		EventMgr.addEvent(junZhu.id,ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				junZhu.id, DailyTaskConstants.xilian_3_id, 1));
		log.info("君主:{}进行洗练结束", junZhu.id);
	}
	
	
	/**
	 * @Description 洗出新属性保存洗练结果
	 */
	public void saveXilian4NewShuxing(UserEquip dbUe, ZhuangBei zhuangBei, EquipXiLian equipXiLian) {
		log.info("君主{}洗练装备(dbUe-EquipId)--{}洗出新属性,自动保存洗练结果开始",dbUe.userId,dbUe.equipId);
		int maxSxValue = getXiLianMaxValue( 0 , zhuangBei,
				dbUe.level);
		if (equipXiLian.wqSHAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqSH)) {
			int curWqSH = dbUe.wqSH; // （武器伤害加深）属性洗练值
			curWqSH += equipXiLian.wqSHAdd;
			curWqSH = getXilianFinalValue(zhuangBei.getWqSH(), curWqSH, maxSxValue);
			dbUe.wqSH = curWqSH;
		}
		if (equipXiLian.wqJMAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqJM)) {
			int curWqJM = dbUe.wqJM; // （武器伤害减免）属性洗练值
			curWqJM += equipXiLian.wqJMAdd;
			curWqJM = getXilianFinalValue(zhuangBei.getWqJM(), curWqJM, maxSxValue);
			dbUe.wqJM = curWqJM;
		}
		if (equipXiLian.wqBJAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqBJ)) {
			int curWqBJ = dbUe.wqBJ; // （武器暴击加深）属性洗练值
			curWqBJ += equipXiLian.wqBJAdd;
			curWqBJ = getXilianFinalValue(zhuangBei.getWqBJ(), curWqBJ, maxSxValue);
			dbUe.wqBJ = curWqBJ;
		}
		if (equipXiLian.wqRXAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqRX)) {
			int curWqRX = dbUe.wqRX; // （武器暴击加深）属性洗练值
			curWqRX += equipXiLian.wqRXAdd;
			curWqRX = getXilianFinalValue(zhuangBei.getWqRX(), curWqRX, maxSxValue);
			dbUe.wqRX = curWqRX;
		}
		if ( equipXiLian.jnSHAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnSH)) {
			int curJnSH = dbUe.jnSH; // （技能伤害加深）属性洗练值
			curJnSH += equipXiLian.jnSHAdd;
			curJnSH = getXilianFinalValue(zhuangBei.getJnSH(), curJnSH, maxSxValue);
			dbUe.jnSH = curJnSH;
		}
		if ( equipXiLian.jnJMAdd!=0){//hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnJM)) {
			int curJnJM = dbUe.jnJM; // （技能伤害减免）属性洗练值
			curJnJM += equipXiLian.jnJMAdd;
			curJnJM = getXilianFinalValue(zhuangBei.getJnJM(), curJnJM, maxSxValue);
			dbUe.jnJM = curJnJM;
		}
		if ( equipXiLian.jnBJAdd!=0){//if (hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnBJ)) {
			int curJnBJ = dbUe.jnBJ; // （技能暴击加深）属性洗练值
			curJnBJ += equipXiLian.jnBJAdd;
			curJnBJ = getXilianFinalValue(zhuangBei.getJnBJ(), curJnBJ, maxSxValue);
			dbUe.jnBJ = curJnBJ;
		}
		if (equipXiLian.jnRXAdd!=0){//if (hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnRX)) {
			int curJnRX = dbUe.jnRX;
			// （技能暴击减免）属性洗练值
			curJnRX += equipXiLian.jnRXAdd;
			curJnRX = getXilianFinalValue(zhuangBei.getJnRX(), curJnRX, maxSxValue);
			dbUe.jnRX = curJnRX;
		}
		//TODO 以下 1.1可能不加入
		//以下属性洗出后不变
		//	武器暴击率       
//		if ( (dbUe.getWqBJL()==0)&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqBJL)) {
//			dbUe.setWqBJL(zhuangBei.getWqBJL());
//		}
//		//技能暴击率	   
//		if( (dbUe.getJnBJL()==0)&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnBJL)) {
//			dbUe.setJnBJL(zhuangBei.getJnBJL());
//		}
//		//武器免暴率	
//		if ((dbUe.getWqMBL()==0)&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.wqMBL)) {
//			dbUe.setWqMBL(zhuangBei.getWqMBL());
//		}
//		//	技能免暴率    
//		if ((dbUe.getJnMBL()==0)&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.jnMBL)) {
//			dbUe.setJnMBL(zhuangBei.getJnMBL());
//		}
//		//	属性加成  
//		if ((dbUe.getSxJiaCheng()==0)&&hasEquipTalent(dbUe,zhuangBei.getId(),UEConstant.sxJiaCheng)) {
//			dbUe.setSxJiaCheng(zhuangBei.getSxJiaCheng());
//		}  
		//以上1.1版本改变洗练逻辑

		if (dbUe.equipId == 0) {
			UserEquipDao.insert(dbUe);
			MC.add(dbUe, dbUe.getIdentifier());
		} else {
			HibernateUtil.save(dbUe);
		}
		if( useEquipXiLianCache ){
			EquipXiLianDao.inst.delete(equipXiLian);
		}else{
			HibernateUtil.delete(equipXiLian);
		}
		log.info("君主{}洗练装备(dbUe-EquipId)--{}洗出新属性,自动保存洗练结果结束",dbUe.userId,dbUe.equipId);
	}
	
	public BagGrid findEquip(long equipId, long pid) {
		Bag<BagGrid> equips = BagMgr.inst.loadBag(pid);
		BagGrid source = null;
		Iterator<BagGrid> lits = equips.grids.iterator();
		while (lits.hasNext()) {
			BagGrid equip = lits.next();
			if (equipId == equip.dbId) {
				source = equip;
				// lits.remove();//不需要移除。移除操作很危险（装备丢失）
				break;
			}
		}
		return source;
	}

	/**
	 *@Description  获取本次洗练增加值
	 * 
	 * @param action
	 *            洗练类型：1-免费洗练、2-元宝洗练
	 *        curAdd  已经洗出的值
	 * @return
	 */
	public int getAddValue(int action, int curAdd) {
		int addValue = 0;
		int typeID = action;
		if (action != 3) {
			List<XilianQujian> qujianList = xilianQujianMap.get(action);
			if (qujianList == null) {
				log.error("XilianQujian配置文件找不到type。", action);
				return addValue;
			}
			for (XilianQujian qujian : qujianList) {
				if (curAdd >= qujian.min && curAdd < qujian.max) {
					typeID = qujian.typeID;
					break;
				}
			}
		}

		int gailv = RandomUtil.getRandomNum(10000);
		int gailvSum = 0;
		List<Xilian> xilianList = xilianMap.get(typeID);
		if (xilianList == null) {
			log.error("Xilian配置文件找不typeID:{}。", typeID);
			return addValue;
		}
		for (Xilian xl : xilianList) {
			gailvSum += xl.gailv;  
			if (gailv < gailvSum) {
				addValue = xl.value;
				log.info("hit at {} value {}", xl.gailv, xl.value);
				break;
			}
		}
		return addValue;
	}
	/**
	
	 * @Description 根据洗练值获取下一条属性是否出现(洗练值用于下一条属性出现概率的计算)
	 * ，出现并保存到数据库，返回装备已有洗练属性   即---刷新装备属性条数
	 * @param ue 装备 
	 * @param	  action 洗练类型：1-免费洗练、2-元宝洗练
	 * @return
	 */
	public Object[] refreshEquipByXiLianValue(UserEquip ue,int templateId) {
		Object[] ret=new Object[2];
		boolean isNew=false;
		ue.xianlianzhi = MathUtils.getMin(ue.xianlianzhi + 1, CanShu.XILIANZHI_MAX );
		HibernateUtil.save(ue);
		
		//根据洗练值获取此区间的属性最少条数    最大条数为xlzCount
		XilianzhiQujian xlzqj=getXilianzhiQuJianByXilianzhi(ue.xianlianzhi);
		
		//FIXME 以后属性条目增加则修改4
		int xlzCount= xlzqj == null ? 4 : xlzqj.ID;
		int hasXLlength=ue.hasXilian==null ? 0 : ue.hasXilian.length();
		
		//获取此装备的最多属性
		StringBuffer shuxing2Max=getMaxShuxing(templateId);
		if(shuxing2Max==null){
			log.error("刷新装备属性条数失败，未找到ZhuangbeiID--{}的配置 Exception",templateId);
			ret[0]=ue;
			ret[1]=isNew;
			return ret;
		}
		if(hasXLlength<xlzCount){
			if(hasXLlength>0)	{
				isNew=true;
			}
			log.info("君主{}的装备{}洗练值攒满某一区间，开启属性{}",ue.userId,ue.equipId,shuxing2Max.substring(hasXLlength, xlzCount));
			ue.hasXilian=shuxing2Max.substring(0, xlzCount);
		}
		log.info("{}洗练装备{},洗练值为{}，洗练后洗练属性为----{}",ue.userId,ue.templateId,ue.xianlianzhi,ue.hasXilian);
		ret[0]=ue;
		ret[1]=isNew;
		return ret;
	}
	
	
	/**
	 * @Description 获取洗练值区间配置
	 */
	public XilianzhiQujian getXilianzhiQuJianByXilianzhi(int xilianzhi) {
		for (XilianzhiQujian xlzqj : xlziqujianList) {
			if(xilianzhi >= xlzqj.Min && xilianzhi < xlzqj.Max){
				return xlzqj;
			}
		}
		return null;
	}
	
	
	/**
	 * @Description 根据洗练类型获取增加的洗练值的增加量的配置
	 * @param templateId 装备配置ID
	 */
	public StringBuffer getMaxShuxing(int templateId) {
		XilianShuxing shuxing=xilianShuxingMap.get(templateId);
		if(shuxing==null){
			log.error("根据洗练类型获取增加的洗练值的增加量的配置失败，未找到ZhuangbeiID--{}的配置",templateId);
			return null;
		}
		StringBuffer shuxing2Max=	new StringBuffer();
		shuxing2Max.append(shuxing.Shuxing1);
		shuxing2Max.append(shuxing.Shuxing2);
		shuxing2Max.append(shuxing.Shuxing3);
		shuxing2Max.append(shuxing.Shuxing4);
//		shuxing2Max.append(shuxing.Shuxing5);
//		shuxing2Max.append(shuxing.Shuxing6);
//		shuxing2Max.append(shuxing.Shuxing7);
		return shuxing2Max;
	}
	
	/**
	 * @Description  根据洗练类型获取增加的洗练值的增加量
	 * @param action 洗练类型：1-免费洗练、2-元宝洗练
	 * @return
	 */
	public int getaddXilianzhiBYAction(int action) {
		if(action==1){
			return CanShu.XILIANZHI_Free;
		}else if(action==2){
			return CanShu.XILIANZHI_YUANBAO;
		}
		return 0;
	}

	public void sendError(int code, IoSession session, String msg) {
		log.error("2016年3月30日 取消向客户端返回code---{}，msg---{}",code,msg);
//		if (session == null) {
//			log.warn("session is null: {}", msg);
//			return;
//		}
//		ErrorMessage.Builder test = ErrorMessage.newBuilder();
//		test.setErrorCode(1);
//		test.setErrorDesc(msg);
//		session.write(test.build());
	}

	/**
	 *  @Description 发送洗练装备信息
	 * 
	 */
	public void sendXiLianInfo(int id, IoSession session,
			qxmobile.protobuf.UserEquipProtos.XiLianReq.Builder req,
			JunZhu junZhu) {
		long equipId = req.getEquipId();
		int equipWhere = req.getType();

		Object target = null;
		long targetInstId = -1;
		int targetItemId = -1;
		if (equipWhere == 1) { // 背包内的装备
			BagGrid source = findEquip(equipId, junZhu.id);
			if (source == null) {
				throw new BaseException("洗练装备不存在");
			}
			target = source;
			targetInstId = source.instId;
			targetItemId = source.itemId;
		} else if (equipWhere == 2) { // 身上的装备
			EquipGrid source = EquipMgr.inst.findEquip(junZhu.id, equipId);
			if (source == null) {
				throw new BaseException("洗练装备不存在 2");
			}
			target = source;
			targetItemId = source.itemId;
			targetInstId = source.instId;
		}
		if (target == null) {
			throw new BaseException("洗练装备不存在");
		}

		TempletService template = TempletService.getInstance();
		ZhuangBei zb = template.getZhuangBei(targetItemId);
		if (zb == null) {
			log.error("不是装备equipId:{}", targetInstId);
			sendError(id, session, "不是装备:" + targetItemId);
			return;
		}
		UserEquip dbUe = null;
		if (targetInstId > 0) {
			dbUe = UserEquipDao.find(junZhu.id, targetInstId);
			if (dbUe == null) {
				log.error("已经强化的数据丢失了,equipId;{}", targetInstId);
				sendError(id, session, "已经强化的数据丢失了,equipId;{}" + targetInstId);
				return;
			}
		} else {
			dbUe = new UserEquip();
			dbUe.userId = (int) junZhu.id;
			dbUe.templateId = zb.getId();
			UserEquipDao.insert(dbUe);
			MC.add(dbUe, dbUe.getIdentifier());
		}
//		TimeWorkerMgr.instance.calcOfflineXilian(junZhu.id);
		// 查找上次洗练结果，若有则是需要玩家进行确认或取消操作
//		
		EquipXiLian equipXiLian = null ;
		if(useEquipXiLianCache){
			equipXiLian = EquipXiLianDao.inst.get(junZhu.id , equipId);
			if(equipXiLian != null && equipXiLian.equipId != equipId ){
				equipXiLian = null ;
			}
		}else{
			equipXiLian = HibernateUtil.find(EquipXiLian.class,
					" where equipId=" + equipId + " and junZhuId=" + junZhu.id);
		}
		
		XiLianRes.Builder resp = sendXiLianInfo(junZhu, zb, dbUe, equipId,0,0,equipXiLian);
		session.write(resp) ;
	}
	/**
	 *  @Description 发送洗练装备信息
	 * 
	 */
	public XiLianRes.Builder sendXiLianInfo(JunZhu junZhu,
			ZhuangBei zb, UserEquip dbUe, long equipId, int freeXilianTimes,
			int time, EquipXiLian equipXiLian) {
		XiLianRes.Builder ret = XiLianRes.newBuilder();
		ret.setEquipId(equipId);
		ret.setTongShuai(zb.getTongli() + dbUe.tongli);
		ret.setWuYi(zb.getWuli() + dbUe.wuli);
		ret.setZhiMou(zb.getMouli() + dbUe.mouli);
		
		XiLian xiLian = PurchaseMgr.inst.getXiLian(junZhu.id);
		int needYuanBao = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.XILIAN, xiLian.num + 1);
		ret.setYuanBao(needYuanBao);
		int dayMaxTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.YBxilianLimit);
		int dayXilianshiTimes=xiLian.xlsCount;
		ret.setYuanBaoTimes(dayMaxTimes - xiLian.num);
		//本日洗练石使用次数
		ret.setXilianshiTimes(dayXilianshiTimes);
//		if (equipXiLian != null) {
//			ret.setTongShuaiAdd(equipXiLian.getTongShuiAdd());
//			ret.setWuYiAdd(equipXiLian.getWuYiAdd());
//			ret.setZhiMouAdd(equipXiLian.getZhiMouAdd());
//		}
		ret.setFreeXilianTimes(freeXilianTimes);
		ret.setTime(time);
		ret.setTongShuaiMax(getXiLianMaxValue(zb.getTongli(), zb,
				dbUe.level));
		ret.setWuYiMax(getXiLianMaxValue(zb.getWuli(), zb, dbUe.level));
		ret.setZhiMouMax(getXiLianMaxValue(zb.getMouli(), zb, dbUe.level));
		
		//以下1.0版本改变洗练逻辑
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqSH)){
			ret.setWqSH(zb.getWqSH() + dbUe.wqSH);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqJM)){
			ret.setWqJM(zb.getWqJM() + dbUe.wqJM);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqBJ)){
			ret.setWqBJ(zb.getWqBJ() + dbUe.wqBJ);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqRX)){
			ret.setWqRX(zb.getWqRX() + dbUe.wqRX);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnSH)){
			ret.setJnSH(zb.getJnSH() + dbUe.jnSH);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnJM)){
			ret.setJnJM(zb.getJnJM() + dbUe.jnJM);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnBJ)){
			ret.setJnBJ(zb.getJnBJ() + dbUe.jnBJ);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnRX)){
			ret.setJnRX(zb.getJnRX() + dbUe.jnRX);
		}
		//TODO 以下 1.1可能不加入
		//TODO 1.1版本以下5个属性有了就不会改变 所以这么判断，若该需求则可能需要改变逻辑
		//武器暴击率       
//		if (dbUe.getWqBJL()>0) {
//			ret.setWqBJL(dbUe.getWqBJL());
//		}
//		//技能暴击率	   
//		if(dbUe.getJnBJL()>0) {
//			ret.setJnBJL(dbUe.getJnBJL());
//		}
//		//武器免暴率	
//		if (dbUe.getWqMBL()>0) {
//			ret.setWqMBL(dbUe.getWqMBL());
//		}
//		//	技能免暴率    
//		if (dbUe.getJnMBL()>0) {
//			ret.setJnMBL(dbUe.getJnMBL());
//		}
//		//	属性加成  
//		if (dbUe.getSxJiaCheng()>0) {
//			ret.setSxJiaCheng(dbUe.getSxJiaCheng());
//		}  
//		log.info(" sendXiLianInfo装备={} 属性:"+
//				"wqSH----<{}> "+        
//				"wqJM----<{}> "+ 
//				"wqBJ----<{}> "+ 
//				"wqRX----<{}> "+ 
//				"jnSH----<{}> "+ 
//				"jnJM----<{}> "+ 
//				"jnBJ----<{}> "+ 
//				"jnRX----<{}> ", 
//				ret.getEquipId(),
//				ret.getWqSH(), 
//				ret.getWqJM(),
//				ret.getWqBJ(),
//				ret.getWqRX(),
//				ret.getJnSH(),
//				ret.getJnJM(),
//				ret.getJnBJ(),
//				ret.getJnRX()
//				);
		if (equipXiLian != null) {
			ret.setWqSHAdd(equipXiLian.wqSHAdd);
			ret.setWqJMAdd(equipXiLian.wqJMAdd);
			ret.setWqBJAdd(equipXiLian.wqBJAdd);
			ret.setWqRXAdd(equipXiLian.wqRXAdd);
			ret.setJnSHAdd(equipXiLian.jnSHAdd);
			ret.setJnJMAdd(equipXiLian.jnJMAdd);
			ret.setJnBJAdd(equipXiLian.jnBJAdd);
			ret.setJnRXAdd(equipXiLian.jnRXAdd);
		}
		//以上1.0版本改变洗练逻辑
		int maxSxValue = getXiLianMaxValue(0, zb, dbUe.level);
		ret.setWqSHMax(maxSxValue);
		ret.setWqJMMax(maxSxValue);
		ret.setWqBJMax(maxSxValue);
		ret.setWqRXMax(maxSxValue);
		ret.setJnSHMax(maxSxValue);
		ret.setJnJMMax(maxSxValue);
		ret.setJnRXMax(maxSxValue);
		ret.setJnBJMax(maxSxValue);
		ret.setAddMin(CanShu.XILIANADD_MIN);
		ret.setAddMax(CanShu.XILIANADD_MAX);
		//2015年10月8日 计算还需count4New次获得新属性   pinzhi=1时装备不可以洗练
		int pinzhi=zb.getPinZhi();
		//还需洗练count4New次出现新属性
		int count4New=0;
		if(pinzhi>1){
			int xilianzhi=	dbUe.xianlianzhi;
			XilianzhiQujian xlzqj=getXilianzhiQuJianByXilianzhi(xilianzhi);
			count4New=xlzqj==null?0:(xlzqj.Max-xilianzhi);
		}
		log.info("装备={} 的品质为={}，还需洗练{}次出现新属性",dbUe.equipId,pinzhi,count4New);
		ret.setCount4New(count4New);
		ret.setZhuangbeiID(zb.getId());
		return ret ;
//		session.write(ret.build());
		
	}
	
	/**
	 * @Description  获取装备信息	此方法只在jsp后台页面调用 
	 */
	public XiLianRes.Builder getXiLianInfoByInstId(long dbId) {
		XiLianRes.Builder ret = XiLianRes.newBuilder();
		EquipGrid eg=EquipMgr.inst.findEquip(dbId/100, dbId);
		long instId=eg.instId;
		UserEquip dbUe = UserEquipDao.find(eg.dbId/EquipMgr.spaceFactor, instId);
		if(dbUe==null){
			return ret;
		}
		long jzId=dbUe.userId;
		JunZhu junZhu=HibernateUtil.find(JunZhu.class, jzId);
//		int freeXilianTimes=	TimeWorkerMgr.instance.getXilianTimes(junZhu);
		int time=TimeWorkerMgr.instance.getXilianCountDown(jzId);
		ZhuangBei zb = TempletService.getInstance().getZhuangBei(dbUe.templateId);
		long equipId=eg.dbId;
		EquipXiLian equipXiLian = null ;
		if(useEquipXiLianCache){
			equipXiLian = EquipXiLianDao.inst.get(jzId, equipId);
		}else{
			equipXiLian = HibernateUtil.find(EquipXiLian.class, " where equipId=" + equipId + " and junZhuId=" + jzId);
		}
		
		ret.setEquipId(equipId);
		ret.setTongShuai(zb.getTongli() + dbUe.tongli);
		ret.setWuYi(zb.getWuli() + dbUe.wuli);
		ret.setZhiMou(zb.getMouli() + dbUe.mouli);
		log.info("{}  tongli{} wuli{} mouli{}", dbUe.equipId,
				dbUe.tongli, dbUe.wuli, dbUe.mouli);
		XiLian xiLian = PurchaseMgr.inst.getXiLian(junZhu.id);
		int needYuanBao = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.XILIAN, xiLian.num + 1);
		ret.setYuanBao(needYuanBao);
		int dayMaxTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.YBxilianLimit);
		ret.setYuanBaoTimes(dayMaxTimes - xiLian.num);
		if (equipXiLian != null) {
			ret.setTongShuaiAdd(equipXiLian.tongShuiAdd);
			ret.setWuYiAdd(equipXiLian.wuYiAdd);
			ret.setZhiMouAdd(equipXiLian.zhiMouAdd);
		}
		ret.setFreeXilianTimes(0);
		ret.setTime(time);
		ret.setTongShuaiMax(getXiLianMaxValue(zb.getTongli(), zb,
				dbUe.level));
		ret.setWuYiMax(getXiLianMaxValue(zb.getWuli(), zb, dbUe.level));
		ret.setZhiMouMax(getXiLianMaxValue(zb.getMouli(), zb, dbUe.level));
		
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqSH)){
			ret.setWqSH(zb.getWqSH() + dbUe.wqSH);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqJM)){
			ret.setWqJM(zb.getWqJM() + dbUe.wqJM);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqBJ)){
			ret.setWqBJ(zb.getWqBJ() + dbUe.wqBJ);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.wqRX)){
			ret.setWqRX(zb.getWqRX() + dbUe.wqRX);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnSH)){
			ret.setJnSH(zb.getJnSH() + dbUe.jnSH);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnJM)){
			ret.setJnJM(zb.getJnJM() + dbUe.jnJM);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnBJ)){
			ret.setJnBJ(zb.getJnBJ() + dbUe.jnBJ);
		}
		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnRX)){
			ret.setJnRX(zb.getJnRX() + dbUe.jnRX);
		}
		
		//TODO 以下 1.1可能不加入
//		//武器暴击率       
//		if (hasEquipTalent(dbUe,zb.getId(),UEConstant.wqBJL)) {
//			ret.setWqBJL(zb.getWqBJL());
//		}
//		//技能暴击率	   
//		if(hasEquipTalent(dbUe,zb.getId(),UEConstant.jnBJL)) {
//			ret.setJnBJL(zb.getJnBJL());
//		}
//		//武器免暴率	
//		if (hasEquipTalent(dbUe,zb.getId(),UEConstant.wqMBL)) {
//			ret.setWqMBL(zb.getWqMBL());
//		}
//		//	技能免暴率    
//		if (hasEquipTalent(dbUe,zb.getId(),UEConstant.jnMBL)) {
//			ret.setJnMBL(zb.getJnMBL());
//		}
//		//	属性加成  
//		if (hasEquipTalent(dbUe,zb.getId(),UEConstant.sxJiaCheng)) {
//			ret.setSxJiaCheng(zb.getSxJiaCheng());
//		} 
		if (equipXiLian != null) {
			ret.setWqSHAdd(equipXiLian.wqSHAdd);
			ret.setWqJMAdd(equipXiLian.wqJMAdd);
			ret.setWqBJAdd(equipXiLian.wqBJAdd);
			ret.setWqRXAdd(equipXiLian.wqRXAdd);
			ret.setJnSHAdd(equipXiLian.jnSHAdd);
			ret.setJnJMAdd(equipXiLian.jnJMAdd);
			ret.setJnBJAdd(equipXiLian.jnBJAdd);
			ret.setJnRXAdd(equipXiLian.jnRXAdd);
		}
		//以上1.0版本改变洗练逻辑
		ret.setWqSHMax(getXiLianMaxValue(zb.getWqSH(), zb, dbUe.level));
		ret.setWqJMMax(getXiLianMaxValue(zb.getWqJM(), zb, dbUe.level));
		ret.setWqBJMax(getXiLianMaxValue(zb.getWqBJ(), zb, dbUe.level));
		ret.setWqRXMax(getXiLianMaxValue(zb.getWqRX(), zb, dbUe.level));
		ret.setJnSHMax(getXiLianMaxValue(zb.getJnSH(), zb, dbUe.level));
		ret.setJnJMMax(getXiLianMaxValue(zb.getJnJM(), zb, dbUe.level));
		ret.setJnRXMax(getXiLianMaxValue(zb.getJnRX(), zb, dbUe.level));
		ret.setJnBJMax(getXiLianMaxValue(zb.getJnBJ(), zb, dbUe.level));
		//FIXME 1.1 加入5个属性洗出后为固定值  不发最大值
		ret.setAddMin(CanShu.XILIANADD_MIN);
		ret.setAddMax(CanShu.XILIANADD_MAX);
		ret.setZhuangbeiID(zb.getId());
		return ret;
	}
	/**
	 *  @Description 计算洗练累计值
	 * 
	 * @param baseValue
	 *            装备基础属性值
	 * @param curValue
	 *            当前记录洗练累计值
	 * @param maxValue
	 *            允许最大洗练值(1 小于等于 baseValue+curValue 小于等于maxValue)
	 * @return
	 */
	public int getXilianFinalValue(int baseValue, int curValue, int maxValue) {
		int totalValue = baseValue + curValue;
		if (totalValue <= 0) {
			return (-baseValue + 1);
		}
		if (totalValue > maxValue) {
			return (maxValue - baseValue);
		}
		return curValue;
	}

	/**
	 * @Description 获取装备属性（武艺、智谋、神佑）所能达到的最大值
	 * 
	 * @param baseValue
	 *            装备属性（武艺、智谋、神佑）的基础值
	 * @param zhuangBei
	 *            装备对象
	 * @param qiangHuaLv
	 *            强化等级
	 * @return
	 */
	public int getXiLianMaxValue(int baseValue, ZhuangBei zhuangBei,
			int qiangHuaLv) {
		int xiLianMaxValue = -1;
		// m11+k11×装备强化等级+装备对应属性基础值
		List<?> confList = TempletService.listAll(ZhuangbeiPinzhi.class
				.getSimpleName());
		if (confList == null || confList.size() == 0) {
			log.error("没有ZhuangbeiPinzhi数据");
			return baseValue;
		}
		int cnt = confList.size();
		ZhuangbeiPinzhi curConf = null;
		for (int i = 0; i < cnt; i++) {
			ZhuangbeiPinzhi conf = (ZhuangbeiPinzhi) confList.get(i);
			if (conf.pinzhi == zhuangBei.pinZhi) {
				curConf = conf;
				break;
			}
		}
		if (curConf == null) {
			log.error("没有找打洗练配置 ZhuangbeiPinzhi ，装备{}，品质{}", zhuangBei.id,
					zhuangBei.pinZhi);
			return baseValue;
		}
//		double v = curConf.paraX + curConf.paraK * qiangHuaLv + baseValue;
		//2015年10月19日 去掉baseValue
		double v = curConf.paraX + curConf.paraK * qiangHuaLv;
		xiLianMaxValue = (int) Math.rint(v);
		return xiLianMaxValue;
	}

	/**
	 * @Description  装备进阶操作
	 */
	public void equipJinJie(int cmd, IoSession session, Builder builder) {
		EquipJinJie.Builder request = (qxmobile.protobuf.UserEquipProtos.EquipJinJie.Builder) builder;
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("未发现君主");
			return;
		}
		long equipId = request.getEquipId();
		//获取请求进阶目标装备信息
		EquipGrid target = null;
		Bag<EquipGrid> equipBag = EquipMgr.inst.loadEquips(junZhu.id);
		for (EquipGrid equipGrid : equipBag.grids) {
			if (equipGrid == null)
				continue;
			if (equipGrid.dbId == equipId) {
				target = equipGrid;
				break;
			}
		}
		if (target == null) {
			log.error("请求进阶操作的装备equipId:{}不再装备栏里", equipId);
			return;
		}
		
//		int targetItemId = target.itemId;
		long targetInstId = target.instId;
		//获取目标装备配置
		TempletService template = TempletService.getInstance();
		ZhuangBei zhuangBeiTmp = template.getZhuangBei(target.itemId);
		if (zhuangBeiTmp == null) {
			log.error("没有找到对应的装备,zhuangBeiId:", target.itemId);
			sendError(cmd, session, "没有找到对应的装备,zhuangBeiId:" + target.itemId);
			return;
		}

		int jinJieLV = zhuangBeiTmp.getJinjieLv();
		if (junZhu.level < jinJieLV) {
			log.error("装备进阶失败，君主等级不够:{}级,zhuangBeiId:{}", jinJieLV,
					target.itemId);
			sendError(cmd, session, "君主等级未达到进阶要求");
			return;
		}

		int jinJieZbId = zhuangBeiTmp.getJiejieId();
		if (jinJieZbId <= 0) {
			log.error("装备品质已达最高，无法进阶,zhuangBeiId:{}", jinJieLV, target.itemId);
			sendError(cmd, session, "装备品阶已达最高");
			return;
		}

		int jinJieItemId = zhuangBeiTmp.getJinjieItem();
		int jinJieNum = zhuangBeiTmp.getJinjieNum();
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<BagGrid> gridList = bag.grids;
		int haveNum = 0;
		for (BagGrid grid : gridList) {
			if (grid.itemId == jinJieItemId) {
				haveNum += grid.cnt;
			}
		}
		if (haveNum < jinJieNum) {
			log.error("装备进阶失败，进阶材料数量不足:{},zhuangBeiId:{}", jinJieNum,
					target.itemId);
			sendError(cmd, session, "进阶材料数量不足");
			return;
		}

		UserEquip dbUe = null;
		if (targetInstId > 0) {
			dbUe = UserEquipDao.find(junZhu.id, targetInstId);
			if (dbUe == null) {
				log.error("已经强化的数据丢失了,equipId;{}", targetInstId);
				sendError(cmd, session, "已经强化的数据丢失了,equipId;{}" + targetInstId);
				return;
			}
		} else {
			dbUe = new UserEquip();
			dbUe.userId = (int) junZhu.id;
			dbUe.templateId = zhuangBeiTmp.getId();
			UserEquipDao.insert(dbUe);
			MC.add(dbUe, dbUe.getIdentifier());
		}

		int gongJiBefore = zhuangBeiTmp.getGongji();
		int fangYuBefore = zhuangBeiTmp.fangyu;
		int shengMingBefore = zhuangBeiTmp.shengming;

		int qiangHuaLv = dbUe.level;
		int qiangHuaExp = dbUe.exp;
		List<ExpTemp> expTempList = template.getExpTemps(zhuangBeiTmp
				.getExpId());
		if (expTempList == null) {
			sendError(0, session, "配置错误。" + zhuangBeiTmp.getId());
			log.error("配置错误，没有经验配置，装备ID{}", zhuangBeiTmp.getId());
			return;
		}
		for (ExpTemp temp : expTempList) {
			if (temp.level < qiangHuaLv) {
				qiangHuaExp += temp.needExp;
			}
		}

		// 进阶后的装备信息
		ZhuangBei jinJieZb = template.getZhuangBei(jinJieZbId);
		if (jinJieZb == null) {
			log.error("未找到对应装备配置信息，zhuangbeiId:{}......................", jinJieZbId);
			sendError(cmd, session, "装备进阶失败，没有进阶后的装备");
			return;
		}

		List<ExpTemp> expTemps = template.getExpTemps(jinJieZb.getExpId());
		Collections.sort(expTemps);
		int afterLevel = 0; // 初始等級為0
		int afterExp = qiangHuaExp;
		for (ExpTemp temp : expTemps) {
			if (temp.needExp == -1) {// 表示满级
				break;
			}
			if (afterExp >= temp.needExp) {
				afterLevel += 1;
				afterExp -= temp.needExp;
			}
		}

		QiangHua qiangHua = TempletService.getInstance().getQiangHua(
				jinJieZb.getQianghuaId(), afterLevel);
		if (qiangHua == null) {
			log.info("{}没有强化配置强化id:{},lv:{}", jinJieZb.getId(),
					jinJieZb.getQianghuaId(), afterLevel);
			sendError(cmd, session, "装备进阶失败，强化配置有误。");
			return;
		}

		// 扣除材料
		int subNum = jinJieNum;
		if (BagMgr.inst.getItemCount(bag, jinJieItemId) < subNum) {
			sendError(cmd, session, "所需的物品不足，请收集齐全后再来！");
			return;
		}
		BagMgr.inst.removeItem(session, bag, jinJieItemId, subNum, "装备进阶",junZhu.level);
//		BagMgr.inst.sendBagInfo(cmd, session, builder); 2015年10月9日优化去掉 下面也会发
		target.itemId = jinJieZb.getId();
		dbUe.templateId = jinJieZb.getId();
		dbUe.level = afterLevel;
		dbUe.exp = afterExp;
		HibernateUtil.save(dbUe);
		HibernateUtil.save(target);

		log.info("{}将装备{}成功进阶为{}", junZhu.id, target.itemId, jinJieZb.getId());
		EventMgr.addEvent(junZhu.id,ED.GAIN_ITEM, new Object[]{junZhu.id, jinJieZb.getId()});
		ActLog.log.EquipLvup(junZhu.id, junZhu.name, jinJieZb.getId(), jinJieZb.getName(), target.itemId, zhuangBeiTmp.getName(), 
				jinJieItemId + "", jinJieNum);

		// 发送进阶后的装备信息
		int maxLv = jinJieZb.getQianghuaMaxLv();
		EquipJinJieResp.Builder response = EquipJinJieResp.newBuilder();
		response.setEquipId(equipId);
		response.setZbItemId(target.itemId);
		response.setExp(dbUe.exp);
		response.setLevel(dbUe.level);
		if (dbUe.level >= maxLv) {
			response.setExpMax(-1);
		} else {
			response.setExpMax(expTemps.get(dbUe.level).needExp);
		}
		int gongJiAfter = jinJieZb.getGongji() + qiangHua.gongji;
		int fangYuAfter = jinJieZb.fangyu + qiangHua.fangyu;
		int shengMingAfter = jinJieZb.shengming + qiangHua.shengming;

		response.setGongJi(gongJiAfter);
		response.setFangYu(fangYuAfter);
		response.setShengMing(shengMingAfter);
		response.setGongJiAdd(Math.max(gongJiAfter - gongJiBefore, 0));
		response.setFangYuAdd(Math.max(fangYuAfter - fangYuBefore, 0));
		response.setShengMingAdd(Math.max(shengMingAfter - shengMingBefore, 0));
		//2015年10月9日优化    先返进阶回结果再推送其他信息
		session.write(response.build());
		//BagMgr.inst.sendBagInfo(session, bag);
		BagMgr.inst.sendEquipInfo(session, equipBag);
		// 主线任务：完成特定的武器进阶一次
		EventMgr.addEvent(junZhu.id,ED.JINJIE_ONE_GONG, new Object[] { junZhu.id,
				jinJieZb.getId(), equipBag});
		// 发送战力信息
//		JunZhuMgr.inst.sendPveMibaoZhanli(junZhu, session);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		
	}
	public void newEquipJinJie(int cmd, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null){
			log.error("装备进阶请求失败，未找到君主信息");
			return ;
		}
		if(!FunctionOpenMgr.inst.isFunctionOpen(jinJieFunctionId, junZhu.id, junZhu.level)){
			log.error("君主{}尝试装备进阶失败，等级{}尚未开启装备进阶功能",junZhu.id , junZhu.level);
			return ;
		}
		
		EquipJinJie.Builder req = (EquipJinJie.Builder) builder;
		
		long equipId = req.getEquipId();
	
		EquipGrid equip = null;
		Bag<EquipGrid> equipBag = EquipMgr.inst.loadEquips(junZhu.id);
		for (EquipGrid equipGrid : equipBag.grids) {
			if (equipGrid == null)
				continue;
			if (equipGrid.dbId == equipId) {
				equip = equipGrid;
				break;
			}
		}
		if (equip == null) {
			log.error("君主{}请求进阶操作的装备equipId:{}不在装备栏里",junZhu.id, equipId);
			return;
		}
		//加载目标装备配置
		ZhuangBei equipPeiZhi = getEquipPeiZhi(equip.itemId);
		if(equipPeiZhi == null){
			log.error("君主{}请求进阶失败，无法获取装备配置信息，装备id:{}" ,junZhu.id , equip.itemId);
			return ;
		}
		if(equipPeiZhi.lvlupExp <= 0 ){
			log.error("君主{}请求进阶失败，装备的已经进阶至最高，装备id:{}",junZhu.id,equip.itemId);
			return ;
		}
		
		//获取请求材料列表
		List<Long> reqCailiaoList = req.getCailiaoListList();
		log.info("获取到的材料列表长度为：{}",reqCailiaoList.size());
		//加载玩家背包，遍历请求材料列表，转换为id+数量的map
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		Map<Long,Integer> reqCailiaoMap = new HashMap<Long,Integer>() ;
		for(long reqid : reqCailiaoList){
			Integer cnt = reqCailiaoMap.put(reqid,1);
			if( cnt != null ){
				cnt ++ ;
				reqCailiaoMap.put(reqid, cnt);
			}
		}
		//检查请求材料的合法性，获取合法材料列表
		List<BagGrid> legalCailiaoList = jinJieCaiLiaoCheck(reqCailiaoMap, bag, equip.itemId);
		log.info("装备进阶材料检查完成，列表长度为：{}",legalCailiaoList.size());
		
		//获取目标装备的强化信息
		UserEquip equipInfo = getUserEquipInfo(equip, junZhu.id);
		
		//记录初始的装备攻击、防御、生命
		QiangHua qhPeiZhiBefore = TempletService.getInstance().getQiangHua(equipPeiZhi.qianghuaId, equipInfo.level);
		if(qhPeiZhiBefore == null ){
			log.error("装备进阶，获取装备的强化配置失败，装备id：{}，装备等级：{}" ,equipPeiZhi.qianghuaId ,equipInfo.level);
			return ;
		}
		int gongJiBefore = equipPeiZhi.getGongji() + qhPeiZhiBefore.gongji ;
		int fangYuBefore = equipPeiZhi.fangyu + qhPeiZhiBefore.fangyu ;
		int shengMingBefore = equipPeiZhi.shengming + qhPeiZhiBefore.shengming ;
		
		//根据经验配置记录装备进阶之前的总强化经验
		//不同装备的强化经验配置不同，所以需要记录总经验在进阶之后重新计算强化等级
		int qhExpBefore = equipInfo.exp;
		List<ExpTemp> qhExpPeiZhi  = TempletService.getInstance().getExpTemps(equipPeiZhi.expId);
		for(ExpTemp et : qhExpPeiZhi){
			if(et.level < equipInfo.level){
				qhExpBefore += et.needExp;
			}
		}
		
		//旧装备的新信息记录完成，开始准备进阶，计算合法材料的总经验，并扣除材料
		int totalExp = 0 ;//总经验
		for(BagGrid bg : legalCailiaoList ){
			int addExp = 0 ;//一格道具的经验
			int perExp = TempletService.getInstance().getZhuangBei(bg.itemId).exp; //一个道具的经验
			if(perExp <= 0 ){
				log.error("道具{}的进阶经验为0，找策划确认配置", bg.itemId);
				continue;
			}
			int cailiaoNum = reqCailiaoMap.get(bg.dbId);
			addExp = perExp * cailiaoNum ;
			totalExp += addExp;
			BagMgr.inst.removeItemByBagdbId(session, bag, "装备进阶，吞噬材料", bg.dbId, cailiaoNum, junZhu.level);
		}
		log.info("玩家：{}请求进阶装备，请求材料总经验：{}",junZhu.id,totalExp);
		
		//开始进阶
		equipInfo.JinJieExp += totalExp;
		while(equipInfo.JinJieExp >= equipPeiZhi.lvlupExp){
			//先检查下一阶的装备配置是否存在，不存在，结束进阶操作
			ZhuangBei equipPeiZhiNew = TempletService.getInstance().getZhuangBei(equipPeiZhi.jiejieId);
			if( equipPeiZhiNew == null){
				log.error("君主{}进阶装备出错：装备{}的下一阶配置存在{}",junZhu.id,equip.itemId,equipPeiZhi.jiejieId);
				break;
			}
			equipInfo.templateId = equipPeiZhi.jiejieId;//经验足够进阶，道具ID更新至下一阶装备，jiejieId属于拼写错误
			equip.itemId = equipInfo.templateId;//装备格子的道具ID同步更新
			equipInfo.JinJieExp -= equipPeiZhi.lvlupExp;//扣除进阶所需经验
			equipPeiZhi = equipPeiZhiNew ;//配置信息指向新一级配置
			//装备进阶 才可触发 获得装备的广播 事件
			EventMgr.addEvent(junZhu.id,ED.GAIN_ITEM, new Object[]{junZhu.id, equip.itemId});
			if(equipPeiZhi.lvlupExp <=0){
				//进阶到最高了，跳出循环
				break;
			}
		}
		//进阶完成，根据原有总强化经验设定新的强化等级
		int qhLevelNew = 0 ;
		ExpTemp expPeiZhi = TempletService.getInstance().getExpTemp(equipPeiZhi.expId, qhLevelNew);
		if(expPeiZhi != null ){
			int expNeed = expPeiZhi.needExp;
			 while(qhExpBefore >= expNeed && expNeed > 0){
				 ExpTemp expPeiZhiNew= TempletService.getInstance().getExpTemp(equipPeiZhi.expId, qhLevelNew +1);
				 if(expPeiZhiNew == null ){
					 log.error("君主{}装备{}进阶，强化经验信息配置找不到，强化等级{}",junZhu.id,equip.itemId,qhLevelNew);
					 break;
				 }
				 qhLevelNew ++ ;
				 qhExpBefore -= expNeed;
				 expPeiZhi = expPeiZhiNew;
				 expNeed = expPeiZhi.needExp;
			}
		}else{
			 log.error("君主{}装备{}进阶，强化经验信息配置找不到，强化等级{}",junZhu.id,equip.itemId,qhLevelNew);
			 return;
		}
		 
		equipInfo.level = qhLevelNew;
		equipInfo.exp = qhExpBefore;
		//强化修改完成，存储信息，返回协议
		HibernateUtil.save(equipInfo) ;
		HibernateUtil.save(equip) ;
		
		//获取装备的当前攻击、防御、生命、强化配置信息、强化等级
		int qianghuaId = equipPeiZhi.getQianghuaId();
		int qianghuaLevel = equipInfo.level;
		QiangHua qhPeiZhi = TempletService.getInstance().getQiangHua(qianghuaId, qianghuaLevel);
		
		int gongJiAfter = equipPeiZhi.getGongji() + qhPeiZhi.gongji;
		int fangYuAfter = equipPeiZhi.fangyu + qhPeiZhi.fangyu;
		int shengMingAfter = equipPeiZhi.shengming + qhPeiZhi.shengming;
		
		log.info("玩家进阶装备成功，装备ID：{}，装备经验：{}",equip.itemId,equipInfo.JinJieExp);
		EquipJinJieResp.Builder response = EquipJinJieResp.newBuilder();
		response.setEquipId(equipId);
		response.setZbItemId(equip.itemId);
		response.setExp(equipInfo.JinJieExp);
		
		response.setLevel(equipInfo.level);
		response.setExpMax(equipPeiZhi.lvlupExp);
		response.setGongJi(gongJiAfter);
		response.setFangYu(fangYuAfter);
		response.setShengMing(shengMingAfter);
		response.setGongJiAdd(Math.max(gongJiAfter - gongJiBefore, 0));
		response.setFangYuAdd(Math.max(fangYuAfter - fangYuBefore, 0));
		response.setShengMingAdd(Math.max(shengMingAfter - shengMingBefore, 0));
		//BagMgr.inst.sendBagInfo(session, bag);
		BagMgr.inst.sendEquipInfo(session, equipBag);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		session.write(response.build());	
		// 主线任务：完成特定的武器进阶一次
		EventMgr.addEvent(junZhu.id,ED.JINJIE_ONE_GONG, 
				new Object[] { junZhu.id,equip.itemId, equipBag});
		
	}
	/**检查进阶材料的合法性，返回合法的格子*/
	public List<BagGrid> jinJieCaiLiaoCheck(Map<Long, Integer> cailiaoMap , Bag<BagGrid> bag , int itemId ){
		List<BagGrid> res = new ArrayList<BagGrid>();
		for(long reqid : cailiaoMap.keySet()){
			BagGrid bg = findByDBId(bag, reqid);
			if(bg == null ){
				//如果背包格子没有信息，跳过
				log.info("装备进阶材料背包信息为空");
				continue;
			}
			if(bg.instId > 0){
				//如果合成材料装备有强化信息，，跳过
				//FIXME 策划没有提这个需求，目前背包里的装备不可强化，如果后续需求有变更，处理一下2016-04-28
				log.info("装备进阶材料有强化信息");
				continue;
			}
			if(bg.cnt  < cailiaoMap.get(reqid)){
				//道具数量不够，跳过
				log.info("装备进阶材料数量不足");
				continue;
			}
			ZhuangBei cailiaoPeiZhi = TempletService.getInstance().getZhuangBei(bg.itemId) ;
			ZhuangBei targetPeiZhi =  TempletService.getInstance().getZhuangBei(itemId) ;
			if(cailiaoPeiZhi == null){
				//材料不是装备，跳过
				log.info("装备进阶材料不是装备");
				continue;
			}
			if(cailiaoPeiZhi.buWei != targetPeiZhi.buWei){
				//装备部位不同，跳过
				log.info("装备进阶材料与目标装备部位不同");
				continue;
			}
			if(cailiaoPeiZhi.pinZhi > targetPeiZhi.pinZhi){
				//材料品质高于目标品质，跳过
				log.info("装备进阶材料材料品质高于目标品质");
				continue;
			}
			res.add(bg);
		}
		
		return res ;
	}
	/**从背包中获取指定位置的背包格子信息*/
	public BagGrid findByDBId(Bag<BagGrid> bag, long dbid) {
		for(BagGrid bg:bag.grids){
			if(bg == null){
				continue;
			}
			if(bg.dbId == dbid){
				return bg;
			}
		}
		return null;
	}
	
	/**获取玩家装备的强化信息，如果没有，就创建一个返回*/
	public UserEquip getUserEquipInfo( EquipGrid equip , long junZhuId){
		UserEquip res = null ;
		if(equip.instId > 0){
			res = UserEquipDao.find(junZhuId, equip.instId);
		}
		//不安全，equip.instId大于0却无法找到装备强化信息时，会导致玩家装备强化信息丢失
		if(res == null ){
			res = new UserEquip();
			res.userId = junZhuId;
			res.templateId = equip.itemId;
			UserEquipDao.insert(res);
			MC.add(res, res.getIdentifier());
			equip.instId = res.equipId;
		}
		return res ;
	}
	
	
	/**
	* @Description 	 判断所有装备是否有可以进阶
	 * 功能改动，此方法对应需求取消
	 */
	public boolean isCanJinJie4All(JunZhu junZhu) {
		//equipId 表示  equipGrid.dbId
		if(junZhu == null ){
			return false;
		}
		long jzId=	junZhu.id;
		int level= junZhu.level;
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.jinJie, jzId, level);
		if(!isOpen){
			log.info("君主--{}的功能---{}未开启,不推送",jzId,FunctionID.jinJie);
			return false;
		}
		Bag<EquipGrid> equipBag = EquipMgr.inst.loadEquips(jzId);
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<UserEquip> ueList = UserEquipDao.list(jzId);
		Map<Long , UserEquip> ueMap = new HashMap<Long , UserEquip>();
		for(UserEquip ue : ueList){
			if(ue == null )continue;
			ueMap.put(ue.equipId, ue);
		}
		for (EquipGrid equipGrid : equipBag.grids) {
			if (equipGrid == null){
				continue;
			}else{
				if(isCanJinJie(equipGrid ,bag ,ueMap)){
					log.info("君主{}有可以进阶的装备equipGrid.dbId--{}",jzId,equipGrid.dbId);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param bag 
	 * @Description 	//判断装备是否可以进阶,推送红点
	 */
	public boolean isCanJinJie(EquipGrid target, Bag<BagGrid> bag,Map<Long , UserEquip> ueMap) {
		//equipId表示  equipGrid.dbId
		if (target == null) {
			return false;
		}
		int targetItemId = target.itemId;
		TempletService template = TempletService.getInstance();
		ZhuangBei zhuangBeiTmp = template.getZhuangBei(targetItemId);
		if (zhuangBeiTmp == null) {
			return false;
		}
		int needExp = zhuangBeiTmp.lvlupExp;
		if(needExp <= 0){
			return false;
		}
		UserEquip ue = ueMap.get(target.instId);
		int hasExp = ue == null ? 0 : ue.JinJieExp ;
		int len = bag.grids.size();
		for(int i=0; i<len; i++){
			BagGrid bg = bag.grids.get(i);
			if(bg == null)continue;
			BaseItem bi = TempletService.itemMap.get(bg.itemId);
			if(bi == null)continue;
			if(bi.getType() != BaseItem.TYPE_EQUIP)continue;
			ZhuangBei caiLiaoTmp = template.getZhuangBei(bg.itemId);
			if(caiLiaoTmp == null)continue;
			if(caiLiaoTmp.buWei != zhuangBeiTmp.buWei)continue;
			if(caiLiaoTmp.pinZhi > zhuangBeiTmp.pinZhi)continue;
			hasExp += caiLiaoTmp.exp;
			if(hasExp >= needExp){
				return true ;
			}
		}
		return false;
	}
	
	/**
	 * @Description 获取装备是否有此洗练属性
	 * @param dbUe 
	 * @param code 洗练属性编码
	 */
	public boolean hasEquipTalent(UserEquip	dbUe,int templateId,String code) {
		if(dbUe==null||dbUe.hasXilian==null){
			//根据装备ID读取洗练配置
			XilianShuxing shuxing=xilianShuxingMap.get(templateId);
			if(shuxing==null){
				return false;
			}
			String returnStr=shuxing.Shuxing1;
//			log.info("获取equipId=={}装备已有的洗练属性的配置数据为{}",dbUe.getEquiped(),returnStr);
			return returnStr.contains(code);
		}
//		log.info("获取equipId=={}装备已有的洗练属性的数据库存储数据为{}",dbUe.getEquiped(),dbUe.hasXilian);
		return dbUe.hasXilian.contains(code);
	}
	//判断洗练信息用不用推送红点
	public void isCanPushXilian(JunZhu jz,IoSession session) {
		long jzId=jz.id;
		int level=jz.level;
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.XiLian, jzId, level);
		if(!isOpen){
//			log.info("君主--{}的功能---{}未开启,不推送",jzId,FunctionID.XiLian);
			return;
		}
		if(isXiMan4All(jzId)){
//			log.info("君主--{}身上装备洗满,不推送",jzId);
			return;
		}
		//免费洗练次数判断 1.1废弃免费洗练
//		int freeXilianCount=TimeWorkerMgr.instance.getXilianTimes(jz);
//		if(freeXilianCount>0){
//			log.info("君主{}有免费洗练次数--{}，推送信息",jzId,freeXilianCount);
//			FunctionID.pushCanShangjiao(jzId, session, FunctionID.XiLian);
//			return;
//		}
		//洗练石次数判断
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		int cnt = BagMgr.inst.getItemCount(bag, xilianshi);
		if(cnt<=0){
			return;
		}
		XiLian xiLian = PurchaseMgr.inst.getXiLian(jzId);
		int xilianshiCount=xiLian.xlsCount;
		if(xilianshiCount <CanShu.XILIANSHI_MAXTIMES){
//			log.info("君主{}洗练石今日洗练次数--{},有洗练石--{}个，推送信息",jzId,xilianshiCount,cnt);
			FunctionID.pushCanShowRed(jzId, session, FunctionID.XiLian);
		}
	}
	
	/**
	 * @Description 判断君主身上所有装备是否洗满
	 * @param jzId
	 * @return 洗满返回true 没洗满返回false
	 */
	public boolean isXiMan4All(long jzId ) {
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(jzId);
		List<UserEquip> ueList = UserEquipDao.list(jzId);
		Map<Long ,UserEquip > ueMap = ueList.stream().collect(Collectors.toMap(ue ->ue.equipId, ue->ue));
		for (EquipGrid eg : equips.grids) {
			if(eg== null ||eg.instId<=0){
				continue;
			}
			if(!isXiMan4One(eg , ueMap.get( eg.instId ))){
				//如果有装备没洗满，返回
				return false;
			}
		}
		return true;
	}
	
	public ZhuangBei getEquipPeiZhi( int itemId ){
		ZhuangBei res = null;
		res = TempletService.getInstance().getZhuangBei(itemId);
		return res ;
	}
	
	/**
	 * @Description 判断一件装备是否洗满
	 * @param bg 
	 * @return  洗满返回true 没洗满返回false
	 */
	public boolean isXiMan4One(EquipGrid bg , UserEquip dbUe ) {
		int targetItemId = bg.itemId;
		long targetInstId = bg.instId;
		ZhuangBei zhuangBeiTmp =  TempletService.getInstance().getZhuangBei(targetItemId);
		if(zhuangBeiTmp==null||zhuangBeiTmp.pinZhi <= 1) {
			log.warn("白色装备不能进行洗练操作zhuangbeiId:{}", targetItemId);
			return true ;
		}
		if (dbUe == null) {
			log.warn("没有装备UserEquip--equipId--{}的数据，没洗满", targetItemId);
			return false;
		}
		StringBuffer shuxing2Max=getMaxShuxing(targetItemId);
		if(shuxing2Max==null){
			log.error("instId--{}，itemId--{}的装备未取到最大洗练条数属性",targetInstId, targetItemId);
			return true;
		}
		//没洗出7条属性，就没洗满，就推送红点
		if(dbUe.hasXilian!=null&&dbUe.hasXilian.length()<shuxing2Max.length()){
				return false;
		}
		int maxSxValue =getXiLianMaxValue(zhuangBeiTmp.getWqSH(), zhuangBeiTmp, dbUe.level);
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqSH)) {
			if(zhuangBeiTmp.getWqSH()+dbUe.wqSH<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqJM)) {
			if(zhuangBeiTmp.getWqJM()+dbUe.wqJM<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqBJ)) {
			if(zhuangBeiTmp.getWqBJ()+dbUe.wqBJ<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqRX)) {
			if(zhuangBeiTmp.getWqRX()+dbUe.wqRX<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnSH)) {
			if(zhuangBeiTmp.getJnSH()+dbUe.jnSH<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnJM)) {
			if(zhuangBeiTmp.getJnJM()+dbUe.jnJM<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnBJ)) {
			if(zhuangBeiTmp.getJnBJ()+dbUe.jnBJ<maxSxValue){
				return false;
			}
		}
		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnRX)) {
			if(zhuangBeiTmp.getJnRX()+dbUe.jnRX<maxSxValue){
				return false;
			}
		}
		
		//TODO 以下 1.1可能不加入
		//TODO 1.1加入5条属性 次五条属性不会增加，有了就算洗满
//		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqBJL)) {
////			int maxWqBJL=getXiLianMaxValue(zhuangBeiTmp.getWqBJL(), zhuangBeiTmp, dbUe.level);
////			if(zhuangBeiTmp.getWqBJL()+dbUe.getWqBJL()<maxWqBJL){
//				return false;
////			}
//		}
//		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnBJL)) {
////			int maxJnBJL=getXiLianMaxValue(zhuangBeiTmp.getJnBJL(), zhuangBeiTmp, dbUe.level);
////			if(zhuangBeiTmp.getJnBJL()+dbUe.getJnBJL()<maxJnBJL){
//				return false;
////			}
//		}
//		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.wqMBL)) {
////			int maxWqMBL=getXiLianMaxValue(zhuangBeiTmp.getWqMBL(), zhuangBeiTmp, dbUe.level);
////			if(zhuangBeiTmp.getWqMBL()+dbUe.getWqMBL()<maxWqMBL){
//				return false;
////			}
//		}
//		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.jnMBL)) {
////			int maxJnMBL=getXiLianMaxValue(zhuangBeiTmp.getJnMBL(), zhuangBeiTmp, dbUe.level);
////			if(zhuangBeiTmp.getJnMBL()+dbUe.getJnMBL()<maxJnMBL){
//				return false;
////			}
//		}
//		//属性加成
//		if (hasEquipTalent(dbUe,zhuangBeiTmp.getId(),UEConstant.sxJiaCheng)) {
//				return false;
//		}
		//TODO 以上 1.1可能不加入
		//以上1.1加入
		return true;
	}
	@Override
	public void proc(Event evt) {
		switch (evt.id) {
		case ED.REFRESH_TIME_WORK:{
			IoSession session=(IoSession) evt.param;
			if(session==null){
				log.error("定时刷新洗练次数错误，session为null");
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz==null){
				log.error("定时刷新洗练次数错误，JunZhu为null");
				break;
			}
			try {
				isCanPushXilian(jz, session);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 判断是否可以进阶
			boolean isCanJinjie=isCanJinJie4All(jz);
			if(isCanJinjie){
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.jinJie);
			}
		}	
		break;
		case ED.JUNZHU_LOGIN :{
			JunZhu junZhu = (JunZhu) evt.param;
			IoSession session = AccountManager.sessionMap.get(junZhu.id);
			if(session == null){
				return;
			}
			if( !FunctionOpenMgr.inst.isFunctionOpen(xiLianFunctionId, junZhu.id, junZhu.level) ){
				//功能未开，结束所有处理
				return;
			}
			try {
				isCanPushXilian(junZhu, session);
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean isCanJinjie=isCanJinJie4All(junZhu);
			if(isCanJinjie){
				FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.jinJie);
			}
		}
		break;
		default:
			log.error("错误事件参数",evt.id);
			break;
		}
	}

	@Override
	public void doReg() {
		//定时刷新 2015年9月17日
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
	}
	
	public Map<Integer, String> getJinJieTuiJian(JunZhu junZhu ,Map<Long,UserEquip > ueMap){
		Map<Integer ,String> res = new LinkedHashMap<Integer ,String>();
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		Bag<EquipGrid> equips = EquipMgr.inst.loadEquips(junZhu.id);
		String jinJieSort[] = new String[9];
		//排序字符串：需要进阶经验：品质：部位：装备id
		Arrays.fill(jinJieSort, "99999:9:99:8888");
		int idx = -1;
		for(EquipGrid eg : equips.grids ){
			idx ++ ;
			if(eg == null || eg.itemId<=0){
				continue;
			}
			ZhuangBei equipTemp = TempletService.getInstance().getZhuangBei(eg.itemId);
			if(equipTemp == null) continue;
			if(equipTemp.jiejieId <=0){		//进阶已满，没有下一阶装备
				jinJieSort[idx] = "99999:"		//进阶经验填写为99999
						+equipTemp.pinZhi+":"	//装备品质
						+String.format("%02d",buWeiMap.get(equipTemp.buWei).rank)+":"		//装备部位
						+eg.itemId ;		//装备id
				continue ;
			}
			if(isCanJinJie(eg, bag, ueMap)){	//背包材料足够装备进阶
				jinJieSort[idx] = "00000:"			//进阶经验为0
						+equipTemp.pinZhi+":"	
						+String.format("%02d",buWeiMap.get(equipTemp.buWei).rank)+":"
						+eg.itemId ;
				continue ;
			}
			UserEquip ue = ueMap.get(eg.instId);
			if(ue == null ){			//无强化信息
				jinJieSort[idx] = String.format("%05d",equipTemp.lvlupExp)+":"	//进阶经验为配置需求经验
						+equipTemp.pinZhi+":"
						+String.format("%02d",buWeiMap.get(equipTemp.buWei).rank)+":"
						+eg.itemId ;
				continue ;
			}
			//有强化信息，进阶经验为配置需求经验减去已有进阶经验
			jinJieSort[idx] = String.format("%05d",equipTemp.lvlupExp - ue.JinJieExp)+":"	
					+equipTemp.pinZhi+":"
					+String.format("%02d",buWeiMap.get(equipTemp.buWei).rank)+":"
					+eg.itemId ;
		}
		//排序
		Arrays.sort(jinJieSort);
		int mark = 0 ;

//		a)	第1优先：可进阶但未进阶的装备。
//		i.	存在多个满足以上条件的装备时，品质低的优先推荐；
//		ii.	存在多个满足以上条件且品质相同的装备时，
//		按照长柄武器>双持武器>远程武器>头>肩>胸>手>腿>脚的优先级推荐

		for(; mark < 3 ; mark++){
			if(!jinJieSort[mark].startsWith("00000:")){
				break;
			}
			res.put(Integer.parseInt(jinJieSort[mark].split(":")[3]), "身上装备");
			log.error("装备{}可进阶；加入进阶推荐", Integer.parseInt(jinJieSort[mark].split(":")[3]) );
		}
		
		

//		b)	第2优先：若当前装备中存在七日礼包奖励进阶的装备，将最快获得的装备推荐出来（仅1件）
		
		//获取七日礼包配置
		List<QiRiLiBao> liBaoList = TempletService.listAll(QiRiLiBao.class.getSimpleName());
		hasQiRi:for(QiRiLiBao lb: liBaoList ){
			//从第一天开始获取七日礼包奖励的装备
			if( lb.zhuangbeiId <=0 )continue;
			ZhuangBei liBaoTemp = TempletService.getInstance().getZhuangBei(lb.zhuangbeiId);
			if( liBaoTemp == null )continue;
			Boolean hasZhuangBei = false ;
			//确认奖励是装备，对排序数组进行遍历
			for(int i = mark ; i < jinJieSort.length ; i++){
				//排序字符串中包含这个部位，就获取装备的ID，进行比较
				if(jinJieSort[i].contains(":"+String.format("%02d",buWeiMap.get(liBaoTemp.buWei).rank)+":")){
					hasZhuangBei = true ; 
					int equipId = Integer.parseInt(jinJieSort[i].split(":")[3]);
					ZhuangBei equipTemp = TempletService.getInstance().getZhuangBei(equipId);
					//已穿装备的品质小于礼包里装备的品质，加入推荐，结束七日礼包的判断循环
					if(equipTemp == null | equipTemp.pinZhi < liBaoTemp.pinZhi){
						String str = "七日礼包中第XX天获得".replace("XX", String.valueOf(lb.day));
						res.put(liBaoTemp.id, str);
						log.error("装备{}有更高级装备在七日礼包中，加入进阶推荐",liBaoTemp.id);
						break hasQiRi;
					}
				}
			}
			//排序数组遍历完成，没有结束七日礼包判断循环；
			//1.该部位没有装备；2.该部位装备比奖励的好（不可能）由hasZhuangBei判断
			
			if(!hasZhuangBei){
				//没有装备，加入推荐，结束七日礼包判断循环
				String str = "七日礼包中第XX天获得".replace("XX", String.valueOf(lb.day));
				res.put(liBaoTemp.id, str);
				log.error("装备{}有更高级装备在七日礼包中，加入进阶推荐",liBaoTemp.id);
				break ;
			}
		}
		
//		c)	第3优先：当前未达满阶且不足以进阶的装备
//		i.	存在多个满足以上条件的装备时，进阶经验值距离可进阶的差值最小的装备优先推荐；
//		ii.	存在多个满足以上条件且差值相同的装备时，品质低的优先推荐；
//		iii.	存在多个满足以上条件且品质相同的装备时，
//		按照长柄武器>双持武器>远程武器>头>肩>胸>手>腿>脚的优先级推荐

		for(; res.size() < 3 ; mark ++ ){
			if(jinJieSort[mark].startsWith("99999:"))break;
			int equipId = Integer.parseInt(jinJieSort[mark].split(":")[3]);
			if(res.get( equipId ) != null )continue;
			res.put(equipId, "身上装备");
			log.error("装备{}进阶第三优先判断，加入进阶推荐",equipId);
		}
		return res ;
	}
	
}
