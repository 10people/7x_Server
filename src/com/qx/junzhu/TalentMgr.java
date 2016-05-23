package com.qx.junzhu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ExpTemp;
import com.manu.dynasty.template.Talent;
import com.manu.dynasty.template.TalentAttribute;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.account.AccountManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.google.protobuf.MessageLite.Builder;

import qxmobile.protobuf.JunZhuProto.PointInfo;
import qxmobile.protobuf.JunZhuProto.TalentInfoResp;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelReq;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelResp;

/**
 * 
 * This class is used for ..
 * @author wangZhuan
 * @version   
 *       9.0, 2015年6月12日 上午11:43:50
 */
public class TalentMgr {

	/*
	 * 防御类型
	 */
	public static final int fangYuType =  2;
	/*
	 * 攻击类型
	 */
	public static final int gongJiType = 1;

	public static final int poJiaPointId = 101;
	public static final int tieBiPointId = 201;

	public static final int functionOpenId = 500000;
	/*
	 * 除了 第一个点数，其他的节点在首次升级的时候扣除对应点数的个数
	 */
	public static final int spendDianShu = 1;
	public static final int repayDianShuByUpLevel = 1;
	
	public static Map<Integer, Talent> talentMap = 
			new HashMap<Integer, Talent>();
	public static Map<Integer, List<TalentAttribute>> talentAttrMap = 
			new HashMap<Integer, List<TalentAttribute>>();
	public static TalentMgr instance;
	public static Logger log = LoggerFactory.getLogger(TalentMgr.class);
	public static List<Talent> listTalent = null;
	
	public TalentMgr(){
		@SuppressWarnings("unchecked")
		List<Talent> list = TempletService.listAll(
				Talent.class.getSimpleName());
		for(Talent t: list){
			talentMap.put(t.point, t);
		}
		listTalent = list;
		@SuppressWarnings("unchecked")
		List<TalentAttribute> listAttr =
				TempletService.listAll(TalentAttribute.class.getSimpleName());
		List<TalentAttribute> temp = null;
		int passId = 0;
		int id = 0;
		for(TalentAttribute att1: listAttr){
			id = att1.id;
			if(id != passId){
				temp = new ArrayList<TalentAttribute>();
				for(TalentAttribute att2: listAttr){
					if(att2.id == id){
						temp.add(att2);
					}
				}
//				log.info("talentAttrMap put id is: {}, and list size is :{}",
//						id ,temp.size());
				talentAttrMap.put(id, temp);
				passId = id;
			}
		}
		/*
		 *  创建唯一实例
		 */
		instance = this; 
	}

	public TalentAttribute getTalentAttrItem(int id, int lv){
		List<TalentAttribute> list = talentAttrMap.get(id);
		for(TalentAttribute ta: list){
			if(ta.lv == lv){
				return ta;
			}
		}
		return null;
	}
	public Map<Integer, TalentPoint> getJunZhuTalentPoints(long jzId){
		String where = "where junZhuId = " + jzId;
		List<TalentPoint> listT = HibernateUtil.list(TalentPoint.class, where);
		Map<Integer, TalentPoint> ret = new HashMap<Integer, TalentPoint>(listT.size());
		for(TalentPoint t : listT){
			ret.put(t.point, t);
		}
		return ret;
	}
	/*
	 * 发送天赋信息
	 */
	public void sendTalentInfo(IoSession session){
		if(session == null){
			return;
		}
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		long jId = Long.valueOf(junZhuId);
//		boolean open = FunctionOpenMgr.isFunctionOpen(functionOpenId, jId, -1);
//		if(!open){
//			log.error("天赋功能没有开启");
//			return;
//		}
		TalentAttr ta = HibernateUtil.find(TalentAttr.class, jId);
		if(ta == null){
			ta = new TalentAttr(jId);
		}
		TalentInfoResp.Builder resp = TalentInfoResp.newBuilder();
		resp.setWuYiJingQi(ta.wuYiJingQi);
		resp.setJinGongDianShu(ta.jinGongDianShu);
		resp.setFangShouDianShu(ta.fangShouDianShu);
		resp.setTiPoJingQi(ta.tiPoJingQi);
		PointInfo.Builder point= null;
		TalentAttribute taAttr = null;
		int level = 0;
		String where = "";
		Map<Integer, TalentPoint> map = getJunZhuTalentPoints(jId);
		for(Talent t: listTalent){
//			where = "where junZhuId=" + jId + " and point =" + t.point;
//			TalentPoint talentBean = HibernateUtil.find(TalentPoint.class, where);
			//避免多次查询
			TalentPoint talentBean = map.get(t.point);
			level = talentBean == null? 0:talentBean.level;
	
			point= PointInfo.newBuilder();
			point.setPointId(t.point);
			point.setPointLev(level);
			point.setDesc("");
			taAttr = getTalentAttrItem(t.attributeId, level);
			point.setDesc(taAttr == null ? "" : taAttr.desc);
			// 升级下一级需要的精气
			if(level < t.maxLv){
				ExpTemp expTemp = 
						TempletService.getInstance().getExpTemp(t.expId, level);
				int needJingQi = expTemp == null? Integer.MAX_VALUE: expTemp.getNeedExp();
				point.setNeedJingQi(needJingQi);
			}
			resp.addPoints(point);
		}
		session.write(resp.build());
	}

	/* 
	 * 增加武艺精气
	 */
	public int addWuYiJingQi(long junzhuId, int addValue){
		TalentAttr attr = HibernateUtil.find(TalentAttr.class, junzhuId);
		if(attr == null){
			attr = new TalentAttr(junzhuId);
			attr.wuYiJingQi = addValue;
			MC.add(attr, junzhuId);
			HibernateUtil.insert(attr);
		}else{
			attr.wuYiJingQi += addValue;
			HibernateUtil.save(attr);
		}
		// 检查是否存在天赋可以满足升级条件
		noticeTalentCanLevUp(junzhuId);
		return attr.wuYiJingQi;
	}
	/*
	 * 增加体魄精气
	 */
	public int addTiPoJingQi(long junzhuId, int addValue){
		TalentAttr attr = HibernateUtil.find(TalentAttr.class, junzhuId);
		if(attr == null){
			attr = new TalentAttr(junzhuId);
			attr.tiPoJingQi = addValue;
			MC.add(attr, junzhuId);
			HibernateUtil.insert(attr);
		}else{
			attr.tiPoJingQi += addValue;
			HibernateUtil.save(attr);
		}
		// 检查是否存在天赋可以满足升级条件
		noticeTalentCanLevUp(junzhuId);
		return attr.tiPoJingQi;
	}

	/*
	 * 元素节点升级
	 * 进攻： 破甲，武艺精气，进攻点数；
	 * 防御：铁壁，体魄精气，防守点数，
	 */
	public void doTalentUpLevel(IoSession session, Builder build){
		Long junZhuId = (Long)session.getAttribute(SessionAttKey.junZhuId);
		long jId = Long.valueOf(junZhuId);
		TalentUpLevelReq.Builder req = (TalentUpLevelReq.Builder)build;
		int pointId = req.getPointId();
		Talent talentData = talentMap.get(pointId);
		if(talentData == null){
			log.error("Talent数据:{}是null", pointId);
			return;
		}
		String where = "where junZhuId=" + jId + " and point =" + pointId;
		TalentPoint talentBean = HibernateUtil.find(TalentPoint.class, where);
		if(talentBean == null){
			talentBean = initTalentPoint(junZhuId, pointId);
		}
		TalentUpLevelResp.Builder resp = TalentUpLevelResp.newBuilder();
		if(talentBean.level == talentData.maxLv){
			log.error("节点:{}天赋等级已经达到最大，不能再升级", pointId);
			resp.setMsg("it is maxLevel");
			resp.setErrorNumber(1);
			session.write(resp.build());
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, jId);
		if(jz == null || talentBean.level + 1 > jz.level){
			log.error("节点:{}天赋等级将大于当前玩家等级，不能升级", pointId);
			resp.setMsg("it is max junzhu Level");
			resp.setErrorNumber(2);
			session.write(resp.build());
			return;
		}
		// 某节点从0级升级到1级的时候，需要前一个节点大于等于X，其他不管 
		if(talentBean.level == 0){
			String fronPoint = talentData.frontPoint;
			String[] ss = fronPoint.split(",");
			for(int i = 0; i < ss.length; i++){
				int id = Integer.parseInt(ss[i]);
				if(id != 0){
					where = "where junZhuId=" + jId + " and point =" + id;
					TalentPoint p = HibernateUtil.find(TalentPoint.class, where);
					if(p.level < talentData.frontLv){
						log.error("节点:{}不能升级，因为前一个节点等级的限制", pointId);
						resp.setMsg("it is front point level little");
						resp.setErrorNumber(7);
						session.write(resp.build());
						return;
					}
				}
			}
		}
		TalentAttr attr = HibernateUtil.find(TalentAttr.class, jId);
		if(attr == null){
			attr = new TalentAttr(jId);
			MC.add(attr, jId);
			HibernateUtil.insert(attr);
		}
		ExpTemp expTemp = 
				TempletService.getInstance().getExpTemp(talentData.expId, talentBean.level);
		int needJingQi = expTemp == null? Integer.MAX_VALUE: expTemp.getNeedExp();
		switch(talentData.type){
			case fangYuType:
				if(attr.tiPoJingQi < needJingQi){
					log.error("玩家:{}当前体魄精气:{}, 升级需要:{},所以:升级{}失败",
							jId, attr.tiPoJingQi, needJingQi, pointId);
					resp.setMsg("ti po jing qi little");
					resp.setErrorNumber(3);
					session.write(resp.build());
					return;
				}
				// 除了第一个节点，其他的在第一次升级的时候需要消耗点数
				if(talentBean.level <= 0 && pointId != tieBiPointId){
					if(attr.fangShouDianShu < spendDianShu){
						log.error("玩家:{}, 首次升级：{}，防守点数不够", jId, pointId);
						resp.setMsg("first time, jinGongDianshu little");
						resp.setErrorNumber(4);
						session.write(resp.build());
						return;
					}
					// 扣除点数
					attr.fangShouDianShu -= spendDianShu;
				}
				// 扣除精气
				attr.tiPoJingQi -= needJingQi;
				break;
			case gongJiType:
				if(attr.wuYiJingQi < needJingQi){
					log.error("玩家:{}当前武艺精气:{}, 升级需要:{},所以:升级{}失败",
							jId, attr.wuYiJingQi, needJingQi, pointId);
					resp.setMsg("wu yi jing qi little");
					resp.setErrorNumber(5);
					session.write(resp.build());
					return;
				}
				if(talentBean.level <= 0 && pointId != poJiaPointId){
					if(attr.jinGongDianShu < spendDianShu){
						log.error("玩家:{}, 首次升级：{}, 进攻点数不够", jId, pointId);
						resp.setMsg("first time, jinGongDianshu little");
						resp.setErrorNumber(6);
						session.write(resp.build());
						return;
					}
					// 扣除点数
					attr.jinGongDianShu -= spendDianShu;
				}
				// 扣除精气
				attr.wuYiJingQi -= needJingQi;
				break;
		}

		talentBean.level += 1;
		HibernateUtil.save(talentBean);

		/*
		 * 判断是否 增加点数
		 */
		int addDianshu = addDianShuByUpLevel(pointId, talentBean.level);
		if(addDianshu > 0){
			switch(pointId){
				case tieBiPointId:
					attr.fangShouDianShu += addDianshu;
					break;
				case poJiaPointId:
					attr.jinGongDianShu += addDianshu;
					break;
			}
		}

		HibernateUtil.save(attr);
		log.info("玩家：{}升级天赋：{}成功, 当前等级是：{}, 防守点数是：{}， 进攻点数是：{}",
				jId, pointId, talentBean.level, attr.fangShouDianShu, attr.jinGongDianShu);
		ActLog.log.KingTalent(jz.id, jz.name, ActLog.vopenid, pointId, talentData.name, talentBean.level, talentData.type, 1);
		sendTalentInfo(session);
		// update君主属性列表
		JunZhuMgr.inst.sendMainInfo(session,jz);
		// 主线任务完成: 升级天赋
		EventMgr.addEvent(ED.tianfu_level_up_x, new Object[]{jId, talentBean.level});
		// 每日任务
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, 
				new DailyTaskCondition(jId, DailyTaskConstants.level_up_tianFu, 1));
		// 刷新君主榜 2015-7-30 14：44
		EventMgr.addEvent(ED.JUN_RANK_REFRESH, jz);
		/*
		 * 检查是否要进行升级提示
		 */
		boolean can = isTalentCanUpLevel(jId);
		if(can){
			session.write(PD.NOTICE_TALENT_CAN_UP);
		}else{
			session.write(PD.NOTICE_TALENT_CAN_NOT_UP);
		}
	}

	public TalentPoint initTalentPoint(long junZhuId, int pointId){
		TalentPoint point = new TalentPoint();
		point.junZhuId = junZhuId;
		point.point = pointId;
		point.level = 0;
		return point;
	}
	public int addDianShuByUpLevel(int pointId, int level){
		if(pointId != poJiaPointId && pointId != tieBiPointId){
			return 0;
		}
		if(level == CanShu.TIANFULV_DIANSHUADD1 
				|| level == CanShu.TIANFULV_DIANSHUADD2
				|| level == CanShu.TIANFULV_DIANSHUADD3
				|| level == CanShu.TIANFULV_DIANSHUADD4
				|| level == CanShu.TIANFULV_DIANSHUADD5
				){
			return repayDianShuByUpLevel;
		}
		return 0;
	}

	public void noticeTalentCanLevUp(long jId){
		IoSession session = AccountManager.getIoSession(jId);
		if(session!=null&&isTalentCanUpLevel(jId)){
			session.write(PD.NOTICE_TALENT_CAN_UP);
		}
	}
	public boolean isTalentCanUpLevel(long jId){
		JunZhu jz = HibernateUtil.find(JunZhu.class, jId);
		if(jz == null){
			return false;
		}
//		boolean yes = FunctionOpenMgr.inst.isFunctionOpen(functionOpenId, jId, jz.level);
//		if(! yes){
//			return false;
//		}
		Map<Integer, TalentPoint> map = getJunZhuTalentPoints(jId);
		for(Talent talentData: listTalent){
			int pointId = talentData.point;
//			String where = "where junZhuId=" + jId + " and point =" + pointId;
//			TalentPoint talentBean = HibernateUtil.find(TalentPoint.class, where);
			//避免多次查询
			TalentPoint talentBean = map.get(pointId);
			if(talentBean == null){
				talentBean = initTalentPoint(jId, pointId);
			}
			if(talentBean.level == talentData.maxLv){
				continue;
			}
			if(talentBean.level + 1 > jz.level){
				continue;
			}
			// 某节点从0级升级到1级的时候，需要前一个节点大于等于X，其他不管 
			if(talentBean.level == 0){
				boolean can = false;
				String fronPoint = talentData.frontPoint;
				String[] ss = fronPoint.split(",");
				for(int i = 0; i < ss.length; i++){
					int id = Integer.parseInt(ss[i]);
					if(id != 0){
//						where = "where junZhuId=" + jId + " and point =" + id;
//						TalentPoint p = HibernateUtil.find(TalentPoint.class, where);
						TalentPoint p = map.get(id);
						if(p == null || p.level < talentData.frontLv){
							can = true;
							break;
						}
					}
				}
				if(can){
					continue;
				}
			}
			
			TalentAttr attr = HibernateUtil.find(TalentAttr.class, jId);
			if(attr == null){
				attr = new TalentAttr(jId);
			}
			ExpTemp expTemp = 
					TempletService.getInstance().getExpTemp(talentData.expId, talentBean.level);
			int needJingQi = expTemp == null? Integer.MAX_VALUE: expTemp.getNeedExp();
			if(talentData.type == fangYuType){
				if(attr.tiPoJingQi < needJingQi){
					continue;
				}
				// 除了第一个节点，其他的在第一次升级的时候需要消耗点数
				if(talentBean.level <= 0 && pointId != tieBiPointId){
					if(attr.fangShouDianShu < spendDianShu){
						continue;
					}
				}
			}else{
				if(attr.wuYiJingQi < needJingQi){
					continue;
				}
				if(talentBean.level <= 0 && pointId != poJiaPointId){
					if(attr.jinGongDianShu < spendDianShu){
						continue;
					}
				}
			}
			return true;
		}
		return false;
	}
}
