package com.qx.mibao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.MibaoProtos.MiBaoDealSkillReq;
import qxmobile.protobuf.MibaoProtos.MiBaoDealSkillResp;
import qxmobile.protobuf.MibaoProtos.MibaoActivate;
import qxmobile.protobuf.MibaoProtos.MibaoActivateResp;
import qxmobile.protobuf.MibaoProtos.MibaoInfo;
import qxmobile.protobuf.MibaoProtos.MibaoInfoOtherReq;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;
import qxmobile.protobuf.MibaoProtos.MibaoLevelupReq;
import qxmobile.protobuf.MibaoProtos.MibaoLevelupResp;
import qxmobile.protobuf.MibaoProtos.MibaoStarUpReq;
import qxmobile.protobuf.MibaoProtos.MibaoStarUpResp;
import qxmobile.protobuf.MibaoProtos.SkillInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChuShiNuQi;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MiBaoExtraAttribute;
import com.manu.dynasty.template.MibaoSkill;
import com.manu.dynasty.template.MibaoSkillLv;
import com.manu.dynasty.template.MibaoStar;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;


public class MibaoMgr extends EventProc{
	public static MibaoMgr inst;
	public Logger logger = LoggerFactory.getLogger(MibaoMgr.class.getSimpleName());
	
	/**
	 *	秘宝升级点数初始值 
	 */
	public static int LEVEL_POINT_INIT = 10;
	
	/**
	 * 秘宝激活初始等级
	 */
	public static int MIBAO_LEVEL_INIT = 1;

	/**
	 * key:mibaoId
	 */
	public static Map<Integer, MiBao> mibaoMap = new HashMap<Integer, MiBao>();
	public static List<MiBao> mibaoList;
	
//	/** 秘宝技能表<组合id，不同品质秘宝技能对象> **/ 
	public Map<Integer, List<MibaoSkillLv>> mibaoSkillLvMap = 
			new HashMap<Integer, List<MibaoSkillLv>>();
	public Map<Integer, MibaoSkill> mibaoSkillMap = new HashMap<Integer, MibaoSkill>();

	/** 秘宝星级表<星级id，秘宝星级对象> **/
	public Map<Integer, MibaoStar> mibaoStarMap;
	
	/** 秘宝碎片表<tempid，秘宝碎片对象> **/
	public Map<Integer, MibaoSuiPian> mibaoSuipianMap;
	
	/** 秘宝碎片表<id，秘宝碎片对象> **/
	public Map<Integer, MibaoSuiPian> mibaoSuipianMap_2;

	/**
	 * key:秘宝的zuheId，value：每个组的秘宝list
	 */
//	public Map<Integer,List<MiBao>> zuheListMap;
	
//	public static int skill_db_space = 10;

	public static int mibao_first_full_star = 1;
	//public static Map<Integer, MibaoJixing> jXMap = new HashMap<Integer, MibaoJixing>();
	
	public static Map<Integer, List<MiBaoExtraAttribute>> mibaoExtraAttrMap = 
			new HashMap<Integer, List<MiBaoExtraAttribute>>();
	public static Map<Integer, ChuShiNuQi> chuShinuQiMap = new HashMap<Integer, ChuShiNuQi>();

	public MibaoMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		mibaoList = TempletService.listAll(MiBao.class.getSimpleName());
		Map<Integer, MiBao> mibaoMapTemp = new HashMap<Integer, MiBao>();
//		Map<Integer,List<MiBao>> zuheListMap = new HashMap<Integer, List<MiBao>>(7);
		for(MiBao miBao : mibaoList) {
			mibaoMapTemp.put(miBao.id, miBao);
//			int zuheId = miBao.getZuheId();
//			List<MiBao> typeList = zuheListMap.get(zuheId);
//			if(typeList == null){
//				typeList = new ArrayList<MiBao>(5);
////				zuheListMap.put(zuheId, typeList);
//			}
//			typeList.add(miBao);
		}
		mibaoMap = mibaoMapTemp;
//		this.zuheListMap = zuheListMap;
		
		List<MibaoSkill> skillList = TempletService.listAll(MibaoSkill.class.getSimpleName());
		for(MibaoSkill mibaoSkill : skillList) {
			mibaoSkillMap.put(mibaoSkill.id, mibaoSkill);
		}

		
		List<MibaoSkillLv> skillLvList = TempletService.listAll(MibaoSkillLv.class.getSimpleName());
		for(MibaoSkillLv sl : skillLvList) {
			List<MibaoSkillLv> mbSkills = mibaoSkillLvMap.get(sl.id);
			if(mbSkills == null) {
				mbSkills = new ArrayList<MibaoSkillLv>();
				mibaoSkillLvMap.put(sl.id, mbSkills);
			}
			mbSkills.add(sl);
		}

		Map<Integer, MibaoStar> mibaoStarMap = new HashMap<Integer, MibaoStar>();
		List<MibaoStar> starList = TempletService.listAll(MibaoStar.class.getSimpleName());
		for(MibaoStar mibaoStar : starList) {
			mibaoStarMap.put(mibaoStar.star, mibaoStar);
			mibao_first_full_star = MathUtils.getMax(mibaoStar.star, mibao_first_full_star);
		}
		this.mibaoStarMap = mibaoStarMap;
		
		Map<Integer, MibaoSuiPian> mibaoSuipianMap = new HashMap<Integer, MibaoSuiPian>();
		Map<Integer, MibaoSuiPian> mibaoSuipianMap_2 = new HashMap<Integer, MibaoSuiPian>();
		List<MibaoSuiPian> suipianList = TempletService.listAll(MibaoSuiPian.class.getSimpleName());
		for(MibaoSuiPian suipian : suipianList) {
			mibaoSuipianMap.put(suipian.tempId, suipian);
			mibaoSuipianMap_2.put(suipian.id, suipian);
		}
		this.mibaoSuipianMap = mibaoSuipianMap;
		this.mibaoSuipianMap_2 = mibaoSuipianMap_2;

		List<MiBaoExtraAttribute> attrList= TempletService.listAll(
				MiBaoExtraAttribute.class.getSimpleName());
		for(MiBaoExtraAttribute attr : attrList) {
			List<MiBaoExtraAttribute> alist = mibaoExtraAttrMap.get(attr.id);
			if(alist == null) {
				alist = new ArrayList<MiBaoExtraAttribute>();
				mibaoExtraAttrMap.put(attr.id, alist);
			}
			alist.add(attr);
		}
		List<ChuShiNuQi> nul = TempletService.listAll(ChuShiNuQi.class.getSimpleName());
		for(ChuShiNuQi nu : nul) {
			chuShinuQiMap.put(nu.num, nu);
		}
//		// 添加
//		List<MibaoJixing> jlist = TempletService.listAll(MibaoJixing.class.getSimpleName());
//		MibaoJixing lastJX = null;
//		for(int index = jlist.size() - 1; index >= 0 ; index--){
//			MibaoJixing j = jlist.get(index);
//			if(lastJX != null){
//				j.nextSum = lastJX.sum;
//			}else{
//				j.nextSum = -1;
//			}
//			jXMap.put(j.sum, j);
//			lastJX = j;
//			logger.info("j.sum == {}; j.nextsum =={}, award =={}", j.sum, j.nextSum, j.award);
//		}
	}
	public void sendError(IoSession session, String msg) {
		if(session == null){
			logger.warn("session is null: {}",msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());		
	}
	/**
	 * 秘宝激活
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void mibaoActivate(int cmd, IoSession session, Builder builder) {
		MibaoActivate.Builder request = (qxmobile.protobuf.MibaoProtos.MibaoActivate.Builder) builder;
		if(request == null){
			sendError(session, "请选择秘宝");
			return;
		}
		int tempId = request.getTempId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("cmd:{},未发现君主",cmd);
			return;
		}
		MibaoSuiPian mibaoSuiPian = mibaoSuipianMap.get(tempId);
		if(mibaoSuiPian == null) {
			logger.error("cmd:{},秘宝类型错误，找不到该类型的秘宝碎片配置",cmd);
			return;
		}
		MiBaoDB miBaoDB = MiBaoDao.inst.get(junZhu.id, tempId);
		if(miBaoDB == null) {
			logger.error("cmd:{},db中未找到类型tempId:{}的秘宝数据",cmd, tempId);
			return;
		}
		if(miBaoDB.level > 0) {
			logger.error("该秘宝已经激活，dbId:{}", miBaoDB.dbId);
			return;
		}
		MiBao miBaoCfg = null;
		for(Map.Entry<Integer, MiBao> entry : mibaoMap.entrySet()) {
			if(entry.getValue().tempId == tempId) {
				miBaoCfg = entry.getValue();
				break;
			}
		}
		if(miBaoCfg == null) {
			logger.error("秘宝配置文件出错，没有找到激活秘宝。tempid:{}", tempId);
			return;
		}
		
		int star = miBaoCfg.initialStar;
		MibaoStar curStarCfg = mibaoStarMap.get(star);
		if(curStarCfg == null) {
			logger.error("cmd:{},秘宝星级表 配置错误，star:{}", cmd, miBaoCfg.initialStar);
			return;
		}
////		boolean lock = !miBaoDB.isClear();
//		if(lock){
//			if(isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, junZhu)){
//				logger.error("该秘宝未解锁，不能激活，mibaoId:{}", miBaoCfg.id);
//				return;
//			}else{
////				miBaoDB.setClear(true);
//				lock = false;
//				logger.info("秘宝：{}解锁3", miBaoDB.getMiBaoId());
//			}
//		}
		MibaoActivateResp.Builder resp = MibaoActivateResp.newBuilder();
		if(miBaoDB.suiPianNum < mibaoSuiPian.hechengNum) {
			resp.setResult(1);
			session.write(resp.build());
			logger.error("cmd:{},碎片数量不足，不能激活，tempId:{}",cmd, tempId);
			return;
		}
		if(junZhu.tongBi < mibaoSuiPian.money) {
			resp.setResult(2);
			session.write(resp.build());
			logger.error("cmd:{},铜币数量不足，不能激活，tempId:{}",cmd, tempId);
			return;
		}
		
		logger.info("junzhuId:{},成功激活秘宝tempId:{},消耗碎片{}张", junZhu.id, tempId, mibaoSuiPian.hechengNum);
		miBaoDB.miBaoId = miBaoCfg.id;
		miBaoDB.suiPianNum = miBaoDB.suiPianNum - mibaoSuiPian.hechengNum;
		miBaoDB.level = MIBAO_LEVEL_INIT;
		miBaoDB.star = star;
		HibernateUtil.save(miBaoDB);
		
		MibaoInfo.Builder mibaoInfo = MibaoInfo.newBuilder();
		fillMibaoInfoBuilder(junZhu, mibaoInfo, miBaoDB, miBaoCfg, curStarCfg, false);
		resp.setResult(0);
		resp.setMibaoInfo(mibaoInfo);
		session.write(resp.build());

		junZhu.tongBi -= mibaoSuiPian.money;
		HibernateUtil.update(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		EventMgr.addEvent(junZhu.id,ED.MIBAO_HECHENG_BROADCAST, new Object[]{junZhu,session,miBaoCfg});
		// 主线任务：秘宝合成完成一次
		EventMgr.addEvent(junZhu.id,ED.MIBAO_HECHENG, new Object[]{junZhu.id, miBaoCfg.id});
		int mibaoCount = getActivateMiBaoCount(junZhu.id);
		if(mibaoCount > 0){
			EventMgr.addEvent(junZhu.id,ED.get_x_mibao, new Object[]{junZhu.id, mibaoCount});
		}
		
		//  主线任务：秘寶星级
		EventMgr.addEvent(junZhu.id,ED.mibao_shengStar_x, new Object[]{junZhu.id, miBaoDB.star});
		//  主线任务：秘寶等级
		EventMgr.addEvent(junZhu.id,ED.mibao_shengji_x, new Object[]{junZhu.id, miBaoDB.level});
		EventMgr.addEvent(junZhu.id,ED.miabao_x_star_n, new Object[]{junZhu.id});
		// 判断是否发送是否秘宝技能可以显示
		if(mibaoCount > 0){
			isCanMiBaoJiNengOpen(junZhu, session, mibaoCount);
		}
		
	}
	
	/**
	 * 请求秘宝信息
	 * @param cmd
	 * @param session
	 */
	public void mibaoInfosRequest(int cmd, IoSession session) {
		if(session == null){
			logger.error("cmd:{},估计是gm操作，但不发送",cmd);
			return;
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("cmd:{},未发现君主",cmd);
			return;
		}
		mibaoInfoRequest(session, junZhu, PD.S_MIBAO_INFO_RESP);
	}

	public void mibaoInfoRequest(IoSession session, JunZhu junZhu, short respCmd) {
		Map<Integer, MiBaoDB> dbMap = MiBaoDao.inst.getMap(junZhu.id);
		Date date = new Date();
		MibaoLevelPoint levelPoint = HibernateUtil.find(MibaoLevelPoint.class, junZhu.id);
		if(levelPoint == null) {
			levelPoint = getDefaultMibaoLevelPoint(junZhu);
		} else {
			refreshLevelPoint(levelPoint, date, junZhu.vipLevel);
		}
		ProtobufMsg msg = new ProtobufMsg();
		MibaoInfoResp.Builder resp = MibaoInfoResp.newBuilder();
		resp.setLevelPoint(levelPoint.point);
		resp.setRemainTime(getLevelPointRemainSeconds(levelPoint, date, junZhu.vipLevel));
//		resp.setNeedAllStar(levelPoint.needAllStar);
		addMibaoInfoList(dbMap, resp, junZhu);
		addSkillInfoList(resp, junZhu.id);
		msg.builder = resp;
		msg.id = respCmd;
		session.write(resp.build());
	}
	
	public MibaoLevelPoint getDefaultMibaoLevelPoint(JunZhu jz){
		if(jz == null) return null;
		Date date = new Date();
		MibaoLevelPoint levelPoint = new MibaoLevelPoint();
		levelPoint.junzhuId = jz.id;
		levelPoint.point = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel, VipData.mibaoCountLimit);
		levelPoint.lastAddTime = date;
		levelPoint.lastBuyTime = date;
		levelPoint.dayTimes = 0;
//		levelPoint.needAllStar = mibao_first_full_star;
		HibernateUtil.save(levelPoint);
		Cache.mbLevelPointCache.put(jz.id,levelPoint);
		return levelPoint;
	}
	
	public void addSkillInfoList(MibaoInfoResp.Builder resp, long jId) {
		List<MiBaoSkillDB> list = getSkillDBList(jId);
		SkillInfo.Builder sInfo = SkillInfo.newBuilder();
		for(MiBaoSkillDB d: list){
			sInfo.setActiveZuheId(d.zuHeId);
			sInfo.setLevel(d.level);
			resp.addSkillList(sInfo);
		}
	}
	/**
	 * 请求别人秘宝信息
	 * @param cmd
	 * @param session
	 */
	public void mibaoInfosOtherRequest(int cmd, IoSession session,Builder builder) {
		MibaoInfoOtherReq.Builder request = (qxmobile.protobuf.MibaoProtos.MibaoInfoOtherReq.Builder)builder;
		long junzhuId = request.getOwnerId();
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
		if(junzhu == null) {
			logger.error("找不到君主，junzhuId:{}", junzhuId);
			return;
		}
		mibaoInfoRequest(session, junzhu, PD.S_MIBAO_INFO_OTHER_RESP);
	}

	/**
	 * 添加密保信息列表
	 * @param dbMap
	 * @param resp
	 */
	public void addMibaoInfoList(Map<Integer, MiBaoDB> dbMap,
			MibaoInfoResp.Builder resp, JunZhu junzhu) {
		if(mibaoList == null){
			logger.info("Mibao.xml的数据没有加载到 mibaoList中，请程序同学查看！");
			return;
		}
		for(MiBao mibao: mibaoList){
			if(mibao == null) {
				logger.error("找不到秘宝配置");
				continue;
			}
			MibaoInfo.Builder mibaoInfo = MibaoInfo.newBuilder();
			mibaoInfo.setMiBaoId(mibao.id);
			mibaoInfo.setTempId(mibao.tempId);
			mibaoInfo.setZhanLi(0);
			MiBaoDB miBaoDB = dbMap.get(mibao.tempId);
			if(miBaoDB == null){//秘宝不再DB中
				mibaoInfo.setDbId(-1);
				mibaoInfo.setStar(0);
				mibaoInfo.setLevel(0);
				mibaoInfo.setSuiPianNum(0);
				mibaoInfo.setNeedSuipianNum(0);
				mibaoInfo.setGongJi(0);
				mibaoInfo.setFangYu(0);
				mibaoInfo.setShengMing(0);
			}else if(miBaoDB.level <= 0){//秘宝在DB中，但未激活
				fillMibaoDBNoActivate(mibaoInfo, miBaoDB);
			} else {//秘宝在DB中，并已经激活
				MibaoStar mibaoStar = mibaoStarMap.get(miBaoDB.star);
				boolean isConfigError = false;
				if(mibaoStar == null) {
					logger.error("找不到秘宝星级配置，mibaoDbId:{},星级:{}", miBaoDB.dbId, miBaoDB.star);
					isConfigError = true;
				}
				if(isConfigError) {
					fillMibaoDBNoActivate(mibaoInfo, miBaoDB);
				} else {
					fillMibaoInfoBuilder(junzhu, mibaoInfo, miBaoDB, mibao, mibaoStar, false);
				}
			}
			resp.addMiBaoList(mibaoInfo);
		}
//				mibaoGroup.addMibaoInfo(mibaoInfo);
//			int mibaoCount = 2;
//			int zuheId = entry.getKey();
//			mibaoGroup.setZuheId(zuheId);
//			MiBaoSkillDB skillD = HibernateUtil.find(MiBaoSkillDB.class, junzhu.id * skill_db_space + zuheId);
//			if(skillD == null){
//				mibaoGroup.setHasActive(0);
//				mibaoGroup.setHasJinjie(0);
//			}else{
//				mibaoGroup.setHasActive(skillD.hasClear? 1:0);
//				mibaoGroup.setHasJinjie(skillD.hasJinjie? 1:0);
//				// 只有手动进阶之后，才显示3阶技能
//				if(skillD.hasJinjie){
//					mibaoCount = 3;
//				}
//			}
//			int skillId = getShowSkills(zuheId, mibaoCount);
//			mibaoGroup.setSkillId(skillId);
			
	}

	public void fillMibaoDBNoActivate(MibaoInfo.Builder mibaoInfo, MiBaoDB miBaoDB) {
		mibaoInfo.setDbId(miBaoDB.dbId);
		mibaoInfo.setStar(0);
		mibaoInfo.setLevel(0);
		mibaoInfo.setSuiPianNum(miBaoDB.suiPianNum);
		mibaoInfo.setNeedSuipianNum(0);
		mibaoInfo.setGongJi(0);
		mibaoInfo.setFangYu(0);
		mibaoInfo.setShengMing(0);
	}
	
	public void fillMibaoInfoBuilder(JunZhu junzhu, MibaoInfo.Builder mibaoInfo,
			MiBaoDB miBaoDB, MiBao miBaoCfg, MibaoStar starCfg, boolean lock) {
		float chengZhang = starCfg.chengzhang;
		int level = miBaoDB.level;
		int needSuipianNum = starCfg.needNum;
		int starMax = mibaoStarMap.size();
		if(miBaoDB.star >= starMax) {
			MibaoStar lastSub1 = mibaoStarMap.get(starMax - 1);
			needSuipianNum = lastSub1.needNum;
		}
		mibaoInfo.setDbId(miBaoDB.dbId);
		mibaoInfo.setTempId(miBaoCfg.tempId);
		mibaoInfo.setMiBaoId(miBaoDB.miBaoId);
		mibaoInfo.setStar(miBaoDB.star);
		mibaoInfo.setLevel(level);
//		mibaoInfo.setIsLock(lock);
		mibaoInfo.setSuiPianNum(miBaoDB.suiPianNum);
		mibaoInfo.setNeedSuipianNum(needSuipianNum);
		int gongJi = clacMibaoAttr(chengZhang, starCfg.gongji, miBaoCfg.gongjiRate, level);
		int fangYu = clacMibaoAttr(chengZhang, starCfg.fangyu, miBaoCfg.fangyuRate, level);
		int shengMing = clacMibaoAttr(chengZhang, starCfg.shengming, miBaoCfg.shengmingRate, level);
		mibaoInfo.setGongJi(gongJi);
		mibaoInfo.setFangYu(fangYu);
		mibaoInfo.setShengMing(shengMing);
		int zhanLi = JunZhuMgr.inst.calcMibaoZhanLi(gongJi, fangYu, shengMing, 
				miBaoDB.miBaoId, miBaoDB.level);
		mibaoInfo.setZhanLi(zhanLi);
	}
	
	/**
	 * 计算秘宝攻击、防御、生命的属性值，根据公式计算
	 * @param chengZhang	秘宝属性成长值
	 * @param attrValue		秘宝某项属性值
	 * @param attrRate		秘宝对应属性的系数
	 * @param level			秘宝当前等级
	 * @return
	 */
	public int clacMibaoAttr(float chengZhang, double attrValue, double attrRate, int level) {
		return (int)(chengZhang * attrRate * level + attrValue);
	}
	
	public void starUpgrade(int cmd, IoSession session, Builder builder) {
		MibaoStarUpReq.Builder request = (qxmobile.protobuf.MibaoProtos.MibaoStarUpReq.Builder) builder;
		int mibaoId = request.getMibaoId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		MiBaoDB miBaoDB = MiBaoDao.inst.getByMiBaoId(junZhu.id, mibaoId);
		if(miBaoDB == null) {
			logger.error("cmd:{},db中找不到对应的秘宝记录，ownerId:{},mibaoId:{}", cmd, junZhu.id, mibaoId);
			return;
		}
		MibaoStar curStarCfg = mibaoStarMap.get(miBaoDB.star);
		MibaoStar nextStarCfg = mibaoStarMap.get(miBaoDB.star + 1);
		if(nextStarCfg == null) {
			logger.error("cmd:{}, 秘宝星级已满级，不能进行升星操作，mibaoId:{}", cmd, mibaoId);
			return;
		}
		MiBao miBaoCfg = mibaoMap.get(miBaoDB.miBaoId);
		if(miBaoCfg == null) {
			logger.error("找不到秘宝配置信息，mibaoId:{}", miBaoDB.miBaoId);
			return;
		}
//		boolean lock = !miBaoDB.isClear();
//		if(lock){
//			if(isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, junZhu)){
//				logger.error("该秘宝未解锁，不能升级星级，mibaoId:{}", miBaoCfg.id);
//				return;
//			}else{
//				miBaoDB.setClear(true);
//				lock = false;
//				logger.info("秘宝：{}解锁1", miBaoDB.getMiBaoId());
//			}
//		}
//		
		if(miBaoDB.suiPianNum < curStarCfg.needNum) {
			logger.error("cmd:{}, 碎片数量不足，不能进行升星操作，mibaoId:{}", cmd, mibaoId);
			return;
		}
		if(junZhu.tongBi < curStarCfg.needMoney){
			logger.error("cmd:{}, 铜币数量不足，不能进行升星操作，mibaoId:{}", cmd, mibaoId);
			return;
		}
		logger.info("junzhuId:{},秘宝升星成功。mibaoId:{},消耗碎片:{},消耗铜币:{}", 
				junZhu.id, mibaoId, curStarCfg.needNum, curStarCfg.needMoney);
		miBaoDB.star = miBaoDB.star + 1;
		miBaoDB.suiPianNum = miBaoDB.suiPianNum - curStarCfg.needNum;
		miBaoDB.hasShengXing = true;
		HibernateUtil.save(miBaoDB);
		EventMgr.addEvent(junZhu.id,ED.MIBAO_UP_STAR, new Object[]{junZhu,session,miBaoCfg,curStarCfg});
		
		MibaoStarUpResp.Builder resp = MibaoStarUpResp.newBuilder();
		
		MibaoInfo.Builder mibaoInfo = MibaoInfo.newBuilder();
		fillMibaoInfoBuilder(junZhu, mibaoInfo, miBaoDB, miBaoCfg, nextStarCfg, false);
		resp.setMibaoInfo(mibaoInfo);
		session.write(resp.build());
		junZhu.tongBi = junZhu.tongBi - curStarCfg.needMoney;
		HibernateUtil.update(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		
		// 主线任务：秘宝升级星级完成一次
//		if(miBao.getId() == TaskData.shengXing_mibao){
			EventMgr.addEvent(junZhu.id,ED.MIBAO_SEHNGXING, new Object[]{junZhu.id, miBaoCfg.id});
//		}
		// 主线任务：秘宝升级星级到x星级
//		if(TaskData.mibao_star_x_1 == miBaoDB.getStar() ||
//				TaskData.mibao_star_x_2 == miBaoDB.getStar()){
			EventMgr.addEvent(junZhu.id,ED.mibao_shengStar_x, new Object[]{junZhu.id, miBaoDB.star});
		/*
		 * 对指定秘宝升星一次
		 */
			EventMgr.addEvent(junZhu.id,ED.mibao_shengStar, new Object[]{junZhu.id, miBaoDB.miBaoId});
			//		}
		// 秘宝升星	
		EventMgr.addEvent(junZhu.id,ED.miabao_x_star_n, new Object[]{junZhu.id});
	}

	public void levelUpgrade(int cmd, IoSession session, Builder builder) {
		MibaoLevelupReq.Builder request = (qxmobile.protobuf.MibaoProtos.MibaoLevelupReq.Builder) builder;
		int mibaoId = request.getMibaoId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if(junZhu == null) {
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		MiBaoDB miBaoDB = MiBaoDao.inst.getByMiBaoId(junZhu.id, mibaoId);
		if(miBaoDB == null) {
			logger.error("cmd:{},db中找不到对应的秘宝记录，ownerId:{},mibaoId:{}", cmd, junZhu.id, mibaoId);
			return;
		}
		if(miBaoDB.level == junZhu.level) {
			logger.error("秘宝:{}等级:{}不能超过君主等级:{}", miBaoDB.miBaoId, miBaoDB.level, junZhu.level);
			return;
		}
		MiBao miBaoCfg = mibaoMap.get(miBaoDB.miBaoId);
		if(miBaoCfg == null) {
			logger.error("找不到秘宝id为:{}的配置", miBaoDB.miBaoId);
			return;
		}
		ExpTemp expTemp = TempletService.templetService.getExpTemp(miBaoCfg.expId, miBaoDB.level);
		if(expTemp == null){
			logger.error("cmd:{},经验配置错误，expId:{},level:{}", cmd, miBaoCfg.expId, miBaoDB.level);
			return;
		}
		MibaoStar mibaoStar = mibaoStarMap.get(miBaoDB.star);
		if(mibaoStar == null) {
			logger.error("找不到mibaostar配置，star:{}, 秘宝id:{}", miBaoDB.star, mibaoId);
			return;
		}
//		boolean lock = !miBaoDB.isClear();
//		if(lock){
//			if(isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, junZhu)){
//				logger.error("该秘宝未解锁，不能升级，mibaoId:{}", miBaoCfg.id);
//				return;
//			}else{
//				miBaoDB.setClear(true);
//				lock = false;
//				logger.info("秘宝：{}解锁2", miBaoDB.getMiBaoId());
//			}
//		}

		if(junZhu.tongBi < expTemp.needExp) {
			logger.error("cmd:{},铜币不足不能进行升级操作expId:{},秘宝等级:{}", cmd, miBaoCfg.expId, miBaoDB.level);
			return;
		}
		// 升级点数初始化肯定在升级之前
		MibaoLevelPoint levelPoint = HibernateUtil.find(MibaoLevelPoint.class, junZhu.id);
		if(levelPoint == null || levelPoint.point <= 0) {
			logger.error("cmd:{},没有升级点数不能进行升级操作 ,秘宝等级:{}", cmd, miBaoDB.level);
			return;
		}
		
		List<ExpTemp> expTemps = TempletService.templetService.getExpTemps(miBaoCfg.expId);
		int maxLevel = 1;
		if(expTemps == null || expTemps.size() == 0) {
			logger.error("cmd:{},经验配置为空，expId:{}", cmd, miBaoCfg.expId);
			return;
		} 
		maxLevel = expTemps.size();
		if(miBaoDB.level >= maxLevel) {//表示满级了
			miBaoDB.level = maxLevel;
			HibernateUtil.save(miBaoDB);
			return;
		} else {
			int newLevel = miBaoDB.level + 1;
			miBaoDB.level = newLevel;
			HibernateUtil.save(miBaoDB);
			
			int maxPointValue = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel, VipData.mibaoCountLimit);
			if(levelPoint.point >= maxPointValue) {
				levelPoint.lastAddTime = new Date();
			}
			levelPoint.point -= 1;
			levelPoint.point = Math.max(levelPoint.point, 0);
			HibernateUtil.save(levelPoint);
			
			// 主线任务：秘宝升级完成一次
	//		if(conf.getId() == TaskData.shengJi_mibao){
	//			GameTaskMgr.inst.recordTaskProcess(junZhu.id, TaskData.MIBAO_SHENGJI, miBaoCfg.getId()+"");
	//		}
				// 主线任务：任意秘宝升级到x等级
	//		if(newLevel == TaskData.mibao_level_x_1 ||
	//					newLevel == TaskData.mibao_level_x_2){
				EventMgr.addEvent(junZhu.id,ED.mibao_shengji_x, new Object[]{junZhu.id, newLevel});
	//		}
		}
		MibaoLevelupResp.Builder resp = MibaoLevelupResp.newBuilder();
		MibaoInfo.Builder mibaoInfo = MibaoInfo.newBuilder();
		fillMibaoInfoBuilder(junZhu, mibaoInfo, miBaoDB, miBaoCfg, mibaoStar, false);
		resp.setMibaoInfo(mibaoInfo);
		Date date = new Date();
		refreshLevelPoint(levelPoint, date, junZhu.vipLevel);
		resp.setRemainTime(getLevelPointRemainSeconds(levelPoint, date, junZhu.vipLevel));
		session.write(resp.build());
		junZhu.tongBi = junZhu.tongBi - expTemp.needExp;
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		logger.info("junzhuId:{},秘宝升级成功。mibaoId:{},消耗铜币:{}", junZhu.id, mibaoId, expTemp.needExp);
		// 每日任务中记录完成秘宝升级1次
		EventMgr.addEvent(junZhu.id,ED.DAILY_TASK_PROCESS, new DailyTaskCondition(junZhu.id, DailyTaskConstants.mibao_shengji_id, 1));
	}
	
	/**
	 * 秘宝属性额外加成，当mibaoLevel >= needlv,有额外属性加成
	 * @param mibaoid
	 * @param mibaoLevel
	 * @return
	 */
	public List<MiBaoExtraAttribute> getAddAttrList(int mibaoid, int mibaoLevel){
		List<MiBaoExtraAttribute> list =  mibaoExtraAttrMap.get(mibaoid);
		List<MiBaoExtraAttribute> addList = new ArrayList<MiBaoExtraAttribute>();
		if(list != null){
			for(MiBaoExtraAttribute m: list){
				if(mibaoLevel >= m.needlv){
					addList.add(m);
				}
			}
		}
		return addList;
	}
	
//	public int getMibaoZuheSkillId(List<Integer> list) {
//		int zuheSkillId = -1;
//		int zuheId = -1;
//		int minPinZhi = 1;
//		if(list == null || list.size() != 3) {
//			return zuheSkillId;
//		}
//		MiBao miBao = mibaoMap.get(list.get(0));
//		if(miBao == null) {
//			return zuheSkillId;
//		}
//		zuheId = miBao.getZuheId();
//		minPinZhi = miBao.getPinzhi();
//		
//		for(Integer id : list) {
//			MiBao mb = mibaoMap.get(id);
//			if(mb == null) {
//				return zuheSkillId;
//			}
//			if(mb.getZuheId() == zuheId) {
//				minPinZhi = Math.min(minPinZhi, mb.getPinzhi());
//				continue;
//			} else {
//				return zuheSkillId;
//			}
//		}
////		List<MibaoSkill> skills = mibaoSkillMap.get(zuheId);
////		for(MibaoSkill skill : skills) {
////			if(skill.pinzhi == minPinZhi) {
////				zuheSkillId = skill.skill;
////				break;
////			}
////		}
//		return zuheSkillId;
//	}
	

	
//	public int getShowSkills(int zuheId, int mibaoCount) {
//		if(mibaoCount < 2) {
//			mibaoCount = 2; // 如果没有激活秘宝技能，则秘宝icon显示激活两个秘宝时的技能
//		}
//		List<MibaoSkill> skillList = TempletService.listAll(MibaoSkill.class.getSimpleName());
//		int skillId = 0;
//		for(MibaoSkill skill : skillList) {
//			if(skill.zuhe == zuheId && skill.pinzhi == mibaoCount) {
//				skillId = skill.skill;
//				break;
//			}
//		}
//		return skillId;
//	}

	public List<Integer> getSkillIdsFromConfig(int zuHeId, int zuHeLevel) {
		List<Integer> retList = new ArrayList<Integer>();
		if(zuHeId <= 0) {
			return retList;
		}
		MibaoSkillLv skillCfg = null;
		List<MibaoSkillLv> list = mibaoSkillLvMap.get(zuHeId);
		for(MibaoSkillLv sl: list){
			if(sl.lv == zuHeLevel){
				skillCfg = sl;
				break;
			}
		}
		if(skillCfg == null){
			return retList;
		}
		String[] showSkills = skillCfg.skill.split(",");
		for(String skillId : showSkills) {
			retList.add(Integer.parseInt(skillId));
		}
		
		String skill2 = skillCfg.skill2;
		if(skill2 == null || skill2.equals("")) {
			return retList;
		} 
		String[] skills = skill2.split(",");
		for(String s : skills) {
			retList.add(Integer.parseInt(s));
		}
		return retList;
	}

	public List<Integer> getBattleJunZhuSkillIds(int zuheId, long jId) {
		List<Integer> retList = new ArrayList<Integer>();
		if(zuheId <= 0) {
			return retList;
		}
		MiBaoSkillDB skillD = getActiveSkillFromDB(jId, zuheId);
		// 没有手动激活
		if(skillD == null){
			return retList;
		}
		return getSkillIdsFromConfig(zuheId, skillD.level);
	}

	/*
	 * // 是否有激活秘宝技能
	 */
	public MiBaoSkillDB getActiveSkillFromDB(long jId, int zuheId){
		if(zuheId <= 0){
			return null;
		}
		/*MiBaoSkillDB skillD = HibernateUtil.find(MiBaoSkillDB.class,
				" where jId = " + jId +" and zuHeId = " + zuheId);*/
		MiBaoSkillDB skillD = MiBaoSkillDao.inst.getMibaoSkillDBySkillId(jId, zuheId);
		return skillD;
	}

	public int getActiveZuHeLevel(long jId, int zuheId){
		if(zuheId <= 0){
			return -1;
		}
		/*MiBaoSkillDB skillD = HibernateUtil.find(MiBaoSkillDB.class,
				" where jId = " + jId +" and zuHeId = " + zuheId);*/
		MiBaoSkillDB skillD = getActiveSkillFromDB(jId, zuheId);
		// 没有手动激活
		return skillD == null? -1: skillD.level;
	}

//	public MibaoSkill getMibaoSkillConfig(int zuheId) {
//		List<MibaoSkill> skillList = mibaoSkillMap.get(zuheId);
//		if(skillList == null || skillList.size() == 0) {
//			logger.error("找不到zuheId:{}的秘宝技能配置");
//			return null;
//		}
//		MibaoSkill mibaoSkill = null;
//		for(MibaoSkill skillCfg : skillList) {
//			if(skillCfg.pinzhi == activateCount) {
//				mibaoSkill = skillCfg;
//				break;
//			}
//		}
//		return mibaoSkill;
//	}
	public int getChuShiNuQi(long junzhuId){
		int size = getActivateMiBaoCount(junzhuId);
		if(size == 0) return 0;
		ChuShiNuQi c = chuShinuQiMap.get(size);
		if(c == null) return 0;
		logger.info("君主进入战斗，秘宝怒气是：{}", junzhuId, c.nuqiValue);
		return c.nuqiValue;
	}
	public int getShowChuShiNuQi(long junzhuId){
		int size = getActivateMiBaoCount(junzhuId);
		if(size == 0) return 0;
		ChuShiNuQi c = chuShinuQiMap.get(size);
		if(c == null) return 0;
		return c.nuqiRatioc;
	}
	public List<MiBaoDB> getActiveMibaosFromDB(long junzhuId) {
		List<MiBaoDB> mibaoDBList = MiBaoDao.inst.getMap(junzhuId).values()
			.stream().filter(t->t.level>0 && t.miBaoId>0)
			.collect(Collectors.toList());
		/*
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB0.class,
				"where ownerId = " + junzhuId + " and level > 0 and miBaoId > 0");
				*/
		return mibaoDBList;
	}
	public boolean isMibaoLevelOk(long junzhuId, int x_number, int l_level){
		Map<Integer, MiBaoDB> map = MiBaoDao.inst.getMap(junzhuId);
		long cnt = 0;
		synchronized (map) {
			cnt = map.values()
					.stream().filter(t->t.level>=l_level && t.miBaoId>0)
					.count();
		}
		/*
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB0.class,
				"where ownerId = " + junzhuId 
				+ " and level >= " + l_level +  " and miBaoId > 0");
				*/
		return cnt >= x_number;
	}
	public int getMaxMibaoLevel(long junzhuId){
		List<MiBaoDB> mibaoDBList=getActiveMibaosFromDB(junzhuId);
		int maxLevel=0;
		for (MiBaoDB miBaoDB : mibaoDBList) {
			if(miBaoDB.level>maxLevel){
				maxLevel=miBaoDB.level;
			}
		}
		return maxLevel;
	}
	
	public boolean isMibaoStarOk(long junzhuId, int x_number, int star){
		long cnt = getMibaoStarNum(junzhuId, x_number, star);
		/*
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB0.class,
				"where ownerId = " + junzhuId 
				+ " and star >= " + star +  " and miBaoId > 0");
				*/
		return cnt >= x_number;
	}
	
	//有几个
	public long getMibaoStarNum(long junzhuId, int x_number, int star){
		Map<Integer, MiBaoDB> map = MiBaoDao.inst.getMap(junzhuId);
		long cnt = 0;
		synchronized (map) {
				cnt=map.values()
				.stream().filter(t->t.star>=star && t.miBaoId>0)
				.count();
		}
		/*
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB0.class,
				"where ownerId = " + junzhuId 
				+ " and star >= " + star +  " and miBaoId > 0");
				*/
		return cnt;
	}
	
	public boolean isMibaoCountOk(long junzhuId, int x_number){
		Map<Integer, MiBaoDB> map = MiBaoDao.inst.getMap(junzhuId);
		long cnt = 0;
		synchronized (map) {
		cnt = map.values()
				.stream().filter(t->t.miBaoId>0)
				.count();
		}
		/*
		List<MiBaoDB> mibaoDBList = HibernateUtil.list(MiBaoDB0.class,
				"where ownerId = " + junzhuId +  " and miBaoId > 0");
				*/
		return cnt >= x_number;
	}
	
	public int getActivateMiBaoCount(long junzhuId) {
		long cnt = MiBaoDao.inst.getMap(junzhuId).values()
				.stream().filter(t->t.level>0 && t.miBaoId>0)
				.count();
//		int cnt = HibernateUtil.getCount(
//				"SELECT count(*) FROM MiBaoDB WHERE ownerId = " + junzhuId 
//				+ " AND level > 0 AND miBaoId > 0");
		return (int)cnt;
//		List<MiBaoDB> mibaoDBList = getActiveMibaosFromDB(junzhuId);
//		return mibaoDBList.size();
	}
	
//	/**
//	 * 是否锁定
//	 * @param unlockType
//	 * @param unlockValue
//	 * @param junzhu
//	 * @return true-锁定，false-未锁定
//	 */
//	public boolean isLock(int unlockType, int unlockValue, JunZhu junzhu) {
//		boolean lock = true;
//		switch(unlockType) {
//			case 0:	// 为0-默认开启
//				lock = false;
//				break;
//			case 1:	// 君主等级 
//				if(junzhu.level >= unlockValue) {
//					lock = false;
//				}
//				break;
//			case 2:	// 过关斩将关卡
//				PveRecord record = HibernateUtil.find(PveRecord0.class, " where uid=" + junzhu.id + " and guanQiaId = "+ unlockValue);
//				if(record != null) {
//					lock = false;
//				}
//				break;
//			case 3: // 传奇关卡
//				PveRecord cqRecord = HibernateUtil.find(PveRecord0.class, " where uid=" + junzhu.id + " and guanQiaId = "+ unlockValue);
//				if(cqRecord != null && cqRecord.chuanQiPass) {
//					lock = false;
//				}
//				break;
//			case 4:	// 主线任务
//				int orderIndex = FunctionOpenMgr.inst.getMaxRenWuOrderIdx(junzhu.id);
//				ZhuXian zhuXianTask = GameTaskMgr.inst.zhuxianTaskMap.get(unlockValue);
//				if(zhuXianTask == null) {
//					logger.error("找不到主线任务配置taskId:{}", unlockValue);
//					break;
//				}
//				if(orderIndex >= zhuXianTask.orderIdx) {
//					lock = false;
//				}
//				break;
//			default:
//				logger.error("没有对应的解锁条件，unlockType:{}", unlockType);
//				break;
//		}
//		return lock;
//	}
//	
	public void refreshLevelPoint(MibaoLevelPoint levelPoint, Date curDate, int vipLevel) {
		long last = levelPoint.lastAddTime.getTime();
		long now = curDate.getTime();
		long time = now - last;
		int interval = CanShu.ADD_MIBAODIANSHU_INTERVAL_TIME;
		int addValue = (int) (time / (interval * 1000));
		int maxValue = VipMgr.INSTANCE.getValueByVipLevel(vipLevel, VipData.mibaoCountLimit);
		if(levelPoint.point >= maxValue) {
			return;
		}
		if(addValue <= 0){
			return;
		}
		levelPoint.point += addValue;
		levelPoint.point = Math.min(maxValue, levelPoint.point);
		logger.info("本次增加秘宝升级点数:{}, lastTime:{}, curTime{}", addValue, levelPoint.lastAddTime, curDate);
		Date curAddTime = new Date(addValue * interval * 1000 + last);
		levelPoint.lastAddTime = curAddTime;
		HibernateUtil.save(levelPoint);
	}
	
	public int getLevelPointRemainSeconds(MibaoLevelPoint levelPoint, Date curDate, int vipLevel) {
		Date lastDate = levelPoint.lastAddTime;
		int maxValue = VipMgr.INSTANCE.getValueByVipLevel(vipLevel, VipData.mibaoCountLimit);
		if(levelPoint.point >= maxValue) {
			logger.info("当前秘宝升级点数已满vipLevel:{}, point:{}", vipLevel, levelPoint.point);
			return -1;
		}
		return (int) (CanShu.ADD_MIBAODIANSHU_INTERVAL_TIME - (curDate.getTime() - lastDate.getTime()) / 1000);
	}

//	public boolean isClear(int mibaoId, JunZhu jz){
//		MiBao miBaoCfg = mibaoMap.get(mibaoId);
//		if(miBaoCfg == null){
//			return false;
//		}
//		if(!isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, jz)){
//			return false;
//		}
//		return true;
//	}

////////20150914： 手动激活秘宝技能，手动进阶秘宝技能////////////////////

	public void doMiBaoDealSkillReq(IoSession session, Builder builder){
		Long junzhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junzhuId == null) {
			logger.error("手动激活或者进阶秘宝技能失败：session无法获取君主id");
			return;
		}
		MiBaoDealSkillReq.Builder req = (MiBaoDealSkillReq.Builder)builder;
		int zuheid = req.getZuheId();
//		int type = req.getActiveOrJinjie();
		MibaoSkill skilConf = mibaoSkillMap.get(zuheid);
		if(skilConf == null){
			logger.error("找不到秘宝配置MibaoSkill.xml信息，id:{}", zuheid);
			return;
		}
		MiBaoDealSkillResp.Builder resp = MiBaoDealSkillResp.newBuilder();
		int count = getActivateMiBaoCount(junzhuId);
		if(count < skilConf.needNum){
			resp.setMessage(-1); //拥有秘宝数不对
			logger.error("君主：{}手动激活秘宝技能失败，完整秘宝不足，组合id是：{}", junzhuId, zuheid);
			session.write(resp.build());
			return;
		}
		List<MiBaoSkillDB> list = getSkillDBList(junzhuId);
		for(MiBaoSkillDB d: list){
			if(d.zuHeId == zuheid){
				resp.setMessage(-2); //已经激活过了
				logger.error("君主：{}手动激活秘宝技能失败，已经激活，组合id是：{}", junzhuId, zuheid);
				session.write(resp.build());
				return;
			}
		}
	
		//激活
		MiBaoSkillDB skillD = new MiBaoSkillDB();
		skillD.jId = junzhuId;
		skillD.zuHeId = zuheid;
		skillD.level = skilConf.lv; //技能初始等级
		MiBaoSkillDao.inst.save(skillD);
		HibernateUtil.save(skillD);
		// 其他技能等级自动加1 ----(from 策划要求)
		for(MiBaoSkillDB lastskill: list){
			int lastZuhe = lastskill.zuHeId;
			if(lastZuhe == zuheid){
				continue;
			}
			// 判断是否是最大等级
			int lv = getMaxLvByZuHeId(lastZuhe);
			if(lastskill.level < lv){
				lastskill.level += 1;
				HibernateUtil.save(lastskill);
				logger.info("君主：{}, mibaoskill的id:{}秘宝技能升级，目前等级是：{}",
						junzhuId, lastZuhe, lastskill.level);
			}else{
				logger.info("君主 ：{} ，mibaoskill,id:{}技能等级已经达到最大值：{}",
						junzhuId, lastZuhe);
			}
		}

		logger.info("君主：{}手动激活秘宝技能成功，MibaoSkill,id是：{}", junzhuId, zuheid);
		// 主线任务: 激活1次秘宝技能 20190916
		EventMgr.addEvent(junzhuId,ED.active_mibao_skill , new Object[] {junzhuId});

		resp.setMessage(0);
		session.write(resp.build());
	}
	
	public int getMaxLvByZuHeId(int zuHeId){
		List<MibaoSkillLv> list = mibaoSkillLvMap.get(zuHeId);
		int lv = 0;
		for(MibaoSkillLv sl: list){
			lv = MathUtils.getMax(lv, sl.lv);
		}
		return lv;
	}
	
	
	
	
	
//		if(skillD == null){
//			skillD = new MiBaoSkillDB();
//			skillD.id = junzhuId * skill_db_space + zuheid;
//		}
//		int count = getActivateCountByZuheId(junzhuId, zuheid);
//		
//		if(count >= 2 && !skillD.hasClear){
//			skillD.hasClear = true;
//			HibernateUtil.save(skillD);
//			// //10-激活失败，11-激活成功；20-进阶失败，21-进阶成功
//			resp.setMessage(11);
////			logger.info("君主：{}手动激活秘宝技能成功，组合id是：{}", junzhuId, zuheid);
//			// 主线任务: 激活1次秘宝技能 20190916
//			EventMgr.addEvent(ED.active_mibao_skill , new Object[] {junzhuId});
//		}else{
//			resp.setMessage(10);
//			logger.error("君主：{}手动激活秘宝技能失败，组合id是：{}", junzhuId, zuheid);
//		MiBaoDealSkillResp.Builder resp = MiBaoDealSkillResp.newBuilder();
//		MiBaoSkillDB skillD = HibernateUtil.find(MiBaoSkillDB.class, junzhuId * skill_db_space + zuheid);
//		if(skillD == null){
//			skillD = new MiBaoSkillDB();
//			skillD.id = junzhuId * skill_db_space + zuheid;
//		}
		
//		// type： 1激活请求，2 进阶请求
//		switch(type){
//			case 1:
			/*if(count >= 2 && !skillD.hasClear){
				skillD.hasClear = true;
				HibernateUtil.save(skillD);
				// //10-激活失败，11-激活成功；20-进阶失败，21-进阶成功
				resp.setMessage(11);
				logger.info("君主：{}手动激活秘宝技能成功，组合id是：{}", junzhuId, zuheid);
				// 主线任务: 激活1次秘宝技能 20190916
				EventMgr.addEvent(ED.active_mibao_skill , new Object[] {junzhuId});
			}else{
				resp.setMessage(10);
				logger.error("君主：{}手动激活秘宝技能失败，组合id是：{}", junzhuId, zuheid);
			}*/
//				break;
//			case 2:
//				if(count >= 3 && !skillD.hasJinjie){
//					// 可以进阶
//					skillD.hasJinjie = true;
//					HibernateUtil.save(skillD);
//					resp.setMessage(21);
//					int skillId = getShowSkills(zuheid, 3);
//					resp.setSkillId(skillId);
//					logger.info("君主：{}手动进阶秘宝技能成功，组合id是：{}", junzhuId, zuheid);
//				}else{
//					resp.setMessage(20);
//					logger.error("君主：{}手动进阶秘宝技能失败，组合id是：{}", junzhuId, zuheid);
//				}
//				break;
//		}
//		session.write(resp.build());

	public void isCanMiBaoShengJi(JunZhu junZhu, IoSession session){
		List<MiBaoDB> list= getActiveMibaosFromDB(junZhu.id);
		for(MiBaoDB miBaoDB: list){
			int level = miBaoDB.level;
			if(level <= 0 || level >= junZhu.level) {
				continue;
			}
			MiBao miBaoCfg = mibaoMap.get(miBaoDB.miBaoId);
			if(miBaoCfg == null) {
				continue;
			}
//			boolean lock = !miBaoDB.isClear();
//			if(lock){
//				if(isLock(miBaoCfg.unlockType, miBaoCfg.unlockValue, junZhu)){
//					continue;
//				}
//			}
			ExpTemp expTemp = TempletService.templetService.getExpTemp(miBaoCfg.expId, level);
			if(expTemp == null || junZhu.tongBi < expTemp.needExp) {
				continue;
			}
			// 升级点数初始化肯定在升级之前
			MibaoLevelPoint levelPoint = HibernateUtil.find(MibaoLevelPoint.class, junZhu.id);
			if(levelPoint == null || levelPoint.point <= 0) {
				continue;
			}
			List<ExpTemp> expTemps = TempletService.templetService.getExpTemps(miBaoCfg.expId);
			int maxLevel = 1;
			if(expTemps == null || expTemps.size() == 0) {
				continue;
			} 
			maxLevel = expTemps.size();
			if(level >= maxLevel) {//表示满级了
				continue;
			}
			// 可以升级
			FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.miBaoShengJi);
			break;
		}
	}

	public void isCanMiBaoJiNengOpen(JunZhu junZhu, IoSession session, int count){
		if(junZhu == null || session == null){
			return;
		}
		if(count <= 0){
			return;
		}
		/*List<MiBaoSkillDB> dblist = getSkillDBList(junZhu.id);
		Map<Integer,  MiBaoSkillDB>  dbmap = new HashMap<Integer, MiBaoSkillDB>();
		for(MiBaoSkillDB d: dblist){
			dbmap.put(d.zuHeId, d);
		}*/
		Map<Integer,  MiBaoSkillDB>  dbmap = MiBaoSkillDao.inst.getMap(junZhu.id);
		for(Map.Entry<Integer, MibaoSkill> e: mibaoSkillMap.entrySet()){
			MibaoSkill confSkill = e.getValue();
			if(count >= confSkill.needNum){
				// 如果秘宝技能不在数据库，则可以开启技能
				if(dbmap.get(confSkill.id) == null){
					FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.miBaoJiNeng);
					logger.info("向君主{}推送--秘宝技能激活的红点提示", junZhu.id);
					break;
				}
			}
		}
	}

	public void isMiBaoCanUpStar(JunZhu junZhu, IoSession session){
		List<MiBaoDB> list= getActiveMibaosFromDB(junZhu.id);
		for(MiBaoDB miBaoDB: list){
			MibaoStar curStarCfg = mibaoStarMap.get(miBaoDB.star);
			if(curStarCfg == null){
				continue;
			}
			MibaoStar nextStarCfg = mibaoStarMap.get(miBaoDB.star + 1);
			if(nextStarCfg == null) {
				continue;
			}
			MiBao miBaoCfg = mibaoMap.get(miBaoDB.miBaoId);
			if(miBaoCfg == null) {
				continue;
			}
			if(miBaoDB.suiPianNum < curStarCfg.needNum) {
				continue;
			}
			if(junZhu.tongBi < curStarCfg.needMoney){
				continue;
			}
			FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.MiBaoShengXing);
			break;
		}
	}
	
	public void isMiBaoCanHeCheng(JunZhu junZhu, IoSession session){
		Map<Integer, MiBaoDB> mibaoDBList = MiBaoDao.inst.getMap(junZhu.id);
		for(MiBaoDB miBaoDB: mibaoDBList.values()){
			if(miBaoDB.level > 0) {
				continue;
			}
			if(miBaoDB.miBaoId > 0){
				continue;
			}
			MibaoSuiPian mibaoSuiPian = mibaoSuipianMap.get(miBaoDB.tempId);
			if(mibaoSuiPian == null) {
				continue;
			}
			
			MiBao miBaoCfg = null;
			for(Map.Entry<Integer, MiBao> entry : mibaoMap.entrySet()) {
				if(entry.getValue().tempId == miBaoDB.tempId) {
					miBaoCfg = entry.getValue();
					break;
				}
			}
			if(miBaoCfg == null) {
				continue;
			}
			int star = miBaoCfg.initialStar;
			MibaoStar curStarCfg = mibaoStarMap.get(star);
			if(curStarCfg == null) {
				continue;
			}
			if(miBaoDB.suiPianNum < mibaoSuiPian.hechengNum) {
				continue;
			}
			if(junZhu.tongBi < mibaoSuiPian.money) {
				continue;
			}
			FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.MiBaoHeCheng);
			break;
		}
	}
//	/**
//	 * 集齐星星数，宝箱领奖
//	 * @param session
//	 * @param cmd
//	 */
//	public void getAwardWhenFullStar(IoSession session, int cmd){
//		Long jid = (Long) session.getAttribute(SessionAttKey.junZhuId);
//		if (jid == null) {
//			logger.error("秘宝集齐星星领取宝箱奖励失败：session无法获取君主id");
//			return;
//		}
//		JunZhu jz = HibernateUtil.find(JunZhu.class, jid);
//		if(jz == null){
//			logger.error("秘宝集齐星星领取宝箱奖励失败, junzhu不存在：jid:{}", jid);
//			return;
//		}
//		List<MiBaoDB> mibaoDBList = getMibaoDBList(jid);
//		MibaoLevelPoint p = HibernateUtil.find(MibaoLevelPoint.class, jid);
//		boolean yes = isFull(mibaoDBList, p);
//		GetFullStarAwardresp.Builder resp = GetFullStarAwardresp.newBuilder();
//		// 添加奖励
//		if(yes){
//			int sum = p == null? mibao_first_full_star: p.needAllStar;
//			MibaoJixing m = jXMap.get(sum);
//			if(m == null){
//				logger.error("没有配置文件MibaoJixing, sum:{}", sum);
//				return;
//			}
//			String[] jiangliArray = m.award.split("#");
//			for(String jiangli : jiangliArray) {
//				String[] infos = jiangli.split(":");
//				int type = Integer.parseInt(infos[0]);
//				int itemId = Integer.parseInt(infos[1]);
//				int count = Integer.parseInt(infos[2]);
//				AwardTemp a = new AwardTemp();
//				a.setItemType(type);
//				a.setItemId(itemId);
//				a.setItemNum(count);
//				AwardMgr.inst.giveReward(session, a, jz);
//				logger.info("君主：{}秘宝星星宝箱奖励：奖励 type {} id {} cnt{}", 
//						jid,type,itemId,count);
//			}
//			if(p == null){
//				p = new MibaoLevelPoint();
//				p.junzhuId = jid;
//				p.point = LEVEL_POINT_INIT;
//				p.lastAddTime = new Date();
//				p.lastBuyTime = new Date();
//				p.dayTimes = 0;
//				logger.error("在这里new了MibaoLevelPoint, 说明秘宝主页获取可能有问题");
//			}
//			p.needAllStar = m.nextSum;
//			HibernateUtil.save(p);
//			logger.info("君主：{}集齐秘宝星星,领取了 sum是：{}的奖励，成功，下一目标是：{}个",
//					jid, sum, p.needAllStar);
//			resp.setSuccess(1);
//			resp.setNexNeedAllStar(p.needAllStar);
//		}else{
//			// 星星没有达到要求，不能领奖
//			resp.setSuccess(0);
//		}
//		session.write(resp.build());
//	}

//	/**
//	 * 是否集齐固定星星数： ture，够了; false: 不够
//	 * @param mibaoDBList
//	 * @param p
//	 * @return
//	 */
//	public boolean isFull(List<MiBaoDB> mibaoDBList, MibaoLevelPoint p){
//		int allStar = 0;
//		for(MiBaoDB mibaoDB : mibaoDBList) {
//			if(mibaoDB.getLevel() <= 0 || mibaoDB.getMiBaoId() <= 0) {
//				continue;
//			}
//			allStar += mibaoDB.getStar();
//		}
//		if(p == null){
//			if(allStar >= mibao_first_full_star){
//				return true;
//			}
//			return false;
//		}
//		return allStar >= p.needAllStar? true: false;
//	}

	@Override
	public void proc(Event evt) {
		switch (evt.id) {
		case ED.REFRESH_TIME_WORK:
			IoSession session=(IoSession) evt.param;
			if(session == null){
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz == null){
				break;
			}
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.mibao, jz.id, jz.level);
			if(!isOpen){
//				logger.info("君主：{}--秘宝升级：{}的功能---未开启,不推送",jz.id,FunctionID.mibao);
				break;
			}
			// 秘宝升级
			isCanMiBaoShengJi(jz, session);
			// 秘宝技能
			int number = MibaoMgr.inst.getActivateMiBaoCount(jz.id);
			if(number > 0) isCanMiBaoJiNengOpen(jz, session, number);
			// 秘宝升星
			isMiBaoCanUpStar(jz, session);
			// 秘宝合成
			isMiBaoCanHeCheng(jz, session);
			break;
		default:
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
	
	public List<MiBaoSkillDB> getSkillDBList(long jId){
		/*List<MiBaoSkillDB> list = HibernateUtil.list(MiBaoSkillDB.class,
				"where jId =" + jId);*/
		List<MiBaoSkillDB> list = new ArrayList<MiBaoSkillDB>();
		Map<Integer,MiBaoSkillDB> m = MiBaoSkillDao.inst.getMap(jId);
		for(Integer key:m.keySet()){
			list.add(m.get(key));
		}
		return list;
	}
}
