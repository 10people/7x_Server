package com.qx.yabiao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.junzhu.ChengHaoBean;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.world.YaBiaoScene;

/**
 * @Description 重启服务器恢复押镖场景的玩家马车
 *
 */
public class YaBiaoRestoreMgr {
	public static Logger log = LoggerFactory.getLogger(YaBiaoRestoreMgr.class.getSimpleName());
	public static YaBiaoRestoreMgr instance;
	
	public static YaBiaoRestoreMgr getInstance(){
		if (instance == null) {
			instance = new YaBiaoRestoreMgr();
		}
		return instance;
	}
	
	/**
	 * @Description 恢复押镖场景的玩家马车
	 */
	public  void backUpYaBiaoData() {
		List<YaBiaoBackUp> backupList=HibernateUtil.list(YaBiaoBackUp.class, "where backupFlag=true");
		log.info("开始恢复押镖场景的玩家马车,待处理数据数目--{}",backupList.size());
		try{
			for (YaBiaoBackUp bcUp : backupList) {
				long jzId=bcUp.jzId;
				JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
				if (jz == null) {
					log.error("恢复押镖场景的玩家--{}马车失败，玩家不存在",jzId);
					continue;
				}
				YaBiaoScene sc=getYaBiaoScene();
				AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jzId);
				int zhiWu=0;
				String lmName="***";
				if(member!= null&&member.lianMengId>0){
					zhiWu= member.title;
					AllianceBean acBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
					if (acBean != null) {
						lmName=acBean.name;
					}
				}
				int chenghao=-1;
				ChengHaoBean bean = HibernateUtil.find(ChengHaoBean.class, "where jzId="+jzId+" and state='U'");
				if(bean!=null){
					chenghao=bean.tid;
				}
				int pathId=bcUp.pathId;
				log.info("恢复押镖场景的玩家--{}马车,职务--{},称号--{}，联盟名字--{}，马车类型--{}，路线编号--{}",jzId,zhiWu, chenghao, lmName,  bcUp.horseType,pathId);
				YaBiaoHuoDongMgr.inst.initYaBiaoRobot(zhiWu, chenghao+"", lmName, jz, bcUp.horseType, sc, pathId);
			}
			log.info("完成恢复押镖场景的玩家马车");
		}catch(Exception e){
			log.error("恢复押镖场景的玩家马车异常。", e);
		}	
	}
	public  YaBiaoScene  getYaBiaoScene() {
		int scId = YaBiaoHuoDongMgr.inst.locateFakeSceneId();
		synchronized (YaBiaoHuoDongMgr.inst.yabiaoScenes) {
			YaBiaoScene sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
			if (sc == null) {// 没有场景
				synchronized (YaBiaoHuoDongMgr.inst.yabiaoScenes) {// 防止多次创建
					sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
					if (sc == null) {
						sc = new YaBiaoScene("YB#" + scId);
						sc.startMissionThread();
						YaBiaoHuoDongMgr.inst.yabiaoScenes.put(scId, sc);
					}
				}
			}
			return sc;
		}
	}
}