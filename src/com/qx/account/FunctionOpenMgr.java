package com.qx.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.JunZhuProto.MainSimpleInfoReq;
import qxmobile.protobuf.JunZhuProto.MainSimpleInfoResp;
import qxmobile.protobuf.JunZhuProto.SimpleInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.FunctionOpen;
import com.manu.dynasty.template.ZhuXian;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.SessionAttKey;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.LveDuoBean;
import com.qx.pvp.LveStaticData;
import com.qx.pvp.PvpBean;
import com.qx.pvp.PvpMgr;
import com.qx.task.GameTaskMgr;
import com.qx.task.WorkTaskBean;
import com.qx.timeworker.FunctionID;
import com.qx.yabiao.YaBiaoBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;

/**
 * @author 康建虎
 *
 */
public class FunctionOpenMgr {
	public static String REN_WU_OVER_ID = "RenWuOverId#";
	public static String awardRenWuOverIdKey = "AwardRenWuOverId#";
	public static FunctionOpenMgr inst = new FunctionOpenMgr();
	public static Logger log = LoggerFactory.getLogger(FunctionOpenMgr.class);
	public static List<Integer> initIds; 
	public static FunctionOpen[] others; 
	public static List<FunctionOpen> list;
	public static Map<Integer, FunctionOpen> openMap = new HashMap<Integer, FunctionOpen>();
//	public static int HUANG_YE_QIU_SHENG = 300200;
	public static final int TYPE_ALLIANCE = FunctionID.LianMeng;			//联盟类型

	public  void init(){
		list = TempletService.listAll(FunctionOpen.class.getSimpleName());
		initIds = new ArrayList<Integer>();
		ArrayList<FunctionOpen> othersL = new ArrayList<FunctionOpen>();
		for(FunctionOpen o : list){
			if(o.lv<=0 && o.RenWuID<=0 && o.RenWuIDAward <=0){
				initIds.add(o.id);
			}else{
				othersL.add(o);
				if(o.RenWuID>0){
					ZhuXian rwConf = GameTaskMgr.inst.zhuxianTaskMap.get(o.RenWuID);
					if(rwConf != null){
						o.renWuOrderIdx = rwConf.orderIdx;
					}
				}
				if(o.RenWuIDAward>0){
					ZhuXian rwConf = GameTaskMgr.inst.zhuxianTaskMap.get(o.RenWuIDAward);
					if(rwConf != null){
						o.awardRenWuOrderIdx = rwConf.orderIdx;
					}
				}
			}
			openMap.put(o.id, o);
		}
		others = new FunctionOpen[othersL.size()];
		othersL.toArray(others);
	}
	public  void fillOther(qxmobile.protobuf.ZhangHao.LoginRet.Builder ret, int level, long pid) {
		int len = others.length;
		int maxRenWuOrderIdx = getMaxRenWuOrderIdx(pid);//注意！这一行和下面一行的顺序不能变！
		int maxAwardOrderIdx = getMaxAwardRenWuOrderIdx(pid);
		if(maxRenWuOrderIdx>0 && maxAwardOrderIdx==0){//memcached清除后，修正maxAwardRenWuId
//			//如果有当前最大任务id，但没有领奖任务id，则用最大任务id减一（即前一个任务）来作为领奖任务id。
			//君主信息中的maxTaskAwardId只要完成任何一个任务就会刷新
			//修改为数据库存储之后，几乎不会触发此判断，为减轻数据库压力所以不在这里进行存储
			maxAwardOrderIdx = maxRenWuOrderIdx-1;
			log.error("君主{}的主线任务领奖记录丢失",pid);
//			//注意！！取当前任务的前一个任务的orderIdx，并保存其【id】到缓存。
//			List<ZhuXian> list = TempletService.listAll(ZhuXian.class.getSimpleName());
//			ZhuXian conf = list.get(maxRenWuOrderIdx-1);
//			MemcachedCRUD.getMemCachedClient().set(awardRenWuOverIdKey+pid, conf.orderIdx);
//			ZhuXian conf = GameTaskMgr.inst.zhuxianTaskMap.get(maxRenWuOrderIdx);
//			maxAwardOrderIdx = conf == null ? 0 : Integer.parseInt(conf.getTriggerCond());
			
		}
		for(int i=0; i<len; i++){
			//
			FunctionOpen o = others[i];
			//等级和任务只有一个限制
			if(o.lv >0){
				if(o.lv>level){//要求等级大于当前等级，不开
					continue;
				}
			}else if(o.RenWuID > 0){
				if(o.renWuOrderIdx>maxRenWuOrderIdx){//要求任务大于当前最大的完成了的任务的序号，不开。
					continue;
				}
			}else if(o.RenWuIDAward > 0){
				if(o.awardRenWuOrderIdx>maxAwardOrderIdx){
					continue;
				}
			}
			ret.addOpenFunctionID(o.id);
		}
	}
	public  int getMaxAwardRenWuOrderIdx(long pid){
//		Object mcV = MemcachedCRUD.getMemCachedClient().get(awardRenWuOverIdKey+pid); 
		int maxAwardRenWuId = 0;// 领奖的最大任务id
//		if(mcV != null){
//			maxAwardRenWuId = Integer.valueOf(mcV.toString());
//		}else{
//			List<WorkTaskBean> rwlist = GameTaskMgr.inst.getTaskList(pid);
//			// 找到数据库中存储的主线任务的最大id
//			for(WorkTaskBean b : rwlist){
//				ZhuXian conf = GameTaskMgr.inst.zhuxianTaskMap.get(b.tid);
//				if(conf != null && conf.type == GameTaskMgr.zhuXianType){
//					maxAwardRenWuId = Math.max(maxAwardRenWuId, b.tid);
//				}
//			}
//			// maxAwardRenWuId(!=0)的触发任务一定是领了奖励的主线任务
//			ZhuXian conf = GameTaskMgr.inst.zhuxianTaskMap.get(maxAwardRenWuId);
//			if(conf != null){
//				// 第一条任务
//				if(maxAwardRenWuId != 100000){
//					maxAwardRenWuId = Integer.parseInt(conf.getTriggerCond());
//				}
//				MemcachedCRUD.getMemCachedClient().set(awardRenWuOverIdKey+pid, maxAwardRenWuId);
//			}
//		}
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, pid);
		if( junZhu != null ){
			maxAwardRenWuId = junZhu.maxTaskAwardId;
		}
		if(maxAwardRenWuId == 0){
			return maxAwardRenWuId; 
		}
		ZhuXian conf = GameTaskMgr.inst.zhuxianTaskMap.get(maxAwardRenWuId);
		if(conf == null){
			log.error("没有找到任务配置{} of pid {}",maxAwardRenWuId, pid);
			maxAwardRenWuId = 0;
		}else{
			maxAwardRenWuId = conf.orderIdx;
		}
		return maxAwardRenWuId;
	}

	public  int getMaxRenWuOrderIdx(long pid) {
//		Object mcV = MemcachedCRUD.getMemCachedClient().get(REN_WU_OVER_ID+pid); 
		
//		if(mcV != null){
//			maxRenWuId = Integer.valueOf(mcV.toString());
//		}else{
//			List<WorkTaskBean> rwlist = GameTaskMgr.inst.getTaskList(pid);
//			for(WorkTaskBean b : rwlist){
//				maxRenWuId = Math.max(maxRenWuId, b.tid);
//			}
//			MemcachedCRUD.getMemCachedClient().set(REN_WU_OVER_ID+pid, maxRenWuId);
//		}
//		if(maxRenWuId==0){
//			return maxRenWuId;
//		}
		
		int maxRenWuId = 0;//已完成的最大任务ID。
		
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, pid);
		if( junZhu == null ){
			return maxRenWuId;
		}
		maxRenWuId = junZhu.maxTaskOverId ;
		ZhuXian maxRenWuConf = GameTaskMgr.inst.zhuxianTaskMap.get(maxRenWuId);
		if(maxRenWuConf == null){
			log.error("没有找到任务配置{} of pid {}",maxRenWuId, pid);
			//如果任务删了，找不到，则找前一个任务
			List<ZhuXian> list = TempletService.listAll(ZhuXian.class.getSimpleName());
			int pre = 0;
			for(ZhuXian z : list){
				if(z.id>=maxRenWuId){
					break;
				}
				pre = z.orderIdx;
			}
			maxRenWuId = pre;
		}else{
			maxRenWuId = maxRenWuConf.orderIdx;
		}
		return maxRenWuId;
	}

	/**
	 * @Title: isFunctionOpen 
	 * @Description:
	 * @param key : FunctionOpen.xml的id
	 * @param junZhuId
	 * @param level 当是以任务为开启功能的方式是，<level>参数可以为任何值
	 * @return
	 */
	public  boolean isFunctionOpen(int key, long junZhuId, int level){
		FunctionOpen o = openMap.get(key);
		if(o == null){
			return false;
		}
		if(o.lv == 0){
			return true;
		}
		if(o.lv > 0){
			if(level >= o.lv){//要求等级大于当前等级，不开
				return true;
			}
			return false;
		}
		if(o.RenWuID > 0){
			int maxRenWuId = getMaxRenWuOrderIdx(junZhuId);
			if(maxRenWuId >= o.renWuOrderIdx){//要求任务大于当前最大的完成了的任务的序号，不开。
				return true;
			}
			return false;
		}
		if(o.RenWuIDAward > 0){
			int max = getMaxAwardRenWuOrderIdx(junZhuId);
			if(max >= o.awardRenWuOrderIdx){//要求任务大于当前最大的完成了的任务的序号，不开。
				return true;
			}
			return false;
		}
		return false;
	}
	
	public static void getFunctionInfo(IoSession session , Builder builder){
		Long jId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(jId == null) {
			log.error("找不到君主");
			return;
		}
		MainSimpleInfoReq.Builder req = (MainSimpleInfoReq.Builder)builder;
		int functionId = req.getFunctionType();
		MainSimpleInfoResp.Builder resp = MainSimpleInfoResp.newBuilder();
		switch(functionId){
		case FunctionID.chuZheng_zhanDou: // 出征-战斗
			SimpleInfo.Builder infob = SimpleInfo.newBuilder();
			YaBiaoBean b = HibernateUtil.find(YaBiaoBean.class, jId);
			int all = YaBiaoHuoDongMgr.inst.getYaBiaoCountForVip(0);
			int remain = all;
			if(b != null){
				if (DateUtils.isTimeToReset(b.lastShowTime, CanShu.REFRESHTIME_PURCHASE)) {
					// nothing
				}else{
					remain = b.remainYB;
					all = b.remainYB + b.usedYB;
				}
			}
			infob.setNum1(remain);
			infob.setNum2(all);
			infob.setFunctionId(FunctionID.yabiao);

			resp.addInfo(infob);

			LveDuoBean lvb= HibernateUtil.find(LveDuoBean.class, jId);
			all = LveStaticData.free_all_battle_times;
			remain = all;
			if(lvb != null){
				if (DateUtils.isTimeToReset(lvb.lastRestTime, CanShu.REFRESHTIME_PURCHASE)) {
				}else{
					all = lvb.todayTimes;
					remain = lvb.todayTimes - lvb.usedTimes;
				}
			}
			infob = SimpleInfo.newBuilder();
			infob.setNum1(remain);
			infob.setNum2(all);
			infob.setFunctionId(FunctionID.lveDuo); // 掠夺
	
			resp.addInfo(infob);
			
			//江源：加入百战信息，2016-05-06
			PvpBean pvpb = HibernateUtil.find(PvpBean.class, jId);
			//先设置总次数为配置默认免费次数，剩余次数等于总次数
			all = CanShu.BAIZHAN_FREE_TIMES;
			remain = all;
			if(pvpb != null){
				//如果玩家的百战信息有记录，先刷新下信息
				PvpMgr.inst.resetPvpBean(pvpb);
				//总次数=以已用次数+剩余次数
				all = pvpb.usedTimes + pvpb.remain;
				remain = pvpb.remain;
			}
			infob = SimpleInfo.newBuilder();
			infob.setNum1(remain);
			infob.setNum2(all);
			infob.setFunctionId(FunctionID.baizhan); 
			resp.addInfo(infob);

			session.write(resp.build());
			break;
			// 其实是游侠按钮改为的试炼按钮，服务器不做处理
//		case FunctionID.shi_lian:
//			break;
		}
	}
}
