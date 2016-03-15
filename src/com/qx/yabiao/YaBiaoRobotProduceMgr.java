package com.qx.yabiao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.qx.ranking.RankingMgr;
import com.qx.world.Player;
import com.qx.world.Scene;

public class YaBiaoRobotProduceMgr  implements Runnable {
	public static Logger log = LoggerFactory.getLogger(YaBiaoRobotProduceMgr.class.getSimpleName());
	public static YaBiaoRobotProduceMgr inst;
	public static int interval=YunbiaoTemp.cartAI_appear_interval*1000;
	//2016年1月20日 xtmcs4Scene修改为存储场景待生产的马车等级列表
	public static List<Integer> produceCartList=Collections.synchronizedList(new ArrayList<Integer>()); 
	//线程Flag
	public static boolean produceReturn = true;

	public YaBiaoRobotProduceMgr() {
		inst = this;
		// 开启线程
		Thread t = new Thread(this, "YaBiaoRobotProduceMgr");
		t.setDaemon(true);
		t.start();
	}


	@Override
	public void run() {
		//先刷新产生服务器机器马车等级列表
		refreshSysCartLevelList();
		while(produceReturn) {
			try {
				produceCart();
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("退出YaBiaoRobotProduceMgr");
	}
	public boolean isNeedProduce(int ybScId,Scene ybsc) {
		if(ybsc==null||ybsc.players==null){
			return false;
		}
		int macheSize=0;
		List<Integer> produceList=new ArrayList<Integer>();
		for (Player p :  ybsc.players.values()){
			long ybjzId=p.jzId;
			if(ybjzId<0){
				YaBiaoRobot	tem=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
				if(tem==null){
					log.error("产生系统马车出错，YaBiaoRobot=={}为空",ybjzId);
					continue;
				}
				if(!produceList.contains(tem.bcNPCNo)){
					produceList.add(tem.bcNPCNo);
				}
				macheSize++;//场景中的押镖马车数目
			}
		}
		if(macheSize>=YunbiaoTemp.cartAImax){
			log.info("场景中有系统马车{}辆，不需要系统马车机器人",macheSize);
			return false;
		}
		return true;
	}
	public synchronized void produceCart() {
		log.info("产生系统马车开始");
		Iterator<?> it4Scene= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
		List<ProduceCartInfo> futureProducelist=new ArrayList<ProduceCartInfo>();
		while (it4Scene.hasNext()) {
			Map.Entry entry4Sc = (Map.Entry) it4Scene.next();
			Integer ybScId =(Integer) entry4Sc.getKey();
			Scene ybsc =(Scene) entry4Sc.getValue();
			if(!isNeedProduce(ybScId, ybsc)){
				continue;
			}
			for(int pathId=1;pathId<5;pathId++) {
				doCheck(ybScId, pathId, ybsc,futureProducelist); 
			}
		}
		doProduce(futureProducelist);
		log.info("产生系统马车结束");
	}
	public void doProduce(List<ProduceCartInfo> futureProducelist) {
		for (ProduceCartInfo pInfo : futureProducelist) {
			log.info("产生参数-----------------------------------------------------------------------------{}",pInfo);
			//安全区内人数不满开始产生马车
			produceXiTongMaChe(pInfo.ybsc, pInfo.ybScId, pInfo.pathId,pInfo.produceNo );
		}
	}
	
	public void  doCheck(int ybScId,int pathId,Scene ybsc, List<ProduceCartInfo> futureProducelist) {
		if(ybsc==null||ybsc.players==null){
			return ;
		}
		log.info("检查并生成场景----{}场景Id----{}路线---{}的马车",ybsc.name,ybScId,pathId);
		int macheSize=0;
		List<Integer> checkedList=new ArrayList<Integer>();
		//马车生产列表也算进去
		for (ProduceCartInfo pcInfo : futureProducelist) {
			checkedList.add(pcInfo.produceNo);
		}
		//遍历检查
		for (Iterator<Player> it = ybsc.players.values().iterator();it.hasNext();){
			Player p = it.next();
			long ybjzId=p.jzId;
			if(ybjzId<0){
				YaBiaoRobot	tem=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
				if(tem==null){
					log.error("产生系统马车出错，YaBiaoRobot=={}为空",ybjzId);
					continue;
				}
				if(!checkedList.contains(tem.bcNPCNo)){
					checkedList.add(tem.bcNPCNo);
				}
				macheSize++;//场景中的押镖马车数目
			}
		}
		int checkSize=checkedList.size();
		if(macheSize>=YunbiaoTemp.cartAImax||checkSize>=YunbiaoTemp.cartAImax){
			log.info("待检测马车列表大小--{}， 场景中有系统马车{}辆,不需要系统马车机器人",checkSize,macheSize);
			return;
		}
		int limitNO=YaBiaoRobotProduceMgr.produceCartList.size()-1;
		Collections.sort(checkedList);
		//得出要生成的最大等级镖车编号
		int produceNo=0;
		for (Integer cartNo : checkedList) {
			if(cartNo==produceNo){
				if(cartNo<limitNO){
					produceNo++;
				}
				continue;
			}else{
				if(produceNo>cartNo){
					produceNo=cartNo;
					break;
				}
			}
		}
		Map<Integer, Integer> safeMap=YaBiaoHuoDongMgr.inst.getSafeAreaCount(ybsc);
		Integer renshu=safeMap.get(pathId);
		renshu=renshu==null?0:renshu;
		if(renshu<YunbiaoTemp.saveArea_people_max){
			log.info("场景中有系统马车{}辆，安全区人数--{}，产生马车NO--{}",macheSize,renshu,produceNo);
			checkedList.add(produceNo);
			//安全区内人数不满开始产生马车
			//				produceXiTongMaChe(ybsc, ybScId, pathId,produceNo );
			ProduceCartInfo produceCartInfo=new ProduceCartInfo(ybsc, ybScId, pathId,produceNo );
			futureProducelist.add(produceCartInfo);
		}
		log.info("检查并生成场景----{}场景Id----{}路线---{}的马车结束",ybsc.name,ybScId,pathId);
	}
	/**
	 * @Description 产生系统马车
	 * @param ybsc 场景
	 * @param ybScId 场景Id
	 * @param pathId 安全区编号/路线编号
	 * @param cartNo  产生马车编号 0-19
	 */
	protected void produceXiTongMaChe(Scene ybsc,int ybScId,int pathId,int cartNo) {
		boolean res=YaBiaoHuoDongMgr.inst.initSysYBRobots(ybsc, pathId,ybScId,cartNo);
		log.info("产生编号--{}系统马车结果isOK?==={}",cartNo,res);
	}
	public void refreshSysCartLevelList() {
		List<Integer> produceCartList=new ArrayList<Integer>();
		//2016年1月19日 YunbiaoTemp.cartAILvlMin从20-》14
		int cartAILvlMin=YunbiaoTemp.cartAILvlMin;
		int fuwuqilevel=cartAILvlMin;
		fuwuqilevel=(int) RankingMgr.inst.getTopJunzhuAvgLevel(50);
		fuwuqilevel=fuwuqilevel>=cartAILvlMin?fuwuqilevel:cartAILvlMin;
		for (int i = 0; i < YunbiaoTemp.cartAImax; i++) {
			int proLevel=(int) (cartAILvlMin+(fuwuqilevel-cartAILvlMin)*(1.0-1.0/(i+1))*0.8);
			produceCartList.add(proLevel);
		}
		Collections.sort(produceCartList,new Comparator<Integer>(){  
			public int compare(Integer arg0, Integer arg1) {  
				return arg1.compareTo(arg0);  
			}  
		});
		YaBiaoRobotProduceMgr.produceCartList=produceCartList;
	}
	
}