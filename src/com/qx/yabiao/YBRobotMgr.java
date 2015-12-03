package com.qx.yabiao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Yabiao.BiaoCheState;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.CartPath;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.persistent.HibernateUtil;
import com.qx.world.Scene;
/**
 * 
 * @author yuquanshui 
 *
 */
public class YBRobotMgr implements Runnable{
	public static Logger log = LoggerFactory.getLogger(YBRobotMgr.class);
	public static int INTERVAL = 100;
	public static boolean isShowLog = false;
	public static YBRobotMgr inst;
	
	/**
	 * yabiaoRobotMap 押镖机器人线程管理YaBiaoRobot的Map
	 */
	public ConcurrentHashMap<Long, Object> yabiaoRobotMap;
	public static HashMap<Integer, HashMap<Integer, RoadNode>> road;//双层map第一层存路线第二层节点
	public static HashMap<Integer,Double> distanceMap;
	public YBRobotMgr(){
		inst = this;
		initData();
		new Thread(this, "ybrobot").start();
	}
	public void initData(){
		yabiaoRobotMap= new  ConcurrentHashMap<Long, Object>();
		road= new  HashMap<Integer, HashMap<Integer, RoadNode>>();
		List<CartPath> list = TempletService.listAll(CartPath.class.getSimpleName());
		double distance4All=0;
		int	pathId=0;
		int baimaTotalTime=60000;//TODO 普通车走到终点的时间600秒 (假设，许根据配置改)
		HashMap<Integer,Double> distMap=new HashMap<Integer, Double>();
		HashMap<Integer, RoadNode> roadPath=null;
		int lsize=list.size();
		for (int i= 0; i < lsize; i++) {
			CartPath c = list.get(i);
			if(pathId==0){
				pathId=c.id;
			}
			if(i+1==lsize){
				distMap.put(pathId,distance4All);
				log.info("路线--{}的路程为--{}。。。",pathId,distance4All);
				log.info("所有路线路程计算完成");
				break;
			}
			CartPath cNext = list.get(i+1);
			if(c.id!=cNext.id){
				if(distance4All>0){
					distMap.put(pathId,distance4All);
					log.info("路线--{}的路程为--{}",pathId,distance4All);
				}
				pathId=cNext.id;
				distance4All=0;
				continue;
			}
			//计算路程
			double distance=Math.sqrt((cNext.x-c.x)*(cNext.x-c.x)+(cNext.z-c.z)*(cNext.z-c.z));
			distance4All+=distance;
		}
		YBRobotMgr.distanceMap=distMap;
		pathId=0;
		for (int i= 0; i < lsize; i++) {
			CartPath c = list.get(i);
			if((pathId>0&&pathId!=c.id)||(i+1==lsize)){
				road.put(pathId, roadPath);
			}
			//计算路程
			if(pathId!=c.id){
				pathId=c.id;
				roadPath=new HashMap<Integer, RoadNode>();
			}
			RoadNode node=new RoadNode();
			node.pathId=c.id;
			node.nodeId=c.position;
			node.posX=c.x;
			node.posZ=c.z;
			CartPath cNext =null;
			if(i+1<lsize){
				//计算去下一点所需时间
				double distance=0;
				cNext = list.get(i+1);
				if(c.id==cNext.id){
					//计算路程
					distance=Math.sqrt((cNext.x-c.x)*(cNext.x-c.x)+(cNext.z-c.z)*(cNext.z-c.z));
					//TODO 初始化节点 算出每个节点的时间 结合 cartTemp对象算出来totaltime 还有速度
					Double distance4all=distMap.get(node.pathId);
					if(distance4all==null){
						log.info("distance4all==null");
					}
					node.totalTime=baimaTotalTime*(distance/distance4all);
				}
			}
			roadPath.put(node.nodeId,node);
		}
		log.info("押镖机器人路线数据加载完成");
//		HashMap<Integer, RoadNode>	roadPath1=road.get(1);
//		Iterator iter = roadPath1.entrySet().iterator();
//		while (iter.hasNext()) {
//				Map.Entry entry = (Map.Entry) iter.next();
//				Object key = entry.getKey();
//				RoadNode val = (RoadNode) entry.getValue();
//				System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath2=road.get(2);
//		Iterator iter2 = roadPath2.entrySet().iterator();
//		while (iter2.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter2.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath3=road.get(3);
//		Iterator iter3 = roadPath3.entrySet().iterator();
//		while (iter3.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter3.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath4=road.get(4);
//		Iterator iter4 = roadPath4.entrySet().iterator();
//		while (iter4.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter4.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
		log.info("押镖机器人路线数据加载完成");
	}

	@Override
	public void run() {
		while (!GameServer.shutdown) {
			try {
				Thread.sleep(INTERVAL);
				robotsMove();
			} catch (Exception e) {
				log.error("押镖机器人线程执行押镖移动出错 : {}", e);
			}
		}
		//log.info("退出押镖机器人线程");
	}
	//移动所有镖车
	public void robotsMove() {
		if(isShowLog){
			log.info("押镖机器人移动开始");
		}
		if(BigSwitch.inst.ybMgr.yabiaoScenes.size()==0){
			return;
		}
		Iterator<?> it= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Integer scId =(Integer) entry.getKey();
			Scene ybsc =(Scene) entry.getValue();
			Iterator<?> itrobot=yabiaoRobotMap.entrySet().iterator();
			while (itrobot.hasNext()) {
				Map.Entry entryRobot = (Map.Entry) itrobot.next();
				YaBiaoRobot ybr = (YaBiaoRobot) entryRobot.getValue();
				if(ybr==null){
					log.error("押镖 场景机器人出错");
					continue;
				}
//				log.info("押镖 场景机器人---{}开始移动",ybr.name);
				long now = System.currentTimeMillis();
				long addTime=now -ybr.startTime;
				if(isShowLog){
					log.info("now-{},start---{},total---{},add---{},used--{}",now,ybr.startTime,ybr.totalTime,addTime,ybr.usedTime);
				}
				move(ybsc,ybr,scId);
				//镖车不会进入战斗了
//					else{//进入战斗保存镖车状态 }

			}
		}
		if(isShowLog){
			log.info("押镖机器人移动结束");
		}
	}
	//移动实际为扣除已走的时间（路程已定，速度恒定，则可按照时间来计算坐标）
	public void move(Scene sc,YaBiaoRobot ybr,int scId) {
		if(isShowLog){
			log.info("{}镖车移动，已用时间为{}-{},进度{}%",ybr.jzId,ybr.usedTime,ybr.totalTime,(int)((ybr.usedTime/(double)ybr.totalTime)*100));
		}
		long now = System.currentTimeMillis();
		long usedtime=now-ybr.startTime;
		int upSpeed2UsedTime=(int) (now-ybr.startTime2upSpeed);
		if(upSpeed2UsedTime>=ybr.upSpeedTime){
			ybr.speed=1;
			ybr.startTime2upSpeed=now;
			ybr.upSpeedTime=0;
		}
		ybr.startTime=now;
		ybr.usedTime+=usedtime;
		ybr.usedtime4short=(int) (now-ybr.startTime4short);
		
		if(ybr.usedtime4short>=ybr.totaltime4short){
			HashMap<Integer, RoadNode> roadPath=road.get(ybr.pathId);
			//跳到下一节点
			ybr.nodeId+=1;
			if(ybr.nodeId>=roadPath.size()){
				log.info("机器人镖车到达最后一个节点{}--{}-{}",ybr.move.getPosX(),ybr.move.getPosY(),ybr.move.getPosZ());
				
				ybr.posX=ybr.nextPosX;
				ybr.posZ=ybr.nextPosZ;
				ybr.move.setPosX(ybr.posX);
				ybr.move.setPosZ(ybr.posZ);
				IoSession	session=ybr.session;
//				sc.exec(PD.Spirite_Move, session, ybr.move);
				log.info("机器人镖车到达终点");
				// 移除押镖人的相关信息
				YaBiaoHuoDongMgr.inst.settleYaBiaoSuccess(ybr.jzId,scId);
				return;
			}
			RoadNode rd=roadPath.get(ybr.nodeId);
			if(rd==null){
				log.error("押镖机器人移动错误,节点rd==null");
				return;
			}
			ybr.usedtime4short=ybr.usedtime4short-ybr.totaltime4short;
			//重置一小段路起点终点
			ybr.startPosX=ybr.nextPosX;
			ybr.startPosZ=ybr.nextPosZ;
			RoadNode rd4next=roadPath.get(ybr.nodeId+1);
			if(rd4next==null){
				log.error("押镖机器人移动错误,节点rd4next==null");
				return;
			}
			ybr.nextPosX=rd4next.posX;
			ybr.nextPosZ=rd4next.posZ;
			double totaltime4Nextshort=rd.totalTime;// TODO 从路线对象里面取出来
			ybr.totaltime4short=totaltime4Nextshort;
			ybr.startTime4short=now;
		}

		if(ybr.totaltime4short==0){
//			ybr.posX=ybr.nextPosX;
//			ybr.posZ=ybr.nextPosZ;
			log.error("押镖机器人移动错误,ybr.totaltime4short==0,~~~~~~~~~~~~~~~~~~~~~~~~");
			return;
		}else{
			//计算当前坐标
			ybr.posX=(float) (ybr.startPosX+(ybr.nextPosX-ybr.startPosX)*(ybr.usedtime4short/ybr.totaltime4short)*ybr.speed);
			ybr.posZ=(float) (ybr.startPosZ+(ybr.nextPosZ-ybr.startPosZ)*(ybr.usedtime4short/ybr.totaltime4short)*ybr.speed);
		}
		ybr.move.setPosX(ybr.posX);
		ybr.move.setPosZ(ybr.posZ);
	
		IoSession	session=ybr.session;
		int baifenb=(int)((ybr.usedTime/(double)ybr.totalTime)*100);
		if(isShowLog){
			log.info("机器人镖车--{},已用时间为{}-{},进度{}%,移动节点id--{}到{}--{}-{},从--{}-{}走向--{}--{}",
					ybr.jzId,ybr.usedTime,ybr.totalTime,baifenb,
					ybr.nodeId,ybr.move.getPosX(),ybr.move.getPosY(),ybr.move.getPosZ(),
					ybr.startPosX,ybr.startPosZ,ybr.nextPosX,ybr.nextPosZ);
		}
		sc.exec(PD.Spirite_Move, session, ybr.move);
	}
	//广播押镖进度
	public void broadBiaoCheEvent(YaBiaoRobot ybr,Scene sc) {
		try {
			if(isShowLog){
				log.info("押镖机器人广播状态开始");
			}
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//广播押镖进度
	public void broadBiaoCheEvent4FeiQi(YaBiaoRobot ybr,Scene sc) {
		try {
			if(isShowLog){
				log.info("押镖机器人广播状态开始");
			}
			BiaoCheState.Builder resp=BiaoCheState.newBuilder();
			resp.setJunZhuId(ybr.jzId);
			resp.setUsedTime(ybr.usedTime);
			YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybr.jzId);
			resp.setHp(ybr.hp);
			resp.setWorth(ybr.worth);
			int protectTime=ybr.protectCD-((int)(System.currentTimeMillis()-ybr.endBattleTime)/1000);
			resp.setBaohuCD(protectTime>0?protectTime:0);
			resp.setState(ybr.isBattle?20:(protectTime>0?30:10));//10押送中 20 战斗中 30 保护CD
			Integer scId=BigSwitch.inst.ybMgr.ybJzId2ScIdMap.get(ybr.jzId);
			if(scId==null){
				log.error("镖车所在场景未找到{}",ybr.jzId);
				return;
			}
			Set<Long> jbSet= BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
			if(jbSet==null){
				if(isShowLog){
					log.info("场景{}中无劫镖人员,无须广播",sc.name);
				}
				return;
			}
			for (Long jId : jbSet) {
				SessionUser su = SessionManager.inst.findByJunZhuId(jId);
				if(su!=null){//su为空时认为此账号已下线，不推送信息
					if(isShowLog){
						log.info("{}镖车移动广播进度给{}",ybr.jzId,jId);
					}
					su.session.write(resp.build());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
